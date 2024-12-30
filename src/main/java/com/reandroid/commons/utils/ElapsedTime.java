package com.reandroid.commons.utils;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ElapsedTime {
    private long startTime;
    private long previous;
    private boolean mIsPaused;
    public ElapsedTime(){
        startTime=System.currentTimeMillis();
    }

    public void reset(){
        startTime=System.currentTimeMillis();
        previous=0;
    }
    public String now(){
        return now(true);
    }
    public String now(boolean ignoreZero){
        long diff=elapsed();
        return msToDisplayTime(diff,true, ignoreZero);
    }
    public boolean isExpired(long ms){
        return elapsed()>=ms;
    }
    public String remaining(long ms){
        long el=elapsed();
        long diff=ms-el;
        if(diff<0){
            return "--:--";
        }
        return msToDisplayTime(diff,true, true);
    }

    public void pause() {
        if(mIsPaused){
            return;
        }
        this.previous = elapsed();
        this.mIsPaused = true;
    }
    public void resume() {
        if(!mIsPaused){
            return;
        }
        this.mIsPaused = false;
        this.startTime=System.currentTimeMillis();
    }
    public boolean isPaused() {
        return mIsPaused;
    }

    public long elapsed(){
        return getDiff()+previous;
    }
    private long getDiff(){
        if(mIsPaused){
            return 0;
        }
        return System.currentTimeMillis()-startTime;
    }
    private String msToDisplayTime(long l, boolean includeMs, boolean ignoreZero) {
        long hr = l / 3600000; // 1 hour = 3600000 milliseconds
        long min = (l % 3600000) / 60000; // 1 minute = 60000 milliseconds
        long sec = (l % 60000) / 1000; // 1 second = 1000 milliseconds
        long ms = l % 1000; // Remaining milliseconds

        if (!includeMs) {
            return String.format(Locale.ENGLISH, "%02d:%02d:%02d", hr, min, sec);
        }

        boolean showMs = min < 10;
        if (ignoreZero) {
            if (hr == 0 && min == 0) {
                return String.format(Locale.ENGLISH, "%02d.%03d", sec, ms);
            }
            if (hr == 0) {
                return showMs ? String.format(Locale.ENGLISH, "%02d:%02d.%03d", min, sec, ms) : String.format(Locale.ENGLISH, "%02d:%02d", min, sec);
            }
        }
        return showMs ? String.format(Locale.ENGLISH, "%02d:%02d:%02d.%03d", hr, min, sec, ms) : String.format(Locale.ENGLISH, "%02d:%02d:%02d", hr, min, sec);
    }
}