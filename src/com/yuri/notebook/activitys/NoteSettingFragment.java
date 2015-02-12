package com.yuri.notebook.activitys;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindCallback;
import cn.bmob.v3.listener.FindListener;

import com.yuri.notebook.R;
import com.yuri.notebook.bean.Note;
import com.yuri.notebook.db.MetaData;
import com.yuri.notebook.db.MetaData.NoteColumns;
import com.yuri.notebook.utils.LogUtils;
import com.yuri.notebook.utils.NoteUtil;

public class NoteSettingFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener{
	private static final String TAG = "NoteSettingFragment";
	
	private SharedPreferences sp;
	private CheckBoxPreference mUsePwPref;
	
	private PreferenceScreen mSyncScreenPref;
	private static final String KEY_SYNC = "sync_screen";
	private static final String KEY_PW = "parent_need_pw_preference";
	
	private ListPreference mLoginPreference;
	private static final String KEY_LOGIN = "login_preference";
	
	
	
	/**设置是否需要密码登陆，ture & false, default is false*/
	private boolean mUsePassword = false;
	/**设置密码登陆方式。0：图案登陆， 1：密码登陆; 默认登陆方式为图案登陆*/
	private String mLoginMode;
	
	private List<File> fileLists = new ArrayList<File>();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.note_setting_config);
		
		sp = getActivity().getSharedPreferences(NoteUtil.SHARED_NAME, Context.MODE_PRIVATE);
		
		mUsePassword = sp.getBoolean(NoteUtil.USE_PASSWORD, false);
		
		mUsePwPref = (CheckBoxPreference) findPreference(KEY_PW);
		mUsePwPref.setOnPreferenceClickListener(this);
		mUsePwPref.setChecked(mUsePassword);
		
		mLoginPreference = (ListPreference) findPreference(KEY_LOGIN);
		mLoginPreference.setOnPreferenceChangeListener(this);
		
		mLoginMode = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(mLoginPreference.getKey(), "-1");
		//设置默认选中值
//		listPreference.setValue(mLoginMode);
		//设置显示值
		mLoginPreference = (ListPreference) findPreference(KEY_LOGIN);
		mLoginPreference.setSummary(mLoginPreference.getEntries()[Integer.parseInt(mLoginMode)]);
		
		//sync
		mSyncScreenPref = (PreferenceScreen) findPreference(KEY_SYNC);
		mSyncScreenPref.setOnPreferenceClickListener(this);
		
		getActivity().setResult(Activity.RESULT_CANCELED);
	}
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (mLoginPreference == preference) {
			int index = mLoginPreference.findIndexOfValue((String)newValue);
			mLoginPreference.setSummary(mLoginPreference.getEntries()[index]);
			//更改选中值
			mLoginPreference.setValueIndex(index);
			
			//保存当前选中模式，方便其他Activity使用
			Editor editor = sp.edit();
			editor.putInt(NoteUtil.LOGIN_MODE, index);
			editor.commit();
		}
		return false;
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (mSyncScreenPref == preference) {
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setMessage("同步中...");
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
			
			BmobQuery<Note> query = new BmobQuery<Note>();
			query.findObjects(getActivity(), new FindListener<Note>() {
				
				@Override
				public void onSuccess(List<Note> arg0) {
					LogUtils.d(TAG, "query.success:" + arg0.size());
					Message message = new Message();
					message.what  = MSG_QUERY_ALL_OVER;
					Bundle bundle = new Bundle();
					bundle.putParcelableArrayList("notelist", (ArrayList<? extends Parcelable>) arg0);
					message.setData(bundle);
					message.setTarget(mHandler);
					message.sendToTarget();
				}
				
				@Override
				public void onError(int arg0, String arg1) {
					// TODO Auto-generated method stub
					LogUtils.e(TAG, "query.failed:" + arg1);
				}
			});
			
			
		}else if (mUsePwPref == preference) {
			Editor editor = sp.edit();
			editor.putBoolean(NoteUtil.USE_PASSWORD, mUsePwPref.isChecked());
			editor.commit();
		}
		return true;
	}
	
	ProgressDialog mProgressDialog;
	public class SyncDataTask extends AsyncTask<List<Note>, String, String>{

		@Override
		protected String doInBackground(List<Note>... params) {
			List<Note> notesList = params[0];
			LogUtils.d(TAG, "doInBackground.size=" + notesList.size());
			ContentResolver conResolver = getActivity().getContentResolver();
			for (Note note: notesList) {
				String objectId = note.getObjectId();
				String group = note.getGroup();
				String content = note.getContent();
				long date = note.getTime();
				
				String selection = NoteColumns.OBJECT_ID + "='" + objectId + "'";
				Cursor cursor = conResolver.query(NoteColumns.CONTENT_URI, null, selection, null, null);
				if (cursor != null && cursor.getCount() > 0) {
					continue;
				}
				
				ContentValues values = new ContentValues();
				values.put(MetaData.NoteColumns.OBJECT_ID, objectId);
				values.put(MetaData.NoteColumns.CONTENT, content);
				values.put(MetaData.NoteColumns.TIME, date);
				
				//数据库更新时，如果更新的类型不是int型，而是text的数据，必须用''包起来，如下面这一句
//				int ret = getActivity().getContentResolver().update(NoteMetaData.Note.CONTENT_URI, values,
//						"title='" + title + "'", null);
				values.put(MetaData.NoteColumns.GROUP, group);
				conResolver.insert(MetaData.NoteColumns.CONTENT_URI, values);
			}
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (mProgressDialog != null) {
				mProgressDialog.cancel();
			}
			Toast.makeText(getActivity(), "同步完成！", Toast.LENGTH_SHORT).show();
		}
	}
	
	public static final int MSG_QUERY_ALL_OVER = 0x01;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			LogUtils.d(TAG, "handler.what=" + msg.what);
			switch (msg.what) {
			case MSG_QUERY_ALL_OVER:
				Bundle bundle = msg.getData();
				List<Note> list = (List<Note>) bundle.get("notelist");
				LogUtils.d(TAG, "handlerMessage.size=" + list.size());
				
				SyncDataTask syncDataTask = new SyncDataTask();
				syncDataTask.execute(list);
				break;

			default:
				break;
			}
		};
	};
	
}
