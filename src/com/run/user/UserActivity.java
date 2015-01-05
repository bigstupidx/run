package com.run.user;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.run.BaseActivity;
import com.run.R;
import com.run.utils.Utils;

public class UserActivity extends BaseActivity implements OnClickListener {

	private TextView tvSex;
	private ImageView mSexImage;
	private ImageView usrImage;
	private TextView usrName;
	private boolean isLoginSuccess;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_user);
		
		tvSex = (TextView) findViewById(R.id.sex);
		mSexImage = (ImageView) findViewById(R.id.sex_icon);	
		usrImage = (ImageView) findViewById(R.id.user_icon);
		usrName = (TextView) findViewById(R.id.user_name);
		
		tvSex.setOnClickListener(this);
		
		SharedPreferences share = getSharedPreferences("userinfo", MODE_PRIVATE);
		isLoginSuccess = share.getBoolean("ret", false);
		usrName.setText(share.getString("nickname", "Runner"));
				
		if (isLoginSuccess) {
			String imageString = share.getString("figureurl", "");
			Bitmap bitmap = Utils.getUserIcon(imageString);
	        bitmap = Utils.createCircleImage(bitmap, 200);
	        usrImage.setBackground(new BitmapDrawable(getResources(), bitmap));		
		} else {
			usrImage.setBackgroundResource(R.drawable.bg_no_head);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sex:
			String text = tvSex.getText().toString();
			if(text.equals("男")) {
				tvSex.setText(R.string.female);
				mSexImage.setImageResource(R.drawable.icon_sex_girl);
			} else if (text.equals("女")) {
				tvSex.setText(R.string.male);
				mSexImage.setImageResource(R.drawable.icon_sex_boy);
			}			
			break;
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
