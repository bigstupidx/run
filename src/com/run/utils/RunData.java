package com.run.utils;

import java.io.Serializable;

public class RunData implements Serializable {

	private static final long serialVersionUID = 1L;

	public double distance;	// 单位m
	
	public int hour;		// 运动时间
	
	public int minutes;
	
	public double calorie;		// 单位kcal
	
	public String date;		// 日期
	
	public String millis;   // 日期秒格式
	
	public RunData() {}

	public RunData(double distance, int hour, int minutes, double calorie, String date, String millis) {		
		this.distance = distance;
		this.hour = hour;
		this.minutes = minutes;
		this.calorie = calorie;
		this.date = date;
		this.millis = millis;
	}
	
}
