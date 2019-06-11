//package com.example.reride.myclass;
//
//import java.io.File;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import com.lidroid.xutils.HttpUtils;
//import com.lidroid.xutils.exception.HttpException;
//import com.lidroid.xutils.http.ResponseInfo;
//import com.lidroid.xutils.http.callback.RequestCallBack;
//import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
//
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.content.pm.PackageManager.NameNotFoundException;
//import android.net.Uri;
//import android.os.Environment;
//import android.util.Log;
//import android.view.View;
//import android.widget.Toast;
//
//public class MyVersion {
//
//	private static final String TAG = "MyVersion";
//	private Context context;
//	private MyConfig mConfig;
//
//	public MyVersion( Context context, MyConfig mConfig ) {
//		this.context = context;
//		this.mConfig = mConfig;
//	}
//
//	// 获取版本信息
//	public final static int getVersionName(Context context){
//		//getPackageName()是你当前类的包名，0代表是获取版本信息
//		PackageManager packageManager = context.getPackageManager();
//		PackageInfo packInfo = null;
//		try {
//			packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//		}
//		return packInfo==null?0:packInfo.versionCode;
//	}
//
//	// 检查最新版本
//	public void checkServerVersion(){
//		HttpUtils http = new HttpUtils();
//		http.send( HttpMethod.GET,
//				"http://bigbike.sinaapp.com/android/version",
//				new RequestCallBack<String>() {
//
//					@Override
//					public void onStart() {
//						Toast.makeText(context.getApplicationContext(), "正在检查", Toast.LENGTH_SHORT).show();
//					}
//
//					@Override
//					public void onLoading(long total, long current, boolean isUploading) {
//					}
//
//					@Override
//					public void onSuccess(ResponseInfo<String> responseInfo) {
//						//Log.e(TAG, "response: " + responseInfo.result);
//						try {
//							JSONObject response = new JSONObject(responseInfo.result);
//							int version = response.getInt("version");
//							String url = response.getString("url");
//							if( version>getVersionName(context) ){
//								mConfig.appLatestVersion = version;
//								mConfig.save();
//								dialogShow(url);
//							}else{
//								Toast.makeText(context.getApplicationContext(), "已是最新版本", Toast.LENGTH_SHORT).show();
//							}
//						} catch (JSONException e) {
//							e.printStackTrace();
//						}
//					}
//
//					@Override
//					public void onFailure(HttpException error, String msg) {
//						Log.e(TAG, error.getExceptionCode() + ":" + msg);
//						Toast.makeText(context.getApplicationContext(), "检查失败", Toast.LENGTH_SHORT).show();
//					}
//				});
//	}
//
//	// 从服务器中下载APK
//	private void downloadApk(String url) {
//		final ProgressDialog pd;    //进度条对话框
//		pd = new  ProgressDialog(context);
//		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//		pd.setMessage("正在下载更新");
//		pd.show();
//		HttpUtils http = new HttpUtils();
//		http.download(url,
//				Environment.getExternalStorageDirectory().getPath() + "/bigbike.apk",
//				false, // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
//				true, // 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
//				new RequestCallBack<File>() {
//
//					@Override
//					public void onStart() {
//					}
//
//					@Override
//					public void onLoading(long total, long current, boolean isUploading) {
//						pd.setMax((int)total);
//						pd.setProgress((int)current);
//					}
//
//					@Override
//					public void onSuccess(ResponseInfo<File> responseInfo) {
//						pd.cancel();
//						installApk( responseInfo.result );
//					}
//
//
//					@Override
//					public void onFailure(HttpException error, String msg) {
//						Log.e(TAG, error.getExceptionCode() + ":" + msg);
//						pd.cancel();
//						Toast.makeText(context.getApplicationContext(), "下载文件失败", Toast.LENGTH_SHORT).show();
//					}
//				});
//	}
//
//	//安装apk
//	protected void installApk(File file) {
//		Intent intent = new Intent();
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		intent.setAction(Intent.ACTION_VIEW);
//		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
//		context.startActivity(intent);
//	}
//
//	private void dialogShow(final String url){
//		final NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(context);
//		dialogBuilder
//				.withTitle("更新")
//				.withTitleColor("#FFFFFF")
//				.withDividerColor("#11000000")
//				.withMessage("发现新版本, 是否下载更新?")
//				.withMessageColor("#FFFFFFFF")
//				.withDialogColor("#FFE74C3C")
//				.withDuration(200)
//				.withEffect(Effectstype.SlideBottom)
//				.withButton1Text("下载")
//				.withButton2Text("忽略")
//				.isCancelableOnTouchOutside(true)
//				.setButton1Click(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						dialogBuilder.cancel();
//						downloadApk(url);
//					}
//				})
//				.setButton2Click(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						dialogBuilder.cancel();
//					}
//				})
//				.show();
//	}
//
//}
