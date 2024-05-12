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
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.commons.command.ARGException;
import com.reandroid.apk.ApkModule;

import java.io.File;
import java.io.IOException;

public class Refactor extends BaseCommand<RefactorOptions> {
    public Refactor(RefactorOptions options){
        super(options, "[REFACTOR] ");
    }
    @Override
    public void run() throws IOException {
        RefactorOptions options = getOptions();
        logMessage("Loading apk: "+options.inputFile);
        ApkModule module = ApkModule.loadApkFile(this, options.inputFile);
        if(!module.hasTableBlock()){
            throw new IOException("Don't have " + TableBlock.FILE_NAME);
        }
        String protect = Util.isProtected(module);
        if(protect!=null){
            logMessage(options.inputFile.getAbsolutePath());
            logMessage(protect);
            return;
        }
        if(options.fixTypeNames){
            TypeNameRefactor typeNameRefactor=new TypeNameRefactor(module);
            typeNameRefactor.setApkLogger(this);
            typeNameRefactor.refactor();
        }
        if(options.publicXml != null){
            logMessage("Renaming from: " + options.publicXml);
            PublicXmlRefactor publicXmlRefactor =
                    new PublicXmlRefactor(module, options.publicXml);
            publicXmlRefactor.refactor();
        }else {
            logMessage("Auto refactoring ...");
            AutoRefactor autoRefactor=new AutoRefactor(module);
            autoRefactor.refactor();
            logMessage("Auto renamed entries");
            StringValueNameGenerator generator = new StringValueNameGenerator(module.getTableBlock());
            generator.refactor();
        }
        if(options.cleanMeta){
            logMessage("Clearing META-INF ...");
            clearMeta(module);
        }
        Util.addApkEditorInfo(module, getClass().getSimpleName());
        String message = module.refreshTable();
        if(message != null){
            logMessage(message);
        }
        logMessage("Writing apk ...");
        module.writeApk(options.outputFile);
        logMessage("Saved to: "+options.outputFile);
    }
    public static void execute(String[] args) throws ARGException, IOException {
        if(Util.isHelp(args)){
            throw new ARGException(RefactorOptions.getHelp());
        }
        RefactorOptions option = new RefactorOptions();
        option.parse(args);
        File outFile=option.outputFile;
        Util.deleteEmptyDirectories(outFile);
        Refactor refactor = new Refactor(option);
        refactor.logVersion();
        if(outFile.exists()){
            if(!option.force){
                throw new ARGException("Path already exists: "+outFile);
            }
            refactor.logMessage("Deleting: "+outFile);
            Util.deleteDir(outFile);
        }
        refactor.logMessage("Refactoring ...\n"+option);
        refactor.run();
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
