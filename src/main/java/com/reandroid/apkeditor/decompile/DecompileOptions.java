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

import com.reandroid.apkeditor.Options;
import com.reandroid.apkeditor.OptionsWithFramework;
import com.reandroid.jcommand.annotations.ChoiceArg;
import com.reandroid.jcommand.annotations.CommandOptions;
import com.reandroid.jcommand.annotations.OptionArg;

import java.io.File;

@CommandOptions(
        name = "d",
        alternates = {"decode"},
        description = "decode_description",
        usage = "decode_usage",
        examples = {
                "decode_example_1",
                "decode_example_2",
                "decode_example_3",
                "decode_example_4",
                "decode_example_5"
        })
public class DecompileOptions extends OptionsWithFramework {

    @ChoiceArg(name = "-t",
            values = {
                    TYPE_XML,
                    TYPE_JSON,
                    TYPE_RAW,
                    TYPE_SIG
            },
            description = "decode_types"
    )
    public String type = TYPE_XML;

    @OptionArg(name = "-split-json", flag = true, description = "split_json")
    public boolean splitJson;

    @OptionArg(name = "-vrd", flag = true, description = "validate_resources_dir")
    public boolean validateResDir;

    @OptionArg(name = "-res-dir", description = "res_dir_name")
    public String resDirName;

    @OptionArg(name = "-keep-res-path", flag = true, description = "keep_original_res")
    public boolean keepResPath;

    @OptionArg(name = "-dex", flag = true, description = "raw_dex")
    public boolean dex;

    @OptionArg(name = "-no-dex-debug", flag = true, description = "no_dex_debug")
    public boolean noDexDebug;

    @OptionArg(name = "-dex-markers", flag = true, description = "dump_dex_markers")
    public boolean dexMarkers;

    public File keepClassListFile;
    public File keepResourceNameListFile;

    public DecompileOptions() {
    }

    @Override
    public void validateInput(boolean isFile, boolean isDirectory) {
        super.validateInput(true, false);
    }
    @Override
    public void validateOutput(boolean isFile) {
        super.validateOutput(false);
    }

    @Override
    public File generateOutputFromInput(File input) {
        String name = input.getName();
        int i = name.lastIndexOf('.');
        if(i > 0){
            name = name.substring(0, i);
        }
        name = name + "_decompile_" + type;
        File dir = input.getParentFile();
        if(dir == null) {
            return new File(name);
        }
        return new File(dir, name);
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
        if(frameworks.size() != 0){
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
        return Options.getHelp(DecompileOptions.class);
    }
}
