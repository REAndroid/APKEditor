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
package com.reandroid.apkeditor.merge;

 import com.reandroid.apkeditor.BaseCommand;
 import com.reandroid.apkeditor.Util;
 import com.reandroid.apkeditor.common.AndroidManifestHelper;
 import com.reandroid.archive.APKArchive;
 import com.reandroid.archive.WriteProgress;
 import com.reandroid.archive2.Archive;
 import com.reandroid.arsc.value.ResTableEntry;
 import com.reandroid.arsc.value.ResValue;
 import com.reandroid.commons.command.ARGException;
 import com.reandroid.commons.utils.log.Logger;
 import com.reandroid.apk.APKLogger;
 import com.reandroid.apk.ApkBundle;
 import com.reandroid.apk.ApkModule;
 import com.reandroid.arsc.chunk.TableBlock;
 import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
 import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
 import com.reandroid.arsc.chunk.xml.ResXmlElement;
 import com.reandroid.arsc.group.EntryGroup;
 import com.reandroid.arsc.value.Entry;
 import com.reandroid.arsc.value.ValueType;

 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Objects;
 import java.util.zip.ZipEntry;

 public class Merger extends BaseCommand implements WriteProgress {
    private final MergerOptions options;
    private APKLogger mApkLogger;
    public Merger(MergerOptions options){
        this.options=options;
    }
    public void run() throws IOException {
        File dir = options.inputFile;
        boolean extracted = false;
        if(dir.isFile()){
            dir = extractFile(dir);
            extracted = true;
        }
        log("Searching apk files ...");
        ApkBundle bundle=new ApkBundle();
        bundle.setAPKLogger(getAPKLogger());
        bundle.loadApkDirectory(dir, extracted);
        log("Found modules: "+bundle.getApkModuleList().size());
        for(ApkModule apkModule:bundle.getApkModuleList()){
            String protect = Util.isProtected(apkModule);
            if(protect!=null){
                log(options.inputFile.getAbsolutePath());
                log(protect);
                return;
            }
        }
        ApkModule mergedModule=bundle.mergeModules();
        if(options.resDirName!=null){
            log("Renaming resources root dir: "+options.resDirName);
            mergedModule.setResourcesRootDir(options.resDirName);
        }
        if(options.validateResDir){
            log("Validating resources dir ...");
            mergedModule.validateResourcesDir();
        }
        if(options.cleanMeta){
            log("Clearing META-INF ...");
            removeSignature(mergedModule);
            mergedModule.setApkSignatureBlock(null);
        }
        if(mergedModule.hasAndroidManifestBlock()){
            sanitizeManifest(mergedModule);
        }
        Util.addApkEditorInfo(mergedModule, getClass().getSimpleName());
        log("Writing apk ...");
        mergedModule.writeApk(options.outputFile, this);
        if(extracted){
            Util.deleteDir(dir);
        }
        log("Saved to: "+options.outputFile);
        log("Done");
    }
    private File extractFile(File file) throws IOException {
        File tmp = toTmpDir(file);
        log("Extracting to: " + tmp);
        if(tmp.exists()){
            log("Delete: " + tmp);
            Util.deleteDir(tmp);
        }
        tmp.deleteOnExit();
        Archive archive = new Archive(file);
        archive.extract(tmp);
        return tmp;
    }
    private File toTmpDir(File file){
        String name = file.getName();
        int i = name.lastIndexOf('.');
        if(i>0){
            name = name.substring(0, i);
        }
        name=name+"_tmp";
        File dir=file.getParentFile();
        if(dir==null){
            return new File(name);
        }
        return new File(dir, name);
    }
    private void sanitizeManifest(ApkModule apkModule) throws IOException {
        AndroidManifestBlock manifest=apkModule.getAndroidManifestBlock();

        log("Sanitizing manifest ...");
        boolean removed = AndroidManifestHelper.removeApplicationAttribute(manifest,
                AndroidManifestBlock.ID_extractNativeLibs);
        if(removed){
            log("Removed: "+AndroidManifestBlock.NAME_extractNativeLibs);
        }
        removed = AndroidManifestHelper.removeApplicationAttribute(manifest,
                AndroidManifestBlock.ID_isSplitRequired);
        if(removed){
            log("Removed: "+AndroidManifestBlock.NAME_isSplitRequired);
        }
        ResXmlElement application = manifest.getApplicationElement();
        List<ResXmlElement> splitMetaDataElements=AndroidManifestHelper.listSplitRequired(application);
        boolean splits_removed=false;
        for(ResXmlElement meta:splitMetaDataElements){
            if(!splits_removed){
                splits_removed=removeSplitsTableEntry(meta, apkModule);
            }
            log("Removed: "+meta.toString());
            application.removeElement(meta);
        }
        manifest.refresh();
    }
    private boolean removeSplitsTableEntry(ResXmlElement metaElement, ApkModule apkModule) throws IOException {
        ResXmlAttribute nameAttribute = metaElement.searchAttributeByResourceId(AndroidManifestBlock.ID_name);
        if(nameAttribute.getValueType()!= ValueType.STRING){
            return false;
        }
        if(!"com.android.vending.splits".equals(nameAttribute.getValueAsString())){
            return false;
        }
        ResXmlAttribute valueAttribute=metaElement.searchAttributeByResourceId(
                AndroidManifestBlock.ID_value);
        if(valueAttribute==null){
            valueAttribute=metaElement.searchAttributeByResourceId(
                    AndroidManifestBlock.ID_resource);
        }
        if(valueAttribute==null || valueAttribute.getValueType()!=ValueType.REFERENCE){
            return false;
        }
        TableBlock tableBlock=apkModule.getTableBlock();
        EntryGroup entryGroup = tableBlock.search(valueAttribute.getData());
        if(entryGroup==null){
            return false;
        }
        APKArchive apkArchive=apkModule.getApkArchive();
        List<Entry> entryList = entryGroup.listItems();
        for(Entry entryBlock:entryList){
            if(entryBlock==null){
                continue;
            }
            ResValue resValue = ((ResTableEntry)entryBlock.getTableEntry()).getValue();
            String path = resValue.getValueAsString();
            log("Removed from table: "+path);
            //Remove file entry
            apkArchive.remove(path);
            // It's not safe to destroy entry, resource id might be used in dex code.
            // Better replace it with boolean value
            entryBlock.setNull(true);
        }
        tableBlock.refresh();
        return true;
    }
    @Override
    public void onCompressFile(String path, int method, long length) {
        StringBuilder builder=new StringBuilder();
        builder.append("Writing:");
        if(method == ZipEntry.STORED){
            builder.append(" method=STORED");
        }
        builder.append(" total=");
        builder.append(length);
        builder.append(" bytes : ");
        if(path.length()>30){
            path=path.substring(path.length()-30);
        }
        builder.append(path);
        logSameLine(builder.toString());
    }
    private APKLogger getAPKLogger(){
        if(mApkLogger!=null){
            return mApkLogger;
        }
        mApkLogger = new APKLogger() {
            @Override
            public void logMessage(String msg) {
                Logger.i(getLogTag()+msg);
            }
            @Override
            public void logError(String msg, Throwable tr) {
                Logger.e(getLogTag()+msg, tr);
            }
            @Override
            public void logVerbose(String msg) {
                if(msg.length()>30){
                    msg=msg.substring(msg.length()-30);
                }
                Logger.sameLine(getLogTag()+msg);
            }
        };
        return mApkLogger;
    }
    public static void execute(String[] args) throws ARGException, IOException {
        if(Util.isHelp(args)){
            throw new ARGException(MergerOptions.getHelp());
        }
        MergerOptions option=new MergerOptions();
        option.parse(args);
        File outFile=option.outputFile;
        if(Objects.equals(outFile.getParentFile(), option.inputFile)){
            throw new IOException("Output file can not be inside input directory!");
        }
        Util.deleteEmptyDirectories(outFile);
        if(outFile.exists()){
            if(!option.force){
                throw new ARGException("Path already exists: "+outFile);
            }
            log("Deleting: "+outFile);
            Util.deleteDir(outFile);
        }
        log("Merging ...\n"+option);
        Merger merger=new Merger(option);
        merger.run();
    }
    private static void logSameLine(String msg){
        Logger.sameLine(getLogTag()+msg);
    }
    private static void log(String msg){
        Logger.i(getLogTag()+msg);
    }
    private static String getLogTag(){
        return "[MERGE] ";
    }
    public static boolean isCommand(String command){
        if(Util.isEmpty(command)){
            return false;
        }
        command=command.toLowerCase().trim();
        return command.equals(ARG_SHORT) || command.equals(ARG_LONG);
    }
    public static final String ARG_SHORT="m";
    public static final String ARG_LONG="merge";
    public static final String DESCRIPTION="Merges split apk files from directory or XAPK, APKM, APKS ...";
}
