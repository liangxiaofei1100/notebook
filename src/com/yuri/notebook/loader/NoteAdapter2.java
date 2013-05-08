package com.yuri.notebook.loader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yuri.notebook.R;
import com.yuri.notebook.db.NoteBookMetaData;
import com.yuri.notebook.utils.DateFormatUtils;
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
		TextView timeTextView = (TextView) view.findViewById(R.id.ItemText);

		snTextView.setText(String.valueOf(cursor.getPosition() + 1));
		String title = noteBook.getTitle();
		if (!TextUtils.isEmpty(mSearchString)) {
			SpannableString spannableString = new SpannableString(title);
			Matcher matcher = mPattern.matcher(title.toLowerCase());
			while (matcher.find()) {
				spannableString.setSpan(new ForegroundColorSpan(Color.RED),
						matcher.start(), matcher.end(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			titleTextView.setText(spannableString);
		} else {
			titleTextView.setText(title);
		}
		timeTextView.setText("最后修改:"
				+ mDateFormatUtils.getDateFormatString(noteBook.getTime()));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mInflater.inflate(R.layout.homepage_list_item, parent, false);
	}

}
