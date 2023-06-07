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
package com.reandroid.apkeditor.compile;

import com.reandroid.apk.*;
import com.reandroid.apkeditor.Util;
import com.reandroid.archive.WriteProgress;
import com.reandroid.archive2.Archive;
import com.reandroid.archive2.block.ApkSignatureBlock;
import com.reandroid.archive2.writer.ApkWriter;
import com.reandroid.commons.command.ARGException;
import com.reandroid.commons.utils.log.Logger;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;

import java.io.File;
import java.io.IOException;

public class Builder implements WriteProgress {
    private final BuildOptions options;
    private APKLogger mApkLogger;
    public Builder(BuildOptions options){
        this.options=options;
    }
    public void run() throws IOException {
        if(options.signaturesDirectory != null && options.inputFile.isFile()){
            restoreSignatures();
            return;
        }
        if(options.isXml){
            buildXml();
        }else {
            buildJson();
        }
    }
    private void restoreSignatures() throws IOException {
        log("Restoring signatures ...");
        Archive archive = new Archive(options.inputFile);
        ApkWriter apkWriter = new ApkWriter(options.outputFile, archive.mapEntrySource().values());
        apkWriter.setAPKLogger(getAPKLogger());
        ApkSignatureBlock apkSignatureBlock = new ApkSignatureBlock();
        apkSignatureBlock.scanSplitFiles(options.signaturesDirectory);
        apkWriter.setApkSignatureBlock(apkSignatureBlock);
        log("Writing apk...");
        apkWriter.write();
        log("Built to: "+options.outputFile);
        log("Done");
    }
    public void buildJson() throws IOException {
        log("Scanning JSON directory ...");
        ApkModuleJsonEncoder encoder=new ApkModuleJsonEncoder();
        encoder.setApkLogger(getAPKLogger());
        encoder.scanDirectory(options.inputFile);
        ApkModule loadedModule = encoder.getApkModule();
        loadedModule.setAPKLogger(getAPKLogger());
        if(options.resDirName!=null){
            log("Renaming resources root dir: "+options.resDirName);
            loadedModule.setResourcesRootDir(options.resDirName);
        }
        if(options.validateResDir){
            log("Validating resources dir ...");
            loadedModule.validateResourcesDir();
        }
        log("Writing apk...");
        loadedModule.getApkArchive().autoSortApkFiles();
        loadedModule.writeApk(options.outputFile, this);
        log("Built to: "+options.outputFile);
        log("Done");
    }
    public void buildXml() throws IOException {
        log("Scanning XML directory ...");
        ApkModuleXmlEncoder encoder=new ApkModuleXmlEncoder();
        encoder.setApkLogger(getAPKLogger());
        ApkModule loadedModule = encoder.getApkModule();
        loadedModule.setPreferredFramework(options.frameworkVersion);
        encoder.scanDirectory(options.inputFile);
        loadedModule = encoder.getApkModule();
        log("Writing apk...");
        loadedModule.writeApk(options.outputFile, null);
        log("Built to: "+options.outputFile);
        log("Done");
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
                if(msg.length()>50){
                    msg=msg.substring(msg.length()-50);
                }
                Logger.sameLine(getLogTag()+msg);
            }
        };
        return mApkLogger;
    }
    @Override
    public void onCompressFile(String path, int method, long length) {
        StringBuilder builder=new StringBuilder();
        builder.append("Writing:");
        if(path.length()>30){
            path=path.substring(path.length()-30);
        }
        builder.append(path);
        logSameLine(builder.toString());
    }
    public static void execute(String[] args) throws ARGException, IOException {
        if(Util.isHelp(args)){
            throw new ARGException(BuildOptions.getHelp());
        }
        BuildOptions option=new BuildOptions();
        option.parse(args);
        if(isJsonInDir(option.inputFile)){
            option.inputFile = getJsonInDir(option.inputFile);
        }else if (isXmlInDir(option.inputFile)){
            option.isXml=true;
        }else if(option.signaturesDirectory == null){
            throw new ARGException("Not xml/json directory: "+option.inputFile);
        }
        File outDir=option.outputFile;
        Util.deleteEmptyDirectories(outDir);
        if(outDir.exists()){
            if(!option.force){
                throw new ARGException("Path already exists: "+outDir);
            }
            log("Deleting: "+outDir);
            Util.deleteDir(outDir);
        }
        log("Building ...\n"+option);
        Builder builder=new Builder(option);
        builder.run();
    }
    private static boolean isXmlInDir(File dir){
        File manifest=new File(dir, AndroidManifestBlock.FILE_NAME);
        if(!manifest.isFile()){
            manifest=new File(dir, AndroidManifestBlock.FILE_NAME_BIN);
        }
        return manifest.isFile();
    }
    private static boolean isJsonInDir(File dir) {
        if(isModuleDir(dir)){
            return true;
        }
        File[] files=dir.listFiles();
        if(files==null){
            return false;
        }
        for(File file:files){
            if(isModuleDir(file)){
                return true;
            }
        }
        return false;
    }
    private static File getJsonInDir(File dir) throws ARGException {
        if(isModuleDir(dir)){
            return dir;
        }
        File[] files=dir.listFiles();
        if(files==null){
            throw new ARGException("Empty directory: "+dir);
        }
        for(File file:files){
            if(isModuleDir(file)){
                return file;
            }
        }
        throw new ARGException("Invalid directory: "+dir+", missing file \"uncompressed-files.json\"");
    }
    private static boolean isModuleDir(File dir){
        if(!dir.isDirectory()){
            return false;
        }
        File manifest=new File(dir,AndroidManifestBlock.FILE_NAME+".json");
        if(manifest.isFile()){
            return true;
        }
        File file=new File(dir, "AndroidManifest.xml.json");
        return file.isFile();
    }
    private static void logSameLine(String msg){
        Logger.sameLine(getLogTag()+msg);
    }
    private static void log(String msg){
        Logger.i(getLogTag()+msg);
    }
    private static String getLogTag(){
        return "[BUILD] ";
    }
    public static boolean isCommand(String command){
        if(Util.isEmpty(command)){
            return false;
        }
        command=command.toLowerCase().trim();
        return command.equals(ARG_SHORT) || command.equals(ARG_LONG);
    }
    public static final String ARG_SHORT="b";
    public static final String ARG_LONG="build";
    public static final String DESCRIPTION="Builds android binary from json/xml";
}
