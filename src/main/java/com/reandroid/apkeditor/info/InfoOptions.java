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

import com.reandroid.apkeditor.APKEditor;
import com.reandroid.apkeditor.Options;
import com.reandroid.apkeditor.Util;
import com.reandroid.apkeditor.utils.StringHelper;
import com.reandroid.commons.command.ARGException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InfoOptions extends Options {
    public boolean verbose = true;
    public boolean packageName = true;
    public boolean versionCode = true;
    public boolean versionName = true;
    public boolean minSdkVersion = true;
    public boolean targetSdkVersion = true;
    public boolean appName = true;
    public boolean appIcon = true;
    public boolean appRoundIcon = true;
    public boolean permissions = true;
    public boolean appClass = true;
    public boolean activities = true;
    public final List<String> resList;
    public boolean resources = false;
    public final List<String> typeFilterList;
    public InfoOptions(){
        super();
        this.resList = new ArrayList<>();
        this.typeFilterList = new ArrayList<>();
    }
    @Override
    public void parse(String[] args) throws ARGException {
        parseInput(args);

        verbose = containsArg(ARG_verbose, args);
        type = parseType(ARG_type, args, availableTypes, TYPE_TEXT);

        parseOutput(args);

        initializeDefaults(args);

        parseResList(args);

        parseResourceFilterList(args);

        parseFrameworks(args);

        packageName = containsArg(ARG_package, args, packageName);
        versionCode = containsArg(ARG_version_code, args, versionCode);
        versionName = containsArg(ARG_version_name, args, versionName);
        minSdkVersion = containsArg(ARG_min_sdk_version, args, minSdkVersion);
        targetSdkVersion = containsArg(ARG_target_sdk_version, args, targetSdkVersion);
        appName = containsArg(ARG_app_name, args, appName);
        appIcon = containsArg(ARG_app_icon, args, appIcon);
        appRoundIcon = containsArg(ARG_app_round_icon, args, appRoundIcon);
        permissions = containsArg(ARG_permissions, args, permissions);
        appClass = containsArg(ARG_app_class, args, appClass);
        activities = containsArg(ARG_activities, args, activities);
        resources = containsArg(ARG_resources, args, false);



        parseHelp(args);

        super.checkUnknownOptions(args);
    }
    private void parseOutput(String[] args) throws ARGException {
        this.outputFile = null;
        File file = parseFile(ARG_output, args);
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
        }else {
            ext = "." + type.toLowerCase();
        }
        if(!name.endsWith(ext)){
            throw new ARGException("Invalid file extension! Expected = \""
                    + ext + "\", " + file);
        }
        this.outputFile = file;
    }
    private void parseResourceFilterList(String[] args) throws ARGException {
        String filter;
        while (( filter = parseArgValue(ARG_filter_type, args)) != null){
            typeFilterList.add(filter);
        }
    }
    private void parseResList(String[] args) throws ARGException {
        String res;
        while (( res = parseArgValue(ARG_res, args)) != null){
            resList.add(res);
            verbose = true;
        }
    }
    private void initializeDefaults(String[] args){
        resources = false;
        if(!Util.isEmpty(args)){
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
        }
    }
    private void parseInput(String[] args) throws ARGException {
        this.inputFile = null;
        File file = parseFile(ARG_input, args);
        if(file == null){
            throw new ARGException("Missing input apk! Specify with  " + ARG_input);
        }
        if(!file.isFile()){
            throw new ARGException("No such file: "+file);
        }
        this.inputFile = file;
    }
    private void parseHelp(String[] args) throws ARGException {
        if(!Util.containsHelp(args)){
            return;
        }
        throw new ARGException(getHelp());
    }
    public static String getHelp(){
        StringBuilder builder=new StringBuilder();
        builder.append(Info.DESCRIPTION);
        builder.append("\nOptions:\n");
        String[][] table=new String[][]{
                new String[]{ARG_input, ARG_DESC_input},
                new String[]{ARG_type, ARG_DESC_type},
                new String[]{ARG_output, ARG_DESC_output},
                new String[]{ARG_res, ARG_DESC_res},
                new String[]{ARG_filter_type, ARG_DESC_filter_type},
                new String[]{ARG_framework, ARG_DESC_framework},
        };
        StringHelper.printTwoColumns(builder, "   ", PRINT_WIDTH, table);
        builder.append("\n\nFlags:\n");
        table=new String[][]{
                new String[]{ARG_verbose, ARG_DESC_verbose},
                new String[]{ARG_package, ARG_DESC_package},
                new String[]{ARG_version_code, ARG_DESC_version_code},
                new String[]{ARG_version_name, ARG_DESC_version_name},
                new String[]{ARG_min_sdk_version, ARG_DESK_min_sdk_version},
                new String[]{ARG_target_sdk_version, ARG_DESK_target_sdk_version},
                new String[]{ARG_app_name, ARG_DESC_app_name},
                new String[]{ARG_app_icon, ARG_DESC_app_icon},
                new String[]{ARG_app_round_icon, ARG_DESC_app_round_icon},
                new String[]{ARG_app_class, ARG_DESC_app_class},
                new String[]{ARG_permissions, ARG_DESC_permissions},
                new String[]{ARG_activities, ARG_DESC_activities},
                new String[]{ARG_resources, ARG_DESC_resources},
                new String[]{"  ", "  "},
                new String[]{ARG_ALL_help, ARG_DESC_help}
        };
        StringHelper.printTwoColumns(builder, "   ", PRINT_WIDTH, table);

        builder.append("\n").append(Options.LINE);

        String jar = APKEditor.getJarName();

        builder.append("\n\nExample-1:");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Info.ARG_SHORT).append(" ")
                .append(ARG_input).append(" file.apk");

        builder.append("\n\nExample-2:");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Info.ARG_SHORT).append(" ")
                .append(ARG_input).append(" file.apk");
        builder.append(" ").append(ARG_type).append(" ").append(TYPE_JSON.toUpperCase());
        builder.append(" ").append(ARG_verbose);
        builder.append(" ").append(ARG_output).append(" info_file.json");

        builder.append("\n\nExample-3:");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Info.ARG_SHORT).append(" ")
                .append(ARG_input).append(" file.apk ")
                .append(ARG_resources)
                .append(" ").append(ARG_filter_type).append(" ").append("mipmap")
                .append(" ").append(ARG_filter_type).append(" ").append("drawable");

        builder.append("\n\nExample-4:");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Info.ARG_SHORT).append(" ")
                .append(ARG_input).append(" file.apk ")
                .append(ARG_verbose)
                .append(" ").append(ARG_res).append(" ").append("@string/app_name")
                .append(" ").append(ARG_res).append(" ").append("0x7f010000");

        return builder.toString();
    }

    protected static final String ARG_output = "-o";
    protected static final String ARG_DESC_output = "Output path, default is print to std stream.";

    private static final String ARG_type = "-t";
    private static final String ARG_DESC_type = "Print type, options:\n  1) TEXT\n  2) JSON\n  3) XML\n   default=TEXT";

    private static final String ARG_verbose = "-v";
    private static final String ARG_DESC_verbose = "Verbose mode.";

    private static final String ARG_package = "-package";
    private static final String ARG_DESC_package = "Package name(s) from manifest and if verbose mode, prints resource table packages.";

    private static final String ARG_version_code = "-version-code";
    private static final String ARG_DESC_version_code = "App version code.";
    private static final String ARG_version_name = "-version-name";
    private static final String ARG_DESC_version_name = "App version name.";
    private static final String ARG_min_sdk_version = "-min-sdk-version";
    private static final String ARG_DESK_min_sdk_version = "Minimum SDK version";
    private static final String ARG_target_sdk_version = "-target-sdk-version";
    private static final String ARG_DESK_target_sdk_version = "Target SDK version";

    private static final String ARG_app_name = "-app-name";
    private static final String ARG_DESC_app_name = "App name. If verbose mode, prints all configurations.";
    private static final String ARG_app_icon = "-app-icon";
    private static final String ARG_DESC_app_icon = "App icon path/value. If verbose mode, prints all configurations.";
    private static final String ARG_app_round_icon = "-app-round-icon";
    private static final String ARG_DESC_app_round_icon = "App round icon path/value. If verbose mode, prints all configurations.";
    private static final String ARG_permissions = "-permissions";
    private static final String ARG_DESC_permissions = "Permissions.";
    private static final String ARG_app_class = "-app-class";
    private static final String ARG_DESC_app_class = "Application class name.";
    private static final String ARG_activities = "-activities";
    private static final String ARG_DESC_activities = "Prints main activity class name. If verbose mode, " +
            "prints all declared activities including <activity-alias>.";

    private static final String ARG_res = "-res";
    private static final String ARG_DESC_res = "Prints resource entries specified by either of:" +
            "\n  1) Hex or decimal resource id.\n  2) Full resource name e.g @string/app_name." +
            "\n  *Can be multiple";


    private static final String ARG_resources = "-resources";
    private static final String ARG_DESC_resources = "Prints all resources.";


    private static final String ARG_filter_type = "-filter-type";
    private static final String ARG_DESC_filter_type = "Prints only the specified resource type names" +
            "\n  *This applies only when flag '-resources' used." +
            "\n  *Can be multiple";


    private static final String[] availableTypes = new String[]{TYPE_TEXT, TYPE_JSON, TYPE_XML};
}
