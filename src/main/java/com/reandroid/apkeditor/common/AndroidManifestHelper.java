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

import com.reandroid.lib.arsc.array.ResXmlAttributeArray;
import com.reandroid.lib.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.lib.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.lib.arsc.chunk.xml.ResXmlElement;
import com.reandroid.lib.arsc.chunk.xml.ResXmlStartElement;

public class AndroidManifestHelper {
    public static boolean removeApplicationAttribute(AndroidManifestBlock manifest, int resId){
        ResXmlElement app = manifest.getApplicationElement();
        if(app==null){
            return true;
        }
        ResXmlStartElement start = app.getStartElement();
        ResXmlAttribute attr = start.getAttribute(resId);
        if(attr==null){
            return false;
        }
        ResXmlAttributeArray array = start.getResXmlAttributeArray();
        array.remove(attr);
        manifest.refresh();
        return true;
    }
}
