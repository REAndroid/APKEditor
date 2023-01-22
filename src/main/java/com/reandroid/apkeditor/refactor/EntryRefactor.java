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

import com.reandroid.apk.ResourceIds;
import com.reandroid.arsc.chunk.TableBlock;

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
         ResourceIds.Table.Package.Type.Entry result;
         result=refactorByValue(entry);
         if(result==null){
             result=refactorGenerateName(entry);
         }
         return result;
     }
     /*
     * TODO: implement refactoring from TableBlock entry value
     *   e.g-1: <string name="***">No internet connection</string>
     *      ==> <string name="no_internet_connection">No internet connection</string>
     *   e.g-2: <color name="***">#FF0000</color>
     *      ==> <color name="red">#FF0000</color>
     */
     private ResourceIds.Table.Package.Type.Entry refactorByValue(ResourceIds.Table.Package.Type.Entry entry){
         return null;
     }
     private ResourceIds.Table.Package.Type.Entry refactorGenerateName(ResourceIds.Table.Package.Type.Entry entry){
         int resourceId=entry.getResourceId();

         String name=RefactorUtil.generateUniqueName(
                 entry.getTypeName(),
                 entry.getResourceId());

         return new ResourceIds.Table.Package.Type.Entry(
                 resourceId,
                 entry.getTypeName(), name);
     }
     public boolean isObfuscated(){
         Set<String> entryNames=getEntryNames();
         if(entryNames.size()!=mType.entryMap.size()){
             //duplicates found
             return true;
         }
         return RefactorUtil.isObfuscated(entryNames);
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
}
