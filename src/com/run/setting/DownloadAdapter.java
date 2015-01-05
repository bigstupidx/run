package com.run.setting;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.run.R;

public class DownloadAdapter extends ArrayAdapter<String> {
	
	private List<Integer> size_array = new ArrayList<Integer>();	

	public DownloadAdapter(Context context, int textViewResourceId, List<String> objects) {
		super(context, textViewResourceId, objects);
		initdata();		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if(convertView == null) {
			view = LayoutInflater.from(getContext()).inflate(R.layout.download_item, null);
		} else {
			view = convertView;
		}
		// 已经下载的城市列表和地图包大小
		TextView mCity = (TextView) view.findViewById(R.id.cityname);	
		mCity.setText(getItem(position));
		TextView mSize = (TextView) view.findViewById(R.id.mapsize);
		mSize.setText(String.format("%.2f",
				Double.valueOf(size_array.get(position)/1000000)) + "MB");
		return view;
	}	
	
	private void initdata() {
		MKOfflineMap mOffline = new MKOfflineMap();
		mOffline.init(null);
		
		ArrayList<MKOLUpdateElement> localMapList = mOffline.getAllUpdateInfo();
		if (localMapList == null) {
			localMapList = new ArrayList<MKOLUpdateElement>();
			size_array = null;			
		} else {
			for (MKOLUpdateElement element : localMapList) {
				if (element.status == MKOLUpdateElement.FINISHED) {
					size_array.add(element.size);					
				}
			}
		}
		/*
		Log.i("offline", size_array.size() + "");
		for (int i=0; i<size_array.size(); i++) {
			Log.i("offline", size_array.get(i) + " ");
		}
		*/
	}
}
