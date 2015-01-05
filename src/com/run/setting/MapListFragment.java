package com.run.setting;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.run.R;

public class MapListFragment extends Fragment {

	private ExpandableListView listView;
	private ExpandAdapter adapter;	
	private MKOfflineMap mOffline = null;
	private TextView mTextRatio = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {		
		return inflater.inflate(R.layout.offlinemap_list, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {		
		listView = (ExpandableListView) getActivity().findViewById(R.id.offlinelist);
		adapter = new ExpandAdapter(getActivity());
		listView.setGroupIndicator(null);
		listView.setAdapter(adapter);
		listView.setOnChildClickListener(new OnChildClickListener() {			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {	
				if (mOffline == null) {
					mOffline = new MKOfflineMap();
					mOffline.init(new MKOfflineMapListener() {
						@Override
						public void onGetOfflineMapState(int type, int state) {
							// Log.i("offline", "onGetOfflineMapState");
							switch (type) {
							case MKOfflineMap.TYPE_DOWNLOAD_UPDATE:
								// 处理下载进度更新提示
								MKOLUpdateElement update = mOffline.getUpdateInfo(state);
								if (update != null && mTextRatio != null) {						
									if (update.ratio == 100) {
										mTextRatio.setText("(已下载)");
									} else {
										mTextRatio.setText(String.format("(%d%%)", update.ratio));
									}
									adapter.notifyDataSetChanged();
								}
								break;
							case MKOfflineMap.TYPE_NEW_OFFLINE:
								// 有新离线地图安装
								// Log.d("offline", String.format("add offlinemap num:%d", state));
								break;
							case MKOfflineMap.TYPE_VER_UPDATE:
								// 版本更新提示
								break;
							}
						}
					});
				}
				
				// 需要进入下载程序
				mTextRatio = (TextView) v.findViewById(R.id.city_state);
				// mTextRatio.setText("下载");
				int cityid = (int) adapter.getChildId(groupPosition, childPosition);				
				mOffline.start(cityid);
//				Toast.makeText(getActivity(), "你点击了"+adapter.getChild(groupPosition, childPosition).toString()
//						+ " " + cityid, Toast.LENGTH_SHORT).show();
				return true;
			}
		});		
		super.onActivityCreated(savedInstanceState);
	}
	
}
