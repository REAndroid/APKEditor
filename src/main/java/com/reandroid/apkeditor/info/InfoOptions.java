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
package com.reandroid.apkeditor.info;

import com.reandroid.apkeditor.OptionsWithFramework;
import com.reandroid.apkeditor.Util;
import com.reandroid.commons.command.ARGException;
import com.reandroid.jcommand.annotations.ChoiceArg;
import com.reandroid.jcommand.annotations.CommandOptions;
import com.reandroid.jcommand.annotations.OptionArg;
import com.reandroid.jcommand.exceptions.CommandException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@CommandOptions(
        name = "info",
        description = "info_description",
        examples = {
                "info_example_1",
                "info_example_2",
                "info_example_3"
        })
public class InfoOptions extends OptionsWithFramework {

    @ChoiceArg(name = "-t", description = "info_print_types", values = {TYPE_TEXT, TYPE_JSON, TYPE_XML})
    public String type = TYPE_TEXT;
    @OptionArg(name = "-v", description = "info_verbose_mode", flag = true)
    public boolean verbose = true;
    @OptionArg(name = "-package", description = "info_package_name", flag = true)
    public boolean packageName = true;
    @OptionArg(name = "-version-code", description = "info_app_version_code", flag = true)
    public boolean versionCode = true;
    @OptionArg(name = "-version-name", description = "info_app_version_name", flag = true)
    public boolean versionName = true;
    @OptionArg(name = "-min-sdk-version", description = "info_min_sdk_version", flag = true)
    public boolean minSdkVersion = true;
    @OptionArg(name = "-target-sdk-version", description = "info_target_sdk_version", flag = true)
    public boolean targetSdkVersion = true;
    @OptionArg(name = "-app-name", description = "info_app_name", flag = true)
    public boolean appName = true;
    @OptionArg(name = "-app-icon", description = "info_app_icon", flag = true)
    public boolean appIcon = true;
    @OptionArg(name = "-app-round-icon", description = "info_app_icon_round", flag = true)
    public boolean appRoundIcon = true;
    @OptionArg(name = "-permissions", description = "info_permissions", flag = true)
    public boolean permissions = false;
    @OptionArg(name = "-app-class", description = "info_app_class_name", flag = true)
    public boolean appClass = true;
    @OptionArg(name = "-activities", description = "info_activities", flag = true)
    public boolean activities = true;
    @OptionArg(name = "-res", description = "info_res")
    public final List<String> resList = new ArrayList<>();
    @OptionArg(name = "-resources", description = "info_resources", flag = true)
    public boolean resources = false;
    @OptionArg(name = "-filter-type", description = "info_filter_type")
    public final List<String> typeFilterList = new ArrayList<>();
    @OptionArg(name = "-dex", description = "info_dex", flag = true)
    public boolean dex = false;
    @OptionArg(name = "-signatures", description = "info_signatures", flag = true)
    public boolean signatures = false;
    @OptionArg(name = "-signatures-base64", description = "info_signatures_base64", flag = true)
    public boolean signatures_base64 = false;

    public InfoOptions(){
        super();
    }

    @Override
    public void parse(String[] args) throws ARGException {
        initializeDefaults(args);
        super.parse(args);
    }

    @Override
    public Info newCommandExecutor() {
        return new Info(this);
    }

    @Override
    public void validateInput(boolean isFile, boolean isDirectory) {
        super.validateInput(true, false);
    }

    @Override
    public void validateOutput(boolean isFile) {
        super.validateOutput(true);
        validateOutputExtension();
    }

    private void validateOutputExtension() {
        File file = this.outputFile;
        if(file == null){
            return;
        }
        String name = file.getName().toLowerCase();
        String ext;
        if(TYPE_TEXT.equals(type)){
            if(name.endsWith(".text")){
                ext = ".text";
            }else {
                ext = ".txt";
            }
        } else {
            ext = "." + type.toLowerCase();
        }
        if(!name.endsWith(ext)){
            throw new CommandException("info_invalid_output_extension", ext, file);
        }
    }

    private void initializeDefaults(String[] args){
        resources = false;
        if(!Util.isEmpty(args) && args.length > 2){
            packageName = false;
            versionCode = false;
            versionName = false;
            minSdkVersion = false;
            targetSdkVersion = false;
            appName = false;
            appIcon = false;
            appRoundIcon = false;
            permissions = false;
            activities = false;
            appClass = false;
            dex = false;
        }
    }
}
