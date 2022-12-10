package com.reandroid.apkeditor.compile;

import com.reandroid.apkeditor.APKEditor;
import com.reandroid.apkeditor.Options;
import com.reandroid.apkeditor.utils.StringHelper;
import com.reandroid.commons.command.ARGException;

import java.io.File;

public class BuildOptions extends Options {
    public BuildOptions(){
    }
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(" Input: "+inputFile);
        builder.append("\nOutput: "+outputFile);
        builder.append("\n Force: "+force);
        builder.append("\n ---------------------------- ");
        return builder.toString();
    }
    public void parse(String[] args) throws ARGException {
        parseInput(args);
        parseOutput(args);
        parseForce(args);
        checkUnknownOptions(args);
    }
    private void parseForce(String[] args) throws ARGException {
        force=containsArg(ARG_force, true, args);
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
    public static String getHelp(){
        StringBuilder builder=new StringBuilder();
        builder.append(Builder.DESCRIPTION);
        builder.append("\nOptions:\n");
        String[][] table=new String[][]{
                new String[]{ARG_input, ARG_DESC_input},
                new String[]{ARG_output, ARG_DESC_output},
                new String[]{ARG_force, ARG_DESC_force}
        };
        StringHelper.printTwoColumns(builder, "   ", 75, table);
        String jar = APKEditor.getJarName();
        builder.append("\nExample-1:");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Builder.ARG_SHORT).append(" ")
                .append(ARG_input).append(" path/to/input_dir");
        builder.append(" ").append(ARG_output).append(" path/to/out.apk");
        builder.append("\nExample-2:");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Builder.ARG_SHORT).append(" ")
                .append(ARG_input).append(" path/to/input_dir");
        return builder.toString();
    }
    private static final String ARG_output="-o";
    private static final String ARG_DESC_output="output file";
    private static final String ARG_input="-i";
    private static final String ARG_DESC_input="input directory";
    private static final String ARG_force="-f";
    private static final String ARG_DESC_force="force delete output path";
}
