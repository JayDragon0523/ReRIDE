package com.example.reride.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lidroid.xutils.BitmapUtils;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class LinearLayoutForAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private int resource;
	private List<? extends Map<String, ?>> data;
	private String[] from;
	private int[] to;
	
	private BitmapUtils bitmapUtils;

	public LinearLayoutForAdapter(Context context, List<? extends Map<String, ?>> data, int resouce, String[] from, int[] to) {
		this.data = data;
		this.resource = resouce;
		this.data = data;
		this.from = from;
		this.to = to;
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		bitmapUtils = new BitmapUtils(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@SuppressWarnings("unchecked")
	public String get(int position, Object key) {
		Map<String, ?> map = (Map<String, ?>) getItem(position);
		return map.get(key).toString();
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		convertView = mInflater.inflate(resource, null);
		Map<String, ?> item = data.get(position);
		int count = to.length;
		for (int i = 0; i < count; i++) {
			View v = convertView.findViewById(to[i]);
			bindView(v, item, from[i]);
		}
		convertView.setTag(position);
		return convertView;
	}

	/**
	 * 绑定视图
	 * 
	 * @param view
	 * @param item
	 * @param from
	 */
	private void bindView(View view, Map<String, ?> item, String from) {
		Object data = item.get(from);
		if (view instanceof TextView) {
			((TextView) view).setText(data == null ? "" : data.toString());
		}
		// 下载头像
		if (view instanceof CircleImageView){
			bitmapUtils.display(view, data.toString());
		}
	}
}
