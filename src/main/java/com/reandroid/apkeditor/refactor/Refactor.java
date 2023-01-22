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

 import com.reandroid.apkeditor.BaseCommand;
 import com.reandroid.apkeditor.Util;
 import com.reandroid.archive.WriteProgress;
 import com.reandroid.archive.ZipAlign;
 import com.reandroid.commons.command.ARGException;
 import com.reandroid.commons.utils.log.Logger;
 import com.reandroid.apk.APKLogger;
 import com.reandroid.apk.ApkModule;

 import java.io.File;
 import java.io.IOException;

 public class Refactor extends BaseCommand implements WriteProgress {
     private final RefactorOptions options;
     private APKLogger mApkLogger;
     public Refactor(RefactorOptions options){
         this.options=options;
     }
     public void run() throws IOException {
         log("Loading apk: "+options.inputFile);
         ApkModule module=ApkModule.loadApkFile(options.inputFile);
         module.setAPKLogger(getAPKLogger());
         if(!module.hasTableBlock()){
             throw new IOException("Don't have resources.arsc");
         }
         if(options.fixTypeNames){
             TypeNameRefactor typeNameRefactor=new TypeNameRefactor(module);
             typeNameRefactor.setApkLogger(getAPKLogger());
             typeNameRefactor.refactor();
         }
         log("Auto refactoring ...");
         AutoRefactor autoRefactor=new AutoRefactor(module);
         int autoRenameCount=autoRefactor.refactor();
         log("Auto renamed entries: "+autoRenameCount);
         if(options.publicXml!=null){
             log("Renaming from: "+options.publicXml);
             PublicXmlRefactor publicXmlRefactor =
                     new PublicXmlRefactor(module, options.publicXml);
             int pubXmlRenameCount = publicXmlRefactor.refactor();
             log("Renamed from public.xml entries: "+pubXmlRenameCount);
         }
         removeSignature(module);
         log("Writing apk ...");
         module.writeApk(options.outputFile, this);
         log("Zip align ...");
         ZipAlign.align4(options.outputFile);
         log("Saved to: "+options.outputFile);
         log("Done");
     }
     @Override
     public void onCompressFile(String path, int method, long length) {
         StringBuilder builder=new StringBuilder();
         builder.append("Writing: ");
         if(path.length()>30){
             path=path.substring(path.length()-30);
         }
         builder.append(path);
         logSameLine(builder.toString());
     }
     private APKLogger getAPKLogger(){
         if(mApkLogger!=null){
             return mApkLogger;
         }
         mApkLogger = new APKLogger() {
             @Override
             public void logMessage(String msg) {
                 Logger.i(getLogTag()+msg);
             }
             @Override
             public void logError(String msg, Throwable tr) {
                 Logger.e(getLogTag()+msg, tr);
             }
             @Override
             public void logVerbose(String msg) {
                 if(msg.length()>30){
                     msg=msg.substring(msg.length()-30);
                 }
                 Logger.sameLine(getLogTag()+msg);
             }
         };
         return mApkLogger;
     }
     public static void execute(String[] args) throws ARGException, IOException {
         if(Util.isHelp(args)){
             throw new ARGException(RefactorOptions.getHelp());
         }
         RefactorOptions option=new RefactorOptions();
         option.parse(args);
         File outFile=option.outputFile;
         Util.deleteEmptyDirectories(outFile);
         if(outFile.exists()){
             if(!option.force){
                 throw new ARGException("Path already exists: "+outFile);
             }
             log("Deleting: "+outFile);
             Util.deleteDir(outFile);
         }
         log("Refactoring ...\n"+option);
         Refactor refactor=new Refactor(option);
         refactor.run();
     }
     private static void logSameLine(String msg){
         Logger.sameLine(getLogTag()+msg);
     }
     private static void log(String msg){
         Logger.i(getLogTag()+msg);
     }
     private static String getLogTag(){
         return "[REFACTOR] ";
     }
     public static boolean isCommand(String command){
         if(Util.isEmpty(command)){
             return false;
         }
         command=command.toLowerCase().trim();
         return command.equals(ARG_SHORT) || command.equals(ARG_LONG);
     }
     public static final String ARG_SHORT="x";
     public static final String ARG_LONG="refactor";
     public static final String DESCRIPTION="Refactors obfuscated resource names";
}
