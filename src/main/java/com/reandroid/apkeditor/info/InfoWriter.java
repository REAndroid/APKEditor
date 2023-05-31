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

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.group.EntryGroup;
import com.reandroid.arsc.util.HexUtil;
import com.reandroid.arsc.value.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public abstract class InfoWriter implements Closeable {
    private final Writer writer;
    public InfoWriter(Writer writer){
        this.writer = writer;
    }

    public void writeResources(PackageBlock packageBlock, List<String> typeFilters, boolean writeEntries) throws IOException {
        List<EntryGroup> entryGroupList = toSortedEntryGroups(packageBlock.listEntryGroup());
        for(EntryGroup entryGroup : entryGroupList){
            writeResources(entryGroup, writeEntries);
        }
    }
    public abstract void writeResources(EntryGroup entryGroup, boolean writeEntries) throws IOException;
    public abstract void writePackageNames(Collection<PackageBlock> packageBlocks) throws IOException;
    public abstract void writeEntries(String name, List<Entry> entryList) throws IOException;
    public abstract void writeArray(String name, Object[] values) throws IOException;
    public abstract void writeNameValue(String name, Object value) throws IOException;
    public abstract void flush() throws IOException;
    boolean contains(SpecTypePair specTypePair, List<String> filterList){
        if(filterList.size() == 0){
            return true;
        }
        return filterList.contains(specTypePair.getTypeName());
    }
    public Writer getWriter() {
        return writer;
    }
    @Override
    public void close() throws IOException{
        this.writer.close();
    }

    static String toString(Object obj){
        if(obj != null){
            return obj.toString();
        }
        return null;
    }
    static String getValueAsString(Entry entry){
        ResValue resValue = entry.getResValue();
        if(resValue == null){
            return "";
        }
        return getValueAsString(resValue);
    }
    static String getValueAsString(Value value){
        ValueType valueType = value.getValueType();
        if(valueType == ValueType.STRING){
            return value.getValueAsString();
        }
        String decoded = ValueCoder.decode(valueType, value.getData());
        if(decoded != null){
            return decoded;
        }
        if(valueType == ValueType.ATTRIBUTE){
            return HexUtil.toHex8("?0x", value.getData());
        }
        if(valueType == ValueType.REFERENCE){
            return HexUtil.toHex8("@0x", value.getData());
        }
        return HexUtil.toHex8("0x", value.getData());
    }

    static List<EntryGroup> toSortedEntryGroups(Collection<EntryGroup> entryGroups){
        List<EntryGroup> results = new ArrayList<>(entryGroups);
        sortEntryGroups(results);
        return results;
    }
    static void sortEntryGroups(List<EntryGroup> entryGroups){
        Comparator<EntryGroup> cmp = new Comparator<EntryGroup>() {
            @Override
            public int compare(EntryGroup entryGroup1, EntryGroup entryGroup2) {
                long l1 = 0x00000000ffffffffL & entryGroup1.getResourceId();
                long l2 = 0x00000000ffffffffL & entryGroup2.getResourceId();
                return Long.compare(l1, l2);
            }
        };
        entryGroups.sort(cmp);
    }

    static List<Entry> sortEntries(Collection<Entry> entryCollection) {
        ArrayList<Entry> results;
        if(entryCollection instanceof ArrayList){
            results = (ArrayList<Entry>) entryCollection;
        }else {
            results = new ArrayList<>(entryCollection);
        }
        Comparator<Entry> cmp = new Comparator<Entry>() {
            @Override
            public int compare(Entry entry1, Entry entry2) {
                return entry1.getResConfig().compareTo(entry2.getResConfig());
            }
        };
        results.sort(cmp);
        return results;
    }

    static final String TAG_RES_PACKAGES = "resource-packages";
    static final String TAG_PUBLIC = "public";
    static final String TAG_RESOURCES = "resources";
    static final String NAME_RESOURCE = "resource";
    static final String TAG_CONFIG = "config";
    static final String TAG_VALUE = "value";
    static final String TAG_BAG = "bag";
    static final String TAG_ITEM = "item";
    static final String NAME_QUALIFIERS = "qualifiers";

}
