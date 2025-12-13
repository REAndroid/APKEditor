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
import com.reandroid.apkeditor.decompile.DecompileOptions;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.dex.model.DexDirectory;
import com.reandroid.dex.model.DexFile;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliWriterSetting;
import com.reandroid.dex.smali.formatters.ResourceIdComment;
import org.jf.baksmali.Baksmali;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.VersionMap;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.raw.HeaderItem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class SmaliDecompiler implements DexDecoder {

    private final TableBlock tableBlock;
    private final DecompileOptions decompileOptions;
    private ResourceComment mComment;
    private SmaliWriterSetting smaliWriterSetting;
    private Opcodes mCurrentOpcodes;
    private APKLogger apkLogger;
    private boolean mDexForCommentLoaded;

    public SmaliDecompiler(TableBlock tableBlock, DecompileOptions decompileOptions) {
        this.tableBlock = tableBlock;
        this.decompileOptions = decompileOptions;
    }

    @Override
    public void decodeDex(DexFileInputSource inputSource, File mainDir) throws IOException {
        if (DecompileOptions.DEX_LIB_INTERNAL.equals(decompileOptions.dexLib)) {
            disassembleWithInternalDexLib(inputSource, mainDir);
        } else {
            disassembleWithJesusFrekeLib(inputSource, mainDir);
        }
        writeDexCache(inputSource, mainDir);
    }
    @Override
    public void decodeDex(ApkModule apkModule, File mainDirectory) throws IOException {
        if (!DecompileOptions.DEX_LIB_INTERNAL.equals(decompileOptions.dexLib)) {
            DexDecoder.super.decodeDex(apkModule, mainDirectory);
            return;
        }
        DexDirectory directory = (DexDirectory) apkModule.getTag(DexDirectory.class);
        if (directory == null) {
            int size = apkModule.listDexFiles().size();
            logMessage("Dex files: " + size);
            if (size > decompileOptions.loadDex) {
                DexDirectory dexDirectory = null;
                if (size < decompileOptions.loadDex * 5) {
                    dexDirectory = loadMinimalDexForComment(apkModule);
                }
                DexDecoder.super.decodeDex(apkModule, mainDirectory);
                if (dexDirectory != null) {
                    dexDirectory.close();
                }
                return;
            }
            logMessage("Loading full dex files ...");
            Predicate<SectionType<?>> filter;
            if (decompileOptions.noDexDebug) {
                filter = sectionType -> sectionType != SectionType.DEBUG_INFO;
            } else {
                filter = null;
            }
            directory = DexDirectory.fromZip(apkModule.getZipEntryMap(), filter);
        }

        File smali = toSmaliRoot(mainDirectory);
        SmaliWriterSetting setting = getSmaliWriterSetting(directory);
        directory.writeSmali(setting, smali, this::logBaksmaliDex);
        setting.clearClassComments();
        setting.clearMethodComments();
        directory.close();

        if (!decompileOptions.noCache) {
            List<DexFileInputSource> dexList = apkModule.listDexFiles();
            for (DexFileInputSource inputSource : dexList) {
                writeDexCache(inputSource, mainDirectory);
            }
        }
    }
    boolean logBaksmaliDex(DexFile dexFile) {
        logMessage("Baksmali<" + dexFile.getDexClassesCount() + ">: " + dexFile.getSimpleName());
        return true;
    }

    private DexDirectory loadMinimalDexForComment(ApkModule apkModule) throws IOException {
        String commentLevel = decompileOptions.commentLevel;
        if (!DecompileOptions.COMMENT_LEVEL_DETAIL.equals(commentLevel) &&
                !DecompileOptions.COMMENT_LEVEL_FULL.equals(commentLevel)) {
            return null;
        }
        logMessage("Loading basic structures of dex ...");
        DexDirectory dexDirectory = DexDirectory.fromZip(
                apkModule.getZipEntryMap(), SectionType.minimal());
        mDexForCommentLoaded = false;
        getSmaliWriterSetting(dexDirectory);
        mDexForCommentLoaded = true;
        return dexDirectory;
    }
    private void disassembleWithJesusFrekeLib(DexFileInputSource inputSource, File mainDir) throws IOException {
        logMessage("Baksmali: " + inputSource.getAlias());
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
    private void disassembleWithInternalDexLib(DexFileInputSource inputSource, File mainDir) throws IOException {
        Predicate<SectionType<?>> filter;
        if (decompileOptions.noDexDebug) {
            filter = sectionType -> sectionType != SectionType.DEBUG_INFO;
        } else {
            filter = null;
        }
        DexFile dexFile = DexFile.read(inputSource.openStream(), filter);
        dexFile.setSimpleName(inputSource.getAlias());
        logBaksmaliDex(dexFile);
        if (dexFile.isMultiLayout()) {
            logMessage("Multi layout dex file: " + inputSource.getAlias()
                    + "version = " + dexFile.getVersion() + ", layouts = " + dexFile.size());
        }
        SmaliWriterSetting setting = getSmaliWriterSetting(dexFile);
        File dir = new File(toSmaliRoot(mainDir), dexFile.buildSmaliDirectoryName());
        dexFile.writeSmali(setting, dir);
        if (!mDexForCommentLoaded) {
            setting.clearClassComments();
            setting.clearMethodComments();
        }
        dexFile.close();
    }
    private void writeDexCache(DexFileInputSource inputSource, File mainDir) throws IOException {
        if (!decompileOptions.noCache) {
            File cache = new File(mainDir, SmaliUtil.CACHE_DIR);
            cache = new File(cache, inputSource.getAlias());
            inputSource.write(cache);
        }
    }
    private File toOutDir(DexFileInputSource inputSource, File mainDir) {
        String name = "classes";
        int num = inputSource.getDexNumber();
        if (num != 0) {
            name = name + num;
        }
        File dir = toSmaliRoot(mainDir);
        dir = new File(dir, name);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
    private File toSmaliRoot(File mainDir) {
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
        if (comment == null) {
            if (tableBlock != null) {
                comment = new ResourceComment(tableBlock);
                this.mComment = comment;
            }
        }
        return mComment;
    }

    private SmaliWriterSetting getSmaliWriterSetting(DexClassRepository classRepository) {
        SmaliWriterSetting setting = getSmaliWriterSetting();
        if (!mDexForCommentLoaded) {
            setting.clearClassComments();
            setting.clearMethodComments();
            String commentLevel = decompileOptions.commentLevel;
            if (DecompileOptions.COMMENT_LEVEL_DETAIL.equals(commentLevel) || DecompileOptions.COMMENT_LEVEL_FULL.equals(commentLevel)) {
                setting.addClassComments(classRepository);
                setting.addMethodComments(classRepository);
            }
        }
        return setting;
    }
    public SmaliWriterSetting getSmaliWriterSetting() {
        SmaliWriterSetting setting = this.smaliWriterSetting;
        if (setting == null) {
            setting = new SmaliWriterSetting();
            this.smaliWriterSetting = setting;
            initializeSmaliWriterSetting(setting);
        }
        return smaliWriterSetting;
    }
    private void initializeSmaliWriterSetting(SmaliWriterSetting setting) {
        initializeSmaliComment(setting);
        setting.setLocalRegistersCount(!decompileOptions.smaliRegisters);
    }
    private void initializeSmaliComment(SmaliWriterSetting setting) {
        String commentLevel = this.decompileOptions.commentLevel;
        if (DecompileOptions.COMMENT_LEVEL_OFF.equals(commentLevel)) {
            setting.setResourceIdComment((ResourceIdComment) null);
            setting.clearClassComments();
            setting.clearMethodComments();
            setting.setEnableComments(false);
            return;
        }
        setting.setEnableComments(true);
        if (tableBlock != null) {
            setting.setResourceIdComment(ResourceIdComment.of(tableBlock.pickOne(), Locale.getDefault()));
        }
        if (DecompileOptions.COMMENT_LEVEL_FULL.equals(commentLevel)) {
            setting.setMaximumCommentLines(-1);
            setting.setCommentUnicodeStrings(true);
        }
    }

    public void setApkLogger(APKLogger apkLogger) {
        this.apkLogger = apkLogger;
    }
    private void logMessage(String msg) {
        APKLogger apkLogger = this.apkLogger;
        if (apkLogger != null) {
            apkLogger.logMessage(msg);
        }
    }
}
