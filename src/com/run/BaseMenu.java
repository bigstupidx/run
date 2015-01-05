package com.run;

import java.util.ArrayList;
import java.util.List;

import net.simonvt.menudrawer.MenuDrawer;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.run.MenuAdapter.MenuListener;
import com.run.utils.Item;
import com.run.utils.Utils;

public abstract class BaseMenu extends MyActivity implements MenuListener {

	public MenuDrawer mDrawer;
	private ListView mList;
	private MenuAdapter mAdapter;
	private int mActivePosition = 0;
	private ActionBar actionbar;
	private static final String STATE_ACTIVE_POSITION = "com.example.run.activePosition";	
	
	// 记录点击back键的时间
	private long clickTime = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityCollector.addActivity(this);
		
		actionbar = getActionBar();
		// 左上角图标的左边加上一个返回的图标
		actionbar.setDisplayHomeAsUpEnabled(true);
		// 左上角图标是否显示
		actionbar.setDisplayShowHomeEnabled(false);
		
		if(savedInstanceState != null) {
			mActivePosition = savedInstanceState.getInt(STATE_ACTIVE_POSITION);
		}
		mDrawer = MenuDrawer.attach(this, MenuDrawer.Type.OVERLAY);
		List<Object> items = new ArrayList<Object>();
		
		// 获取数据
		SharedPreferences share = getSharedPreferences("userinfo", MODE_PRIVATE);		
		String name = share.getString("nickname", "Runner");
		Item item;
		if(share.getBoolean("ret", false)) {
			// 当登录成功时
			String imageString = share.getString("figureurl", "");
			Bitmap bitmap = Utils.getUserIcon(imageString);
			item = new Item(name, bitmap);			
		} else {
			// 没有登录时
			item = new Item(name, R.drawable.bg_no_head);
		}
		
		items.add(item);
		items.add(new Item("开始运动", R.drawable.ic_action_refresh_dark));
		items.add(new Item("跑步记录", R.drawable.ic_action_select_all_dark));
		items.add(new Item("跑步有道", R.drawable.ic_action_refresh_dark));
		items.add(new Item("相册", R.drawable.ic_action_select_all_dark));
		items.add(new Item("设置", R.drawable.ic_action_select_all_dark));
		
		mAdapter = new MenuAdapter(this, items);
		mAdapter.setAcitivePosition(mActivePosition);		
		mAdapter.setListener(this);
		
		mList = new ListView(this);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(mItemClickListener);
		// 修改menu背景颜色
		mList.setBackgroundResource(R.color.menu_background);
		mDrawer.setMenuView(mList);
		
	}	

	protected abstract void onMenuItemClicked(int position);
	
	private AdapterView.OnItemClickListener mItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// Log.i("test", "position: " + position + " id: " + id);
			mActivePosition = position;
			mDrawer.setActiveView(view, position);
			mAdapter.setAcitivePosition(position);			
			onMenuItemClicked(position);
		}
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {		
		case android.R.id.home:
			mDrawer.toggleMenu();
			break;
		}
		return true;
	}	

	@Override
	protected void onDestroy() {		
		super.onDestroy();
		ActivityCollector.removeActivity(this);
	}

	@Override
	public void onActiveViewChanged(View v) {
		// TODO Auto-generated method stub
		mDrawer.setActiveView(v, mActivePosition);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putInt(STATE_ACTIVE_POSITION, mActivePosition);
	}
	
	@Override
	public void onBackPressed() {
		mDrawer.closeMenu();
		if((System.currentTimeMillis() - clickTime) > 2000) {
			Toast.makeText(BaseMenu.this, "再按一次退出", Toast.LENGTH_LONG).show();
			clickTime = System.currentTimeMillis();
		} else {
			ActivityCollector.finishAll();
		}
	}

}
