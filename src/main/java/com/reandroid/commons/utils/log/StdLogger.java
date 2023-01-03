package com.reandroid.commons.utils.log;

import java.io.PrintStream;

public class StdLogger extends Logger{
    private static StdLogger sInstance;
    private final Object mLock=new Object();
    private final PrintStream printStream;
    private boolean mSameLine;
    private int lastLineLen;
    private StdLogger() {
        super();
        this.printStream=System.err;
    }
    @Override
    public void writeLine(String line) {
        synchronized (mLock){
            if(line==null){
                line="null";
            }
            PrintStream stream=printStream;
            if(mSameLine){
                stream.print('\n');
                mSameLine=false;
            }
            stream.println(line);
        }
    }
    @Override
    public void writeSameLine(String line) {
        synchronized (mLock){
            if(line==null){
                line="null";
            }
            PrintStream stream=printStream;
            stream.print('\r');
            if(!mSameLine){
                mSameLine=true;
            }
            int max=lastLineLen;
            for(int i=0;i<max;i++){
                stream.print(' ');
            }
            stream.print('\r');
            stream.print(line);
            stream.print('\r');
            lastLineLen=line.length();
        }
    }
    static StdLogger getInstance(){
        if(sInstance!=null){
            return sInstance;
        }
        synchronized (StdLogger.class){
            sInstance=new StdLogger();
            sInstance.setEnable(true);
            return sInstance;
        }
    }
}
