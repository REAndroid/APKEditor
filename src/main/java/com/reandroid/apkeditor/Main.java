package com.reandroid.apkeditor;

import com.reandroid.apkeditor.compile.Builder;
import com.reandroid.apkeditor.decompile.Decompiler;
import com.reandroid.commons.command.ARGException;

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
        } catch (ARGException ex) {
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
        builder.append("    -  ").append(Decompiler.DESCRIPTION);
        builder.append("\n  2)  ").append(Builder.ARG_SHORT).append(" | ").append(Builder.ARG_LONG);
        builder.append("    -  ").append(Builder.DESCRIPTION);
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
