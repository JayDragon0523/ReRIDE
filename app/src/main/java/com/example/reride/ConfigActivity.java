package com.example.reride;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.reride.bean.User;
import com.example.reride.myclass.MyConfig;
import com.example.reride.myclass.MyLocation;
import com.example.reride.system.MyApp;
import com.example.reride.utils.DateTime;

public class ConfigActivity extends AppCompatActivity {

    private static final String TAG = "ConfigActivity";

    private MyConfig mConfig;
    private MyApp myApp;
    private User user;

    private ToggleButton altitudeState, displayState;
    private TextView  lastSycnTime;

    private SyncReceiver sReceiver = new SyncReceiver();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        myApp = (MyApp) this.getApplication();
        user = myApp.user;
        mConfig = new MyConfig(this);

        ImageButton back = (ImageButton) findViewById(R.id.titlebar_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        displayState = (ToggleButton) findViewById(R.id.activity_config_display_value);
        LinearLayout display = (LinearLayout) findViewById(R.id.activity_config_display);
        display.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (displayState.isChecked()) {
                    displayState.setChecked(false);
                    mConfig.displayLongBright = false;
                } else {
                    displayState.setChecked(true);
                    mConfig.displayLongBright = true;
                }
                mConfig.save();
            }
        });

        LinearLayout altitude = (LinearLayout) findViewById(R.id.activity_config_altitude);
        altitude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        LinearLayout sync = (LinearLayout) findViewById(R.id.activity_config_sync);
        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.uid == 0) {
                    Toast.makeText(getApplicationContext(), "登陆后才能同步数据", Toast.LENGTH_SHORT).show();
                    // 打开登陆窗口
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    return;
                }
                myApp.localService.mSync.start();
            }
        });
        altitudeState = (ToggleButton) findViewById(R.id.activity_config_altitude_value);
        lastSycnTime = (TextView) findViewById(R.id.activity_config_lastsycntime);

        registerReceiver(sReceiver, new IntentFilter("cn.bigbike.cycling.SYNC_STATE"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MyLocation.isOpenGPS(this)) {
            altitudeState.setChecked(true);
        } else {
            altitudeState.setChecked(false);
        }
        mConfig.read();

        // 屏幕长亮
        displayState.setChecked(mConfig.displayLongBright);
        // 最后同步时间
        if (user.lastSyncTime == 0) {
            lastSycnTime.setText("");
        } else {
            String timeText = DateTime.getDateFormat(user.lastSyncTime, DateTime.FORMAT_SHORT);
            if (DateTime.getDateFormat(user.lastSyncTime, DateTime.FORMAT_LONG).equals(DateTime.getTodayDate()))
                timeText = "今日 " + DateTime.getDateFormat(user.lastSyncTime, DateTime.FORMAT_HIDE, DateTime.FORMAT_SHORT);
            if (DateTime.getDateFormat(user.lastSyncTime, DateTime.FORMAT_LONG).equals(DateTime.getYestoryDate()))
                timeText = "昨日 " + DateTime.getDateFormat(user.lastSyncTime, DateTime.FORMAT_HIDE, DateTime.FORMAT_SHORT);
            lastSycnTime.setText(timeText);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(sReceiver);
    }

    // 检测同步状态
    private class SyncReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            lastSycnTime.setText("刚刚   ");
        }
    }
}
