package com.yuri.notebook.loader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yuri.notebook.R;
import com.yuri.notebook.utils.DateFormatUtils;
import com.yuri.notebook.utils.LogUtils;
import com.yuri.notebook.utils.NoteUtil;
import com.yuri.notebook.utils.Notes;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class NoteAdapter2 extends CursorAdapter {
	private static final String TAG = "NoteAdapter2";

	private final LayoutInflater mInflater;
	private DateFormatUtils mDateFormatUtils;
	private String mSearchString;
	private Pattern mPattern;
	
	private static final int THEME_COLOR_DEFAULT = 0x7F33b5e5;
	
	private int mCount = 0;
	
	//default is normal mode
	private int mMode = 0;
	
	// 定义一个数组，保存每一个item是否被选中
	private boolean mCheckedArray[] = null;

	/** 被选中的数量 */
	private int mCheckedCount = 0;

	private static final int SHOW_LENGTH = 20;
	
	//for menu
	public void setMode(int mode){
		this.mMode = mode;
	}
	
	public boolean isMode(int mode){
		return mMode == mode;
	}
	
//	public int getSelectedPoition(){
//		
//	}
	
	public void setChecked(int position){
		mCheckedArray[position] = !mCheckedArray[position];
		LogUtils.d(TAG, "mCheckedArray[" + position + "]=" + mCheckedArray[position]);
		notifyDataSetChanged();
	}
	
	public int getCheckedItemCount(){
		int selectedCount = 0;
		for (int i = 0; i < mCount; i++) {
			if (mCheckedArray[i]) {
				selectedCount ++;
			}
		}
		LogUtils.d(TAG, "getCheckedItemCount = " + selectedCount);
		return selectedCount;
	}
	
	public void unSelectedAll(){
		for (int i = 0; i < mCount; i++) {
			mCheckedArray[i] = false;
		}
		notifyDataSetChanged();
	}
	
	public void selectAll(){
		for (int i = 0; i < mCount; i++) {
			mCheckedArray[i] = true;
		}
		notifyDataSetChanged();
	}
	
	public boolean isSelected(int position){
		return mCheckedArray[position];
	}
	//for menu
	public String getSearchString() {
		return mSearchString;
	}

	public void setSearchString(String searchString) {
		mSearchString = searchString;
		if (!TextUtils.isEmpty(searchString)) {
			mPattern = Pattern.compile(searchString.toLowerCase());
		}
	}

	public NoteAdapter2(Context context, Cursor c, int flags) {
		super(context, c, flags);
		mInflater = LayoutInflater.from(context);
		mDateFormatUtils = new DateFormatUtils(context,
				System.currentTimeMillis());
	}
	
	@Override
	protected void onContentChanged() {
		// TODO Auto-generated method stub
		super.onContentChanged();
		
	}
	
	@Override
	public Cursor swapCursor(Cursor newCursor) {
		// TODO Auto-generated method stub
		//当cursor改变后，重新new mCheckArray
		Cursor oldCursor = super.swapCursor(newCursor);
		mCount = newCursor.getCount();
		LogUtils.d(TAG, "new COunt = " + mCount);
		mCheckedArray = new boolean[mCount];
		for (int i = 0; i < mCount; i++) {
			mCheckedArray[i] = false;
		}
		return oldCursor;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Notes noteBook = Notes.createNotebookFromCursor(cursor);
		
		TextView snTextView = (TextView) view.findViewById(R.id.id_text);
		TextView titleTextView = (TextView) view.findViewById(R.id.ItemTitle);
		TextView contentTextView = (TextView) view.findViewById(R.id.ItemContent);
		TextView timeTextView = (TextView) view.findViewById(R.id.ItemTime);
		snTextView.setText(String.valueOf(cursor.getPosition() + 1));
		String title = noteBook.getTitle();
		String content = noteBook.getContent();
		if (!TextUtils.isEmpty(mSearchString)) {
			//title search
			hightLightMatcherText(titleTextView, title);
			
			//content search
			hightLightMatcherTextCut(contentTextView, content);
		} else {
			titleTextView.setText(title);
			
			int enter = content.indexOf(NoteUtil.ENTER);
			if (enter != -1) {//有回车
				content = content.substring(0, enter);
			}
			contentTextView.setText(content);
		}
		
		if (mMode == NoteUtil.MODE_MENU) {
			updateBackout(cursor.getPosition(), view);
		}else {
			// do nothing
			view.setBackgroundColor(Color.TRANSPARENT);
		}
		
		timeTextView.setText(mDateFormatUtils.getDateFormatString(noteBook.getTime()));
	}
	
	private void updateBackout(int position, View view){
		if (mCheckedArray[position]) {
			view.setBackgroundColor(THEME_COLOR_DEFAULT);
		}else {
			view.setBackgroundColor(Color.TRANSPARENT);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		/**TODO 碰到难题了
		 * 这里的mCount只有在第一次进入的时候才会初始化，当新添家一个的时候，这个count不会改变
		 * 因为不会再调用newView，而是直接调用bindView，但是这个CheckedArray初始化不能在bindView吧
		 * 难！难！！难啊!!!
		 * 
		 * 已解决，覆写swapCusor。在那里对mCheckArray数组进行变更
		 * */
//		mCount = cursor.getCount();
//		mCheckedArray = new boolean[mCount];
//		for (int i = 0; i < mCount; i++) {
//			mCheckedArray[i] = false;
//		}
		return mInflater.inflate(R.layout.homepage_list_item, parent, false);
	}
	
	/**
	 * 高亮显示指定字符串中的字符
	 * @param view 显示字符串的textview
	 * @param text 初始字符串
	 */
	public void hightLightMatcherText(TextView view, String text){
		SpannableString spannableString = new SpannableString(text);
		Matcher matcher = mPattern.matcher(text.toLowerCase());
		while (matcher.find()) {
			spannableString.setSpan(new ForegroundColorSpan(Color.RED),
					matcher.start(), matcher.end(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		view.setText(spannableString);
	}
	
	/**
	 * 截取指定字符串的一部分，并高亮显示其中的指定字符串
	 * @param view  显示字符串的textview
	 * @param text  初始字符串
	 */
	public void hightLightMatcherTextCut(TextView view, String text){
		SpannableString spannableString = null;
		Matcher matcher = mPattern.matcher(text.toLowerCase());
		
		if (matcher.find()) {
			int start, end;
			int span_start = 0;
			if (matcher.start() - SHOW_LENGTH < 0) {
				start = 0;
				span_start = matcher.start();
			} else {
				start = matcher.start() - SHOW_LENGTH;
				span_start = SHOW_LENGTH;
			}

			if (matcher.end() + SHOW_LENGTH > text.length()) {
				end = text.length();
			} else {
				end = matcher.end() + SHOW_LENGTH;
			}

			text = text.substring(start, end);

			spannableString = new SpannableString(text);
			spannableString.setSpan(new ForegroundColorSpan(Color.RED),
					span_start, span_start + mSearchString.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			view.setText(spannableString);
		}
	}

}
