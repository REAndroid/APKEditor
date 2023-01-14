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

 import com.reandroid.apkeditor.APKEditor;
 import com.reandroid.apkeditor.Options;
 import com.reandroid.apkeditor.utils.StringHelper;
 import com.reandroid.commons.command.ARGException;

 import java.io.File;

 public class RefactorOptions extends Options {
     public File publicXml;
     public boolean fixTypeNames;
     public RefactorOptions(){
         super();
     }
     @Override
     public void parse(String[] args) throws ARGException {
         parseInput(args);
         parseOutput(args);
         parsePublicXml(args);
         parseFixTypes(args);
         super.parse(args);
     }
     private void parseFixTypes(String[] args) throws ARGException {
         fixTypeNames=containsArg(ARG_fix_types, true, args);
     }
     private void parsePublicXml(String[] args) throws ARGException {
         this.publicXml=null;
         File file=parseFile(ARG_public_xml, args);
         if(file==null){
             return;
         }
         if(!file.isFile()){
             throw new ARGException("No such file: "+file);
         }
         this.publicXml=file;
     }
     private void parseOutput(String[] args) throws ARGException {
         this.outputFile=null;
         File file=parseFile(ARG_output, args);
         if(file==null){
             file=getOutputApkFromInput(inputFile);
         }
         this.outputFile=file;
     }
     private File getOutputApkFromInput(File file){
         String name = file.getName();
         int i=name.lastIndexOf('.');
         if(i>0){
             name=name.substring(0, i);
         }
         name=name+"_refactored.apk";
         File dir=file.getParentFile();
         if(dir==null){
             return new File(name);
         }
         return new File(dir, name);
     }
     private void parseInput(String[] args) throws ARGException {
         this.inputFile=null;
         File file=parseFile(ARG_input, args);
         if(file==null){
             throw new ARGException("Missing input file");
         }
         if(!file.isFile()){
             throw new ARGException("No such file: "+file);
         }
         this.inputFile=file;
     }
     @Override
     public String toString(){
         StringBuilder builder=new StringBuilder();
         builder.append("      Input: ").append(inputFile);
         builder.append("\n    Output: ").append(outputFile);
         if(publicXml!=null){
             builder.append("\n PublicXml: ").append(publicXml);
         }
         if(force){
             builder.append("\n Force: true");
         }
         builder.append("\n ---------------------------- ");
         return builder.toString();
     }
     public static String getHelp(){
         StringBuilder builder=new StringBuilder();
         builder.append(Refactor.DESCRIPTION);
         builder.append("\nOptions:\n");
         String[][] table=new String[][]{
                 new String[]{ARG_input, ARG_DESC_input},
                 new String[]{ARG_output, ARG_DESC_output},
                 new String[]{ARG_public_xml, ARG_DESC_public_xml}
         };
         StringHelper.printTwoColumns(builder, "   ", 75, table);
         builder.append("\nFlags:\n");
         table=new String[][]{
                 new String[]{ARG_fix_types, ARG_DESC_fix_types},
                 new String[]{ARG_force, ARG_DESC_force}
         };
         StringHelper.printTwoColumns(builder, "   ", 75, table);
         String jar = APKEditor.getJarName();
         builder.append("\n\nExample-1:");
         builder.append("\n   java -jar ").append(jar).append(" ").append(Refactor.ARG_SHORT).append(" ")
                 .append(ARG_input).append(" path/to/input.apk");
         builder.append(" ").append(ARG_output).append(" path/to/out.apk");
         return builder.toString();
     }

     private static final String ARG_public_xml = "-public-xml";
     private static final String ARG_DESC_public_xml = "Path of resource ids xml file (public.xml)\nLoads names and applies to resources from 'public.xml' file";

     private static final String ARG_fix_types = "-fix-types";
     private static final String ARG_DESC_fix_types = "Corrects resource type names based on usages and values";
}
