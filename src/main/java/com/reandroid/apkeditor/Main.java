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
import com.reandroid.arsc.BuildInfo;
import com.reandroid.arsc.coder.xml.XmlEncodeException;
import com.reandroid.commons.command.ARGException;
import com.reandroid.apk.xmlencoder.EncodeException;

import java.io.IOException;

public class Main {

    public static void main(String[] args){
        int result = execute(args);
        System.exit(result);
    }

    /**
     * If you are running inside java application, use this method to
     * avoid unwanted System.exit()
     *
     * Returns 0 - executed successfully
     * Returns 1 - error
     * Returns 2 - non executing commands like help, version
     *
     * */

    public static int execute(String[] args){
        args = Util.trimNull(args);
        if(Util.isHelp(args) || args == null){
            System.err.println(getHelp());
            return 2;
        }
        if(Util.isVersion(args)){
            System.err.println(getVersion());
            return 2;
        }
        String command = getCommand(args);
        args = Util.trimNull(args);
        int result = 1;
        try {
            execute(command, args);
            result = 0;
        } catch (ARGException ex1) {
            System.err.flush();
            System.err.println(ex1.getMessage());
        }catch (EncodeException | XmlEncodeException ex) {
            System.err.flush();
            System.err.println("\nERROR:\n"+ex.getMessage());
        } catch (IOException ex2) {
            System.err.flush();
            System.err.println("\nERROR:");
            ex2.printStackTrace(System.err);
        }
        return result;
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

        builder.append("\n\n other options: \n");
        table = new String[][]{
                new String[]{" -h | -help | --help", "prints this help and exit"},
                new String[]{"-version | --version", "prints version information of this tool and exit"}
        };
        StringHelper.printTwoColumns(builder, "  ", "   -   ", Options.PRINT_WIDTH, table);

        builder.append("\n\n run with <command> -h to get detailed help about each command\n");

        return builder.toString();
    }
    private static String getVersion(){
        return APKEditor.getName() +
                " version " + APKEditor.getVersion() +
                ", " + BuildInfo.getName() +
                " version " + BuildInfo.getVersion();
    }
    private static String getWelcome(){
        return APKEditor.getName() +
                " - " + APKEditor.getVersion() +
                "\nUsing: " + APKEditor.getARSCLibInfo() +
                "\n" + APKEditor.getRepo() +
                "\n" + APKEditor.getDescription();
    }
    private static String getCommand(String[] args){
        String cmd=args[0];
        args[0]=null;
        return cmd;
    }
}
