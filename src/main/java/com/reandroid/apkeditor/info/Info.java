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
import com.reandroid.apkeditor.BaseCommand;
import com.reandroid.apkeditor.Util;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ReferenceString;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.group.EntryGroup;
import com.reandroid.arsc.util.HexUtil;
import com.reandroid.arsc.value.AttributeDataFormat;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResValue;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.commons.command.ARGException;

import java.io.*;
import java.util.*;

public class Info extends BaseCommand {
    private final InfoOptions options;
    private InfoWriter mInfoWriter;
    public Info(InfoOptions options){
        super();
        this.options = options;
        super.setLogTag(LOG_TAG_INFO);
        super.setEnableLog(options.outputFile != null);
    }
    @Override
    public void run() throws IOException{
        setEnableLog(options.outputFile != null);
        logMessage("Loading: " + options.inputFile);
        ApkModule apkModule = ApkModule.loadApkFile(options.inputFile);
        String msg = Util.isProtected(apkModule);
        if(msg != null){
            logMessage(msg);
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
        AndroidManifestBlock manifest = apkModule.getAndroidManifestBlock();
        printVersionCode(manifest);
        printVersionName(manifest);

        printAppName(apkModule);
        printAppIcon(apkModule);
        printAppRoundIcon(apkModule);
        printAppClass(apkModule);
        printActivities(apkModule);
        printUsesPermissions(apkModule);

        printResList(apkModule);

        printResources(apkModule);
    }
    private void printSourceFile() throws IOException {
        if(options.outputFile == null){
            return;
        }
        if(options.verbose || !options.resources){
            getInfoWriter().writeNameValue("source-file",
                    options.inputFile.getAbsolutePath());
        }
    }
    private void printResources(ApkModule apkModule) throws IOException {
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
    private void printResList(ApkModule apkModule) throws IOException {
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
            EntryGroup entryGroup = packageBlock
                    .getEntryGroup(referenceString.type, referenceString.name);
            if(entryGroup == null){
                continue;
            }
            printEntries(apkModule, "resource", entryGroup.getResourceId());
            return;
        }
        logMessage("WARN: resource not found: " + res);
    }
    private void printPackage(ApkModule apkModule) throws IOException {
        if(!options.packageName){
            return;
        }
        AndroidManifestBlock manifest = apkModule.getAndroidManifestBlock();
        if(manifest != null){
            getInfoWriter().writeNameValue("package" , manifest.getPackageName());
        }
        if(!options.verbose || !apkModule.hasTableBlock()){
            return;
        }
        TableBlock tableBlock = apkModule.getTableBlock();
        InfoWriter infoWriter = getInfoWriter();
        infoWriter.writePackageNames(tableBlock.listPackages());
    }
    private void printVersionCode(AndroidManifestBlock manifest) throws IOException {
        if(!options.versionCode || manifest == null){
            return;
        }
        getInfoWriter().writeNameValue("VersionCode" , manifest.getVersionCode());
    }
    private void printVersionName(AndroidManifestBlock manifest) throws IOException {
        if(!options.versionName || manifest == null){
            return;
        }
        getInfoWriter().writeNameValue("VersionName" , manifest.getVersionName());
    }
    private void printAppName(ApkModule apkModule) throws IOException {
        if(!options.appName){
            return;
        }
        AndroidManifestBlock manifest = apkModule.getAndroidManifestBlock();
        ResXmlElement application = manifest.getApplicationElement();
        ResXmlAttribute attributeLabel = application
                .searchAttributeByResourceId(AndroidManifestBlock.ID_label);
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
        if(!options.appIcon){
            return;
        }
        AndroidManifestBlock manifest = apkModule.getAndroidManifestBlock();
        ResXmlElement application = manifest.getApplicationElement();
        ResXmlAttribute attribute = application
                .searchAttributeByResourceId(AndroidManifestBlock.ID_icon);
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
        if(!options.appRoundIcon){
            return;
        }
        AndroidManifestBlock manifest = apkModule.getAndroidManifestBlock();
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
        if(!options.permissions || !options.verbose){
            return;
        }
        AndroidManifestBlock manifest = apkModule.getAndroidManifestBlock();
        if(manifest == null){
            return;
        }
        List<String> usesPermissions = manifest.getUsesPermissions();
        if(usesPermissions.size() == 0){
            return;
        }
        //printLine("Uses permission (" + usesPermissions.size() + ")");
        String tag = AndroidManifestBlock.TAG_uses_permission;
        InfoWriter infoWriter = getInfoWriter();
        infoWriter.writeArray(tag, usesPermissions.toArray(new String[0]));
    }
    private void printActivities(ApkModule apkModule) throws IOException {
        if(!options.activities){
            return;
        }
        AndroidManifestBlock manifest = apkModule.getAndroidManifestBlock();
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
        if(!options.appClass){
            return;
        }
        AndroidManifestBlock manifest = apkModule.getAndroidManifestBlock();
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
    private String getValueOfName(ResXmlElement element){
        ResXmlAttribute attribute = element
                .searchAttributeByResourceId(AndroidManifestBlock.ID_name);
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

    public static void execute(String[] args) throws ARGException, IOException {
        if(Util.isHelp(args)){
            throw new ARGException(InfoOptions.getHelp());
        }
        InfoOptions option = new InfoOptions();
        option.parse(args);
        Info info = new Info(option);
        info.run();
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

    public static boolean isCommand(String command){
        if(Util.isEmpty(command)){
            return false;
        }
        command=command.toLowerCase().trim();
        return command.equals(ARG_SHORT);
    }

    public static final String ARG_SHORT = "info";
    public static final String DESCRIPTION = "Prints information of apk";

    private static final String LOG_TAG_INFO = "[INFO] ";
}
