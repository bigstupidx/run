package com.run;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.run.db.DBManager;
import com.run.db.DBPoints;
import com.run.notify.InformationActivity;
import com.run.push.TestActivity;
import com.run.record.RecordActivity;
import com.run.record.RecordDetail;
import com.run.setting.SetActivity;
import com.run.unlock.SliderLayout;
import com.run.user.UserActivity;
import com.run.utils.RunData;
import com.run.weather.WeatherActivity;

@SuppressWarnings("deprecation")
public class MainActivity extends BaseMenu {
	private TextView tvTime;
	private Button btnStart;
	private TextView tvSpeed;
	private TextView tvDistance;
	private TextView tvEnergy;
	private TextView tvGPS;
	private LinearLayout btnLayout;
	private TextView btnFinish;
	private TextView btnPause;
	
	private long mCount = 0;
	private int mHour = 0;
	private int mMin = 0;
	private int mSec = 0;
	private Handler handler = null;
	private Timer timer = null;
	private TimerTask task = null;
	private Message msg = null;
	
	private boolean isRunningFlag = false;
	private LatLng point = null;
	private double totalDistance = 0.0;
	private double totalCalorie = 0.0;
	
	// 定位相关
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationClient mLocClient;
		
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	
	boolean isFirstLoc = true;  // 是否是第一次定位
	// 记数器(三秒钟进行一次计算距离)
	private int timeCounter = 0;
	
	// 用于存放绘制路线的点
	private List<LatLng> points;	
	private static float ZOOMLEVEL_RUN = 16;	
	// zoom level 16 时屏幕能显示的最长距离    需要屏幕进行适配
	private static float ZOOM_DISTANCE = 1750;
	// 根据路线来调整地图显示范围
	private LatLng mTopPoint = null;
	private LatLng mBottomPoint = null;
	private LatLng mLeftPoint = null;
	private LatLng mRightPoint = null;

	public final static int MSG_TIMER_BEGIN = 1;
	public final static int MSG_LOCK_SUCESS = 2;	// 解锁成功向主界面发送的消息
	private SliderLayout slider_layout;	
	
	// 开始结束图标
	BitmapDescriptor bdStart = BitmapDescriptorFactory.fromResource(R.drawable.location_start);
	BitmapDescriptor bdEnd = BitmapDescriptorFactory.fromResource(R.drawable.location_finish);
	private LatLng llStart = null;

	// 城市名称用于查询天气
	private String mCityName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);

		mDrawer.setMenuSize(600);
        mDrawer.setContentView(R.layout.activity_main);        
        mDrawer.setSlideDrawable(R.drawable.navigationbar_icon_menu_normal);
        mDrawer.setDrawerIndicatorEnabled(true);               
		
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvSpeed = (TextView) findViewById(R.id.tvSpeed);
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvEnergy = (TextView) findViewById(R.id.tvEnergy);
        tvGPS = (TextView) findViewById(R.id.tvGPS);
		btnStart = (Button) findViewById(R.id.btnStart);
		btnLayout = (LinearLayout) findViewById(R.id.twoButton);
		btnFinish = (TextView) findViewById(R.id.btnFinish);
		btnPause = (TextView) findViewById(R.id.btnPause);
        
        initTimer();
                    
        initMap();
        
        initSrceenLock();
        
        // 判断GPS服务是否打开
        if(!isOpenLocationService(getApplicationContext())) {
			Intent intent = new Intent(MainActivity.this, CheckLocation.class);
			startActivity(intent);
		}
	}

	private void initSrceenLock() {
		slider_layout = (SliderLayout) findViewById(R.id.slider_layout);
		slider_layout.setMainHandle(handler);
		
		// 注册广播接收器，用于监听屏幕亮暗广播
		IntentFilter mScreenOnFilter = new IntentFilter("android.intent.action.SCREEN_ON");
		MainActivity.this.registerReceiver(mSrceenOnReceiver, mScreenOnFilter);
		
		IntentFilter mSrceenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
		MainActivity.this.registerReceiver(mSrceenOffReceiver, mSrceenOffFilter);
	}

	private void initMap() {					
		mMapView = (MapView) findViewById(R.id.bmapView);	
		// 隐藏地图缩放控件,地图LOGO,比例尺	
		mMapView.showScaleControl(false);
		mMapView.showZoomControls(false);
		
		// 用于放置线路点
		points = new ArrayList<LatLng>();

		mBaiduMap = mMapView.getMap();
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		mBaiduMap.setMaxAndMinZoomLevel(mBaiduMap.getMaxZoomLevel(), ZOOMLEVEL_RUN);		
		
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setIsNeedAddress(true);
		option.setCoorType("bd09ll");
		option.setScanSpan(LocationClientOption.MIN_SCAN_SPAN);
		mLocClient.setLocOption(option);
		mLocClient.start();
	}	

	@SuppressLint("HandlerLeak")
	private void initTimer() {				
		handler = new Handler() {		
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_TIMER_BEGIN:
					mCount++;
					mSec = (int)(mCount / 10);
					if(60 == mSec) {
						mCount = 0;
						mSec = 0;
						mMin++;
					}
					if(60 == mMin) {
						mMin = 0;
						mHour++;
					}
					//Log.i("Time: ", mHour + " : " + mMin + " : " + mSec);
					tvTime.setText(String.format("%1$02d : %2$02d : %3$02d", mHour, mMin, mSec));					
					break;

				case MSG_LOCK_SUCESS:
					slider_layout.setVisibility(View.INVISIBLE);
					if(isRunningFlag) {
						btnLayout.setVisibility(View.VISIBLE);
					}
					break;
				}
				super.handleMessage(msg);
			}
			
		};
		btnStart.setOnClickListener(startListener);
		btnFinish.setOnClickListener(startListener);
		btnPause.setOnClickListener(startListener);
	}
	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置			
			if(location == null || mMapView == null) {	
				return;
			}

			MyLocationData locData = new MyLocationData.Builder()
				.accuracy(location.getRadius())
				// 此处设置开发者获取到的方向信息，顺时针0-360
				.direction(100).latitude(location.getLatitude())
				.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);	
			//mCityName = location.getCity();
			if(null != location.getCity())
				mCityName = location.getCity();
			
			// 第一次定位
			if(isFirstLoc) {
				isFirstLoc = false;
				llStart = new LatLng(location.getLatitude(), location.getLongitude());
				MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(llStart);
				mBaiduMap.animateMapStatus(update);				
			}
									
			// 当点击开始按钮开始绘制路线
			if(isRunningFlag) {
				// 标记起点
				OverlayOptions ooStart = new MarkerOptions().position(llStart).icon(bdStart);
				mBaiduMap.addOverlay(ooStart);
				
				if(point == null) {
					point = new LatLng(location.getLatitude(), location.getLongitude());
					// 第一次定位时只有一个点不能绘制直线
					points.add(point);
					// 初始化标记点
					mTopPoint = point;
					mBottomPoint = point;
					mLeftPoint = point;
					mRightPoint = point;
				} else {
					if(point.latitude != location.getLatitude() || 
					   point.longitude != location.getLongitude()) {
						timeCounter++;
						point = new LatLng(location.getLatitude(), location.getLongitude());
						// 每三秒钟计算每个点之间的距离
						if (timeCounter == 3) {
							timeCounter = 0;
							totalDistance += DistanceUtil.getDistance(points.get(points.size()-1), point);
						}							
						points.add(point);
					}
				}
				// 记录经纬度的变化,用于优化路线绘制
				// saveLatLngToFile(point, location.getSatelliteNumber());
				// 绘制出路线图		
				points.add(point);				
				OverlayOptions options = new PolylineOptions().width(10)
						.color(0xAAFF0000).points(points);
				mBaiduMap.addOverlay(options);		
				
				// 根据路线图来调整地图显示
				adjustMapStatus(point);
				
				// 根据卫星数据来判断GPS信号强弱
				int number = location.getSatelliteNumber();
				if(number == -1) {
					tvGPS.setText(R.string.gps_no);
				} else if (number < 3) {
					tvGPS.setText(R.string.gps_weak);
					tvGPS.setTextColor(Color.parseColor("#FFB90F"));
				} else {
					tvGPS.setText(R.string.gps_good);
					tvGPS.setTextColor(Color.parseColor("#66CD00"));
				}
				// 显示当前速度				
				tvSpeed.setText(String.format("%.2f", location.getSpeed()));
				
				// 显示当前消耗的热量
				totalCalorie = 60 * (mHour + mMin/60 + mSec/3600) * (location.getSpeed()/10);
				tvEnergy.setText(String.format("%.2f", totalCalorie));
				
				// 显示当前距离
				tvDistance.setText(String.format("%.2f", totalDistance / 1000));
			}						
		}	
	}	
	/*
	private void saveLatLngToFile(LatLng p, int satelliteNumber) {
		// 检测sd卡状态
		if(!(Environment.isExternalStorageEmulated() && 
				Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {			
			return;
		}
		String path = Environment.getExternalStorageDirectory() + "/latlng.txt";
		String content = "Latitude:" + p.latitude + "  Longitude:" + p.longitude + "  Number:" + satelliteNumber + "\r\n";
		FileOutputStream outStream = null;
		try{
			File file = new File(path);
			// 判断文件是否存在
			if(file.exists()) {			
				outStream = new FileOutputStream(file, true);
			} else {
				outStream = new FileOutputStream(file);
			}
			outStream.write(content.getBytes());
			outStream.flush();
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}				
	}
	*/
	
	private void adjustMapStatus(LatLng p) {
		if(p.longitude < mLeftPoint.longitude)
			mLeftPoint = p;
		if(p.longitude > mRightPoint.longitude)
			mRightPoint = p;
		if(p.latitude < mBottomPoint.latitude)
			mBottomPoint = p;
		if(p.latitude > mTopPoint.latitude)
			mTopPoint = p;
		if(totalDistance > ZOOM_DISTANCE) {
			double d = 0.0;
			double d1 = DistanceUtil.getDistance(mTopPoint, mBottomPoint);
			double d2 = DistanceUtil.getDistance(mLeftPoint, mRightPoint);
			if(d1 > d2)
				d = d1;
			else
				d = d2;
			if(d > ZOOM_DISTANCE) {				
				ZOOMLEVEL_RUN++;
				ZOOM_DISTANCE *= 2;
				mBaiduMap.setMaxAndMinZoomLevel(mBaiduMap.getMaxZoomLevel(), ZOOMLEVEL_RUN);
			}
		}
	}
	
	
	public class MyItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Log.i("test", "position: " + position + " viewid: " + view.getId());
			switch (position) {
			case 1:
				mDrawer.closeMenu();
				break;
			case 2:
				Intent intent = new Intent(MainActivity.this, RecordActivity.class);
				startActivity(intent);
				break;
			}
		}
	}	
	
	
	View.OnClickListener startListener = new View.OnClickListener() {		
		@Override
		public void onClick(View v) {				
			switch (v.getId()) {
			case R.id.btnStart:
				// 开启定时器
				startTimer();
				if(!isRunningFlag) {
					isRunningFlag = true;				
					btnLayout.setVisibility(LinearLayout.VISIBLE);					
				}
				break;
			case R.id.btnPause:		
				startTimer();
				if(isRunningFlag) {
					isRunningFlag = false;				
					btnPause.setText(R.string.contin);
					task.cancel();
					task = null;
					timer.cancel();
					timer.purge();
					timer = null;
					handler.removeMessages(msg.what);
				} else {
					isRunningFlag = true;
					btnPause.setText(R.string.pause);	
				}
				break;
			case R.id.btnFinish:	
				// 加入结束图标				
				OverlayOptions ooEnd = new MarkerOptions().position(points.get(points.size()-1)).icon(bdEnd);
				mBaiduMap.addOverlay(ooEnd);			
				// 开启系统锁屏
				if(null != mKeyLock) {
					mKeyLock.reenableKeyguard();
				}
				// 存放数据
				saveRunData();				
				break;
			}							
		}		
	};
	
	private void saveRunData() {
		// 初始化数据库
		DBManager dbMgr = new DBManager(this);			
		RunData rundata = new RunData();
		rundata.distance = totalDistance;
		rundata.hour = mHour;
		rundata.minutes = mMin;
		rundata.calorie = totalCalorie;
		long millis = System.currentTimeMillis();
		rundata.date = getCurrentDate(millis);
		rundata.millis = String.valueOf(millis);
		dbMgr.insert(rundata);
		Cursor c = dbMgr.queryTheCursor();
		
		// 计算当前路线中心
		double la = (mBottomPoint.latitude + mTopPoint.latitude) / 2;
		double lo = (mLeftPoint.longitude + mRightPoint.longitude) / 2;
		LatLng center = new LatLng(la, lo);
		
		DBPoints dbPoints = new DBPoints(this);
		// 获取刚刚插入数据的ID
		int pointid = c.getInt(c.getColumnIndex("_id"));
		if(c.moveToLast()) {
			dbPoints.insert(points, pointid, center, ZOOMLEVEL_RUN);
		}
		dbPoints.closeDB();
		dbMgr.closeDB();
		
		Intent intent = new Intent(MainActivity.this, RecordDetail.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable(INTENT_KEY, rundata);
		bundle.putSerializable(INTENT_ID, pointid);
		startActivity(intent);
	}
	
	// 开启定时器
	private void startTimer() {
		if(null == timer) {
			if(null == task) {
				task = new TimerTask() {						
					@Override
					public void run() {
						if(null == msg) {
							msg = new Message();
						} else {
							msg = Message.obtain();
						}
						msg.what = MSG_TIMER_BEGIN;
						handler.sendMessage(msg);
					}
				};
			}
			timer = new Timer(true);
			timer.schedule(task, 100, 100);
		}
	}
	
	// 监听截图按钮
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			mBaiduMap.snapshot(new SnapshotReadyCallback() {		 
				@Override
				public void onSnapshotReady(Bitmap pic) {
					// 检测sd卡状态
					if(!(Environment.isExternalStorageEmulated() && 
							Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {			
						return;
					}
					String path = Environment.getExternalStorageDirectory() + "/run";
					FileOutputStream outStream = null;
					try{
						File fileDir = new File(path);
						// 判断文件是否存在
						if(!fileDir.exists()) {
							fileDir.mkdir();
						}
						File file = new File(path + "/" + System.currentTimeMillis() + ".png");
						outStream = new FileOutputStream(file);
						// 保存图片格式为PNG, 图片质量100%， 数据outStream
						if(pic.compress(Bitmap.CompressFormat.PNG, 100, outStream)) {
							outStream.flush();
							outStream.close();
						}
						Toast.makeText(MainActivity.this, "屏幕截图成功，图片存在: " + file.toString(),
								Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			Toast.makeText(MainActivity.this, "正在截取屏幕图片...", Toast.LENGTH_SHORT).show();
		}		
	};
	
	// 监听屏幕点亮广播
	private BroadcastReceiver mSrceenOnReceiver = new BroadcastReceiver() {		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(isRunningFlag) {
				btnLayout.setVisibility(View.INVISIBLE);
				slider_layout.setVisibility(View.VISIBLE);
			}
		}
	};
	// 监听屏幕变暗广播  当屏幕变暗时取消系统锁屏
	private KeyguardManager mKeyManager = null;
	private KeyguardLock mKeyLock = null;
	private BroadcastReceiver mSrceenOffReceiver = new BroadcastReceiver() {		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals("android.intent.action.SCREEN_OFF") && 
					btnLayout.getVisibility() == View.VISIBLE) {
				mKeyManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
				mKeyLock = mKeyManager.newKeyguardLock("MyLock");
				mKeyLock.disableKeyguard();
			}
		}
	};
	
	private String getCurrentDate(long current) {		
		String date = (String)DateUtils.formatDateTime(this, current, DateUtils.FORMAT_SHOW_DATE);
		String week = (String)DateUtils.formatDateTime(this, current, DateUtils.FORMAT_SHOW_WEEKDAY);
		String time = (String)DateUtils.formatDateTime(this, current, DateUtils.FORMAT_SHOW_TIME);
		String mCurrentTime = date + " " + week + " " + time;
		return mCurrentTime;
	}
	
	@Override
	protected void onMenuItemClicked(int position) {
		// Log.i("test", "position: " + position);
		Intent intent = null;
		switch (position) {
		case 0:
			intent = new Intent(MainActivity.this, UserActivity.class);
			startActivity(intent);
			break;
		case 1:
			mDrawer.closeMenu();
			break;
		case 2:
			intent = new Intent(MainActivity.this, RecordActivity.class);
			startActivity(intent);
			break;
		case 3:
			intent = new Intent(MainActivity.this, InformationActivity.class);
			startActivity(intent);
			break;
		case 4:
			intent = new Intent(MainActivity.this, TestActivity.class);
			startActivity(intent);
			break;
		case 5:
			intent = new Intent(MainActivity.this, SetActivity.class);
			startActivity(intent);
			break;
		}
	}	
/*
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {			
			exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}	

	private void exit() {
		if((System.currentTimeMillis() - clickTime) > 2000) {
			Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_LONG).show();
			clickTime = System.currentTimeMillis();
		} else {
			ActivityCollector.finishAll();
		}
	}
*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Log.i("test", "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.weather, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_weather:
			// item.setIcon(R.drawable.cloudy2);
			// Log.i("weather", mCityName);
			if(null != mCityName) {
				Intent intent = new Intent(MainActivity.this, WeatherActivity.class);				
				intent.putExtra("cityname", mCityName);
				startActivity(intent);
			}
			break;
			
		case android.R.id.home:	
			mDrawer.toggleMenu();
			break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
		JPushInterface.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
		JPushInterface.onPause(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 开启系统锁屏
		if(mKeyLock != null) {
			mKeyLock.reenableKeyguard();
		}
		// 取消广播监听
		MainActivity.this.unregisterReceiver(mSrceenOnReceiver);
		MainActivity.this.unregisterReceiver(mSrceenOffReceiver);				
		// 退出时销毁定位		
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mBaiduMap.clear();
		mMapView.onDestroy();
		mMapView = null;		
		// 回收图标资源
		bdStart.recycle();
		bdEnd.recycle();
	}

	private boolean isOpenLocationService(Context context) {
		LocationManager location = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		// 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
		// 判断GPS有没有打开
		boolean gps = location.isProviderEnabled(LocationManager.GPS_PROVIDER);
		// 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
		boolean network = location.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if(gps || network) {
			return true;
		}
		return false;
	}
	
}
