package com.reandroid.commons.utils;

import java.util.Calendar;

public class StringUtil {

    public static boolean isEmpty(CharSequence cs){
        if(cs==null){
            return true;
        }
        return isEmpty(cs.toString());
    }
    public static boolean isEmpty(String txt){
        if(txt==null){
            return true;
        }
        txt=txt.trim();
        return txt.length()==0;
    }
    public static String splitThousandComma(long val){
        String str=String.valueOf(val);
        char[] allChars=str.toCharArray();
        int len=allChars.length;
        StringBuilder builder=new StringBuilder();
        for(int i=0; i<len;i++){
            int rem=len-i;
            if(i>0){
                if(rem%3==0){
                    builder.append(',');
                }
            }
            builder.append(allChars[i]);
        }
        return builder.toString();
    }
    public static String toFileSize(long val){
        String[] suffix=new String[]{
                "PB",
                "TB",
                "GB",
                "MB",
                "KB",
                "bytes"
        };
        int index=0;
        long rem=val;
        long prev=0;
        while (rem>1024){
            prev=rem;
            rem=rem/1024;
            prev=prev-(rem*1024);
            index++;
        }
        if(index>= suffix.length){
            return String.valueOf(val);
        }
        int i=suffix.length-index-1;
        return rem+"."+String.format("%03d",prev)+" "+suffix[i];
    }
    public static String toReadableDate(long timeMsOrSec){
        if(timeMsOrSec< 9999999999L){
            timeMsOrSec=timeMsOrSec*1000;
        }
        Calendar calendar= Calendar.getInstance();
        calendar.setTimeInMillis(timeMsOrSec);
        StringBuilder builder=new StringBuilder();
        builder.append(String.format("%04d", calendar.get(Calendar.YEAR)));
        builder.append('-');
        int m=calendar.get(Calendar.MONTH)+1;
        builder.append(String.format("%02d", m));
        builder.append('-');
        builder.append(String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)));
        builder.append(' ');
        builder.append(String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)));
        builder.append(':');
        builder.append(String.format("%02d", calendar.get(Calendar.MINUTE)));
        builder.append(':');
        builder.append(String.format("%02d", calendar.get(Calendar.SECOND)));
        return builder.toString();
    }
}
