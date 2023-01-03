package com.reandroid.commons.utils;


import java.io.*;
import java.util.*;
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

    public static String simpleSmaliFileName(File file){
        String path=file.getAbsolutePath();
        Matcher matcher=PATTERN_DEX_DIR.matcher(path);
        if(!matcher.find()){
            int i=path.length();
            if(i>80){
                i=i-80;
                return path.substring(i);
            }
            return path;
        }
        String dex=matcher.group("DexName");
        String relPath=matcher.group("Path");
        return dex+relPath;
    }
    public static List<File> getSmaliFiles(File dir, boolean recursive){
        if(recursive){
            return recursiveSmaliFiles(dir);
        }
        return listSmaliFiles(dir);
    }

    public static List<File> listSmaliFiles(File dir){
        return listFilesOnly(dir, EXT_SMALI_FILE);
    }
    public static List<File> recursiveSmaliFiles(File dir){
        return recursiveListFilesOnly(dir, EXT_SMALI_FILE);
    }
    public static List<File> listSmaliDirectories(File dir){
        List<File> subDirs=listSubDirectories(dir);
        if(!hasSmaliDir(subDirs)){
            return subDirs;
        }
        List<File> results=new ArrayList<>();
        for(File file:subDirs){
            if(isSmaliDir(file)){
                results.add(file);
            }
        }
        sortSmaliDirs(results);
        return results;
    }
    private static boolean hasSmaliDir(List<File> smaliDirs){
        for(File file:smaliDirs){
            if(isSmaliDir(file)){
                return true;
            }
        }
        return false;
    }
    private static boolean isSmaliDir(File dir){
        if(!dir.isDirectory()){
            return false;
        }
        String name=dir.getName();
        if(name.equals("smali")){
            return true;
        }
        return name.startsWith("smali_classes");
    }
    public static List<File> listSubDirectories(File dir){
        List<File> results=new ArrayList<>();
        if(dir==null||!dir.isDirectory()){
            return results;
        }
        File[] subDirs=dir.listFiles();
        if(subDirs==null){
            return results;
        }
        int max=subDirs.length;
        for(int i=0;i<max;i++){
            File f=subDirs[i];
            if(f.isDirectory()){
                results.add(f);
            }
        }
        return results;
    }
    public static void sortSmaliDirs(List<File> smaliDirList){
        Comparator<File> cmp=new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                String s1=getSmaliDirCompareName(f1);
                String s2=getSmaliDirCompareName(f2);
                return s1.compareTo(s2);
            }
        };
        smaliDirList.sort(cmp);
    }
    private static String getSmaliDirCompareName(File dir){
        String name=dir.getName();
        if(name.equals("smali")){
            return toHex(0);
        }
        String prefix="smali_classes";
        if(!name.startsWith(prefix)){
            return "FFFFFFFF "+name;
        }
        name=name.replace(prefix,"");
        int i=toInt(name);
        return toHex(i);
    }
    private static int toInt(String txt){
        try {
            return Integer.parseInt(txt);
        }catch (Exception ex){
            return 0;
        }
    }
    private static String toHex(int i){
        return String.format("%08x", i);
    }
    public static List<File> searchFilesContains(File dir, String extension, String... search){
        List<File> results=new ArrayList<>();
        if(dir==null){
            return results;
        }
        File[] files=dir.listFiles();
        if(files==null){
            return results;
        }
        for(int i=0;i<files.length;i++){
            File f=files[i];
            if(f.isDirectory()){
                List<File> sub=searchFilesContains(f,extension,search);
                results.addAll(sub);
            }else if(f.isFile()){
                String name=f.getName();
                if(name.endsWith(extension)){
                    if(fileContains(f,search)){
                        results.add(f);
                    }
                }
            }
        }
        return results;
    }
    public static List<File> listFilesOnly(File dir, String extension){
        List<File> results=new ArrayList<>();
        if(dir==null){
            return results;
        }
        if(dir.isFile()){
            if(hasExtension(dir, extension)){
                results.add(dir);
            }
            return results;
        }
        File[] files=dir.listFiles();
        if(files==null){
            return results;
        }
        for(int i=0;i<files.length;i++){
            File f=files[i];
            if(f.isFile()){
                if(hasExtension(f, extension)){
                    results.add(f);
                }
            }
        }
        return results;
    }
    public static List<File> recursiveListFilesOnly(File dir, String extension){
        List<File> results=new ArrayList<>();
        if(dir==null){
            return results;
        }
        if(dir.isFile()){
            if(hasExtension(dir, extension)){
                results.add(dir);
            }
            return results;
        }
        File[] files=dir.listFiles();
        if(files==null){
            return results;
        }
        for(int i=0;i<files.length;i++){
            File f=files[i];
            if(f.isDirectory()){
                List<File> sub=recursiveListFilesOnly(f,extension);
                results.addAll(sub);
            }else if(f.isFile()){
                if(hasExtension(f, extension)){
                    results.add(f);
                }
            }
        }
        return results;
    }
    private static boolean hasExtension(File file, String ext){
        if(ext==null){
            return true;
        }
        String name=file.getName().toLowerCase();
        ext=ext.toLowerCase();
        return name.endsWith(ext);
    }

    public static boolean fileContains(File file, String... search){
        if(file==null||search==null){
            return false;
        }
        if(search.length==0){
            return false;
        }
        try {
            if(!file.exists()){
                return false;
            }
            if(!file.isFile()){
                return false;
            }
            String filePath=file.getAbsolutePath();
            boolean foundMatch=false;
            final BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.trim().startsWith("#")){
                    continue;
                }
                if(stringContains(line,search)){
                    foundMatch=true;
                    break;
                }
            }
            reader.close();
            return foundMatch;
        }
        catch (IOException ex) {
            return false;
        }
    }
    private static boolean stringContains(String text, String... search){
        if(text==null||search==null){
            return false;
        }
        if(text.length()==0){
            return false;
        }
        int max=search.length;
        if(max==0){
            return false;
        }
        for(int i=0;i<max;i++){
            if(text.contains(search[i])){
                return true;
            }
        }
        return false;
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
    public static List<String> readLines(File file) throws IOException{
        if(file==null){
            throw new IOException("file == null");
        }
        FileInputStream inputStream=new FileInputStream(file);
        return readLines(inputStream);
    }
    public static List<String> readLines(InputStream inputStream) throws IOException{
        if(inputStream==null){
            throw new IOException("inputStream == null");
        }
        List<String> results=new ArrayList<>();
        InputStreamReader inputStreamReader=new InputStreamReader(inputStream);
        final BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        while ((line = reader.readLine()) != null) {
            results.add(line);
        }
        return results;
    }
    public static String readTextFile(File file){
        if(file==null){
            return null;
        }
        try {
            if(!file.exists()){
                return null;
            }
            if(!file.isFile()){
                return null;
            }
            StringBuilder builder=new StringBuilder();
            String filePath=file.getAbsolutePath();
            boolean nextLine=false;
            final BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                if(nextLine){
                    builder.append("\n");
                }else {
                    nextLine=true;
                }
                builder.append(line);
            }
            reader.close();
            return builder.toString();
        }
        catch (IOException ex) {
            return null;
        }
    }
    public static String readTextFile(InputStream inputStream){
        if(inputStream==null){
            return null;
        }
        try {
            StringBuilder builder=new StringBuilder();
            InputStreamReader inputStreamReader=new InputStreamReader(inputStream);
            boolean nextLine=false;
            final BufferedReader reader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = reader.readLine()) != null) {
                if(nextLine){
                    builder.append("\n");
                }else {
                    nextLine=true;
                }
                builder.append(line);
            }
            return builder.toString();
        }
        catch (IOException ex) {
            return null;
        }
    }
    public static void saveToFile(File file, String content){
        if(file==null){
            return;
        }
        try {
            File dir=file.getParentFile();
            if(!ensureDirectoryExists(dir)){
                return;
            }
            if(file.isDirectory()){
                return;
            }
            boolean append=file.exists();
            FileWriter writer=new FileWriter(file);
            if(append){
                writer.write("\n");
            }
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException ex) {
        }
    }
    public static boolean ensureDirectoryExists(File dir){
        if(dir==null){
            return false;
        }
        try{
            if(dir.isDirectory()){
                return true;
            }
            if(dir.isFile()){
                return false;
            }
            return dir.mkdirs();
        }catch (Exception ex){
        }
        return false;
    }

    private static final Pattern PATTERN_DEX_DIR=Pattern.compile("^(?<Root>.*)(?<DexName>smali(_classes[0-9]+)?)(?<Path>[/\\\\].+\\.smali)$");

}
