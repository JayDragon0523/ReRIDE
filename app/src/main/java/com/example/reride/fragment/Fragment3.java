package com.example.reride.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.reride.R;
import com.example.reride.MainActivity;
import com.example.reride.WebActivity;


public class Fragment3 extends Fragment {
    private MainActivity mainActivity;
    private static final String TAG = "Fragment3";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mall, container, false);

        ImageView x1 = (ImageView)view.findViewById(R.id.fragment_mall_banner_x1);
        x1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("url", "https://detail.tmall.com/item.htm?id=556324681187&ali_refid=a3_430582_1006:1104426545:N:H53f8Yj1MaMsPVP95CfydSN+TdKihNTW:91607fc9207300582b75c3bee7cc7288&ali_trackid=1_91607fc9207300582b75c3bee7cc7288&spm=a230r.1.14.1");
                intent.setClass(mainActivity.getApplicationContext(), WebActivity.class);
                startActivity(intent);
            }
        });

        ImageView letdooo = (ImageView)view.findViewById(R.id.fragment_mall_banner_letdooo);
        letdooo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("url", "https://detail.tmall.com/item.htm?spm=a230r.1.14.34.677b51bddZ3Smn&id=550987262696&ns=1&abbucket=14");
                intent.setClass(mainActivity.getApplicationContext(), WebActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

}
