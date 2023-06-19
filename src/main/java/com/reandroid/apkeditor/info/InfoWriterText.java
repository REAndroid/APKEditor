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

import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.util.HexUtil;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.arsc.value.ResValue;
import com.reandroid.arsc.value.ResValueMap;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class InfoWriterText extends InfoWriter{
    public InfoWriterText(Writer writer) {
        super(writer);
    }

    @Override
    public void writeResources(PackageBlock packageBlock, List<String> typeFilters, boolean writeEntries) throws IOException {
        Writer writer = getWriter();
        writer.write("Package name=");
        writer.write(packageBlock.getName());
        writer.write(" id=");
        writer.write(HexUtil.toHex2((byte) packageBlock.getId()));
        writer.write("\n");
        packageBlock.sortTypes();

        for(SpecTypePair specTypePair : packageBlock.listSpecTypePairs()){
            if(!contains(specTypePair, typeFilters)){
                continue;
            }
            writeResources(specTypePair, writeEntries);
        }
    }
    public void writeResources(SpecTypePair specTypePair, boolean writeEntries) throws IOException {
        Writer writer = getWriter();
        writer.write("  type ");
        writer.write(specTypePair.getTypeName());
        writer.write(" id=");
        writer.write(Integer.toString(specTypePair.getId()));
        writer.write(" entryCount=");
        writer.write(Integer.toString(specTypePair.getHighestEntryCount()));
        writer.write("\n");

        Iterator<ResourceEntry> iterator = specTypePair.getResources();
        while (iterator.hasNext()){

            ResourceEntry resourceEntry = iterator.next();
            writeResources(resourceEntry, writeEntries);
        }
    }
    @Override
    public void writeResources(ResourceEntry resourceEntry, boolean writeEntries) throws IOException {
        if(resourceEntry.isEmpty()){
            return;
        }
        Writer writer = getWriter();
        writer.write("    ");
        writer.write(NAME_RESOURCE);
        writer.write(" ");
        writer.write(HexUtil.toHex8(resourceEntry.getResourceId()));
        writer.write(" ");
        writer.write(resourceEntry.getType());
        writer.write("/");
        writer.write(resourceEntry.getName());
        writer.write("\n");
        if(writeEntries){
            writeEntries(resourceEntry);
        }
        writer.flush();
    }

    public void writeEntries(ResourceEntry entryList) throws IOException {
        for(Entry entry : entryList){
            writeEntry(entry);
        }
    }
    public void writeEntry(Entry entry) throws IOException {
        Writer writer = getWriter();
        writer.write("      (");
        writer.write(entry.getResConfig().getQualifiers());
        writer.write(") ");
        //write file
        if(entry.isComplex()){
            writeBagEntry(entry);
        }else {
            writeResEntry(entry);
        }
        writer.flush();
    }
    private void writeResEntry(Entry entry) throws IOException {
        ResValue resValue = entry.getResValue();
        if(resValue == null){
            return;
        }
        Writer writer = getWriter();
        writer.write(getValueAsString(resValue));
        writer.write("\n");
    }
    private void writeBagEntry(Entry entry) throws IOException {
        Writer writer = getWriter();
        ResValueMapArray mapArray = entry.getResValueMapArray();
        writer.write(" size=");
        writer.write(Integer.toString(mapArray.childesCount()));
        writer.write(" parent=");
        writer.write(HexUtil.toHex8(((ResTableMapEntry)entry.getTableEntry()).getParentId()));
        writer.write("\n");
        for(ResValueMap resValueMap : mapArray.getChildes()){
            writeValueMap(resValueMap);
        }
    }
    private void writeValueMap(ResValueMap resValueMap) throws IOException {
        Writer writer = getWriter();
        writer.write("        ");
        String name = resValueMap.decodeName();
        if(name != null){
            writer.write(name);
            writer.write("(");
        }
        writer.write(HexUtil.toHex8(resValueMap.getNameResourceID()));
        if(name != null){
            writer.write(")");
        }
        writer.write("=");
        writer.write(getValueAsString(resValueMap));
        writer.write("\n");
    }
    @Override
    public void writePackageNames(Collection<PackageBlock> packageBlocks) throws IOException {
        if(packageBlocks == null || packageBlocks.size() == 0){
            return;
        }
        Writer writer = getWriter();
        writer.write(TAG_RES_PACKAGES);
        writer.write("  [ count ");
        writer.write(Integer.toString(packageBlocks.size()));
        writer.write("]");
        writer.write("\n");
        for(PackageBlock packageBlock : packageBlocks){
            writer.write(ARRAY_TAB);
            writer.write(HexUtil.toHex2((byte) packageBlock.getId()));
            writer.write("  \"");
            writer.write(packageBlock.getName());
            writer.write("\"");
            writer.write("\n");
        }
    }
    @Override
    public void writeEntries(String name, List<Entry> entryList) throws IOException {
        if(entryList == null || entryList.size() == 0){
            return;
        }
        Entry first = entryList.get(0);
        Writer writer = getWriter();
        writer.write(name);
        writer.write("  [ configs = ");
        writer.write(Integer.toString(entryList.size()));
        writer.write(", id=");
        writer.write(HexUtil.toHex8(first.getResourceId()));
        writer.write(", type=");
        writer.write(first.getTypeName());
        writer.write(", name=");
        writer.write(first.getName());
        writer.write(" ]");
        writer.write("\n");
        int index = 0;
        for(Entry entry : entryList){
            index++;
            String config = entry.getResConfig().getQualifiers();
            if(config.length() == 0){
                config = "default";
            }
            writer.write(ARRAY_TAB);
            writer.write(config);
            writer.write("  \"");
            String text = getValueAsString(entry);
            writer.write(text);
            writer.write("\"\n");
            if((index % 3) == 0){
                writer.flush();
            }
        }
    }

    @Override
    public void writeArray(String name, Object[] values) throws IOException {
        if(values == null){
            return;
        }
        Writer writer = getWriter();
        writer.write(name);
        writer.write("  [ count ");
        writer.write(Integer.toString(values.length));
        writer.write("]");
        writer.write("\n");
        String format = "%0" + getDecimalPlaces(values.length) + "d) ";
        int index = 0;
        for(Object value : values){
            index ++;
            writer.write(ARRAY_TAB);
            writer.write(String.format(format, index));
            String text = toString(value);
            if(text == null){
                text = "null";
            }
            writer.write(text);
            writer.write("\n");
            if((index % 3) == 0){
                writer.flush();
            }
        }
    }
    @Override
    public void writeNameValue(String name, Object value) throws IOException {
        String text = toString(value);
        if(text == null){
            return;
        }
        Writer writer = getWriter();
        writer.write(name);
        writer.write("=\"");
        writer.write(text);
        writer.write("\"");
        writer.write("\n");
        writer.flush();
    }

    @Override
    public void flush() throws IOException {
        Writer writer = getWriter();
        writer.flush();
    }

    private static int getDecimalPlaces(int max){
        int i = 0;
        while (max != 0){
            i++;
            max = max / 10;
        }
        return i;
    }

    private static final String ARRAY_TAB = "     ";
}
