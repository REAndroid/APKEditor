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
package com.reandroid.apkeditor.info;

import com.reandroid.apk.ApkModule;
import com.reandroid.apk.ResFile;
import com.reandroid.apkeditor.CommandExecutor;
import com.reandroid.apkeditor.Util;
import com.reandroid.app.AndroidManifest;
import com.reandroid.archive.InputSource;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ReferenceString;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.dex.model.DexDirectory;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.HexUtil;
import com.reandroid.arsc.value.AttributeDataFormat;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResValue;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.utils.collection.CollectionUtil;

import java.io.*;
import java.util.*;

public class Info extends CommandExecutor<InfoOptions> {
    private InfoWriter mInfoWriter;
    public Info(InfoOptions options){
        super(options, "[INFO] ");
        super.setEnableLog(options.outputFile != null);
    }
    @Override
    public void runCommand() throws IOException{
        InfoOptions options = getOptions();
        setEnableLog(options.outputFile != null);
        delete(options.outputFile);
        logMessage("Loading: " + options.inputFile);
        ApkModule apkModule = ApkModule.loadApkFile(this, options.inputFile,
                options.getFrameworks());
        String msg = Util.isProtected(apkModule);
        if(msg != null){
            logWarn(msg);
            return;
        }
        apkModule.setAPKLogger(this);
        apkModule.setLoadDefaultFramework(options.verbose);
        File out = options.outputFile;
        if(out != null){
            logMessage("Writing ...");
        }
        print(apkModule);
        flush();
        close();
        if(out != null){
            logMessage("Saved to: " + out);
        }
    }
    private void print(ApkModule apkModule) throws IOException {
        printSourceFile();

        printPackage(apkModule);
        AndroidManifestBlock manifest = apkModule.getAndroidManifest();
        printVersionCode(manifest);
        printVersionName(manifest);
        printMinSdkVersion(manifest);
        printTargetSdkVersion(manifest);

        printAppName(apkModule);
        printAppIcon(apkModule);
        printAppRoundIcon(apkModule);
        printAppClass(apkModule);
        printActivities(apkModule);
        printUsesPermissions(apkModule);

        printResList(apkModule);

        printResources(apkModule);
        printDex(apkModule);
        printSignatures(apkModule);

        printXmlTree(apkModule);
        printXmlStrings(apkModule);
        listFiles(apkModule);
        listXmlFiles(apkModule);
    }
    private void printSourceFile() throws IOException {
        InfoOptions options = getOptions();
        if(options.outputFile == null){
            return;
        }
        if(options.verbose || !options.resources){
            InfoWriter infoWriter = getInfoWriter();
            infoWriter.writeNameValue("source-file",
                    options.inputFile.getAbsolutePath());
        }
    }
    private void printResources(ApkModule apkModule) throws IOException {
        InfoOptions options = getOptions();
        if(!options.resources){
            return;
        }
        if(!apkModule.hasTableBlock()){
            return;
        }
        TableBlock tableBlock = apkModule.getTableBlock();
        InfoWriter infoWriter = getInfoWriter();
        boolean writeEntries = options.verbose;
        for(PackageBlock packageBlock : tableBlock.listPackages()){
            infoWriter.writeResources(packageBlock, options.typeFilterList, writeEntries);
        }
    }
    private void printDex(ApkModule apkModule) throws IOException {
        InfoOptions options = getOptions();
        if(!options.dex){
            return;
        }
        InfoWriter infoWriter = getInfoWriter();
        DexDirectory dexDirectory = DexDirectory.readStrings(apkModule.getZipEntryMap());
        infoWriter.writeDexInfo(dexDirectory);
    }
    private void printSignatures(ApkModule apkModule) throws IOException {
        InfoOptions options = getOptions();
        if(!options.signatures && !options.signatures_base64){
            return;
        }
        InfoWriter infoWriter = getInfoWriter();
        infoWriter.writeSignatureInfo(apkModule.getApkSignatureBlock(), options.signatures_base64);
    }
    private void printResList(ApkModule apkModule) throws IOException {
        InfoOptions options = getOptions();
        if(options.resList.size() == 0){
            return;
        }
        if(!apkModule.hasTableBlock()){
            return;
        }
        for(String res : options.resList){
            printRes(apkModule, res);
        }
    }
    private void printRes(ApkModule apkModule, String res) throws IOException {
        if(res == null || res.length() < 3){
            return;
        }
        if(res.startsWith("@0x")){
            res = res.substring(1);
        }
        EncodeResult encodeResult = ValueCoder.encode(res, AttributeDataFormat.INTEGER.valueTypes());
        if(encodeResult != null){
            printEntries(apkModule, "resource", encodeResult.value);
            return;
        }
        ReferenceString referenceString = ReferenceString.parseReference(res);
        if(referenceString == null){
            logWarn("WARN: Invalid resource: " + res);
            return;
        }
        TableBlock tableBlock = apkModule.getTableBlock();
        for(PackageBlock packageBlock : tableBlock.listPackages()){
            ResourceEntry resourceEntry = packageBlock
                    .getResource(referenceString.type, referenceString.name);
            if(resourceEntry == null){
                continue;
            }
            printEntries(apkModule, "resource", resourceEntry.getResourceId());
            return;
        }
        logMessage("WARN: resource not found: " + res);
    }
    private void printPackage(ApkModule apkModule) throws IOException {
        InfoOptions options = getOptions();
        if(!options.packageName){
            return;
        }
        AndroidManifestBlock manifest = apkModule.getAndroidManifest();
        if(manifest != null){
            getInfoWriter().writeNameValue("package" , manifest.getPackageName());
        }
        if(!options.verbose || !apkModule.hasTableBlock()){
            return;
        }
        TableBlock tableBlock = apkModule.getTableBlock();
        InfoWriter infoWriter = getInfoWriter();
        infoWriter.writePackageNames(CollectionUtil.collect(tableBlock.iterator()));
    }
    private void printVersionCode(AndroidManifestBlock manifest) throws IOException {
        InfoOptions options = getOptions();
        if(!options.versionCode || manifest == null){
            return;
        }
        getInfoWriter().writeNameValue("VersionCode" , manifest.getVersionCode());
    }
    private void printVersionName(AndroidManifestBlock manifest) throws IOException {
        InfoOptions options = getOptions();
        if(!options.versionName || manifest == null){
            return;
        }
        getInfoWriter().writeNameValue("VersionName" , manifest.getVersionName());
    }
    private void printMinSdkVersion(AndroidManifestBlock manifest) throws IOException {
        InfoOptions options = getOptions();
        if(!options.minSdkVersion || manifest == null){
            return;
        }
        getInfoWriter().writeNameValue("MinSdkVersion" , manifest.getMinSdkVersion());
    }
    private void printTargetSdkVersion(AndroidManifestBlock manifest) throws IOException {
        InfoOptions options = getOptions();
        if(!options.targetSdkVersion || manifest == null){
            return;
        }
        getInfoWriter().writeNameValue("TargetSdkVersion" , manifest.getTargetSdkVersion());
    }
    private void printAppName(ApkModule apkModule) throws IOException {
        InfoOptions options = getOptions();
        if(!options.appName){
            return;
        }
        AndroidManifestBlock manifest = apkModule.getAndroidManifest();
        ResXmlElement application = manifest.getApplicationElement();
        ResXmlAttribute attributeLabel = application
                .searchAttributeByResourceId(AndroidManifest.ID_label);
        if(attributeLabel == null){
            return;
        }
        if(attributeLabel.getValueType() == ValueType.STRING){
            getInfoWriter().writeNameValue("AppName", attributeLabel.getValueAsString());
            return;
        }
        int resourceId = attributeLabel.getData();
        printEntries(apkModule, "AppName", resourceId);
    }
    private void printAppIcon(ApkModule apkModule) throws IOException {
        InfoOptions options = getOptions();
        if(!options.appIcon){
            return;
        }
        AndroidManifestBlock manifest = apkModule.getAndroidManifest();
        ResXmlElement application = manifest.getApplicationElement();
        ResXmlAttribute attribute = application
                .searchAttributeByResourceId(AndroidManifest.ID_icon);
        if(attribute == null){
            return;
        }
        if(attribute.getValueType() == ValueType.STRING){
            getInfoWriter().writeNameValue("AppIcon", attribute.getValueAsString());
            return;
        }
        int resourceId = attribute.getData();
        printEntries(apkModule, "AppIcon", resourceId);
    }
    private void printAppRoundIcon(ApkModule apkModule) throws IOException {
        InfoOptions options = getOptions();
        if(!options.appRoundIcon){
            return;
        }
        AndroidManifestBlock manifest = apkModule.getAndroidManifest();
        ResXmlElement application = manifest.getApplicationElement();
        int id_roundIcon = 0x0101052c;
        ResXmlAttribute attribute = application
                .searchAttributeByResourceId(id_roundIcon);
        if(attribute == null){
            return;
        }
        if(attribute.getValueType() == ValueType.STRING){
            getInfoWriter().writeNameValue("AppRoundIcon", attribute.getValueAsString());
            return;
        }
        int resourceId = attribute.getData();
        printEntries(apkModule, "AppRoundIcon", resourceId);
    }
    private void printUsesPermissions(ApkModule apkModule) throws IOException {
        InfoOptions options = getOptions();
        if(!options.permissions){
            return;
        }
        AndroidManifestBlock manifest = apkModule.getAndroidManifest();
        if(manifest == null){
            return;
        }
        List<String> usesPermissions = manifest.getUsesPermissions();
        if(usesPermissions.size() == 0){
            return;
        }
        usesPermissions.sort(CompareUtil.getComparableComparator());
        //printLine("Uses permission (" + usesPermissions.size() + ")");
        String tag = AndroidManifest.TAG_uses_permission;
        InfoWriter infoWriter = getInfoWriter();
        infoWriter.writeArray(tag, usesPermissions.toArray(new String[0]));
    }
    private void printActivities(ApkModule apkModule) throws IOException {
        InfoOptions options = getOptions();
        if(!options.activities){
            return;
        }
        AndroidManifestBlock manifest = apkModule.getAndroidManifest();
        if(manifest == null){
            return;
        }
        List<ResXmlElement> activityList = manifest.listActivities(true);
        if(activityList.size() == 0){
            return;
        }
        ResXmlElement main = manifest.getMainActivity();
        if(main != null){
            String value = getValueOfName(main);
            getInfoWriter().writeNameValue("activity-main", value);
        }
        if(!options.verbose){
            return;
        }
        String[] activityNames = new String[activityList.size()];
        for(int i = 0; i < activityList.size(); i++){
            ResXmlElement activity = activityList.get(i);
            activityNames[i] = getValueOfName(activity);
        }
        InfoWriter infoWriter = getInfoWriter();
        infoWriter.writeArray("activities", activityNames);
    }
    private void printAppClass(ApkModule apkModule) throws IOException {
        InfoOptions options = getOptions();
        if(!options.appClass){
            return;
        }
        AndroidManifestBlock manifest = apkModule.getAndroidManifest();
        if(manifest == null){
            return;
        }
        ResXmlElement applicationElement = manifest.getApplicationElement();
        if(applicationElement == null){
            return;
        }
        String value = getValueOfName(applicationElement);
        if(value != null){
            getInfoWriter().writeNameValue("application-class", value);
        }
    }
    private void printXmlStrings(ApkModule apkModule) throws IOException {
        InfoOptions options = getOptions();
        String xmlStrings = options.xmlStrings;
        if (xmlStrings == null) {
            return;
        }
        InfoWriter infoWriter = getInfoWriter();
        ResXmlDocument document = apkModule.loadResXmlDocument(xmlStrings);
        document.setApkFile(null);
        document.setPackageBlock(null);
        infoWriter.writeStringPool(xmlStrings, document.getStringPool());
    }
    private void printXmlTree(ApkModule apkModule) throws IOException {
        InfoOptions options = getOptions();
        InfoWriter infoWriter = getInfoWriter();
        for (String path : options.xmlTree) {
            logMessage("Writing: " + path);
            ResXmlDocument document = apkModule.loadResXmlDocument(path);
            document.setApkFile(null);
            document.setPackageBlock(null);
            infoWriter.writeXmlDocument(path, document);
        }
    }
    private void listFiles(ApkModule apkModule) throws IOException {
        InfoOptions options = getOptions();
        if (!options.listFiles) {
            return;
        }
        InputSource[] inputSources = apkModule.getInputSources();
        int count = inputSources.length;
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            names[i] = inputSources[i].getAlias();
        }
        getInfoWriter().writeArray("Files", names);
    }
    private void listXmlFiles(ApkModule apkModule) throws IOException {
        InfoOptions options = getOptions();
        if (!options.listXmlFiles) {
            return;
        }
        List<ResFile> resFileList = apkModule.listResFiles();
        List<String> names = new ArrayList<>();
        for (ResFile resFile : resFileList) {
            if(resFile.isBinaryXml()) {
                names.add(resFile.getFilePath());
            }
        }
        getInfoWriter().writeArray("CompiledXmlFiles", names.toArray(new String[0]));
    }
    private String getValueOfName(ResXmlElement element){
        ResXmlAttribute attribute = element
                .searchAttributeByResourceId(AndroidManifest.ID_name);
        if(attribute == null){
            return null;
        }
        return attribute.getValueAsString();
    }
    private void printEntries(ApkModule apkModule, String varName, int resourceId) throws IOException {
        TableBlock tableBlock = apkModule.getTableBlock();
        InfoWriter infoWriter = getInfoWriter();
        if(tableBlock == null){
            infoWriter.writeNameValue(varName, HexUtil.toHex8( "@0x", resourceId));
            return;
        }
        List<Entry> entryList = tableBlock.resolveReference(resourceId);
        if(entryList.size() == 0){
            logWarn("WARN: Can't find resource: " + HexUtil.toHex8("@0x", resourceId));
            //infoWriter.writeNameValue(varName, HexUtil.toHex8("@0x", resourceId));
            return;
        }
        entryList = sortEntries(entryList);
        InfoOptions options = getOptions();
        if(!options.verbose){
            infoWriter.writeNameValue(varName, getValueAsString(entryList.get(0)));
            return;
        }
        infoWriter.writeEntries(varName, entryList);
    }
    private String getValueAsString(Entry entry){
        ResValue resValue = entry.getResValue();
        if(resValue == null){
            return "";
        }
        ValueType valueType = resValue.getValueType();
        if(valueType == ValueType.STRING){
            return resValue.getValueAsString();
        }
        String decoded = ValueCoder.decode(valueType, resValue.getData());
        if(decoded != null){
            return decoded;
        }
        return HexUtil.toHex8("@0x", resValue.getData());
    }
    private InfoWriter getInfoWriter() throws IOException{
        if(mInfoWriter != null){
            return mInfoWriter;
        }
        InfoOptions options = getOptions();
        Writer writer = createWriter();
        InfoWriter infoWriter;
        if(InfoOptions.TYPE_JSON.equals(options.type)){
            infoWriter = new InfoWriterJson(writer);
        }else if(InfoOptions.TYPE_XML.equals(options.type)){
            infoWriter = new InfoWriterXml(writer);
        }else {
            infoWriter = new InfoWriterText(writer);
        }
        mInfoWriter = infoWriter;
        return mInfoWriter;
    }
    private Writer createWriter() throws IOException{
        InfoOptions options = getOptions();
        File file = options.outputFile;
        if(file == null){
            return new PrintWriter(System.out);
        }
        File dir = file.getParentFile();
        if(dir != null && !dir.exists()){
            dir.mkdirs();
        }
        return new OutputStreamWriter(new FileOutputStream(file));
    }
    private void flush() throws IOException {
        InfoWriter writer = this.mInfoWriter;
        if(writer != null){
            writer.flush();
        }
    }
    private void close() throws IOException {
        InfoWriter writer = this.mInfoWriter;
        if(writer != null){
            writer.close();
            mInfoWriter = null;
        }
    }

    private static List<Entry> sortEntries(Collection<Entry> entryCollection) {
        ArrayList<Entry> results;
        if(entryCollection instanceof ArrayList){
            results = (ArrayList<Entry>) entryCollection;
        }else {
            results = new ArrayList<>(entryCollection);
        }
        Comparator<Entry> cmp = new Comparator<Entry>() {
            @Override
            public int compare(Entry entry1, Entry entry2) {
                return entry1.getResConfig().compareTo(entry2.getResConfig());
            }
        };
        results.sort(cmp);
        return results;
    }
}
