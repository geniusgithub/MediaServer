package com.github.mediaserver.server.center;

import com.github.mediaserver.ServerApplication;
import com.github.mediaserver.server.jni.DMSJniInterface;
import com.github.mediaserver.util.CommonLog;
import com.github.mediaserver.util.CommonUtil;
import com.github.mediaserver.util.LogFactory;
import android.content.Context;


public class DMSWorkThread extends Thread implements IBaseEngine{


	private static final CommonLog log = LogFactory.createLog();
	
	private static final int CHECK_INTERVAL = 30 * 1000; 
	
	private Context mContext = null;
	private boolean mStartSuccess = false;
	private boolean mExitFlag = false;
	
	private String mRootdir = "";
	private String mFriendName = "";
	private String mUUID = "";	
	private ServerApplication mApplication;
	
	public DMSWorkThread(Context context){
		mContext = context;
		mApplication = ServerApplication.getInstance();
	}
	
	public void  setFlag(boolean flag){
		mStartSuccess = flag;
	}
	
	public void setParam(String rootDir, String friendName, String uuid){
		mRootdir = rootDir;
		mFriendName = friendName;
		mUUID = uuid;
		mApplication.updateDevInfo(mRootdir, mFriendName, mUUID);
	}
	
	public void awakeThread(){
		synchronized (this) {
			notifyAll();
		}
	}
	
	public void exit(){
		mExitFlag = true;
		awakeThread();
	}

	@Override
	public void run() {

		log.e("DMSWorkThread run...");
		
		while(true)
		{
			if (mExitFlag){
				stopEngine();
				break;
			}
			refreshNotify();
			synchronized(this)
			{				
				try
				{
					wait(CHECK_INTERVAL);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}								
			}
			if (mExitFlag){
				stopEngine();
				break;
			}
		}
		
		log.e("DMSWorkThread over...");
		
	}
	
	public void refreshNotify(){
		if (!CommonUtil.checkNetworkState(mContext)){
			return ;
		}
		
		if (!mStartSuccess){
			stopEngine();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			boolean ret = startEngine();
			if (ret){
				mStartSuccess = true;
			}
		}

	}
	
	@Override
	public boolean startEngine() {
		if (mFriendName.length() == 0){
			return false;
		}

		int ret = DMSJniInterface.startServer(mRootdir, mFriendName, mUUID);
		
		boolean result = (ret == 0 ? true : false);
		mApplication.setDevStatus(result);
		return result;
	}

	@Override
	public boolean stopEngine() {
		DMSJniInterface.stopServer();
		mApplication.setDevStatus(false);
		return true;
	}

	@Override
	public boolean restartEngine() {
		setFlag(false);
		awakeThread();
		return true;
	}

}
