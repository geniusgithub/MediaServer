package com.github.mediaserver.server.center;


import android.content.Context;
import android.content.Intent;

import com.github.mediaserver.ServerApplication;
import com.github.mediaserver.server.DMSService;
import com.github.mediaserver.util.CommonLog;
import com.github.mediaserver.util.LogFactory;

public class MediaServerProxy implements IBaseEngine{

	private static final CommonLog log = LogFactory.createLog();
	
	private static  MediaServerProxy mInstance;
	private Context mContext;
	
	private MediaServerProxy(Context context) {
		mContext = context;
	}

	public static synchronized MediaServerProxy getInstance() {
		if (mInstance == null){
			mInstance  = new MediaServerProxy(ServerApplication.getInstance());
		}
		return mInstance;
	}

	@Override
	public boolean startEngine() {
		mContext.startService(new Intent(DMSService.START_SERVER_ENGINE));
		return true;
	}

	@Override
	public boolean stopEngine() {
		mContext.stopService(new Intent(mContext, DMSService.class));
		return true;
	}

	@Override
	public boolean restartEngine() {
		mContext.startService(new Intent(DMSService.RESTART_SERVER_ENGINE));
		return true;
	}

}
