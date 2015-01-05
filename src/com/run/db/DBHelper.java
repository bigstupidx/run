package com.run.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	
	public final static String DATABASE_DATA = "run.db";
	public final static String DATABASE_LATLNG = "latlng.db";
	public final static int DATABASE_VERSION = 1;
	// 创建跑步数据表语句
	public final static String CREATE_RUN = "CREATE TABLE IF NOT EXISTS run ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "distance DOUBLE, "
			+ "hour INTEGER, "
			+ "minutes INTEGER, "
			+ "calorie DOUBLE, "
			+ "date VARCHAR, " 
			+ "millis VARCHAR)";
	
	// 创建路线表语句
	public final static String CREATE_LATLNG = "CREATE TABLE IF NOT EXISTS latlng ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "latitude DOUBLE, "
			+ "longitude DOUBLE, "
			+ "pointid INTEGER, "
			+ "la DOUBLE, "
			+ "lo DOUBLE, "
			+ "level INTEGER)";
	
	public DBHelper(Context context, String databaseName, int databaseVersion) {		
		super(context, databaseName, null, databaseVersion);		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub		
		db.execSQL(CREATE_RUN);
		db.execSQL(CREATE_LATLNG);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	    // db.execSQL("ALTER TABLE person ADD COLUMN other STRING");
	}

}
