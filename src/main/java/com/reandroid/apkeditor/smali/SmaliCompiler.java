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
package com.reandroid.apkeditor.smali;

import com.reandroid.apk.APKLogger;
import com.reandroid.apk.ApkModuleEncoder;
import com.reandroid.apk.DexEncoder;
import com.reandroid.apkeditor.APKEditor;
import com.reandroid.archive.FileInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.dex.model.DexFile;
import com.reandroid.dex.sections.Marker;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.io.FileIterator;
import com.reandroid.utils.io.IOUtil;
import org.jf.dexlib2.extra.DexMarker;
import org.jf.smali.Smali;
import org.jf.smali.SmaliOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SmaliCompiler implements DexEncoder {
    private APKLogger apkLogger;
    private final boolean noCache;
    private Integer minSdkVersion;
    public SmaliCompiler(boolean noCache){
        this.noCache = noCache;
    }
    @Override
    public List<InputSource> buildDexFiles(ApkModuleEncoder apkModuleEncoder, File mainDir) throws IOException {
        File smaliDir = new File(mainDir, "smali");
        if(!smaliDir.isDirectory()){
            return null;
        }
        AndroidManifestBlock manifestBlock =  apkModuleEncoder.getApkModule().getAndroidManifest();
        if (manifestBlock != null) {
            this.minSdkVersion = manifestBlock.getMinSdkVersion();
        }
        if(minSdkVersion == null){
            minSdkVersion = 30;
        }
        List<InputSource> results = new ArrayList<>();
        List<File> classesDirList = listClassesDirectories(smaliDir);
        int i = 0;
        int size = classesDirList.size();
        for(File classesDir : classesDirList){
            i++;
            String progress = "(" + StringsUtil.formatNumber(i, size) + "/" + size + ") ";
            InputSource inputSource = build(progress, classesDir);
            results.add(inputSource);
        }
        return results;
    }
    private InputSource build(String progress, File classesDir) throws IOException {
        File dexCacheFile = toDexCache(classesDir);
        if(isModified(classesDir, dexCacheFile)){
            return build(progress, classesDir, dexCacheFile);
        }else {
            logMessage(progress + "Cached: " + dexCacheFile.getName());
            return new FileInputSource(dexCacheFile, dexCacheFile.getName());
        }
    }
    private InputSource build(String progress, File classesDir, File dexCacheFile) throws IOException {
        if(APKEditor.isExperimental()) {
            return buildExperimental(progress, classesDir, dexCacheFile);
        }
        return buildJesusFreke(progress, classesDir, dexCacheFile);
    }
    private InputSource buildJesusFreke(String progress, File classesDir, File dexCacheFile) throws IOException {
        logMessage(progress + "Smali: " + dexCacheFile.getName());
        SmaliOptions smaliOptions = new SmaliOptions();
        File dir = dexCacheFile.getParentFile();
        if(dir != null && !dir.exists()){
            dir.mkdirs();
        }
        smaliOptions.outputDexFile = dexCacheFile.getAbsolutePath();
        File marker = new File(classesDir, DexMarker.FILE_NAME);
        if(marker.isFile()){
            smaliOptions.markersListFile = marker.getAbsolutePath();
        }
        if(smaliOptions.jobs <= 0){
            smaliOptions.jobs = 1;
        }
        if (this.minSdkVersion != null) {
            smaliOptions.apiLevel = this.minSdkVersion;
        }
        boolean success = Smali.assemble(smaliOptions, classesDir.getAbsolutePath());
        if(!success){
            throw new IOException("Failed to build smali, check the logs");
        }
        return new FileInputSource(dexCacheFile, dexCacheFile.getName());
    }
    private InputSource buildExperimental(String progress, File classesDir, File dexCacheFile) throws IOException {
        logMessage(progress + "Smali: " + dexCacheFile.getName());
        DexFile dexFile = DexFile.createDefault();
        FileIterator fileIterator = new FileIterator(classesDir,
                FileIterator.getExtensionFilter(".smali"));
        while (fileIterator.hasNext()) {
            File file = fileIterator.next();
            try {
                dexFile.fromSmali(SmaliReader.of(file));
            }catch (Exception e) {
                throw new IOException("Error at: " + file, e);
            }
        }
        dexFile.refresh();
        readMarkers(dexFile, classesDir);
        int version = 0;
        if (this.minSdkVersion != null) {
            version = minSdkVersion;
        }
        version = apiToDexVersion(version);
        dexFile.setVersion(version);
        dexFile.clearEmptySections();
        dexFile.sortSection(SectionType.getR8Order());
        dexFile.shrink();
        dexFile.refreshFull();
        dexFile.write(dexCacheFile);
        dexFile.close();
        return new FileInputSource(dexCacheFile, dexCacheFile.getName());
    }
    private void readMarkers(DexFile dexFile, File classesDir) throws IOException {
        File markersFile = new File(classesDir, DexMarker.FILE_NAME);
        if(markersFile.isFile()){
            logMessage("Reading markers ...");
            String[] content = StringsUtil.split(IOUtil.readUtf8(markersFile), '\n');
            for(String markerString : content) {
                Marker marker = Marker.parse(markerString);
                if(marker != null) {
                    dexFile.addMarker(marker);
                }
            }
        }
    }

    private boolean isModified(File classesDir, File dexCacheFile){
        if(noCache || !dexCacheFile.isFile()){
            return true;
        }
        long dexMod = dexCacheFile.lastModified();
        return isModified(classesDir, dexMod);
    }
    private boolean isModified(File dir, long dexMod){
        if(dir.lastModified() > dexMod){
            return true;
        }
        if(!dir.isDirectory()){
            return false;
        }
        File[] files = dir.listFiles();
        if(files == null){
            return false;
        }
        for(File file : files){
            if(isModified(file, dexMod)){
                return true;
            }
        }
        return false;
    }
    private File toDexCache(File classesDir){
        File mainDir = classesDir.getParentFile().getParentFile();
        File dir = new File(mainDir, SmaliUtil.CACHE_DIR);
        return new File(dir, SmaliUtil.getDexFileName(SmaliUtil.getDexNumber(classesDir.getName())));
    }
    private List<File> listClassesDirectories(File smaliDir){
        List<File> results = new ArrayList<>();
        if(!smaliDir.isDirectory()){
            return results;
        }
        File[] files = smaliDir.listFiles();
        if(files == null){
            return results;
        }
        for(File file : files){
            if(!SmaliUtil.isClassesDir(file)){
                logMessage("WARN: Ignore: " + file);
                continue;
            }
            results.add(file);
        }
        SmaliUtil.sortDexFiles(results);
        return results;
    }
    public void setApkLogger(APKLogger apkLogger) {
        this.apkLogger = apkLogger;
    }
    private void logMessage(String msg){
        APKLogger apkLogger = this.apkLogger;
        if(apkLogger != null){
            apkLogger.logMessage(msg);
        }
    }

    public static int apiToDexVersion(int api) {
        if (api <= 23) {
            return 35;
        }
        switch (api) {
            case 24:
            case 25:
                return 37;
            case 26:
            case 27:
                return 38;
            case 28:
                return 39;
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
                return 40;
            case 35:
                return 41;
        }
        return 39;
    }
}
