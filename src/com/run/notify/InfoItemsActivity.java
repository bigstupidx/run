package com.run.notify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;

import com.run.BaseActivity;
import com.run.R;

@SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" })
public class InfoItemsActivity extends BaseActivity {
	
	private WebView mWebView;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    
        setContentView(R.layout.activity_notification);
        
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");       
        Log.i("refresh", "get: " + url);
        mWebView = (WebView) findViewById(R.id.webview);
//        mWebView.addJavascriptInterface(new InJavaScriptLocalObj(), "local_obj");
        // webview 设置支持javascript
        mWebView.getSettings().setJavaScriptEnabled(true);        
//        mWebView.setWebViewClient(new myWebViewClient());
        mWebView.loadUrl(url);
        
	}
	/*
	class myWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
//			Toast.makeText(InfoItemsActivity.this, "网页加载完成", 0).show();
//			view.loadUrl("javascript:window.handler.show(document.body.innerHTML);");
			view.loadUrl("javascript:window.local_obj.showSource('<head>'+" 
                    + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
			super.onPageFinished(view, url);
		}
		
	}
	
	final class InJavaScriptLocalObj { 
        public void showSource(String html) { 
            System.out.println("====>html="+html); 
        } 
    } 
	*/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
}
