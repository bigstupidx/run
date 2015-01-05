package com.run.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Base64;
import android.util.Log;

import com.run.MyActivity;
import com.run.R;

public class Utils extends MyActivity {

	private static final String TAG = "Util";	
	
	/**
	 * 根据一个网络连接(String)获取bitmap图像,并本地保存
	 * 
	 * @param imageUri
	 * @return 
	 * @throws MalformedURLException
	 */
	public static String getbitmap(String imageUrl) {	
		String iconString = null;
		// 判断文件夹是否存在,不存在就创建
		File fileDir = new File(DIR);			
		if(!fileDir.exists()) {
			fileDir.mkdir();
		}
		try {			
			File file = new File(USER_ICON);
			FileOutputStream out = new FileOutputStream(file);		
			ByteArrayOutputStream arrayOut = new ByteArrayOutputStream();			
			
			URL url = new URL(imageUrl);
			HttpURLConnection connect = (HttpURLConnection) url.openConnection();
			connect.setDoInput(true);
			connect.connect();
			InputStream in = connect.getInputStream();
			Bitmap bitmap = BitmapFactory.decodeStream(in);			
			if(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, arrayOut);	
				iconString = new String(Base64.encode(arrayOut.toByteArray(), Base64.DEFAULT));
				arrayOut.close();
				in.close();
				out.close();
			} else {
				Log.i(TAG, "image download failed");
				return "";
			}
			Log.i(TAG, "image download finished " + imageUrl);
		} catch(Exception e) {
			Log.i(TAG, "image download failed");
			return "";
		}
		return iconString;
	}
	
	/**
	 * 将一张图片转换成圆形
	 * 
	 * @param sourceimage
	 * @param circle image size
	 * @return 
	 * @throws MalformedURLException
	 */
	public static Bitmap createCircleImage(Bitmap source, int min)
    {  
        final Paint paint = new Paint();  
        paint.setAntiAlias(true);  
        Bitmap target = Bitmap.createBitmap(min, min, Config.ARGB_8888);  
        /** 
         * 产生一个同样大小的画布 
         */  
        Canvas canvas = new Canvas(target);  
        /** 
         * 首先绘制圆形 
         */  
        canvas.drawCircle(min/2, min/2, min/2, paint);  
        /** 
         * 使用SRC_IN
         */  
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));  
        /** 
         * 绘制图片
         */  
        canvas.drawBitmap(source, 0, 0, paint);  
        return target;  
    }
	
	/**
	 * 获取用户头像
	 * @param imageString  头像数据
	 * @return 用户头像
	 */
	public static Bitmap getUserIcon(String imageString) {
		Bitmap bitmap = null;
		File file = new File(USER_ICON);
		// 如果头像存在于本地文件夹中
		if (file.exists()) {
			bitmap = BitmapFactory.decodeFile(USER_ICON);
		} else {
			// 头像不存在，查找头像数据(存在用户将头像文件删除的情况)				
			byte[] imageByte = Base64.decode(imageString.getBytes(), Base64.DEFAULT);
			ByteArrayInputStream arrayin = new ByteArrayInputStream(imageByte);
			bitmap = BitmapFactory.decodeStream(arrayin);			
		}
		// 将图片大小进行调整
		bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
		return bitmap;
	}
	
}
