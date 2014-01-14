package com.yuri.notebook.utils;

import com.yuri.notebook.db.NoteMetaData;
import com.yuri.notebook.db.NoteMetaData.Note;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class Notes implements Parcelable {

	private long note_id;
	private String note_title;
	private String note_content;
	private String note_group;
	// use long time
	private long note_time;

	// 保存是否选中，在备份的时候用，该字段不保存到数据库中
	private boolean selected;

	public static final Parcelable.Creator<Notes> CREATOR = new Parcelable.Creator<Notes>() {

		@Override
		public Notes createFromParcel(Parcel source) {
			return new Notes(source);
		}

		@Override
		public Notes[] newArray(int size) {
			return new Notes[size];
		}
	};
	
	public Notes(){}

	public Notes(long id) {
		note_id = id;
	}

	public Notes(String title) {
		note_title = title;
	}

	private Notes(Parcel in) {
		readFromParcel(in);
	}

	public long getId() {
		return note_id;
	}

	public void setId(long id) {
		note_id = id;
	}

	public String getTitle() {
		return note_title;
	}

	public void setTitle(String title) {
		note_title = title;
	}

	public String getContent() {
		return note_content;
	}

	public void setContent(String content) {
		note_content = content;
	}
	
	public String getGroup(){
		return note_group;
	}
	
	public void setGroup(String group){
		note_group = group;
	}

	public long getTime() {
		return note_time;
	}

	public void setTime(long time) {
		note_time = time;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeLong(note_id);

		dest.writeString(note_title);
		dest.writeString(note_content);
		// dest.writeString(note_time);
		dest.writeLong(note_time);
		dest.writeInt(selected ? 1 : 0);
	}

	public void readFromParcel(Parcel in) {
		note_id = in.readLong();
		note_title = in.readString();
		note_content = in.readString();
		// note_time = in.readString();
		note_time = in.readLong();
		selected = in.readInt() == 1 ? true : false;
	}

	public static Notes createNotebookFromCursor(Cursor cursor) {
		int id = cursor.getInt(cursor
				.getColumnIndex(NoteMetaData.Note._ID));
		String title = cursor.getString(cursor
				.getColumnIndex(NoteMetaData.Note.TITLE));
		String content = cursor.getString(cursor
				.getColumnIndex(NoteMetaData.Note.CONTENT));
		String group = cursor.getString(cursor.getColumnIndex(Note.GROUP));
		long time = cursor.getLong(cursor
				.getColumnIndex(NoteMetaData.Note.TIME));

		Notes note = new Notes(id);
		note.setTitle(title);
		note.setContent(content);
		note.setGroup(group);
		note.setTime(time);
		return note;
	}
}
