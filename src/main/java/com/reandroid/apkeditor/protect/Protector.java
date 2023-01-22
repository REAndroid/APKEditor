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

import com.reandroid.apkeditor.BaseCommand;
import com.reandroid.apkeditor.Util;
import com.reandroid.archive.WriteProgress;
import com.reandroid.archive.ZipAlign;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.commons.command.ARGException;
import com.reandroid.commons.utils.log.Logger;
import com.reandroid.apk.*;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.value.EntryBlock;
import com.reandroid.arsc.value.ResConfig;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;

public class Protector extends BaseCommand implements WriteProgress {
    private final ProtectorOptions options;
    private APKLogger mApkLogger;
    public Protector(ProtectorOptions options){
        this.options=options;
    }
    public void run() throws IOException {
        log("Loading apk file ...");
        ApkModule module=ApkModule.loadApkFile(options.inputFile);
        module.setAPKLogger(getAPKLogger());
        confuseAndroidManifest(module);
        log("Protecting files ..");
        confuseResDir(module);
        log("Protecting resource table ..");
        confuseByteOffset(module);
        module.getTableBlock().refresh();
        log("Writing apk ...");
        module.writeApk(options.outputFile, this);
        log("Zip align ...");
        ZipAlign.align4(options.outputFile);
        log("Saved to: "+options.outputFile);
        log("Done");
    }
    private void confuseAndroidManifest(ApkModule apkModule) throws IOException {
        log("Confusing AndroidManifest ...");
        AndroidManifestBlock manifestBlock = apkModule.getAndroidManifestBlock();
        manifestBlock.setAttributesUnitSize(24, true);
        manifestBlock.refresh();
    }
    private void confuseByteOffset(ApkModule apkModule) throws IOException {
        TableBlock tableBlock=apkModule.getTableBlock();
        for(PackageBlock packageBlock:tableBlock.listPackages()){
            for(SpecTypePair specTypePair:packageBlock.listAllSpecTypePair()){
                for(ResConfig resConfig:specTypePair.listResConfig()){
                    resConfig.trimToSize(ResConfig.SIZE_16);
                }
            }
        }
    }
    private void confuseResDir(ApkModule apkModule) throws IOException {
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
            EntryBlock entryBlock = resFile.pickOne();
            // TODO: make other solution to decide user which types/dirs to ignore
            if(entryBlock!=null && "font".equals(entryBlock.getTypeName())){
                log("  Ignored: "+path);
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

    @Override
    public void onCompressFile(String path, int method, long length) {
        StringBuilder builder=new StringBuilder();
        builder.append("Writing:");
        if(method == ZipEntry.STORED){
            builder.append(" method=STORED");
        }
        builder.append(" total=");
        builder.append(length);
        builder.append(" bytes : ");
        if(path.length()>30){
            path=path.substring(path.length()-30);
        }
        builder.append(path);
        logSameLine(builder.toString());
    }
    private APKLogger getAPKLogger(){
        if(mApkLogger!=null){
            return mApkLogger;
        }
        mApkLogger = new APKLogger() {
            @Override
            public void logMessage(String msg) {
                Logger.i(getLogTag()+msg);
            }
            @Override
            public void logError(String msg, Throwable tr) {
                Logger.e(getLogTag()+msg, tr);
            }
            @Override
            public void logVerbose(String msg) {
                if(msg.length()>30){
                    msg=msg.substring(msg.length()-30);
                }
                Logger.sameLine(getLogTag()+msg);
            }
        };
        return mApkLogger;
    }
    public static void execute(String[] args) throws ARGException, IOException {
        if(Util.isHelp(args)){
            throw new ARGException(ProtectorOptions.getHelp());
        }
        ProtectorOptions option=new ProtectorOptions();
        option.parse(args);
        File outFile=option.outputFile;
        Util.deleteEmptyDirectories(outFile);
        if(outFile.exists()){
            if(!option.force){
                throw new ARGException("Path already exists: "+outFile);
            }
            log("Deleting: "+outFile);
            Util.deleteDir(outFile);
        }
        log("Protecting ...\n"+option);
        Protector protector=new Protector(option);
        protector.run();
    }
    private static void logSameLine(String msg){
        Logger.sameLine(getLogTag()+msg);
    }
    private static void log(String msg){
        Logger.i(getLogTag()+msg);
    }
    private static String getLogTag(){
        return "[PROTECT] ";
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
