package com.example.reride.system;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.reride.myclass.MyConfig;
import com.example.reride.myclass.MyController;
import com.example.reride.myclass.MyLocation;
import com.example.reride.myclass.MySync;


public class MyService extends Service {

	private static final String TAG = "MyService";

	private IBinder LocalBinder = new MyService.LocalBinder();
	public HeadsetPlugReceiver hpReceiver = new HeadsetPlugReceiver();
	
	private MyApp myApp;
	
	public MyController mCont;  // ��������
	public MyLocation mLoc;  // ��λ��
	public MySync mSync;  // ͬ����
	
	// ����������̳�Binder
	public class LocalBinder extends Binder {
		// ���ر��ط���
		public MyService getService() {
			return MyService.this;
		}
	}
	
	// ��������������Ķ���
	public void destroyController(){
		mCont.stop();
		mCont.saveData();  // ��������
		mCont.release();
	}
	
	// ��������������Ķ���
	public void createController(){
		mCont = new MyController(this, myApp.user);
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "Service onCreate");
		super.onCreate();
		
		myApp = (MyApp)this.getApplication();
		registerReceiver(hpReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		
		createController();
		mLoc = new MyLocation(this, mCont);
		mSync = new MySync(this, myApp);
		
		if( mCont.runMode== MyConfig.MODE_GPS ){
			mLoc.start();
		}
		if( myApp.user.lastRunTime>myApp.user.lastSyncTime ){
			//mSync.start();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Service onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "Service onBind");
		return LocalBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "SeiviceonUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Service onDestroy");
		super.onDestroy();
		unregisterReceiver(hpReceiver);
		destroyController();
		mLoc.stop();
		mSync.release();
		myApp.GPSOpenTip = false;  // ��ʼ��GPS����ʾ
	}
	
    // ����������Ͱγ�
    public class HeadsetPlugReceiver extends BroadcastReceiver {
    	
    	private static final String TAG = "HeadsetPlugReceiver";
    	
    	public static final int PULLOUT = 0;
    	public static final int INSERT = 1;
    	
    	public int status = PULLOUT;
    	
    	@Override
    	public void onReceive(Context context, Intent intent) {
			if ( intent.hasExtra("state") ){
				if( intent.getIntExtra("state", 0)==PULLOUT ){
					if( status!=2 ){
						Log.i(TAG, "����û������");
						if( mCont.runMode==MyConfig.MODE_HARDWARE ){
							// ֹͣ����
							mCont.stop();
							mLoc.stop();
						}
						status = 2;
					}
				}else if( intent.getIntExtra("state", 0)==INSERT ){
					if( status!=1 ){
						Log.i(TAG, "�����Ѿ�����");
						if( mCont.runMode==MyConfig.MODE_HARDWARE ){
							// ��������
							mCont.start();
							mLoc.start();
						}
						status = 1;
					}
				}
			}
    	}

    }

}
