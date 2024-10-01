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
import com.reandroid.apk.ResFile;
import com.reandroid.apk.UncompressedFiles;
import com.reandroid.apkeditor.utils.CyclicIterator;
import com.reandroid.archive.Archive;
import com.reandroid.utils.collection.CollectionUtil;

import java.util.List;

public class DirectoryConfuser extends Confuser {

    private final CyclicIterator<String> namesIterator;

    public DirectoryConfuser(Protector protector) {
        super(protector, "DirectoryConfuser: ");
        this.namesIterator = new CyclicIterator<>(loadDirNameList(protector.getApkModule()));
    }

    @Override
    public void confuse() {
        logMessage("Confusing ...");

        ApkModule apkModule = getApkModule();
        UncompressedFiles uf = apkModule.getUncompressedFiles();

        for(ResFile resFile : getApkModule().listResFiles()){
            int method = resFile.getInputSource().getMethod();
            String pathNew = generateNewPath(resFile);
            if(pathNew != null) {
                String path = resFile.getFilePath();
                if(method == Archive.STORED) {
                    uf.replacePath(path, pathNew);
                }
                resFile.setFilePath(pathNew);
                onPathChanged(path, pathNew);
            }
        }
    }
    private String generateNewPath(ResFile resFile) {
        if (isKeepType(resFile.pickOne().getTypeName())) {
            return null;
        }
        return generateNewPath(resFile.getFilePath());
    }
    private String generateNewPath(String path) {
        CyclicIterator<String> iterator = this.namesIterator;
        iterator.resetCycleCount();
        while (iterator.getCycleCount() == 0) {
            String newPath = replaceDirectory(path, iterator.next());
            if (!containsFilePath(newPath)) {
                return newPath;
            }
        }
        return null;
    }

    private static String replaceDirectory(String path, String dirName) {
        int i = path.lastIndexOf('/');
        if (i < 0) {
            i = 0;
        } else {
            i = i + 1;
            if (i == path.length()) {
                i = i - 1;
            }
        }
        String simpleName = path.substring(i);
        if (dirName.length() != 0) {
            dirName = dirName + "/";
        }
        return dirName + simpleName;
    }
    private static String[] loadDirNameList(ApkModule apkModule) {
        List<String> nameList = CollectionUtil.asList(
                "AndroidManifest.xml",
                "/AndroidManifest.xml",
                "resources.arsc",
                "/resources.arsc",
                "classes.dex",
                "/classes.dex",
                "kotlin",
                "META-INF",
                "",
                "kotlin/annotation",
                "kotlin/collections",
                "kotlin/coroutines",
                "kotlin/internal",
                "kotlin/ranges",
                "kotlin/reflect",
                "res/values/arrays.xml",
                "res/values/attrs.xml",
                "res/values/bools.xml",
                "res/values/colors.xml",
                "res/values/dimens.xml",
                "res/values/drawables.xml",
                "res/values/ids.xml",
                "res/values/integers.xml",
                "res/values/plurals.xml",
                "res/values/public.xml",
                "res/values/strings.xml",
                "res/values/styles.xml"
        );
        List<DexFileInputSource> dexList = apkModule.listDexFiles();
        int size = dexList.size();
        for (int i = 1; i < size; i++) {
            nameList.add(dexList.get(i).getAlias());
        }
        return nameList.toArray(new String[0]);
    }
}
