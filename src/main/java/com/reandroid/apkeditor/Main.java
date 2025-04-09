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

import com.reandroid.apk.xmlencoder.EncodeException;
import com.reandroid.apkeditor.compile.BuildOptions;
import com.reandroid.apkeditor.decompile.DecompileOptions;
import com.reandroid.apkeditor.info.InfoOptions;
import com.reandroid.apkeditor.merge.MergerOptions;
import com.reandroid.apkeditor.protect.ProtectorOptions;
import com.reandroid.apkeditor.refactor.RefactorOptions;
import com.reandroid.arsc.ARSCLib;
import com.reandroid.arsc.coder.xml.XmlEncodeException;
import com.reandroid.jcommand.CommandHelpBuilder;
import com.reandroid.jcommand.CommandParser;
import com.reandroid.jcommand.annotations.MainCommand;
import com.reandroid.jcommand.annotations.OnOptionSelected;
import com.reandroid.jcommand.annotations.OtherOption;
import com.reandroid.jcommand.exceptions.CommandException;


@SuppressWarnings("unused")
@MainCommand(
        headers = {"title_app_name_and_version", "title_app_repo", "title_app_description"},
        options = {
                DecompileOptions.class,
                BuildOptions.class,
                MergerOptions.class,
                RefactorOptions.class,
                ProtectorOptions.class,
                InfoOptions.class
                }   
)      

public class Main {
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_ERROR = 1;
    private static final int EXIT_HELP = 2;
    
    private Options currentOptions;
    private int exitCode = EXIT_HELP;
    private boolean emptyOption;

    public static void main(String[] args) {
        System.exit(execute(args));
    }

    public static int execute(String[] args) {
        return new Main().run(args);
    }

    
    @OtherOption(
            names = {"-h", "--help"},
            description = "Display this help and exit"
    )
    private void displayHelp() {
        exitCode = EXIT_HELP;
        System.err.println(buildHelpText());
    }

    @OtherOption(
            names = {"-v", "--version"},
            description = "Display version information"
    )
    private void displayVersion() {
        exitCode = EXIT_HELP;
        System.out.println(buildVersionText());
    }

    // Option selection handle 
    @OnOptionSelected
    private void onOptionSelected(Object option, boolean emptyArgs) {
        this.currentOptions = (Options) option;
        this.emptyOption = emptyArgs;
    }

    private int run(String[] args) {
        resetState();
        
        try {
            CommandParser parser = new CommandParser(Main.class);
            parser.parse(this, args);
            
            if (currentOptions == null) {
                return exitCode;
            }
            
            if (emptyOption) {
                throw new CommandException("empty_command_option_exception");
            }
            
            processSelectedOption();
            
        } catch (CommandException e) {
            handleCommandException(e);
        } catch (EncodeException | XmlEncodeException e) {
            handleEncodeException(e);
        } catch (Exception e) {
            handleUnexpectedException(e);
        }
        
        return exitCode;
    }

    
    private void resetState() {
        currentOptions = null;
        exitCode = EXIT_HELP;
        emptyOption = false;
    }

    private void processSelectedOption() throws Exception {
        currentOptions.validate();
        
        if (currentOptions.help) {
            System.err.println(currentOptions.getHelp());
            return;
        }
        
        exitCode = EXIT_ERROR;
        currentOptions.runCommand();
        exitCode = EXIT_SUCCESS;
    }

    private String buildHelpText() {
        CommandHelpBuilder builder = new CommandHelpBuilder(
                ResourceStrings.INSTANCE, Main.class);
        builder.setFooters("", "help_main_footer", "<command> -h", "");
        return builder.build();
    }

    private String buildVersionText() {
        return String.format("%s version %s, %s version %s",
                APKEditor.getName(), APKEditor.getVersion(),
                ARSCLib.getName(), ARSCLib.getVersion());
    }

    private void handleCommandException(CommandException e) {
        System.err.flush();
        System.err.println(e.getMessage(ResourceStrings.INSTANCE));
    }

    private void handleEncodeException(Exception e) {
        System.err.flush();
        System.err.println("\nERROR:\n" + e.getMessage());
    }

    private void handleUnexpectedException(Exception e) {
        System.err.flush();
        System.err.println("\nUNEXPECTED ERROR:");
        e.printStackTrace(System.err);
    }
}    
                
