package com.reandroid.apkeditor;

import com.reandroid.lib.arsc.BuildInfo;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class APKEditor {
    private static Properties sProperties;
    public static String getARSCLibInfo(){
        return BuildInfo.getName()+"-"+BuildInfo.getVersion();
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
            sProperties.load(APKEditor.class.getResourceAsStream("/apkeditor.properties"));
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
}
