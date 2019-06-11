package com.example.reride.myclass;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.reride.MainActivity;
import com.example.reride.R;
import com.example.reride.system.MyApp;

import java.text.DecimalFormat;

public class MyFloat {
	
	private Context context;
	private MyApp myApp;
	private MyConfig mConfig;
	
	private RelativeLayout mFloatLayout;
	private WindowManager mWindowManager;
	private TextView mileage, speedNow, speedAvg;
	private static final int DELAY_MILLIS = 1000;  // UIˢ�¼��ʱ��
	
	private WakeLock mWakeLock;
	
	public MyFloat(Context context, MyApp myApp) {
		this.context = context.getApplicationContext();
		this.myApp = myApp;
		mConfig = new MyConfig(context);
	}
	
	public void createFloatView() {
		final LayoutParams wmParams = new LayoutParams();
		//��ȡ����WindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		//����window type
		wmParams.type = LayoutParams.TYPE_PHONE;
		//����ͼƬ��ʽ��Ч��Ϊ����͸��
        wmParams.format = PixelFormat.RGBA_8888;
        //���ø������ڲ��ɾ۽���ʵ�ֲ���������������������ɼ����ڵĲ�����
        wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
        //������������ʾ��ͣ��λ��Ϊ����ö�
        wmParams.gravity = Gravity.START | Gravity.TOP;
        // ����Ļ���Ͻ�Ϊԭ�㣬����x��y��ʼֵ�������gravity
        wmParams.x = 0;
        wmParams.y = 0;
        //�����������ڳ�������
        wmParams.width = LayoutParams.MATCH_PARENT;
        wmParams.height = LayoutParams.WRAP_CONTENT;

		/*// �����������ڳ�������
        wmParams.width = 200;
        wmParams.height = 80;*/
   
        LayoutInflater inflater = LayoutInflater.from(context);
        //��ȡ����������ͼ���ڲ���
        mFloatLayout = (RelativeLayout)inflater.inflate(R.layout.float_layout, null);
        //���mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
        // �������
        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        // �����������ڵĴ����ƶ�
        mFloatLayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(mFloatLayout==null) // FloatView�Ƴ�ʱOnTouchListener��δ��������ִ���У�����������ø����
					return false;
				// getRawX/getRawY�Ǵ���λ���������Ļ�����꣬getX/getY������ڰ�ť������
				// ��25Ϊ״̬���ĸ߶�
	            wmParams.y = (int) event.getRawY() - mFloatLayout.getMeasuredHeight()/2 - 25;
	            //Log.i(TAG, "RawY��" + event.getRawY() + " Y��" + event.getY());
	            // ˢ��
	            mWindowManager.updateViewLayout(mFloatLayout, wmParams);
				return false;  //�˴����뷵��false������OnClickListener��ȡ��������
			}
		});
        
        // �����������ڵĵ���¼�
        mFloatLayout.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				//Log.i(TAG, "mFloatLayout - onClick");
				Intent intent = new Intent(context, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);  
				context.startActivity(intent);
				return false;
			}
		});
        
        mileage = (TextView)mFloatLayout.findViewById(R.id.float_layout_mileage);
        speedNow = (TextView)mFloatLayout.findViewById(R.id.float_layout_speednow);
        speedAvg = (TextView)mFloatLayout.findViewById(R.id.float_layout_speedavg);
        Typeface typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/text.otf");
        mileage.setTypeface(typeFace);
        speedNow.setTypeface(typeFace);
        speedAvg.setTypeface(typeFace);
        
        refreshFloatView();
        handler.removeCallbacks(runnable);  // �رն�ʱˢ��
		handler.postDelayed(runnable, DELAY_MILLIS);  // ������ʱˢ��
		
		// ���ó���
        mConfig.read();
        if( mConfig.displayLongBright ){
        	PowerManager powerManager = (PowerManager)context.getSystemService(context.POWER_SERVICE);
        	mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "lock_tag");
        	mWakeLock.acquire();
        }
	}
	
	public void removeFloatView(){
		if(mFloatLayout!=null){
			handler.removeCallbacks(runnable);  // �رն�ʱˢ��
            mWindowManager.removeView(mFloatLayout);  // �Ƴ���ͼ
        	// �رճ���
        	if( mWakeLock!=null ){
        		mWakeLock.release();
        		mWakeLock = null;
        	}
        	mFloatLayout = null;
		}
	}
	
	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			refreshFloatView();
			handler.postDelayed(this, DELAY_MILLIS);
		}
	};
	
	public void refreshFloatView(){
		myApp.localService.mCont.refreshTodayDate();  // ���ڱ仯ʱ��ˢ�½�������
		DecimalFormat df = new DecimalFormat("0.0");
		speedNow.setText( df.format(myApp.localService.mCont.speedNow) );
		speedAvg.setText( df.format(myApp.localService.mCont.todaySpeedAvg) );
		mileage.setText( df.format(myApp.localService.mCont.todayMileage) );
	}
	
	public boolean isShowFloatView(){
		return mFloatLayout==null?false:true;
	}

}
