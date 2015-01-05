package com.run.user;

import java.util.ArrayList;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class ViewPageAdapter extends PagerAdapter {

	private ArrayList<View> views;
	
	public ViewPageAdapter(ArrayList<View> views) {
		this.views = views;
	}
	
	@Override
	public int getCount() {
		if (views != null)
			return views.size();
		return 0;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return (arg0 == arg1);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(views.get(position));		
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		container.addView(views.get(position));
		return views.get(position);
	}

}
