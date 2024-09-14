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
package com.reandroid.apkeditor.compile;

import com.reandroid.apkeditor.Options;
import com.reandroid.apkeditor.OptionsWithFramework;
import com.reandroid.jcommand.annotations.ChoiceArg;
import com.reandroid.jcommand.annotations.CommandOptions;
import com.reandroid.jcommand.annotations.OptionArg;
import com.reandroid.jcommand.exceptions.CommandException;

import java.io.File;


@CommandOptions(
        name = "b",
        alternates = {"build"},
        description = "build_description",
        examples = {
                "build_example_1",
                "build_example_2",
                "build_example_3",
                "build_example_4"
        })
public class BuildOptions extends OptionsWithFramework {

    @ChoiceArg(
            name = "-t",
            values = {
                    TYPE_XML,
                    TYPE_JSON,
                    TYPE_RAW, TYPE_SIG
            },
            description = "build_types")
    public String type = TYPE_XML;

    @OptionArg(name = "-vrd", description = "validate_resources_dir", flag = true)
    public boolean validateResDir;

    @OptionArg(name = "-res-dir", description = "res_dir_name")
    public String resDirName;

    @OptionArg(name = "-no-cache", description = "build_no_cache", flag = true)
    public boolean noCache;

    public boolean isXml;
    public boolean isRaw;

    public BuildOptions() {
    }

    @Override
    public void validateInput(boolean isFile, boolean isDirectory) {
        isFile = TYPE_SIG.equals(type);
        super.validateInput(isFile, !isFile);
        validateSignaturesDirectory();
    }

    @Override
    public void validateOutput(boolean isFile) {
        super.validateOutput(true);
    }

    private void validateSignaturesDirectory() {
        if (TYPE_SIG.equals(type)) {
            File file = this.signaturesDirectory;
            if(file == null) {
                throw new CommandException("missing_sig_directory");
            }
            validateInputFile(file, false, true);
        } else if(this.signaturesDirectory != null) {
            throw new CommandException("invalid_sig_parameter_combination");
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
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
        if(frameworkVersion != null){
            builder.append("\nFramework version: ").append(frameworkVersion);
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

    public File generateOutputFromInput(File file){
        String name = file.getName();
        name = name + "_out.apk";
        File dir = file.getParentFile();
        if(dir == null){
            return new File(name);
        }
        return new File(dir, name);
    }
    public static String getHelp(){
        return Options.getHelp(BuildOptions.class);
    }
}
