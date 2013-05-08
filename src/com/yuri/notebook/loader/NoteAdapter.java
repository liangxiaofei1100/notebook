package com.yuri.notebook.loader;

import java.util.List;

import com.yuri.notebook.R;
import com.yuri.notebook.utils.DateFormatUtils;
import com.yuri.notebook.utils.Notes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class NoteAdapter extends BaseAdapter{
	private final LayoutInflater mInflater;
	
	private List<Notes> notes;
	private DateFormatUtils mDateFormatUtils;

	public NoteAdapter(Context context, List<Notes> data) {
		notes = data;
		mInflater = LayoutInflater.from(context);		
		
		mDateFormatUtils = new DateFormatUtils(context, System.currentTimeMillis());
	}
	
	private class ViewHolder{
		TextView snTextView,titleTextView,timeTextView;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		 View view = null;
		 ViewHolder holder = null;

         if (convertView == null) {
             view = mInflater.inflate(R.layout.homepage_list_item, parent, false);
             holder = new ViewHolder();
             holder.snTextView = (TextView)view.findViewById(R.id.id_text);
             holder.titleTextView = (TextView)view.findViewById(R.id.ItemTitle);
             holder.timeTextView = (TextView)view.findViewById(R.id.ItemText);
             
             view.setTag(holder);
         } else {
             view = convertView;
             holder = (ViewHolder) view.getTag();
         }
         
         Notes item = notes.get(position);
         holder.snTextView.setText(position+1 + "");
         holder.titleTextView.setText(item.getTitle());
         holder.timeTextView.setText("最后修改:" + mDateFormatUtils.getDateFormatString(item.getTime()));
		return view;
	}

	@Override
	public int getCount() {
		return notes.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
