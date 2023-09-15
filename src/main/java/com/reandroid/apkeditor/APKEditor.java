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

import com.reandroid.arsc.ARSCLib;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class APKEditor {
    private static Properties sProperties;
    public static String getARSCLibInfo(){
        return ARSCLib.getName() + "-" + ARSCLib.getVersion();
    }
    public static String getDescription(){
        return getProperties().getProperty("app.description", "---");
    }
    public static String getRepo(){
        return getProperties().getProperty("app.repo", "https://github.com/REAndroid");
    }
    public static String getName(){
        return getProperties().getProperty("app.name", "---");
    }
    public static String getVersion(){
        return getProperties().getProperty("app.version", "---");
    }
    private static Properties getProperties(){
        if(sProperties!=null){
            return sProperties;
        }
        sProperties=new Properties();
        try {
            sProperties.load(APKEditor.class.getResourceAsStream(PATH_properties));
        } catch (IOException ex) {
            sProperties.put("app.description", ex.getMessage());
        }
        return sProperties;
    }
    public static String getJarName(){
        File file = new File(APKEditor.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation().getPath());
        if(file.isFile()){
            return file.getName();
        }
        return getName()+".jar";
    }

    public static final String PATH_properties = "/apkeditor.properties";

}
