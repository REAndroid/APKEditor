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
package com.reandroid.apkeditor;

import com.reandroid.commons.command.ARGException;
import com.reandroid.jcommand.CommandHelpBuilder;
import com.reandroid.jcommand.CommandParser;
import com.reandroid.jcommand.annotations.OptionArg;
import com.reandroid.jcommand.exceptions.CommandException;

import java.io.File;

public class Options {

    @OptionArg(name = "-i", description = "input_path")
    public File inputFile;
    @OptionArg(name = "-o", description = "output_path")
    public File outputFile;
    @OptionArg(name = "-f", flag = true, description = "force_delete")
    public boolean force;
    public String type;
    @OptionArg(name = "-h", alternates = {"-help", "--help"}, description = "help_description", flag = true)
    public boolean help = false;

    public Options(){
    }

    public String getHelpString() {
        return new CommandHelpBuilder(this.getClass(), ResourceStrings.INSTANCE).build();
    }
    public void parse(String[] args) throws ARGException {
        CommandParser.parse(this, args);
        if (!help) {
            validateValues();
        }
    }
    public void validateValues() {
        validateInput(true, false);
        validateOutput(true);
    }
    public void validateInput(boolean isFile, boolean isDirectory) {
        File file = this.inputFile;
        if (file == null) {
            throw new CommandException("missing_input_file");
        }
        validateInputFile(file, isFile, isDirectory);
    }
    public void validateInputFile(File file, boolean isFile, boolean isDirectory) {
        if (isFile) {
            if(file.isFile()) {
                return;
            }
            if(!isDirectory) {
                throw new CommandException("no_such_file", file);
            }
        }
        if (isDirectory) {
            if(file.isDirectory()) {
                return;
            }
            throw new CommandException("no_such_directory", file);
        }
        if (!file.exists()) {
            throw new CommandException("no_such_file_or_directory", file);
        }
    }

    public void validateOutput(boolean isFile) {
        File file = this.outputFile;
        if (file == null) {
            file = generateOutputFromInput(this.inputFile);
            this.outputFile = file;
        }
        if (file == null || !file.exists()) {
            return;
        }
        if (isFile != file.isFile()) {
            if (file.isFile()) {
                throw new CommandException("path_is_file_expect_directory", file);
            }
            throw new CommandException("path_is_directory_expect_file", file);
        }
        if(!force) {
            throw new CommandException("path_already_exists", file);
        }
    }
    public File generateOutputFromInput(File input) {
        return null;
    }

    private File parseFramework(String[] args) throws ARGException {
        String path = parseArgValue(ARG_framework, args);
        if(path == null){
            return null;
        }
        File file = new File(path);
        if(!file.isFile()){
            throw new ARGException("No such file: " + path);
        }
        return file;
    }
    protected void parseType(String[] args) throws ARGException {
        parseType(args, TYPE_JSON);
    }
    protected void parseType(String[] args, String def) throws ARGException {
        String[] choices = new String[]{TYPE_JSON, TYPE_XML, TYPE_RAW, TYPE_SIG};
        this.type = parseType(ARG_type, args, choices, def);
    }
    protected void checkUnknownOptions(String[] args) throws ARGException {
        args=Util.trimNull(args);
        if(Util.isEmpty(args)){
            return;
        }
        throw new ARGException("Unknown option: "+args[0]);
    }

    protected String parseType(String argSwitch, String[] args, String[] availableTypes, String def) throws ARGException {
        String type = parseArgValue(argSwitch, args);
        if(type == null){
            return def;
        }
        type = type.trim();
        String typeLower = type.toLowerCase();
        for(String choice : availableTypes){
            if(typeLower.equals(choice)){
                return typeLower;
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Unknown type: '");
        builder.append(type);
        builder.append("' , must be one of {");
        for(int i = 0; i < availableTypes.length; i++){
            if(i != 0){
                builder.append(", ");
            }
            builder.append(availableTypes[i]);
        }
        builder.append("}");
        throw new ARGException(builder.toString());
    }
    protected String parseArgValue(String argSwitch, String[] args) throws ARGException {
        return parseArgValue(argSwitch, true, args);
    }
    protected String parseArgValue(String argSwitch, boolean ignore_case, String[] args) throws ARGException {
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
    protected File parseFile(String argSwitch, String[] args) throws ARGException {
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
    protected boolean containsArg(String argSwitch, String[] args) {
        return containsArg(argSwitch, true, args, false);
    }
    protected boolean containsArg(String argSwitch, String[] args, boolean def) {
        return containsArg(argSwitch, true, args, def);
    }
    protected boolean containsArg(String argSwitch, boolean ignore_case, String[] args){
        return containsArg(argSwitch, ignore_case, args, false);
    }
    protected boolean containsArg(String argSwitch, boolean ignore_case, String[] args, boolean def) {
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
        return def;
    }

    public static String getHelp(Class<?> optionClass){
        CommandHelpBuilder builder = new CommandHelpBuilder(optionClass, ResourceStrings.INSTANCE);
        builder.setMaxWidth(Options.PRINT_WIDTH);
        return builder.build();
    }

    public static final int PRINT_WIDTH = 80;

    protected static final String ARG_ALL_help = "-h|-help";
    protected static final String ARG_DESC_help = "Prints this help";

    protected static final String ARG_output="-o";
    protected static final String ARG_DESC_output="output path";
    protected static final String ARG_input="-i";
    protected static final String ARG_DESC_input="input path";
    protected static final String ARG_resDir="-res-dir";
    protected static final String ARG_DESC_resDir="sets resource files root dir name\n(eg. for obfuscation to move files from 'res/*' to 'r/*' or vice versa)";
    protected static final String ARG_validate_res_dir="-vrd";
    protected static final String ARG_DESC_validate_res_dir="validate resources dir name\n(eg. if a drawable resource file path is 'res/abc.png' then\nit will be moved to 'res/drawable/abc.png')";
    protected static final String ARG_force="-f";
    protected static final String ARG_DESC_force="force delete output path";
    protected static final String ARG_cleanMeta = "-clean-meta";
    protected static final String ARG_DESC_cleanMeta = "cleans META-INF directory along with signature block";

    protected static final String ARG_sig = "-sig";
    protected static final String ARG_DESC_sig = "signatures directory path";
    protected static final String ARG_framework_version = "-framework-version";
    protected static final String ARG_DESC_framework_version = "preferred framework version number";
    protected static final String ARG_framework = "-framework";
    protected static final String ARG_DESC_framework = "path of framework file (can be multiple)";
    public static final String ARG_type = "-t";

    public static final String TYPE_SIG = "sig";
    public static final String TYPE_JSON = "json";
    public static final String TYPE_RAW = "raw";
    public static final String TYPE_XML = "xml";
    public static final String TYPE_TEXT = "text";

    protected static final String LINE = "    ------------------------------------------------------------------------";
}
