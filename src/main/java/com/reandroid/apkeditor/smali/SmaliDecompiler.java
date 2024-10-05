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
import com.reandroid.apk.ApkModule;
import com.reandroid.apk.DexDecoder;
import com.reandroid.apk.DexFileInputSource;
import com.reandroid.apkeditor.APKEditor;
import com.reandroid.apkeditor.decompile.DecompileOptions;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.dex.common.AnnotationVisibility;
import com.reandroid.dex.data.AnnotationItem;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.dex.model.DexDirectory;
import com.reandroid.dex.model.DexFile;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.SmaliWriterSetting;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ArrayCollection;
import org.jf.baksmali.Baksmali;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.VersionMap;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.raw.HeaderItem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SmaliDecompiler implements DexDecoder {

    private final TableBlock tableBlock;
    private final DecompileOptions decompileOptions;
    private ResourceComment mComment;
    private Opcodes mCurrentOpcodes;
    private APKLogger apkLogger;

    public SmaliDecompiler(TableBlock tableBlock, DecompileOptions decompileOptions){
        this.tableBlock = tableBlock;
        this.decompileOptions = decompileOptions;
    }
    @Deprecated
    public SmaliDecompiler(TableBlock tableBlock){
        this(tableBlock, new DecompileOptions());
    }
    @Override
    public void decodeDex(DexFileInputSource inputSource, File mainDir) throws IOException {
        logMessage("Baksmali: " + inputSource.getAlias());
        if(APKEditor.isExperimental()) {
            disassembleDexFileExperimental(inputSource, mainDir);
        }else {
            disassembleJesusFreke(inputSource, mainDir);
        }
        writeDexCache(inputSource, mainDir);
    }
    @Override
    public void decodeDex(ApkModule apkModule, File mainDirectory) throws IOException {
        if(!APKEditor.isExperimental()) {
            DexDecoder.super.decodeDex(apkModule, mainDirectory);
            return;
        }
        DexDirectory directory = (DexDirectory) apkModule.getTag(DexDirectory.class);
        if(directory == null) {
            if(!canLoadFullDex(apkModule)) {
                DexDecoder.super.decodeDex(apkModule, mainDirectory);
                return;
            }
            logMessage("Loading full dex ...");
            directory = DexDirectory.fromZip(apkModule.getZipEntryMap());
        }
        if(decompileOptions.noDexDebug) {
            logMessage("Clean debug info ...");
            clearDebug_DirtyMethod(directory);
            directory.refresh();
        }

        logMessage("Dumping smali ...");
        File smali = toSmaliRoot(mainDirectory);
        SmaliWriterSetting setting = new SmaliWriterSetting();
        setting.setResourceIdComment(tableBlock.pickOne());
        setting.addClassComments(directory);
        setting.addMethodComments(directory);
        SmaliWriter smaliWriter = new SmaliWriter();
        smaliWriter.setWriterSetting(setting);
        logMessage("Baksmali ...");
        directory.writeSmali(smaliWriter, smali);
        directory.close();

        List<DexFileInputSource> dexList = apkModule.listDexFiles();
        for(DexFileInputSource inputSource : dexList) {
            writeDexCache(inputSource, mainDirectory);
        }
    }
    private boolean canLoadFullDex(ApkModule apkModule) {
        int CLASSES_LIMIT = 5;
        int size = apkModule.listDexFiles().size();
        logMessage("Total dex files: " + size);
        if(size > CLASSES_LIMIT) {
            logMessage("Huge classes your memory might not handle it, decoding separately without advanced features." +
                    " You can disable this restrictions by increasing \"CLASSES_LIMIT\" variable here on source code");
            return false;
        }
        return true;
    }

    private void disassembleJesusFreke(DexFileInputSource inputSource, File mainDir) throws IOException {
        File dir = toOutDir(inputSource, mainDir);
        BaksmaliOptions options = new BaksmaliOptions();
        options.localsDirective = true;
        options.sequentialLabels = true;
        options.skipDuplicateLineNumbers = true;
        options.debugInfo = !decompileOptions.noDexDebug;
        options.dumpMarkers = decompileOptions.dexMarkers;
        options.setCommentProvider(getComment());
        DexBackedDexFile dexFile = getInputDexFile(inputSource, options);
        Baksmali.disassembleDexFile(dexFile, dir, 1, options);
    }
    private void disassembleDexFileExperimental(DexFileInputSource inputSource, File mainDir) throws IOException {
        DexFile dexFile = DexFile.read(inputSource.openStream());
        dexFile.setSimpleName(inputSource.getAlias());
        if(decompileOptions.noDexDebug) {
            logMessage("Clean debug info ...");
            clearDebug_DirtyMethod(dexFile);
            dexFile.refresh();
        }
        SmaliWriterSetting setting = new SmaliWriterSetting();
        setting.setResourceIdComment(tableBlock.pickOne());
        setting.addClassComments(dexFile);
        setting.addMethodComments(dexFile);
        SmaliWriter smaliWriter = new SmaliWriter();
        smaliWriter.setWriterSetting(setting);
        dexFile.writeSmali(smaliWriter, toSmaliRoot(mainDir));
        dexFile.close();
    }
    private void clearDebug_DirtyMethod(DexClassRepository repository) {
        repository.clearDebug();
        Set<TypeKey> removeSet = new HashSet<>();
        Iterator<AnnotationItem> iterator = repository.getItems(SectionType.ANNOTATION_ITEM);
        while (iterator.hasNext()) {
            AnnotationItem annotationItem = iterator.next();
            TypeKey typeKey = annotationItem.getTypeKey();
            if(annotationItem.getVisibility() == AnnotationVisibility.BUILD ||
                    removeAnnotation(typeKey)) {
                removeSet.add(typeKey);
            }
        }
        List<TypeKey> removedList = new ArrayCollection<>(removeSet);
        removedList.sort(CompareUtil.getComparableComparator());
        for(TypeKey typeKey : removedList) {
            logMessage("   Removed @: " + typeKey);
            repository.removeAnnotations(typeKey);
        }
    }
    private boolean removeAnnotation(TypeKey typeKey) {
        String name = typeKey.getTypeName();
        if(name.startsWith("Lkotlin")) {
            return true;
        }
        if(name.startsWith("Ljava/")) {
            return true;
        }
        if(name.startsWith("Ljavax/")) {
            return true;
        }
        if(name.contains("SourceDebugExtension")) {
            return true;
        }
        return name.contains("Null");
    }
    private void writeDexCache(DexFileInputSource inputSource, File mainDir) throws IOException {
        File cache = new File(mainDir, SmaliUtil.CACHE_DIR);
        cache = new File(cache, inputSource.getAlias());
        inputSource.write(cache);
    }
    private File toOutDir(DexFileInputSource inputSource, File mainDir){
        String name = "classes";
        int num = inputSource.getDexNumber();
        if(num != 0){
            name = name + num;
        }
        File dir = toSmaliRoot(mainDir);
        dir = new File(dir, name);
        if(!dir.exists()){
            dir.mkdirs();
        }
        return dir;
    }
    private File toSmaliRoot(File mainDir){
        return new File(mainDir, DexDecoder.SMALI_DIRECTORY_NAME);
    }

    private DexBackedDexFile getInputDexFile(DexFileInputSource inputSource, BaksmaliOptions options) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        inputSource.write(outputStream);
        outputStream.close();
        byte[] bytes = outputStream.toByteArray();
        int version = HeaderItem.getVersion(bytes, 0);
        int api = VersionMap.mapDexVersionToApi(version);
        options.apiLevel = api;
        Opcodes opcodes = this.mCurrentOpcodes;
        if (opcodes == null || api != opcodes.api) {
            opcodes = Opcodes.forApi(api);
            this.mCurrentOpcodes = opcodes;
        }
        return new DexBackedDexFile(opcodes, bytes);
    }
    public ResourceComment getComment() {
        ResourceComment comment = this.mComment;
        if(comment == null){
            if(tableBlock != null){
                comment = new ResourceComment(tableBlock);
                this.mComment = comment;
            }
        }
        return mComment;
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
}
