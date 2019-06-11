package com.example.reride.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.reride.R;


public class LoadingDialog {
	
	/** 
     * 得到自定义的progressDialog 
     * @param context 
     * @return Dialog
     */
    public static Dialog createDialog(Context context) {
    	
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_loading, null);
        
        LinearLayout layout = (LinearLayout)v.findViewById(R.id.dialog_loading_view);
        ImageView spaceshipImage = (ImageView)v.findViewById(R.id.dialog_loading_img);
        
        // 加载动画
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.dialog_loading);
        
        // 使用ImageView显示动画  
        spaceshipImage.startAnimation(anim);
  
        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog
  
        loadingDialog.setCancelable(false);  // 不可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
        
        return loadingDialog;
    }

}
