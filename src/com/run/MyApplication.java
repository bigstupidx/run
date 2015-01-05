package com.run;

import android.app.Application;
import cn.jpush.android.api.JPushInterface;

import com.baidu.mapapi.SDKInitializer;

public class MyApplication extends Application {
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		// 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
		SDKInitializer.initialize(this);
		
		JPushInterface.setDebugMode(true);		// 设置开启日志,发布时请关闭日志
		JPushInterface.init(this);			// 初始化 JPush
	}

}
