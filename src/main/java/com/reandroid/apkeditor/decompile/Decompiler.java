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
import com.reandroid.apkeditor.APKEditor;
import com.reandroid.apkeditor.BaseCommand;
import com.reandroid.apkeditor.Util;
import com.reandroid.apkeditor.smali.SmaliDecompiler;
import com.reandroid.archive.ArchiveFile;
import com.reandroid.archive.block.ApkSignatureBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.coder.xml.XmlCoder;
import com.reandroid.common.DiagnosticMessage;
import com.reandroid.common.DiagnosticsReporter;
import com.reandroid.commons.command.ARGException;
import com.reandroid.dex.model.DexDirectory;
import com.reandroid.graph.ApkBuildOption;
import com.reandroid.graph.ApkBuilder;

import java.io.File;
import java.io.IOException;

public class Decompiler extends BaseCommand<DecompileOptions> {
    private Decompiler(DecompileOptions options){
        super(options, "[DECOMPILE] ");
    }
    @Override
    public void run() throws IOException {
        DecompileOptions options = getOptions();
        logMessage("Loading ...");
        ApkModule apkModule=ApkModule.loadApkFile(this,
                options.inputFile, options.frameworks);
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

        runFullApkRebuildExperimental(apkModule);

        ApkModuleDecoder decoder = getApkModuleDecoder(apkModule);
        decoder.decode(options.outputFile);
        logMessage("Saved to: "+options.outputFile);
    }
    private void runFullApkRebuildExperimental(ApkModule apkModule) throws IOException {
        if(!APKEditor.isExperimental()) {
            return;
        }
        if(!canLoadFullDex(apkModule)) {
            return;
        }
        DexDirectory directory = DexDirectory.fromZip(apkModule.getZipEntryMap());
        DiagnosticsReporter reporter = new DiagnosticsReporter() {
            @Override
            public void report(DiagnosticMessage diagnosticMessage) {
                DiagnosticMessage.Type type = diagnosticMessage.type();
                String log = diagnosticMessage.toString();
                if(type == DiagnosticMessage.Type.DEBUG) {
                    logVerbose(log);
                }else {
                    logMessage(log);
                }
            }
            @Override
            public boolean isVerboseEnabled() {
                return true;
            }
            @Override
            public boolean isDebugEnabled() {
                return true;
            }
        };
        logMessage("Rebuilding apk with new resource id generation ...");
        ApkBuilder builder = new ApkBuilder(apkModule, directory);
        builder.setReporter(reporter);
        ApkBuildOption apkBuildOption = builder.getBuildOption();

        File keepClassListFile = getOptions().keepClassListFile;
        if(keepClassListFile != null) {
            apkBuildOption.readKeepClassesList(keepClassListFile);
        }
        File keepResourceNameListFile = getOptions().keepResourceNameListFile;
        if(keepResourceNameListFile != null) {
            apkBuildOption.readKeepResourceNameList(keepResourceNameListFile);
        }

        // TODO: set values from user command args
        apkBuildOption.setMinifyClasses(true);
        apkBuildOption.setMinifyMethods(false);
        apkBuildOption.setMinifyFields(true);
        apkBuildOption.setMinifyResources(true);

        builder.apply();
        logMessage("Refreshing and merging ...");
        directory.refresh();

        // Cleans duplicate strings, codes, section entries ...
        directory.shrink();

        // Combines dex files to minimum possible number
        directory.merge();

        directory.refreshFull();
        directory.save();
        apkModule.putTag(DexDirectory.class, directory);
    }
    private boolean canLoadFullDex(ApkModule apkModule) {
        int CLASSES_LIMIT = 5;
        int size = apkModule.listDexFiles().size();
        logMessage("Total dex files: " + size);
        if(size > CLASSES_LIMIT) {
            logMessage("Huge classes your memory might not handle it, decoding separately without advanced features." +
                    " You can disable this restrictions by increasing \"CLASSES_LIMIT\" variable here on source code");
            return false;
        }
        return true;
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
            XmlCoder.getInstance().setLogger(this);
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
    public static void execute(String[] args) throws ARGException, IOException {
        if(Util.isHelp(args)){
            throw new ARGException(DecompileOptions.getHelp());
        }
        DecompileOptions option = new DecompileOptions();
        option.parse(args);
        File outDir;
        if(option.signaturesDirectory != null){
            outDir = option.signaturesDirectory;
        }else {
            outDir = option.outputFile;
        }
        Util.deleteEmptyDirectories(outDir);
        Decompiler decompiler = new Decompiler(option);
        decompiler.logVersion();
        decompiler.logMessage("Decompiling ...\n" + option);
        if(outDir.exists()){
            if(!option.force){
                throw new ARGException("Path already exists: "+outDir);
            }
            decompiler.logMessage("Deleting: " + outDir);
            Util.deleteDir(outDir);
        }
        decompiler.run();
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
