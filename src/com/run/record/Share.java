package com.run.record;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.aliyun.mbaas.oss.OSSClient;
import com.aliyun.mbaas.oss.callback.GetBytesCallback;
import com.aliyun.mbaas.oss.callback.SaveCallback;
import com.aliyun.mbaas.oss.model.OSSException;
import com.aliyun.mbaas.oss.model.OSSException.ExceptionType;
import com.aliyun.mbaas.oss.model.OSSResponseInfo;
import com.aliyun.mbaas.oss.model.TokenGenerator;
import com.aliyun.mbaas.oss.storage.OSSData;
import com.aliyun.mbaas.oss.storage.OSSFile;
import com.aliyun.mbaas.oss.util.OSSToolKit;
import com.baidu.mapapi.model.LatLng;
import com.run.BaseActivity;
import com.run.R;
import com.run.db.DBManager;
import com.run.db.DBPoints;
import com.run.utils.RunData;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class Share extends BaseActivity implements OnClickListener {
		
	private ImageView image;
	private ImageButton mshareWX;
	private ImageButton msharePyq;
	private ImageButton mshareQQ;
	private ImageButton mshareSina;	
	
	private String shareText;
	private String picPath;				// 路线结果截图路径
	private String picName;				// 路线结果截图
	private String imageName = null;	// 自拍照片
	
	private Tencent mTencent;
	
	// 上传到服务器的数据
	private String runresult = null;
	private String pointresult = null;
	private String picturename = null;
	private String object = null;
	private String updateresult = null;	
	private Handler handler = null;
	
	// 更新数据顺序
	// private final static int UPDATE_ID = 1;
	private final static int UPDATE_RUN = 2;
	private final static int UPDATE_POINT = 3;
	private final static int UPDATE_PICTXT = 4;
	private final static int UPDATE_PIC = 5;
	
	// 是否更行更新
	private boolean isUpdate = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		
		// 初始化
		mTencent = Tencent.createInstance(mAppid, Share.this);
		
		// 初始化阿里云
		OSSClient.setApplicationContext(getApplicationContext());
		// 指明你的bucket所放在数据oss-cn-hangzhou.aliyuncs.com
		OSSClient.setOSSHostId("oss-cn-hangzhou.aliyuncs.com");
		OSSClient.setTokenGenerator(new TokenGenerator() {			
			@Override
			public String generateToken(String httpMethod, String md5, String type, String date,
					String ossHeaders, String resource) {
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
		
		image = (ImageView) findViewById(R.id.share_pictrue);
		// 获取图片名称
		Intent intent = getIntent();
		picName = intent.getStringExtra(INTENT_PIC);
		imageName = intent.getStringExtra(INTENT_IMAGE);
		shareText = intent.getStringExtra(INTENT_SHARE);
		
		picPath = DIR + "/" + picName;
		Bitmap bm = getLocalBitmap(picPath);		
		image.setImageBitmap(bm);		
		
		mshareWX = (ImageButton) findViewById(R.id.share_weixin);
		msharePyq = (ImageButton) findViewById(R.id.share_pyq);
		mshareQQ = (ImageButton) findViewById(R.id.share_qq);
		mshareSina = (ImageButton) findViewById(R.id.share_sina);
		
		mshareWX.setOnClickListener(this);
		msharePyq.setOnClickListener(this);
		mshareQQ.setOnClickListener(this);
		mshareSina.setOnClickListener(this);
		
		// 更新上传数据
		getUpdateData();
		
		startHandler();
	}	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.share_weixin:
			
			break;
		case R.id.share_pyq:
			
			break;
		case R.id.share_qq:
			// 当获取了用户名才能更新数据
			if (isUpdate) {
				if (object == null) {
					updateInfo(CLIENT_TABLE, updateresult);
				} else {
					updateInfo(object + "/" + RUN, runresult);
				}			
			}
			final Bundle params = new Bundle();
			params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
			params.putString(QzoneShare.SHARE_TO_QQ_TITLE, shareText);	
			params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, "http://www.qq.com");
			ArrayList<String> imageUrls = new ArrayList<String>();
			imageUrls.add(picPath);
			params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);			
			doShareToQzone(params);
			break;
		case R.id.share_sina:
			
			break;
		}
	}
	
	/**
     * 用异步方式启动分享
     * @param params
     */
	private void doShareToQzone(final Bundle params) {
		new Thread(new Runnable() {			
			@Override
			public void run() {
				mTencent.shareToQzone(Share.this, params, new IUiListener() {					
					@Override
					public void onError(UiError error) {
						// Toast.makeText(activity, "分享出错", Toast.LENGTH_SHORT).show();
						Log.i("oss", "分享出错  " + error.errorCode + " " + error.errorMessage + " "
								+ error.errorDetail);
					}
					
					@Override
					public void onComplete(Object response) {
						// Toast.makeText(activity, response.toString(), Toast.LENGTH_SHORT).show();
						Toast.makeText(Share.this, "分享成功", Toast.LENGTH_SHORT).show();
						
					}
					
					@Override
					public void onCancel() {
						Toast.makeText(Share.this, "取消分享", Toast.LENGTH_SHORT).show();
					}
				});
			}
		}).start();		
	}		

	private Bitmap getLocalBitmap(String path) {
		try {
			// 将文件转换成bitmap格式
			FileInputStream inStream = new FileInputStream(path);
			return BitmapFactory.decodeStream(inStream);
		} catch (FileNotFoundException e) {			
			return null;
		}		
	}
	
	private void getUpdateData() {
		// 获取文件夹下面的所有图片		
		File file = new File(DIR);
		File[] fileList = file.listFiles();
		for (File f : fileList) {
			String filename = f.getName();
			String filesuffix = filename.substring(filename.indexOf(".")+1, 
					filename.length()).toLowerCase(Locale.getDefault());
			if (filesuffix.equals("jpg") && filename.indexOf("_") > 0) {
				if (picturename == null)
					picturename = filename + "\r\n";
				else
					picturename += filename + "\r\n";
			}
		}
		
		DBManager mgr = new DBManager(this);
		List<Integer> ids = mgr.queryId();
		int i = 0;
		for(RunData data : mgr.query()) {
			// 上传到服务器的数据
			if (runresult == null) {
				runresult = ids.get(i++) + " " + data.distance + " " + data.hour + " " + data.minutes
						+ " " + data.calorie + " " + data.millis + "\r\n";
			} else {
				runresult += ids.get(i++) + " " + data.distance + " " + data.hour + " " + data.minutes
						+ " " + data.calorie + " " + data.millis + "\r\n";
			}
		}
		
		DBPoints dbPoints = new DBPoints(this);		
		// 上传到服务器的路线数据
		if (ids.size() > 0 && !ids.isEmpty()) {
			for (int id : ids) {
				List<LatLng> points = dbPoints.query(id);
				if (pointresult == null) {
					pointresult = id + "/" + dbPoints.queryTheCenter(id) + "/"
								+ dbPoints.queryTheZoomlevel(id) + "/";
				} else {
					pointresult += id + "/" + dbPoints.queryTheCenter(id) + "/"
								+ dbPoints.queryTheZoomlevel(id) + "/";
				}
				for (LatLng ll : points) {
					pointresult += ll.latitude + " " + ll.longitude + "/";
				}
				
				pointresult += "\r\n";
			}
//			Log.i("oss", "run: " + runresult);
//			Log.i("oss", "point: " + pointresult);
			
			SharedPreferences share = getSharedPreferences(QQLOGIN, MODE_PRIVATE);		
			final String openid = share.getString(OPENID, "null");
			// Log.i("oss", "openid: " + openid);
			if (openid == null) {
				Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
				// 重新写一个登录界面
			}
			OSSData ossdata = new OSSData(BUCKET_NAME, CLIENT_TABLE);
			ossdata.getInBackground(new GetBytesCallback() {			
				@Override
				public void onProgress(int arg0, int arg1) {				
				}
				
				@Override
				public void onFailure(OSSException ossException) {
					Log.i("oss", "Download txt failed");
					isUpdate = false;
					HandlerError(ossException);
				}
				
				@Override
				public void onSuccess(String objectKey, byte[] data) {
					Log.i("oss", "On Download Success");
					isUpdate = true;
					String list = new String(data);
					String[] list1 = list.split("\r\n");
					// Log.i("oss", "list: " + list + "  list1: " + list1[0] + " size: " + list1.length);
					for (String client : list1) {						
						String id = client.substring(0, client.indexOf(" "));
						if (id.equals(openid)) {
							object = client.substring(client.indexOf(" ")+1, client.length());
						}
					}					
					if (object == null) {
						String last = list1[list1.length - 1];
						int client = Integer.valueOf(last.substring(last.indexOf(" ")+1, last.length()))+1;
						object = String.valueOf(client);
						updateresult = list + "\r\n" + openid + " " + object;
					}
					// Log.i("oss", "on " + updateresult);
				}
			});			
		}
	}
	
	// 上传数据
	private void updateInfo(String objectkey, String data) {			
		OSSData ossData = new OSSData(BUCKET_NAME, objectkey);	
		try {
			ossData.setData(data.getBytes(), "raw");
			ossData.enableUploadCheckMd5sum();
			ossData.uploadInBackground(new SaveCallback() {				
				@Override
				public void onProgress(int arg0, int arg1) {
				}
				
				@Override
				public void onFailure(OSSException ossException) {
					HandlerError(ossException);
					Log.i("oss", "上传出错!");
					onSuccess(ossException.getObjectKey());
				}
				
				@Override
				public void onSuccess(String objectKey) {
					Log.i("oss", "上传成功   " + objectKey);
					if (objectKey.equals(BUCKET_NAME)) {
						Message msg = new Message();
						msg.what = UPDATE_RUN;
						handler.sendMessage(msg);
					} else if (objectKey.equals(object + "/" + RUN)) {
						Message msg = new Message();
						msg.what = UPDATE_POINT;
						handler.sendMessage(msg);
					} else if (objectKey.equals(object + "/" + POINTS)) {
						Message msg = new Message();
						msg.what = UPDATE_PICTXT;
						handler.sendMessage(msg);
					} else if (objectKey.equals(object + "/" + SNAPSHOT)) {
						Message msg = new Message();
						msg.what = UPDATE_PIC;
						handler.sendMessage(msg);
					}
				}
			});
		} catch (OSSException e) {			
		}
	}
	
	// 上传文件
	private void updateFile(String objectkey, String filepath) {		
		OSSFile ossFile = new OSSFile(BUCKET_NAME, objectkey);
		ossFile.setUploadFilePath(filepath, "raw");
		ossFile.enableUploadCheckMd5sum();
		ossFile.uploadInBackground(new SaveCallback() {			
			@Override
			public void onProgress(int arg0, int arg1) {
			}
			
			@Override
			public void onFailure(OSSException ossException) {
				HandlerError(ossException);				
				Log.i("oss", "上传出错!");
				// onSuccess(ossException.getObjectKey());
			}
			
			@Override
			public void onSuccess(String objectKey) {
				Log.i("oss", "上传成功   " + objectKey);				
			}
		});
	}
	
	@SuppressLint("HandlerLeak")
	private void startHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UPDATE_RUN:					
					updateInfo(object + "/" + RUN, runresult);
					break;
				case UPDATE_POINT:					
					updateInfo(object + "/" + POINTS, pointresult);
					break;
				case UPDATE_PICTXT:
					// 将自拍照文件名上传(如果没有找到图片文件，图片也不需要上传)
					if (picturename != null)
						updateInfo(object + "/" + SNAPSHOT, picturename);
					break;
				case UPDATE_PIC:
					// 有自拍照时上传自拍照					
					// 图片上传的名字跟本地的名字一样
					if (imageName != null)
						updateFile(object + "/" + imageName, DIR + "/" + imageName);
					break;
				}
			}			
		};
	}
	
	private void HandlerError(OSSException ossException) {		
		//处理前务必进行异常类别的判断，不同类别处理方法不同
		if (ossException.getExceptionType() == ExceptionType.OSS_EXCEPTION) {
			String objectKey = ossException.getObjectKey(); // 获取该任务对应的ObjectKey
			OSSResponseInfo resp = ossException.getOssRespInfo(); // 获取根据OSS响应的内容构造的数据结构
			int statusCode = resp.getStatusCode(); // OSS响应的http状态码
			// org.w3c.dom.Document dom = resp.getResponseInfoDom(); // 根据OSS响应内容解析得到的文档结构，你可以通过它获取更详细的信息
			String errorCode = resp.getCode(); // OSS反馈的错误码
			String requestId = resp.getRequestId(); // 该次任务的请求ID
			String hostId = resp.getHostId(); // 该次任务请求的主机
			String message = resp.getMessage(); // OSS反馈的错误信息
			String info = ossException.toString(); // 本次异常的信息汇总
			ossException.printStackTrace(); // 打印栈
			Log.i("oss", objectKey+"  "+statusCode+"  "+errorCode+"  "+requestId
					+"  "+hostId+"  "+message+"  "+info);						
		}
		Log.i("oss", "error: " + ossException.getMessage());
		ossException.printStackTrace(); // 打印栈
	}
	
	@Override
	protected void onDestroy() {		
		super.onDestroy();		
		handler.removeCallbacksAndMessages(null);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return true;
	}
}
