package com.reandroid.apkeditor;

import com.reandroid.commons.command.ARGException;

import java.io.File;

public class Options {
    public File inputFile;
    public File outputFile;
    public boolean force;

    protected void checkUnknownOptions(String[] args) throws ARGException {
        args=Util.trimNull(args);
        if(Util.isEmpty(args)){
            return;
        }
        throw new ARGException("Unknown option: "+args[0]);
    }
    protected String parseArgValue(String argSwitch, boolean ignore_case, String[] args) throws ARGException {
        if(ignore_case){
            argSwitch=argSwitch.toLowerCase();
        }
        int max=args.length;
        for(int i=0;i<max;i++){
            String s=args[i];
            if(s==null){
                continue;
            }
            s=s.trim();
            String tmpArg=s;
            if(ignore_case){
                tmpArg=tmpArg.toLowerCase();
            }
            if(tmpArg.equals(argSwitch)){
                int i2=i+1;
                if(i2>=max){
                    throw new ARGException("Missing value near: \""+s+"\"");
                }
                String value=args[i2];
                if(Util.isEmpty(value)){
                    throw new ARGException("Missing value near: \""+s+"\"");
                }
                value=value.trim();
                args[i]=null;
                args[i2]=null;
                return value;
            }
        }
        return null;
    }
    protected File parseFile(String argSwitch, String[] args) throws ARGException {
        int max=args.length;
        for(int i=0;i<max;i++){
            String s=args[i];
            if(s==null){
                continue;
            }
            s=s.trim();
            if(s.equals(argSwitch)){
                int i2=i+1;
                if(i2>=max){
                    throw new ARGException("Missing path near: \""+argSwitch+"\"");
                }
                String path=args[i2];
                if(Util.isEmpty(path)){
                    throw new ARGException("Missing path near: \""+argSwitch+"\"");
                }
                path=path.trim();
                args[i]=null;
                args[i2]=null;
                return new File(path);
            }
        }
        return null;
    }
    protected boolean containsArg(String argSwitch, boolean ignore_case, String[] args) throws ARGException {
        if(ignore_case){
            argSwitch=argSwitch.toLowerCase();
        }
        int max=args.length;
        for(int i=0;i<max;i++){
            String s=args[i];
            if(s==null){
                continue;
            }
            s=s.trim();
            if(ignore_case){
                s=s.toLowerCase();
            }
            if(s.equals(argSwitch)){
                args[i]=null;
                return true;
            }
        }
        return false;
    }

}
