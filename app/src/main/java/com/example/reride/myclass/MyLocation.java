package com.example.reride.myclass;

import android.content.Context;
import android.location.LocationManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

public class MyLocation {
	
	private static final String TAG = "MyLocation";
	
	private Context context;
	private MyController mCont;
	
	private LocationClient mLocationClient = null;
	private BDLocationListener myListener = new MyLocationListener();
	
	public static final int DELAY_MILLIS = 1000;  // GPS刷新间隔时间
	
	public static final float ALTITUDE_INIT = 88888F;  // 高度初始值
	
	public float altitude = ALTITUDE_INIT;  // 高度
	public float speed = 0.0F;  // 速度
	public int satelliteNumber = 0;  // 卫星数
	public float direction = 0.0F;  // 方向
	
	public MyLocation(Context context, MyController mCont) {
		this.context = context;
		this.mCont = mCont;
		mLocationClient = new LocationClient(context);     //声明LocationClient类
		initLocation();
	    mLocationClient.registerLocationListener( myListener );    //注册监听函数
	}
	
	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			requestLocation();
			handler.postDelayed(this, DELAY_MILLIS);
		}
	};
	
	public void setController(MyController mCont){
		this.mCont = mCont;
	}
	
	public void start(){
		Log.w(TAG, "start");
		if( mLocationClient.isStarted() ){
			Log.w(TAG, "start failure，已经在运行中");
			return;
		}
		mLocationClient.start();
		handler.removeCallbacks(runnable);  // 关闭定时刷新
		handler.postDelayed(runnable, DELAY_MILLIS);  // 开启定时刷新
	}

	public void stop(){
		Log.w(TAG, "stop");
		handler.removeCallbacks(runnable);  // 关闭定时刷新
		if( mLocationClient.isStarted() )
			mLocationClient.stop();
	}
	
	private void requestLocation(){
		if( mLocationClient.isStarted() ){
			if( isOpenGPS(context) ){
				mLocationClient.requestLocation();
			}else{				
				// 恢复初始值
				if( altitude!=ALTITUDE_INIT ){
					altitude = ALTITUDE_INIT;
					mCont.altitude = altitude;
				}
				// GPS模式
    			if( mCont.runMode==MyConfig.MODE_GPS ){
    				mCont.runState = MyController.GPS_CLOSE;
    			}
			}
		}
	}
	
	private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy); //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll"); //可选，默认gcj02，设置返回的定位结果坐标系
        int span = 0;
        option.setScanSpan(span); //可选，默认0，即仅定位一次，设置发起定位请求的间隔，需要大于等于1000ms才是有效的
        option.setIsNeedAddress(false); //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true); //可选，默认false,设置是否使用gps
        option.setLocationNotify(false); //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(false); //可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false); //可选，默认false，设置是否收集CRASH信息，默认收集
        mLocationClient.setLocOption(option);
    }
	
	public static final boolean isOpenGPS(Context context) {
        LocationManager locationManager  = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快） 
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(gps){
			Log.e(TAG, "isOpenGPS: gps true" );
			return true;
        }
        return false;
    }
	
	public class MyLocationListener implements BDLocationListener {
		private static final String TAG = "MyLocationListener";
        @Override
        public void onReceiveLocation(BDLocation location) {

			//获取纬度信息
			double latitude = location.getLatitude();
			//获取经度信息
			double longitude = location.getLongitude();
			Log.e(TAG, "onReceiveLocation: latitude:"+latitude );
			Log.e(TAG, "onReceiveLocation: longitude:"+longitude );

			//Log.e(TAG, "onReceiveLocation: location.getLocType():"+ location.getLocType());
			//Log.e(TAG, "onReceiveLocation: BDLocation.TypeGpsLocation:"+ BDLocation.TypeGpsLocation);
        	if( location.getLocType()== BDLocation.TypeGpsLocation ){

        		altitude = (float)location.getAltitude();
        		speed = location.getSpeed();
				Log.e(TAG, "onReceiveLocation:speed: "+speed );
        		satelliteNumber = location.getSatelliteNumber();
        		direction = location.getDirection();
        		
    			mCont.altitude = altitude;
    			// GPS模式
    			if( mCont.runMode==MyConfig.MODE_GPS ){
    				mCont.speedNow = speed;
    				mCont.runState = MyController.IS_RUNNING;
    				mCont.run();
    			}
        	}else{
        		// GPS模式
    			if( mCont.runMode==MyConfig.MODE_GPS ){
    				mCont.runState = MyController.GPS_NO_SIGNAL;
    			}
        	}

        	// 输出状态
            String state = "定位失败";
            if (location.getLocType() == BDLocation.TypeGpsLocation){
            	state = "gps定位成功"; 
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
            	state = "网络定位成功";
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
            	state = "离线定位成功，离线定位结果也是有效的";
            } else if (location.getLocType() == BDLocation.TypeServerError) {
            	state = "服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因";
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
            	state = "网络不同导致定位失败，请检查网络是否通畅";
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
            	state = "无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机";
            }
            Log.i(TAG, state.toString());
            //Toast.makeText(context, state.toString(), Toast.LENGTH_SHORT);
        }
	}
}
