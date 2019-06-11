package com.example.reride.myclass;

import android.content.Context;
import android.content.SharedPreferences;

public class MyConfig {

	private Context context;

	public static final int MODE_GPS = 1;  // GPS模式
	public static final int MODE_HARDWARE = 2;  // 硬件模式

	public String wheelSize = "26 × 1.95";
	public float wheelPerimeter = 205.0F;  // 单位：厘米
	public float maxVolume = 62;
	public float minVolume = 45;
	public int appLatestVersion = 0;  // 服务器下载
	public int appLocalVersion = 0;
	public boolean appIsFirstStart = true;
	public boolean displayLongBright = true;
	public int appRunMode = MODE_GPS;  // 运行模式

	public MyConfig( Context context ) {
		this.context = context;
		read();
	}

	public void read(){
		SharedPreferences config = context.getSharedPreferences("app_config", 0);
		wheelSize = config.getString("WHEEL_SIZE", wheelSize);
		wheelPerimeter = config.getFloat("WHEEL_PERIMETER", wheelPerimeter);
		maxVolume = config.getFloat("MAX_VOLUME", maxVolume);
		minVolume = config.getFloat("MIN_VOLUME", minVolume);
		appLatestVersion = config.getInt("APP_LATEST_VERSION", appLatestVersion);
		appLocalVersion = config.getInt("APP_LOCAL_VERSION", appLocalVersion);
		appIsFirstStart = config.getBoolean("APP_IS_FIRST_START", appIsFirstStart);
		displayLongBright = config.getBoolean("DISPLAY_LONG_BRIGHT", displayLongBright);
		appRunMode = config.getInt("APP_RUN_MODE", appRunMode);
	}

	public void save(){
		SharedPreferences config = context.getSharedPreferences("app_config", 0);
		SharedPreferences.Editor editor = config.edit();
		editor.putString("WHEEL_SIZE", wheelSize);
		editor.putFloat("WHEEL_PERIMETER", wheelPerimeter);
		editor.putFloat("MAX_VOLUME", maxVolume);
		editor.putFloat("MIN_VOLUME", minVolume);
		editor.putInt("APP_LATEST_VERSION", appLatestVersion);
		editor.putInt("APP_LOCAL_VERSION", appLocalVersion);
		editor.putBoolean("APP_IS_FIRST_START", appIsFirstStart);
		editor.putBoolean("DISPLAY_LONG_BRIGHT", displayLongBright);
		editor.putInt("APP_RUN_MODE", appRunMode);
		editor.commit();
	}
	
}
