package com.github.mediaserver.server;




import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.github.mediaserver.server.center.DMSWorkThread;
import com.github.mediaserver.server.center.IBaseEngine;
import com.github.mediaserver.server.center.MediaStoreCenter;
import com.github.mediaserver.util.CommonLog;
import com.github.mediaserver.util.DlnaUtils;
import com.github.mediaserver.util.LogFactory;

public class DMSService extends Service implements IBaseEngine{

	private static final CommonLog log = LogFactory.createLog();
	
	public static final String START_SERVER_ENGINE = "com.geniusgithub.start.dmsengine";
	public static final String RESTART_SERVER_ENGINE = "com.geniusgithub.restart.dmsengine";

	private DMSWorkThread mWorkThread;

	private Handler mHandler;
	private static final int START_ENGINE_MSG_ID = 0x0001;
	private static final int RESTART_ENGINE_MSG_ID = 0x0002;
	
	private static final int DELAY_TIME = 1000;
	
	private MediaStoreCenter mMediaStoreCenter;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();		
		initService();	
		log.e("MediaServerService onCreate");
	}

	@Override
	public void onDestroy() {
		unInitService();	
		log.e("MediaServerService onDestroy");
		super.onDestroy();
	
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if (intent != null){
			String actionString = intent.getAction();
			if (actionString != null){		
				if (actionString.equalsIgnoreCase(START_SERVER_ENGINE)){
					delayToSendStartMsg();
				}else if (actionString.equalsIgnoreCase(RESTART_SERVER_ENGINE)){
					delayToSendRestartMsg();
				}
			}
		}	
	
		return super.onStartCommand(intent, flags, startId);
		
	}
	
	
	private void initService(){

		mWorkThread = new DMSWorkThread(this);
		
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what){
				case START_ENGINE_MSG_ID:
					startEngine();
					break;
				case RESTART_ENGINE_MSG_ID:
					restartEngine();
					break;
				}
			}
			
		};
		
		mMediaStoreCenter = MediaStoreCenter.getInstance();
		mMediaStoreCenter.clearWebFolder();
		mMediaStoreCenter.createWebFolder();
		mMediaStoreCenter.doScanMedia();
	}

	
	private void unInitService(){
		stopEngine();
		removeStartMsg();
		removeRestartMsg();
		mMediaStoreCenter.clearAllData();
	}

	private void delayToSendStartMsg(){
		removeStartMsg();
		mHandler.sendEmptyMessageDelayed(START_ENGINE_MSG_ID, DELAY_TIME);
	}
	
	private void delayToSendRestartMsg(){
		removeStartMsg();
		removeRestartMsg();
		mHandler.sendEmptyMessageDelayed(RESTART_ENGINE_MSG_ID, DELAY_TIME);
	}
	
	private void removeStartMsg(){
		mHandler.removeMessages(START_ENGINE_MSG_ID);
	}
	
	private void removeRestartMsg(){
		mHandler.removeMessages(RESTART_ENGINE_MSG_ID);	
	}
	
	
	@Override
	public boolean startEngine() {
		awakeWorkThread();
		return true;
	}

	@Override
	public boolean stopEngine() {
		mWorkThread.setParam("", "", "");
		exitWorkThread();
		return true;
	}

	@Override
	public boolean restartEngine() {
		String friendName = DlnaUtils.getDevName(this);
		String uuid = DlnaUtils.creat12BitUUID(this);
		mWorkThread.setParam(mMediaStoreCenter.getRootDir(), friendName, uuid);
		if (mWorkThread.isAlive()){
			mWorkThread.restartEngine();
		}else{
			mWorkThread.start();
		}
		return true;
	}

	private void awakeWorkThread(){
		String friendName = DlnaUtils.getDevName(this);
		String uuid = DlnaUtils.creat12BitUUID(this);
		mWorkThread.setParam(mMediaStoreCenter.getRootDir(), friendName, uuid);
		
		
		if (mWorkThread.isAlive()){
			mWorkThread.awakeThread();
		}else{
			mWorkThread.start();
		}
	}
	
	private void exitWorkThread(){
		if (mWorkThread != null && mWorkThread.isAlive()){
			mWorkThread.exit();
			long time1 = System.currentTimeMillis();
			while(mWorkThread.isAlive()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			long time2 = System.currentTimeMillis();
			log.e("exitWorkThread cost time:" + (time2 - time1));
			mWorkThread = null;
		}
	}


}
