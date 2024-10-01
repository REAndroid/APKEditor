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

import com.reandroid.apk.APKLogger;
import com.reandroid.apk.ApkModule;
import com.reandroid.archive.InputSource;
import com.reandroid.archive.ZipEntryMap;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.ComputeIterator;

import java.util.Set;

public abstract class Confuser implements APKLogger {

    private final Protector protector;
    private final String logTag;
    private Set<String> filePaths;

    public Confuser(Protector protector, String logTag) {
        this.protector = protector;
        this.logTag = logTag;
    }

    public abstract void confuse();


    public boolean containsFilePath(String path) {
        return getFilePaths().contains(path);
    }
    public void onPathChanged(String original, String newPath) {
        Set<String> filePaths = getFilePaths();
        filePaths.add(newPath);
        logVerbose(original + " -> " + newPath);
    }
    public Set<String> getFilePaths() {
        if (this.filePaths == null) {

            ZipEntryMap zipEntryMap = getApkModule().getZipEntryMap();

            this.filePaths = CollectionUtil.toHashSet(
                    ComputeIterator.of(zipEntryMap.iterator(), InputSource::getAlias));
            this.filePaths.addAll(CollectionUtil.toHashSet(
                    ComputeIterator.of(zipEntryMap.iterator(), InputSource::getName)));

        }
        return filePaths;
    }
    public boolean isKeepType(String type) {
        return getOptions().keepTypes.contains(type);
    }
    public Protector getProtector() {
        return protector;
    }
    public ProtectorOptions getOptions() {
        return getProtector().getOptions();
    }
    public ApkModule getApkModule() {
        return getProtector().getApkModule();
    }

    @Override
    public void logMessage(String msg) {
        protector.logMessage(logTag + msg);
    }
    @Override
    public void logError(String msg, Throwable tr) {
        protector.logError(msg, tr);
    }
    @Override
    public void logVerbose(String msg) {
        protector.logVerbose(msg);
    }
}
