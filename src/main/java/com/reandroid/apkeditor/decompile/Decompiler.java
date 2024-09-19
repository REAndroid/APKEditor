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

import com.reandroid.apk.*;
import com.reandroid.apkeditor.CommandExecutor;
import com.reandroid.apkeditor.Util;
import com.reandroid.apkeditor.smali.SmaliDecompiler;
import com.reandroid.archive.ArchiveFile;
import com.reandroid.archive.block.ApkSignatureBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.coder.xml.XmlCoder;

import java.io.IOException;

public class Decompiler extends CommandExecutor<DecompileOptions> {
    public Decompiler(DecompileOptions options){
        super(options, "[DECOMPILE] ");
    }
    @Override
    public void runCommand() throws IOException {
        DecompileOptions options = getOptions();
        delete(options.outputFile);
        logMessage("Loading ...");
        ApkModule apkModule=ApkModule.loadApkFile(this,
                options.inputFile, options.getFrameworks());
        apkModule.setPreferredFramework(options.frameworkVersion);
        if(options.signaturesDirectory != null){
            dumpSignatureBlock();
            return;
        }
        String protect = Util.isProtected(apkModule);
        if(protect!=null){
            logMessage(options.inputFile.getAbsolutePath());
            logMessage(protect);
            return;
        }
        if(options.resDirName!=null){
            logMessage("Renaming resources root dir: "+options.resDirName);
            apkModule.setResourcesRootDir(options.resDirName);
        }
        if(options.validateResDir){
            logMessage("Validating resources dir ...");
            apkModule.validateResourcesDir();
        }
        logMessage("Decompiling to " + options.type + " ...");

        ApkModuleDecoder decoder = getApkModuleDecoder(apkModule);
        decoder.decode(options.outputFile);
        logMessage("Saved to: "+options.outputFile);
    }
    private ApkModuleDecoder getApkModuleDecoder(ApkModule apkModule){

        DecompileOptions options = getOptions();
        ApkModuleDecoder decoder;
        if(DecompileOptions.TYPE_JSON.equals(options.type)){
            decoder = new ApkModuleJsonDecoder(apkModule, options.splitJson);
            decoder.setDexDecoder(getSmaliDecompiler(apkModule.getTableBlock()));
        }else if(DecompileOptions.TYPE_RAW.equals(options.type)){
            decoder = new ApkModuleRawDecoder(apkModule);
            decoder.setDexDecoder(getSmaliDecompiler(apkModule.getTableBlock()));
        }else{
            ApkModuleXmlDecoder xmlDecoder = new ApkModuleXmlDecoder(apkModule);
            xmlDecoder.setKeepResPath(options.keepResPath);
            decoder = xmlDecoder;
            XmlCoder.getInstance().getSetting().setLogger(this);
        }
        decoder.sanitizeFilePaths();
        decoder.setDexDecoder(getSmaliDecompiler(apkModule.getTableBlock()));
        return decoder;
    }
    private SmaliDecompiler getSmaliDecompiler(TableBlock tableBlock){
        if(getOptions().dex){
            return null;
        }
        SmaliDecompiler smaliDecompiler = new SmaliDecompiler(tableBlock, getOptions());
        smaliDecompiler.setApkLogger(this);
        return smaliDecompiler;
    }
    private void dumpSignatureBlock() throws IOException {
        logMessage("Dumping signature blocks ...");
        DecompileOptions options = getOptions();
        ArchiveFile archive = new ArchiveFile(options.inputFile);
        ApkSignatureBlock apkSignatureBlock = archive.getApkSignatureBlock();
        if(apkSignatureBlock == null){
            logMessage("Don't have signature block");
            return;
        }
        apkSignatureBlock.writeSplitRawToDirectory(options.signaturesDirectory);
        logMessage("Signatures dumped to: " + options.signaturesDirectory);
    }
}
