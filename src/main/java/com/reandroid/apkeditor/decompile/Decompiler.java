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
package com.reandroid.apkeditor.decompile;

import com.android.tools.smali.baksmali.Baksmali;
import com.android.tools.smali.baksmali.BaksmaliOptions;
import com.android.tools.smali.dexlib2.Opcodes;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.reandroid.apk.*;
import com.reandroid.apkeditor.Util;
import com.reandroid.archive2.Archive;
import com.reandroid.archive2.block.ApkSignatureBlock;
import com.reandroid.commons.command.ARGException;
import com.reandroid.commons.utils.log.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Decompiler {
    private final DecompileOptions options;
    private APKLogger mApkLogger;
    private Decompiler(DecompileOptions options){
        this.options=options;
    }
    public void run() throws IOException {
        log("Loading ...");
        ApkModule apkModule=ApkModule.loadApkFile(options.inputFile);
        apkModule.setAPKLogger(getAPKLogger());
        if(options.signaturesDirectory != null){
            dumpSignatureBlock();
            return;
        }
        String protect = Util.isProtected(apkModule);
        if(protect!=null){
            log(options.inputFile.getAbsolutePath());
            log(protect);
            return;
        }
        if(options.resDirName!=null){
            log("Renaming resources root dir: "+options.resDirName);
            apkModule.setResourcesRootDir(options.resDirName);
        }
        if(options.validateResDir){
            log("Validating resources dir ...");
            apkModule.validateResourcesDir();
        }
        if(DecompileOptions.TYPE_JSON.equals(options.type)){
            log("Decompiling to JSON ...");
            ApkJsonDecoder decoder=new ApkJsonDecoder(apkModule, options.splitJson);
            decoder.sanitizeFilePaths();
            decoder.writeToDirectory(options.outputFile);
        }else{
            log("Decompiling to XML ...");
            ApkModuleXmlDecoder xmlDecoder=new ApkModuleXmlDecoder(apkModule);
            xmlDecoder.sanitizeFilePaths();
            try {
                xmlDecoder.decodeTo(options.outputFile);
            } catch (Exception ex) {
                throw new IOException(ex.getMessage(), ex);
            }
        }
        if(options.disassembleDexFiles){
            log("Disassembling DEX files ...");
            disassembleDexFiles(apkModule);
        }
        log("Saved to: "+options.outputFile);
        log("Done");
    }
    private void disassembleDexFiles(ApkModule apkModule) {
        final List<DexFileInputSource> dexFiles = apkModule.listDexFiles();
        for (DexFileInputSource dexFileInputSource : dexFiles) {
            try {
                final String dexName = dexFileInputSource.getName();
                log("Disassembling " + dexName + " ...");
                final File smaliDir = new File(options.outputFile, "smali_" + dexName.replace(".dex", ""));
                if(!Baksmali.disassembleDexFile(
                        DexBackedDexFile.fromInputStream(
                                Opcodes.getDefault(),
                                new BufferedInputStream(dexFileInputSource.openStream())
                        ),smaliDir,
                        Runtime.getRuntime().availableProcessors(),
                        new BaksmaliOptions()
                )) {
                    log("Failed disassemble " + dexName);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void dumpSignatureBlock() throws IOException {
        log("Dumping signature blocks ...");
        Archive archive = new Archive(options.inputFile);
        ApkSignatureBlock apkSignatureBlock = archive.getApkSignatureBlock();
        if(apkSignatureBlock == null){
            log("Don't have signature block");
            return;
        }
        apkSignatureBlock.writeSplitRawToDirectory(options.signaturesDirectory);
        log("Signatures dumped to: " + options.signaturesDirectory);
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
                Logger.sameLine(getLogTag()+msg);
            }
        };
        return mApkLogger;
    }
    public static void execute(String[] args) throws ARGException, IOException {
        if(Util.isHelp(args)){
            throw new ARGException(DecompileOptions.getHelp());
        }
        DecompileOptions option=new DecompileOptions();
        option.parse(args);
        log("Decompiling ...\n"+option);
        File outDir;
        if(option.signaturesDirectory != null){
            outDir = option.signaturesDirectory;
        }else {
            outDir = option.outputFile;
        }
        Util.deleteEmptyDirectories(outDir);
        if(outDir.exists()){
            if(!option.force){
                throw new ARGException("Path already exists: "+outDir);
            }
            log("Deleting: "+outDir);
            Util.deleteDir(outDir);
        }
        Decompiler decompiler=new Decompiler(option);
        decompiler.run();
    }
    private static void log(String msg){
        Logger.i(getLogTag()+msg);
    }
    private static String getLogTag(){
        return "[DECOMPILE] ";
    }
    public static boolean isCommand(String command){
        if(Util.isEmpty(command)){
            return false;
        }
        command=command.toLowerCase().trim();
        return command.equals(ARG_SHORT) || command.equals(ARG_LONG);
    }
    public static final String ARG_SHORT="d";
    public static final String ARG_LONG="decode";
    public static final String DESCRIPTION="Decodes android resources binary to readable json/xml";
}
