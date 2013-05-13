package com.yuri.notebook;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import com.yuri.notebook.R;
import com.yuri.notebook.db.NoteBookMetaData;
import com.yuri.notebook.utils.NoteUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ZipBackupActivity extends Activity implements OnClickListener{
	private static final String TAG = "BackupDeleteActivity";
	
	private EditText mEditText;
	private Button mDoButton;
	
	private ArrayList<File> fileLists = new ArrayList<File>();
	
	private SharedPreferences sp;
	//设置要发送的Mail收件人
	private String mBackupMail;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_backup);
		
        sp = getSharedPreferences(NoteUtil.SHARED_NAME, MODE_PRIVATE);
        mBackupMail = sp.getString(NoteUtil.MAIL, null);
		
		/////////////
		String name = "Note_" + NoteUtil.FILE_FORMAT.format(new Date());
		mEditText = (EditText)findViewById(R.id.backup_name);
		mEditText.setText(name);
		
		mDoButton = (Button)findViewById(R.id.backup_button);
		mDoButton.setOnClickListener(this);
		///////////////
	}
	
	@Override
	public void onClick(View v) {
		// 判断压缩位置是否存在
		File tempFile = new File(NoteUtil.DEFAULT_PATH);
		if (!tempFile.exists()) {
			boolean ret = tempFile.mkdirs();
			if (!ret) {
				Log.e(TAG, NoteUtil.DEFAULT_PATH + " make failed");
			}
		}

		ZipBackupTask zipBackupTask = new ZipBackupTask();
		zipBackupTask.execute(NoteUtil.DEFAULT_PATH + File.separator
				+ mEditText.getText().toString().trim()
				+ NoteUtil.EXTENSION_ZIP);
	}

	//开始备份
	public void doBackup(File file){
		NoteUtil.showLongToast(ZipBackupActivity.this, "Saved to " + file.getPath());
		
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{mBackupMail});
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "NoteBook Backup");
		// intent.putExtra(android.content.Intent.EXTRA_TEXT,"This is Back up for notebook.\n\t"
		// + note.getContent());
		//
		intent.setType("application/octet-stream");
		//单纯的Mail
//		intent.setType("message/rfc822");

		// 当无法确认发送类型的时候使用如下语句
		//	intent.setType("*/*");
		// 当没有附件,纯文本发送时使用如下语句
		// intent.setType("plain/text");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		startActivity(Intent.createChooser(intent, "选择应用程序"));
	}
	
	//备份完毕后，删除文件
	public void deleteFiles(ArrayList<File> files){
		for(File file: files){
			file.delete();
		}
	}
	
	private class ZipBackupTask extends AsyncTask<String, File, String>{
		ProgressDialog progressDialog;
		@Override
		protected String doInBackground(String... params) {
			//备份的文件路径
			String desPath = params[0];
			
			//从数据库中将记事写入到文件中 begin
			Cursor cursor = null;
			try {
				ContentResolver contentResolver = getContentResolver();
				String[] projection = new String[]{
						NoteBookMetaData.NoteBook.TITLE,
						NoteBookMetaData.NoteBook.CONTENT,
						NoteBookMetaData.NoteBook.TIME
	                    };
				
				cursor = contentResolver.query(NoteBookMetaData.NoteBook.CONTENT_URI, 
						projection, null, null, 
						"_id ASC");
				if (cursor.moveToFirst()) {
					String title;  
	                String content;  
	                do {
	                	title = cursor.getString(cursor.getColumnIndex(NoteBookMetaData.NoteBook.TITLE));  
	                    if (title == null) {  
	                    	title = "";  
	                    }  
	                    content = cursor.getString(cursor.getColumnIndex(NoteBookMetaData.NoteBook.CONTENT));  
	                    if (content == null) {  
	                    	content = "";  
	                    }  
	                    
	                    File file = NoteUtil.fileWriteDemo(title, content);
						fileLists.add(file);
	                    
					} while (cursor.moveToNext());
				}
			} catch (SQLiteException e) {
				e.printStackTrace();
				Log.e("SmsBackup", "SQLiteExeption:" + e.getMessage());
			}finally{
				if (cursor != null) {
					cursor.close();
				}
			}
			//从数据库中将记事写入到文件中 end
			
			File forZipFile = new File(desPath);
			
			//普通压缩
//			NoteUtil.ZipFiles(zipFile, files);
			
			//加密压缩
			NoteUtil.zipForPw(forZipFile, fileLists);
			
			//打包后，删除文件
			deleteFiles(fileLists);
			
			//update ui
			publishProgress(forZipFile);
			return null;
		}
		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ZipBackupActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
			super.onPreExecute();
		}
		
		@Override
		protected void onProgressUpdate(File... values) {
			if (progressDialog != null) {
				progressDialog.cancel();	
			}
			
			doBackup(values[0]);
			ZipBackupActivity.this.finish();
			super.onProgressUpdate(values);
		}
	}
}
