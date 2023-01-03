package com.reandroid.commons.utils.log;

import com.reandroid.commons.utils.ShutdownHook;
import com.reandroid.commons.utils.StringUtil;

import java.io.IOException;
import java.io.Writer;

public class WriterLogger extends Logger implements ShutdownHook.ShutdownListener {
    private final Object mLock=new Object();
    private Writer mWriter;
    private boolean ignoreSameLine;
    private int mLineCount;
    private boolean mWrittenOnce;
    private int initialLinesCount;
    public WriterLogger(Writer writer, int initialLinesCount) {
        super();
        this.mWriter=writer;
        this.initialLinesCount=initialLinesCount;
        ShutdownHook.INS.addListener(this);
    }
    public WriterLogger(Writer writer) {
        this(writer, 0);
    }

    @Override
    public void onShutdown() {
        synchronized (mLock){
            Writer writer=mWriter;
            if(writer==null){
                return;
            }
            try {
                writer.flush();
                writer.close();
            }catch (IOException ex){
            }
            mWriter=null;
        }
    }
    public int getTotalLineCount() {
        return getInitialLinesCount()+getLineCount();
    }
    public int getLineCount() {
        return mLineCount;
    }
    public int getInitialLinesCount(){
        return initialLinesCount;
    }
    public void resetCount(){
        initialLinesCount=0;
        mLineCount=0;
    }
    public void setIgnoreSameLine(boolean ignore) {
        this.ignoreSameLine = ignore;
    }
    public boolean isIgnoreSameLine() {
        return ignoreSameLine;
    }
    @Override
    public void writeLine(String line) {
        write(line);
    }
    @Override
    public void writeSameLine(String line) {
        if(isIgnoreSameLine()){
            return;
        }
        write(line);
    }
    public Writer getWriter(){
        synchronized (mLock){
            return mWriter;
        }
    }
    public void setWriter(Writer writer) {
        this.mWriter = writer;
    }

    private void write(String line){
        synchronized (mLock){
            Writer writer=getWriter();
            if(writer==null){
                return;
            }
            if(line==null){
                line="null";
            }
            try {
                if(!mWrittenOnce){
                    writer.write("\n");
                    writer.write(getStartLine());
                    mWrittenOnce=true;
                }
                writer.write("\n");
                writer.write(line);
                mLineCount++;
                writer.flush();
                checkLineCount(writer);
            } catch (IOException exception) {
            }
        }
    }
    private void checkLineCount(Writer writer){
        int tot=getTotalLineCount();
        if(tot==0){
            return;
        }
        if(tot%CHECK_LINE_INTERVAL!=0){
            return;
        }
        onTotalCount(writer, tot);
    }
    void onTotalCount(Writer writer, int totalCount){

    }
    String getStartLine(){
        String t= StringUtil.toReadableDate(System.currentTimeMillis());
        return "\n  // Log start << "+t+" >>  //\n  ------------------------------------------";
    }
    private static final int CHECK_LINE_INTERVAL=100;

}
