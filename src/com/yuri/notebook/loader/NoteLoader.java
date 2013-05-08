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
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

public class NoteLoader extends Activity implements OnItemClickListener,
		OnItemLongClickListener, OnClickListener, OnQueryTextListener,
		LoaderCallbacks<Cursor>, OnAttachStateChangeListener {
	private static final String TAG = "NoteLoader";
	// 用户显示数据
	private ListView mListView;
	private TextView mTipsView;
	private ImageView mAddTipsBtn;
	private TextView mSearchResultEmptyTextView;

	private NoteAdapter2 mAdapter2;

	// 临时性保存数据
	public static List<Notes> mList = new ArrayList<Notes>();

	private Context mContext;

	// 记录退出的时间点
	private long mExitTime = 0;

	private SearchView mSearchView;
	private String mSearchString;
	private boolean mIsSearchMode = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.homepage);

		mContext = this;
		mListView = (ListView) findViewById(R.id.ListViewAppend);
		mListView.setOnItemClickListener(this);
		mListView.setOnCreateContextMenuListener(this);

		mAdapter2 = new NoteAdapter2(this, null,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		mListView.setAdapter(mAdapter2);

		mTipsView = (TextView) findViewById(R.id.tips);
		mAddTipsBtn = (ImageView) findViewById(R.id.add_tips_btn);
		mAddTipsBtn.setOnClickListener(this);
		mSearchResultEmptyTextView = (TextView) findViewById(R.id.tv_homepage_search_result_empty);

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onResume() {
		if (NoteManager.isNeedRefresh) {
			NoteManager.isNeedRefresh = false;
			getLoaderManager().restartLoader(0, null, this);
		}
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.note_loader, menu);

		mSearchView = (SearchView) menu.findItem(R.id.menu_note_loader_search)
				.getActionView();
		mSearchView.setOnQueryTextListener(this);
		mSearchView.addOnAttachStateChangeListener(this);

		if (NoteUtil.DEBUG) {
			menu.add(0, NoteUtil.MENU_RECOVER, 4, "Recover");
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.menu_note_loader_add:
			intent = new Intent(NoteLoader.this, AppendNoteActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_note_loader_delete_multi:
			intent = new Intent(NoteLoader.this, BackupDeleteActivity.class);
			intent.putExtra(NoteUtil.MENU_MODE, NoteUtil.MENU_DELETE_MULTI);
			startActivity(intent);
			break;
		case NoteUtil.MENU_BACKUP:
			intent = new Intent(NoteLoader.this, BackupDeleteActivity.class);
			intent.putExtra(NoteUtil.MENU_MODE, NoteUtil.MENU_BACKUP);
			startActivity(intent);

			break;
		case NoteUtil.MENU_RECOVER:// 恢复
			long times = System.currentTimeMillis() - 360000;
			CharSequence dateText = DateUtils.getRelativeTimeSpanString(times,
					System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
					DateUtils.FORMAT_ABBREV_RELATIVE);

			NoteUtil.showToast(mContext, dateText + "");
			break;
		case R.id.menu_note_loader_setting:
			intent = new Intent();
			intent.setClass(NoteLoader.this, NoteSettingActivity.class);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (NoteUtil.DEBUG)
			Log.d(TAG, "onItem.posititon=" + position);
		openNote(id);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.note_loader_context_menu, menu);
		menu.setHeaderTitle(R.string.menu_title);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Cursor cursor = mAdapter2.getCursor();
		switch (item.getItemId()) {
		case R.id.menu_note_loader_open:
			openNote(menuInfo.id);
			break;
		case R.id.menu_note_loader_delete://
			cursor.moveToPosition(menuInfo.position);
			String deleteMessage = getResources().getString(
					R.string.delete_msg,
					cursor.getString(cursor
							.getColumnIndex(NoteBookMetaData.NoteBook.TITLE)));

			new AlertDialog.Builder(mContext)
					.setTitle(R.string.menu_delete)
					.setMessage(deleteMessage)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Uri uri = Uri
											.parse(NoteBookMetaData.NoteBook.CONTENT_URI
													+ "/" + menuInfo.id);
									getContentResolver()
											.delete(uri, null, null);
								}
							}).setNegativeButton(android.R.string.cancel, null)
					.create().show();
			break;
		case R.id.menu_note_loader_edit://
			Intent intent = new Intent(NoteLoader.this, EditNoteActivity.class);
			intent.putExtra(NoteUtil.ITEM_ID_INDEX, menuInfo.id);
			startActivity(intent);
			break;
		case R.id.menu_note_loader_edit_title:
			final EditText editText = new EditText(mContext);
			cursor.moveToPosition(menuInfo.position);
			editText.setSingleLine();
			editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
			editText.setText(cursor.getString(cursor
					.getColumnIndex(NoteBookMetaData.NoteBook.TITLE)));
			new AlertDialog.Builder(mContext)
					.setTitle("编辑标题")
					.setIcon(R.drawable.ic_menu_edit_current)
					.setView(editText)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									String title = editText.getText()
											.toString().trim();
									ContentValues values = new ContentValues();
									values.put(NoteBookMetaData.NoteBook.TITLE,
											title);
									Uri uri = Uri
											.parse(NoteBookMetaData.NoteBook.CONTENT_URI
													+ "/" + menuInfo.id);
									getContentResolver().update(uri, values,
											null, null);
								}
							}).setNegativeButton(android.R.string.cancel, null)
					.create().show();
			break;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * Open note with _id in database.
	 * 
	 * @param id
	 */
	public void openNote(long id) {
		Intent intent = new Intent(this, CheckNoteActivity.class);
		intent.putExtra(NoteUtil.ITEM_ID_INDEX, id);
		startActivity(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 再按一次back退出
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if ((System.currentTimeMillis() - mExitTime > 2000)) {
				NoteUtil.showLongToast(mContext, "再按一次BACK退出");
				mExitTime = System.currentTimeMillis();
			} else {
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

	@Override
	public boolean onQueryTextChange(String newText) {
		mSearchString = !TextUtils.isEmpty(newText) ? newText : null;
		mAdapter2.setSearchString(newText);
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// Don't care about this.
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = null;
		if (mSearchString == null) {
			uri = NoteBookMetaData.NoteBook.CONTENT_URI;
		} else {
			uri = Uri.withAppendedPath(
					NoteBookMetaData.NoteBook.CONTENT_FILTER_URI,
					Uri.encode(mSearchString));
		}
		return new CursorLoader(this, uri, NoteUtil.COLUMNS, null, null,
				NoteBookMetaData.NoteBook.SORT_ORDER_TIME);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		if (cursor.getCount() <= 0) {
			if (mIsSearchMode) {
				mSearchResultEmptyTextView.setVisibility(View.VISIBLE);
			} else {
				mTipsView.setVisibility(View.VISIBLE);
				mAddTipsBtn.setVisibility(View.VISIBLE);
			}
		} else {
			if (mIsSearchMode) {
				mSearchResultEmptyTextView.setVisibility(View.GONE);
			} else {
				mTipsView.setVisibility(View.GONE);
				mAddTipsBtn.setVisibility(View.GONE);
			}

		}

		// TODO for compatibility, init mList.
		loadNotesToList(cursor, mList);

		mAdapter2.swapCursor(cursor);
	}

	private void loadNotesToList(Cursor cursor, List<Notes> list) {
		while (cursor.moveToNext()) {
			list.add(Notes.createNotebookFromCursor(cursor));
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter2.swapCursor(null);

	}

	@Override
	public void onViewAttachedToWindow(View v) {
		mIsSearchMode = true;
	}

	@Override
	public void onViewDetachedFromWindow(View v) {
		mIsSearchMode = false;
		if (!TextUtils.isEmpty(mSearchString)) {
			mSearchString = null;
			mAdapter2.setSearchString(null);
			getLoaderManager().restartLoader(0, null, this);
		}
		
		if(mSearchResultEmptyTextView.getVisibility() == View.VISIBLE) {
			mSearchResultEmptyTextView.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		Cursor cursor = mAdapter2.getCursor();
		if (cursor != null) {
			cursor.close();
			mAdapter2.swapCursor(null);
		}
		super.onDestroy();
	}
}
