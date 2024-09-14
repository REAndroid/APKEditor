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
package com.reandroid.apkeditor.protect;

import com.reandroid.apkeditor.Options;
import com.reandroid.commons.command.ARGException;
import com.reandroid.jcommand.annotations.CommandOptions;
import com.reandroid.jcommand.annotations.OptionArg;

import java.io.File;

@CommandOptions(
        name = "p",
        alternates = {"protect"},
        description = "protect_description",
        examples = {
                "protect_example_1"
        })
public class ProtectorOptions extends Options {

    @OptionArg(name = "-skip-manifest", flag = true, description = "protect_skip_manifest")
    public boolean skipManifest;

    public ProtectorOptions() {
        super();
    }

    @Override
    public void validateInput(boolean isFile, boolean isDirectory) {
        super.validateInput(true, false);
    }

    @Override
    public void validateOutput(boolean isFile) {
        super.validateOutput(true);
    }

    @Override
    public File generateOutputFromInput(File file){
        String name = file.getName();
        int i = name.lastIndexOf('.');
        if(i>0){
            name = name.substring(0, i);
        }
        name=name + "_protected.apk";
        File dir = file.getParentFile();
        if(dir == null){
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
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append("   Input: ").append(inputFile);
        builder.append("\n Output: ").append(outputFile);
        if(force){
            builder.append("\n Force: true");
        }
        builder.append("\n ---------------------------- ");
        return builder.toString();
    }
    public static String getHelp(){
        return Options.getHelp(ProtectorOptions.class);
    }

    protected static final String ARG_skipManifest = "-skip-manifest";
    protected static final String ARG_DESC_skipManifest = "skips/ignores manifest";
}
