package com.example.reride.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDbHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "reride.db";
	private static final int DATABASE_VERSION = 1;
	private SQLiteDatabase db;

	public MyDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// 删除所有数据表
	private void dropAllTable(SQLiteDatabase db){
		db.execSQL("DROP TABLE IF EXISTS cycling_today");
		db.execSQL("DROP TABLE IF EXISTS cycling_once");
		db.execSQL("DROP TABLE IF EXISTS cycling_total");
	}

	// 创建数据表
	private void createTable(SQLiteDatabase db){
		db.execSQL("CREATE TABLE IF NOT EXISTS cycling_today(uid INTEGER, date INTEGER, speedmax FLOAT, speedavg FLOAT, mileage FLOAT, totaltime INTEGER, altitudemax FLOAT, altitudemin FLOAT)");
		db.execSQL("CREATE INDEX IF NOT EXISTS uid ON cycling_today (uid)");
		db.execSQL("CREATE INDEX IF NOT EXISTS date ON cycling_today (date)");

		db.execSQL("CREATE TABLE IF NOT EXISTS cycling_once(id VARCHAR, uid INTEGER, title VARCHAR, createtime INTEGER, updatetime INTEGER, state SMALLINT, speedmax FLOAT, speedavg FLOAT, mileage FLOAT, totaltime INTEGER, altitudemax FLOAT, altitudemin FLOAT)");
		db.execSQL("CREATE INDEX IF NOT EXISTS id ON cycling_once (id)");
		db.execSQL("CREATE INDEX IF NOT EXISTS uid ON cycling_once (uid)");
		db.execSQL("CREATE INDEX IF NOT EXISTS updatetime ON cycling_once (updatetime)");
		db.execSQL("CREATE INDEX IF NOT EXISTS state ON cycling_once (state)");

		db.execSQL("CREATE TABLE IF NOT EXISTS cycling_total(uid INTEGER, speedmax FLOAT, speedavg FLOAT, mileage FLOAT, totaltime INTEGER)");
		db.execSQL("CREATE INDEX IF NOT EXISTS uid ON cycling_total (uid)");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO 创建数据库后，对数据库的操作
		createTable(db); // 创建数据表
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO 更改数据库版本的操作
		dropAllTable(db); // 删除所有数据表
		onCreate(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db){
		super.onOpen(db);
		// TODO 每次成功打开数据库后首先被执行
	}

	public void openDB() throws SQLException{
		db = this.getReadableDatabase();
	}

	public void closeDB(){
		db.close();
	}

	// 执行查询语句
	public Cursor query(String sql, String[] args) {
		Cursor cursor = db.rawQuery(sql, args);
		return cursor;
	}

	// 执行添加、更新和删除
	public void execSQL(String sql) throws SQLException{
		db.execSQL(sql);
	}

	// 执行添加、更新和删除
	public void execSQL(String sql, Object[] bindArgs) throws SQLException{
		db.execSQL(sql,bindArgs);
	}

}
