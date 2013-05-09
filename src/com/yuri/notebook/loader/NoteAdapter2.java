package com.yuri.notebook.loader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yuri.notebook.R;
import com.yuri.notebook.utils.DateFormatUtils;
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

	private final LayoutInflater mInflater;
	private DateFormatUtils mDateFormatUtils;
	private String mSearchString;
	private Pattern mPattern;
	
	private static final int SHOW_LENGTH = 20;

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
		
		timeTextView.setText(mDateFormatUtils.getDateFormatString(noteBook.getTime()));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
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
