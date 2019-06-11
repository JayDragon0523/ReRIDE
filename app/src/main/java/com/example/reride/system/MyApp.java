package com.example.reride.system;

import android.app.Application;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.example.reride.bean.User;

import java.util.HashMap;

public class MyApp extends Application {

	private static final String TAG = "MyApp";
	
	private HashMap<String,Object> map = new HashMap<String,Object>();
	
	public MyService localService;
	public User user;
	
	public boolean GPSOpenTip = false;  // GPS开启提示
	
	// Activity之间传递数据
	public void putObject( String key, Object value ) {
		map.put(key, value);
	}
	public Object getObject( String key ) {
		return map.get(key);
	}
	public void clearObject() {
		map.clear();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		SDKInitializer.initialize(this);
		user = new User(getApplicationContext());
	}

	@Override
	public void onTerminate() {
		Log.d(TAG, "onTerminate");
		super.onTerminate();
		user.save();  // 保存用户数据
		clearObject();  // 清除传递数据
	}
	
	@Override
	public void onLowMemory() {
		Log.d(TAG, "onLowMemory");
		super.onLowMemory();
	}

}
