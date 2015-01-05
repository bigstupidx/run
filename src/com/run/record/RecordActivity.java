package com.run.record;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.run.BaseMenu;
import com.run.MainActivity;
import com.run.R;
import com.run.db.DBManager;
import com.run.db.DBPoints;
import com.run.notify.InformationActivity;
import com.run.push.TestActivity;
import com.run.setting.SetActivity;
import com.run.user.UserActivity;
import com.run.utils.RunData;

public class RecordActivity extends BaseMenu {
	
	private ListView listview;
	private DBManager mgr;
	
	private TextView tvDistance;
	private TextView tvHour;
	private TextView tvMinutes;
	private TextView tvCalorie;
	// 从数据库中查询到的数据
	private List<RunData> runDatas;
	
	private static final int MINUTES = 60;
	// private int i = 0;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        mDrawer.setMenuSize(600);
        mDrawer.setContentView(R.layout.activity_record);        
        mDrawer.setSlideDrawable(R.drawable.navigationbar_icon_menu_normal);
        mDrawer.setDrawerIndicatorEnabled(true);       
        
        listview = (ListView) findViewById(R.id.lv_record);
		tvDistance = (TextView) findViewById(R.id.Total_Distance);
		tvHour = (TextView) findViewById(R.id.Total_Hour);
		tvMinutes = (TextView) findViewById(R.id.Total_Min);
		tvCalorie = (TextView) findViewById(R.id.Total_Calorie);
        
		mgr = new DBManager(RecordActivity.this);
		// addData();
		runDatas = mgr.query();
		if (runDatas.size() < 1) {
			ImageView image = (ImageView) findViewById(R.id.lv_nodata);
			image.setVisibility(View.VISIBLE);
			listview.setVisibility(View.INVISIBLE);
		} else {
			Cursor c = mgr.queryTheCursor();
			// CursorWrapper wrapper = new CursorWrapper(c);
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(RecordActivity.this, 
					R.layout.record_item, c,
					new String[]{"distance", "hour", "minutes", "calorie", "date"},
					new int[]{R.id.Item_Distance, R.id.Item_Hour, R.id.Item_Min, R.id.Item_Calorie, R.id.Item_Date}, 1);
			listview.setOnItemClickListener(itemListener);
			listview.setAdapter(adapter);
		}
		setTitleData();
	}

	private OnItemClickListener itemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {	
//			Log.i("record", "size: " + runDatas.size() + " position: " + position +
//					" content: " + runDatas.get(position).distance);			
			Intent intent = new Intent(RecordActivity.this, RecordDetail.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable(INTENT_KEY, runDatas.get(position));
			bundle.putSerializable(INTENT_ID, runDatas.size() - position);
			intent.putExtras(bundle);
			startActivity(intent);
		}
	};
	
	private void setTitleData() {
		double totalDistance = 0.0;
		int totalHour = 0;
		int totalMinutes = 0;
		double totalCalorie = 0.0;
		for(RunData data : runDatas) {			
			totalDistance += data.distance;
			totalHour += data.hour;
			totalMinutes += data.minutes;
			if((totalMinutes - MINUTES) >= 0) {
				totalMinutes = totalMinutes - MINUTES;
				totalHour++;
			}
			totalCalorie += data.calorie;			
		}
		tvDistance.setText(String.format("%.2f", totalDistance));
		tvHour.setText(String.format("%2d", totalHour));
		tvMinutes.setText(String.format("%2d", totalMinutes));
		tvCalorie.setText(String.format("%.2f", totalCalorie));
	}
	
	private void addData() {				
		for(int i = 0; i < 10; i++) {			
			RunData rundata = new RunData();
			rundata.distance = 5.6 + i * 0.1;
			rundata.hour = 0;
			rundata.minutes = 32 + i;
			rundata.calorie = 35.5 + i * 0.5;
			long millis = System.currentTimeMillis();
			rundata.date = getCurrentDate(millis);
			rundata.millis = String.valueOf(millis);
			mgr.insert(rundata);					 
		}
		
		DBPoints dbPoints = new DBPoints(RecordActivity.this);
		for(int i = 1; i < 11; i++) {
			List<LatLng> points = new ArrayList<LatLng>();
			LatLng p = new LatLng(30.318023, 120.102326);
			LatLng p1 = new LatLng(30.3052345, 120.103464);
			points.add(p);
			p = new LatLng(30.317844, 120.10194);
			points.add(p);
			p = new LatLng(30.292132, 120.10521);
			points.add(p);
			p = new LatLng(30.292171, 120.105289);
			points.add(p);
			p = new LatLng(30.292141, 120.105275);
			points.add(p);
			p = new LatLng(30.292173, 120.105071);
			points.add(p);
			p = new LatLng(30.29219, 120.105013);
			points.add(p);
			p = new LatLng(30.29219, 120.105013);
			points.add(p);
			p = new LatLng(30.292215, 120.10493);
			points.add(p);
			p = new LatLng(30.292317, 120.10486);
			points.add(p);
			p = new LatLng(30.292414, 120.104714);
			points.add(p);
			p = new LatLng(30.292446, 120.104602);
			points.add(p);
			// Log.i("map", "point: " + points.size());
			dbPoints.insert(points, i, p1, 15);
		}
		dbPoints.closeDB();
	}
	
	private String getCurrentDate(long current) {
		String date = (String)DateUtils.formatDateTime(this, current, DateUtils.FORMAT_SHOW_DATE);
		String week = (String)DateUtils.formatDateTime(this, current, DateUtils.FORMAT_SHOW_WEEKDAY);
		String time = (String)DateUtils.formatDateTime(this, current, DateUtils.FORMAT_SHOW_TIME);
		String mCurrentTime = date + " " + week + " " + time;
		return mCurrentTime;
	}
	
	@Override
	protected void onMenuItemClicked(int position) {
		Intent intent = null;
		switch (position) {
		case 0:
			intent = new Intent(RecordActivity.this, UserActivity.class);
			startActivity(intent);
			break;
		case 1:
			intent = new Intent(RecordActivity.this, MainActivity.class);
			startActivity(intent);
			break;
		case 2:
			mDrawer.closeMenu();				
			break;
		case 3:
			intent = new Intent(RecordActivity.this, InformationActivity.class);
			startActivity(intent);
			break;
		case 4:
			intent = new Intent(RecordActivity.this, TestActivity.class);
			startActivity(intent);
		case 5:
			intent = new Intent(RecordActivity.this, SetActivity.class);
			startActivity(intent);
			break;
		}
	}
}
