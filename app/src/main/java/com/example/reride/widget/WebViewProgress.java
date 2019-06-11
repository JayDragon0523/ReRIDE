package com.example.reride.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WebViewProgress extends WebView {
	
	@SuppressWarnings("unused")
    private static final String TAG = "ProgressWebView";
    private ProgressBar progressBar;
    private TextView titleView;

    public WebViewProgress(final Context context, AttributeSet attrs) {
        super(context, attrs);
        setWebChromeClient(new WebChromeClient(){//加载进度
            //加载进度回调
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {//页面加载完成
                    progressBar.setVisibility(GONE);
                } else {//正在加载
                    if ( progressBar.getVisibility()==GONE )
                        progressBar.setVisibility(VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }
        });
        setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.startsWith("http")) {
                    // 淘宝、天猫等APP链接
                    try {
                        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        // 没有安装对应的APP时, 输出错误
                        e.printStackTrace();
                    }
                }else{
                    // http链接
                    loadUrl(url);
                }
                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String title = view.getTitle();
                if( title!=null && !title.equals("") )
                    titleView.setText(title);
            }
        });
    }

    public void setTitle(TextView titleView){
        this.titleView = titleView;
    }

    public void setProgressBar(ProgressBar progressBar){
        this.progressBar = progressBar;
    }

}
