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
package com.reandroid.apkeditor;

import com.reandroid.apkeditor.cloner.Cloner;
import com.reandroid.apkeditor.compile.Builder;
import com.reandroid.apkeditor.decompile.Decompiler;
import com.reandroid.apkeditor.info.Info;
import com.reandroid.apkeditor.merge.Merger;
import com.reandroid.apkeditor.protect.Protector;
import com.reandroid.apkeditor.refactor.Refactor;
import com.reandroid.apkeditor.utils.StringHelper;
import com.reandroid.commons.command.ARGException;
import com.reandroid.apk.xmlencoder.EncodeException;

import java.io.IOException;

public class Main {
    public static void main(String[] args){
        args=Util.trimNull(args);
        if(Util.isHelp(args)){
            System.err.println(getHelp());
            return;
        }
        String command=getCommand(args);
        args=Util.trimNull(args);
        try {
            execute(command, args);
        } catch (ARGException ex1) {
            System.err.flush();
            System.err.println(ex1.getMessage());
            System.exit(1);
        }catch (EncodeException ex) {
            System.err.flush();
            System.err.println("\nERROR:\n"+ex.getMessage());
            System.exit(1);
        } catch (IOException ex2) {
            System.err.flush();
            System.err.println("\nERROR:\n"+ex2.getMessage());
            ex2.printStackTrace(System.err);
            System.exit(1);
        }catch (Exception ex3) {
            System.err.flush();
            System.err.println("\nUnexpected error:\n"+ex3.getMessage());
            ex3.printStackTrace(System.err);
            System.exit(1);
        }
    }
    private static void execute(String command, String[] args) throws ARGException, IOException {
        if(Decompiler.isCommand(command)){
            Decompiler.execute(args);
            return;
        }
        if(Builder.isCommand(command)){
            Builder.execute(args);
            return;
        }
        if(Merger.isCommand(command)){
            Merger.execute(args);
            return;
        }
        if(Refactor.isCommand(command)){
            Refactor.execute(args);
            return;
        }
        if(Protector.isCommand(command)){
            Protector.execute(args);
            return;
        }
        if(Cloner.isCommand(command)){
            Cloner.execute(args);
            return;
        }
        if(Info.isCommand(command)){
            Info.execute(args);
            return;
        }
        throw new ARGException("Unknown command: "+command);
    }
    private static String getHelp(){
        StringBuilder builder=new StringBuilder();
        builder.append(getWelcome());
        builder.append("\nUsage: \n");
        builder.append(" java -jar ").append(APKEditor.getJarName());
        builder.append(" <command> <args>");
        builder.append("\n commands: \n");
        String[][] table = new String[][]{
                new String[]{"  1)  " + Decompiler.ARG_SHORT + " | " + Decompiler.ARG_LONG, Decompiler.DESCRIPTION},
                new String[]{"  2)  " + Builder.ARG_SHORT + " | " + Builder.ARG_LONG, Builder.DESCRIPTION},
                new String[]{"  3)  " + Merger.ARG_SHORT + " | " + Merger.ARG_LONG, Merger.DESCRIPTION},
                new String[]{"  4)  " + Refactor.ARG_SHORT + " | " + Refactor.ARG_LONG, Refactor.DESCRIPTION},
                new String[]{"  5)  " + Protector.ARG_SHORT + " | " + Protector.ARG_LONG, Protector.DESCRIPTION},
                //new String[]{"  6)  " + Cloner.ARG_SHORT + " | " + Cloner.ARG_LONG, Cloner.DESCRIPTION},
                new String[]{"  6)  " + Info.ARG_SHORT, Info.DESCRIPTION}
        };

        StringHelper.printTwoColumns(builder, "  ", "  -  ", Options.PRINT_WIDTH, table);

        builder.append("\n\n run with <command> -h to get detailed help about each command\n");

        return builder.toString();
    }
    private static String getWelcome(){
        StringBuilder builder=new StringBuilder();
        builder.append(APKEditor.getName());
        builder.append(" - ").append(APKEditor.getVersion());
        builder.append("\nUsing: ").append(APKEditor.getARSCLibInfo());
        builder.append("\n").append(APKEditor.getRepo());
        builder.append("\n").append(APKEditor.getDescription());
        return builder.toString();
    }
    private static String getCommand(String[] args){
        String cmd=args[0];
        args[0]=null;
        return cmd;
    }
}
