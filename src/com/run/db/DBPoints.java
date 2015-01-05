package com.run.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.mapapi.model.LatLng;

public class DBPoints {
	
	private DBHelper helper;
	private SQLiteDatabase db;

	public DBPoints(Context context) {
		helper = new DBHelper(context, DBHelper.DATABASE_LATLNG, DBHelper.DATABASE_VERSION);
		//因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);  
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里 
		db = helper.getWritableDatabase();
	}
	
	public void insert(List<LatLng> points, int id, LatLng center, float zoomlevel) {		
		// 开始事务
		db.beginTransaction();				
		try {
			for(LatLng p : points) {
				db.execSQL("INSERT INTO latlng VALUES(NULL, ?, ?, ?, ?, ?, ?)",
						new Object[]{p.latitude, p.longitude, id, center.latitude, 
						center.longitude, zoomlevel});
			}
			// 设置事务成功完成
			db.setTransactionSuccessful();		
		} finally {
			// 结束事务
			db.endTransaction();
		}
	}
	
	public List<LatLng> query(int id) {
		List<LatLng> points = new ArrayList<LatLng>();
		Cursor c = queryTheCursor(id);
		while(c.moveToNext()) {
			LatLng p = new LatLng(c.getDouble(c.getColumnIndex("latitude")), 
					c.getDouble(c.getColumnIndex("longitude")));
			points.add(p);
		}
		return points;
	}
	
	public String queryTheCenter(int id) {
		Cursor c = queryTheCursor(id);
		String ll = "";
		// 返回的cursor是-1位置,需要向后移动一次才有数据
		if(c.moveToNext()) {
			ll = c.getDouble(c.getColumnIndex("la")) + " " + c.getDouble(c.getColumnIndex("lo"));		
		}
		return ll;
	}
	
	public int queryTheZoomlevel(int id) {
		Cursor c = queryTheCursor(id);
		int level = 0;		
		// 返回的cursor是-1位置,需要向后移动一次才有数据
		if(c.moveToNext()) {
			level = c.getInt(c.getColumnIndex("level"));			
		}
		return level;
	}
	
	public Cursor queryTheCursor(int id) {
		return db.rawQuery("SELECT * FROM latlng WHERE pointid = " + id, null);
	}
	
	public void closeDB() {
		db.close();
	}
	
}
