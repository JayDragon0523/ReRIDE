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
	
	public static final int DELAY_MILLIS = 1000;  // GPSˢ�¼��ʱ��
	
	public static final float ALTITUDE_INIT = 88888F;  // �߶ȳ�ʼֵ
	
	public float altitude = ALTITUDE_INIT;  // �߶�
	public float speed = 0.0F;  // �ٶ�
	public int satelliteNumber = 0;  // ������
	public float direction = 0.0F;  // ����
	
	public MyLocation(Context context, MyController mCont) {
		this.context = context;
		this.mCont = mCont;
		mLocationClient = new LocationClient(context);     //����LocationClient��
		initLocation();
	    mLocationClient.registerLocationListener( myListener );    //ע���������
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
			Log.w(TAG, "start failure���Ѿ���������");
			return;
		}
		mLocationClient.start();
		handler.removeCallbacks(runnable);  // �رն�ʱˢ��
		handler.postDelayed(runnable, DELAY_MILLIS);  // ������ʱˢ��
	}

	public void stop(){
		Log.w(TAG, "stop");
		handler.removeCallbacks(runnable);  // �رն�ʱˢ��
		if( mLocationClient.isStarted() )
			mLocationClient.stop();
	}
	
	private void requestLocation(){
		if( mLocationClient.isStarted() ){
			if( isOpenGPS(context) ){
				mLocationClient.requestLocation();
			}else{				
				// �ָ���ʼֵ
				if( altitude!=ALTITUDE_INIT ){
					altitude = ALTITUDE_INIT;
					mCont.altitude = altitude;
				}
				// GPSģʽ
    			if( mCont.runMode==MyConfig.MODE_GPS ){
    				mCont.runState = MyController.GPS_CLOSE;
    			}
			}
		}
	}
	
	private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy); //��ѡ��Ĭ�ϸ߾��ȣ����ö�λģʽ���߾��ȣ��͹��ģ����豸
        option.setCoorType("bd09ll"); //��ѡ��Ĭ��gcj02�����÷��صĶ�λ�������ϵ
        int span = 0;
        option.setScanSpan(span); //��ѡ��Ĭ��0��������λһ�Σ����÷���λ����ļ������Ҫ���ڵ���1000ms������Ч��
        option.setIsNeedAddress(false); //��ѡ�������Ƿ���Ҫ��ַ��Ϣ��Ĭ�ϲ���Ҫ
        option.setOpenGps(true); //��ѡ��Ĭ��false,�����Ƿ�ʹ��gps
        option.setLocationNotify(false); //��ѡ��Ĭ��false�������Ƿ�gps��Чʱ����1S1��Ƶ�����GPS���
        option.setIgnoreKillProcess(false); //��ѡ��Ĭ��false����λSDK�ڲ���һ��SERVICE�����ŵ��˶������̣������Ƿ���stop��ʱ��ɱ��������̣�Ĭ��ɱ��
        option.SetIgnoreCacheException(false); //��ѡ��Ĭ��false�������Ƿ��ռ�CRASH��Ϣ��Ĭ���ռ�
        mLocationClient.setLocOption(option);
    }
	
	public static final boolean isOpenGPS(Context context) {
        LocationManager locationManager  = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // ͨ��GPS���Ƕ�λ����λ������Ծ�ȷ���֣�ͨ��24�����Ƕ�λ��������Ϳտ��ĵط���λ׼ȷ���ٶȿ죩 
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

			//��ȡγ����Ϣ
			double latitude = location.getLatitude();
			//��ȡ������Ϣ
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
    			// GPSģʽ
    			if( mCont.runMode==MyConfig.MODE_GPS ){
    				mCont.speedNow = speed;
    				mCont.runState = MyController.IS_RUNNING;
    				mCont.run();
    			}
        	}else{
        		// GPSģʽ
    			if( mCont.runMode==MyConfig.MODE_GPS ){
    				mCont.runState = MyController.GPS_NO_SIGNAL;
    			}
        	}

        	// ���״̬
            String state = "��λʧ��";
            if (location.getLocType() == BDLocation.TypeGpsLocation){
            	state = "gps��λ�ɹ�"; 
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
            	state = "���綨λ�ɹ�";
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
            	state = "���߶�λ�ɹ������߶�λ���Ҳ����Ч��";
            } else if (location.getLocType() == BDLocation.TypeServerError) {
            	state = "��������綨λʧ�ܣ����Է���IMEI�źʹ��嶨λʱ�䵽loc-bugs@baidu.com��������׷��ԭ��";
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
            	state = "���粻ͬ���¶�λʧ�ܣ����������Ƿ�ͨ��";
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
            	state = "�޷���ȡ��Ч��λ���ݵ��¶�λʧ�ܣ�һ���������ֻ���ԭ�򣬴��ڷ���ģʽ��һ���������ֽ�����������������ֻ�";
            }
            Log.i(TAG, state.toString());
            //Toast.makeText(context, state.toString(), Toast.LENGTH_SHORT);
        }
	}
}
