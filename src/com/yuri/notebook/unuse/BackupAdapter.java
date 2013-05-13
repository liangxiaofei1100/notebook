package com.yuri.notebook.unuse;

import java.util.List;

import com.yuri.notebook.R;
import com.yuri.notebook.R.id;
import com.yuri.notebook.utils.Notes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

//ArrayAdapter 一般用于只显示一行或者最多加一个imageview，不太适合用户复杂的布局，
//如果一定要用复杂的布局，最好使用baseAdapter
public class BackupAdapter extends ArrayAdapter<Notes>{
	private static final String TAG = "BackupAdapter";
	
	private class NoteHolder{
		private TextView mIdText;
		private TextView mTitleText;
		private CheckBox mCheckBox;
		private boolean mSelected;
	}
	
	private int mListItemLayoutId = -1;//the layout id
	private LayoutInflater mInflater = null;
	private boolean mSelectedArray[] = null;
	private static int mSize = -1;
	
	private List<Notes> mList;
	
	public BackupAdapter(Context context, int layoutResId, List<Notes> data) {
		super(context, layoutResId, data);
		// TODO Auto-generated constructor stub
		mListItemLayoutId = layoutResId;
		mInflater = LayoutInflater.from(context);
		mList = data;
		
		mSize = data.size();
		mSelectedArray = new boolean[mSize];
		//default all is unselected
		for (int i = 0; i < mSize; i++) {
			mSelectedArray[i] = false;
		}
	}
	
	//设置选中
	public void setChecked(int position){
		mSelectedArray[position] = ! mSelectedArray[position];
		notifyDataSetChanged();
	}
	
	//全选
	public void selectAll(){
		for (int i = 0; i < mSize; i++) {
			mSelectedArray[i] = true;
		}
		notifyDataSetChanged();
	}
	
	//取消全选
	public void cancelAll(){
		for (int i = 0; i < mSize; i++) {
			mSelectedArray[i] = false;
		}
		notifyDataSetChanged();
	}
	
	//获得是否选中
	public boolean getChecked(int position){
		return mSelectedArray[position];
	}
	
	//获得选中的Notebook id
	public long getSelectedId(int position){
		return mList.get(position).getId();
	}
	
	public Notes getSelectedNote(int position){
		return mList.get(position);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = null;
		NoteHolder holder = null;
		
		if (null == convertView) {
			view = mInflater.inflate(mListItemLayoutId,parent, false);
			holder = new NoteHolder();
			holder.mIdText = (TextView)view.findViewById(R.id.id_text);
			holder.mTitleText = (TextView)view.findViewById(R.id.ItemTitle);
			holder.mCheckBox = (CheckBox)view.findViewById(R.id.select_checkbox);
			holder.mSelected = false;
			
			view.setTag(holder);
		}else {
			view = convertView;
			holder = (NoteHolder)view.getTag();
		}
		
		Notes item = (Notes)getItem(position);
		if (null == item) {
			return null;
		}
		
		if (item != null) {
			holder.mIdText.setText( (position + 1) + ".");
			holder.mTitleText.setText(item.getTitle());
			holder.mCheckBox.setChecked(mSelectedArray[position]);
		}
		return view;
	}

}
