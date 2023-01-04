package com.reandroid.commons.utils;


import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtil implements ShutdownHook.ShutdownListener {
    private static FileUtil INS;
    private static final String EXT_SMALI_FILE=".smali";
    private static String tmp_dir_root_name;
    private static long current_pid;
    private static File mGlobalTmpDir;

    private FileUtil(){
    }
    static {
        INS=new FileUtil();
        ShutdownHook.INS.addListener(INS);
    }
    @Override
    public void onShutdown() {
        clearTmp();
    }
    private static void clearTmp(){
        File dir=getTmpDir();
        deleteDir(dir);
    }
    private static void deleteDir(File dir){
        if(!dir.exists()){
            return;
        }
        if(dir.isFile()){
            dir.delete();
            return;
        }
        if(!dir.isDirectory()){
            return;
        }
        File[] files=dir.listFiles();
        if(files==null){
            deleteEmptyDirectories(dir);
            return;
        }
        for(File file:files){
            deleteDir(file);
        }
        deleteEmptyDirectories(dir);
    }

    public static File getTmpDir(){
        File dir=getGlobalTmpDir();
        String name=getTmpDirRootName();
        dir=new File(dir, name);
        name=String.valueOf(getCurrentPid());
        dir=new File(dir, name);
        if(!dir.exists()){
            dir.mkdirs();
        }
        return dir;
    }
    private static File getGlobalTmpDir(){
        if(mGlobalTmpDir!=null){
            return mGlobalTmpDir;
        }
        File dir=new File(File.separator+"tmp");
        if(checkDirWritableDir(dir)){
            mGlobalTmpDir=dir;
            return dir;
        }
        dir=new File("tmp");
        checkDirWritableDir(dir);
        mGlobalTmpDir=dir;
        return dir;
    }
    public static void setTmpDirRootName(String name){
        tmp_dir_root_name=name;
    }

    private static String getTmpDirRootName(){
        if(tmp_dir_root_name!=null){
            return tmp_dir_root_name;
        }
        return "reandroid";
    }
    private static long getCurrentPid(){
        if(current_pid!=0){
            return current_pid;
        }
        long pid = System.currentTimeMillis();
        current_pid=pid;
        return pid;
    }
    private static boolean checkDirWritableDir(File dir){
        if(!dir.exists() && !dir.mkdirs()){
            return false;
        }
        File file=new File(dir, "test55557");
        file.deleteOnExit();
        try {
            file.createNewFile();
            if(!file.isFile()){
                return false;
            }
            file.delete();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    public static File getNumberedUniqueFile(File file){
        if(file==null){
            return null;
        }
        if(!file.exists()){
            return file;
        }
        String name=file.getName();
        String ext="";
        int i=name.lastIndexOf(".");
        if(i>0){
            ext=name.substring(i);
            name=name.substring(0,i);
        }
        name=removeLastNum(name);
        File dir=file.getParentFile();
        i=1;
        File result=new File(dir,buildNumberFileName(name, i, ext));
        while(result.exists()&&i<Integer.MAX_VALUE){
            result=new File(dir,buildNumberFileName(name, i, ext));
            i++;
        }
        return result;
    }
    public static File getNextNumberedFile(File file){
        if(file==null){
            return null;
        }
        String name=file.getName();
        String ext="";
        int i=name.lastIndexOf(".");
        if(i>0){
            ext=name.substring(i);
            name=name.substring(0,i);
        }
        i=getLastNum(name)+1;
        name=removeLastNum(name);
        File dir=file.getParentFile();
        File result=new File(dir,buildNumberFileName(name, i, ext));
        return result;
    }
    public static File getLastNumberedFile(File file){
        if(file==null){
            return null;
        }
        if(!file.isFile()){
            return file;
        }
        String name=file.getName();
        String ext="";
        int i=name.lastIndexOf(".");
        if(i>0){
            ext=name.substring(i);
            name=name.substring(0,i);
        }
        File dir=file.getParentFile();
        File[] allFiles=dir.listFiles();
        if(allFiles==null){
            return file;
        }
        i=getLastNum(name)+1;
        name=removeLastNum(name);
        File result=file;
        for(File f:allFiles){
            if(!f.isFile()){
                continue;
            }
            String n=f.getName();
            if(!n.startsWith(name) || !n.endsWith(ext)){
                continue;
            }
            int i2=getLastNum(f);
            if(i2<=i){
                continue;
            }
            result=f;
            i=i2;
        }
        return result;
    }
    private static int getLastNum(File file){
        String name=file.getName();
        int i=name.lastIndexOf(".");
        if(i>0){
            name=name.substring(0,i);
        }
        Pattern pattern=Pattern.compile("^.+_([0-9]{1,6})$");
        Matcher matcher=pattern.matcher(name);
        if(!matcher.find()){
            return 0;
        }
        String num=matcher.group(1);
        return Integer.parseInt(num);
    }

    private static String buildNumberFileName(String name, int i, String ext){
        String num;
        if(i<10000){
            num=String.format("%04d",i);
        }else {
            num=String.valueOf(i);
        }
        return name+"_"+num+ext;
    }
    private static String removeLastNum(String name){
        Pattern pattern=Pattern.compile("^.+(_[0-9]{1,6})$");
        Matcher matcher=pattern.matcher(name);
        if(!matcher.find()){
            return name;
        }
        String num=matcher.group(1);
        int i=name.length()-num.length();
        return name.substring(0, i);
    }
    private static int getLastNum(String name){
        Pattern pattern=Pattern.compile("^.+_([0-9]{1,6})$");
        Matcher matcher=pattern.matcher(name);
        if(!matcher.find()){
            return 0;
        }
        String num=matcher.group(1);
        return Integer.parseInt(num);
    }
    public static void deleteEmptyDirectories(File dir){
        if(dir==null || !dir.isDirectory()){
            return;
        }
        File[] allFiles=dir.listFiles();
        if(allFiles==null || allFiles.length==0){
            dir.delete();
            return;
        }
        int len=allFiles.length;
        for(int i=0;i<len;i++){
            File file=allFiles[i];
            if(file.isDirectory()){
                deleteEmptyDirectories(file);
            }
        }
        allFiles=dir.listFiles();
        if(allFiles==null || allFiles.length==0){
            dir.delete();
        }
    }
    public static void moveFile(File srcFile, File destFile) throws IOException{
        copyFile(srcFile, destFile);
        srcFile.delete();
        deleteEmptyDirectories(srcFile.getParentFile());
    }

    public static void copyFile(File srcFile, File destFile) throws IOException{
        if(destFile.isFile()){
            throw new IOException("Destination exists: "+destFile.getAbsolutePath());
        }
        if(!srcFile.isFile()){
            throw new IOException("Source file does NOT exist: "+srcFile.getAbsolutePath());
        }
        File dir=destFile.getParentFile();
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        FileInputStream inputStream=new FileInputStream(srcFile);
        FileOutputStream outputStream=new FileOutputStream(destFile, false);
        int bufferSize=1024;
        byte[] buffer=new byte[bufferSize];
        int len;
        try{
            while ((len=inputStream.read(buffer, 0, bufferSize))>0){
                outputStream.write(buffer, 0, len);
            }
        }catch (IOException ex){
            destFile.delete();
            throw ex;
        }
        inputStream.close();
        outputStream.flush();
        outputStream.close();
    }
    public static int countLines(File file) throws IOException{
        if(file==null){
            throw new IOException("file == null");
        }
        if(!file.isFile()){
            return 0;
        }
        int result=0;
        InputStreamReader inputStreamReader=new InputStreamReader(new FileInputStream(file));
        final BufferedReader reader = new BufferedReader(inputStreamReader);
        while (reader.readLine() != null) {
            result++;
        }
        reader.close();
        return result;
    }
}
