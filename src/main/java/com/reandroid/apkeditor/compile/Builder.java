package com.reandroid.apkeditor.compile;

import com.reandroid.apkeditor.Util;
import com.reandroid.apkeditor.decompile.DecompileOptions;
import com.reandroid.archive.WriteProgress;
import com.reandroid.commons.command.ARGException;
import com.reandroid.commons.utils.log.Logger;
import com.reandroid.lib.apk.ApkJsonEncoder;
import com.reandroid.lib.apk.ApkModule;
import com.reandroid.lib.arsc.chunk.xml.AndroidManifestBlock;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;

public class Builder implements WriteProgress {
    private final BuildOptions options;
    public Builder(BuildOptions options){
        this.options=options;
    }
    public void run() throws IOException {
        log("Scanning directory ...");
        ApkJsonEncoder encoder=new ApkJsonEncoder();
        ApkModule loadedModule=encoder.scanDirectory(options.inputFile);
        log("Writing apk...");
        loadedModule.writeApk(options.outputFile, this);
        log("Done");
    }
    @Override
    public void onCompressFile(String path, int method, long length) {
        if("resources.arsc".equals(path)){
            path.trim();
        }
        StringBuilder builder=new StringBuilder();
        builder.append("Writing:");
        if(method == ZipEntry.STORED){
            builder.append(" method=STORED");
        }
        builder.append(" total=");
        builder.append(length);
        builder.append(" bytes : ");
        if(path.length()>50){
            path=path.substring(path.length()-50);
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
        option.inputFile=getInDir(option.inputFile);
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
    private static File getInDir(File dir) throws ARGException {
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
        File file=new File(dir, "uncompressed-files.json");
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
    public static final String DESCRIPTION="Builds android binary from json";
}
