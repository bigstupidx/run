package com.run;

import android.app.ActionBar;
import android.os.Bundle;

public class BaseActivity extends MyActivity {

	protected ActionBar actionbar; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		ActivityCollector.addActivity(this);
		
		actionbar = getActionBar();
		// 左上角图标的左边加上一个返回的图标
		actionbar.setDisplayHomeAsUpEnabled(false);
		// 左上角图标是否显示
		actionbar.setDisplayShowHomeEnabled(true);
		// 左上角返回按钮功能
		actionbar.setHomeButtonEnabled(true);
		
	}

	@Override
	protected void onDestroy() {		
		super.onDestroy();
		ActivityCollector.removeActivity(this);
	}
	

}
