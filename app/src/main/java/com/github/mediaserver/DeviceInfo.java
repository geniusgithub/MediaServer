package com.github.mediaserver;

public class DeviceInfo {

	public String dev_name;
	public String uuid;
	public String rootDir;
	public boolean status;
	
	public DeviceInfo(){
		rootDir = "";
		dev_name = "";
		uuid = "";
		status = false;
	}
}
