package com.yuri.notebook.bean;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import cn.bmob.v3.BmobObject;

import com.yuri.notebook.db.MetaData;
import com.yuri.notebook.db.MetaData.NoteColumns;

public class Note extends BmobObject implements Parcelable {

	private long note_id;
	private String note_content;
	private String note_group;
	// use long time
	private long note_time;

	public static final Parcelable.Creator<Note> CREATOR = new Parcelable.Creator<Note>() {

		@Override
		public Note createFromParcel(Parcel source) {
			return new Note(source);
		}

		@Override
		public Note[] newArray(int size) {
			return new Note[size];
		}
	};
	
	public Note(){
	}
	
	public Note(long id){
		setId(id);
	}

	private Note(Parcel in) {
		readFromParcel(in);
	}

	public long getId() {
		return note_id;
	}

	public void setId(long id) {
		note_id = id;
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(note_id);
		
		dest.writeString(note_content);
		dest.writeString(note_group);
		dest.writeLong(note_time);
	}

	public void readFromParcel(Parcel in) {
		note_id = in.readLong();
		
		note_content = in.readString();
		note_group = in.readString();
		note_time = in.readLong();
	}

	public static Note createNotebookFromCursor(Cursor cursor) {
		int id = cursor.getInt(cursor
				.getColumnIndex(MetaData.NoteColumns._ID));
		String content = cursor.getString(cursor
				.getColumnIndex(MetaData.NoteColumns.CONTENT));
		String group = cursor.getString(cursor.getColumnIndex(NoteColumns.GROUP));
		long time = cursor.getLong(cursor
				.getColumnIndex(MetaData.NoteColumns.TIME));

		Note note = new Note();
		note.setId(id);
		note.setContent(content);
		note.setGroup(group);
		note.setTime(time);
		return note;
	}
	
	public static Note getNoteFromCursor(Cursor cursor) {
		int id = cursor.getInt(cursor
				.getColumnIndex(MetaData.NoteColumns._ID));
		String content = cursor.getString(cursor
				.getColumnIndex(MetaData.NoteColumns.CONTENT));
		String group = cursor.getString(cursor.getColumnIndex(NoteColumns.GROUP));
		long time = cursor.getLong(cursor
				.getColumnIndex(MetaData.NoteColumns.TIME));

		Note note = new Note();
		note.setId(id);
		note.setContent(content);
		note.setGroup(group);
		note.setTime(time);
		return note;
	}
}
