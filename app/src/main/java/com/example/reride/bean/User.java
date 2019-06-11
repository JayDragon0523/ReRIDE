package com.example.reride.bean;

import android.content.Context;
import android.content.SharedPreferences;

public class User {
	
	private Context context;
	
	public int uid = 0;
	
	public String openId = null;
	public String accessToken = null;
	public String expiresIn = null;	
	
	public String avatarUrl = null;
	
	public String nickname = null;
	public String gender = null;
	public String province = null;
	public String city = null;
	
	public long lastSyncTime = 0;
	public long lastRunTime = 0;
	
	public User(Context context ) {
		this.context = context;
		read();
	}
	
	public void empty(){
		uid = 0;
		openId = null;
		accessToken = null;
		expiresIn = null;
		avatarUrl = null;
		nickname = null;		
		gender = null;
		province = null;
		city = null;
		lastSyncTime = 0;
		lastRunTime = 0;
		save();
	}
	
	public void read(){
		SharedPreferences user = context.getSharedPreferences("app_user", 0);
		uid = user.getInt("UID", uid);
		openId = user.getString("OPEN_ID", openId);
		accessToken = user.getString("ACCESS_TOKEN", accessToken);
		expiresIn = user.getString("EXPIRES_IN", expiresIn);
		avatarUrl = user.getString("AVATAR_URL", avatarUrl);
		nickname = user.getString("NICKNAME", nickname);		
		gender = user.getString("GENDER", gender);
		province = user.getString("PROVINCE", province);
		city = user.getString("CITY", city);
		lastSyncTime = user.getLong("LAST_SYNC_TIME", lastSyncTime);
		lastRunTime = user.getLong("LAST_RUN_TIME", lastRunTime);
	}
	
	public void save(){
		SharedPreferences user = context.getSharedPreferences("app_user", 0);
		SharedPreferences.Editor editor = user.edit();
		editor.putInt("UID", uid);
		editor.putString("OPEN_ID", openId);
		editor.putString("ACCESS_TOKEN", accessToken);
		editor.putString("EXPIRES_IN", expiresIn);
		editor.putString("AVATAR_URL", avatarUrl);
		editor.putString("NICKNAME", nickname);		
		editor.putString("GENDER", gender);
		editor.putString("PROVINCE", province);
		editor.putString("CITY", city);
		editor.putLong("LAST_SYNC_TIME", lastSyncTime);
		editor.putLong("LAST_RUN_TIME", lastRunTime);
		editor.commit();
	}
	
}
