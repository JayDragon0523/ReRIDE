package com.example.reride;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.reride.R;
import com.example.reride.widget.WebViewProgress;

public class WebActivity extends Activity {

	@SuppressWarnings("unused")
	private static final String TAG = "WebActivity";
	private WebViewProgress webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);

		String url = getIntent().getStringExtra("url");
		TextView title = (TextView) findViewById(R.id.titlebar_title);
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.titlebar_progressbar);
		webView = (WebViewProgress) findViewById(R.id.activity_web_webview);
		webView.setTitle(title);
		webView.setProgressBar(progressBar);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(url);

		//关闭按钮
		ImageButton close = (ImageButton) findViewById(R.id.titlebar_close);
		close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		//刷新按钮
		ImageButton refresh = (ImageButton) findViewById(R.id.titlebar_refresh);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				webView.reload();
			}
		});
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {		
		if( event.getKeyCode()==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN ) {//点击返回按钮的时候判断有没有上一页
			if( webView.canGoBack() ){
				webView.goBack();//返回上一页面
				return true;
			}else{
				finish();
			}
		}
		return super.dispatchKeyEvent(event);
	}

}
