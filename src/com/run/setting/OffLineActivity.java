package com.run.setting;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.os.Bundle;
import android.view.MenuItem;

import com.run.BaseActivity;
import com.run.R;

public class OffLineActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		Tab tab = actionbar.newTab()
				.setText(R.string.city)
				.setTabListener(
						new TabListener<MapListFragment>(this, "MapList", MapListFragment.class));
		actionbar.addTab(tab);
		tab = actionbar.newTab()
				.setText(R.string.download)
				.setTabListener(new TabListener<DownLoadFragment>(this, "Download", DownLoadFragment.class));
		actionbar.addTab(tab);
				
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return true;
	}
}
