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
package com.reandroid.apkeditor.info;

import com.android.org.kxml2.io.KXmlSerializer;
import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.utils.HexUtil;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.arsc.value.ResValue;
import com.reandroid.arsc.value.ResValueMap;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class InfoWriterXml extends InfoWriter{
    private KXmlSerializer mSerializer;
    private int mIndent;
    public InfoWriterXml(Writer writer) {
        super(writer);
    }
    @Override
    public void writeResources(PackageBlock packageBlock, List<String> typeFilters, boolean writeEntries) throws IOException {
        KXmlSerializer serializer = getSerializer();
        int indent = mIndent + 2;
        mIndent = indent;
        writeIndent(serializer, indent);
        serializer.startTag(null, "package");
        serializer.attribute(null, "id", HexUtil.toHex2((byte) packageBlock.getId()));
        serializer.attribute(null, "name", packageBlock.getName());
        packageBlock.sortTypes();

        for(SpecTypePair specTypePair : packageBlock.listSpecTypePairs()){
            if(!contains(specTypePair, typeFilters)){
                continue;
            }
            writeResources(specTypePair, writeEntries);
        }

        writeIndent(serializer, indent);
        indent = indent - 2;
        mIndent = indent;
        serializer.endTag(null, "package");
    }
    public void writeResources(SpecTypePair specTypePair, boolean writeEntries) throws IOException {
        KXmlSerializer serializer = getSerializer();
        int indent = mIndent + 2;
        mIndent = indent;
        writeIndent(serializer, indent);
        serializer.startTag(null, "type");

        serializer.attribute(null, "name",
                specTypePair.getTypeName());
        serializer.attribute(null, "id",
                Integer.toString(specTypePair.getId()));
        serializer.attribute(null, "entryCount",
                Integer.toString(specTypePair.getHighestEntryCount()));

        Iterator<ResourceEntry> iterator = specTypePair.getResources();
        while (iterator.hasNext()){
            ResourceEntry resourceEntry = iterator.next();
            writeResources(resourceEntry, writeEntries);
        }

        writeIndent(serializer, indent);
        indent = indent - 2;
        mIndent = indent;
        serializer.endTag(null, "type");
        serializer.flush();
    }
    @Override
    public void writeResources(ResourceEntry resourceEntry, boolean writeEntries) throws IOException {
        if(resourceEntry.isEmpty()){
            return;
        }
        KXmlSerializer serializer = getSerializer();
        int indent = mIndent + 2;
        mIndent = indent;
        writeIndent(serializer, indent);
        serializer.startTag(null, NAME_RESOURCE);
        serializer.attribute(null, "id", HexUtil.toHex8(resourceEntry.getResourceId()));
        serializer.attribute(null, "type", resourceEntry.getType());
        serializer.attribute(null, "name", resourceEntry.getName());

        if(writeEntries){

            writeEntries(resourceEntry);

            writeIndent(serializer, indent);
        }

        indent = indent - 2;
        mIndent = indent;

        serializer.endTag(null, NAME_RESOURCE);
        serializer.flush();
    }
    public void writeEntries(ResourceEntry entryList) throws IOException {
        for(Entry entry : entryList){
            writeEntry(entry);
        }
    }
    public void writeEntry(Entry entry) throws IOException {
        KXmlSerializer serializer = getSerializer();
        int indent = mIndent + 2;
        mIndent = indent;
        writeIndent(serializer, indent);
        serializer.startTag(null, TAG_CONFIG);
        serializer.attribute(null, NAME_QUALIFIERS,
                entry.getResConfig().getQualifiers());
        if(entry.isComplex()){
            writeBagEntry(entry);
        }else {
            writeResEntry(entry);
        }
        writeIndent(serializer, indent);
        serializer.endTag(null, TAG_CONFIG);
        indent = indent - 2;
        mIndent = indent;
    }
    private void writeResEntry(Entry entry) throws IOException {
        ResValue resValue = entry.getResValue();
        if(resValue == null){
            return;
        }
        KXmlSerializer serializer = getSerializer();
        int indent = mIndent + 2;
        mIndent = indent;
        writeIndent(serializer, indent);
        serializer.startTag(null, TAG_VALUE);
        serializer.attribute(null, "type", resValue.getValueType().name());
        serializer.text(getValueAsString(resValue));
        serializer.endTag(null, TAG_VALUE);
        indent = indent - 2;
        mIndent = indent;
    }
    private void writeBagEntry(Entry entry) throws IOException {
        KXmlSerializer serializer = getSerializer();
        int indent = mIndent + 2;
        mIndent = indent;
        writeIndent(serializer, indent);
        serializer.startTag(null, TAG_BAG);
        serializer.attribute(null, "parent",
                HexUtil.toHex8(((ResTableMapEntry)entry.getTableEntry()).getParentId()));
        ResValueMapArray mapArray = entry.getResValueMapArray();
        for(ResValueMap resValueMap : mapArray.getChildes()){
            writeValueMap(resValueMap);
        }
        writeIndent(serializer, indent);
        serializer.endTag(null, TAG_BAG);
        indent = indent - 2;
        mIndent = indent;
    }
    private void writeValueMap(ResValueMap resValueMap) throws IOException {
        KXmlSerializer serializer = getSerializer();
        int indent = mIndent + 2;
        mIndent = indent;
        writeIndent(serializer, indent);
        serializer.startTag(null, TAG_VALUE);
        serializer.attribute(null, "name",
                HexUtil.toHex8(resValueMap.getNameResourceID()));
        serializer.attribute(null, "type", resValueMap.getValueType().name());
        serializer.text(getValueAsString(resValueMap));
        serializer.endTag(null, TAG_VALUE);
        indent = indent - 2;
        mIndent = indent;
    }
    @Override
    public void writePackageNames(Collection<PackageBlock> packageBlocks) throws IOException {
        if(packageBlocks == null || packageBlocks.size() == 0){
            return;
        }
        int level = INDENT;
        KXmlSerializer serializer = getSerializer();
        writeIndent(serializer, level);
        serializer.startTag(null, TAG_RES_PACKAGES);
        serializer.attribute(null, "count", Integer.toString(packageBlocks.size()));
        level = level + 2;
        for(PackageBlock packageBlock : packageBlocks){
            writeIndent(serializer, level);
            serializer.startTag(null, "package");
            serializer.attribute(null, "id",
                    HexUtil.toHex2((byte) packageBlock.getId()));
            serializer.attribute(null, "name",
                    packageBlock.getName());
            serializer.endTag(null, "package");
        }
        level = level - 2;
        writeIndent(serializer, level);
        serializer.endTag(null, TAG_RES_PACKAGES);
        serializer.flush();
    }
    @Override
    public void writeEntries(String name, List<Entry> entryList) throws IOException {
        if(entryList == null || entryList.size() == 0){
            return;
        }
        Entry first = entryList.get(0);
        int level = INDENT;
        KXmlSerializer serializer = getSerializer();
        writeIndent(serializer, level);
        serializer.startTag(null, name);
        serializer.attribute(null, "count", Integer.toString(entryList.size()));
        serializer.attribute(null, "id", HexUtil.toHex8(first.getResourceId()));
        serializer.attribute(null, "type", first.getTypeName());
        serializer.attribute(null, "name", first.getName());
        level = level + 2;
        for(Entry entry : entryList){
            writeEntry(entry);
        }
        level = level - 2;
        writeIndent(serializer, level);
        serializer.endTag(null, name);
        serializer.flush();
    }

    @Override
    public void writeArray(String name, Object[] values) throws IOException {
        if(values == null){
            return;
        }
        int level = INDENT;
        KXmlSerializer serializer = getSerializer();
        writeIndent(serializer, level);
        serializer.startTag(null, name);
        serializer.attribute(null, "count", Integer.toString(values.length));
        level = level + 2;
        for(Object value : values){
            String text = toString(value);
            if(text == null){
                text =  "";
            }
            writeIndent(serializer, level);
            serializer.startTag(null, "item");
            serializer.text(text);
            serializer.endTag(null, "item");
        }
        level = level - 2;
        writeIndent(serializer, level);
        serializer.endTag(null, name);
        serializer.flush();
    }

    @Override
    public void writeNameValue(String name, Object value) throws IOException {
        String text = toString(value);
        if(text == null){
            return;
        }
        KXmlSerializer serializer = getSerializer();
        writeIndent(serializer, INDENT);
        serializer.startTag(null, name);
        serializer.text(text);
        serializer.endTag(null, name);
        serializer.flush();
    }

    @Override
    public void flush() throws IOException {
        KXmlSerializer serializer = this.mSerializer;
        if(serializer != null){
            writeIndent(serializer, 0);
            serializer.endTag(null, TAG_INFO);
            serializer.endDocument();
            writeIndent(serializer, 0);
            serializer.flush();
        }
    }
    private void writeIndent(KXmlSerializer serializer, int level) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append('\n');
        for(int i = 0; i < level; i++){
            builder.append(' ');
        }
        serializer.text(builder.toString());
    }
    private KXmlSerializer getSerializer() throws IOException {
        KXmlSerializer serializer = this.mSerializer;
        if(serializer != null){
            return serializer;
        }
        serializer = new KXmlSerializer();
        serializer.setOutput(getWriter());
        serializer.startDocument("utf-8", null);
        writeIndent(serializer, 0);
        serializer.startTag(null, TAG_INFO);
        mSerializer = serializer;
        return serializer;
    }

    private static final int INDENT = 3;
    private static final String TAG_INFO = "info";
}
