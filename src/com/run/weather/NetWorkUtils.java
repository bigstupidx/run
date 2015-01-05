package com.run.weather;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class NetWorkUtils {

	/**
	 * 通过Get获取网页内容
	 * 
	 * @param url
	 *            地址只能传入.xml后缀
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String HttpGet(String url) throws ClientProtocolException, 
		IOException {
		// 新建一个默认连接
		DefaultHttpClient client = new DefaultHttpClient();
		// 新建一个GET方法
		HttpGet get = new HttpGet(url);
		// 得到网络回应
		HttpResponse response = client.execute(get);
		// 获取网址源代码
		String content = null;
		
		// 判断回应是否正确
		if(200 == response.getStatusLine().getStatusCode()) {
			// 读取内容
			InputStream in = response.getEntity().getContent();
			byte[] buffer = new byte[1024];
			int length = 0;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while((length = in.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}
			// 把字节流转换成UTF-8格式的字符串
			content = new String(out.toByteArray(), "utf-8");
		}
		return content;
		
	}
}
