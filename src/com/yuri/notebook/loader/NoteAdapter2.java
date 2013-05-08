package com.yuri.notebook.loader;

import com.yuri.notebook.R;
import com.yuri.notebook.db.NoteBookMetaData;
import com.yuri.notebook.utils.DateFormatUtils;
import com.yuri.notebook.utils.Notes;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class NoteAdapter2 extends CursorAdapter {

	private final LayoutInflater mInflater;
	private DateFormatUtils mDateFormatUtils;

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
		titleTextView.setText(noteBook.getTitle());
		timeTextView.setText("最后修改:"
				+ mDateFormatUtils.getDateFormatString(noteBook.getTime()));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mInflater.inflate(R.layout.homepage_list_item, parent, false);
	}

}
