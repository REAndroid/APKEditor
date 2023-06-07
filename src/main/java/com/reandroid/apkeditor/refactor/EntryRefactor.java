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

import com.reandroid.identifiers.ResourceIdentifier;
import com.reandroid.identifiers.TypeIdentifier;

public class EntryRefactor {
    private final TypeIdentifier mTypeIdentifier;
    public EntryRefactor(TypeIdentifier typeIdentifier){
        this.mTypeIdentifier = typeIdentifier;
    }
    public int refactorAll(){
        int result = 0;
        for(ResourceIdentifier ri : mTypeIdentifier.getItems()){
            if(!ri.isGeneratedName()){
                continue;
            }
            boolean renamed = refactor(ri);
            if(renamed){
                result ++;
            }
        }
        return result;
    }
    private boolean refactor(ResourceIdentifier entry){
        return refactorByValue(entry);
    }
    /*
     * TODO: implement refactoring from TableBlock entry value
     *   e.g-1: <string name="***">No internet connection</string>
     *      ==> <string name="no_internet_connection">No internet connection</string>
     *   e.g-2: <color name="***">#FF0000</color>
     *      ==> <color name="red">#FF0000</color>
     */
    private boolean refactorByValue(ResourceIdentifier resourceIdentifier){
        return false;
    }
}
