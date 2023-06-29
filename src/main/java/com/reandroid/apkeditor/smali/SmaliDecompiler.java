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
import com.reandroid.apk.DexDecoder;
import com.reandroid.apk.DexFileInputSource;
import com.reandroid.arsc.chunk.TableBlock;
import org.jf.baksmali.Baksmali;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class SmaliDecompiler implements DexDecoder {
    private final TableBlock tableBlock;
    private ResourceComment mComment;
    private APKLogger apkLogger;
    public SmaliDecompiler(TableBlock tableBlock){
        this.tableBlock = tableBlock;
    }
    @Override
    public boolean decodeDex(DexFileInputSource inputSource, File mainDir) throws IOException {
        logMessage("Smali: " + inputSource.getAlias());
        disassembleDexFile(inputSource, mainDir);
        File cache = new File(mainDir, SmaliUtil.CACHE_DIR);
        cache = new File(cache, inputSource.getAlias());
        inputSource.write(cache);
        return true;
    }
    private void disassembleDexFile(DexFileInputSource inputSource, File mainDir) throws IOException {
        File dir = toOutDir(inputSource, mainDir);
        BaksmaliOptions options = new BaksmaliOptions();
        options.localsDirective = true;
        options.setCommentProvider(getComment());
        DexBackedDexFile dexFile = getInputDexFile(inputSource, options);
        Baksmali.disassembleDexFile(dexFile, dir, 1, options);
    }
    private File toOutDir(DexFileInputSource inputSource, File mainDir){
        String name = "classes";
        int num = inputSource.getDexNumber();
        if(num != 0){
            name = name + num;
        }
        File dir = new File(mainDir, "smali");
        dir = new File(dir, name);
        if(!dir.exists()){
            dir.mkdirs();
        }
        return dir;
    }

    private DexBackedDexFile getInputDexFile(DexFileInputSource inputSource, BaksmaliOptions options) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        inputSource.write(outputStream);
        outputStream.close();
        return new DexBackedDexFile(Opcodes.forApi(options.apiLevel), outputStream.toByteArray());
    }
    public ResourceComment getComment() {
        ResourceComment comment = this.mComment;
        if(comment == null){
            comment = new ResourceComment(tableBlock);
            this.mComment = comment;
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
