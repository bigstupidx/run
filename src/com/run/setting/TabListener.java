package com.run.setting;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;

public class TabListener<T extends Fragment> implements ActionBar.TabListener {

	private Fragment mFragment;
	private final Activity mActivity;
	private final String mTag;
	private final Class<T> mClass;	
	
	public TabListener(Activity activity, String tag, Class<T> clz) {		
		this.mActivity = activity;
		this.mTag = tag;
		this.mClass = clz;
	}

	/**
	 * 当Tab被选中的时候会调用
	 */
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (mFragment == null) {
			mFragment = Fragment.instantiate(mActivity, mClass.getName());
			ft.add(android.R.id.content, mFragment, mTag);
		} else {
			ft.attach(mFragment);
		}
	}

	/**
	 * 而当Tab没有被选中会调用
	 */
	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if (mFragment != null) {
			ft.detach(mFragment);
		}
	}

	/**
	 * 而当Tab再次被选中会调用
	 */
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

}
