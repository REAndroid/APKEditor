 /*
  *  Copyright (C) 2022 github.com/REAndroid
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.reandroid.apkeditor.protect;

import com.reandroid.apkeditor.APKEditor;
import com.reandroid.apkeditor.BaseCommand;
import com.reandroid.apkeditor.Util;
import com.reandroid.arsc.ARSCLib;
import com.reandroid.arsc.chunk.UnknownChunk;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.item.ByteArray;
import com.reandroid.arsc.item.FixedLengthString;
import com.reandroid.commons.command.ARGException;
import com.reandroid.apk.*;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;

public class Protector extends BaseCommand<ProtectorOptions> {
    public Protector(ProtectorOptions options){
        super(options, "[PROTECT] ");
    }

    @Override
    public void run() throws IOException {
        logMessage("Loading apk ...");
        ProtectorOptions options = getOptions();
        ApkModule module = ApkModule.loadApkFile(this, options.inputFile);
        module.setLoadDefaultFramework(false);
        String protect = Util.isProtected(module);
        if(protect != null){
            logMessage(options.inputFile.getAbsolutePath());
            logMessage(protect);
            return;
        }
        confuseAndroidManifest(module);
        logMessage("Protecting files ..");
        confuseResDir(module);
        logMessage("Protecting resource table ..");
        confuseByteOffset(module);
        confuseResourceTable(module);
        Util.addApkEditorInfo(module, Util.EDIT_TYPE_PROTECTED);
        module.getTableBlock().refresh();
        logMessage("Writing apk ...");
        module.writeApk(options.outputFile);
        module.close();
        logMessage("Saved to: " + options.outputFile);
    }
    private void confuseAndroidManifest(ApkModule apkModule) {
        ProtectorOptions options = getOptions();
        if(options.skipManifest){
            logMessage("Skip AndroidManifest");
            return;
        }
        logMessage("Confusing AndroidManifest ...");
        AndroidManifestBlock manifestBlock = apkModule.getAndroidManifest();
        manifestBlock.setAttributesUnitSize(24, true);
        manifestBlock.refresh();
    }
    private void confuseByteOffset(ApkModule apkModule) {
        logMessage("METHOD-1 Protecting resource table ..");
        TableBlock tableBlock=apkModule.getTableBlock();
        for(PackageBlock packageBlock:tableBlock.listPackages()){
            for(SpecTypePair specTypePair:packageBlock.listSpecTypePairs()){
                for(ResConfig resConfig:specTypePair.listResConfig()){
                    resConfig.trimToSize(ResConfig.SIZE_16);
                }
            }
        }
        tableBlock.refresh();
    }
    private void confuseResourceTable(ApkModule apkModule) {
        logMessage("METHOD-2 Protecting resource table ..");
        TableBlock tableBlock=apkModule.getTableBlock();
        UnknownChunk unknownChunk = new UnknownChunk();
        FixedLengthString fixedLengthString = new FixedLengthString(256);
        fixedLengthString.set(APKEditor.getRepo());
        ByteArray extra = unknownChunk.getHeaderBlock().getExtraBytes();
        byte[] bts = fixedLengthString.getBytes();
        extra.setSize(bts.length);
        extra.putByteArray(0, bts);
        fixedLengthString.set(ARSCLib.getRepo());
        extra = unknownChunk.getBody();
        bts = fixedLengthString.getBytes();
        extra.setSize(bts.length);
        extra.putByteArray(0, bts);
        fixedLengthString.set(ARSCLib.getRepo());
        unknownChunk.refresh();
        tableBlock.getFirstPlaceHolder().setItem(unknownChunk);
        tableBlock.refresh();
    }
    private void confuseResDir(ApkModule apkModule) {
        logMessage("Protecting files ..");
        String[] dirNames=new String[]{
                "AndroidManifest.xml",
                "resources.arsc",
                "classes.dex"
        };
        UncompressedFiles uf = apkModule.getUncompressedFiles();
        int i=0;
        for(ResFile resFile:apkModule.listResFiles()){
            if(i>=dirNames.length){
                i=0;
            }
            int method=resFile.getInputSource().getMethod();
            String path = resFile.getFilePath();
            Entry entryBlock = resFile.pickOne();
            // TODO: make other solution to decide user which types/dirs to ignore
            if(entryBlock!=null && "font".equals(entryBlock.getTypeName())){
                logMessage("  Ignored: "+path);
                continue;
            }
            String pathNew = ApkUtil.replaceRootDir(path, dirNames[i]);
            if(method==ZipEntry.STORED){
                uf.replacePath(path, pathNew);
            }
            resFile.setFilePath(pathNew);
            i++;
        }
    }
    public static void execute(String[] args) throws ARGException, IOException {
        if(Util.isHelp(args)){
            throw new ARGException(ProtectorOptions.getHelp());
        }
        ProtectorOptions option = new ProtectorOptions();
        option.parse(args);
        File outFile = option.outputFile;
        Util.deleteEmptyDirectories(outFile);
        Protector protector = new Protector(option);
        protector.logVersion();
        if(outFile.exists()){
            if(!option.force){
                throw new ARGException("Path already exists: "+outFile);
            }
            protector.logMessage("Deleting: " + outFile);
            Util.deleteDir(outFile);
        }
        protector.logMessage("Protecting ...\n" + option);
        protector.run();
    }

    public static boolean isCommand(String command){
        if(Util.isEmpty(command)){
            return false;
        }
        command=command.toLowerCase().trim();
        return command.equals(ARG_SHORT) || command.equals(ARG_LONG);
    }
    public static final String ARG_SHORT="p";
    public static final String ARG_LONG="protect";
    public static final String DESCRIPTION="Protects/Obfuscates apk resource";
}
