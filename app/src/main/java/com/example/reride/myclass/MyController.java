package com.example.reride.myclass;

import android.content.Context;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.reride.bean.User;
import com.example.reride.utils.DateTime;

import java.util.Arrays;
import java.util.Calendar;

public class MyController {

	private static final String TAG = "MyController";
	
	private static final int SAMPLE_RATE_IN_HZ = 44100;
	private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize( SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT );
	
	private AudioRecord mAudioRecord = null;
	private boolean mAudioIsRun = false;

	public boolean go = false;
	
	public static final int NO_START = 0;
	public static final int IS_RUNNING = 1;
	public static final int INIT_FAILED = 2;
	public static final int MIC_OCCUPIED = 3;
	public static final int GPS_CLOSE = 4;
	public static final int GPS_NO_SIGNAL = 5;
	
	public int runState = NO_START;  // 运行状态
	
	public static final int WHEEL_STOP = 0;
	public static final int WHEEL_ROTATION = 1;
	
	public int wheelState = WHEEL_STOP;  // 车轮状态
	
	private static final int OVER_TIME = (int)( 3*1000 );  // 车轮转动超时时间，小于步行时速计算得出
	public static final float ALTITUDE_MAX_INIT = -10000F;
	public static final float ALTITUDE_MIN_INIT = 10000F;
	
	public float maxVolume;
	public float minVolume;
	public float perimeter;
	
	public float speedNow = 0;	// 实时时速, 单位：千米
	public float altitude = MyLocation.ALTITUDE_INIT;  // 实时海拔, 单位：米
    
	private String todayDate = null;  // 保存标示
    public float todaySpeedMax = 0;	// 最大时速, 单位：千米
    public float todaySpeedAvg = 0;	// 平均时速, 单位：千米
	public float todayMileage = 0;  // 骑行里程, 单位：千米
	public long todayTotalTime = 0;	// 骑行时间, 单位：微米
	public float todayAltitudeMax = ALTITUDE_MAX_INIT;  // 最大高度, 单位：米
	public float todayAltitudeMin = ALTITUDE_MIN_INIT;  // 最小高度, 单位：米
	
	private String onceId = null;  // 保存标示
	public String onceTitle = null;
	public float onceSpeedMax = 0;	// 最大时速, 单位：千米
	public float onceSpeedAvg = 0;	// 平均时速, 单位：千米
	public float onceMileage = 0;   // 骑行里程, 单位：千米
	public long onceTotalTime = 0;	// 骑行时间, 单位：微米
	public float onceAltitudeMax = ALTITUDE_MAX_INIT;  // 最大高度, 单位：米
	public float onceAltitudeMin = ALTITUDE_MIN_INIT;  // 最小高度, 单位：米
	
	public float totalSpeedMax = 0;	// 最大时速, 单位：千米
	public float totalSpeedAvg = 0;	// 平均时速, 单位：千米
	public float totalMileage = 0;  // 骑行里程, 单位：千米
	public long totalTotalTime = 0;	// 骑行时间, 单位：微米
	
	private MyModel myModel;
	private User user;
	private MyConfig myConfig;
	
	public int runMode = MyConfig.MODE_GPS;  // 运行模式

	public MyController(Context context, User user){
		this.user = user;
		myModel = new MyModel(context);
		myConfig = new MyConfig(context);
		// 初始化存储的里程数据
		Cursor cursor = null;
		// 结束
		cursor = myModel.onceGetChecked( user.uid );
		/*while( cursor.moveToNext() ){
			onceId = cursor.getString(cursor.getColumnIndex("id"));
			onceTitle = cursor.getString(cursor.getColumnIndex("title"));
			onceSpeedMax = cursor.getFloat(cursor.getColumnIndex("speedmax"));
			onceSpeedAvg = cursor.getFloat(cursor.getColumnIndex("speedavg"));
			onceMileage = cursor.getFloat(cursor.getColumnIndex("mileage"));
			onceTotalTime = cursor.getLong(cursor.getColumnIndex("totaltime"));
			onceAltitudeMax = cursor.getFloat(cursor.getColumnIndex("altitudemax"));
			onceAltitudeMin = cursor.getFloat(cursor.getColumnIndex("altitudemin"));
		}
		cursor.close();*/
		// 全程
		cursor = myModel.totalRead( user.uid );
		while( cursor.moveToNext() ){
			totalSpeedMax = cursor.getFloat(cursor.getColumnIndex("speedmax"));
			totalSpeedAvg = cursor.getFloat(cursor.getColumnIndex("speedavg"));
			totalMileage = cursor.getFloat(cursor.getColumnIndex("mileage"));
			totalTotalTime = cursor.getLong(cursor.getColumnIndex("totaltime"));
		}
		cursor.close();

		// 初始化配置数据
		maxVolume = myConfig.maxVolume;
		minVolume = myConfig.minVolume;
		perimeter = myConfig.wheelPerimeter;
		runMode = myConfig.appRunMode;
	}

	public void start(){
		Log.w(TAG, "start");
		if (mAudioIsRun) {
			Log.i(TAG, "start failure，录音已经在进行中");
			return;
		}
		mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
		if ( mAudioRecord==null ) {
			Log.i(TAG, "mAudioRecord初始化失败");
			runState = INIT_FAILED;  // 状态
			return;
		}
		mAudioIsRun = true;
		new Thread(mRunnable).start();
	}
	
	public void stop(){
		Log.w(TAG, "stop");
		mAudioIsRun = false;
	}
	
	public void release(){
		Log.w(TAG, "release");
		myModel.release();
	}
	
	public void saveData(){
		Log.w(TAG, "saveData");
		// 保存骑行数据
		todayDate = myModel.todaySave( user.uid, todayDate, todaySpeedMax, todaySpeedAvg, todayMileage, todayTotalTime, todayAltitudeMax, todayAltitudeMin );
		myModel.onceSave( user.uid, onceId, onceSpeedMax, onceSpeedAvg, onceMileage, onceTotalTime, onceAltitudeMax, onceAltitudeMin );
		myModel.totalSave( user.uid, totalSpeedMax, totalSpeedAvg, totalMileage, totalTotalTime );
		user.save();  // 保存最后运行时间
	}
	
	// 日期变化时，刷新今日数据
	public void refreshTodayDate(){
		if( todayDate==null )
			return;
		if( todayDate.equals( DateTime.getTodayTimestamp() ) )
			return;
		saveData();
		// 重新初始化
		todayDate = null;
	    todaySpeedMax = 0;
	    todaySpeedAvg = 0;
		todayMileage = 0;
		todayTotalTime = 0;
		todayAltitudeMax = ALTITUDE_MAX_INIT;
		todayAltitudeMin = ALTITUDE_MIN_INIT;
	}
	
	// 单程选择变化时，刷新单程数据
	public void refreshOnce(){
		saveData();
		// 重新初始化
		onceId = null;
		onceTitle = null;
		onceSpeedMax = 0;
		onceSpeedAvg = 0;
		onceMileage = 0;
		onceTotalTime = 0;
		onceAltitudeMax = ALTITUDE_MAX_INIT;
		onceAltitudeMin = ALTITUDE_MIN_INIT;
		// 单程
		Cursor cursor = myModel.onceGetChecked( user.uid );
		while( cursor.moveToNext() ){
			onceId = cursor.getString(cursor.getColumnIndex("id"));
			onceTitle = cursor.getString(cursor.getColumnIndex("title"));
			onceSpeedMax = cursor.getFloat(cursor.getColumnIndex("speedmax"));
			onceSpeedAvg = cursor.getFloat(cursor.getColumnIndex("speedavg"));
			onceMileage = cursor.getFloat(cursor.getColumnIndex("mileage"));
			onceTotalTime = cursor.getLong(cursor.getColumnIndex("totaltime"));
			onceAltitudeMax = cursor.getFloat(cursor.getColumnIndex("altitudemax"));
			onceAltitudeMin = cursor.getFloat(cursor.getColumnIndex("altitudemin"));
		}
		cursor.close();
	}
	
	// GPS模式计算骑行数据
	public void run(){
		if(go) {
			if (speedNow == 0) {
				wheelState = WHEEL_STOP; // 车轮状态
			} else {
				wheelState = WHEEL_ROTATION; // 车轮状态
				// 极速
				if (speedNow > todaySpeedMax) {
					// 今日最高时速
					todaySpeedMax = speedNow;
				}
				if (onceId != null & speedNow > onceSpeedMax) {
					// 单程最高时速
					onceSpeedMax = speedNow;
				}
				if (speedNow > totalSpeedMax) {
					// 总程最高时速
					totalSpeedMax = speedNow;
				}
				// 总里程
				todayMileage += (speedNow / 3600) * ((float)MyLocation.DELAY_MILLIS / 1000.0);  // 单位:千米
				onceMileage += (speedNow / 3600) * (MyLocation.DELAY_MILLIS / 1000);  // 单位:千米
				totalMileage += (speedNow / 3600) * (MyLocation.DELAY_MILLIS / 1000);  // 单位:千米
				// 骑行时间
				todayTotalTime += (long) MyLocation.DELAY_MILLIS;  // 单位:微秒
				onceTotalTime += (long) MyLocation.DELAY_MILLIS;  // 单位:微秒
				totalTotalTime += (long) MyLocation.DELAY_MILLIS;  // 单位:微秒
				// 平均时速
				todaySpeedAvg = todayMileage / todayTotalTime * 1000 * 3600;
				onceSpeedAvg = onceMileage / onceTotalTime * 1000 * 3600;
				totalSpeedAvg = totalMileage / totalTotalTime * 1000 * 3600;
				// 高度
				if (altitude != MyLocation.ALTITUDE_INIT) {
					// 今日
					if (altitude > todayAltitudeMax)
						todayAltitudeMax = altitude;
						onceAltitudeMax = altitude;

					if (altitude < todayAltitudeMin)
						todayAltitudeMin = altitude;
						onceAltitudeMin = altitude;
				}
				// 最后运行时间
				user.lastRunTime = Calendar.getInstance().getTimeInMillis();
			}
		}
		else
			runState = NO_START;
	}

	private Runnable mRunnable = new Runnable() {
		
		@Override
		public void run() {
			
			// 防止某些手机崩溃，原因为麦克风被占用
			try{
				mAudioRecord.startRecording();
            }catch (IllegalStateException e){
                e.printStackTrace();
                Log.i(TAG, "麦克风被其他APP占用，或被360等软件限制录音权限");
                runState = MIC_OCCUPIED;  // 状态
                return;
            }
			
			Log.i(TAG, "录音进行中");
			runState = IS_RUNNING;  // 状态
			
			short[] buffer = new short[BUFFER_SIZE];
			int state = 0;
			int before = 0;
			long timestamp = 0;
			long intervalTime = 0;
			long[] timeArray = new long[3];
			float tempSpeedNow = 0;
			
			while (mAudioIsRun) {
				// r是实际读取的数据长度，一般r会小于BUFFER_SIZE
				int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
				long v = 0;
				// 将buffer内容取出，进行平方和运算
				for (int i = 0; i < buffer.length; i++) {
					v += buffer[i] * buffer[i];
				}
				// 平方和除以数据的长度，得到音量大小
				double mean = v / (double) r;
				float volume = (float)(10 * Math.log10(mean));
				//Log.d(TAG, "VOLUME " + volume + " MAX " + maxVolume + " MIN " + minVolume);
			
				// 记录变化
				before = state;
				if( volume>maxVolume ){
					state = 1;
				}
				if( volume<minVolume ){
					state = 0;
				}
				
				// 计算结果
				if( before!=state && before==1 ){
					if( timestamp==0 ){
						// 忽略第一次计数，因为无法计算间隔时间
					}else{
						intervalTime = Calendar.getInstance().getTimeInMillis()-timestamp;
						if( intervalTime>OVER_TIME ){
							// 忽略间隔时间大于超时时间的计数，超时停车后第一次计数在这里被忽略
						}else{
							tempSpeedNow = 3600*1000 / intervalTime * (perimeter/100) / 1000;	 // 单位:千米
							if( (tempSpeedNow>speedNow+15) || (timeArray[timeArray.length-1]==0 & tempSpeedNow>15) ){
								// 忽略近距离频繁接触感应器出现的多次假计数
							}else{
								wheelState = WHEEL_ROTATION; // 车轮状态
								// 时速
								speedNow = 3600*1000 / avgInterval(intervalTime, timeArray) * (perimeter/100) / 1000;	 // 单位:千米
								// 极速
								if( speedNow>todaySpeedMax ){
									// 今日最高时速
									todaySpeedMax = speedNow;
								}
								if( onceId!=null & speedNow>onceSpeedMax ){
									// 单程最高时速
									onceSpeedMax = speedNow;
								}
								if( speedNow>totalSpeedMax ){
									// 总程最高时速
									totalSpeedMax = speedNow;
								}
								// 总里程
								todayMileage += (perimeter/100) / 1000;  // 单位:千米
								if( onceId!=null )
									onceMileage += (perimeter/100) / 1000;  // 单位:千米
								totalMileage += (perimeter/100) / 1000;  // 单位:千米			
								// 骑行时间
								todayTotalTime += intervalTime;  // 单位:微秒
								if( onceId!=null )
									onceTotalTime += intervalTime;  // 单位:微秒
								totalTotalTime += intervalTime;  // 单位:微秒
								// 平均时速
								todaySpeedAvg = todayMileage/todayTotalTime*1000*3600;
								if( onceId!=null )
									onceSpeedAvg = onceMileage/onceTotalTime*1000*3600;
								totalSpeedAvg = totalMileage/totalTotalTime*1000*3600;
								// 高度
								if( altitude!=MyLocation.ALTITUDE_INIT ){
									// 今日
									if( altitude>todayAltitudeMax )
										todayAltitudeMax = altitude;
									if( altitude<todayAltitudeMin )
										todayAltitudeMin = altitude;
									// 单程
									if( onceId!=null ){
										if( altitude>onceAltitudeMax )
											onceAltitudeMax = altitude;
										if( altitude<onceAltitudeMin )
											onceAltitudeMin = altitude;
									}
								}
								// 最后运行时间
								user.lastRunTime = Calendar.getInstance().getTimeInMillis();
							}
						}
					}
					timestamp = Calendar.getInstance().getTimeInMillis();
				}else{
					if( Calendar.getInstance().getTimeInMillis()-timestamp>OVER_TIME ){
						wheelState = WHEEL_STOP; // 车轮状态
						if( timeArray[0]!=0 )
							Arrays.fill(timeArray, 0);  // 清空数组
					}
				}
			}
			
			Log.i(TAG, "麦克风关闭");
			mAudioRecord.stop();
			mAudioRecord.release();
			mAudioRecord = null;
			runState = NO_START;  // 状态
			wheelState = WHEEL_STOP; // 车轮状态
		}
	};
	
	// 添加间隔时间, 计算平均间隔时间
	private long avgInterval( long time, long[] timeArray ){
		int length = timeArray.length;
		long tmp = 0;
		int cnt = 0;
		for(int i = 0 ; i < length-1 ; i++ ){
			timeArray[i] = timeArray[i+1];
			tmp += timeArray[i+1];
			if( timeArray[i+1]!=0 ){
				cnt++;
			}
		}
		timeArray[length-1] = time;
		tmp += time;
		if( time!=0 ){
			cnt++;
		}
		return tmp/cnt;
	}
	
}

