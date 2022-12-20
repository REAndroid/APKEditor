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
package com.reandroid.apkeditor.utils;


public class StringHelper {
    public static void printTwoColumns(StringBuilder builder, String tab, int totalWidth, String[][] table){
        int leftWidth=0;
        for(String[] col:table){
            int len=col[0].length();
            if(len>leftWidth){
                leftWidth=len;
            }
        }
        int bnColumns=3;
        leftWidth=leftWidth+bnColumns;
        int maxRight=totalWidth-leftWidth;
        for(int i=0;i<table.length;i++){
            String[] col=table[i];
            if(i!=0){
                builder.append("\n");
            }
            printRow(builder, tab, leftWidth, maxRight, col[0], col[1]);
        }
    }
    private static void printRow(StringBuilder builder, String tab, int leftWidth, int maxRight, String left, String right){
        builder.append(tab);
        builder.append(left);
        fillSpace(builder, leftWidth-left.length());
        char[] rightChars=right.toCharArray();
        int rightWidth=0;
        for(int i=0;i<rightChars.length;i++){
            char ch=rightChars[i];
            if(i==0){
                builder.append(ch);
                rightWidth++;
                continue;
            }
            if(ch=='\n' || (rightWidth > 0 && rightWidth%maxRight==0)){
                builder.append('\n');
                builder.append(tab);
                fillSpace(builder, leftWidth);
                rightWidth=0;
            }
            if(ch!='\n'){
                boolean skipFirstSpace=(rightWidth==0 && ch==' ');
                if(!skipFirstSpace){
                    builder.append(ch);
                    rightWidth++;
                }
            }
        }
    }
    private static void fillSpace(StringBuilder builder, int count){
        for(int i=0;i<count;i++){
            builder.append(' ');
        }
    }
}
