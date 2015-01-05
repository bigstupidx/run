package com.run.notify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.mbaas.oss.OSSClient;
import com.aliyun.mbaas.oss.callback.GetBytesCallback;
import com.aliyun.mbaas.oss.model.OSSException;
import com.aliyun.mbaas.oss.model.OSSException.ExceptionType;
import com.aliyun.mbaas.oss.model.OSSResponseInfo;
import com.aliyun.mbaas.oss.model.TokenGenerator;
import com.aliyun.mbaas.oss.storage.OSSData;
import com.aliyun.mbaas.oss.util.OSSToolKit;
import com.run.BaseMenu;
import com.run.MainActivity;
import com.run.R;
import com.run.notify.RefreshableView.PullToRefreshListener;
import com.run.push.TestActivity;
import com.run.record.RecordActivity;
import com.run.setting.SetActivity;
import com.run.user.UserActivity;

public class InformationActivity extends BaseMenu {

	private RefreshableView refreshView;
	private ListView listView;	
	private List<Map<String, String>> updateData;
	private ProgressBar progressbar;
	private int downloadStatus;
	private static final int DOWNLOAD_SUCCESS = 2;
	private static final int DOWNLOADING = 1;
	private static final int DOWNLOAD_FAIL = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		mDrawer.setMenuSize(600);
        mDrawer.setContentView(R.layout.activity_info);        
        mDrawer.setSlideDrawable(R.drawable.navigationbar_icon_menu_normal);
        mDrawer.setDrawerIndicatorEnabled(true);
        
		refreshView = (RefreshableView) findViewById(R.id.refresh_view);
		listView = (ListView) findViewById(R.id.lv_refresh);
		progressbar = (ProgressBar) findViewById(R.id.progress);
		
		intitOSS();
		
		getUpdateData();
		/*
		SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.info_item, 
				new String[]{"target", "title", "content", "time", "author"}, 
				new int[]{R.id.info_target, R.id.info_title, R.id.info_content, R.id.info_time, R.id.info_author});
//		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		listView.setAdapter(adapter);
		refreshView.setOnRefreshListener(new PullToRefreshListener() {			
			@Override
			public void onRefresh() {
				// 刷新后获取数据
				sleep(2000);
				//Log.i("refresh", "OnRefresh");
				refreshView.finishingRefresh();
			}
		}, 0);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(InformationActivity.this, InfoItemsActivity.class);
				String url = data.get(position).get("url");
				Log.i("refresh", "send: " + url);
				intent.putExtra("url", url);
				startActivity(intent);
			}
		});
		*/
	}		

	private void intitOSS() {
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
	}
	
	private void initData() {
		SimpleAdapter adapter = new SimpleAdapter(this, updateData, R.layout.info_item, 
				new String[]{"target", "title", "content", "time", "author"}, 
				new int[]{R.id.info_target, R.id.info_title, R.id.info_content, R.id.info_time, R.id.info_author});
//		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		listView.setAdapter(adapter);
		refreshView.setOnRefreshListener(new PullToRefreshListener() {			
			@Override
			public void onRefresh() {
				// 刷新后获取数据
				sleep(2000);
				//Log.i("refresh", "OnRefresh");
				refreshView.finishingRefresh();
			}
		}, 0);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(InformationActivity.this, InfoItemsActivity.class);
				String url = updateData.get(position).get("url");
				Log.i("refresh", "send: " + url);
				intent.putExtra("url", url);
				startActivity(intent);
			}
		});
	}

	private void getUpdateData() {
		updateData = new ArrayList<Map<String, String>>();
		
		OSSData ossdata = new OSSData(BUCKET_NAME, CLIENT_NEWS);
		ossdata.getInBackground(new GetBytesCallback() {			
			@Override
			public void onProgress(int byteWirtten, int totalSize) {
				downloadStatus = DOWNLOADING;				
				Message msg = new Message();
				msg.arg1 = byteWirtten;
				msg.arg2 = totalSize;
				handler.sendMessage(msg);
			}
			
			@Override
			public void onFailure(OSSException ossException) {
				HandlerError(ossException);
				downloadStatus = DOWNLOAD_FAIL;
			}
			
			@Override
			public void onSuccess(String objectKey, byte[] data) {
				String list = new String(data);
				String[] items = list.split("\r\n");				
				for (String item : items) {					
					String[] list1 = item.split("sk]");
					Map<String, String> newItem = new HashMap<String, String>();
					newItem.put("target", list1[0]);
					newItem.put("title", list1[1]);
					newItem.put("content", list1[2]);
					newItem.put("time", list1[3]);
					newItem.put("author", list1[4]);
					newItem.put("url", list1[5]);
//					Log.i("refresh", "t: " + list1[0] + " i: " + list1[1] + " url: " + list1[5]
//							+ " c: " + list1[2] + " m: " + list1[3] + " a: " + list1[4]);
					updateData.add(newItem);					
				}
				downloadStatus = DOWNLOAD_SUCCESS;
			}
		});
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
		ossException.printStackTrace();		// 打印栈
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {			
			//Log.i("oss", ""+(msg.arg1/msg.arg2)*100);			
			switch (downloadStatus) {
			case DOWNLOAD_SUCCESS:
				sleep(500);
				progressbar.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
				initData();
				break;
			case DOWNLOADING:
				progressbar.setProgress((msg.arg1/msg.arg2)*100);
				break;
			case DOWNLOAD_FAIL:
				Toast.makeText(InformationActivity.this, "更新出错，请重试!", Toast.LENGTH_LONG).show();
				break;
			}
		}		
	};

	@Override
	protected void onMenuItemClicked(int position) {
		Intent intent = null;
		switch (position) {
		case 0:
			intent = new Intent(InformationActivity.this, UserActivity.class);
			startActivity(intent);
			break;
		case 1:
			intent = new Intent(InformationActivity.this, MainActivity.class);
			startActivity(intent);			
			break;
		case 2:
			intent = new Intent(InformationActivity.this, RecordActivity.class);
			startActivity(intent);
			break;
		case 3:
			mDrawer.closeMenu();
			break;
		case 4:
			intent = new Intent(InformationActivity.this, TestActivity.class);
			startActivity(intent);
			break;
		case 5:
			intent = new Intent(InformationActivity.this, SetActivity.class);
			startActivity(intent);
			break;
		}
	}
	
	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
		}
	}

}
