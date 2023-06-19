package com.reandroid.apkeditor.refactor;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.value.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringValueNameGenerator {
    private final TableBlock tableBlock;
    private final Set<String> mGeneratedNames;
    private final Set<Integer> mSkipIds;
    public StringValueNameGenerator(TableBlock tableBlock){
        this.tableBlock = tableBlock;
        this.mGeneratedNames=new HashSet<>();
        this.mSkipIds=new HashSet<>();
    }
    public void refactor(){
        Map<Integer, ResourceEntry> resourceEntryMap = mapResourceEntries();
        Map<Integer, String> nameMap = generate();
        for(Map.Entry<Integer, String> entry:nameMap.entrySet()){
            ResourceEntry resourceEntry = resourceEntryMap.get(entry.getKey());
            String name = entry.getValue();
            resourceEntry.setName(name);
        }
    }
    private boolean isGenerated(ResourceEntry resourceEntry){
       String generated = RefactorUtil.generateUniqueName(
                resourceEntry.getType(),
                resourceEntry.getResourceId());
       return generated.equals(resourceEntry.getName());
    }
    private Map<Integer, String> generate(){
        mGeneratedNames.clear();
        mSkipIds.clear();
        Map<Integer, String> results = new HashMap<>();
        Set<Integer> skipIds = this.mSkipIds;
        List<ResourceEntry> resourceEntryList = listResources();
        for(ResourceEntry resourceEntry:resourceEntryList){
            if(!isGenerated(resourceEntry)){
                skipIds.add(resourceEntry.getResourceId());
            }
        }
        for(ResourceEntry resourceEntry:resourceEntryList){
            int resourceId = resourceEntry.getResourceId();
            if(results.containsKey(resourceId) || skipIds.contains(resourceId)){
                continue;
            }
            Entry entry = getEnglishOrDefault(resourceEntry);
            if(entry==null){
                continue;
            }
            ResValue resValue = ((ResTableEntry)entry.getTableEntry()).getValue();
            String text = resValue.getValueAsString();
            String name = generate(resourceId, text);
            if(name!=null){
                results.put(resourceId, name);
                mGeneratedNames.add(name);
            }
        }
        return results;
    }
    private Entry getEnglishOrDefault(ResourceEntry resourceEntry){
        Entry def = null;
        for(Entry entry:resourceEntry){
            if(entry==null){
                continue;
            }
            TableEntry<?, ?> tableEntry = entry.getTableEntry();
            if(!(tableEntry instanceof ResTableEntry)){
                continue;
            }
            ResValue resValue = ((ResTableEntry)tableEntry).getValue();
            if(resValue.getValueType() != ValueType.STRING){
                continue;
            }
            if(entry.getResConfig().isDefault()){
                def=entry;
            }
            String lang = entry.getResConfig().getLanguage();
            if(lang==null){
                continue;
            }
            if(lang.equals("en")){
                return entry;
            }
        }
        return def;
    }
    private List<ResourceEntry> listResources(){
        return new ArrayList<>(mapResourceEntries().values());
    }
    private Map<Integer, ResourceEntry> mapResourceEntries(){
        Map<Integer, ResourceEntry> results = new HashMap<>();
        for(PackageBlock packageBlock:tableBlock.listPackages()){
            SpecTypePair specTypePair = packageBlock.getSpecTypePair(TYPE);
            if(specTypePair == null){
                continue;
            }
            Iterator<ResourceEntry> itr = specTypePair.getResources();
            while (itr.hasNext()){
                ResourceEntry resourceEntry = itr.next();
                if(resourceEntry.isEmpty()){
                    continue;
                }
                results.put(resourceEntry.getResourceId(), resourceEntry);
            }
        }
        return results;
    }
    private String generate(int resourceId, String text){
        if(text==null){
            return null;
        }
        String name = generateEnglish(text);
        if(name==null){
            return null;
        }
        if(!mGeneratedNames.contains(name)){
            return name;
        }
        resourceId=0xffff & resourceId;
        name = name + "_" +String.format("%04x", resourceId);
        if(!mGeneratedNames.contains(name)){
            return name;
        }
        int i=0;
        while (i<10){
            String numberedName=name+"_"+i;
            if(!mGeneratedNames.contains(numberedName)){
                return numberedName;
            }
            i++;
        }
        return null;
    }
    private String generateEnglish(String text){
        String name=getPathDataName(text);
        if(name==null){
            name=getUrlName(text);
        }
        if(name==null){
            name=getDefaultName(text);
        }
        return name;
    }


    private String getPathDataName(String str){
        Matcher matcher=PATTERN_PATH.matcher(str);
        if(!matcher.find()){
            return null;
        }
        return PATH_DATA_NAME;
    }
    private String getUrlName(String str){
        Matcher matcher=PATTERN_URL.matcher(str);
        if(!matcher.find()){
            return null;
        }
        StringBuilder builder=new StringBuilder();
        builder.append("url_");
        String dom=matcher.group(3);
        dom=dom.replace('.', '/');
        String path=matcher.group(4);
        if(path!=null){
            path=path.replace('?', '/');
            path=path.replace('=', '/');
            dom=dom+path;
        }
        int len=0;
        String[] allPaths=dom.split("/");
        int max=allPaths.length;
        boolean appendOnce=false;
        for(int i=0;i<max;i++){
            String sub=allPaths[i];
            if(!isAToZName(sub)){
                continue;
            }
            int subLen=sub.length();
            if(len+subLen>MAX_NAME_LEN){
                if(!appendOnce){
                    continue;
                }
                break;
            }
            if(appendOnce){
                builder.append('_');
            }
            builder.append(sub);
            appendOnce=true;
            len=len+subLen;
        }
        if(!appendOnce){
            return null;
        }
        return builder.toString();
    }
    private String getDefaultName(String str){
        str=str.replaceAll("'", "");
        str=str.replaceAll("%([0-9]+\\$)?s", " STR ");
        str=str.replaceAll("%([0-9]+\\$)?d", " NUM ");
        str=str.replaceAll("&amp;", " and ");
        str=str.replaceAll("&(lt|gt);", " ");
        str=replaceNonAZ(str);
        String[] allWords=str.split("[\\s]+");
        int len=0;
        boolean appendOnce=false;
        StringBuilder builder=new StringBuilder();
        int max=allWords.length;
        for(int i=0;i<max;i++){
            String sub=allWords[i];
            if(!isAToZName(sub)){
                if(i+1<max){
                    continue;
                }
                if(!isNumber(sub) || !appendOnce){
                    continue;
                }
            }
            int subLen=sub.length();
            if(len+subLen>MAX_NAME_LEN){
                if(!appendOnce){
                    continue;
                }
                break;
            }
            if(appendOnce){
                builder.append('_');
                len++;
            }else {
                sub=sub.toLowerCase();
            }
            builder.append(sub);
            appendOnce=true;
            len=len+subLen;
        }
        if(!appendOnce || len<3){
            return null;
        }
        return builder.toString();
    }
    private String replaceNonAZ(String fullStr){
        String str;
        String num=null;
        Matcher matcher=PATTERN_END_NUMBER.matcher(fullStr);
        if(matcher.find()){
            str=matcher.group(1);
            num=matcher.group(2);
        }else {
            str=fullStr;
        }
        str=str.replaceAll("[^A-Za-z]+", " ");
        StringBuilder builder=new StringBuilder();
        builder.append(str);
        if(num!=null){
            builder.append(' ');
            builder.append(num);
        }
        builder.append(' ');
        return builder.toString();
    }

    private static boolean isAToZName(String str){
        Matcher matcher = PATTERN_EN.matcher(str);
        return matcher.find();
    }
    private static boolean isNumber(String str){
        Matcher matcher=PATTERN_NUMBER.matcher(str);
        return matcher.find();
    }

    private static final Pattern PATTERN_EN=Pattern.compile("^[A-Za-z]{2,15}(_[A-Za-z]{1,15})*[0-9]*$");
    private static final Pattern PATTERN_PATH=Pattern.compile("^M[0-9.]+[\\s,]+[0-9\\-ACLHMSVZaclhmsvz,.\\s]+$");
    private static final Pattern PATTERN_URL=Pattern.compile("^(https?://)(www\\.)?([^/]+)(/.*)?$");
    private static final Pattern PATTERN_END_NUMBER=Pattern.compile("^([^0-9]+)([0-9]+)\\s*([^a-zA-Z0-9]{0,2})\\s*$");
    private static final Pattern PATTERN_NUMBER= Pattern.compile("^[0-9]+$");


    private static final int MAX_NAME_LEN = 40;
    private static final String PATH_DATA_NAME = "vector_path_data";
    private static final String TYPE = "string";
}
