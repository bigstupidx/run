package com.run;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.run.utils.Item;
import com.run.utils.Utils;

public class MenuAdapter extends BaseAdapter {

	public interface MenuListener {
		void onActiveViewChanged(View v);
	}
	
	private Context mContext;
	private List<Object> mItems;
	private MenuListener mListener;
	private int mActivePosition = -1;
	
	public MenuAdapter(Context context, List<Object> items) {
		mContext = context;
		mItems = items;
	}
	
	public void setListener(MenuListener listener) {
		mListener = listener;
	}
	
	public void setAcitivePosition(int position) {
		mActivePosition = position;
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean areAllItemsEnabled() {		
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return getItem(position) instanceof Item;
	}

	@Override
	public int getItemViewType(int position) {		
		return getItem(position) instanceof Item ? 0 : 1;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		Object item = getItem(position);
		
		if(item instanceof Item) {
			if(v == null) {
				v = LayoutInflater.from(mContext).inflate(R.layout.menu_row_item, parent, false);
			}
			
			TextView tv = (TextView) v;
			tv.setText(((Item)item).mTitle);
			// Log.i("menuadapter", "item");
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {		
				if (position == 0 && ((Item)item).mIcon != null) {
					// Log.i("menuadapter", "position == 0");					
					Bitmap bitmap = Utils.createCircleImage(((Item)item).mIcon, 200);
					tv.setCompoundDrawablesWithIntrinsicBounds(
							new BitmapDrawable(parent.getResources(), bitmap), null, null, null);
				} else {
					tv.setCompoundDrawablesRelativeWithIntrinsicBounds(((Item)item).mIconRes, 0, 0, 0);
				}				
			} else {
				if (position == 0) {
					Bitmap bitmap = BitmapFactory.decodeResource(parent.getResources(), ((Item)item).mIconRes);
					bitmap = Utils.createCircleImage(bitmap, 200);
					tv.setCompoundDrawablesWithIntrinsicBounds(
							new BitmapDrawable(parent.getResources(), bitmap), null, null, null);
				} else {
					tv.setCompoundDrawablesWithIntrinsicBounds(((Item)item).mIconRes, 0, 0, 0);
				}				
			}
		}
		v.setTag(R.id.mdActiveViewPosition, position);
		
		if(position == mActivePosition) {
			mListener.onActiveViewChanged(v);
		}
		return v;
	}		

}
