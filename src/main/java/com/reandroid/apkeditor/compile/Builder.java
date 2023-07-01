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
import com.reandroid.apkeditor.BaseCommand;
import com.reandroid.apkeditor.Util;
import com.reandroid.apkeditor.smali.SmaliCompiler;
import com.reandroid.archive2.Archive;
import com.reandroid.archive2.block.ApkSignatureBlock;
import com.reandroid.archive2.writer.ApkWriter;
import com.reandroid.commons.command.ARGException;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;

import java.io.File;
import java.io.IOException;

public class Builder extends BaseCommand<BuildOptions> {
    public Builder(BuildOptions options){
        super(options, "[BUILD] ");
    }
    @Override
    public void run() throws IOException {
        BuildOptions options = getOptions();
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
        logMessage("Restoring signatures ...");
        BuildOptions options = getOptions();
        Archive archive = new Archive(options.inputFile);
        ApkWriter apkWriter = new ApkWriter(options.outputFile, archive.mapEntrySource().values());
        apkWriter.setAPKLogger(this);
        ApkSignatureBlock apkSignatureBlock = new ApkSignatureBlock();
        apkSignatureBlock.scanSplitFiles(options.signaturesDirectory);
        apkWriter.setApkSignatureBlock(apkSignatureBlock);
        logMessage("Writing apk...");
        apkWriter.write();
        logMessage("Saved to: " + options.outputFile);
    }
    public void buildJson() throws IOException {
        logMessage("Scanning JSON directory ...");
        ApkModuleJsonEncoder encoder=new ApkModuleJsonEncoder();
        encoder.setApkLogger(this);

        SmaliCompiler smaliCompiler = new SmaliCompiler();
        smaliCompiler.setApkLogger(this);
        encoder.setDexEncoder(smaliCompiler);

        BuildOptions options = getOptions();

        encoder.scanDirectory(options.inputFile);
        ApkModule loadedModule = encoder.getApkModule();
        loadedModule.setAPKLogger(this);
        if(options.resDirName!=null){
            logMessage("Renaming resources root dir: "+options.resDirName);
            loadedModule.setResourcesRootDir(options.resDirName);
        }
        if(options.validateResDir){
            logMessage("Validating resources dir ...");
            loadedModule.validateResourcesDir();
        }
        logMessage("Writing apk...");
        loadedModule.getApkArchive().autoSortApkFiles();
        loadedModule.writeApk(options.outputFile, null);
        logMessage("Saved to: " + options.outputFile);
    }
    public void buildXml() throws IOException {
        logMessage("Scanning XML directory ...");
        ApkModuleXmlEncoder encoder=new ApkModuleXmlEncoder();
        encoder.setApkLogger(this);
        SmaliCompiler smaliCompiler = new SmaliCompiler();
        smaliCompiler.setApkLogger(this);
        encoder.setDexEncoder(smaliCompiler);
        ApkModule loadedModule = encoder.getApkModule();
        loadedModule.setAPKLogger(this);
        BuildOptions options = getOptions();
        loadedModule.setPreferredFramework(options.frameworkVersion);
        if(options.frameworks != null){
            for(File file : options.frameworks){
                loadedModule.addExternalFramework(file);
            }
        }
        encoder.scanDirectory(options.inputFile);
        loadedModule = encoder.getApkModule();
        logMessage("Writing apk...");
        loadedModule.writeApk(options.outputFile, null);
        logMessage("Saved to: " + options.outputFile);
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
        File outDir = option.outputFile;
        Util.deleteEmptyDirectories(outDir);
        Builder builder = new Builder(option);
        builder.logMessage("Building ...\n" + option.toString());
        if(outDir.exists()){
            if(!option.force){
                throw new ARGException("Path already exists: "+outDir);
            }
            builder.logMessage("Deleting: " + outDir);
            Util.deleteDir(outDir);
        }
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
