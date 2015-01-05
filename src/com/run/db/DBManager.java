package com.run.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.run.utils.RunData;

public class DBManager {
	
	private DBHelper helper;
	private SQLiteDatabase db;

	public DBManager(Context context) {
		helper = new DBHelper(context, DBHelper.DATABASE_DATA, DBHelper.DATABASE_VERSION);
		//因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);  
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里 
		db = helper.getWritableDatabase();
	}
	
	public void insert(RunData data) {		
		// 开始事务
		db.beginTransaction();				
		try {
			db.execSQL("INSERT INTO run VALUES(NULL, ?, ?, ?, ?, ?, ?)",
					new Object[]{data.distance, data.hour, data.minutes, data.calorie, data.date, data.millis});
			// 设置事务成功完成
			db.setTransactionSuccessful();	
		} finally {
			// 结束事务
			db.endTransaction();
		}
	}
	
	public List<RunData> query() {
		ArrayList<RunData> rundatas = new ArrayList<RunData>();
		Cursor c = queryTheCursor();
		while(c.moveToNext()) {
			RunData data = new RunData();
			data.distance = c.getDouble(c.getColumnIndex("distance"));
			data.hour = c.getInt(c.getColumnIndex("hour"));
			data.minutes = c.getInt(c.getColumnIndex("minutes"));
			data.calorie = c.getDouble(c.getColumnIndex("calorie"));
			data.date = c.getString(c.getColumnIndex("date"));
			data.millis = c.getString(c.getColumnIndex("millis"));
			rundatas.add(data);
		}
		c.close();
		return rundatas;
	}
	
	public List<Integer> queryId() {
		List<Integer> mIds = new ArrayList<Integer>();
		Cursor c = queryTheCursor();
		while(c.moveToNext()) {
			int id = c.getInt(c.getColumnIndex("_id"));
			mIds.add(id);
		}
		return mIds;
	}
	
	public Cursor queryTheCursor() {
		return db.rawQuery("SELECT * FROM run ORDER BY _id DESC", null);
	}
	
	public void closeDB() {
		db.close();
	}
	
}
