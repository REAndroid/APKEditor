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

import com.reandroid.apk.ApkModule;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.utils.collection.CollectionUtil;

import java.util.List;
import java.util.Random;

public class ManifestConfuser extends Confuser {

    public ManifestConfuser(Protector protector) {
        super(protector, "ManifestConfuser: ");
    }

    @Override
    public void confuse() {
        if (getOptions().skipManifest) {
            logMessage("Skip");
            return;
        }
        ApkModule apkModule = getApkModule();
        AndroidManifestBlock manifestBlock = apkModule.getAndroidManifest();
        int defaultAttributeSize = 20;
        List<ResXmlElement> elementList = CollectionUtil.toList(manifestBlock.recursiveElements());
        Random random = new Random();
        for (ResXmlElement element : elementList) {
            int size = defaultAttributeSize + random.nextInt(6) + 1;
            element.setAttributesUnitSize(size, false);
            ResXmlAttribute attribute = element.newAttribute();
            attribute.setName(" >\n  </" + element.getName() + ">\n  android:name", 0);
            attribute.setValueAsBoolean(false);
        }
        manifestBlock.getManifestElement().setAttributesUnitSize(
                defaultAttributeSize, false);
        manifestBlock.refresh();
    }
}
