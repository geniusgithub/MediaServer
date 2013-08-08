package com.github.mediaserver;

import com.github.mediaserver.util.CommonLog;
import com.github.mediaserver.util.LogFactory;

import android.app.Application;



public class ServerApplication  extends Application{

	private static final CommonLog log = LogFactory.createLog();

	private static ServerApplication mInstance;

	private DeviceInfo mDeviceInfo;
	
	
	public synchronized static ServerApplication getInstance(){
		return mInstance;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		log.e("ServerApplication onCreate");
		
		mInstance = this;
		mDeviceInfo = new DeviceInfo();
	}

	public void updateDevInfo(String rootDir, String name, String uuid){
		mDeviceInfo.rootDir = rootDir;
		mDeviceInfo.dev_name = name;
		mDeviceInfo.uuid = uuid;
	}
	
	public void setDevStatus(boolean flag){
		mDeviceInfo.status = flag;
		DeviceUpdateBrocastFactory.sendDevUpdateBrocast(this);
	}
	
	public DeviceInfo getDevInfo(){
		return mDeviceInfo;
	}
	
}
