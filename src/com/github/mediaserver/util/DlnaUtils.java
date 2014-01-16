package com.github.mediaserver.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.mediaserver.server.center.LocalConfigSharePreference;

import android.content.Context;



public class DlnaUtils {

	private static final CommonLog log = LogFactory.createLog();
	
	public static boolean setDevName(Context context, String friendName){
		return LocalConfigSharePreference.commintDevName(context, friendName);
	}
	
	public static String getDevName(Context context){
		return LocalConfigSharePreference.getDevName(context);
	}
	
	
	public static String creat12BitUUID(Context context){
		String defaultUUID  = "123456789abc";
		
		String mac = CommonUtil.getLocalMacAddress(context);
	
		mac = mac.replace(":","");
		mac = mac.replace(".","");
		
		if (mac.length() != 12){
			mac = defaultUUID;
		}
		
		mac += "-dms";
		return mac;
	}
}
