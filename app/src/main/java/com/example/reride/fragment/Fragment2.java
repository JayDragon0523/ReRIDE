package com.example.reride.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.example.reride.ConfigActivity;
import com.example.reride.MainActivity;
import com.example.reride.R;
import com.example.reride.bean.User;
import com.example.reride.myclass.MyConfig;
import com.example.reride.myclass.MyController;
import com.example.reride.myclass.MyLocation;
import com.example.reride.myclass.MyModel;
import com.example.reride.system.MyApp;
import com.example.reride.system.MyService;
import com.example.reride.utils.Calorie;
import com.lidroid.xutils.BitmapUtils;
import com.spark.submitbutton.SubmitButton;

import java.text.DecimalFormat;

import de.hdodenhof.circleimageview.CircleImageView;
import ng.max.slideview.SlideView;


public class Fragment2 extends Fragment {

    private static final String TAG = "Fragment2";
    private MainActivity mainActivity;

    private static final int DELAY_MILLIS = 500;  // UI刷新间隔时间
    private int arrowCount = 1;	// 箭头闪烁计数
    private String titleRadioGroupChecked = "today";

    RadioButton end_radio_btn;

    private TextView runState, speedNow, speedAvg, speedMax, mileage, totalTime, totalTimeUnit, onceTitleText, altitude, calorie, altitudeNow;
    private LinearLayout baidumap,todayOnceView,onceTitleView;
    private RelativeLayout toolBar;
    private ImageButton toolLocation;
    private View blockOne, blockTwo, blockThree;
    private CircleImageView userInfo;
    private RelativeLayout toolView;
    private SubmitButton start;
    BottomNavigationView navView;
    SlideView slideView;
    MapView mMapView = null;
    //百度地图
    private BaiduMap baiduMap = null;

    private BitmapUtils bitmapUtils;
    private MyModel myModel;
    private User user;

    LinearLayout.LayoutParams lp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity)getActivity();
        myModel = new MyModel(mainActivity.getApplicationContext());
        MyApp myApp = (MyApp)mainActivity.getApplication();
        user = myApp.user;
        bitmapUtils = new BitmapUtils(mainActivity.getApplicationContext());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SDKInitializer.initialize(getActivity().getApplicationContext());//初始化百度地图SDK
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cycling, container, false);
        navView = getActivity().findViewById(R.id.nav_view);
        slideView = view.findViewById(R.id.slider_end);
        toolBar = view.findViewById(R.id.toolbar);
        slideView.setVisibility(View.GONE);

        baidumap = view.findViewById(R.id.baidumap);
        mMapView = (MapView)view.findViewById(R.id.bmapView);//百度地图
        baidumap.setVisibility(View.GONE);
        mMapView.showZoomControls(false);
        mMapView.removeViewAt(1);
        baiduMap = mMapView.getMap();
        /*
        设置地图类型
         */
        //普通地图
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        baiduMap.setTrafficEnabled(true);
        baiduMap.setMyLocationEnabled(true);

        ImageButton config = (ImageButton)view.findViewById(R.id.titlebar_cycling_config);
        config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mainActivity.getApplicationContext(), ConfigActivity.class);
                startActivity(intent);
            }
        });

        toolLocation = (ImageButton)view.findViewById(R.id.fragment_cycling_tool_location);
        toolLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        lp = (LinearLayout.LayoutParams) navView.getLayoutParams();

        start =  view.findViewById(R.id.start_btn);
        Log.e(TAG, "onCreateView: navView:"+navView );
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.localService.mCont.go = true;
                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        if(baiduMap != null)
                            mainActivity.localService.mLoc.baiduMap = baiduMap;
                        start.setVisibility(View.GONE);
                        slideView.setVisibility(View.VISIBLE);
                        baidumap.setVisibility(View.VISIBLE);
                        toolBar.setVisibility(View.GONE);

                        lp.bottomMargin = -200;
                        navView.setLayoutParams(lp);

                    }
                }, 2000);

            }
        });
        slideView.setOnSlideCompleteListener(new SlideView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(SlideView slideView) {
                mainActivity.localService.mCont.go = false;
                slideView.setVisibility(View.GONE);
                start.setVisibility(View.VISIBLE);
                baidumap.setVisibility(View.GONE);
                toolBar.setVisibility(View.VISIBLE);

                lp.bottomMargin = 0;
                navView.setLayoutParams(lp);

                end_radio_btn.setChecked(true);
                titleRadioGroupChecked = "once";

                if(mainActivity.localService.mCont.todayMileage > 0.015)
                    mainActivity.localService.mCont.saveData();
                else
                    Toast.makeText(getActivity(),"此次骑行路程太短，不记录！",Toast.LENGTH_SHORT).show();
                refreshUI();
            }
        });
        RadioGroup titleRadioGroup = (RadioGroup)view.findViewById(R.id.titlebar_cycling_radiogroup);
        titleRadioGroup.getChildAt(1).setEnabled(false);
        titleRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 切换工具栏
                switch (checkedId) {
                    case R.id.titlebar_cycling_radiogroup_today:
                        titleRadioGroupChecked = "today";
                        start.setVisibility(View.VISIBLE);
                        break;
                }
                refreshUI();  // 刷新UI
            }
        });

        end_radio_btn = view.findViewById(R.id.titlebar_cycling_radiogroup_once);
        runState = (TextView)view.findViewById(R.id.fragment_cycling_runstate);

        speedNow = (TextView)view.findViewById(R.id.fragment_cycling_speednow);
        speedAvg = (TextView)view.findViewById(R.id.fragment_cycling_speedavg);
        speedMax = (TextView)view.findViewById(R.id.fragment_cycling_speedmax);
        mileage = (TextView)view.findViewById(R.id.fragment_cycling_mileage);
        totalTime = (TextView)view.findViewById(R.id.fragment_cycling_totaltime);
        totalTimeUnit = (TextView)view.findViewById(R.id.fragment_cycling_totaltime_unit);

        todayOnceView = (LinearLayout)view.findViewById(R.id.fragment_cycling_today_once);
        onceTitleView = (LinearLayout)view.findViewById(R.id.fragment_cycling_once_title);
        onceTitleText = (TextView)view.findViewById(R.id.fragment_cycling_once_title_text);

        calorie = (TextView)view.findViewById(R.id.fragment_cycling_calorie);
        altitude = (TextView)view.findViewById(R.id.fragment_cycling_altitude);
        altitudeNow  = (TextView)view.findViewById(R.id.fragment_cycling_altitude_now);

        blockOne = (View)view.findViewById(R.id.fragment_cycling_block_one);
        blockTwo = (View)view.findViewById(R.id.fragment_cycling_block_two);
        blockThree = (View)view.findViewById(R.id.fragment_cycling_block_three);

        toolView = (RelativeLayout)view.findViewById(R.id.fragment_cycling_tool);

        Typeface typeFace = Typeface.createFromAsset(mainActivity.getAssets(), "fonts/text.otf");
        speedNow.setTypeface(typeFace);
        speedAvg.setTypeface(typeFace);
        speedMax.setTypeface(typeFace);
        mileage.setTypeface(typeFace);
        totalTime.setTypeface(typeFace);
        altitude.setTypeface(typeFace);
        altitudeNow.setTypeface(typeFace);
        calorie.setTypeface(typeFace);
        return view;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        mMapView.onResume();
        handler.removeCallbacks(runnable);  // 关闭定时刷新
        handler.postDelayed(runnable, DELAY_MILLIS);  // 开启定时刷新
        refreshUI();  // 刷新UI
        // 头像设置
        /*if( user.uid==0 ){
            login.setVisibility(View.VISIBLE);
            userInfo.setVisibility(View.GONE);
        }else{
            login.setVisibility(View.GONE);
            if( userInfo.getVisibility()!=View.VISIBLE ){
                bitmapUtils.display(userInfo, user.avatarUrl);
                userInfo.setVisibility(View.VISIBLE);
            }
        }*/
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        mMapView.onResume();
        handler.removeCallbacks(runnable);  // 关闭定时刷新
        if( mainActivity.localService!=null ){
            mainActivity.localService.mCont.saveData();  // 保存数据
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mMapView.onResume();
        myModel.release();
    }

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            refreshUI();
            handler.postDelayed(this, DELAY_MILLIS);
        }
    };

    public void refreshUI(){
        if( mainActivity.localService==null ){
            return;
        }
        if( mainActivity.localService.mCont==null )
            return;

        mainActivity.localService.mCont.refreshTodayDate();  // 日期变化时，刷新今日数据


        float mMileage = 0;
        long mTotalTime = 0;
        float mSpeedMax = 0;
        float mSpeedAvg = 0;
        float mAltitudeMax = -10000;
        float mAltitudeMin = 10000;
        if (mainActivity.localService.mCont.go) {

            mMileage = mainActivity.localService.mCont.todayMileage;
            mTotalTime = mainActivity.localService.mCont.todayTotalTime;
            mSpeedMax = mainActivity.localService.mCont.todaySpeedMax;
            mSpeedAvg = mainActivity.localService.mCont.todaySpeedAvg;
            mAltitudeMax = mainActivity.localService.mCont.todayAltitudeMax;
            mAltitudeMin = mainActivity.localService.mCont.todayAltitudeMin;
        }

        // Tab切换
        todayOnceView.setVisibility(View.VISIBLE);
        if( titleRadioGroupChecked.equals("today") ){
            onceTitleView.setVisibility(View.GONE);
        }
        if( titleRadioGroupChecked.equals("once") ){
            start.setVisibility(View.GONE);
            mMileage = 0;
            mTotalTime = mainActivity.localService.mCont.onceTotalTime;
            mSpeedMax = mainActivity.localService.mCont.onceSpeedMax;
            mSpeedAvg = mainActivity.localService.mCont.onceSpeedAvg;
            mAltitudeMax = mainActivity.localService.mCont.onceAltitudeMax;
            mAltitudeMin = mainActivity.localService.mCont.onceAltitudeMin;
            if(mainActivity.localService.mCont.onceTitle!=null){
                onceTitleText.setText(mainActivity.localService.mCont.onceTitle);
                onceTitleView.setVisibility(View.VISIBLE);
            }
        }


        // 状态
        int mRunState = mainActivity.localService.mCont.runState;
        Log.e(TAG, "refreshUI: mRunState:"+mRunState );
        switch( mRunState ){
            case MyController.NO_START:
                if( mainActivity.localService.mCont.runMode== MyConfig.MODE_GPS ){
                    runState.setText("GPS未启动，待出发中...");
                }else{
                    if( mainActivity.localService.hpReceiver.status== MyService.HeadsetPlugReceiver.PULLOUT ){
                        runState.setText("未连接设备");
                    }else{
                        runState.setText("设备已连接");
                    }
                }
                flicker(0);  // 隐藏闪耀方块
                break;
            case MyController.INIT_FAILED:
                runState.setText("初始化失败");
                break;
            case MyController.MIC_OCCUPIED:
                runState.setText("麦克风被其他APP占用，或被360等软件限制录音权限");
                break;
            case MyController.GPS_CLOSE:
                runState.setText("GPS关闭，请进入设置开启");
                break;
            case MyController.GPS_NO_SIGNAL:
                    runState.setText("GPS没有信号，请移动");
                break;
            case MyController.IS_RUNNING:
                if( mainActivity.localService.mCont.wheelState==MyController.WHEEL_ROTATION ){
                    runState.setText("骑行中");
                    //toolView.setVisibility(View.INVISIBLE);
                    if( arrowCount<=3 ){
                        flicker(arrowCount);
                        arrowCount++;
                        if(arrowCount==4)
                            arrowCount = 1;
                    }
                }else{
                    runState.setText("车辆停止");
                    flicker(0);  // 隐藏闪耀方块
                }
                break;
        }

        // GPS状态按钮
        boolean isOpenGPS = MyLocation.isOpenGPS(mainActivity.getApplicationContext());
        if( isOpenGPS ){
            toolLocation.setBackgroundResource(R.drawable.cycling_location_open);
        }else{
            toolLocation.setBackgroundResource(R.drawable.cycling_location);
        }

        // 输出数据
        DecimalFormat dfInt = new DecimalFormat("0");
        DecimalFormat dfFloat = new DecimalFormat("0.0");

        speedNow.setText( dfFloat.format(mainActivity.localService.mCont.speedNow) );
        speedAvg.setText( dfFloat.format(mSpeedAvg) );
        speedMax.setText( mSpeedMax>=100?dfInt.format(mSpeedMax):dfFloat.format(mSpeedMax) );
        mileage.setText( dfFloat.format(mMileage) );
        // 计算时间
        int hour = (int)Math.floor( mTotalTime/(1000*60*60) ) ;
        int minute = (int)Math.floor( (mTotalTime % (1000*60*60))/(1000*60) );
        int second = (int)Math.floor( ((mTotalTime % (1000*60*60)) % (1000*60))/1000 );
        if( hour==0 ){
            totalTimeUnit.setText("M");
            totalTime.setText( (minute<10?"0":"") + String.valueOf(minute) + ":" + (second<10?"0":"") + String.valueOf(second) );
        }else{
            totalTimeUnit.setText("H");
            totalTime.setText( (hour<10?"0":"") + String.valueOf(hour) + ":" + (minute<10?"0":"") + String.valueOf(minute) );
        }
        // 卡路里
        if( mTotalTime!=0 ){
            calorie.setText( Calorie.run(mMileage/mTotalTime*1000*3600, mTotalTime) );
        }else{
            calorie.setText("0");
        }
        // 高度
        if( mAltitudeMax!=MyController.ALTITUDE_MAX_INIT && mAltitudeMin!=MyController.ALTITUDE_MIN_INIT ){
            altitude.setText( String.valueOf( (int)(mAltitudeMax-mAltitudeMin) ) );
        }else{
            altitude.setText("0");
        }
        // 实时海拔
        altitudeNow.setText("");
        if( titleRadioGroupChecked.equals("today") || ( titleRadioGroupChecked.equals("once") & mainActivity.localService.mCont.onceTitle!=null ) ){
            if( isOpenGPS & mainActivity.localService.mCont.altitude!=MyLocation.ALTITUDE_INIT ){
                altitudeNow.setText( "海拔 " + String.valueOf( (int)mainActivity.localService.mCont.altitude) );
            }
        }
    }

    // 让箭头闪烁
    private void flicker( int num ){
        blockOne.setVisibility(View.INVISIBLE);
        blockTwo.setVisibility(View.INVISIBLE);
        blockThree.setVisibility(View.INVISIBLE);
        switch( num ){
            case 1:
                blockOne.setVisibility(View.VISIBLE);
                break;
            case 2:
                blockTwo.setVisibility(View.VISIBLE);
                break;
            case 3:
                blockThree.setVisibility(View.VISIBLE);
                break;
        }
    }
}
