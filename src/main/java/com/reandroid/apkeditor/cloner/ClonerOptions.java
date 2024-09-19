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

import com.reandroid.apkeditor.Options;
import com.reandroid.jcommand.annotations.CommandOptions;
import com.reandroid.jcommand.annotations.OptionArg;


@CommandOptions(
        name = "c",
        alternates = {"clone"},
        description = "Clones application (NOT Implemented)",
        examples = {
                "[Basic]\n  java -jar APKEditor.jar p -i input.apk -o output.apk"
        })
public class ClonerOptions extends Options {

    @OptionArg(name = "-package", description = "Package name")
    public String packageName;

    @OptionArg(name = "-app-name", description = "Application name")
    public String appName;

    @OptionArg(name = "-app-icon", description = "Application icon. File path of app icon(s).")
    public String appIcon;

    @OptionArg(name = "-keep-auth", description = "Do not rename authorities as per package. \n  *Applies only when option -package used.")
    public boolean keepAuth;

    public ClonerOptions(){
        super();
    }

    @Override
    public Cloner newCommandExecutor() {
        return new Cloner(this);
    }
}
