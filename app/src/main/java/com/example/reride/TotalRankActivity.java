package com.example.reride;

import android.app.Dialog;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.reride.bean.User;
import com.example.reride.system.MyApp;
import com.example.reride.utils.DateTime;
import com.example.reride.widget.LinearLayoutForAdapter;
import com.example.reride.widget.LinearLayoutForListView;
import com.example.reride.widget.LoadingDialog;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.reride.myclass.MyLocation.DELAY_MILLIS;

public class TotalRankActivity extends AppCompatActivity {

    private static final String TAG = "TotalRankActivity";

    private MyApp myApp;
    private User user;

    private Dialog loading;

    private LinearLayout body;
    private LinearLayoutForListView listView;
    private List<HashMap<String, String>> data;
    private LinearLayoutForAdapter adapter;

    private TextView rankNumber, rankMileage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_rank);

        myApp = (MyApp) this.getApplication();
        user = myApp.user;

        ImageButton back = (ImageButton)findViewById(R.id.titlebar_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loading = LoadingDialog.createDialog(this);

        body = (LinearLayout) findViewById(R.id.activity_body);
        body.setVisibility(View.INVISIBLE);

        listView = (LinearLayoutForListView) findViewById(R.id.activity_rank_listview);
        data = new ArrayList<HashMap<String,String>>();

        rankNumber = (TextView) findViewById(R.id.activity_rank_number);
        rankMileage = (TextView) findViewById(R.id.activity_rank_mileage);
        //loadServerData();

    }

    private void setAdapter() {
        adapter = new LinearLayoutForAdapter(this, data,
                R.layout.listview_item_rank_total, new String[] { "number", "nickname", "avatarurl", "mileage" },
                new int[] {
                        R.id.listview_item_rank_total_number,
                        R.id.listview_item_rank_total_nickname,
                        R.id.listview_item_rank_total_avatar,
                        R.id.listview_item_rank_total_mileage
                });
        listView.setAdapter(adapter);
    }

    private void loadServerData() {
        RequestParams params = new RequestParams();
        params.addBodyParameter("uid", String.valueOf(user.uid));
        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.POST,
                "http://bigbike.sinaapp.com/android/ranktotal", params,
                new RequestCallBack<String>() {

                    @Override
                    public void onStart() {
                        loading.show();
                    }

                    @Override
                    public void onLoading(long total, long current,
                                          boolean isUploading) {
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        //Log.e(TAG, "response: " + responseInfo.result);
                        loading.cancel();
                        try {
                            JSONObject response = new JSONObject(responseInfo.result);
                            int code = response.getInt("code");
                            if (code == 2000) {
                                JSONObject dataObj = response.getJSONObject("data");
                                String rank = dataObj.getString("rank");
                                String mileage = dataObj.getString("mileage");
                                JSONArray top = dataObj.getJSONArray("top");
                                List<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
                                HashMap<String, String> map = null;
                                for (int i = 0; i < top.length(); i++) {
                                    JSONObject obj = top.getJSONObject(i);
                                    map = new HashMap<String, String>();
                                    map.put("number", String.valueOf(i+1));
                                    map.put("nickname", obj.getString("nickname"));
                                    map.put("avatarurl", obj.getString("avatarurl"));
                                    map.put("mileage", obj.getString("mileage")+" km");
                                    temp.add(map);
                                }
                                synchronized (data) {
                                    data.addAll(temp);
                                }
                                setAdapter();
                                rankNumber.setText(rank);
                                rankMileage.setText(mileage);
                                body.setVisibility(View.VISIBLE);
                            }
                            if (code == 3000) {
                                String msg = response.getString("msg");
                                Log.e(TAG, msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Log.e(TAG, error.getExceptionCode() + ":" + msg);
                        loading.cancel();
                    }
                });
    }

}
