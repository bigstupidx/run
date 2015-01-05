package com.run.user;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.aliyun.mbaas.oss.OSSClient;
import com.aliyun.mbaas.oss.callback.GetBytesCallback;
import com.aliyun.mbaas.oss.model.OSSException;
import com.aliyun.mbaas.oss.model.TokenGenerator;
import com.aliyun.mbaas.oss.storage.OSSData;
import com.aliyun.mbaas.oss.util.OSSToolKit;
import com.run.BaseActivity;
import com.run.R;
import com.run.utils.Utils;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQAuth;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class LoginActivity extends BaseActivity implements OnClickListener {
	
	private QQAuth mQQAuth;
	private Tencent mTencent;
	
	private ImageButton btnWX;
	private ImageButton btnPyq;
	private ImageButton btnQQ;
	private ImageButton btnSina;
	private UserInfo mInfo;
	private boolean isLogin = false;
	
	private Timer timer = null;
	private TimerTask task = null;
	
	private String object = null;
//	private boolean isUserRegisted = false;  // 用户之前是否有数据

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		mQQAuth = QQAuth.createInstance(mAppid, getApplicationContext());
		mTencent = Tencent.createInstance(mAppid, LoginActivity.this);			
		
		btnWX = (ImageButton) findViewById(R.id.share_weixin);
		btnPyq = (ImageButton) findViewById(R.id.share_pyq);
		btnQQ = (ImageButton) findViewById(R.id.share_qq);
		btnSina = (ImageButton) findViewById(R.id.share_sina);
		
		btnWX.setOnClickListener(this);
		btnPyq.setOnClickListener(this);
		btnQQ.setOnClickListener(this);
		btnSina.setOnClickListener(this);
		
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.share_weixin:
			
			break;
		case R.id.share_pyq:
			
			break;
		case R.id.share_qq:
			onClickQQLogin();	
			// 开启定时器，当头像下载完成时返回
			beginTimer();				
			break;
		case R.id.share_sina:
			
			break;
		}
	}

	private void onClickQQLogin() {
		if (!mQQAuth.isSessionValid()) {
			IUiListener listener = new BaseUiListener() {
				@Override
				protected void doComplete(JSONObject values) {					
					saveUserInfo();					
				}
			};
			mTencent.loginWithOEM(this, "all", listener, "10000144", "10000144", "xxxx");
			// mTencent.login(this, "all", listener);
		} else {
			mTencent.logout(this);
		}
	}
	
	private void saveUserInfo() {
		if (mQQAuth != null && mQQAuth.isSessionValid()) {
			IUiListener listener = new IUiListener() {
				@Override
				public void onError(UiError arg0) {
				}
				
				@Override
				public void onComplete(final Object response) {
					// Log.i("QQ", response.toString());			
					Message msg = new Message();
					msg.obj = response;
					msg.what = 0;
					mHandler.sendMessage(msg);					
					new Thread() {
						@Override
						public void run() {
							JSONObject json = (JSONObject)response;
							if (json.has("figureurl")) {
								String imageString = null;
								try {
									// Log.i("QQ", json.getString("figureurl_qq_2"));
									String url = json.getString("figureurl_qq_2");
									if (url.isEmpty() || url == "") {
										url = json.getString("figureurl_qq_1");										
									}
									imageString = Utils.getbitmap(url);
								} catch (JSONException e) {
								}
								Message msg = new Message();
								msg.obj = imageString;
								msg.what = 1;
								mHandler.sendMessage(msg);
							}
						}						
					}.start();
				}
				
				@Override
				public void onCancel() {
				}
			};
			
			mInfo = new UserInfo(this, mQQAuth.getQQToken());
			mInfo.getUserInfo(listener);
		}
	}
	
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			SharedPreferences.Editor editor = 
					getSharedPreferences("userinfo", MODE_PRIVATE).edit();
			if (msg.what == 0) {		
				JSONObject response = (JSONObject) msg.obj;
				if (response.has("nickname")) {
					try {
						// Log.i("QQ", response.getString("nickname"));
						editor.putString("nickname", response.getString("nickname"));
						saveQQInfo();
					} catch (JSONException e) {
					}		
				}
			} else if (msg.what == 1) {
				String url = (String) msg.obj;
				// Log.i("QQ", url);
				editor.putString("figureurl", url);
				isLogin = true;
				editor.putBoolean("ret", isLogin);
			}
			editor.commit();
			// Log.i("QQ", "commited");
		}		
	};
	
	private void saveQQInfo() {
//		Log.i("QQ", "Login  openid: " + mTencent.getOpenId() + " access: " + mTencent.getAccessToken() + 
//				" expires_in: " + mTencent.getExpiresIn());
		SharedPreferences pref = getSharedPreferences(QQLOGIN, MODE_PRIVATE);		
		
		String openid = pref.getString(OPENID, "null");
		String token = pref.getString("access_token", "null");
		String expires = pref.getString("expires_in", "null");
		
		// 在首次登录成功后，将返回的openid、access_token、expires_in三个参数保存在本地
		if (openid.equals("null") || token.equals("null") || expires.equals("null")) {
			SharedPreferences.Editor ed = pref.edit();
			openid = mTencent.getOpenId();
			ed.putString(OPENID, openid);
			ed.putString("access_token", mTencent.getAccessToken());
			// expires_in存储之前需要进行计算获得token失效的日期
			expires = String.valueOf(System.currentTimeMillis() + mTencent.getExpiresIn()*1000);
			ed.putString("expires_in", expires);
			ed.commit();
		} else {
			mTencent.setOpenId(openid);
			// （上一步保存的token失效日期-当前系统时间）/1000。
			// 这里计算出的结果是当前保存的token的有效时间，如果结果小于或等于0，表示token已经过期，应该提示用户重新走登录流程
			String exp = String.valueOf((Long.parseLong(expires) - System.currentTimeMillis())/1000);
			mTencent.setAccessToken(token, exp);
		}
		
		// 根据openid来查找用户数据
		DownLoadDataFromService(openid);
	}
	
	private void beginTimer() {
		if (timer == null) {
			if (task == null) {
				task = new TimerTask() {					
					@Override
					public void run() {
						// Log.i("QQ", "login: " + isLogin);
						if (isLogin) {
							// Log.i("QQ", "end timer");
							task.cancel();
							task = null;
							timer.cancel();
							timer.purge();
							timer = null;
							// 开启后台下载服务
							startDonwloadService();
							finish();
						}
					}
				};
			}
			timer = new Timer(true);
			timer.schedule(task, 100, 1000);			
		}
	}	
	
	private void DownLoadDataFromService(final String openid) {			
		OSSData ossData = new OSSData(BUCKET_NAME, CLIENT_TABLE);
		ossData.getInBackground(new GetBytesCallback() {			
			@Override
			public void onProgress(int arg0, int arg1) {
			}
			
			@Override
			public void onFailure(OSSException arg0) {
			}
			
			@Override
			public void onSuccess(String objectKey, byte[] data) {
				String list = new String(data);
				String[] list1 = list.split("\r\n");
				// Log.i("oss", "list: " + list + "  list1: " + list1[0] + " size: " + list1.length);
				for (String client : list1) {
					String id = client.substring(0, client.indexOf(" "));
					if (id.equals(openid)) {
						object = client.substring(client.indexOf(" ")+1, client.length());
//						isUserRegisted = true;
					}
				}
				Log.i("oss", "object: " + object);
				if (object == null) {
					String last = list1[list1.length - 1];
					int client = Integer.valueOf(last.substring(last.indexOf(" ")+1, last.length()))+1;
					object = String.valueOf(client);
//					isUserRegisted = false;
				}
			}
		});
	}

	private class BaseUiListener implements IUiListener {
		@Override
		public void onCancel() {
			Toast.makeText(getApplicationContext(), "取消登录", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onComplete(Object response) {			
			Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
			// Log.i("QQ", response.toString());
			doComplete((JSONObject)response);
		}
		
		protected void doComplete(JSONObject values) {			 
		}

		@Override
		public void onError(UiError err) {			
			Log.i("QQ", "OnError: " + err.toString());
		}		
	}
	
	// 开启后台下载服务
	private void startDonwloadService() {
		// 开启后台下载数据服务
		Log.i("myservice", "start service");
		Intent startIntent = new Intent(this, DownloadService.class);
		startIntent.putExtra("bucket", BUCKET_NAME);
		startIntent.putExtra("objectkey", CLIENT_TABLE);
		startService(startIntent);
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
