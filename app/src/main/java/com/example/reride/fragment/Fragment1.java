package com.example.reride.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.reride.LoginActivity;
import com.example.reride.MainActivity;
import com.example.reride.R;
import com.example.reride.TodayRankActivity;
import com.example.reride.TotalRankActivity;
import com.example.reride.bean.User;
import com.example.reride.system.MyApp;


public class Fragment1 extends Fragment {

    private MainActivity mainActivity;
    private static final String TAG = "Fragment1";
    private User user;
    private DrawerLayout mDrawerLayout;

    Activity mActivity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mainActivity = (MainActivity) getActivity();
        MyApp myApp = (MyApp)mainActivity.getApplication();
        user = myApp.user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mActivity = getActivity();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_interact, container, false);

        NavigationView navView = view.findViewById(R.id.nav_view);
        Toolbar toolbar = (Toolbar)view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        mDrawerLayout = view.findViewById(R.id.drawer_layout);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.titlebar_user_click);
        }
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        LinearLayout rankTotal = (LinearLayout) view.findViewById(R.id.fragment_interact_rank_total);
        rankTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( user.uid==0 ){
                    Toast.makeText(mainActivity.getApplicationContext(), "Please login first!", Toast.LENGTH_SHORT).show();
                    // 打开登陆窗口
                    Intent intent = new Intent();
                    intent.setClass(mainActivity.getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(mainActivity.getApplicationContext(), TotalRankActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout rankToday = (LinearLayout) view.findViewById(R.id.fragment_interact_rank_today);
        rankToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( user.uid==0 ){
                    Toast.makeText(mainActivity.getApplicationContext(), "Please login first!", Toast.LENGTH_SHORT).show();
                    // 打开登陆窗口
                    Intent intent = new Intent();
                    intent.setClass(mainActivity.getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(mainActivity.getApplicationContext(), TodayRankActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.e(TAG, "onOptionsItemSelected: id="+id );
        if (id == 16908332) {
            Log.e(TAG, "onOptionsItemSelected: user:"+user );
            if( user.uid==0 ){
                Toast.makeText(mainActivity.getApplicationContext(), "Please login first!", Toast.LENGTH_SHORT).show();
                // 打开登陆窗口
                Intent intent = new Intent();
                intent.setClass(mainActivity.getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                return false;
            }
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return true;
    }

}
