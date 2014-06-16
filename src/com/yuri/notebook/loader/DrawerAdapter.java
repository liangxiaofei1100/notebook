package com.yuri.notebook.loader;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yuri.notebook.R;

public class DrawerAdapter extends BaseAdapter {
	
	private LayoutInflater mInflater = null;
	private List<String> mData = new ArrayList<String>();
	private int mSelectPosition = -1;
	
	private ColorStateList mBlue;
	private ColorStateList mBlack;

	public DrawerAdapter(Context context, List<String> data) {
		// TODO Auto-generated constructor stub
		mInflater = LayoutInflater.from(context);
		mData = data;
		
		mBlue = context.getResources().getColorStateList(android.R.color.holo_blue_light);
		mBlack = context.getResources().getColorStateList(android.R.color.black);
	}
	
	public void setSelectPosition(int position){
		mSelectPosition = position;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = mInflater.inflate(R.layout.loader_drawer_item, null);
		TextView textView = (TextView) view.findViewById(R.id.tv_drawer_item);
		textView.setText(mData.get(position));
		
		if (mSelectPosition == position) {
			textView.setTextColor(mBlue);
		}else {
			textView.setTextColor(mBlack);
		}
		return view;
	}

}
