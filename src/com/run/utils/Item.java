package com.run.utils;

import android.graphics.Bitmap;

public class Item {

	public String mTitle;
	
	public int mIconRes;
	
	public Bitmap mIcon = null;	
	
	public Item(String title, int icons) {
		mTitle = title;
		mIconRes = icons;
	}
	
	public Item(String title, Bitmap icons) {
		mTitle = title;
		mIcon = icons;
	}
	
}
