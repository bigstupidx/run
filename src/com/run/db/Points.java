package com.run.db;

import java.util.List;

import com.baidu.mapapi.model.LatLng;

public class Points {

	private double latitude;
	
	private double longitude;

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public void insert(List<LatLng> poi) {
		for(LatLng p : poi) {
			setLatitude(p.latitude);
			setLongitude(p.longitude);
		}
	}
	
}
