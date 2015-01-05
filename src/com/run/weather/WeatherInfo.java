package com.run.weather;

public class WeatherInfo {
	private String date;
	private String weather;		// 天气情况	
	private String temperature;	// 温度
//	private String humidity;	// 湿度
	private String wind;		// 风力
	
	public WeatherInfo() {		
	}
	
	public WeatherInfo(String date, String weather, String temperature, String wind) {
		this.date = date;
		this.weather = weather;
		this.temperature = temperature;
		this.wind = wind;
	}

	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getWeather() {
		return weather;
	}
	public void setWeather(String weather) {
		this.weather = weather;
	}
	public String getTemperature() {
		return temperature;
	}
	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}
	public String getWind() {
		return wind;
	}
	public void setWind(String wind) {
		this.wind = wind;
	}

}
