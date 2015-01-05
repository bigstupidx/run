package com.run.setting;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.run.R;

public class ExpandAdapter extends BaseExpandableListAdapter {
	
	private Context context;
    private LayoutInflater father_Inflater = null;
    private LayoutInflater son_Inflater = null;
    
    private ArrayList<String> father_array;	// 父菜单名称
    private ArrayList<List<String>> son_array;	// 子菜单名称
	private ArrayList<List<Integer>> size_array;
	private ArrayList<List<Integer>> id_array;
	private ArrayList<MKOLUpdateElement> elements = null;  // 已经下载城市信息
    
    public ExpandAdapter(Context context) {
		this.context = context;
		father_Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		son_Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
		init_data();
    }

    // 返回父菜单的行数
 	@Override
 	public int getGroupCount() {
 		return father_array.size();
 	}
 	
    // 返回当前父菜单下的子菜单
	@Override
	public Object getGroup(int groupPosition) {
		return father_array.get(groupPosition);
	}
	
	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		Father_ViewHolder father = null;
		if(convertView == null) {
			convertView = father_Inflater.inflate(R.layout.province, null);
			father = new Father_ViewHolder();
			father.father_TextView = (TextView) convertView.findViewById(R.id.province_name);
			father.image_view = (ImageView) convertView.findViewById(R.id.province_imageview);
			father.layout = (RelativeLayout) convertView.findViewById(R.id.province);
			convertView.setTag(father);
		} else {
			father = (Father_ViewHolder) convertView.getTag();
		}
		// Log.i("father", father_array.get(groupPosition));
		father.father_TextView.setText(father_array.get(groupPosition));		
		if(isExpanded) {
			father.image_view.setImageDrawable(context.getResources().getDrawable(R.drawable.arrow_down));			
			father.layout.setBackgroundResource(R.color.setting_bg_color);
		} else {
			father.image_view.setImageDrawable(context.getResources().getDrawable(R.drawable.arrow_right));			
			father.layout.setBackgroundResource(R.drawable.set_list_bg);
		}
		return convertView;
	}

	// 返回子菜单的行数
	@Override
	public int getChildrenCount(int groupPosition) {
		return son_array.get(groupPosition).size();
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return son_array.get(groupPosition).get(childPosition);
	}
		
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// return childPosition;
		return id_array.get(groupPosition).get(childPosition);
	}
		
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		Son_ViewHolder son = null;
		if(convertView == null) {
			convertView = son_Inflater.inflate(R.layout.city, null);
			son = new Son_ViewHolder();
			son.son_TextView = (TextView) convertView.findViewById(R.id.city_name);
			son.son_TextView1 = (TextView) convertView.findViewById(R.id.city_size);
			son.son_TextView2 = (TextView) convertView.findViewById(R.id.city_state);
			convertView.setTag(son);
		} else {
			son = (Son_ViewHolder)convertView.getTag();
		}
		// Log.i("father", "g: " + groupPosition + " c: " + childPosition);		
		son.son_TextView.setText(son_array.get(groupPosition).get(childPosition));
		// 判断当前地图包状态
		if (elements != null) {
			for (MKOLUpdateElement e : elements) {
				if (e.cityName.equals(son_array.get(groupPosition).get(childPosition))) {
					if (e.status == MKOLUpdateElement.FINISHED) {
						son.son_TextView2.setText("(已下载)");
					} else if (e.status == MKOLUpdateElement.DOWNLOADING) {
						son.son_TextView2.setText("(下载中)");
					}
				}
			}
		}
		
		son.son_TextView1.setText(String.format("%.2f",
				Double.valueOf(size_array.get(groupPosition).get(childPosition))/1000000) + "MB");
		
		return convertView;
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	private void init_data() {
		
		father_array = new ArrayList<String>();
        son_array = new ArrayList<List<String>>();
        // 地图包大小
        size_array = new ArrayList<List<Integer>>();
        // 城市id
        id_array = new ArrayList<List<Integer>>();
        
        List<String> three = new ArrayList<String>();
        List<Integer> three1 = new ArrayList<Integer>();
        List<Integer> three2 = new ArrayList<Integer>();        
        List<String> four = new ArrayList<String>();
        List<Integer> four1 = new ArrayList<Integer>();
        List<Integer> four2 = new ArrayList<Integer>();        
        
        // 离线地图下载初始化
        MKOfflineMap offline = new MKOfflineMap();
        offline.init(null);
        
        ArrayList<MKOLSearchRecord> record = offline.getOfflineCityList();
        elements = offline.getAllUpdateInfo();      
        if (record != null) {
        	for(MKOLSearchRecord r : record) {
        		switch (r.cityType) {
				case 0:
					father_array.add("缩略图");
					List<String> one = new ArrayList<String>();					
					one.add(r.cityName);
					son_array.add(one);			
					
					List<Integer> one1 = new ArrayList<Integer>();
					one1.add(r.size);
					size_array.add(one1);
					
					List<Integer> one2 = new ArrayList<Integer>();
					one2.add(r.cityID);
					id_array.add(one2);

					break;
				case 1:
					father_array.add(r.cityName);
					ArrayList<MKOLSearchRecord> record1 = r.childCities;
					List<Integer> citysize = new ArrayList<Integer>();
					List<Integer> cityid = new ArrayList<Integer>();					
					List<String> cityname = new ArrayList<String>();					
					if (record1 != null) {
						for(MKOLSearchRecord s : record1) {
							citysize.add(s.size);							
							cityname.add(s.cityName);	
							cityid.add(s.cityID);
						}
						size_array.add(citysize);
						id_array.add(cityid);
						son_array.add(cityname);						
					}
					break;
				case 2:
					if(r.cityName.equals("香港特别行政区") || r.cityName.equals("澳门特别行政区")) {
						if(!father_array.contains("港澳")) {
							father_array.add("港澳");	
						}																							
						three.add(r.cityName);
						three1.add(r.size);
						three2.add(r.cityID);						
						
						if(three.size() == 2) {
							size_array.add(three1);
							son_array.add(three);
							id_array.add(three2);							
						}						
					} else {
						if(!father_array.contains("直辖市")) {
							father_array.add("直辖市");
						}								
						four.add(r.cityName);						
						four1.add(r.size);		
						four2.add(r.cityID);						
						
						if(four.size() == 4) {
							size_array.add(four1);
							son_array.add(four);
							id_array.add(four2);							
						}																
					}								       
					break;
				}
        	}        
        }        
        offline.destroy();
	}
	
	public final class Father_ViewHolder
    {
        private TextView father_TextView;
        private ImageView image_view;
        private RelativeLayout layout;
    }
	
    public final class Son_ViewHolder
    {
        private TextView son_TextView;
        private TextView son_TextView1;
        private TextView son_TextView2;
    }

}
