package com.reandroid.commons.utils;

import java.util.ArrayList;
import java.util.List;

public class ShutdownHook implements Runnable{
    public static final ShutdownHook INS;
    private final List<ShutdownListener> mListeners;
    private ShutdownHook(){
        this.mListeners=new ArrayList<>();
    }
    public void addListener(ShutdownListener listener){
        synchronized (ShutdownHook.class){
            if(listener==null){
                return;
            }
            if(!mListeners.contains(listener)){
                mListeners.add(listener);
            }
        }
    }
    @Override
    public void run() {
        synchronized (ShutdownHook.class){
            for(ShutdownListener listener:mListeners){
                callListener(listener);
            }
        }
    }
    private void callListener(ShutdownListener listener){
        try{
            listener.onShutdown();
        }catch (Throwable tr){

        }
    }
    static {
        INS=new ShutdownHook();
        Thread thread=new Thread(INS);
        Runtime.getRuntime().addShutdownHook(thread);
    }
    public interface ShutdownListener{
        void onShutdown();
    }
}
