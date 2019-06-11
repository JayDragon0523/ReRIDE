package com.example.reride.myclass;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.reride.bean.User;
import com.example.reride.system.MyApp;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class MySync {
	
	private static final String TAG = "MySync";
	
	private Context context;
	private boolean isRun = false;
	private MyApp myApp;
	private User user;
	private MyModel myModel;
	private MyConfig myConfig;
	
	public MySync(Context context, MyApp myApp ) {
		this.context = context;
		this.myApp = myApp;
		this.user = myApp.user;
		myModel = new MyModel(context);
		myConfig = new MyConfig(context);
	}
	
	public void release(){
		Log.w(TAG, "release");
		myModel.release();
		myModel = null;
	}
	
	public void start(){
		Log.w(TAG, "start");
		if( user.uid==0 ){
			Log.e(TAG, "start failure, uid not null");
			return;
		}
		if( isRun ){
			Log.e(TAG, "is running");
			return;
		}
		String data = myModel.getLocalDataByJSON(user.uid);
		//Log.e(TAG, data);
		RequestParams params = new RequestParams();
		params.addBodyParameter("uid", String.valueOf(user.uid));
		params.addBodyParameter("openid", user.openId);
		params.addBodyParameter("data", data);
		HttpUtils http = new HttpUtils();
		http.send( HttpMethod.POST,
		    "http://bigbike.sinaapp.com/android/sync",
		    params,
		    new RequestCallBack<String>() {
		        @Override
		        public void onStart() {
		        	isRun = true;
		        	Toast.makeText(context.getApplicationContext(), "ͬ����������", Toast.LENGTH_SHORT).show();
		        }
		        
		        @Override
		        public void onLoading(long total, long current, boolean isUploading) {
		        }

		        @Override
		        public void onSuccess(ResponseInfo<String> responseInfo) {
		        	//Log.e(TAG, "response: " + responseInfo.result);
		        	try {
			        	JSONObject response = new JSONObject(responseInfo.result);
			        	int code = response.getInt("code");
						if( code==2000 ){
							myModel.onceDeleteHide(user.uid);  // ��յ��̱�ɾ������Ŀ
							JSONObject data = response.getJSONObject("data");							
							JSONArray today = data.getJSONArray("today");
							JSONArray once = data.getJSONArray("once");
							JSONArray total = data.getJSONArray("total");
							int version = response.getInt("version");							
							// ��������APP�汾��
							myConfig.appLatestVersion = version;
							myConfig.save();
							
							// ---------------------------------------------- //
							// ���ݱ仯��UID�仯��Ϊʹ����һ������Ҫ���½���Controller����
							int state = myApp.localService.mCont.runState;  // ��������ǰ������״̬
							int mode = myApp.localService.mCont.runMode;  // ��������ǰ������ģʽ
							myApp.localService.destroyController();  // ����֮ǰ��Controller����ֹͣ/��������/�ͷ�
							
							// �������
							myModel.jsonUpdateLocal(today, once, total);
							
							// �ؽ�����
							myApp.localService.createController();  // ���½���Controller����
							myApp.localService.mLoc.setController(myApp.localService.mCont);  // ���½���Controller��������ô���Location����
							if( state==MyController.IS_RUNNING & mode==MyConfig.MODE_HARDWARE )
								myApp.localService.mCont.start();  // �ָ�֮ǰ������״̬
							// ---------------------------------------------- //
							
							// ����������ʱ��							
							user.lastSyncTime = Calendar.getInstance().getTimeInMillis();
							user.save();
							// ����״̬�㲥
							Intent intent = new Intent();
			                intent.setAction("cn.bigbike.cycling.SYNC_STATE");
			                context.sendBroadcast(intent);
						}
						if ( code==3000 ) {
							String msg = response.getString("msg");
							Log.e(TAG, msg);
						}
		        	} catch (JSONException e) {
						e.printStackTrace();
					}
		        	isRun = false;
		        	Toast.makeText(context.getApplicationContext(), "ͬ�����", Toast.LENGTH_SHORT).show();
		        }

		        @Override
		        public void onFailure(HttpException error, String msg) {
		            Log.e(TAG, error.getExceptionCode() + ":" + msg);
		            isRun = false;
		            Toast.makeText(context.getApplicationContext(), "ͬ��ʧ��", Toast.LENGTH_SHORT).show();
		        }
		});
	}

}
