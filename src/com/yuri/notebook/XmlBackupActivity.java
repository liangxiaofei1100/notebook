package com.yuri.notebook;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.yuri.notebook.utils.ExportXml;
import com.yuri.notebook.utils.NoteUtil;

/**使用xml文件进行数据备份*/
public class XmlBackupActivity extends Activity implements OnClickListener {
	private static final String TAG = "NoteBackupActivity";
	
	private EditText mEditText = null;
	private Button mBackupBtn = null;
	
	private String mBackupName = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_backup);
		
		mBackupName = "Note_" + NoteUtil.FILE_FORMAT.format(new Date());
		
		mEditText = (EditText) findViewById(R.id.backup_name);
		mEditText.setText(mBackupName);
		mEditText.setSelection(mBackupName.length());
		
		mBackupBtn = (Button)findViewById(R.id.backup_button);
		mBackupBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		//判断压缩位置是否存在
		File tempFile = new File(NoteUtil.DEFAULT_PATH);
		if (!tempFile.exists()) {
			boolean ret = tempFile.mkdirs();
			if (!ret) {
				Log.e(TAG, NoteUtil.DEFAULT_PATH + " make failed");
			}
		}
		
		NoteBackupTask noteBackupTask = new NoteBackupTask();
		noteBackupTask.execute(NoteUtil.DEFAULT_PATH + File.separator + 
				mEditText.getText().toString().trim() + ".xml");
	}
	
	private class NoteBackupTask extends AsyncTask<String, String, String>{
		ProgressDialog progressDialog;
		@Override
		protected String doInBackground(String... params) {
			ExportXml exportXml = new ExportXml(XmlBackupActivity.this, params[0]);
			try {
				exportXml.createXml();
			} catch (Exception e) {
				e.printStackTrace();
			}
			publishProgress("");
			return null;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(XmlBackupActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("数据备份中...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(String... values) {
			progressDialog.cancel();	
			XmlBackupActivity.this.finish();
			super.onProgressUpdate(values);
		}
		
	}
}
