package com.run.weather;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class WeatherHandler extends DefaultHandler {
	private String status;
	private String date;			// 日期	
	private String sport;			// 运动指数
	private int pm25;				// PM2.5
	private String ultraviolet;		// 紫外线
	private String tagName;	
	private WeatherInfo weatherinfo = null;
	private ArrayList<WeatherInfo> weatherinfos;
	private int flag = 0;
	private boolean isSportInfo = false;
	private boolean isUltraviolet = false;
	
	public String getDate() {
		return " " + date;
	}
	
	public ArrayList<WeatherInfo> getWeatherinfos() {
		return weatherinfos;
	}
	
	public String getSport() {
		return sport;
	}
	
	public String getPM25() {
		// 判断污染级别
		return getPM25Info();		
	}
	
	public String getUltraviolet() {
		return " " + ultraviolet;
	}
	
	// 判断污染级别
	private String getPM25Info() {		
		if(pm25 <= 50) {
			return " " + pm25 + " 优";
		} else if (pm25 <= 100) {
			return " " + pm25 + " 良";
		} else if (pm25 <= 150) {
			return " " + pm25 + " 轻度污染";
		} else if (pm25 <= 200) {
			return " " + pm25 + " 中度污染";
		} else if (pm25 <= 300) {
			return " " + pm25 + " 重度污染";
		} else {
			return " " + pm25 + " 严重污染";
		}	
	}

	/**
	 * 开始解析xml文件
	 */
	@Override
	public void startDocument() throws SAXException {
		weatherinfos = new ArrayList<WeatherInfo>();
		System.out.println("````````begin````````");
	}

	/**
	 * 解析xml文件结束
	 */
	@Override
	public void endDocument() throws SAXException {
		System.out.println("````````end````````");
	}

	/**
	 * 开始解析标签
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attr) throws SAXException {
		tagName = localName;
		if (localName.equals("CityWeatherResponse")) {
			System.out.println("start-----CityWeatherResponse" + attr.getLength());
			// 获取标签的全部属性
			for (int i = 0; i < attr.getLength(); i++) {
				System.out.println("startElement " + attr.getLocalName(i) + "="
						+ attr.getValue(i));
			}
		} else if (localName.equals("date")) {
			System.out.println("start-----date");
			flag++;
			if (flag > 1) {
				weatherinfo = new WeatherInfo();
			}
		} else if (localName.equals("results")) {
			System.out.println("start-----results");
		} else if (localName.equals("weather_data")) {
			System.out.println("start-----weather_data");
		} else if (localName.equals("index")) {
			System.out.println("start-----index");
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (tagName != null) {
			String data = new String(ch, start, length);
			if (tagName.equals("status"))
				status = data;
			else if (tagName.equals("date")) {
				if (flag == 1) {
					date = data;
				} else {
					weatherinfo.setDate(data);
				}
			} else if (tagName.equals("weather")) {
				weatherinfo.setWeather(data);
			} else if (tagName.equals("wind")) {
				weatherinfo.setWind(data);
			} else if (tagName.equals("temperature")) {
				weatherinfo.setTemperature(data);
			} else if (tagName.equals("pm25")) {
				pm25 = Integer.valueOf(data);
			} else if (tagName.equals("title")) {				
				if (data.equals("运动"))
					isSportInfo = true;
				if (data.equals("紫外线强度"))
					isUltraviolet = true;
			} else if (isSportInfo && tagName.equals("des")) {			
				sport = data;
				isSportInfo = false;
			} else if (isUltraviolet && tagName.equals("zs")) {
				ultraviolet = data;
				isUltraviolet = false;
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// 在workr标签解析完之后，会打印出所有得到的数据
		tagName = "";
		if (localName.equals("CityWeatherResponse")) {
			System.out.println("end-----CityWeatherResponse");
			flag = 0;
			// printout();
		} else if (localName.equals("results")) {
			System.out.println("end-----results");
		} else if (localName.equals("weather_data")) {
			System.out.println("end-----weather_data");
		} else if (localName.equals("temperature")) {
			weatherinfos.add(weatherinfo);
		}
	}

}
