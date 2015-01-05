package com.run.user;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.run.MainActivity;
import com.run.R;

public class GuideActivity extends Activity implements OnPageChangeListener {	

	private ViewPager viewpager;
	
	private ArrayList<View> views; // 定义一个ArrayList来存放View
	
	private ViewPageAdapter viewPagerAdapter;  // 实例化ViewPager适配器
	
	private Handler handler;
	
	private int counter = 0;	// 记数器
	
	private Timer timer = null;
	
	private TimerTask task = null;	
	
	// 引导界面图片
	private static final int[] pics = {R.drawable.bg_guide1, 
		R.drawable.bg_guide2, R.drawable.bg_guide3, R.drawable.bg_guide4};

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		ActionBar actionbar = getActionBar();
		actionbar.hide();
		
		// 判断是否是第一次登录
		isFirstLogin();
		
		// 设定定时器
		initTimer();
				
		viewpager = (ViewPager) findViewById(R.id.guide);
		
		views = new ArrayList<View>();
		
		viewPagerAdapter = new ViewPageAdapter(views);
		
		// 定义一个布局并设置参数
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		for (int i = 0; i < pics.length; i++) {
			ImageView image = new ImageView(this);
			image.setLayoutParams(params);
			// image.setImageResource(pics[i]);
			image.setBackgroundResource(pics[i]);
			views.add(image);
		}
		
		viewpager.setAdapter(viewPagerAdapter);
		viewpager.setOnPageChangeListener(this);
		
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	
	}

	@Override
	public void onPageSelected(int position) {		
		if (position == views.size()-1) {
			// 开启定时器
			startTimer();
		}
	}
	
	private void startTimer() {
		if (timer == null) {
			if (task == null) {
				task = new TimerTask() {					
					@Override
					public void run() {
						Message msg = new Message();
						msg.what = 1;
						handler.sendMessage(msg);
					}
				};
			}
			timer = new Timer(true);
			timer.schedule(task, 100, 1000);
		}
	}
	
	@SuppressLint("HandlerLeak")
	private void initTimer() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					counter++;
					if (counter == 2)
						finish();
					break;
				}
			}			
		};
	}
	
	private void isFirstLogin() {
		SharedPreferences share = getSharedPreferences("login", MODE_PRIVATE);
		// 判断是不是首次登录
		if (share.getBoolean("firstlogin", true)) {			
			SharedPreferences.Editor editor = share.edit();
			// 将登录标志位设置为false，下次登录时不在显示首次登录界面
			editor.putBoolean("firstlogin", false);
			editor.commit();
			Intent intent = new Intent(GuideActivity.this, LoginActivity.class);
			startActivity(intent);
		}
		Intent intent = new Intent(GuideActivity.this, MainActivity.class);
		startActivity(intent);
	}

}
