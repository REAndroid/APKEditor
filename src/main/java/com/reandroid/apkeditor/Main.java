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

import com.reandroid.apkeditor.compile.Builder;
import com.reandroid.apkeditor.decompile.Decompiler;
import com.reandroid.apkeditor.merge.Merger;
import com.reandroid.apkeditor.protect.Protector;
import com.reandroid.apkeditor.refactor.Refactor;
import com.reandroid.commons.command.ARGException;
import com.reandroid.lib.apk.xmlencoder.EncodeException;

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
        } catch (ARGException| EncodeException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        } catch (IOException ex2) {
            System.err.println(ex2.getMessage());
            ex2.printStackTrace(System.err);
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
        throw new ARGException("Unknown command: "+command);
    }
    private static String getHelp(){
        StringBuilder builder=new StringBuilder();
        builder.append(getWelcome());
        builder.append("\nUsage: \n");
        builder.append(" java -jar ").append(APKEditor.getJarName());
        builder.append(" <command> <args>");
        builder.append("\n commands: ");
        builder.append("\n  1)  ").append(Decompiler.ARG_SHORT).append(" | ").append(Decompiler.ARG_LONG);
        builder.append("     -   ").append(Decompiler.DESCRIPTION);
        builder.append("\n  2)  ").append(Builder.ARG_SHORT).append(" | ").append(Builder.ARG_LONG);
        builder.append("      -   ").append(Builder.DESCRIPTION);
        builder.append("\n  3)  ").append(Merger.ARG_SHORT).append(" | ").append(Merger.ARG_LONG);
        builder.append("      -   ").append(Merger.DESCRIPTION);
        builder.append("\n  4)  ").append(Refactor.ARG_SHORT).append(" | ").append(Refactor.ARG_LONG);
        builder.append("   -   ").append(Refactor.DESCRIPTION);
        builder.append("\n  5)  ").append(Protector.ARG_SHORT).append(" | ").append(Protector.ARG_LONG);
        builder.append("    -   ").append(Protector.DESCRIPTION);
        builder.append("\n run with <command> -h to get detailed help about each command");
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
