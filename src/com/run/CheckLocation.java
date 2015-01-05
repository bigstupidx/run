package com.run;

import com.run.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CheckLocation extends Activity implements OnClickListener {

	private Button btnLocation;
	private Button btnReflash;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check);	
		ActivityCollector.addActivity(this);
		
		btnLocation = (Button) findViewById(R.id.setLocation);
		btnReflash = (Button) findViewById(R.id.reflash);
		
		btnLocation.setOnClickListener(this);
		btnReflash.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setLocation:
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
			break;
			
		case R.id.reflash:
			if(isOpenLocationService(getApplicationContext())) {
				finish();
			}
			break;
		}
	}
	
	private boolean isOpenLocationService(Context context) {
		LocationManager location = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		// 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
		// 判断GPS有没有打开
		boolean gps = location.isProviderEnabled(LocationManager.GPS_PROVIDER);
		// 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
		boolean network = location.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if(gps || network) {
			return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		ActivityCollector.finishAll();
	}
	
}
