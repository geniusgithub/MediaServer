package com.github.mediaserver.util;



import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Environment;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

public class CommonUtil {

	private static final CommonLog log = LogFactory.createLog();
	
	public static boolean hasSDCard() {
		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			return false;
		} 
		return true;
	}
	
	public static String getRootFilePath() {
		if (hasSDCard()) {
			return Environment.getExternalStorageDirectory().getAbsolutePath() + "/";// filePath:/sdcard/
		} else {
			return Environment.getDataDirectory().getAbsolutePath() + "/data/"; // filePath: /data/data/
		}
	}
	
	public static boolean checkNetworkState(Context context){
    	boolean netstate = false;
		ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivity != null)
		{
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++)
				{
					if (info[i].getState() == NetworkInfo.State.CONNECTED) 
					{
						netstate = true;
						break;
					}
				}
			}
		}
		return netstate;
    }

	public static String getLocalMacAddress(Context mc){
		String defmac = "00:00:00:00:00:00";
		InputStream   input   =   null;
		String wifimac = getWifiMacAddress(mc);
		if(null != wifimac){
			if(!wifimac.equals(defmac))
				return wifimac;
		}
		try{

			ProcessBuilder builder = new ProcessBuilder( "busybox","ifconfig");
			Process process = builder.start();
			input = process.getInputStream();



			byte[] b = new byte[1024];
			StringBuffer buffer = new StringBuffer();
			while(input.read(b)>0){
				buffer.append(new String(b));
			}
			String value = buffer.substring(0);
			String systemFlag ="HWaddr ";
			int index = value.indexOf(systemFlag);
			//List <String> address   = new ArrayList <String> ();
			if(0<index){
				value = buffer.substring(index+systemFlag.length());
				//address.add(value.substring(0,18));
				defmac=value.substring(0,17);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return defmac;
	}
	
	public static String getWifiMacAddress(Context mc) {   
        WifiManager wifi = (WifiManager) mc.getSystemService(Context.WIFI_SERVICE);   
        WifiInfo info = wifi.getConnectionInfo();   
        return info.getMacAddress();   
    } 
	
	public static boolean openWifiBrocast(Context context){
		WifiManager wifiManager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		MulticastLock  multicastLock=wifiManager.createMulticastLock("MediaServer");
		if (multicastLock != null){
			multicastLock.acquire();
			return true;
		}
		return false;
	}
	
	

	
	public static void showToask(Context context, String tip){
		Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();
	}
	
    
	  public static boolean getWifiState(Context context){
		  ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
		  State wifistate = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		  if (wifistate != State.CONNECTED){
			  return false;
		  }
		  
		  State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		  boolean ret = State.CONNECTED != mobileState;
		  return ret;
	  }
	  
	  
	  public static boolean getMobileState(Context context){
		  ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
		  State wifistate = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		  if (wifistate != State.CONNECTED){
			  return false;
		  }
		  
		  State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		  boolean ret = State.CONNECTED == mobileState;
		  return ret;
	  }
	  
}


