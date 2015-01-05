package com.run.user;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;

import com.aliyun.mbaas.oss.OSSClient;
import com.aliyun.mbaas.oss.callback.GetBytesCallback;
import com.aliyun.mbaas.oss.callback.GetFileCallback;
import com.aliyun.mbaas.oss.callback.SaveCallback;
import com.aliyun.mbaas.oss.model.OSSException;
import com.aliyun.mbaas.oss.model.TokenGenerator;
import com.aliyun.mbaas.oss.storage.OSSData;
import com.aliyun.mbaas.oss.storage.OSSFile;
import com.aliyun.mbaas.oss.util.OSSToolKit;
import com.baidu.mapapi.model.LatLng;
import com.run.db.DBManager;
import com.run.db.DBPoints;
import com.run.utils.RunData;

public class DownloadService extends Service {
	
	private String object;				// 保存用户数据的标志
	private boolean isUserRegisted;		// 该用户是否注册过
	private Handler mHandler;
	
	private String bucketname;
	private String objectkey;
	private String userId;
		
	private static final String CLIENT_TABLE = "default/id.txt";
	private static final String RUN = "rundata.txt";
	private static final String POINTS = "points.txt";
	private static final String SNAPSHOT = "pictures.txt";
	private static final String DIR = Environment.getExternalStorageDirectory() + "/run";
	private static final int START = 1;
	private static final int RUNDATA = 2;			// 下载rundata数据
	private static final int RUNLINE = 3;			// 下载跑步路线数据
	private static final int PICTURES = 4;			// 下载图片
	private static final int UPDATE_SUCCESS = 5;    // 更新完成
	// 阿里云，实际使用中，AK/SK不应明文保存在代码中
	private final static String accessKey = "NTP8U7r0o5mCmPQq";
	private final static String screctKey = "sYiCD5mul0duG1gzxpeeuLqiSx7U9H";

	@Override
	public void onCreate() {		
		super.onCreate();
		Log.i("myservice", "OnCreate service");
		// 初始化阿里云
		OSSClient.setApplicationContext(getApplicationContext());
		// 指明你的bucket所放在数据oss-cn-hangzhou.aliyuncs.com
		OSSClient.setOSSHostId("oss-cn-hangzhou.aliyuncs.com");
		OSSClient.setTokenGenerator(new TokenGenerator() {
			@Override
			public String generateToken(String httpMethod, String md5, String type,
					String date, String ossHeaders, String resource) {
				String signature = null;				
				String content = httpMethod + "\n" + md5 + "\n" + type + "\n" + date + "\n" + 
								ossHeaders + resource;			
				try {
					signature = OSSToolKit.getHmacSha1Signature(content, screctKey);
					signature = signature.trim();
				} catch (Exception e) {	
				}
				
				signature = "OSS " + accessKey + ":" + signature;				
				return signature;
			}
		});		
		initHandler();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {	
		Log.i("myservice", "OnStartCommand");
		bucketname = intent.getStringExtra("bucket");
		objectkey = intent.getStringExtra("objectkey");
		SharedPreferences share = getSharedPreferences("qqlogin", MODE_PRIVATE);
		userId = share.getString("openid", "");		
		Log.i("myservice", "name: " + bucketname + " objectkey: " + objectkey + " openid: " + userId);
		
		DownloadData(objectkey);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	// 处理下载的数据，并且开始下一次下载
	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case START:
					// 检查用户是否为注册用户
					CheckUserId((String)msg.obj);					
					break;
				case RUNDATA:
					// 将用户跑步数据存入数据库
					saveRundata((String)msg.obj);
					// 下载路线数据
					DownloadData(object + "/" + POINTS); 
					break;
				case RUNLINE:
					// 将路线数据存入数据库
					savePoints((String)msg.obj);
					// 获取图片名称列表
					DownloadData(object + "/" + SNAPSHOT); 					
					break;
				case PICTURES:
					// 根据图片名称下载图片
					String data = (String)msg.obj;
					if (data != null) {
						String[] namelist = data.split("\r\n");
						for (String name : namelist)
							DownloadPicture(name);						
					}						
					break;
				case UPDATE_SUCCESS:
					stopSelf();
					break;
				}
			}			
		};
	}
	
	// 下载文件数据
	private void DownloadData(String objectKey) {
		OSSData ossData = new OSSData(bucketname, objectKey);
		ossData.getInBackground(new GetBytesCallback() {			
			@Override
			public void onProgress(int arg0, int arg1) {
			}
			
			@Override
			public void onFailure(OSSException ossException) {
				Log.i("myservice", ossException.getObjectKey() + " download failed! " + ossException.getMessage());
				// onSuccess(ossException.getObjectKey(), null);
				Message msg = new Message();
				msg.what = UPDATE_SUCCESS;
				mHandler.sendMessage(msg);					
			}
			
			@Override
			public void onSuccess(String objectKey, byte[] data) {
				Log.i("myservice", objectKey + " dowload successed!");
				String list = null;
				if (data.length > 0)
					list = new String(data);
				
				if (objectKey.equals(CLIENT_TABLE)) {
					Message msg = new Message();
					msg.what = START;
					msg.obj = list;
					mHandler.sendMessage(msg);
				} else if (objectKey.equals(object + "/" + RUN)) {
					Message msg = new Message();
					msg.what = RUNDATA;
					msg.obj = list;
					mHandler.sendMessage(msg);
				} else if (objectKey.equals(object + "/" + POINTS)) {
					Message msg = new Message();
					msg.what = RUNLINE;
					msg.obj = list;
					mHandler.sendMessage(msg);
				} else if (objectKey.equals(object + "/" + SNAPSHOT)) {
					Message msg = new Message();
					msg.what = PICTURES;
					msg.obj = list;
					mHandler.sendMessage(msg);
				}
			}
		});
	}			
	
	// 下载图片
	private void DownloadPicture(String fileName) {
		OSSFile ossFile = new OSSFile(bucketname, object + "/" + fileName);
		ossFile.downloadToInBackground(DIR + "/" + fileName, new GetFileCallback() {			
			@Override
			public void onProgress(int arg0, int arg1) {
			}
			
			@Override
			public void onFailure(OSSException ossException) {
				Log.i("myservice", ossException.getObjectKey() + " download failed! " + ossException.getMessage());
				onSuccess("", "");
			}
			
			@Override
			public void onSuccess(String objectKey, String filepath) {
				Log.i("myservice", objectKey + " dowload successed!");
				Message msg = new Message();
				msg.what = UPDATE_SUCCESS;
				mHandler.sendMessage(msg);
			}
		});
	}
	
	private void CheckUserId(String data) {
		String[] list1 = data.split("\r\n");
		String updateresult = null;
		// Log.i("myservice", "list: " + data + "  list1: " + list1[0] + " size: " + list1.length);
		for (String client : list1) {
			String id = client.substring(0, client.indexOf(" "));
			if (id.equals(userId)) {
				object = client.substring(client.indexOf(" ")+1, client.length());
				isUserRegisted = true;
				break;
			}
		}
		Log.i("myservice", "object: " + object);
		if (object == null) {
			String last = list1[list1.length - 1];
			int client = Integer.valueOf(last.substring(last.indexOf(" ")+1, last.length()))+1;
			object = String.valueOf(client);
			updateresult = data + "\r\n" + userId + " " + object;
			isUserRegisted = false;
		}
		
		if (isUserRegisted) {
			// 注册用户，需要下载该用户历史数据
			DownloadData(object + "/" + RUN);
		} else {
			// 新用户，需要更新用户文件
			UpdateClient(updateresult);
		}
	}
	
	// 将用户跑步数据存入数据库
	private void saveRundata(String data) {
		DBManager mgr = new DBManager(getApplicationContext());
		String list = new String(data);
		String[] arrayData = list.split("\r\n");
		int length = arrayData.length;
		for (int i = length-1; i >= 0; i--) {
			String[] run = arrayData[i].split(" ");			
			RunData rundata = new RunData();		
			rundata.distance = Double.valueOf(run[1]);
			rundata.hour = Integer.valueOf(run[2]);
			rundata.minutes = Integer.valueOf(run[3]);
			rundata.calorie = Double.valueOf(run[4]);
			rundata.date = getCurrentDate(Long.valueOf(run[5]));
			rundata.millis = run[5];
			mgr.insert(rundata);
		}
		mgr.closeDB();
	}
	
	// 将路线数据存入数据库
	private void savePoints(String data) {
		DBPoints dbPoints = new DBPoints(getApplicationContext());
		String[] list = data.split("\r\n");
		for (int i = list.length-1; i >= 0; i--) {
			String[] point = list[i].split("/");
			
			int id = Integer.valueOf(point[0]);
			String[] cen = point[1].split(" ");
			LatLng center = new LatLng(Double.valueOf(cen[0]), Double.valueOf(cen[1]));
			float zoomlevel = Float.valueOf(point[2]);
			
			List<LatLng> points = new ArrayList<LatLng>();
			for (int j = 3; j < point.length; j++) {
				String[] item = point[j].split(" ");
				LatLng ll = new LatLng(Double.valueOf(item[0]), Double.valueOf(item[1]));
				points.add(ll);
			}									
			
			dbPoints.insert(points, id, center, zoomlevel);
		}
		dbPoints.closeDB();
	}
	
	private void UpdateClient(String data) {
		OSSData ossData = new OSSData(bucketname, objectkey);
		try {
			ossData.setData(data.getBytes(), "raw");
			ossData.enableUploadCheckMd5sum();
			ossData.uploadInBackground(new SaveCallback() {				
				@Override
				public void onProgress(int arg0, int arg1) {
				}
				
				@Override
				public void onFailure(OSSException ossException) {
					Log.i("myservice", "updata failed! " + ossException.getMessage());
				}
				
				@Override
				public void onSuccess(String objectKey) {
					Log.i("myservice", objectKey + " update successed!");
					Message msg = new Message();
					msg.what = UPDATE_SUCCESS;
					mHandler.sendMessage(msg);
				}
			});
		} catch (OSSException e) {
			e.printStackTrace();
		}
	}
	
	private String getCurrentDate(long current) {
		String date = (String)DateUtils.formatDateTime(this, current, DateUtils.FORMAT_SHOW_DATE);
		String week = (String)DateUtils.formatDateTime(this, current, DateUtils.FORMAT_SHOW_WEEKDAY);
		String time = (String)DateUtils.formatDateTime(this, current, DateUtils.FORMAT_SHOW_TIME);
		String mCurrentTime = date + " " + week + " " + time;
		return mCurrentTime;
	}

	@Override
	public void onDestroy() {		
		super.onDestroy();
		mHandler.removeCallbacksAndMessages(null);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
