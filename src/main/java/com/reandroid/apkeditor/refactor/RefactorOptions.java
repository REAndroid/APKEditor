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
package com.reandroid.apkeditor.refactor;

import com.reandroid.apkeditor.Options;
import com.reandroid.jcommand.annotations.CommandOptions;
import com.reandroid.jcommand.annotations.OptionArg;

import java.io.File;

@CommandOptions(
        name = "x",
        alternates = {"refactor"},
        description = "refactor_description",
        examples = {
                "refactor_example_1"
        })
public class RefactorOptions extends Options {

    @OptionArg(name = "-public-xml", description = "refactor_public_xml")
    public File publicXml;

    @OptionArg(name = "-fix-types", flag = true, description = "refactor_fix_types")
    public boolean fixTypeNames;

    @OptionArg(name = "-clean-meta", flag = true, description = "clean_meta")
    public boolean cleanMeta;

    public RefactorOptions(){
        super();
    }

    @Override
    public Refactor newCommandExecutor() {
        return new Refactor(this);
    }

    @Override
    public void validateValues() {
        super.validateValues();
        validatePublicXml();
    }

    private void validatePublicXml() {
        File file = this.publicXml;
        if(file != null) {
            validateInputFile(file, true, false);
        }
    }

    @Override
    public File generateOutputFromInput(File file) {
        return generateOutputFromInput(file, "_refactored.apk");
    }
}
