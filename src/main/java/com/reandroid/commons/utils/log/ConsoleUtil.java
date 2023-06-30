/*
 * Taken from https://github.com/JesusFreke/smali
 */

package com.reandroid.commons.utils.log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleUtil {
    private static Boolean succeedOnce;
    /**
     * Attempt to find the width of the console. If it can't get the width, return a default of 80
     * @return The current console width
     */
    public static int getConsoleWidth() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            try {
                return attemptMode();
            } catch (Exception ex) {
                succeedOnce = false;
            }
        } else {
            try {
                return attemptStty();
            } catch (Exception ex) {
                succeedOnce = false;
            }
        }

        return 80;
    }
    public static Boolean getSucceedOnce() {
        return succeedOnce;
    }
    private static int attemptStty() {
        String output = attemptCommand(new String[]{"sh", "-c", "stty size < /dev/tty"});
        if (output == null) {
            succeedOnce = false;
            return 80;
        }

        String[] vals = output.split(" ");
        if (vals.length < 2) {
            succeedOnce = false;
            return 80;
        }
        int result = Integer.parseInt(vals[1]);
        if(succeedOnce == null || !succeedOnce){
            succeedOnce = true;
        }
        return result;
    }

    private static int attemptMode() {
        String output = attemptCommand(new String[]{"mode", "con"});
        if (output == null) {
            succeedOnce = false;
            return 80;
        }

        Pattern pattern = Pattern.compile("Columns:[ \t]*(\\d+)");
        Matcher m = pattern.matcher(output);
        if (!m.find()) {
            succeedOnce = false;
            return 80;
        }

        int result = Integer.parseInt(m.group(1));
        if(succeedOnce == null || !succeedOnce){
            succeedOnce = true;
        }
        return result;
    }

    private static String attemptCommand(String[] command) {
        StringBuffer buffer = null;

        try {

            Process p = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;

            while ((line = reader.readLine()) != null) {
                if (buffer == null) {
                    buffer = new StringBuffer();
                }

                buffer.append(line);
            }

            if (buffer != null) {
                return buffer.toString();
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }
}
