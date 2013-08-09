a MediaServer run in Android Platform 
===========
MediaServer is a DLNA device(DMS) that can be browsed by control<br />  It uses the framework of Platinum

Example screenshot below:

![github](http://img.my.csdn.net/uploads/201308/09/1376054213_2513.png "github")  



Code fragment
--------------------
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



Run requirements
------------------------------
Android OS 2.3x and up<br />
Tested with: Samsung, HTC, HuaWei Phone and so on...


Contributing
------------------------------
Feel free to drop me pull requests. If you plan to implement something more than a few lines, then open the pull request early so that there aren't any nasty surprises later.
If you want to add something that will require some for of persistence incl. persistent configuration or API keys, etc., then open a pull request/issue especially early!


### Links
csdn blog : [http://blog.csdn.net/geniuseoe2012](http://blog.csdn.net/geniuseoe2012)<br /> 
dlna doc: [DLNA Document](http://download.csdn.net/detail/geniuseoe2012/4969961)<br />
Platinum SDK : [Platinum](http://sourceforge.net/projects/platinum/)<br /> 

### Development
If you think this article useful Nepal , please pay attention to me<br />
Your support is my motivation, I will continue to strive to do better
