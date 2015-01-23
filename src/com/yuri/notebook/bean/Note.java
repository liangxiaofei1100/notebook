package com.yuri.notebook.bean;

import java.util.Random;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.yuri.notebook.db.MetaData;
import com.yuri.notebook.db.MetaData.NoteColumns;

public class Note implements Parcelable {

	private long note_id;
	private String objectId;
	private String frontia_key;
	private String note_content;
	private String note_group;
	// use long time
	private long note_time;

	// 保存是否选中，在备份的时候用，该字段不保存到数据库中
	private boolean selected;
	
	public static final String ID = "id";
	public static final String OBJECTID = "objectId";
	public static final String CONTENT = "content";
	public static final String GROUP = "group";
	public static final String TIME = "time";
	public static final String FRONTIA_KEY = "note";
	public static final String FRONTIA_VALUE = "com.yuri.notebook.value";

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
	
	public void init(){
		this.frontia_key = FRONTIA_VALUE;
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
	
	public String getObjectId(){
		return objectId;
	}
	
	public static String getObjectId(long time){
		return Long.toHexString(time + new Random().nextInt(10000));
	}
	
	public void setObjectId(String objectId){
		this.objectId = objectId;
	}
	
	public String getFrontiaKey(){
		return frontia_key;
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
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(note_id);
		
		dest.writeString(objectId);
		dest.writeString(frontia_key);
		dest.writeString(note_content);
		dest.writeLong(note_time);
		dest.writeInt(selected ? 1 : 0);
	}

	public void readFromParcel(Parcel in) {
		note_id = in.readLong();
		
		objectId = in.readString();
		frontia_key = in.readString();
		note_content = in.readString();
		note_time = in.readLong();
		selected = in.readInt() == 1 ? true : false;
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
		note.init();
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
