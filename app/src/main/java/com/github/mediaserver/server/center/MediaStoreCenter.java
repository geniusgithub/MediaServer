package com.github.mediaserver.server.center;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.github.mediaserver.ServerApplication;
import com.github.mediaserver.server.media.IMediaScanListener;
import com.github.mediaserver.server.media.MediaScannerCenter;
import com.github.mediaserver.util.CommonLog;
import com.github.mediaserver.util.FileHelper;
import com.github.mediaserver.util.LogFactory;

public class MediaStoreCenter implements IMediaScanListener{


	private static final CommonLog log = LogFactory.createLog();
	
	private static  MediaStoreCenter mInstance;
	private Context mContext;
	
	
	private String mShareRootPath = "";
	private String mImageFolderPath = "";
	private String mVideoFolderPath = "";
	private String mAudioFolderPath = "";
	
	private MediaScannerCenter mMediaScannerCenter;
	private Map<String, String> mMediaStoreMap = new HashMap<String, String>();
	
	
	private MediaStoreCenter(Context context) {
		mContext = context;
		
		initData();
	}

	public static synchronized MediaStoreCenter getInstance() {
		if (mInstance == null){
			mInstance  = new MediaStoreCenter(ServerApplication.getInstance());
		}
		return mInstance;
	}

	private void initData(){
		mShareRootPath = mContext.getFilesDir().getAbsolutePath()+"/" + "rootFolder";
		mImageFolderPath = mShareRootPath + "/" + "Image";
		mVideoFolderPath = mShareRootPath + "/" + "Video";
		mAudioFolderPath = mShareRootPath + "/" + "Audio";
		mMediaScannerCenter = MediaScannerCenter.getInstance();
	}
	
	public String getRootDir(){
		return mShareRootPath;
	}
	public void clearAllData(){
		stopScanMedia();
		clearMediaCache();
		clearWebFolder();
	}
	
	public boolean createWebFolder(){
		boolean ret = FileHelper.createDirectory(mShareRootPath);
		if (!ret){
			return false;
		}
		
		FileHelper.createDirectory(mImageFolderPath);
		FileHelper.createDirectory(mVideoFolderPath);
		FileHelper.createDirectory(mAudioFolderPath);
		
		return true;
	}
	
	public boolean clearWebFolder(){

		long time = System.currentTimeMillis();
		boolean ret = FileHelper.deleteDirectory(mShareRootPath);
		long time1 = System.currentTimeMillis();
		log.e("clearWebFolder cost : " + (time1 - time));
		return ret;
	}

	public void clearMediaCache(){
		mMediaStoreMap.clear();
	}
	
	public void doScanMedia(){
		mMediaScannerCenter.startScanThread(this);
	}
	
	public void stopScanMedia(){
		mMediaScannerCenter.stopScanThread();
		while(!mMediaScannerCenter.isThreadOver()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void mediaScan(int mediaType, String mediaPath, String mediaName) {
		
		switch (mediaType) {
		case MediaScannerCenter.AUDIO_TYPE:
			mapAudio(mediaPath, mediaName);
			break;
		case MediaScannerCenter.VIDEO_TYPE:
			mapVideo(mediaPath, mediaName);
			break;
		case MediaScannerCenter.IMAGE_TYPE:
		    mapImage(mediaPath, mediaName);
			break;
		default:
			break;
		}
		
	}
	
	
	private void mapAudio( String mediaPath, String mediaName){
		String webPath = mAudioFolderPath + "/" + mediaName;
		mMediaStoreMap.put(mediaPath, webPath);
		softLinkMode(mediaPath, webPath);
	}
	
	private void mapVideo( String mediaPath, String mediaName){
		String webPath = mVideoFolderPath + "/" + mediaName;
		mMediaStoreMap.put(mediaPath, webPath);
		softLinkMode(mediaPath, webPath);
	}
	
	private void mapImage( String mediaPath, String mediaName){
		String webPath = mImageFolderPath + "/" + mediaName;
		mMediaStoreMap.put(mediaPath, webPath);
		softLinkMode(mediaPath, webPath);
	}
	
	
	private boolean softLinkMode(String localPath, String webPath){
		Process p;
		int status;
		try {
			long time = System.currentTimeMillis();
			String cmd = "ln -s " + localPath + " "+ webPath;
			p = Runtime.getRuntime().exec(cmd);
			releaseProcessStream(p);
			
			status = p.waitFor();		
			if (status == 0) {
				return true;//success
			} else {
				log.e("status = " + status + ", run ln -s failed !localPath = " + localPath);
				return false;
			}
		}catch (Exception e) {
			log.e("Catch Exceptino run ln -s failed !localPath = " + localPath);
			return false;
		}
	}
	
	private void releaseProcessStream(Process p) throws IOException{
		InputStream stderr = p.getErrorStream();
		InputStreamReader isr = new InputStreamReader(stderr);
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		while ( (line = br.readLine()) != null)
			System.out.println(line);
	}
}
