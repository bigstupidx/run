package com.run;

import android.app.Activity;
import android.os.Environment;

public class MyActivity extends Activity {
	
	protected static final String mAppid = "1103545234";
	protected static final String INTENT_KEY = "rundata";
	protected static final String INTENT_ID = "id";	
	protected static final String INTENT_IMAGE = "image";
	protected static final String INTENT_PIC = "name";
	protected static final String INTENT_SHARE = "share";
	protected static final String DIR = Environment.getExternalStorageDirectory() + "/run";
	
	// 数据上传
	protected static final String BUCKET_NAME = "runne";
	protected static final String CLIENT_TABLE = "default/id.txt";
	protected static final String CLIENT_NEWS = "notification/news.txt";
	protected static final String RUN = "rundata.txt";
	protected static final String POINTS = "points.txt";	
	protected static final String SNAPSHOT = "pictures.txt";
	protected static final String PIC_URL = "http://runne.oss-cn-hangzhou.aliyuncs.com/";
	protected static final String DEFAULT_URL = "http://runne.oss-cn-hangzhou.aliyuncs.com/default/1.jpg";
	
	// 阿里云，实际使用中，AK/SK不应明文保存在代码中
	protected final static String accessKey = "NTP8U7r0o5mCmPQq";
	protected final static String screctKey = "sYiCD5mul0duG1gzxpeeuLqiSx7U9H";
	
	// QQ登录信息
	protected final static String QQLOGIN = "qqlogin";
	protected final static String OPENID = "openid";
	
	// 用户信息
	protected static final String USER_ICON = DIR + "/usricon.png";
	
}
