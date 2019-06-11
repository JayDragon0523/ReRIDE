package com.example.reride;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.reride.fragment.Fragment1;
import com.example.reride.fragment.Fragment2;
import com.example.reride.fragment.Fragment3;
import com.example.reride.myclass.MyConfig;
import com.example.reride.myclass.MyLocation;
import com.example.reride.system.MyApp;
import com.example.reride.system.MyService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Fragment1 mInteract;
    private Fragment2 mCycling;
    private Fragment3 mMall;
    public MyService localService;
    private MyApp myApp;
    private MyConfig mConfig;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            fragmentManager = getSupportFragmentManager();
            transaction = fragmentManager.beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if(mInteract == null)
                        mInteract = new Fragment1();
                    transaction.replace(R.id.content,mInteract);
                    transaction.commit();
                    return true;
                case R.id.navigation_cycling:
                    if(mCycling == null)
                        mCycling = new Fragment2();
                    transaction.replace(R.id.content,mCycling);
                    transaction.commit();
                    return true;
                case R.id.navigation_goods:
                    if(mMall == null)
                        mMall = new Fragment3();
                    transaction.replace(R.id.content,mMall);
                    transaction.commit();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 解决Activity被回收后重启所导致的Fragment重复创建和重叠的问题
        if( savedInstanceState!=null ){
            mInteract = (Fragment1)getSupportFragmentManager().findFragmentByTag("fragment1");
            mCycling = (Fragment2)getSupportFragmentManager().findFragmentByTag("fragment2");
            mMall = (Fragment3)getSupportFragmentManager().findFragmentByTag("fragment3");
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.nav_view);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(navigation.getMenu().getItem(1).getItemId());
        myApp = (MyApp)getApplication();
        mConfig = new MyConfig(this);

        setDefaultFragment();
        binderService();

        if( mConfig.appIsFirstStart ){
            Intent intent = new Intent();
            intent.putExtra("appIsFirstStart", mConfig.appIsFirstStart);
            intent.setClass(getApplicationContext(), ConfigActivity.class);
            Toast.makeText(MainActivity.this,"应用第一次启动，请确认运行模式",Toast.LENGTH_LONG);
            startActivity(intent);
            mConfig.appIsFirstStart = false;
            mConfig.save();
        }
    }

    @Override
    protected void onStart() {
        // GPS打开提示
        mConfig.read();
        if( mConfig.appRunMode==MyConfig.MODE_GPS & myApp.GPSOpenTip==false & MyLocation.isOpenGPS(getApplicationContext())==false ){
            Toast.makeText(MainActivity.this,"GPS未开启",Toast.LENGTH_SHORT);
            myApp.GPSOpenTip = true;
        }
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 设置长亮
        mConfig.read();
        if( mConfig.displayLongBright )
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  // 禁止屏幕休眠和锁屏
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConn);
    }

    private ServiceConnection serviceConn = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            localService = ((MyService.LocalBinder)service).getService();
            Log.e(TAG, "onServiceConnected: "+localService );
            myApp.localService = localService; // 把Service的引用存放到全局类
            mCycling.refreshUI();  // 刷新UI
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            localService = null;
        }
    };

    // 用bindService方法启动服务
    private void binderService() {
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
        bindService(intent, serviceConn, BIND_AUTO_CREATE);
    }

    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;

    // 设置初始显示的页面
    private void setDefaultFragment(){
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content,new Fragment2());
        transaction.commit();
    }

}
