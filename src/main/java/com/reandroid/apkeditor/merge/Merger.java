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
 import com.reandroid.archive.WriteProgress;
 import com.reandroid.archive.ZipAlign;
 import com.reandroid.commons.command.ARGException;
 import com.reandroid.commons.utils.log.Logger;
 import com.reandroid.lib.apk.APKLogger;
 import com.reandroid.lib.apk.ApkBundle;
 import com.reandroid.lib.apk.ApkModule;
 import com.reandroid.lib.arsc.chunk.TableBlock;
 import com.reandroid.lib.arsc.chunk.xml.AndroidManifestBlock;
 import com.reandroid.lib.arsc.chunk.xml.ResXmlAttribute;
 import com.reandroid.lib.arsc.chunk.xml.ResXmlElement;
 import com.reandroid.lib.arsc.group.EntryGroup;
 import com.reandroid.lib.arsc.value.EntryBlock;
 import com.reandroid.lib.arsc.value.ValueType;

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
        log("Searching apk files ...");
        ApkBundle bundle=new ApkBundle();
        bundle.setAPKLogger(getAPKLogger());
        bundle.loadApkDirectory(options.inputFile);
        log("Found modules: "+bundle.getApkModuleList().size());
        ApkModule mergedModule=bundle.mergeModules();
        if(options.resDirName!=null){
            log("Renaming resources root dir: "+options.resDirName);
            mergedModule.setResourcesRootDir(options.resDirName);
        }
        if(options.validateResDir){
            log("Validating resources dir ...");
            mergedModule.validateResourcesDir();
        }
        removeSignature(mergedModule);
        if(mergedModule.hasAndroidManifestBlock()){
            sanitizeManifest(mergedModule.getAndroidManifestBlock()
                    , mergedModule.getTableBlock());
        }
        log("Writing apk ...");
        mergedModule.writeApk(options.outputFile, this);
        log("Zip align ...");
        ZipAlign.align4(options.outputFile);
        log("Saved to: "+options.outputFile);
        log("Done");
    }
    private void sanitizeManifest(AndroidManifestBlock manifest, TableBlock tableBlock){
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
                splits_removed=removeSplitsTableEntry(meta, tableBlock);
            }
            log("Removed: "+meta.toString());
            application.removeElement(meta);
        }
        manifest.refresh();
    }
    private boolean removeSplitsTableEntry(ResXmlElement metaElement, TableBlock tableBlock){
        ResXmlAttribute nameAttribute = metaElement.searchAttributeByResourceId(AndroidManifestBlock.ID_name);
        if(nameAttribute.getValueType()!= ValueType.STRING){
            return false;
        }
        if(!"com.android.vending.splits".equals(nameAttribute.getValueAsString())){
            return false;
        }
        // TODO: add on AndroidManifestBlock as ID_*
        int idValue=0x01010024;
        int idResource=0x01010025;

        ResXmlAttribute valueAttribute=metaElement.searchAttributeByResourceId(idValue);
        if(valueAttribute==null){
            valueAttribute=metaElement.searchAttributeByResourceId(idResource);
        }
        if(valueAttribute==null || valueAttribute.getValueType()!=ValueType.REFERENCE){
            return false;
        }
        EntryGroup entryGroup = tableBlock.search(valueAttribute.getRawValue());
        if(entryGroup==null){
            return false;
        }
        for(EntryBlock entryBlock:entryGroup.listItems()){
            log("Removed from table: "+entryBlock);
            // It's not safe to destroy entry, resource id might be used in dex code.
            // Better replace it with boolean value
            entryBlock.setValueAsBoolean(false);
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
    public static final String DESCRIPTION="Merges split apk files from directory";
}
