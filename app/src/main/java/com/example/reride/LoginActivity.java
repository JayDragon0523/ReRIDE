package com.example.reride;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reride.bean.User;
import com.example.reride.myclass.MyConfig;
import com.example.reride.myclass.MyController;
import com.example.reride.myclass.MyModel;
import com.example.reride.system.MyApp;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQToken;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static Tencent mTencent;
    private static String mAppid = "101616770";  // 腾讯APP_ID

    private LinearLayout qq;
    private CircleImageView avatarView;
    private TextView nicknameView;
    private ProgressDialog dialog;
    private MyApp myApp;
    private User user;
    private BitmapUtils bitmapUtils;
    private MyModel dataManager;
    private int cnt = 0;
    private String openid = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mTencent = Tencent.createInstance(mAppid, getApplicationContext());
        Log.e(TAG, "onCreate:mTencent: "+mTencent );
        myApp = (MyApp)this.getApplication();
        user = myApp.user;
        bitmapUtils = new BitmapUtils(getApplicationContext());
        bitmapUtils.configDefaultLoadingImage(R.drawable.app_logo);
        dataManager = new MyModel(getApplicationContext());

        ImageButton back = (ImageButton)findViewById(R.id.titlebar_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        qq = (LinearLayout)findViewById(R.id.activity_login_qq);
        qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTencent.login(LoginActivity.this, "all", new BaseUiListener());
            }
        });

        avatarView = (CircleImageView)findViewById(R.id.activity_login_avatar);
        nicknameView = (TextView)findViewById(R.id.activity_login_nickname);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Tencent.onActivityResultData(requestCode, resultCode, data, new BaseUiListener());

        if(requestCode == Constants.REQUEST_API) {
            if(resultCode == Constants.REQUEST_LOGIN) {
                Tencent.handleResultData(data, new BaseUiListener());
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataManager.release();
    }

    private class BaseUiListener implements IUiListener {

        @Override
        public void onCancel() {
            Toast.makeText(LoginActivity.this,"取消登陆",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(UiError arg0) {
            Toast.makeText(getApplicationContext(), "onError", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onComplete(Object arg0) {
            JSONObject response = (JSONObject)arg0;
            Log.e(TAG, "response: " + response.toString());
            try {
                if(openid != response.getString("openid")) cnt++;
                user.uid = cnt;
                user.openId = response.getString("openid");
                openid = user.openId;
                user.accessToken = response.getString("access_token");
                user.expiresIn = response.getString("expires_in");
                user.save();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            dialog = ProgressDialog.show(LoginActivity.this, null, "登录中...");

            // 获取一些QQ的基本信息，比如昵称，头像
            QQToken qqToken = mTencent.getQQToken();
            UserInfo info = new UserInfo(getApplicationContext(), qqToken);

            info.getUserInfo( new IUiListener() {

                @Override
                public void onCancel() {
                    dialog.dismiss();
                }

                @Override
                public void onError(UiError arg0) {
                    Log.i(TAG, arg0.errorMessage);
                    dialog.dismiss();
                }

                @Override
                public void onComplete(Object arg0) {
                    JSONObject response = (JSONObject)arg0;
                    Log.e(TAG, "response: " + response.toString());
                    try {
                        user.nickname = response.getString("nickname");
                        user.avatarUrl = response.getString("figureurl_qq_2");
                        user.gender = response.getString("gender");
                        user.province = response.getString("province");
                        user.city = response.getString("city");
                        user.save();
                        bitmapUtils.display(avatarView, user.avatarUrl, new BitmapLoadCallBack<View>() {
                            @Override
                            public void onLoadCompleted(View arg0, String arg1, Bitmap arg2, BitmapDisplayConfig arg3, BitmapLoadFrom arg4) {
                                ((CircleImageView)arg0).setImageBitmap(arg2);
                                nicknameView.setText(user.nickname);
                                if( user.gender.equals("") || user.province.equals("") ){
                                    Toast.makeText(getApplicationContext(), "QQ资料不完整，请完善后再试", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                LoginServer();
                            }
                            @Override
                            public void onLoadFailed(View arg0, String arg1, Drawable arg2) {
                                dialog.dismiss();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    private void LoginServer(){
        RequestParams params = new RequestParams();
        params.addBodyParameter("openid", user.openId);
        params.addBodyParameter("avatarurl", user.avatarUrl);
        params.addBodyParameter("nickname", user.nickname);
        params.addBodyParameter("gender", user.gender);
        params.addBodyParameter("province", user.province);
        params.addBodyParameter("city", user.city);
        HttpUtils http = new HttpUtils();
        http.send( HttpRequest.HttpMethod.POST,
                "http://bigbike.sinaapp.com/android/login",
                params,
                new RequestCallBack<String>() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        //Log.e(TAG, "response: " + responseInfo.result);
                        dialog.dismiss();
                        try {
                            JSONObject response = new JSONObject(responseInfo.result);
                            int code = response.getInt("code");
                            if( code==2000 || code==2001 || code==2002 ){
                                int uid = response.getInt("data");

                                // ---------------------------------------------- //
                                // 数据变化或UID变化，为使数据一至，需要重新建立Controller对象
                                int state = myApp.localService.mCont.runState;  // 保存销毁前的运行状态
                                int mode = myApp.localService.mCont.runMode;  // 保存销毁前的运行模式
                                myApp.localService.destroyController();  // 销毁之前uid为0的Controller对象，停止/保存数据/释放

                                // 变更数据
                                user.uid = uid;
                                user.save();
                                if( dataManager.existUnmanned() ){
                                    if( code==2001 || code==2002 ){  // 第一次注册登录的用户  或  服务器没有数据的用户
                                        dataManager.changeUnmannedTo(user.uid);  // 将uid为0的用户数据修改为登录后的用户uid
                                    }
                                }

                                // 重建对象
                                myApp.localService.createController();  // 按新uid重新建立Controller对象
                                myApp.localService.mLoc.setController(myApp.localService.mCont);  // 将新建的Controller对象的引用传给Location对象
                                if( state== MyController.IS_RUNNING & mode== MyConfig.MODE_HARDWARE )
                                    myApp.localService.mCont.start();  // 恢复之前的运行状态
                                // ---------------------------------------------- //

                                //启动同步
                                myApp.localService.mSync.start();
                                finish();
                            }
                            if ( code==3000 ) {
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
                        dialog.dismiss();
                    }
                });
    }
}
