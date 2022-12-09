package com.reandroid.apkeditor.compile;

import com.reandroid.apkeditor.Util;
import com.reandroid.apkeditor.decompile.Decompiler;
import com.reandroid.commons.command.ARGException;

import java.io.File;

public class BuildOptions {
    public File inputFile;
    public File outputFile;
    public BuildOptions(){
    }
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(" Input: "+inputFile);
        builder.append("\nOutput: "+outputFile);
        return builder.toString();
    }
    public void parse(String[] args) throws ARGException {
        parseInput(args);
        parseOutput(args);
    }
    private void parseOutput(String[] args) throws ARGException {
        this.outputFile=null;
        File file=parseFile(ARG_output, args);
        if(file==null){
            file=getOutputApkFromInput(inputFile);
        }
        this.outputFile=file;
    }
    private File getOutputApkFromInput(File file){
        String name = file.getName();
        name=name+"_out.apk";
        File dir=file.getParentFile();
        if(dir==null){
            return new File(name);
        }
        return new File(dir, name);
    }
    private void parseInput(String[] args) throws ARGException {
        this.inputFile=null;
        File file=parseFile(ARG_input, args);
        if(file==null){
            throw new ARGException("Missing input directory");
        }
        if(!file.isDirectory()){
            throw new ARGException("No such directory: "+file);
        }
        this.inputFile=file;
    }
    private String parseArgValue(String argSwitch, boolean ignore_case, String[] args) throws ARGException {
        if(ignore_case){
            argSwitch=argSwitch.toLowerCase();
        }
        int max=args.length;
        for(int i=0;i<max;i++){
            String s=args[i];
            if(s==null){
                continue;
            }
            s=s.trim();
            String tmpArg=s;
            if(ignore_case){
                tmpArg=tmpArg.toLowerCase();
            }
            if(tmpArg.equals(argSwitch)){
                int i2=i+1;
                if(i2>=max){
                    throw new ARGException("Missing value near: \""+s+"\"");
                }
                String value=args[i2];
                if(Util.isEmpty(value)){
                    throw new ARGException("Missing value near: \""+s+"\"");
                }
                value=value.trim();
                args[i]=null;
                args[i2]=null;
                return value;
            }
        }
        return null;
    }
    private File parseFile(String argSwitch, String[] args) throws ARGException {
        int max=args.length;
        for(int i=0;i<max;i++){
            String s=args[i];
            if(s==null){
                continue;
            }
            s=s.trim();
            if(s.equals(argSwitch)){
                int i2=i+1;
                if(i2>=max){
                    throw new ARGException("Missing path near: \""+argSwitch+"\"");
                }
                String path=args[i2];
                if(Util.isEmpty(path)){
                    throw new ARGException("Missing path near: \""+argSwitch+"\"");
                }
                path=path.trim();
                args[i]=null;
                args[i2]=null;
                return new File(path);
            }
        }
        return null;
    }

    private boolean containsArg(String argSwitch, boolean ignore_case, String[] args) throws ARGException {
        if(ignore_case){
            argSwitch=argSwitch.toLowerCase();
        }
        int max=args.length;
        for(int i=0;i<max;i++){
            String s=args[i];
            if(s==null){
                continue;
            }
            s=s.trim();
            if(ignore_case){
                s=s.toLowerCase();
            }
            if(s.equals(argSwitch)){
                args[i]=null;
                return true;
            }
        }
        return false;
    }
    public static String getHelp(){
        StringBuilder builder=new StringBuilder();
        builder.append(Decompiler.DESCRIPTION);
        builder.append("\nOptions:");
        builder.append("\n ").append(ARG_input).append("    ").append(ARG_DESC_input);
        builder.append("\n ").append(ARG_output).append("    ").append(ARG_DESC_output);
        builder.append("\nExample-1:");
        builder.append("\n ").append(ARG_input).append(" path/to/input_dir");
        builder.append(" ").append(ARG_output).append(" path/to/out.apk");
        builder.append("\nExample-2:");
        builder.append("\n ").append(ARG_input).append(" path/to/input_dir");
        return builder.toString();
    }
    private static final String ARG_output="-o";
    private static final String ARG_DESC_output="output file";
    private static final String ARG_input="-i";
    private static final String ARG_DESC_input="input directory";
}
