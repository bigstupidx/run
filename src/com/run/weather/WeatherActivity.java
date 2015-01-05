package com.run.weather;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.run.BaseActivity;
import com.run.R;
import com.run.weather.WeatherSource.OnFinishListener;

public class WeatherActivity extends BaseActivity {

	private String mCityName;
	private TextView mCityDate;
	private TextView mCity;
	private TextView mTemprature;
	private TextView mTempText;
	private TextView mUV;
	private TextView mHM;
	private TextView mWind;
	private TextView mPM25;
	private TextView mAdvise;
	private WeatherSource mWeatherSource;
	private WeatherHandler weatherHandler;
	private ArrayList<WeatherInfo> weatherInfos;
	private ImageView mWeatherImage;
	private ImageView mWeatherImage1;
	private ImageView mWeatherImage2;
	private ImageView mWeatherImage3;
	private TextView mDate1;
	private TextView mDate2;
	private TextView mDate3;
	private TextView mTemp1;
	private TextView mTemp2;
	private TextView mTemp3;
	private TextView mWind1;
	private TextView mWind2;
	private TextView mWind3;
	private ActionBar actionbar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.activity_weather);
		
		Intent intent = getIntent();
		mCityName = intent.getStringExtra("cityname");
		// Log.i("weather", mCityName);
		mCityName = mCityName.substring(0, mCityName.length()-1);
		
		initView();
						
		mWeatherSource = new WeatherSource(mCityName);
		mWeatherSource.setOnFinishListener(new FinishListener());
	}

	private void initView() {
		mCity = (TextView) findViewById(R.id.city_name);
		mCityDate = (TextView) findViewById(R.id.city_date);
		mTemprature = (TextView) findViewById(R.id.city_temprature);
		mTempText = (TextView) findViewById(R.id.city_text);
		mUV = (TextView) findViewById(R.id.second_uv);
		mHM = (TextView) findViewById(R.id.second_hm);
		mWind = (TextView) findViewById(R.id.second_wd);
		mPM25 = (TextView) findViewById(R.id.second_pm);
		mAdvise = (TextView) findViewById(R.id.run_advise);
		mWeatherImage = (ImageView) findViewById(R.id.weather_icon);
		mWeatherImage1 = (ImageView) findViewById(R.id.fource_oneicon);
		mWeatherImage2 = (ImageView) findViewById(R.id.fource_twoicon);
		mWeatherImage3 = (ImageView) findViewById(R.id.fource_threeicon);
		mDate1 = (TextView) findViewById(R.id.fource_onetm);
		mDate2 = (TextView) findViewById(R.id.fource_twotm);
		mDate3 = (TextView) findViewById(R.id.fource_threetm);
		mTemp1 = (TextView) findViewById(R.id.fource_onetp);
		mTemp2 = (TextView) findViewById(R.id.fource_twotp);
		mTemp3 = (TextView) findViewById(R.id.fource_threetp);
		mWind1 = (TextView) findViewById(R.id.fource_onete);
		mWind2 = (TextView) findViewById(R.id.fource_twote);
		mWind3 = (TextView) findViewById(R.id.fource_threete);
		
		weatherInfos = new ArrayList<WeatherInfo>();
		// Log.i("weather", "name: " + mCityName + " city: " + mCity);
		mCity.setText(mCityName);
	}
	
	private class FinishListener implements OnFinishListener {		
		@Override
		public void OnFinish(String result) {			
			try {
				// 创建一个SAXParserFactory
				SAXParserFactory factory = SAXParserFactory.newInstance();
				XMLReader reader = factory.newSAXParser().getXMLReader();
				weatherHandler = new WeatherHandler();
				// 为XMLReader设置内容处理器
				reader.setContentHandler(weatherHandler);
				// 开始解析文件
				reader.parse(new InputSource(new StringReader(result)));
				parseWeather();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("-----------Exception");
				e.printStackTrace();
			}
		}		
	}
	
	private void parseWeather() {
		weatherInfos = weatherHandler.getWeatherinfos();
		WeatherInfo weatherinfo = new WeatherInfo();		
		weatherinfo = weatherInfos.get(0);				
		String data = weatherinfo.getDate();		
		mTemprature.setText(data.substring(data.lastIndexOf("：")+1, data.lastIndexOf(")")));
		mTempText.setText(weatherinfo.getWeather());
		mWeatherImage.setBackgroundResource(getWeatherPic(weatherinfo.getWeather()));
		// 获取日期和时间
		final Calendar c = Calendar.getInstance();
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		String mWeek = data.substring(0, data.indexOf(" "));
		String date = month + "月" + day + "日  " + mWeek;
		mCityDate.setText(date);
		mUV.setText(weatherHandler.getUltraviolet());
		mWind.setText(weatherinfo.getWind());
		mPM25.setText(weatherHandler.getPM25());
		mAdvise.setText(weatherHandler.getSport());
		
		weatherinfo = weatherInfos.get(1);
		day += 1;
		date = month + "月" + day + "日  " + weatherinfo.getDate();
		mDate1.setText(date);
		mWeatherImage1.setBackgroundResource(getWeatherSmallPic(weatherinfo.getWeather()));
		mTemp1.setText(weatherinfo.getTemperature());
		mWind1.setText(weatherinfo.getWind());
		
		weatherinfo = weatherInfos.get(2);
		day += 1;
		date = month + "月" + day + "日  " + weatherinfo.getDate();
		mDate2.setText(date);
		mWeatherImage2.setBackgroundResource(getWeatherSmallPic(weatherinfo.getWeather()));
		mTemp2.setText(weatherinfo.getTemperature());
		mWind2.setText(weatherinfo.getWind());
		
		weatherinfo = weatherInfos.get(3);
		day += 1;
		date = month + "月" + day + "日  " + weatherinfo.getDate();
		mDate3.setText(date);
		mWeatherImage3.setBackgroundResource(getWeatherSmallPic(weatherinfo.getWeather()));
		mTemp3.setText(weatherinfo.getTemperature());
		mWind3.setText(weatherinfo.getWind());		
	}
		
	private int getWeatherPic(String weather) {		
		if("晴".equals(weather)) {
			return R.drawable.sunny;
		} else if ("多云".equals(weather)) {
			return R.drawable.cloudy;
		} else if ("阴".equals(weather) || "多云转阴".equals(weather)) {
			return R.drawable.overcast;
		} else if ("晴转多云".equals(weather)) {
			return R.drawable.partly_cloudy;
		} else if ("浮尘".equals(weather) || "扬沙".equals(weather) || "霾".equals(weather)) {
			return R.drawable.dust;
		} else if ("强沙尘暴".equals(weather) || "沙尘暴".equals(weather)) {
			return R.drawable.tornado;
		} else if ("大雨".equals(weather) || "暴雨".equals(weather)) {
			return R.drawable.heavy_rain;
		} else if ("中雨".equals(weather)) {
			return R.drawable.moderate_rain;
		} else if ("小雨".equals(weather)) {
			return R.drawable.light_rain;
		} else if ("雷阵雨".equals(weather) || "阵雨".equals(weather)) {
			return R.drawable.shower;
		} else if ("大雪".equals(weather) || "暴雪".equals(weather)) {
			return R.drawable.heavy_snow;
		} else if ("小雪".equals(weather)) {
			return R.drawable.light_snow;
		} else if ("雨夹雪".equals(weather)) {
			return R.drawable.sleet;
		} else if ("雷阵雨伴有冰雹".equals(weather) || "冰雹".equals(weather)) {
			return R.drawable.with_hail;
		} else {
			return R.drawable.sunny;
		}
	}
	
	private int getWeatherSmallPic(String weather) {
		if("晴".equals(weather)) {
			return R.drawable.sunny2;
		} else if ("多云".equals(weather)) {
			return R.drawable.cloudy2;
		} else if ("阴".equals(weather) || "多云转阴".equals(weather)) {
			return R.drawable.overcast2;
		} else if ("晴转多云".equals(weather)) {
			return R.drawable.partly_cloudy2;
		} else if ("浮尘".equals(weather) || "扬沙".equals(weather) || "霾".equals(weather)) {
			return R.drawable.dust2;
		} else if ("强沙尘暴".equals(weather) || "沙尘暴".equals(weather)) {
			return R.drawable.tornado2;
		} else if ("大雨".equals(weather) || "暴雨".equals(weather)) {
			return R.drawable.heavy_rain2;
		} else if ("中雨".equals(weather)) {
			return R.drawable.moderate_rain2;
		} else if ("小雨".equals(weather)) {
			return R.drawable.light_rain2;
		} else if ("雷阵雨".equals(weather) || "阵雨".equals(weather)) {
			return R.drawable.shower2;
		} else if ("大雪".equals(weather) || "暴雪".equals(weather)) {
			return R.drawable.heavy_snow2;
		} else if ("小雪".equals(weather)) {
			return R.drawable.light_snow2;
		} else if ("雨夹雪".equals(weather)) {
			return R.drawable.sleet2;
		} else if ("雷阵雨伴有冰雹".equals(weather) || "冰雹".equals(weather)) {
			return R.drawable.with_hail2;
		} else {
			return R.drawable.sunny2;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return true;
	}
	
}
