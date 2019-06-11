package com.example.reride.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class LinearLayoutForListView extends LinearLayout {

	private LinearLayoutForAdapter adapter;
	private OnClickListener onClickListener = null;

	/**
	 * �󶨲���
	 */
	public void bindLinearLayout() {
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			View v = adapter.getView(i, null, null);
			v.setOnClickListener(this.onClickListener);
			addView(v, i);
		}
		//Log.v("countTAG", "" + count);
	}

	public LinearLayoutForListView(Context context) {
		super(context);

	}

	public LinearLayoutForListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub

	}

	/**
	 * ��ȡAdapter
	 * 
	 * @return adapter
	 */
	public LinearLayoutForAdapter getAdpater() {
		return adapter;
	}

	/**
	 * ��������
	 * 
	 * @param adpater
	 */
	public void setAdapter(LinearLayoutForAdapter adpater) {
		this.adapter = adpater;
		bindLinearLayout();
	}

	/**
	 * ��ȡ����¼�
	 * 
	 * @return
	 */
	public OnClickListener getOnclickListner() {
		return onClickListener;
	}

	/**
	 * ���õ���¼�
	 * 
	 * @param onClickListener
	 */
	public void setOnclickLinstener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

}
