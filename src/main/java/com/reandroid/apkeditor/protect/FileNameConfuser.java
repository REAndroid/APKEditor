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
import com.reandroid.apk.ResFile;
import com.reandroid.apk.UncompressedFiles;
import com.reandroid.apkeditor.utils.CyclicIterator;
import com.reandroid.archive.Archive;

public class FileNameConfuser extends Confuser {

    private final CyclicIterator<String> namesIterator;

    public FileNameConfuser(Protector protector) {
        super(protector, "FileNameConfuser: ");
        this.namesIterator = new CyclicIterator<>(loadFileNames());
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
            String newPath = replaceSimpleName(path, iterator.next());
            if (!containsFilePath(newPath)) {
                return newPath;
            }
        }
        return null;
    }

    private static String replaceSimpleName(String path, String symbol) {
        int i = path.lastIndexOf('/');
        String dirName;
        String simpleName;
        if (i < 0) {
            dirName = "";
            simpleName = path;
        } else {
            i = i + 1;
            dirName = path.substring(0, i);
            simpleName = path.substring(i);
        }
        i = simpleName.lastIndexOf('.');
        String ext;
        if (i < 0) {
            ext = ".";
        } else {
            if (simpleName.endsWith(".9.png")) {
                ext = ".9.png";
            } else {
                ext = simpleName.substring(i);
            }
        }
        return dirName + symbol + ext;
    }
    private static String[] loadFileNames() {
        return new String[]{
                ".",
                "//",
                "///",
                "////",
                "\\\\",
                "\\\\\\",
                "\\/",
                " ",
                "  ",
                "classes.dex",
                "AndroidManifest.xml",
                "AndroidManifest",
                "resources.arsc",
        };
    }
}
