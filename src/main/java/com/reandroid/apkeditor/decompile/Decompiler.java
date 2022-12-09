package com.reandroid.apkeditor.decompile;

import com.reandroid.apkeditor.Util;
import com.reandroid.commons.command.ARGException;
import com.reandroid.commons.utils.log.Logger;
import com.reandroid.lib.apk.ApkJsonDecoder;
import com.reandroid.lib.apk.ApkModule;

import java.io.IOException;

public class Decompiler {
    private final DecompileOptions options;
    private Decompiler(DecompileOptions options){
        this.options=options;
    }
    public void run() throws IOException {
        log("Loading ...");
        ApkModule apkModule=ApkModule.loadApkFile(options.inputFile);
        log("Decompiling to json ...");
        ApkJsonDecoder serializer=new ApkJsonDecoder(apkModule, options.splitJson);
        serializer.writeToDirectory(options.outputFile);
        log("Done");
    }
    public static void execute(String[] args) throws ARGException, IOException {
        if(Util.isHelp(args)){
            throw new ARGException(DecompileOptions.getHelp());
        }
        DecompileOptions option=new DecompileOptions();
        option.parse(args);
        log("Decompiling ...\n"+option);
        Decompiler decompiler=new Decompiler(option);
        decompiler.run();
    }
    private static String getError(String err){
        return "Error: " + err+"\n Run with -h for help";
    }
    private static String getName(){
        return "Decompiler";
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
    public static final String DESCRIPTION="Decodes android binary to readable json string";
}
