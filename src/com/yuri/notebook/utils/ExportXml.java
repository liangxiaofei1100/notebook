package com.yuri.notebook.utils;

import java.io.File;
import java.io.FileOutputStream;

import org.xmlpull.v1.XmlSerializer;

import com.yuri.notebook.db.NoteMetaData;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.util.Xml;

//将记事导出到xml保存
public class ExportXml {
	private Context context;
	
	private FileOutputStream outputStream = null;
	
	private XmlSerializer serializer;
	
	private String mPath;
	
	public ExportXml(Context context, String path){
		this.context = context;
		this.mPath = path;
	}
	
	public void xmlStart() {
		System.out.println("mpath=" + mPath);
		File file = new File(mPath);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			outputStream = new FileOutputStream(file);
			serializer = Xml.newSerializer();
			serializer.setOutput(outputStream, "UTF-8");
			serializer.startDocument("UTF-8", true);
			serializer.startTag(null, "note");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean createXml() throws Exception{
		this.xmlStart();
		Cursor cursor = null;
		try {
			ContentResolver contentResolver = context.getContentResolver();
			String[] projection = new String[]{
					NoteMetaData.Note.TITLE,
					NoteMetaData.Note.CONTENT,
					NoteMetaData.Note.TIME
                    };
			
			cursor = contentResolver.query(NoteMetaData.Note.CONTENT_URI, 
					projection, null, null, 
					"_id ASC");
			if (cursor.moveToFirst()) {
				String title;  
                String content;  
                String date;  
                do {
                	title = cursor.getString(cursor.getColumnIndex(NoteMetaData.Note.TITLE));  
                    if (title == null) {  
                    	title = "";  
                    }  
                    content = cursor.getString(cursor.getColumnIndex(NoteMetaData.Note.CONTENT));  
                    if (content == null) {  
                    	content = "";  
                    }  
                    date = cursor.getString(cursor.getColumnIndex(NoteMetaData.Note.TIME));  
                    if (date == null) {  
                        date = "";  
                    }  
                    
                    serializer.startTag(null, "item");  
                    serializer.attribute(null, NoteMetaData.Note.TITLE, title);  
                    serializer.attribute(null, NoteMetaData.Note.CONTENT, content);  
                    serializer.attribute(null, NoteMetaData.Note.TIME, date);  
                    serializer.endTag(null, "item");
				} while (cursor.moveToNext());
			}else {
				return false;
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
			Log.e("SmsBackup", "SQLiteExeption:" + e.getMessage());
		}finally{
			if (cursor != null) {
				cursor.close();
			}
		}
		serializer.endTag(null, "note");
		serializer.endDocument();
		outputStream.flush();
		outputStream.close();
		return true;
	}
}
