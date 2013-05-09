package com.yuri.notebook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.yuri.notebook.db.NoteBookMetaData;
import com.yuri.notebook.utils.NoteManager;
import com.yuri.notebook.utils.NoteUtil;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

public class NoteSettingFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener{
	private static final String TAG = "NoteSettingFragment";
	
	private ListPreference listPreference;
	private EditTextPreference editTextPreference;
	private ListPreference noteRestorePref;
	private ListPreference mColorSetPref;
	private ListPreference mFontSetPref;
	private SharedPreferences sp;
	private CheckBoxPreference mUsePwPref;
	
	private PreferenceScreen backupScreen;
	
	
	/**设置是否需要密码登陆，ture & false, default is false*/
	private boolean mUsePassword = false;
	/**设置密码登陆方式。0：图案登陆， 1：密码登陆; 默认登陆方式为图案登陆*/
	private String mLoginMode;
	//设置字体，0：较大字体；1：中等字体；2：较小字体
	private int mFont;
	//设置背景颜色：0：灰色；1：浅黄色；2：粉红色；3：蓝色
	private int mColor;
	
	private String mBackupMail = "";
	
	private CharSequence[] restore_values;
	private CharSequence[] restores ;
	
	private List<File> fileLists = new ArrayList<File>();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.note_setting_config);
		
		sp = getActivity().getSharedPreferences(NoteUtil.SHARED_NAME, Context.MODE_PRIVATE);
		
		mUsePassword = sp.getBoolean(NoteUtil.USE_PASSWORD, false);
		
		mUsePwPref = (CheckBoxPreference) findPreference("parent_need_pw_preference");
		mUsePwPref.setOnPreferenceClickListener(this);
		mUsePwPref.setChecked(mUsePassword);
		
		listPreference = (ListPreference) findPreference("list_preference");
		listPreference.setOnPreferenceChangeListener(this);
		
		mLoginMode = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(listPreference.getKey(), "-1");
		//设置默认选中值
//		listPreference.setValue(mLoginMode);
		//设置显示值
		listPreference.setSummary(listPreference.getEntries()[Integer.parseInt(mLoginMode)]);
		
		//set mail
		editTextPreference = (EditTextPreference) findPreference("edit_mail_preference");
		editTextPreference.setOnPreferenceChangeListener(this);
		
		if (null != editTextPreference.getText()) {
			editTextPreference.setSummary(editTextPreference.getText());
		}
		
		//backup
		backupScreen = (PreferenceScreen) findPreference("backup_screen");
		backupScreen.setOnPreferenceClickListener(this);
		
		//note_restore
		getBackupsFromSdCard(NoteUtil.DEFAULT_PATH);
		noteRestorePref = (ListPreference)findPreference("note_restore_list");
		if (null != noteRestorePref) {
			if (restores == null || restore_values == null) {
				noteRestorePref.setShouldDisableView(false);
			}else {
				noteRestorePref.setEntryValues(restore_values);
				noteRestorePref.setEntries(restores);
				noteRestorePref.setOnPreferenceChangeListener(this);
				noteRestorePref.setOnPreferenceClickListener(this);
			}
		}else {
			Log.e(TAG, "noteRestorePref is null");
		}
		
		mFontSetPref = (ListPreference) findPreference("note_font_set");
		mFontSetPref.setOnPreferenceChangeListener(this);
		
		mColorSetPref = (ListPreference) findPreference("note_background_color_set");
		mColorSetPref.setOnPreferenceChangeListener(this);
		
	}
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (listPreference == preference) {
			int index = listPreference.findIndexOfValue((String)newValue);
			listPreference.setSummary(listPreference.getEntries()[index]);
			//更改选中值
			listPreference.setValueIndex(index);
			
			//保存当前选中模式，方便其他Activity使用
			Editor editor = sp.edit();
			editor.putInt(NoteUtil.LOGIN_MODE, index);
			editor.commit();
		}else if (editTextPreference == preference) {
			mBackupMail = editTextPreference.getEditText().getText().toString().trim();
			editTextPreference.setSummary(mBackupMail);
			editTextPreference.setText(mBackupMail);
		}else if (noteRestorePref == preference) {
			int index = noteRestorePref.findIndexOfValue((String)newValue);
			final String value = noteRestorePref.getEntryValues()[index] + "";
			int start = value.lastIndexOf("/") + 1;
			int end = value.length() - 4;
			Builder dialog = new AlertDialog.Builder(getActivity());
			dialog.setTitle(R.string.note_restore);
			dialog.setMessage("确定从 " + value.substring(start, end) + " 恢复数据？");
			dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					RestoreTask restoreTask = new RestoreTask();
					restoreTask.execute(value);
				}
			});
			dialog.setNegativeButton(android.R.string.cancel, null);
			dialog.create().show();
		}else if (mFontSetPref == preference) {
			int index = mFontSetPref.findIndexOfValue((String)newValue);
			mFontSetPref.setValueIndex(index);
			
			//保存当前选中模式，方便其他Activity使用
			Editor editor = sp.edit();
			editor.putInt(NoteUtil.FONT_SET, index);
			editor.commit();
		}else if (mColorSetPref == preference) {
			int index = mColorSetPref.findIndexOfValue((String)newValue);
			mColorSetPref.setValueIndex(index);
			
			//保存当前选中模式，方便其他Activity使用
			Editor editor = sp.edit();
			editor.putInt(NoteUtil.COLOR_SET, index);
			editor.commit();
		}
		return false;
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (backupScreen == preference) {
			Intent intent = new Intent(getActivity(), BackupDeleteActivity.class);
			intent.putExtra(NoteUtil.MENU_MODE, NoteUtil.MENU_BACKUP);
			startActivity(intent);
		}else if (noteRestorePref == preference) {
			if (restores == null || restore_values == null) {
//				Toast.show
			}
		}else if (mUsePwPref == preference) {
			Editor editor = sp.edit();
			editor.putBoolean(NoteUtil.USE_PASSWORD, mUsePwPref.isChecked());
			editor.commit();
		}
		return true;
	}
	
	private void getBackupsFromSdCard(String path){
		fileLists.clear();
		File file = new File(path);
		if (!file.exists()) {
			Log.e(TAG, path + " is not exists");
			file.mkdirs();
		}
		
		File[] tempFiles = null;
		if (file.isDirectory()) {
			tempFiles = file.listFiles();
			for (int i = 0; i < tempFiles.length; i++) {
				if (file.listFiles()[i].getAbsolutePath().endsWith("zip")) {
					fileLists.add(tempFiles[i]);
				}else {
//					System.out.println("==>" + file.listFiles()[i].getPath());
				}
			}
			
			int size = fileLists.size();
			restore_values = new CharSequence[size];
			for (int i = 0; i < size; i++) {
				restore_values[i] = fileLists.get(i).getPath();
			}
			
			restores = new CharSequence[size];
			for (int i = 0; i < size; i++) {
				restores[i] = fileLists.get(i).getName().substring(0, fileLists.get(i).getName().length() - NoteUtil.EXTENSION_ZIP.length());
			}
			
		} else {
			// fileOptMenu(file);
		}
	}
	
	//恢复备份
	public class RestoreTask extends AsyncTask<String, String, String>{
		ProgressDialog progressDialog;
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			File file = new File(params[0]);
			String filePath;
			String fileName;
			String content = null;
			String title = null;
			try {
				//普通解压
//				String path = NoteManager.unZipFiles(file, NoteUtil.DEFAULT_PATH);
				//解加密压缩包
				NoteUtil.unZipForPw(params[0]);
				File tempFile = new File(NoteUtil.UNZIP_PATH);
				
				File[] files = tempFile.listFiles();
//				if(NoteUtil.DEBUG) Log.d(TAG, "path=" + path + "lenght=" + files.length);
				for (int i = 0; i < files.length; i++) {
					filePath = files[i].getPath();
					fileName = files[i].getName();
					title = fileName.substring(0, fileName.length() - NoteUtil.EXTENSION_TXT.length());
					content = NoteUtil.BufferReaderDemo(filePath);
					
					ContentValues values = new ContentValues();
					values.put(NoteBookMetaData.NoteBook.CONTENT, content);

					//数据库更新时，如果更新的类型不是int型，而是text的数据，必须用''包起来，如下面这一句
					int ret = getActivity().getContentResolver().update(NoteBookMetaData.NoteBook.CONTENT_URI, values,
							"title='" + title + "'", null);
					if (ret == 0) {
						values.put(NoteBookMetaData.NoteBook.TITLE, title);
						values.put(NoteBookMetaData.NoteBook.TIME, System.currentTimeMillis());
						getActivity().getContentResolver().insert(NoteBookMetaData.NoteBook.CONTENT_URI, values);
					}

				}
				
				NoteManager.isNeedRefresh = true;
				NoteManager.isFirst = true;
				
				//删除掉解压出来的文件夹
				NoteManager.recursionDeleteFile(tempFile);
			} catch (IOException e) {
				e.printStackTrace();
			}catch (SQLException e) {
				e.printStackTrace();
				Log.e(TAG, e.toString());
			}
			
			publishProgress("");
			return null;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			progressDialog = new ProgressDialog(getActivity());
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
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			if (progressDialog != null) {
				progressDialog.cancel();
			}
			Toast.makeText(getActivity(), "数据恢复成功！", Toast.LENGTH_SHORT).show();
			super.onProgressUpdate(values);
		}
		
	}
	
}
