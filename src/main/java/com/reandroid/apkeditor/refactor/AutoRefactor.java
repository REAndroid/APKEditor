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
package com.reandroid.apkeditor.refactor;

import com.reandroid.lib.apk.ApkModule;
import com.reandroid.lib.apk.ResFile;
import com.reandroid.lib.apk.ResourceIds;
import com.reandroid.lib.arsc.chunk.TableBlock;

import java.io.IOException;
import java.util.List;

public class AutoRefactor {
    private final ApkModule mApkModule;
    public AutoRefactor(ApkModule apkModule){
        this.mApkModule=apkModule;
    }
    public int refactor() throws IOException {
        ResourceIds refactoredId=buildRefactor();
        TableBlock tableBlock=mApkModule.getTableBlock();
        int renameCount=refactoredId.applyTo(tableBlock);
        List<ResFile> resFileList = mApkModule.listResFiles();
        for(ResFile resFile:resFileList){
            String path=RefactorUtil.RES_DIR+"/"+resFile.buildPath();
            resFile.setFilePath(path);
        }
        return renameCount;
    }
    private ResourceIds buildRefactor() throws IOException {
        ResourceIds.Table obfTable=new ResourceIds.Table();
        ResourceIds resourceIds=new ResourceIds(obfTable);
        TableBlock tableBlock=mApkModule.getTableBlock();
        resourceIds.loadTableBlock(tableBlock);
        ResourceIds.Table table=new ResourceIds.Table();
        for(ResourceIds.Table.Package obfPackage: obfTable.listPackages()){
            ResourceIds.Table.Package pkg=new ResourceIds.Table.Package(obfPackage.id);
            pkg.name=obfPackage.name;
            for(ResourceIds.Table.Package.Type obfType:obfPackage.listTypes()){
                EntryRefactor entryRefactor=new EntryRefactor(tableBlock, obfType);
                pkg.add(entryRefactor.refactorAll());
            }
            table.add(pkg);
        }
        return new ResourceIds(table);
    }
}
