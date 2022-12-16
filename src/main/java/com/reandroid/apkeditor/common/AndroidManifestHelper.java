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
