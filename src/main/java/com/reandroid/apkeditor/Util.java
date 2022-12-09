package com.reandroid.apkeditor;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static boolean isHelp(String[] args){
        if(isEmpty(args)){
            return true;
        }
        String command=args[0];
        command=command.toLowerCase().trim();
        return command.equals("-h")
                ||command.equals("-help")
                ||command.equals("h")
                ||command.equals("help");
    }
    public static String[] trimNull(String[] args){
        if(isEmpty(args)){
            return null;
        }
        List<String> results=new ArrayList<>();
        for(String str:args){
            if(!isEmpty(str)){
                results.add(str);
            }
        }
        return results.toArray(new String[0]);
    }
    public static boolean isEmpty(String[] args){
        if(args==null||args.length==0){
            return true;
        }
        for (String s:args){
            if(!isEmpty(s)){
                return false;
            }
        }
        return true;
    }
    public static boolean isEmpty(String str){
        if(str==null){
            return true;
        }
        str=str.trim();
        return str.length()==0;
    }
}
