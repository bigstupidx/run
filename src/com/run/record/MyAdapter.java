package com.run.record;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.run.R;

public class MyAdapter extends BaseAdapter {
	
	private Intent intent;
	
	private LayoutInflater mInflater;
	
	private Context context;
	
	public MyAdapter(Intent intent, Context context) {
		this.mInflater = LayoutInflater.from(context);
		this.intent = intent;
		this.context = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		OneView oneview;
		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.record_item, null);
			oneview = new OneView();
			oneview.time = (TextView) convertView.findViewById(R.id.Item_Date);
			oneview.distance = (TextView) convertView.findViewById(R.id.Item_Distance);
			oneview.calorie = (TextView) convertView.findViewById(R.id.Item_Calorie);
			oneview.hour = (TextView) convertView.findViewById(R.id.Item_Hour);
			oneview.min = (TextView) convertView.findViewById(R.id.Item_Min);
			convertView.setTag(oneview);
		} else {
			oneview = (OneView) convertView.getTag();
		}
		/*
		String time = getCurrentTime();
		RunData mRunData = (RunData) intent.getSerializableExtra("rundata");
		oneview.time.setText(time);		
		oneview.distance.setText(String.valueOf(mRunData.getDistance()));
		oneview.calorie.setText(String.valueOf(mRunData.getEnergy()));
		oneview.hour.setText(String.valueOf(mRunData.getHour()));
		oneview.min.setText(String.valueOf(mRunData.getMin()));
		*/
		return convertView;
	}	

	private class OneView {
		TextView time;  
	    TextView distance;  
	    TextView calorie;  
	    TextView hour;
	    TextView min;
	}

}
