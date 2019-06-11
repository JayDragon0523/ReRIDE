package com.example.reride.myclass;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import com.example.reride.dao.MyDbHelper;
import com.example.reride.utils.DateTime;
import com.example.reride.utils.RandomString;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class MyModel {
	
	private static final String TAG = "MyDbHelper";
	private MyDbHelper dbHelper;
	
	public MyModel(Context context) {
		dbHelper = new MyDbHelper(context);
		dbHelper.openDB();
	}
	
	public void release(){
		dbHelper.closeDB();
		dbHelper = null;
	}
	
	public Cursor todayRead(int uid){
		String date = DateTime.getTodayTimestamp();
		// 查询数据
		String sql = "SELECT * FROM cycling_today WHERE uid=? AND date=?";
		Cursor cursor = dbHelper.query(sql, new String[] { String.valueOf(uid), date });
		return cursor;
	}
	
	public Cursor todayReadHistory(int uid, int row, int pageRows ){
		// 查询数据
		String sql = "SELECT * FROM cycling_today WHERE uid=? ORDER BY date DESC LIMIT ?,?";
		Cursor cursor = dbHelper.query(sql, new String[] { String.valueOf(uid), String.valueOf(row), String.valueOf(pageRows) });
		return cursor;
	}
	
	public String todaySave(int uid, String date, float speedMax, float speedAvg, float mileage, long totalTime, float altitudeMax, float altitudeMin){
		if( mileage>0 ){
			// 整理数据
			date = date==null?DateTime.getTodayTimestamp():date;
			// 检查是否存在记录
			String sql = "SELECT * FROM cycling_today WHERE uid=? AND date=?";
			Cursor cursor = dbHelper.query(sql, new String[] { String.valueOf(uid), date });
			// 更新数据
			if( !cursor.moveToNext() ){
				// 不存在
				try{
					sql = "INSERT INTO cycling_today ( uid, date, speedmax, speedavg, mileage, totaltime, altitudemax, altitudemin ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )";
					dbHelper.execSQL(sql, new Object[] { uid, date, speedMax, speedAvg, mileage, totalTime, altitudeMax, altitudeMin });
				}catch (SQLException sqle) {
					Log.i(TAG, sqle.getMessage());
				}
			}else{
				// 存在
				try{
					sql = "UPDATE cycling_today SET speedmax=?, speedavg=?, mileage=?, totaltime=?, altitudemax=?, altitudemin=? WHERE uid=? AND date=?";
					dbHelper.execSQL(sql, new Object[] { speedMax, speedAvg, mileage, totalTime, altitudeMax, altitudeMin, uid, date });
				}catch (SQLException sqle) {
					Log.i(TAG, sqle.getMessage());
				}
			}
			cursor.close();
		}
		return date;
	}
	
	public Cursor totalRead(int uid){
		// 查询数据
		String sql = "SELECT * FROM cycling_total WHERE uid=?";
		Cursor cursor = dbHelper.query(sql, new String[] { String.valueOf(uid) });
		return cursor;
	}
	
	public void totalSave(int uid, float speedMax, float speedAvg, float mileage, long totalTime){
		if( mileage==0 ){
			return;
		}
		// 检查是否存在记录
		String sql = "SELECT * FROM cycling_total WHERE uid=?";
		Cursor cursor = dbHelper.query(sql, new String[] { String.valueOf(uid) });
		// 更新数据
		if( !cursor.moveToNext() ){
			// 不存在
			try{
				sql = "INSERT INTO cycling_total ( uid, speedmax, speedavg, mileage, totaltime ) VALUES ( ?, ?, ?, ?, ? )";
				dbHelper.execSQL(sql, new Object[] { uid, speedMax, speedAvg, mileage, totalTime });
			}catch (SQLException sqle) {
				Log.i(TAG, sqle.getMessage());
			}
		}else{
			// 存在
			try{
				sql = "UPDATE cycling_total SET speedmax=?, speedavg=?, mileage=?, totaltime=? WHERE uid=?";
				dbHelper.execSQL(sql, new Object[] { speedMax, speedAvg, mileage, totalTime, uid });
			}catch (SQLException sqle) {
				Log.i(TAG, sqle.getMessage());
			}
		}
		cursor.close();
	}
	
	public boolean onceAdd(int uid, String title){
		// 查询数据
		int state = 0;  // 没有选中的单程
		Cursor cursor = onceGetChecked(uid);
		if( cursor.getCount()>0 )
			state = 1;  // 有选中的单程
		cursor.close();
		// 处理数据
		try{
			String sql = "INSERT INTO cycling_once ( id, uid, title, createtime, updatetime, state, speedmax, speedavg, mileage, totaltime, altitudemax, altitudemin ) VALUES ( ?, ?, ?, ?, ?, ?, 0, 0, 0, 0, "+MyController.ALTITUDE_MAX_INIT+", "+MyController.ALTITUDE_MIN_INIT+" )";
			dbHelper.execSQL(sql, new Object[] { RandomString.generateId(), uid, title, Calendar.getInstance().getTimeInMillis(), Calendar.getInstance().getTimeInMillis(), state==0?1:0 });
		}catch (SQLException sqle) {
			Log.i(TAG, sqle.getMessage());
		}
		return state==0?true:false;
	}
	
	public void onceSave(int uid, String id, float speedMax, float speedAvg, float mileage, long totalTime, float altitudeMax, float altitudeMin ){
		if( mileage==0 ){
			return;
		}
		// 更新数据
		try{
			String sql = "UPDATE cycling_once SET speedmax=?, speedavg=?, mileage=?, totaltime=?, altitudemax=?, altitudemin=? WHERE uid=? AND id=?";
			dbHelper.execSQL(sql, new Object[] { speedMax, speedAvg, mileage, totalTime, altitudeMax, altitudeMin, uid, id });
		}catch (SQLException sqle) {
			Log.i(TAG, sqle.getMessage());
		}
	}
	
	public Cursor onceReadHistory( int uid, int row, int pageRows ){
		// 查询数据
		String sql = "SELECT * FROM cycling_once WHERE uid=? AND updatetime<>0 ORDER BY updatetime DESC LIMIT ?,?";
		Cursor cursor = dbHelper.query(sql, new String[] { String.valueOf(uid), String.valueOf(row), String.valueOf(pageRows) });
		return cursor;
	}
	
	public Cursor onceGetChecked( int uid ){
		// 查询数据
		String sql = "SELECT * FROM cycling_once WHERE uid=? AND state=1";
		Cursor cursor = dbHelper.query(sql, new String[] { String.valueOf(uid) });
		return cursor;
	}
	
	public void onceSetChecked( int uid, String id ){
		// 处理数据
		try{
			String sql = "UPDATE cycling_once SET state=0 WHERE uid=?";
			dbHelper.execSQL(sql, new Object[] { uid });
		}catch (SQLException sqle) {
			Log.i(TAG, sqle.getMessage());
		}
		if( id==null )
			return;
		try{
			String sql = "UPDATE cycling_once SET state=1, updatetime=? WHERE uid=? AND id=?";
			dbHelper.execSQL(sql, new Object[] { Calendar.getInstance().getTimeInMillis(), uid, id });
		}catch (SQLException sqle) {
			Log.i(TAG, sqle.getMessage());
		}
	}
	
	public void onceDelete( int uid, String id ){
		// 处理数据
		try{
			String sql;
			// uid为空时直接删除
			if( uid==0 ){
				sql = "DELETE FROM cycling_once WHERE uid=? AND id=?";
			}else{
				sql = "UPDATE cycling_once SET updatetime=0, state=0 WHERE uid=? AND id=?";
			}
			dbHelper.execSQL(sql, new Object[] { uid, id });
		}catch (SQLException sqle) {
			Log.i(TAG, sqle.getMessage());
		}
	}
	
	public void onceDeleteHide( int uid ){
		// 处理数据
		try{
			String sql = "DELETE FROM cycling_once WHERE uid=? AND updatetime=0";
			dbHelper.execSQL(sql, new Object[] { uid });
		}catch (SQLException sqle) {
			Log.i(TAG, sqle.getMessage());
		}
	}
	
	// 是否存在没有用户的数据
	public boolean existUnmanned(){
		boolean exist = false;
		String sql = null;
		Cursor cursor = null;
		// 今日
		sql = "SELECT * FROM cycling_today WHERE uid=0";
		cursor = dbHelper.query(sql, null);
		if( cursor.getCount()>0 )
			exist = true;
		// 单程
		sql = "SELECT * FROM cycling_once WHERE uid=0";
		cursor = dbHelper.query(sql, null);
		if( cursor.getCount()>0 )
			exist = true;
		// 总程
		sql = "SELECT * FROM cycling_total WHERE uid=0";
		cursor = dbHelper.query(sql, null);
		if( cursor.getCount()>0 )
			exist = true;
		return exist;
	}
	
	// 将没有用户的数据变为有用户的数据
	public void changeUnmannedTo( int uid ){
		String sql = null;
		Cursor cursor = null;
		// 今日
		String inStr = "";
		sql = "SELECT date, COUNT(date) FROM cycling_today WHERE uid=0 OR uid=? GROUP BY date HAVING COUNT(date)>1";
		cursor = dbHelper.query(sql, new String[] { String.valueOf(uid) });
		while( cursor.moveToNext() ){
			inStr += cursor.getString(cursor.getColumnIndex("date"))+((cursor.getPosition()==cursor.getCount()-1)?"":",");
		}
		try{
			// 删除日期重复的数据
			sql = "DELETE FROM cycling_today WHERE uid=0 AND date IN (" + inStr +")";
			dbHelper.execSQL(sql, new Object[]{});
		}catch (SQLException sqle) {
			Log.i(TAG, sqle.getMessage());
		}
		try{
			// 移转不重复的数据
			sql = "UPDATE cycling_today SET uid=? WHERE uid=0";
			dbHelper.execSQL(sql, new Object[] { uid });
		}catch (SQLException sqle) {
			Log.i(TAG, sqle.getMessage());
		}
		// 单程
		try{
			sql = "UPDATE cycling_once SET uid=?,state=0 WHERE uid=0";
			dbHelper.execSQL(sql, new Object[] { uid });
		}catch (SQLException sqle) {
			Log.i(TAG, sqle.getMessage());
		}
		// 总程
		reviseTotal(uid);
		try{
			// 删除之前的总程数据
			sql = "DELETE FROM cycling_total WHERE uid=0";
			dbHelper.execSQL(sql, new Object[]{});
		}catch (SQLException sqle) {
			Log.i(TAG, sqle.getMessage());
		}
	}
	
	// 根据“今日”数据重新生成“总程”数据
	public void reviseTotal( int uid ){
		String sql = null;
		Cursor cursor = null;
		sql = "SELECT MAX(speedmax) AS speedmax, SUM(mileage) AS mileage, SUM(totaltime) AS totaltime FROM cycling_today WHERE uid=?";
		cursor = dbHelper.query(sql, new String[] { String.valueOf(uid) });
		float speedMax = 0.0F, speedAvg=0.0F, mileage = 0.0F;
		long totalTime = 0L;
		while( cursor.moveToNext() ){
			speedMax = cursor.getFloat(cursor.getColumnIndex("speedmax"));
			mileage = cursor.getFloat(cursor.getColumnIndex("mileage"));
			totalTime = cursor.getLong(cursor.getColumnIndex("totaltime"));
		}
		speedAvg = mileage/totalTime*1000*3600;
		totalSave(uid, speedMax, speedAvg, mileage, totalTime);
	}
	
	// 将本地数据转换为JSON
	public String getLocalDataByJSON( int uid ){
		String json = "";
		if( uid==0 ){
			Log.e(TAG, "getLocalDataByJSON: uid not null");
			return json;
		}
		String sql;
		Cursor cursor;
		// 今日
		sql = "SELECT * FROM cycling_today WHERE uid=? ORDER BY date DESC";
		cursor = dbHelper.query(sql, new String[] { String.valueOf(uid) });
		String todayJson = "";
		while( cursor.moveToNext() ){
			String date = cursor.getString(cursor.getColumnIndex("date"));
			String speedmax = cursor.getString(cursor.getColumnIndex("speedmax"));
			String speedavg = cursor.getString(cursor.getColumnIndex("speedavg"));
			String mileage = cursor.getString(cursor.getColumnIndex("mileage"));
			String totaltime = cursor.getString(cursor.getColumnIndex("totaltime"));
			String altitudemax = cursor.getString(cursor.getColumnIndex("altitudemax"));
			String altitudemin = cursor.getString(cursor.getColumnIndex("altitudemin"));
			todayJson += "{" +
					"\"uid\":"+uid+"," +
					"\"date\":"+date+"," +
					"\"speedmax\":"+speedmax+"," +
					"\"speedavg\":"+speedavg+"," +
					"\"mileage\":"+mileage+"," +
					"\"totaltime\":"+totaltime+"," +
					"\"altitudemax\":"+altitudemax+"," +
					"\"altitudemin\":"+altitudemin +
					"}" + (cursor.getPosition()==cursor.getCount()-1?"":",");
		}
		json += "\"today\":["+todayJson+"],";
		// 单程
		sql = "SELECT * FROM cycling_once WHERE uid=? ORDER BY updatetime DESC";
		cursor = dbHelper.query(sql, new String[] { String.valueOf(uid) });
		String onceJson = "";
		while( cursor.moveToNext() ){
			String id = cursor.getString(cursor.getColumnIndex("id"));
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String createtime = cursor.getString(cursor.getColumnIndex("createtime"));
			String updatetime = cursor.getString(cursor.getColumnIndex("updatetime"));
			String state = cursor.getString(cursor.getColumnIndex("state"));
			String speedmax = cursor.getString(cursor.getColumnIndex("speedmax"));
			String speedavg = cursor.getString(cursor.getColumnIndex("speedavg"));
			String mileage = cursor.getString(cursor.getColumnIndex("mileage"));
			String totaltime = cursor.getString(cursor.getColumnIndex("totaltime"));
			String altitudemax = cursor.getString(cursor.getColumnIndex("altitudemax"));
			String altitudemin = cursor.getString(cursor.getColumnIndex("altitudemin"));
			onceJson += "{" +
					"\"id\":\""+id+"\"," +
					"\"uid\":"+uid+"," +
					"\"title\":\""+title+"\"," +
					"\"createtime\":"+createtime+"," +
					"\"updatetime\":"+updatetime+"," +
					"\"state\":"+state+"," +
					"\"speedmax\":"+speedmax+"," +
					"\"speedavg\":"+speedavg+"," +
					"\"mileage\":"+mileage+"," +
					"\"totaltime\":"+totaltime+"," +
					"\"altitudemax\":"+altitudemax+"," +
					"\"altitudemin\":"+altitudemin +
					"}" + (cursor.getPosition()==cursor.getCount()-1?"":",");
		}
		json += "\"once\":["+onceJson+"]";
		return "{"+json+"}";
	}
	
	// 同步成功后将数据更新到本地
	public void jsonUpdateLocal( JSONArray today, JSONArray once, JSONArray total ){
		JSONArray temp;
		String sql;
		// 今日
		temp = today;
		for (int i = 0; i < temp.length(); i++) {
			try {
				JSONObject obj = temp.getJSONObject(i);
				try{
					if( obj.getString("action").equals("insert") ){
						sql = "INSERT INTO cycling_today ( uid, date, speedmax, speedavg, mileage, totaltime, altitudemax, altitudemin ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )";
						dbHelper.execSQL(sql, new Object[] { obj.getInt("uid"), obj.getLong("date"), obj.getDouble("speedmax"), obj.getDouble("speedavg"), obj.getDouble("mileage"), obj.getLong("totaltime"), obj.getDouble("altitudemax"), obj.getDouble("altitudemin") });
					}
					if( obj.getString("action").equals("update") ){
						sql = "UPDATE cycling_today SET speedmax=?, speedavg=?, mileage=?, totaltime=?, altitudemax=?, altitudemin=? WHERE uid=? AND date=?";
						dbHelper.execSQL(sql, new Object[] { obj.getDouble("speedmax"), obj.getDouble("speedavg"), obj.getDouble("mileage"), obj.getLong("totaltime"), obj.getDouble("altitudemax"), obj.getDouble("altitudemin"), obj.getInt("uid"), obj.getLong("date") });
					}
				}catch (SQLException sqle) {
					Log.i(TAG, sqle.getMessage());
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// 单程
		temp = once;
		for (int i = 0; i < temp.length(); i++) {
			try {
				JSONObject obj = temp.getJSONObject(i);
				try{
					if( obj.getString("action").equals("insert") ){
						sql = "INSERT INTO cycling_once ( id, uid, title, createtime, updatetime, state, speedmax, speedavg, mileage, totaltime, altitudemax, altitudemin ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
						dbHelper.execSQL(sql, new Object[] { obj.getString("id"), obj.getInt("uid"), obj.getString("title"), obj.getLong("createtime"), obj.getLong("updatetime"), obj.getInt("state"), obj.getDouble("speedmax"), obj.getDouble("speedavg"), obj.getDouble("mileage"), obj.getLong("totaltime"), obj.getDouble("altitudemax"), obj.getDouble("altitudemin") });
					}
					if( obj.getString("action").equals("update") ){
						sql = "INSERT INTO cycling_once SET uid=?, title=?, createtime=?, updatetime=?, state=?, speedmax=?, speedavg=?, mileage=?, totaltime=?, altitudemax=?, altitudemin=? WHERE id=?";
						dbHelper.execSQL(sql, new Object[] { obj.getString("id"), obj.getInt("uid"), obj.getString("title"), obj.getLong("createtime"), obj.getLong("updatetime"), obj.getInt("state"), obj.getDouble("speedmax"), obj.getDouble("speedavg"), obj.getDouble("mileage"), obj.getLong("totaltime"), obj.getDouble("altitudemax"), obj.getDouble("altitudemin") });
					}
				}catch (SQLException sqle) {
					Log.i(TAG, sqle.getMessage());
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// 总程
		temp = total;
		if( temp.length()==1 ){
			try {
				JSONObject obj = temp.getJSONObject(0);
				totalSave( obj.getInt("uid"), (float)obj.getDouble("speedmax"), (float)obj.getDouble("speedavg"), (float)obj.getDouble("mileage"), obj.getLong("totaltime") );
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
}
