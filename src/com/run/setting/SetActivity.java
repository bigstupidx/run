package com.run.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.run.BaseMenu;
import com.run.MainActivity;
import com.run.R;
import com.run.notify.InformationActivity;
import com.run.push.TestActivity;
import com.run.record.RecordActivity;
import com.run.user.LoginActivity;
import com.run.user.UserActivity;
import com.run.utils.Utils;

public class SetActivity extends BaseMenu implements OnClickListener {	
		
	private TextView mLogin;
	private RelativeLayout mAccount;
	private RelativeLayout mOffline;
	private RelativeLayout mUpdate;
	private RelativeLayout mFeed;
	private RelativeLayout mQuite;
	private ImageView mUserIcon;
	private ImageView mVoiceButton;
	
	private boolean isClicked = true;
	
	// 是否登录成功
	private boolean isLoginSuccess;

	private ImageView mQQ;
	private ImageView mSina;
	private ImageView mWX;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);			
		
		mDrawer.setMenuSize(600);
        mDrawer.setContentView(R.layout.activity_setting);        
        mDrawer.setSlideDrawable(R.drawable.navigationbar_icon_menu_normal);
        mDrawer.setDrawerIndicatorEnabled(true);
				
        mLogin = (TextView) findViewById(R.id.set_name);   
        mUserIcon = (ImageView) findViewById(R.id.set_icon);
        mAccount = (RelativeLayout) findViewById(R.id.setting_account);
        mOffline = (RelativeLayout) findViewById(R.id.setting_offline);
        mUpdate  = (RelativeLayout) findViewById(R.id.setting_updates);
        mFeed  = (RelativeLayout) findViewById(R.id.setting_feed);
        mQuite = (RelativeLayout) findViewById(R.id.setting_quite);             
        mVoiceButton = (ImageView) findViewById(R.id.setting_reminder);
        // 从数据库中读取数据来判定
        mVoiceButton.setBackgroundResource(R.drawable.btn_switchon);
        
        // 第三方账号登录
        mQQ = (ImageView) findViewById(R.id.setting_QQ);
        mSina = (ImageView) findViewById(R.id.setting_sina);
        mWX = (ImageView) findViewById(R.id.setting_weixin);
        
        mAccount.setOnClickListener(this);
        mOffline.setOnClickListener(this);
        mUpdate.setOnClickListener(this);
        mFeed.setOnClickListener(this);
        mQuite.setOnClickListener(this);
        mVoiceButton.setOnClickListener(this);
        mLogin.setOnClickListener(this);
        mUserIcon.setOnClickListener(this);
        
        SharedPreferences share = getSharedPreferences("userinfo", MODE_PRIVATE);
        isLoginSuccess = share.getBoolean("ret", false);
        // Log.i("QQ", "is: " + isLoginSuccess);
        // 获取用户名
		String nickname = share.getString("nickname", "点击登录");   
		// Log.i("QQ", "nicke " + nickname);
		mLogin.setText(nickname);
    	if (isLoginSuccess) {
    		// Log.i("QQ", "picurl " + share.getString("figureurl", ""));
    		Bitmap bitmap = Utils.getUserIcon(share.getString("figureurl", ""));
    		bitmap = Utils.createCircleImage(bitmap, 200);
            mUserIcon.setBackground(new BitmapDrawable(getResources(), bitmap));
            mQQ.setBackgroundResource(R.drawable.icon_qq_login);
    	} else {
    		mUserIcon.setBackgroundResource(R.drawable.bg_no_head);
    	}
        // bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user1);
        // 将图片进行压缩   需要根据获取的图片来选择是否要进行压缩，还是直接取图片宽度
        // bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false);
        // bitmap = createCircleImage(bitmap, 200);
        // mUserIcon.setBackground(new BitmapDrawable(getResources(), bitmap));
	}
	
	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.setting_account:
			//Log.i("set", 1+"");
			return;
		case R.id.setting_offline:
			//Log.i("set", 2+"");
			intent = new Intent(SetActivity.this, OffLineActivity.class);
			startActivity(intent);
			return;
		case R.id.setting_updates:
			//Log.i("set", 3+"");
			return;
		case R.id.setting_feed:
			//Log.i("set", 4+"");
			return;
		case R.id.setting_quite:
			//Log.i("set", 5+"");			
			mQQ.setBackgroundResource(R.drawable.icon_qq);
			mWX.setBackgroundResource(R.drawable.icon_weixin);
			mSina.setBackgroundResource(R.drawable.icon_weibo);
			mLogin.setText("点击登录");
			mUserIcon.setBackgroundResource(R.drawable.bg_no_head);			
			SharedPreferences.Editor editor = getSharedPreferences("userinfo", MODE_PRIVATE).edit();
			editor.putString("nickname", "点击登录");			
			editor.putBoolean("ret", false);
			editor.commit();
			onResume();
			return;
		case R.id.setting_reminder:
			if(isClicked) {
				mVoiceButton.setBackgroundResource(R.drawable.btn_switchon);				
			} else {
				mVoiceButton.setBackgroundResource(R.drawable.btn_switchoff);
			}
			isClicked = !isClicked;
			// 存储当前用户选择的状态
			return;
		case R.id.set_name:
		case R.id.set_icon:
			if (!isLoginSuccess) {		
				intent = new Intent(SetActivity.this, LoginActivity.class);
				startActivity(intent);								
			}			
			return;
		}
	}

	@Override
	protected void onMenuItemClicked(int position) {
		Intent intent = null;
		switch (position) {
		case 0:
			intent = new Intent(SetActivity.this, UserActivity.class);
			startActivity(intent);
			break;
		case 1:
			intent = new Intent(SetActivity.this, MainActivity.class);
			startActivity(intent);
			break;
		case 2:
			intent = new Intent(SetActivity.this, RecordActivity.class);
			startActivity(intent);		
			break;
		case 3:
			intent = new Intent(SetActivity.this, InformationActivity.class);
			startActivity(intent);
			break;
		case 4:
			intent = new Intent(SetActivity.this, TestActivity.class);
			startActivity(intent);
		case 5:
			mDrawer.closeMenu();
			break;
		}
	}

	@Override
	protected void onResume() {		
		super.onResume();
		// Log.i("QQ", "onResume");
		// 从上一界面返回时，刷新界面
		onCreate(null);
	}
}
