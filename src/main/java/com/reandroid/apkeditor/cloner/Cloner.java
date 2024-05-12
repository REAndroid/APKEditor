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
import com.reandroid.apkeditor.BaseCommand;
import com.reandroid.apkeditor.Util;
import com.reandroid.commons.command.ARGException;

import java.io.IOException;

public class Cloner extends BaseCommand<ClonerOptions> {
    public Cloner(ClonerOptions options){
        super(options, "[CLONE] ");
    }
    @Override
    public void run() throws IOException{
        logWarn("This feature not implemented, follow updates on: " + APKEditor.getRepo());
    }

    public static void execute(String[] args) throws ARGException, IOException {
        if(Util.isHelp(args)){
            throw new ARGException(ClonerOptions.getHelp());
        }
        ClonerOptions option = new ClonerOptions();
        option.parse(args);
        Cloner cloner = new Cloner(option);
        cloner.logVersion();
        cloner.run();
    }

    public static boolean isCommand(String command){
        if(Util.isEmpty(command)){
            return false;
        }
        command = command.toLowerCase().trim();
        return command.equals(ARG_LONG);
        //return command.equals(ARG_SHORT) || command.equals(ARG_LONG);
    }

    public static final String ARG_SHORT = "c";
    public static final String ARG_LONG = "clone";

    public static final String DESCRIPTION = "Clones apk";
}
