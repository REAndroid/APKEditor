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
package com.reandroid.apkeditor.common;

import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.value.ValueType;

import java.util.ArrayList;
import java.util.List;

 public class AndroidManifestHelper {
    public static List<ResXmlElement> listSplitRequired(ResXmlElement parentElement){
        List<ResXmlElement> results = new ArrayList<>();
        if(parentElement == null){
            return results;
        }
        List<ResXmlElement> metaDataList = parentElement
                .listElements(AndroidManifestBlock.TAG_meta_data);

        for(ResXmlElement metaData:metaDataList){
            ResXmlAttribute nameAttribute = metaData
                    .searchAttributeByResourceId(AndroidManifestBlock.ID_name);
            if(nameAttribute == null){
                continue;
            }
            if(nameAttribute.getValueType() != ValueType.STRING){
                continue;
            }
            String value = nameAttribute.getValueAsString();
            if(value.startsWith("com.android.vending.")
                    ||value.startsWith("com.android.stamp.")){
                results.add(metaData);
            }
        }
        return results;
    }
    public static boolean removeApplicationAttribute(AndroidManifestBlock manifest, int resId){
        ResXmlElement applicationElement = manifest.getApplicationElement();
        if(applicationElement == null){
            return true;
        }
        ResXmlAttribute attribute = applicationElement
                .searchAttributeByResourceId(resId);
        if(attribute == null){
            return false;
        }
        applicationElement.removeAttribute(attribute);
        return true;
    }
}
