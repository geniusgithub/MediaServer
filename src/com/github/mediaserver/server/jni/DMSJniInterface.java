package com.github.mediaserver.server.jni;

import java.io.UnsupportedEncodingException;

public class DMSJniInterface {

    static {
        System.loadLibrary("git-platinum");
    }
 
    public static native int startServer(byte[] rootDir,byte[] name ,byte[] uid);
    public static native int stopServer();  
    
    
    
    public static native boolean enableLogPrint(boolean flag);
     
    //////////////////////////////////////////////////////////////////////////////////////////           
    public static  int startServer(String rootDir, String name ,String uid){
    	if (rootDir == null){
    		rootDir = "";
    	}
    	if (name == null){
    		name = "";
    	}
    	if (uid == null){
    		uid = "";
    	}
    	int ret = -1;
    	try {
    		ret = startServer(rootDir.getBytes("utf-8"), name.getBytes("utf-8"), uid.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	return ret;
    }
    
}
