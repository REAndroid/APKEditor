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

import java.util.regex.Pattern;

public class RefactorUtil {
    public static boolean isGoodName(String name){
        if(name==null){
            return false;
        }
        return PATTERN_GOOD_NAME.matcher(name).matches();
    }
    public static final String RES_DIR="res";
    private static final Pattern PATTERN_GOOD_NAME =Pattern.compile("^[A-Za-z]{2,15}(_[A-Za-z]{1,15})*[0-9]*$");
}
