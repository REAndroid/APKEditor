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
package com.reandroid.apkeditor.protect;

import com.reandroid.apk.ApkModule;
import com.reandroid.apk.DexFileInputSource;
import com.reandroid.app.AndroidManifest;
import com.reandroid.archive.block.CentralEntryHeader;
import com.reandroid.archive.block.DataDescriptor;
import com.reandroid.archive.block.LocalFileHeader;
import com.reandroid.archive.writer.ApkFileWriter;
import com.reandroid.archive.writer.HeaderInterceptor;
import com.reandroid.arsc.chunk.TableBlock;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ProtectedFileWriter implements HeaderInterceptor {

    private final ApkModule apkModule;
    private final File file;
    private final Set<String> mProtectedFiles;

    public ProtectedFileWriter(ApkModule apkModule, File file) {
        this.apkModule = apkModule;
        this.file = file;
        this.mProtectedFiles = new HashSet<>();
    }

    public void write() throws IOException {
        ApkFileWriter writer = apkModule.createApkFileWriter(this.file);
        writer.getInterceptorChain().setHeaderInterceptor(this);
        writer.write();
        writer.close();
    }

    @Override
    public void onWriteLfh(LocalFileHeader lfh) {
        String name = lfh.getFileName();
        if (needsProtection(name)) {
            mProtectedFiles.add(name);
            lfh.getGeneralPurposeFlag().setEncryption(true);
        }
    }

    @Override
    public void onWriteDD(DataDescriptor dataDescriptor) {
    }

    @Override
    public void onWriteCeh(CentralEntryHeader ceh) {
        if (mProtectedFiles.contains(ceh.getFileName())) {
            ceh.getGeneralPurposeFlag().setEncryption(true);
        }
    }

    private boolean needsProtection(String name) {
        if (AndroidManifest.FILE_NAME.equals(name)) {
            return true;
        }
        if (TableBlock.FILE_NAME.equals(name)) {
            return true;
        }
        if (name.startsWith("lib/") && name.endsWith(".so")) {
            return true;
        }
        return DexFileInputSource.isDexName(name);
    }
}
