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
		        	Toast.makeText(context.getApplicationContext(), "同步数据启动", Toast.LENGTH_SHORT).show();
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
							myModel.onceDeleteHide(user.uid);  // 清空单程被删除的项目
							JSONObject data = response.getJSONObject("data");							
							JSONArray today = data.getJSONArray("today");
							JSONArray once = data.getJSONArray("once");
							JSONArray total = data.getJSONArray("total");
							int version = response.getInt("version");							
							// 保存最新APP版本号
							myConfig.appLatestVersion = version;
							myConfig.save();
							
							// ---------------------------------------------- //
							// 数据变化或UID变化，为使数据一至，需要重新建立Controller对象
							int state = myApp.localService.mCont.runState;  // 保存销毁前的运行状态
							int mode = myApp.localService.mCont.runMode;  // 保存销毁前的运行模式
							myApp.localService.destroyController();  // 销毁之前的Controller对象，停止/保存数据/释放
							
							// 变更数据
							myModel.jsonUpdateLocal(today, once, total);
							
							// 重建对象
							myApp.localService.createController();  // 重新建立Controller对象
							myApp.localService.mLoc.setController(myApp.localService.mCont);  // 将新建的Controller对象的引用传给Location对象
							if( state==MyController.IS_RUNNING & mode==MyConfig.MODE_HARDWARE )
								myApp.localService.mCont.start();  // 恢复之前的运行状态
							// ---------------------------------------------- //
							
							// 保存最后更新时间							
							user.lastSyncTime = Calendar.getInstance().getTimeInMillis();
							user.save();
							// 发送状态广播
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
		        	Toast.makeText(context.getApplicationContext(), "同步完成", Toast.LENGTH_SHORT).show();
		        }

		        @Override
		        public void onFailure(HttpException error, String msg) {
		            Log.e(TAG, error.getExceptionCode() + ":" + msg);
		            isRun = false;
		            Toast.makeText(context.getApplicationContext(), "同步失败", Toast.LENGTH_SHORT).show();
		        }
		});
	}

}
