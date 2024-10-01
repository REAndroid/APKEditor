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
import com.reandroid.apkeditor.APKEditor;
import com.reandroid.arsc.ARSCLib;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.UnknownChunk;
import com.reandroid.arsc.item.ByteArray;
import com.reandroid.arsc.item.FixedLengthString;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.pool.TypeStringPool;

public class TableConfuser extends Confuser {

    public TableConfuser(Protector protector) {
        super(protector, "TableConfuser: ");
    }

    @Override
    public void confuse() {
        logMessage("Confusing ...");
        confuseWithUnknownChunk();
        confuseTypeNames();
    }
    private void confuseWithUnknownChunk() {
        ApkModule apkModule = getApkModule();
        TableBlock tableBlock = apkModule.getTableBlock();
        UnknownChunk unknownChunk = new UnknownChunk();
        FixedLengthString fixedLengthString = new FixedLengthString(256);
        fixedLengthString.set(APKEditor.getRepo());
        ByteArray extra = unknownChunk.getHeaderBlock().getExtraBytes();
        byte[] bytes = fixedLengthString.getBytes();
        extra.setSize(bytes.length);
        extra.putByteArray(0, bytes);
        fixedLengthString.set(ARSCLib.getRepo());
        extra = unknownChunk.getBody();
        bytes = fixedLengthString.getBytes();
        extra.setSize(bytes.length);
        extra.putByteArray(0, bytes);
        fixedLengthString.set(ARSCLib.getRepo());
        unknownChunk.refresh();
        tableBlock.getFirstPlaceHolder().setItem(unknownChunk);
        tableBlock.refresh();
    }
    private void confuseTypeNames() {
        //TODO: let the user choose which types to confuse,
        // and use user provided type names
        ApkModule apkModule = getApkModule();
        TableBlock tableBlock = apkModule.getTableBlock();
        for(PackageBlock packageBlock:tableBlock.listPackages()){
            TypeStringPool pool = packageBlock.getTypeStringPool();
            for(TypeString typeString : pool) {
                String type = typeString.get();
                if ("attr".equals(type) ) {
                    typeString.set("style");
                } else if ("style".equals(type)) {
                    typeString.set("plurals");
                } else if ("id".equals(type)) {
                    typeString.set("attr");
                } else if ( "mipmap".equals(type)) {
                    typeString.set("id");
                } else {
                    continue;
                }
                logVerbose("'" + type + "' -> '" + typeString.get() + "'");
            }
        }
        tableBlock.refresh();
    }
}
