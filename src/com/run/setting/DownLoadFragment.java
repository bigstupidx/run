package com.run.setting;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.run.R;
import com.run.setting.DownloadList.OnDeleteListener;

public class DownLoadFragment extends Fragment {
	
	private List<String> contentList = null;	
	private DownloadList downloadList;
	private DownloadAdapter adapter;
	private MKOfflineMap mOffline;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {		
		return inflater.inflate(R.layout.offlinemap_download, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		initList();		
		downloadList = (DownloadList) getActivity().findViewById(R.id.downloadList);
		downloadList.setOnDeleteListener(new OnDeleteListener() {			
			@Override
			public void onDelete(int index) {
				// 删除离线地图
				if (mOffline.remove(mOffline.searchCity(contentList.get(index)).get(0).cityID)) {
					// Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_SHORT).show();
					Log.i("offline", "删除成功");
				}
				contentList.remove(index);
				getOfflineList();
				adapter.notifyDataSetChanged();
			}	
		});
		adapter = new DownloadAdapter(getActivity(), 0, contentList);
		downloadList.setAdapter(adapter);
		super.onActivityCreated(savedInstanceState);
	}		

	private void initList() {
		mOffline = new MKOfflineMap();
		mOffline.init(null);
		getOfflineList();
	}
	
	private void getOfflineList() {
		// 已下载的离线地图信息列表
		ArrayList<MKOLUpdateElement> localMapList = mOffline.getAllUpdateInfo();
		contentList = new ArrayList<String>();
		if (localMapList == null) {			
			localMapList = new ArrayList<MKOLUpdateElement>();			
		} else {
			for (MKOLUpdateElement element : localMapList) {
				if (element.status == MKOLUpdateElement.FINISHED) {
					contentList.add(element.cityName);					
				}
			}
		}
	}

}
