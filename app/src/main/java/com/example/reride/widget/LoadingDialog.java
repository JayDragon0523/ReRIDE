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
     * �õ��Զ����progressDialog 
     * @param context 
     * @return Dialog
     */
    public static Dialog createDialog(Context context) {
    	
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_loading, null);
        
        LinearLayout layout = (LinearLayout)v.findViewById(R.id.dialog_loading_view);
        ImageView spaceshipImage = (ImageView)v.findViewById(R.id.dialog_loading_img);
        
        // ���ض���
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.dialog_loading);
        
        // ʹ��ImageView��ʾ����  
        spaceshipImage.startAnimation(anim);
  
        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// �����Զ�����ʽdialog
  
        loadingDialog.setCancelable(false);  // �������á����ؼ���ȡ��
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// ���ò���
        
        return loadingDialog;
    }

}
