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

import com.android.tools.smali.dexlib2.Opcodes;
import com.android.tools.smali.dexlib2.writer.builder.DexBuilder;
import com.android.tools.smali.dexlib2.writer.io.FileDataStore;
import com.android.tools.smali.smali.smaliFlexLexer;
import com.android.tools.smali.smali.smaliParser;
import com.android.tools.smali.smali.smaliTreeWalker;
import com.reandroid.apkeditor.Util;
import com.reandroid.archive.WriteProgress;
import com.reandroid.archive2.Archive;
import com.reandroid.archive2.block.ApkSignatureBlock;
import com.reandroid.archive2.writer.ApkWriter;
import com.reandroid.commons.command.ARGException;
import com.reandroid.commons.utils.log.Logger;
import com.reandroid.apk.APKLogger;
import com.reandroid.apk.ApkJsonEncoder;
import com.reandroid.apk.ApkModule;
import com.reandroid.apk.ApkModuleXmlEncoder;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.xml.XMLException;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;

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
        if(options.assembleDexFiles) {
            assembleSmali();
        }
        if(options.isXml){
            buildXml();
        }else {
            buildJson();
        }
    }
    private void assembleSmali() {
        final File decodeDir = options.inputFile;
        final File rootDir = new File(decodeDir, "root");
        try {
            Files.walkFileTree(decodeDir.toPath(), new HashSet<>(), 1, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    final File smaliDir = dir.toFile();
                    if (smaliDir.isDirectory() && smaliDir.getName().startsWith("smali_classes")) {
                        final String smaliDirName = smaliDir.getName();
                        final String dexName = smaliDirName.replace("smali_", "") + ".dex";
                        log("Assembling " + smaliDirName + " ...");
                        build(smaliDir, new File(rootDir,  dexName));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void build(File smaliDir, File dexFile) throws IOException {
        final DexBuilder dexBuilder = new DexBuilder(Opcodes.getDefault());
        Files.walkFileTree(smaliDir.toPath(), new HashSet<>(), 1, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                final File smaliFile = dir.toFile();
                if (smaliFile.isFile() && smaliFile.getName().endsWith(".smali")) {
                    try {
                        if(!assembleSmaliFile(smaliFile, dexBuilder, 21, false, false)) {
                            log("Failed assemble smali file: " + smaliFile);
                        }
                    } catch (RecognitionException e) {
                        throw new RuntimeException(e);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        dexBuilder.writeTo(new FileDataStore(dexFile));
    }
    public static boolean assembleSmaliFile(File smaliFile, DexBuilder dexBuilder, int apiLevel, boolean verboseErrors, boolean printTokens) throws IOException, RecognitionException {
        final InputStream is = Files.newInputStream(smaliFile.toPath());
        final InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);

        final smaliFlexLexer lexer = new smaliFlexLexer(reader, apiLevel);
        (lexer).setSourceFile(smaliFile);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);

        final smaliParser parser = new smaliParser(tokens);
        parser.setApiLevel(apiLevel);
        parser.setVerboseErrors(verboseErrors);

        final smaliParser.smali_file_return result = parser.smali_file();

        if (parser.getNumberOfSyntaxErrors() > 0 || lexer.getNumberOfSyntaxErrors() > 0) {
            is.close();
            reader.close();
            log("Source: " + lexer.getSourceName() + ";Line: " + lexer.getLine() + ";Column: " + lexer.getColumn());
        }

        final CommonTree t = result.getTree();

        final CommonTreeNodeStream treeStream = new CommonTreeNodeStream(t);
        treeStream.setTokenStream(tokens);

        final smaliTreeWalker dexGen = new smaliTreeWalker(treeStream);
        dexGen.setApiLevel(apiLevel);
        dexGen.setVerboseErrors(verboseErrors);
        dexGen.setDexBuilder(dexBuilder);
        dexGen.smali_file();

        is.close();
        reader.close();

        if (dexGen.getNumberOfSyntaxErrors() != 0) {
            log("Source: " + lexer.getSourceName() + ";Line: " + lexer.getLine() + ";Column: " + lexer.getColumn());
        }
        return dexGen.getNumberOfSyntaxErrors() == 0;
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
        ApkJsonEncoder encoder=new ApkJsonEncoder();
        encoder.setAPKLogger(getAPKLogger());
        ApkModule loadedModule=encoder.scanDirectory(options.inputFile);
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
        loadedModule.getApkArchive().sortApkFiles();
        loadedModule.writeApk(options.outputFile, this);
        log("Built to: "+options.outputFile);
        log("Done");
    }
    public void buildXml() throws IOException {
        log("Scanning XML directory ...");
        ApkModuleXmlEncoder encoder=new ApkModuleXmlEncoder();
        encoder.setApkLogger(getAPKLogger());
        ApkModule loadedModule;
        try {
            encoder.scanDirectory(options.inputFile);
            loadedModule=encoder.getApkModule();
        } catch (XMLException ex) {
            throw new IOException(ex.getMessage(), ex);
        }
        log("Writing apk...");
        loadedModule.getApkArchive().sortApkFiles();
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
