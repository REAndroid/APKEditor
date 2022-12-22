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

import com.reandroid.lib.apk.ResourceIds;
import com.reandroid.lib.arsc.chunk.TableBlock;

import java.util.*;


public class EntryRefactor {
    private final TableBlock mTableBlock;
    private final ResourceIds.Table.Package.Type mType;
    public EntryRefactor(TableBlock tableBlock, ResourceIds.Table.Package.Type type){
        this.mTableBlock=tableBlock;
        this.mType=type;
    }
    public ResourceIds.Table.Package.Type refactorAll(){
        ResourceIds.Table.Package.Type result=new ResourceIds.Table.Package.Type(mType.getId());
        for(ResourceIds.Table.Package.Type.Entry entry:mType.listEntries()){
            result.add(refactor(entry));
        }
        return result;
    }
    private ResourceIds.Table.Package.Type.Entry refactor(ResourceIds.Table.Package.Type.Entry entry){
        int resourceId=entry.getResourceId();
        String name=generateName(entry);
        return new ResourceIds.Table.Package.Type.Entry(
                resourceId,
                entry.getTypeName(), name);
    }
    private String generateName(ResourceIds.Table.Package.Type.Entry entry){
        return entry.getTypeName()+"_"+entry.getHexId();
    }
    public boolean isObfuscated(){
        Set<String> entryNames=getEntryNames();
        if(entryNames.size()!=mType.entryMap.size()){
            //duplicates found
            return true;
        }
        if(isAllEqualLength(entryNames)){
            return true;
        }
        return false;
    }
    private boolean isAllEqualLength(Collection<String> entryNames){
        int length=0;
        for(String name:entryNames){
            int len=name.length();
            if(length==0){
                length=len;
                continue;
            }
            if(len!=length){
                return false;
            }
            length=len;
        }
        return length!=0;
    }
    private Set<String> getEntryNames(){
        Set<String> results=new HashSet<>();
        for(ResourceIds.Table.Package.Type.Entry entry:mType.listEntries()){
            results.add(entry.name);
        }
        return results;
    }
    private boolean contains(String entryName){
        for(ResourceIds.Table.Package.Type.Entry entry:mType.listEntries()){
            if(entryName.equals(entry.name)){
                return true;
            }
        }
        return false;
    }
    private boolean isUnique(String entryName){
        boolean firstFound=false;
        for(ResourceIds.Table.Package.Type.Entry entry:mType.listEntries()){
            if(entryName.equals(entry.name)){
                if(!firstFound){
                    firstFound=true;
                    continue;
                }
                return false;
            }
        }
        return true;
    }
    private int countOccurrences(String entryName){
        int result=0;
        for(ResourceIds.Table.Package.Type.Entry entry:mType.listEntries()){
            if(entryName.equals(entry.name)){
                result++;
            }
        }
        return result;
    }
}
