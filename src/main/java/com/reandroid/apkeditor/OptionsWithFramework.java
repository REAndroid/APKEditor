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

import com.reandroid.jcommand.annotations.OptionArg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OptionsWithFramework extends Options {

    @OptionArg(name = "-framework-version", description = "framework_version_number")
    public Integer frameworkVersion;
    @OptionArg(name = "-framework", description = "path_of_framework")
    public final List<File> frameworks = new ArrayList<>();


    public OptionsWithFramework() {
        super();
    }

    @Override
    public void validateValues() {
        super.validateValues();
        validateFrameworkFiles();
    }

    public File[] getFrameworks() {
        return frameworks.toArray(new File[0]);
    }
    public void validateFrameworkFiles() {
        for(File file : frameworks) {
            validateInputFile(file, true, false);
        }
    }
}
