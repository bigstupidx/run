package com.run;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
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
import com.run.record.RecordActivity;
import com.run.setting.SetActivity;
import com.run.user.UserActivity;

public class RunInfo extends BaseMenu implements OnClickListener {

	private Button mUpdate;
	private Button mDownload;
	private TextView mProgress;
	private TextView mInfo;
	// 记录点击back的时间
	private long clickTime = 0;
	
	// 实际使用中，AK/SK不应明文保存在代码中
	private final static String accessKey = "NTP8U7r0o5mCmPQq";
	private final static String screctKey = "sYiCD5mul0duG1gzxpeeuLqiSx7U9H";	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityCollector.addActivity(this);
		
		mDrawer.setMenuSize(600);
		mDrawer.setContentView(R.layout.activity_testoss);
		mDrawer.setSlideDrawable(R.drawable.navigationbar_icon_menu_normal);
		mDrawer.setDrawerIndicatorEnabled(true);
		
		mUpdate = (Button) findViewById(R.id.update);
		mDownload = (Button) findViewById(R.id.download);
		mProgress = (TextView) findViewById(R.id.progress);
		mInfo = (TextView) findViewById(R.id.response);			
		
		mUpdate.setOnClickListener(this);
		mDownload.setOnClickListener(this);
		
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
	}
	
	@Override
	public void onClick(View v) {
		OSSFile ossFile = null;
		OSSData ossData = null;
		switch (v.getId()) {
		case R.id.update:
			/*
			OSSMeta meta = OSSClient.newOSSMeta("runne", "client1");
			meta.getMetaInBackground(new GetMetaCallback() {				
				@Override
				public void onFailure(OSSException ossException) {
					HandlerError(ossException);
				}
				
				@Override
				public void onSuccess(String objectKey, List<BasicNameValuePair> meta) {
					Log.i("oss", "get objectkey: " + objectKey);
					for (BasicNameValuePair element : meta) {
						Log.i("oss", "name: " + element.getName() + " key: " + element.getValue());
					}
				}
			});
			
			ossData = new OSSData("runne", "id.txt");			
			String data = "sk/client" + "\r\n" + "sk0/client0" + "\r\n" + "sk1/client1" + "\r\n" + "sk2/client2";		
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
					}
					
					@Override
					public void onSuccess(String objectKey) {
						Log.i("oss", "上传数据成功!  objectKey: " + objectKey);
					}
				});
			} catch (OSSException e) {
			}
			*/
			// http://runne.oss-cn-hangzhou.aliyuncs.com/1/1.png		
			
			OSSFile ofile = new OSSFile("runne", "2/points.txt");
			ofile.setUploadFilePath(DIR + "/points.txt", "raw");
			ofile.enableUploadCheckMd5sum();								
			ofile.uploadInBackground(new SaveCallback() {				
				@Override
				public void onProgress(int arg0, int arg1) {
				}
				
				@Override
				public void onFailure(OSSException ossException) {
					HandlerError(ossException);
				}
				
				@Override
				public void onSuccess(String objectkey) {
					Log.i("oss", "上传数据成功!  objectKey: " + objectkey);
				}
			});					
			
			break;
		case R.id.download:						
			ossData = new OSSData("runne", "id.txt");
			ossData.getInBackground(new GetBytesCallback() {				
				@Override
				public void onProgress(int byteWirtten, int totalSize) {
				}
				
				@Override
				public void onFailure(OSSException ossException) {
					HandlerError(ossException);
				}
				
				@Override
				public void onSuccess(String objectKey, byte[] data) {
					Log.i("oss", "key: " + objectKey);
					String list = new String(data);
					String[] list1 = list.split("\r\n");
					for (String d : list1) {
						String name = d.substring(0, d.indexOf("/"));
						String file = d.substring(d.indexOf("/")+1, d.length());
						Log.i("oss", "name: " + name + " file: " + file);
					}					
				}
			});
			/*
			ossFile = new OSSFile("runne", "client1/snapshot.png");		
			ossFile.downloadToInBackground(DIR+"/1.png", new GetFileCallback() {				
				@Override
				public void onProgress(int byteCount, int totalSize) {
//					mProgress.setText("文件下载进度(" +
//							String.format("%2.2f", (byteCount/totalSize)*100)  + "%)");
					Log.i("oss", "文件下载进度(" + (byteCount/totalSize)*100  + "%)");
				}
				
				@Override
				public void onFailure(OSSException ossException) {
					HandlerError(ossException);
				}
				
				@Override
				public void onSuccess(String objectKey, String filePath) {					
					String info = "文件成功下载到" + filePath + "目录" 
								+ "\n" + " objectkey: " + objectKey;
					mInfo.setText(info);
					
					Log.i("oss", "文件成功下载到" + filePath + "目录" 
								+ "\n" + " objectkey: " + objectKey);
				}		
			});
			*/
			break;
		}
	}
	
	private void HandlerError(OSSException ossException) {
		Log.i("oss", "failed:");
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
	protected void onMenuItemClicked(int position) {
		Intent intent = null;
		switch (position) {
		case 0:
			intent = new Intent(RunInfo.this, UserActivity.class);
			startActivity(intent);
			break;
		case 1:
			intent = new Intent(RunInfo.this, MainActivity.class);
			startActivity(intent);
			break;
		case 2:
			intent = new Intent(RunInfo.this, RecordActivity.class);
			startActivity(intent);
			break;
		case 3:
			mDrawer.closeMenu();
			break;
		case 5:
			intent = new Intent(RunInfo.this, SetActivity.class);
			startActivity(intent);
			break;
		}
	}

	@Override
	public void onBackPressed() {		
		// super.onBackPressed();
		if((System.currentTimeMillis() - clickTime) > 2000) {
			Toast.makeText(RunInfo.this, "再按一次退出", Toast.LENGTH_LONG).show();
			clickTime = System.currentTimeMillis();
		} else {
			ActivityCollector.finishAll();
		}
	}

	@Override
	protected void onDestroy() {		
		super.onDestroy();
		ActivityCollector.removeActivity(this);
	}	

}
