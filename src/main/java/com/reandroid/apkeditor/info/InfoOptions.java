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
    public boolean verbose = false;

    @OptionArg(name = "-package", description = "info_package_name", flag = true)
    public boolean packageName = false;

    @OptionArg(name = "-version-code", description = "info_app_version_code", flag = true)
    public boolean versionCode = false;

    @OptionArg(name = "-version-name", description = "info_app_version_name", flag = true)
    public boolean versionName = false;

    @OptionArg(name = "-min-sdk-version", description = "info_min_sdk_version", flag = true)
    public boolean minSdkVersion = false;

    @OptionArg(name = "-target-sdk-version", description = "info_target_sdk_version", flag = true)
    public boolean targetSdkVersion = false;

    @OptionArg(name = "-app-name", description = "info_app_name", flag = true)
    public boolean appName = false;

    @OptionArg(name = "-app-icon", description = "info_app_icon", flag = true)
    public boolean appIcon = false;

    @OptionArg(name = "-app-round-icon", description = "info_app_icon_round", flag = true)
    public boolean appRoundIcon = false;

    @OptionArg(name = "-permissions", description = "info_permissions", flag = true)
    public boolean permissions = false;

    @OptionArg(name = "-app-class", description = "info_app_class_name", flag = true)
    public boolean appClass = false;

    @OptionArg(name = "-activities", description = "info_activities", flag = true)
    public boolean activities = false;

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

    @OptionArg(name = "-xmlstrings", description = "info_xml_strings")
    public List<String> xmlStrings = new ArrayList<>();

    @OptionArg(name = "-strings", description = "info_strings", flag = true)
    public boolean strings = false;

    @OptionArg(name = "-xmltree", description = "info_xml_tree")
    public final List<String> xmlTree = new ArrayList<>();

    @OptionArg(name = "-list-files", description = "info_list_files", flag = true)
    public boolean listFiles = false;

    @OptionArg(name = "-list-xml-files", description = "info_list_xml_files", flag = true)
    public boolean listXmlFiles = false;

    @OptionArg(name = "-configurations", description = "info_configurations", flag = true)
    public boolean configurations = false;

    @OptionArg(name = "-languages", description = "info_languages", flag = true)
    public boolean languages = false;

    @OptionArg(name = "-locales", description = "info_locales", flag = true)
    public boolean locales = false;

    public InfoOptions(){
        super();
    }

    @Override
    public Info newCommandExecutor() {
        return new Info(this);
    }

    @Override
    public void validateValues() {
        super.validateValues();
        initializeDefaults();
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

    private void initializeDefaults(){
        if(!isDefault()) {
            return;
        }
        appName = true;
        appIcon = true;
        activities = true;
        appClass = true;
        packageName = true;
        versionCode = true;
        versionName = true;
        if (verbose) {
            permissions = true;
        }
    }
    private boolean isDefault() {
        boolean flagsChanged = activities || appClass || appIcon || appName || appRoundIcon ||
                dex || minSdkVersion || packageName || permissions || targetSdkVersion ||
                resources || signatures || signatures_base64 || versionCode || versionName ||
                listFiles || listXmlFiles || configurations || languages || locales || strings;

        return !flagsChanged && resList.isEmpty() && typeFilterList.isEmpty() &&
                xmlTree.isEmpty() && xmlStrings.isEmpty();
    }
}
