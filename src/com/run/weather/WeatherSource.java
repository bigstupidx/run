package com.run.weather;

import android.os.AsyncTask;
import android.util.Log;

public class WeatherSource {
	
	private String mCity;
	private boolean mIsFinish;
	private String mResult;
	private OnFinishListener mFinishListener = null;

	public WeatherSource(String city) {
		mCity = city;		
		new GetResource().execute();
	}
	
	public boolean getFinish() {
		return mIsFinish;
	}
	
	public String getResult() {
		return mResult;
	}
	
	class GetResource extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {
			mIsFinish = false;			
			try {
				String path = "http://api.map.baidu.com/telematics/v3/weather?location="
						+ mCity
						+ "&output=xml&ak=CfQGtTn0bPbIqHO1LkNq8cwm";
				return NetWorkUtils.HttpGet(path);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {			
			super.onPostExecute(result);
			mResult = result;
			mIsFinish = true;
			if(mFinishListener != null) {
				mFinishListener.OnFinish(result);
			}
			Log.i("weather", result);
		}
		
	}
	
	public void setOnFinishListener(OnFinishListener finishlistener) {
		mFinishListener = finishlistener;
	}
	
	public interface OnFinishListener {
		public void OnFinish(String result);
	}
}
