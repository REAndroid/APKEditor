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

import com.reandroid.apkeditor.APKEditor;
import com.reandroid.apkeditor.Options;
import com.reandroid.apkeditor.utils.StringHelper;
import com.reandroid.commons.command.ARGException;

import java.io.File;

public class DecompileOptions extends Options {
    public boolean splitJson;
    public boolean validateResDir;
    public String resDirName;
    public boolean keepResPath;
    public boolean dex;
    public DecompileOptions(){
        type=TYPE_XML;
    }
    @Override
    public void parse(String[] args) throws ARGException {
        parseInput(args);
        parseType(args, type);
        parseOutput(args);
        parseSplitResources(args);
        parseDex(args);
        parseKeepResPath(args);
        parseResDirName(args);
        parseValidateResDir(args);
        parseSignaturesDir(args);
        if(signaturesDirectory == null && type == null){
            type = TYPE_XML;
        }
        super.parse(args);
    }
    private void parseKeepResPath(String[] args) throws ARGException {
        keepResPath = containsArg(ARG_keep_res_path, true, args);
    }
    private void parseValidateResDir(String[] args) throws ARGException {
        validateResDir=containsArg(ARG_validate_res_dir, true, args);
    }
    private void parseResDirName(String[] args) throws ARGException {
        this.resDirName=parseArgValue(ARG_resDir, true, args);
    }
    private void parseSplitResources(String[] args) throws ARGException {
        splitJson=containsArg(ARG_split_resources, true, args);
    }
    private void parseDex(String[] args) throws ARGException {
        dex = containsArg(ARG_dex, true, args);
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
        name=name+"_decompile_"+type;
        File dir=file.getParentFile();
        if(dir==null){
            return new File(name);
        }
        return new File(dir, name);
    }
    private void parseInput(String[] args) throws ARGException {
        this.inputFile=null;
        File file = parseFile(ARG_input, args);
        if(file==null){
            throw new ARGException("Missing input file");
        }
        if(!file.isFile()){
            throw new ARGException("No such file: "+file);
        }
        this.inputFile=file;
    }

    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append("   Input: ").append(inputFile);
        File out;
        if(signaturesDirectory != null){
            out = signaturesDirectory;
        }else {
            out = outputFile;
        }
        builder.append("\n Output: ").append(out);
        if(resDirName!=null){
            builder.append("\nres dir: ").append(resDirName);
        }
        if(validateResDir){
            builder.append("\n Validate res dir name: true");
        }
        if(force){
            builder.append("\n Force: true");
        }
        if(keepResPath){
            builder.append("\n Keep res path: true");
        }
        if(frameworkVersion != null){
            builder.append("\nFramework version: ").append(frameworkVersion);
        }
        builder.append("\n Type: ").append(type);
        if(!TYPE_XML.equals(type) && signaturesDirectory == null){
            builder.append("\n Split: ").append(splitJson);
        }
        if(frameworks != null && frameworks.length > 0){
            builder.append("\nFrameworks:");
            for(File file : frameworks){
                builder.append("\n           ");
                builder.append(file);
            }
        }
        builder.append("\n ---------------------------- ");
        return builder.toString();
    }
    public static String getHelp(){
        StringBuilder builder=new StringBuilder();
        builder.append(Decompiler.DESCRIPTION);
        builder.append("\nOptions:\n");
        String[][] table=new String[][]{
                new String[]{ARG_input, ARG_DESC_input},
                new String[]{ARG_output, ARG_DESC_output},
                new String[]{ARG_framework_version, ARG_DESC_framework_version},
                new String[]{ARG_framework, ARG_DESC_framework},
                new String[]{ARG_sig, ARG_DESC_sig},
                new String[]{ARG_type, ARG_DESC_type},
                new String[]{ARG_resDir, ARG_DESC_resDir}
        };
        StringHelper.printTwoColumns(builder, "   ", Options.PRINT_WIDTH, table);
        builder.append("\nFlags:\n");
        table=new String[][]{
                new String[]{ARG_dex, ARG_DESC_dex},
                new String[]{ARG_force, ARG_DESC_force},
                new String[]{ARG_keep_res_path, ARG_DESC_keep_res_path},
                new String[]{ARG_split_resources, ARG_DESC_split_resources},
                new String[]{ARG_validate_res_dir, ARG_DESC_validate_res_dir}
        };
        StringHelper.printTwoColumns(builder, "   ", Options.PRINT_WIDTH, table);
        String jar = APKEditor.getJarName();
        builder.append("\n\nExample-1:");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Decompiler.ARG_SHORT).append(" ")
                .append(ARG_input).append(" path/to/input.apk");
        builder.append(" ").append(ARG_output).append(" path/to/out_dir");
        builder.append("\nExample-2:");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Decompiler.ARG_SHORT).append(" ")
                .append(ARG_input).append(" path/to/input.apk");
        builder.append("\nExample-3:");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Decompiler.ARG_SHORT).append(" ")
                .append(ARG_input).append(" path/to/input.apk").append(" ").append(ARG_split_resources);
        builder.append("\nExample-4: (XML)");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Decompiler.ARG_SHORT).append(" ")
                .append(ARG_type).append(" ").append(TYPE_XML).append(" ").append(ARG_input)
                .append(" path/to/input.apk");
        builder.append("\nExample-5: (signatures)");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Decompiler.ARG_SHORT).append(" ")
                .append(ARG_type).append(" ").append(TYPE_SIG).append(" ").append(ARG_input)
                .append(" path/to/input.apk")
                .append(" ").append(ARG_sig).append(" path/to/signatures_dir");
        builder.append("\nExample-6: (framework)");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Decompiler.ARG_SHORT).append(" ")
                .append(ARG_input).append(" input.apk");
        builder.append(" ").append(ARG_framework).append(" framework-res.apk");
        builder.append(" ").append(ARG_framework).append(" platforms/android-32/android.jar");
        return builder.toString();
    }
    private static final String ARG_split_resources="-split-json";
    private static final String ARG_DESC_split_resources="splits resources.arsc into multiple parts as per type entries (use this for large files)";

    private static final String ARG_DESC_type = "Decode types: \n  1) json \n  2) xml \n  3) raw \n  4) sig \n default=" + TYPE_XML;


    private static final String ARG_keep_res_path = "-keep-res-path";
    private static final String ARG_DESC_keep_res_path = "Keeps original res/* file paths:" + "\n  *Applies only when decoding to xml" +
            "\n  *All res/* files will be placed on dir <res-files>\n  *The relative paths will be linked to values/*xml";


    private static final String ARG_dex = "-dex";
    private static final String ARG_DESC_dex = "Copy raw dex files / skip smali";


}
