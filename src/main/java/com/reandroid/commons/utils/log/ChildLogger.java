package com.reandroid.commons.utils.log;

public interface ChildLogger {
    void log(String msg);
    void log(String tag, String msg);
    void log(Throwable tr);
    void log(String msg, Throwable tr);
    void logSameLine(String msg);
    void logSameLineTimely(String msg);
}
