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
package com.reandroid.apkeditor.cloner;

import com.reandroid.apkeditor.APKEditor;
import com.reandroid.apkeditor.Options;
import com.reandroid.apkeditor.Util;
import com.reandroid.apkeditor.utils.StringHelper;
import com.reandroid.commons.command.ARGException;

import java.io.File;

public class ClonerOptions extends Options {
    public String packageName;
    public String appName;
    public String appIcon;
    public boolean keepAuth;
    public ClonerOptions(){
        super();
    }
    @Override
    public void parse(String[] args) throws ARGException {
        parseInput(args);

        packageName = parseArgValue(ARG_package, args);
        appName = parseArgValue(ARG_app_name, args);
        appIcon = parseArgValue(ARG_app_icon, args);
        keepAuth = containsArg(ARG_app_icon, args);

        parseHelp(args);

        checkUnknownOptions(args);
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
        builder.append(Cloner.DESCRIPTION);
        builder.append("\nOptions:\n");
        String[][] table=new String[][]{
                new String[]{ARG_input, ARG_DESC_input},
                new String[]{ARG_output, ARG_DESC_output},
                new String[]{ARG_package, ARG_DESC_package},
                new String[]{ARG_app_name, ARG_DESC_app_name},
                new String[]{ARG_app_icon, ARG_DESC_app_icon}
        };
        StringHelper.printTwoColumns(builder, "   ", PRINT_WIDTH, table);
        builder.append("\n\nFlags:\n");
        table=new String[][]{
                new String[]{ARG_keep_auth, ARG_DESC_keep_auth},
                new String[]{"  ", "  "},
                new String[]{ARG_ALL_help, ARG_DESC_help}
        };
        StringHelper.printTwoColumns(builder, "   ", PRINT_WIDTH, table);

        builder.append("\n").append(Options.LINE);

        String jar = APKEditor.getJarName();

        builder.append("\n\nExample-1:");
        builder.append("\n   java -jar ").append(jar).append(" ").append(Cloner.ARG_SHORT).append(" ")
                .append(ARG_input).append(" file.apk");

        return builder.toString();
    }

    private static final String ARG_package = "-package";
    private static final String ARG_DESC_package = "Package name.";

    private static final String ARG_app_name = "-app-name";
    private static final String ARG_DESC_app_name = "Application name.";

    private static final String ARG_app_icon = "-app-icon";
    private static final String ARG_DESC_app_icon = "Application icon. File path of app icon(s).";

    private static final String ARG_keep_auth = "-keep-auth";
    private static final String ARG_DESC_keep_auth = "Do not rename authorities as per package. \n  *Applies only when option -package used.";

}
