package com.yuri.notebook;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.yuri.notebook.db.NoteBookMetaData;
import com.yuri.notebook.loader.NoteLoader;
import com.yuri.notebook.utils.NoteManager;
import com.yuri.notebook.utils.NoteUtil;
import com.yuri.notebook.utils.Notes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

/**
 * 备份和删除多个记事共用一个Activity UI
 * 
 * */
public class BackupDeleteActivity extends Activity implements OnItemClickListener, OnClickListener{
	private static final String TAG = "BackupDeleteActivity";
	
	//总列表
	private List<Notes> mAllList;
	
	//选中的列表
	private List<Notes> mSelectedLists = new ArrayList<Notes>();
	
	private BackupDeleteAdapter mBackupAdapter;
	private ListView mListView;
	
	private Button mDoButton;
	
	private BackupTask mBackupTask;
	private ArrayList<File> fileLists = new ArrayList<File>();
	
	private int mode;
	
	private SharedPreferences sp;
	//设置要发送的Mail收件人
	private String mBackupMail;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multi_select);
		
		//标题栏返回
		NoteUtil.setShowTitleBackButton(BackupDeleteActivity.this);
        
        mode = getIntent().getIntExtra(NoteUtil.MENU_MODE, -1);
        
        sp = getSharedPreferences(NoteUtil.SHARED_NAME, MODE_PRIVATE);
        mBackupMail = sp.getString(NoteUtil.MAIL, null);
        
        mAllList = NoteLoader.mList;
        
        mListView = (ListView) findViewById(R.id.ListViewAppend);
		
		mBackupAdapter = new BackupDeleteAdapter(BackupDeleteActivity.this, R.layout.multi_list_item, mAllList);
		mListView.setAdapter(mBackupAdapter);
		mListView.setOnItemClickListener(this);
		
		mDoButton = (Button)findViewById(R.id.do_button);
		mDoButton.setOnClickListener(this);
		if (NoteUtil.MENU_BACKUP == mode) {
			setTitle(R.string.menu_backup);
			mDoButton.setText(R.string.menu_backup);
		}else if (NoteUtil.MENU_DELETE_MULTI == mode) {
			setTitle(R.string.menu_delete_multi);
			mDoButton.setText(R.string.menu_delete);
		}
		
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position < 0 || position > mAllList.size())
			return;

		mBackupAdapter.setChecked(position);
	}
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.do_button) {
			if (NoteUtil.MENU_BACKUP == mode) {
				Notes note;
				for (int pos = mAllList.size() - 1; pos >= 0; pos--) {
					if (mBackupAdapter.getChecked(pos)) {
						note = mBackupAdapter.getSelectedNote(pos);
						File file = NoteUtil.fileWriteDemo(note.getTitle(), note.getContent());
						fileLists.add(file);
					}
				}
				
				if (fileLists.size() <= 0) {
					NoteUtil.showToast(this, R.string.tip_msg);
				}else {
					if (mBackupTask != null && mBackupTask
							.getStatus() == AsyncTask.Status.RUNNING) {
						return;
					}else {
						mBackupTask = new BackupTask();
						mBackupTask.execute(fileLists);
					}
				}
				
			}else if (NoteUtil.MENU_DELETE_MULTI == mode) {
				mSelectedLists.clear();
				Notes note;
				for (int pos = mAllList.size() - 1; pos >= 0; pos--) {
					if (mBackupAdapter.getChecked(pos)) {
						note = mBackupAdapter.getSelectedNote(pos);
						mSelectedLists.add(note);
					}
				}
				
				if (mSelectedLists.size() <= 0) {
					NoteUtil.showToast(this, R.string.tip_msg);
				}else {
					new AlertDialog.Builder(BackupDeleteActivity.this)
					.setTitle(R.string.menu_delete)
					.setMessage(getResources().getString(R.string.delete_msg_multi, mSelectedLists.size()))
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//循环删除
							for (int j = 0; j < mSelectedLists.size(); j++) {
								Uri uri = Uri.parse(NoteBookMetaData.NoteBook.CONTENT_URI + "/" + mSelectedLists.get(j).getId());
								getContentResolver().delete(uri, null, null);
							}
							
							NoteManager.isNeedRefresh = true;
							NoteManager.isFirst = true;
							
							BackupDeleteActivity.this.finish();
						}
					})
					.setNegativeButton(android.R.string.cancel, null)
					.setCancelable(false)
					.create().show();
				}
				
			}
		}
	}

	//开始备份
	public void doBackup(File file,String mail){
		NoteUtil.showLongToast(BackupDeleteActivity.this, "Saved to " + file.getPath());
		
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{mail});
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
	public void deleteFiles(File[] files){
		for (int i = 0; i < files.length; i++) {
			files[i].delete();
		}
	}
	
	private class BackupTask extends AsyncTask<ArrayList<File>, File, String>{
		ProgressDialog progressDialog;
		@Override
		protected String doInBackground(ArrayList<File>... params) {
			// TODO Auto-generated method stub
			String desPath = NoteUtil.DEFAULT_PATH + File.separator +  NoteUtil.FILE_FORMAT.format(new Date()) + NoteUtil.EXTENSION_ZIP;
//			String backupPath = NoteUtil.DATA_PATH + NoteUtil.AUDIO_FILE_FORMAT.format(new Date()) + NoteUtil.EXTENSION_ZIP;
			//判断压缩位置是否存在
			File tempFile = new File(NoteUtil.DEFAULT_PATH);
			if (!tempFile.exists()) {
				boolean ret = tempFile.mkdirs();
				if (!ret) {
					Log.e(TAG, NoteUtil.DEFAULT_PATH + " make failed");
				}
			}
			
			File forZipFile = new File(desPath);
//			File zipFile = new File(NoteUtil.DEFAULT_PATH + NoteUtil.AUDIO_FILE_FORMAT.format(new Date()) + NoteUtil.EXTENSION_ZIP);
			File[] files = new File[params[0].size()];
			for (int i = 0; i < params[0].size(); i++) {
				files[i] = params[0].get(i);
			}
			
			try {
				//普通压缩
//					NoteUtil.ZipFiles(zipFile, files);
				//加密压缩
				NoteUtil.zipForPw(forZipFile, files);
			}
//			 catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			finally{
				//打包后，删除文件
				deleteFiles(files);
			}
			
			//update ui
			publishProgress(forZipFile);
			return null;
		}
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			progressDialog = new ProgressDialog(BackupDeleteActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
			super.onPreExecute();
		}
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}
		@Override
		protected void onProgressUpdate(File... values) {
			// TODO Auto-generated method stub
			if (progressDialog != null) {
				progressDialog.cancel();	
			}
			
			doBackup(values[0],mBackupMail);
			BackupDeleteActivity.this.finish();
			super.onProgressUpdate(values);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuItem selectall_item = menu.add(0, NoteUtil.MENU_SELETEALL, 1, R.string.seleteall).setIcon(R.drawable.ic_select_all);
		selectall_item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		MenuItem cancelall_item = menu.add(0, NoteUtil.MENU_CANCELALL, 1, R.string.cancelall).setIcon(R.drawable.ic_clear_select);
		cancelall_item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case NoteUtil.MENU_SELETEALL:
			mBackupAdapter.selectAll();
			break;
		case NoteUtil.MENU_CANCELALL:
			mBackupAdapter.cancelAll();
			break;
		case android.R.id.home://默认情况下，标题图标的ID
			BackupDeleteActivity.this.finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
