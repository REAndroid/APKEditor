package com.reandroid.apkeditor.decompile;

import com.reandroid.apkeditor.Util;
import com.reandroid.commons.command.ARGException;
import com.reandroid.commons.utils.FileUtil;
import com.reandroid.commons.utils.log.Logger;
import com.reandroid.lib.apk.APKLogger;
import com.reandroid.lib.apk.ApkJsonDecoder;
import com.reandroid.lib.apk.ApkModule;

import java.io.File;
import java.io.IOException;

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
        if(options.resDirName!=null){
            log("Renaming resources root dir: "+options.resDirName);
            apkModule.setResourcesRootDir(options.resDirName);
        }
        if(options.validateResDir){
            log("Validating resources dir ...");
            apkModule.validateResourcesDir();
        }
        log("Decompiling to json ...");
        ApkJsonDecoder decoder=new ApkJsonDecoder(apkModule, options.splitJson);
        decoder.writeToDirectory(options.outputFile);
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
        File outDir=option.outputFile;
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
    public static final String DESCRIPTION="Decodes android resources binary to readable json";
}
