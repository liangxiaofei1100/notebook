package com.yuri.notebook.loader;

import java.util.ArrayList;
import java.util.List;

import com.yuri.notebook.AppendNoteActivity;
import com.yuri.notebook.BackupDeleteActivity;
import com.yuri.notebook.CheckNoteActivity;
import com.yuri.notebook.EditNoteActivity;
import com.yuri.notebook.NoteSettingActivity;
import com.yuri.notebook.R;
import com.yuri.notebook.db.NoteBookMetaData;
import com.yuri.notebook.utils.NoteManager;
import com.yuri.notebook.utils.NoteUtil;
import com.yuri.notebook.utils.Notes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NoteLoader extends Activity implements OnItemClickListener, OnItemLongClickListener, OnClickListener {
	private static final String TAG = "NoteLoader";
	//用户显示数据
	private ListView mListView;
	private TextView mTipsView;
	private ImageView mAddTipsBtn;

	//数据显示Adapter
	private NoteAdapter mAdapter;

	//临时性保存数据
	public static  List<Notes> mList;

	//异步Task
	private GetNoteTask mGetNoteTask;

	private Context mContext;

	//当前选中item的Notes
	private Notes mCurrentNotes;
	//当前选中Item的position
	private int mCurrentPos;
	
	private int back = 0;
	
	//记录退出的时间点
	private long mExitTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.homepage);

		mContext = this;
		mListView = (ListView) findViewById(R.id.ListViewAppend);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		mListView.setOnCreateContextMenuListener(new ListOnCreate());

		mTipsView = (TextView) findViewById(R.id.tips);
		mAddTipsBtn = (ImageView) findViewById(R.id.add_tips_btn);
		mAddTipsBtn.setOnClickListener(this);

		getData();
	}

	@Override
	protected void onResume() {
		if (NoteManager.isNeedRefresh) {
			NoteManager.isNeedRefresh = false;
			getData();
		}
		super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(0, NoteUtil.MENU_ADD, 0, R.string.menu_add).setIcon(R.drawable.ic_menu_compose);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//		menu.add(0, NoteUtil.MENU_BACKUP, 1, R.string.menu_backup);
		menu.add(0, NoteUtil.MENU_DELETE_MULTI, 2, R.string.menu_delete_multi);
		menu.add(0, NoteUtil.MENU_SETTING, 3, R.string.setting);
		if (NoteUtil.DEBUG) {
			menu.add(0, NoteUtil.MENU_RECOVER, 4, "Recover");
		}
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case NoteUtil.MENU_ADD:
			intent = new Intent(NoteLoader.this, AppendNoteActivity.class);
			startActivity(intent);
			break;
		case NoteUtil.MENU_DELETE_MULTI:
			intent = new Intent(NoteLoader.this, BackupDeleteActivity.class);
			intent.putExtra(NoteUtil.MENU_MODE, NoteUtil.MENU_DELETE_MULTI);
			startActivity(intent);
			break;
		case NoteUtil.MENU_BACKUP:
			intent = new Intent(NoteLoader.this, BackupDeleteActivity.class);
			intent.putExtra(NoteUtil.MENU_MODE, NoteUtil.MENU_BACKUP);
			startActivity(intent);
					
			break;
		case NoteUtil.MENU_RECOVER://恢复
			long times = System.currentTimeMillis() - 360000;
	        CharSequence dateText =
	                DateUtils.getRelativeTimeSpanString(times,
	                		System.currentTimeMillis(),
	                        DateUtils.MINUTE_IN_MILLIS,
	                        DateUtils.FORMAT_ABBREV_RELATIVE);
	        
	        NoteUtil.showToast(mContext, dateText + "");
			break;
		case NoteUtil.MENU_SETTING:
			intent = new Intent();
			intent.setClass(NoteLoader.this, NoteSettingActivity.class);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void getData() {
		if (null != mList && mList.size() > 0) {
			mList.clear();
		}
		
		if (mGetNoteTask != null && mGetNoteTask.getStatus() == AsyncTask.Status.RUNNING) {
			return;
		} else {
			mGetNoteTask = new GetNoteTask();
			mGetNoteTask.execute();
		}
	}

	public class GetNoteTask extends AsyncTask<Void, List<Notes>, Void> {
		@SuppressWarnings("unchecked")
		@Override
		protected Void doInBackground(Void... params) {
			if(NoteUtil.DEBUG) Log.d(TAG, "doInBackground");
			List<Notes> list = queryAllRecord();
			publishProgress(list);
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(List<Notes>... values) {
			if(NoteUtil.DEBUG) Log.d(TAG, "onProgressUpdate");
			mList = values[0];
			if (mList.size() <= 0) {
				mTipsView.setVisibility(View.VISIBLE);
				mAddTipsBtn.setVisibility(View.VISIBLE);
			} else {
				mTipsView.setVisibility(View.GONE);
				mAddTipsBtn.setVisibility(View.GONE);
			}

			if (NoteManager.isFirst) {
				NoteManager.isFirst = false;

				mAdapter = new NoteAdapter(NoteLoader.this, mList);
				mListView.setAdapter(mAdapter);
			} else {
				mAdapter.notifyDataSetChanged();
			}
			super.onProgressUpdate(values);
		}

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		mCurrentPos = position;
		mCurrentNotes = mList.get(position);
		if(NoteUtil.DEBUG) Log.d(TAG, "onItemLong.posititon=" + mCurrentPos);
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mCurrentPos = position;
		mCurrentNotes = mList.get(position);
		if(NoteUtil.DEBUG) Log.d(TAG, "onItem.posititon=" + mCurrentPos);
		openNote();
	}

	class ListOnCreate implements OnCreateContextMenuListener {// ListView上下文菜单
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			menu.setHeaderTitle(R.string.menu_title);
			menu.add(0, NoteUtil.MENU_OPEN, 0, R.string.menu_open);
			menu.add(0, NoteUtil.MENU_DELETE, 1, R.string.menu_delete);
			menu.add(0, NoteUtil.MENU_EDIT, 2, R.string.menu_edit);
			menu.add(0, NoteUtil.MENU_EDIT_TITLE, 3, R.string.menu_edit_title);
		}
	}

	// 长按弹出菜单监听事件
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// System.out.println("postion= " + mCurrentPos);
		switch (item.getItemId()) {
		case NoteUtil.MENU_OPEN:
			openNote();
			break;
		case NoteUtil.MENU_DELETE://
			new AlertDialog.Builder(mContext).setTitle(R.string.menu_delete)
					.setMessage(getResources().getString(R.string.delete_msg, mCurrentNotes.getTitle()))
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Uri uri = Uri.parse(NoteBookMetaData.NoteBook.CONTENT_URI + "/" + mCurrentNotes.getId());
							getContentResolver().delete(uri, null, null);

							mList.remove(mCurrentPos);
							if (mAdapter == null) {
							}
							mAdapter.notifyDataSetChanged();
						}
					}).setNegativeButton(android.R.string.cancel, null).create().show();
			break;
		case NoteUtil.MENU_EDIT://
			Intent intent = new Intent(NoteLoader.this, EditNoteActivity.class);
			intent.putExtra(NoteUtil.ITEM_ID_INDEX, mCurrentNotes.getId());
			startActivity(intent);
			break;
		case NoteUtil.MENU_EDIT_TITLE:
			final EditText editText = new EditText(mContext);
			editText.setSingleLine();
			editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
			editText.setText(mCurrentNotes.getTitle());
			new AlertDialog.Builder(mContext).setTitle("编辑标题").setIcon(R.drawable.ic_menu_edit_current).setView(editText)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String title = editText.getText().toString().trim();
							ContentValues values = new ContentValues();
							values.put(NoteBookMetaData.NoteBook.TITLE, title);
							Uri uri = Uri.parse(NoteBookMetaData.NoteBook.CONTENT_URI + "/" + mCurrentNotes.getId());
							getContentResolver().update(uri, values, null, null);

							mList.get(mCurrentPos).setTitle(title);
							mAdapter.notifyDataSetChanged();// 重绘当前可见区域
						}
					}).setNegativeButton(android.R.string.cancel, null).create().show();
			break;
		}
		return super.onContextItemSelected(item);
	}

	public void openNote() {
		Intent intent = new Intent(this, CheckNoteActivity.class);
		intent.putExtra(NoteUtil.ITEM_ID_INDEX, mCurrentNotes.getId());
		startActivityForResult(intent, NoteUtil.REQUEST_CODE_EDIT);
	}

	private List<Notes> queryAllRecord() {
		List<Notes> mNotes = new ArrayList<Notes>();

		Notes noteBook = null;
		Cursor cursor = null;
		try {
			// 查询所有的字段
			cursor = getContentResolver().query(NoteBookMetaData.NoteBook.CONTENT_URI, NoteUtil.COLUMNS, null, null, null);

			int id;
			String title;
			String content;
			long time;

			if (cursor.moveToFirst()) {
				do {
					id = cursor.getInt(cursor.getColumnIndex(NoteBookMetaData.NoteBook._ID));
					title = cursor.getString(cursor.getColumnIndex(NoteBookMetaData.NoteBook.TITLE));
					content = cursor.getString(cursor.getColumnIndex(NoteBookMetaData.NoteBook.CONTENT));
//					time = cursor.getString(cursor.getColumnIndex(NoteBookMetaData.NoteBook.TIME));
					time = cursor.getLong(cursor.getColumnIndex(NoteBookMetaData.NoteBook.TIME));

					noteBook = new Notes(id);
					noteBook.setTitle(title);
					noteBook.setContent(content);
					noteBook.setTime(time);

					mNotes.add(noteBook);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return mNotes;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//再按一次back退出
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if ((System.currentTimeMillis() - mExitTime > 2000)) {
				NoteUtil.showLongToast(mContext, "再按一次BACK退出");
				mExitTime = System.currentTimeMillis();
			}else {
				mExitTime = 0;
				this.finish();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.add_tips_btn) {
			Intent intent = new Intent();
			intent = new Intent(NoteLoader.this, AppendNoteActivity.class);
			startActivity(intent);
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (NoteUtil.REQUEST_CODE_EDIT == requestCode) {
				if (data != null) {
					long time = data.getLongExtra("time", -1);
					mList.get(mCurrentPos).setTime(time);
					mAdapter.notifyDataSetChanged();
				}
			}
		}
	};
}
