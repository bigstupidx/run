package com.run.record;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
import com.run.BaseActivity;
import com.run.R;
import com.run.utils.RunData;

public class RecordDetail extends BaseActivity implements OnClickListener {

	private ImageView leftButton;
	private ImageView rightButton;
	private FragmentManager fragmentManager;
	// 用于显示地图的fragment
	private MapFragment mapFragment;
	// 用于显示照片的fragment
	private ImageFragment imageFragment;
	private MenuItem menuItem = null;
	private int select;
	
	private RunData data;
	private int pointid;
	private String shareText;
	
	// 截图的名称
	private String picName;
	// 照片的名称
	private String imageName = null;
	// 照片的地址
	private Uri imageUri = null;
	// 自拍照片
	private Bitmap snapshot = null;
	private LinearLayout runlayout;
	private ImageView imageView = null;
	
	public static final int TAKE_PHOTO = 1;
	public static final int CROP_PHOTO = 2;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recorddetail);

		// 初始化布局元素
		initViews();
		fragmentManager = getFragmentManager();
		// 初始化选中为0
		setTabSelection(0);
	}

	private void initViews() {
		// TODO Auto-generated method stub
		leftButton = (ImageView) findViewById(R.id.rundetail_left);
		rightButton = (ImageView) findViewById(R.id.rundetail_right);
		runlayout = (LinearLayout) findViewById(R.id.rundetail);		
		// View shareButton = (View) findViewById(R.id.action_share);
		leftButton.setOnClickListener(this);
		rightButton.setOnClickListener(this);		
		
		// 获得初始化数据
		Bundle bundle = RecordDetail.this.getIntent().getExtras();
		data = (RunData) bundle.getSerializable(INTENT_KEY);
		pointid = (Integer) bundle.getSerializable(INTENT_ID);
		// 获取文件夹下面的所有图片
		File file = new File(DIR);
		File[] fileList = file.listFiles();
		for (File f : fileList) {			
			String filepath = f.getPath();
			String filename = f.getName();
			String filesuffix = filename.substring(filename.indexOf(".")+1, 
					filename.length()).toLowerCase(Locale.getDefault());
			Log.i("myservice", "suffix: " + filesuffix + " path: " + filepath);
			if (filesuffix.equals("jpg") && filename.indexOf("_") > 0) {
				int fileid = Integer.valueOf(filename.substring(filename.indexOf("_")+1, filename.indexOf(".")));
				Log.i("myservice", "id: " + fileid + " pointid: " + pointid);
				if (fileid == pointid) {
					snapshot = BitmapFactory.decodeFile(filepath);
					break;
				}
			}
		}
		if(data != null) {
			initRunData();
		}
	}
	
	private void initRunData() {
		TextView distance = (TextView) findViewById(R.id.detail_Distance);
		TextView hour = (TextView) findViewById(R.id.detail_Hour);
		TextView minutes = (TextView) findViewById(R.id.detail_Min);
		TextView calorie = (TextView) findViewById(R.id.detail_Calorie);
				
		distance.setText(String.format("%.2f", data.distance));
		hour.setText(String.format("%2d", data.hour));
		minutes.setText(String.format("%2d", data.minutes));
		calorie.setText(String.format("%.2f", data.calorie));	
		
		// 分享输入的文本
		shareText = "我用爱跑步运动" + String.format("%.2f", data.distance) + "公里，耗时"
				+ String.format("%2d", data.hour) + ":" + String.format("%2d", data.minutes)
				+ "，消耗" + String.format("%.2f", data.calorie) + "千卡路里。";
		
	}

	@Override
	public void onClick(View v) { 
		switch (v.getId()) {
		case R.id.rundetail_left:			
			select = 0;
			if (menuItem != null)
				menuItem.setIcon(R.drawable.navigationbar_icon_share_normal);	
			setTabSelection(0);
			break;			
		case R.id.rundetail_right:			
			select = 1;
			if (menuItem != null)
				menuItem.setIcon(R.drawable.navigationbar_icon_camera);
			setTabSelection(1);
			break;
		}
	}

	private void setTabSelection(int tabIndex) {
		// 每次选中之前清除上次选中状态
		clearSelection();
		// 开启一个fragment事务
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		// 显示之前先隐藏所有fragment,防止多个fragment同时显示
		hideFragment(transaction);
		switch (tabIndex) {
		case 0:
			// 当点击图片时，改变图片显示状态
			leftButton.setImageResource(R.drawable.runned_tab_map_chosen);
			if(null == mapFragment) {
				Log.i("myservice", "create mapfragment");
				mapFragment = new MapFragment();
				Bundle bundle = new Bundle();
				bundle.putSerializable(INTENT_ID, pointid);
				mapFragment.setArguments(bundle);
				transaction.add(R.id.rundetail_content, mapFragment);
			} else {
				transaction.show(mapFragment);
			}
			break;
		case 1:
			rightButton.setImageResource(R.drawable.runned_tab_feeling_chosen);
			if(null == imageFragment) {
				Log.i("myservice", "create imagefragment");
				imageFragment = new ImageFragment();
				transaction.add(R.id.rundetail_content, imageFragment);			
			} else {
				transaction.show(imageFragment);
			}
			
			if (snapshot != null) {
				if (imageView == null) {
					imageView = imageFragment.getImageView();					
				}
				imageView.setImageBitmap(snapshot);
			}else if (imageUri != null) {			 		
				try {
					snapshot = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
					imageView.setImageBitmap(snapshot);
				} catch (FileNotFoundException e) {					
				}
			}		
			
			break;
		}		
		transaction.commit();
	}	

	private void clearSelection() {
		leftButton.setImageResource(R.drawable.runned_tab_map);
		rightButton.setImageResource(R.drawable.runned_tab_feeling);
	}
	
	private void hideFragment(FragmentTransaction transaction) {
		if(mapFragment != null) {
			transaction.hide(mapFragment);
		}
		if(imageFragment != null) {
			transaction.hide(imageFragment);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.share, menu);
		menuItem = menu.findItem(R.id.action_share);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		switch (item.getItemId()) {		
		case R.id.action_share:
			if (select == 0) {
				picName = System.currentTimeMillis() + ".jpg";
				takeSnapShot();
				SystemClock.sleep(1000);
				Intent intent = new Intent(RecordDetail.this, Share.class);			
				intent.putExtra(INTENT_PIC, picName);
				intent.putExtra(INTENT_SHARE, shareText);
				intent.putExtra(INTENT_IMAGE, imageName);
				startActivity(intent);
			} else if (select == 1) {
				imageName = System.currentTimeMillis() + "_" + pointid + ".jpg";
				// 创建File对象，用于存储拍照后的图片
				File outputImage = new File(DIR, imageName);
				try {
					if (outputImage.exists()) {
						outputImage.delete();
					}
					outputImage.createNewFile();
				} catch (IOException e) {					
				}
				imageUri = Uri.fromFile(outputImage);
				Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				// 启动相机程序
				startActivityForResult(intent, TAKE_PHOTO);
			}			
			break;
			
		case android.R.id.home:			
			finish();
			break;
		}
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case TAKE_PHOTO:
			if (resultCode == RESULT_OK) {				
				Intent intent = new Intent("com.android.camera.action.CROP");
				intent.setDataAndType(imageUri, "image/*");
				intent.putExtra("scale", true);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				// 启动照片裁剪程序
				startActivityForResult(intent, CROP_PHOTO);
			}
			break;
		case CROP_PHOTO:
			if (resultCode == RESULT_OK) {
				setTabSelection(1);
			}
			break;
		}
	}

	// 截图
	private void takeSnapShot() {
		mapFragment.getMap().snapshot(new SnapshotReadyCallback() {				
			@Override
			public void onSnapshotReady(Bitmap bit) {
				// 检测sd卡状态
				if(!(Environment.isExternalStorageEmulated() && 
						Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {			
					return;
				}				
				FileOutputStream outStream = null;
				try{
					File fileDir = new File(DIR);
					// 判断文件夹是否存在
					if(!fileDir.exists()) {
						fileDir.mkdir();
					}
					File file = new File(DIR + "/" + picName);
					outStream = new FileOutputStream(file);
					
					// 获取控件的bitmap
					Bitmap bit2 = takeScreenShot(RecordDetail.this);
					// 将两个bitmap拼接成在一起
					Bitmap pic = mixtureBitmap(bit, bit2);
					if(pic.compress(Bitmap.CompressFormat.JPEG, 100, outStream)) {
						outStream.flush();
						outStream.close();
					}
//					Toast.makeText(RecordDetail.this, "屏幕截图成功，图片存在: " + file.toString(),
//							Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private Bitmap mixtureBitmap(Bitmap first, Bitmap second) {
		Bitmap newBit = Bitmap.createBitmap(first.getWidth(), first.getHeight()+second.getHeight(), Config.ARGB_4444);
		Canvas cv = new Canvas(newBit);
		cv.drawBitmap(first, 0, 0, null);
		cv.drawBitmap(second, 0, first.getHeight(), null);
		cv.save(Canvas.ALL_SAVE_FLAG);
		cv.restore();
		return newBit;
	}
	
	private Bitmap takeScreenShot(Activity activity) {
		if(null == activity || activity.isFinishing()) {
			Log.i("snapshot", "activity为空");
			return null;
		}
		// 获取当前视图的view
		View screenView = activity.getWindow().getDecorView();
		screenView.setDrawingCacheEnabled(true);
		screenView.buildDrawingCache(true);
		
		// 获取数据控件的位置
		int[] location = new int[2];
		runlayout.getLocationOnScreen(location);
		int x = location[0];
		int y = location[1];
		int screenWidth = runlayout.getWidth();
		int screenHeight = runlayout.getHeight();
		
		Bitmap bitmap = null;
		try {
			// 返回数据控件的bitmap
			bitmap = Bitmap.createBitmap(screenView.getDrawingCache(), x, y, 
					screenWidth, screenHeight);
		} catch(IllegalArgumentException e) {
			Log.i("snapshot", "生成图片失败");
		}
		screenView.setDrawingCacheEnabled(false);
		screenView.destroyDrawingCache();		
		return bitmap;
	}

}
