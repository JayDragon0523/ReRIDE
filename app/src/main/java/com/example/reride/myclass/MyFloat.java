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
	private static final int DELAY_MILLIS = 1000;  // UI刷新间隔时间
	
	private WakeLock mWakeLock;
	
	public MyFloat(Context context, MyApp myApp) {
		this.context = context.getApplicationContext();
		this.myApp = myApp;
		mConfig = new MyConfig(context);
	}
	
	public void createFloatView() {
		final LayoutParams wmParams = new LayoutParams();
		//获取的是WindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		//设置window type
		wmParams.type = LayoutParams.TYPE_PHONE;
		//设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.START | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = 0;
        wmParams.y = 0;
        //设置悬浮窗口长宽数据
        wmParams.width = LayoutParams.MATCH_PARENT;
        wmParams.height = LayoutParams.WRAP_CONTENT;

		/*// 设置悬浮窗口长宽数据
        wmParams.width = 200;
        wmParams.height = 80;*/
   
        LayoutInflater inflater = LayoutInflater.from(context);
        //获取浮动窗口视图所在布局
        mFloatLayout = (RelativeLayout)inflater.inflate(R.layout.float_layout, null);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
        // 测量宽高
        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        // 监听浮动窗口的触摸移动
        mFloatLayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(mFloatLayout==null) // FloatView移除时OnTouchListener还未被回收在执行中，避免出错设置该语句
					return false;
				// getRawX/getRawY是触摸位置相对于屏幕的坐标，getX/getY是相对于按钮的坐标
				// 减25为状态栏的高度
	            wmParams.y = (int) event.getRawY() - mFloatLayout.getMeasuredHeight()/2 - 25;
	            //Log.i(TAG, "RawY：" + event.getRawY() + " Y：" + event.getY());
	            // 刷新
	            mWindowManager.updateViewLayout(mFloatLayout, wmParams);
				return false;  //此处必须返回false，否则OnClickListener获取不到监听
			}
		});
        
        // 监听浮动窗口的点击事件
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
        handler.removeCallbacks(runnable);  // 关闭定时刷新
		handler.postDelayed(runnable, DELAY_MILLIS);  // 开启定时刷新
		
		// 设置长亮
        mConfig.read();
        if( mConfig.displayLongBright ){
        	PowerManager powerManager = (PowerManager)context.getSystemService(context.POWER_SERVICE);
        	mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "lock_tag");
        	mWakeLock.acquire();
        }
	}
	
	public void removeFloatView(){
		if(mFloatLayout!=null){
			handler.removeCallbacks(runnable);  // 关闭定时刷新
            mWindowManager.removeView(mFloatLayout);  // 移除视图
        	// 关闭长亮
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
		myApp.localService.mCont.refreshTodayDate();  // 日期变化时，刷新今日数据
		DecimalFormat df = new DecimalFormat("0.0");
		speedNow.setText( df.format(myApp.localService.mCont.speedNow) );
		speedAvg.setText( df.format(myApp.localService.mCont.todaySpeedAvg) );
		mileage.setText( df.format(myApp.localService.mCont.todayMileage) );
	}
	
	public boolean isShowFloatView(){
		return mFloatLayout==null?false:true;
	}

}
