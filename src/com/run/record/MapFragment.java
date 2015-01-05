package com.run.record;

import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.run.R;
import com.run.db.DBPoints;

public class MapFragment extends Fragment {
	
	private BaiduMap mBaiduMap;
	private int pointid;	
	private static final String INTENT_ID = "id";
	BitmapDescriptor bdStart = BitmapDescriptorFactory.fromResource(R.drawable.location_start);
	BitmapDescriptor bdEnd = BitmapDescriptorFactory.fromResource(R.drawable.location_finish);
	
	public BaiduMap getMap() {
		return mBaiduMap;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.rundetail_left, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {		
		// 获取从上个界面传过来的ID
		pointid = (Integer) getArguments().get(INTENT_ID);
		MapView mMapView = (MapView) getActivity().findViewById(R.id.detail_map);	
		mMapView.showScaleControl(false);
		mMapView.showZoomControls(false);		
		// 根据ID从数据库中获取路线点
		DBPoints dbPoints = new DBPoints(getActivity());		
		List<LatLng> points = dbPoints.query(pointid);
		if(null != points && points.size() >= 2 && points.size() < 1000) {			
			mBaiduMap = mMapView.getMap();
			// 在地图上绘制路线图
			// 获取线路开始和结束点
			LatLng llStart = points.get(0);
			LatLng llEnd = points.get(points.size() - 1);			
			
			// 加入开始和结束图标
			OverlayOptions ooStart = new MarkerOptions().position(llStart).icon(bdStart);
			mBaiduMap.addOverlay(ooStart);
			OverlayOptions ooEnd = new MarkerOptions().position(llEnd).icon(bdEnd);
			mBaiduMap.addOverlay(ooEnd);
			// 绘制跑步路线
			OverlayOptions options = new PolylineOptions().width(10)
					.color(0xAAFF0000).points(points);
			mBaiduMap.addOverlay(options);
			
			// 计算地图中心
			String center = dbPoints.queryTheCenter(pointid);
			LatLng llCenter = new LatLng(Double.valueOf(center.substring(0, center.indexOf(" "))), 
					Double.valueOf(center.substring(center.indexOf(" ")+1, center.length())));
			int zoomlevel = dbPoints.queryTheZoomlevel(pointid);
			// 设置地图状态
			// Log.i("mapfragment", "level: " + zoomlevel + "  center: " + llCenter.latitude + " " + llCenter.longitude);
			MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(llCenter, zoomlevel);
			mBaiduMap.setMapStatus(update);
		}
		dbPoints.closeDB();
		super.onActivityCreated(savedInstanceState);
	}
	
}
