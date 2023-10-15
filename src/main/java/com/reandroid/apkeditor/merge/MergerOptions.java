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
package com.reandroid.apkeditor.merge;

import com.reandroid.apkeditor.APKEditor;
import com.reandroid.apkeditor.Options;
import com.reandroid.apkeditor.utils.StringHelper;
import com.reandroid.commons.command.ARGException;

import java.io.File;

public class MergerOptions extends Options {
    public boolean validateResDir;
    public boolean cleanMeta;
    public boolean keepExtractNativeLibs;
    public String resDirName;
    public MergerOptions(){
        super();
    }
    @Override
    public void parse(String[] args) throws ARGException {
        parseInput(args);
        parseOutput(args);
        parseResDirName(args);
        parseValidateResDir(args);
        parseCleanMeta(args);
        parseKeepExtractNativeLibs(args);
        super.parse(args);
    }
    private void parseValidateResDir(String[] args) throws ARGException {
        validateResDir=containsArg(ARG_validate_res_dir, true, args);
    }
    private void parseCleanMeta(String[] args) throws ARGException {
        cleanMeta = containsArg(ARG_cleanMeta, true, args);
    }
    private void parseResDirName(String[] args) throws ARGException {
        this.resDirName=parseArgValue(ARG_resDir, true, args);
    }
    private void parseOutput(String[] args) throws ARGException {
        this.outputFile=null;
        File file=parseFile(ARG_output, args);
        if(file==null){
            file=getOutputApkFromInput(inputFile);
        }
        this.outputFile=file;
    }
    private void parseKeepExtractNativeLibs(String[] args) throws ARGException {
        this.keepExtractNativeLibs=containsArg(ARG_keepExtractNativeLibs, false, args);
    }
    private File getOutputApkFromInput(File file){
        String name = file.getName();
        int i = name.lastIndexOf('.');
        if(i>0){
            name = name.substring(0, i);
        }
        name=name+"_merged.apk";
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
        if(!file.exists()){
            throw new ARGException("No such file/directory: "+file);
        }
        this.inputFile=file;
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append("   Input: ").append(inputFile);
        builder.append("\n Output: ").append(outputFile);
        if(resDirName!=null){
            builder.append("\nres dir: ").append(resDirName);
        }
        if(validateResDir){
            builder.append("\n Validate res dir name: true");
        }
        if(force){
            builder.append("\n Force: true");
        }
        if(cleanMeta){
            builder.append("\n Keep meta: true");
        }
        if(keepExtractNativeLibs){
            builder.append("\n Keep android:extractNativeLibs: true");
        }
        builder.append("\n ---------------------------- ");
        return builder.toString();
    }
    public static String getHelp(){
        StringBuilder builder=new StringBuilder();
        builder.append(Merger.DESCRIPTION);
        builder.append("\nOptions:\n");
        String[][] table=new String[][]{
                new String[]{ARG_input, ARG_DESC_input},
                new String[]{ARG_output, ARG_DESC_output},
                new String[]{ARG_resDir, ARG_DESC_resDir}
        };
        StringHelper.printTwoColumns(builder, "   ", Options.PRINT_WIDTH, table);
        builder.append("\nFlags:\n");
        table=new String[][]{
                new String[]{ARG_force, ARG_DESC_force},
                new String[]{ARG_cleanMeta, ARG_DESC_cleanMeta},
                new String[]{ARG_keepExtractNativeLibs, ARG_DESC_keepExtractNativeLibs}
        };
        StringHelper.printTwoColumns(builder, "   ", Options.PRINT_WIDTH, table);
        String jar = APKEditor.getJarName();
        builder.append("\n\nExample-1:");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Merger.ARG_SHORT).append(" ")
                .append(ARG_input).append(" path/to/input");
        builder.append(" ").append(ARG_output).append(" path/to/out.apk");
        return builder.toString();
    }
}
