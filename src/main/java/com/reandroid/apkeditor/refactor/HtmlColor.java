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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HtmlColor {
    private static final String RES_NAME_PREFIX="col_";
    private static Map<Integer, HtmlColor> knownColors;
    private final String name;
    private final int alpha;
    private final int red;
    private final int green;
    private final int blue;
    private String mSplitName;
    HtmlColor(String hex){
        this(null, hex);
    }
    HtmlColor(String name,String hex){
        this(name, parseHex(hex));
    }
    HtmlColor(String name, int val){
        if(name!=null){
            if(name.trim().length()==0){
                name=null;
            }
        }
        this.name = name;
        alpha=(val >> 24) & 0xff;
        red=(val >> 16) & 0x00ff;
        green=(val >> 8) & 0x0000ff;
        blue=val & 0x000000ff;
    }
    String getName(){
        return name;
    }
    int getARGBValue(){
        return (alpha << 24) +(red << 16) + (green << 8) + blue;
    }
    int getRGBValue(){
        return (red << 16) + (green << 8) + blue;
    }
    String getRGBHex(){
        return toRgbHex(getRGBValue());
    }
    String getARGBHex(){
        return toARgbHex(getARGBValue());
    }

    String toResourceName(){
        return getSplitName();
    }
    private String getSplitName(){
        if(mSplitName==null){
            mSplitName=splitByUpperCase(RES_NAME_PREFIX, getName());
        }
        return mSplitName;
    }
    double getDistance(HtmlColor htmlColor){
        double d1=red-htmlColor.red;
        d1=d1*d1;
        double d2=green-htmlColor.green;;
        d2=d2*d2;
        double d3=blue-htmlColor.blue;
        d3=d3*d3;
        return Math.sqrt(d1+d2+d3);
    }

    boolean isEqualRGB(HtmlColor htmlColor, double tolerance){
        double dis=getDistance(htmlColor);
        return dis<=tolerance;
    }
    int getAlpha(){
        return alpha;
    }
    private int getRed(){
        return red;
    }
    private int getGreen(){
        return green;
    }
    private int getBlue(){
        return blue;
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getRGBHex());
        String n=getName();
        if(n!=null){
            builder.append(": ");
            builder.append(getName());
            builder.append(", ");
            builder.append("@color/");
            builder.append(getSplitName());
        }
        return builder.toString();
    }

    private static String splitByUpperCase(String prefix, String colorName){
        if(colorName==null){
            return null;
        }
        colorName=colorName.trim();
        char[] allChars=colorName.toCharArray();
        int max=allChars.length;
        boolean appendSeparator=false;
        StringBuilder builder=new StringBuilder();
        if(prefix!=null){
            builder.append(prefix);
        }
        for(int i=0;i<max;i++){
            char ch=allChars[i];
            if(ch==' '){
                if(!appendSeparator){
                    builder.append("_");
                    appendSeparator=true;
                }
                continue;
            }
            if(i>0){
                if(Character.isUpperCase(ch) && !appendSeparator){
                    builder.append("_");
                    ch=Character.toLowerCase(ch);
                    builder.append(ch);
                    appendSeparator=true;
                    continue;
                }
            }
            ch=Character.toLowerCase(ch);
            builder.append(ch);
            appendSeparator=false;
        }
        return builder.toString();
    }
    private static String toRgbHex(int i){
        return String.format("#%06x", i);
    }
    private static String toARgbHex(int i){
        return String.format("#%08x", i);
    }
    private static int parseHex(String str){
        boolean isNegative=false;
        if(str.startsWith("#")){
            str=str.substring(1);
        }
        if(str.startsWith("-")){
            str=str.substring(1);
            isNegative=true;
        }
        if(str.startsWith("0x")){
            str=str.substring(2);
        }
        str=str.toUpperCase();
        long l=Long.parseLong(str, 16);
        if(isNegative){
            l=-l;
        }
        return (int)l;
    }
    static HtmlColor getBestMatch(HtmlColor htmlColor, double tolerance){
        Map<Integer, HtmlColor> knownMap=getKnownColors();
        HtmlColor result=knownMap.get(htmlColor.getRGBValue());
        if(result!=null){
            return result;
        }
        double foundDis=0;
        for(HtmlColor color:knownMap.values()){
            double dis=color.getDistance(htmlColor);
            if(dis>tolerance){
                continue;
            }
            if(result==null){
                foundDis=dis;
                result=color;
                continue;
            }
            if(dis<foundDis){
                foundDis=dis;
                result=color;
                if(dis==0){
                    break;
                }
            }
        }
        return result;
    }
    private static Map<Integer, HtmlColor> getKnownColors(){
        if(knownColors==null){
            knownColors=loadColorNames();
        }
        return knownColors;
    }

    private static Map<Integer, HtmlColor> loadColorNames(){
        Map<Integer, HtmlColor> results=new HashMap<>();
        InputStream in = getColorNamesResource();
        if(in==null){
            return results;
        }
        InputStreamReader inReader=new InputStreamReader(in);
        BufferedReader reader=new BufferedReader(inReader);
        String line;
        try {
            while (((line=reader.readLine()) != null)) {
                HtmlColor htmlColor=parseLine(line);
                if(htmlColor==null){
                    continue;
                }
                Integer val=htmlColor.getRGBValue();
                results.putIfAbsent(val, htmlColor);
            }
        } catch (IOException e) {
        }
        try{
            reader.close();
        }catch (Exception ex){

        }
        return results;
    }
    private static InputStream getColorNamesResource(){
        try {
            return HtmlColor.class.getResourceAsStream("/color_names.txt");
        }catch (Exception ex){
            return null;
        }
    }
    private static HtmlColor parseLine(String line){
        Matcher matcher=PATTERN_LINE.matcher(line);
        if(!matcher.find()){
            return null;
        }
        String hex=matcher.group(1);
        String name=matcher.group(2);
        return new HtmlColor(name, hex);
    }
    //#FFFFD700,Gold
    private static final Pattern PATTERN_LINE=Pattern.compile("^\\s*(#[A-fa-f0-9]{6,8})\\s*,\\s*([^\\s]+.*[^\\s]+)\\s*$");
}
