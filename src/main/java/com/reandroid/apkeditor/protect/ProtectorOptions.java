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
import com.reandroid.jcommand.annotations.CommandOptions;
import com.reandroid.jcommand.annotations.OptionArg;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

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

    @OptionArg(name = "-keep-type", description = "protect_keep_type")
    public final Set<String> keepTypes = new HashSet<>();

    public ProtectorOptions() {
        super();
    }

    @Override
    public void validateValues() {
        super.validateValues();
        addDefaultKeepTypes();
    }
    private void addDefaultKeepTypes() {
        Set<String> keepTypes = this.keepTypes;
        if (!keepTypes.isEmpty()) {
            return;
        }
        keepTypes.add("font");
    }

    @Override
    public Protector newCommandExecutor() {
        return new Protector(this);
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
    public File generateOutputFromInput(File input) {
        return generateOutputFromInput(input, "_protected.apk");
    }
}
