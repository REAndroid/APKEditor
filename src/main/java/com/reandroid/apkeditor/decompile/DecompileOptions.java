package com.reandroid.apkeditor.decompile;

import com.reandroid.apkeditor.APKEditor;
import com.reandroid.apkeditor.Options;
import com.reandroid.apkeditor.compile.Builder;
import com.reandroid.apkeditor.utils.StringHelper;
import com.reandroid.commons.command.ARGException;

import java.io.File;

public class DecompileOptions extends Options {
    public boolean splitJson;
    public DecompileOptions(){
    }
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(" Input: "+inputFile);
        builder.append("\nOutput: "+outputFile);
        builder.append("\n Force: "+force);
        builder.append("\n Split: "+splitJson);
        builder.append("\n ---------------------------- ");
        return builder.toString();
    }
    public void parse(String[] args) throws ARGException {
        parseInput(args);
        parseOutput(args);
        parseForce(args);
        parseSplitResources(args);
        checkUnknownOptions(args);
    }
    private void parseForce(String[] args) throws ARGException {
        force=containsArg(ARG_force, true, args);
    }
    private void parseSplitResources(String[] args) throws ARGException {
        splitJson=containsArg(ARG_split_resources, true, args);
    }
    private void parseOutput(String[] args) throws ARGException {
        this.outputFile=null;
        File file=parseFile(ARG_output, args);
        if(file==null){
            file=getOutputFromInput(inputFile);
        }
        this.outputFile=file;
    }
    private File getOutputFromInput(File file){
        String name = file.getName();
        int i=name.lastIndexOf('.');
        if(i>0){
            name=name.substring(0, i);
        }
        name=name+"_out";
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
            throw new ARGException("Missing input file");
        }
        if(!file.isFile()){
            throw new ARGException("No such file: "+file);
        }
        this.inputFile=file;
    }
    public static String getHelp(){
        StringBuilder builder=new StringBuilder();
        builder.append(Decompiler.DESCRIPTION);
        builder.append("\nOptions:\n");
        String[][] table=new String[][]{
                new String[]{ARG_input, ARG_DESC_input},
                new String[]{ARG_output, ARG_DESC_output},
                new String[]{ARG_force, ARG_DESC_force},
                new String[]{ARG_split_resources, ARG_DESC_split_resources},
        };
        StringHelper.printTwoColumns(builder, "   ", 75, table);
        String jar = APKEditor.getJarName();
        builder.append("\nExample-1:");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Builder.ARG_SHORT).append(" ")
                .append(ARG_input).append(" path/to/input.apk");
        builder.append(" ").append(ARG_output).append(" path/to/out_dir");
        builder.append("\nExample-2:");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Builder.ARG_SHORT).append(" ")
                .append(ARG_input).append(" path/to/input.apk");
        builder.append("\nExample-3:");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Builder.ARG_SHORT).append(" ")
                .append(ARG_input).append(" path/to/input.apk").append(" ").append(ARG_split_resources);
        return builder.toString();
    }
    private static final String ARG_output="-o";
    private static final String ARG_DESC_output="output directory";
    private static final String ARG_input="-i";
    private static final String ARG_DESC_input="input file";
    private static final String ARG_split_resources="-split";
    private static final String ARG_DESC_split_resources="splits resources.arsc into multiple parts as per type entries (use this for large files)";
    private static final String ARG_force="-f";
    private static final String ARG_DESC_force="force delete output path";

}
