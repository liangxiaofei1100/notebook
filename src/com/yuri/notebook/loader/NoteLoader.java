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

import android.app.AlertDialog;
import android.app.ListActivity;
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
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

public class NoteLoader extends ListActivity implements OnItemClickListener,
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
//		mListView = (ListView) findViewById(R.id.ListViewAppend);
		mListView = getListView();
		mListView.setOnItemClickListener(this);
//		mListView.setOnCreateContextMenuListener(this);
		//long click show edit popmenu
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		mListView.setMultiChoiceModeListener(new ModeCallBack());
//		mListView.setOnLongClickListener(this);

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
									// bug: When search string is not empty, the list can not update. Why?
									if (mIsSearchMode && !TextUtils.isEmpty(mSearchString)) {
										getLoaderManager().restartLoader(0, null, NoteLoader.this);
									}
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
									
									// bug: When search string is not empty, the list can not update. Why?
									if (mIsSearchMode && !TextUtils.isEmpty(mSearchString)) {
										getLoaderManager().restartLoader(0, null, NoteLoader.this);
									}
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
				NoteBookMetaData.NoteBook.SORT_ORDER_DEFAULT);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		Log.d(TAG, "onLoadFinished. count = " + cursor.getCount());
		// TODO for compatibility, init mList.
		loadNotesToList(cursor, mList);

		mAdapter2.swapCursor(cursor);

		updateTipsView(mAdapter2.getCount());
	}

	private void loadNotesToList(Cursor cursor, List<Notes> list) {
		list.clear();
		while (cursor.moveToNext()) {
			list.add(Notes.createNotebookFromCursor(cursor));
		}
	}

	private void updateTipsView(int resultCount) {
		if (resultCount <= 0) {
			if (mIsSearchMode && mTipsView.getVisibility() != View.VISIBLE) {
				mSearchResultEmptyTextView.setVisibility(View.VISIBLE);
			} else {
				mTipsView.setVisibility(View.VISIBLE);
				mAddTipsBtn.setVisibility(View.VISIBLE);
				mSearchResultEmptyTextView.setVisibility(View.GONE);
			}
		} else {
			if (mIsSearchMode) {
				mSearchResultEmptyTextView.setVisibility(View.GONE);
			} else {
				mTipsView.setVisibility(View.GONE);
				mAddTipsBtn.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter2.swapCursor(null);

	}

	@Override
	public void onViewAttachedToWindow(View v) {
		mIsSearchMode = true;
		updateTipsView(mAdapter2.getCount());
	}

	@Override
	public void onViewDetachedFromWindow(View v) {
		mIsSearchMode = false;
		if (!TextUtils.isEmpty(mSearchString)) {
			mSearchString = null;
			mAdapter2.setSearchString(null);
			getLoaderManager().restartLoader(0, null, this);
		}

		updateTipsView(mAdapter2.getCount());
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

	/**
	 * Listview Selection Action Mode
	 */
	private class ModeCallBack implements MultiChoiceModeListener, OnMenuItemClickListener{
		/**
		 * 用户弹出全选和取消全选的菜单的
		 */
		private PopupMenu mSelectPopupMenu = null;
		private boolean mSelectedAll = true;
		/**
		 * 显示选中多少个，以及全选和取消全选
		 */
	    private Button mSelectBtn = null;
	    
	    private int currentPosition = -1;
	    
	    private ActionMode actionMode;
	    
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// TODO Auto-generated method stub
			Cursor cursor = mAdapter2.getCursor();
			switch (item.getItemId()) {
			case R.id.actionbar_edit:
				cursor.moveToPosition(currentPosition);
				long index = cursor.getLong(cursor.getColumnIndex(NoteBookMetaData.NoteBook._ID));
				Intent intent = new Intent(NoteLoader.this, EditNoteActivity.class);
				intent.putExtra(NoteUtil.ITEM_ID_INDEX, index);
				startActivity(intent);
				mode.finish();
				break;
			case R.id.actionbar_delete:
				final ArrayList<Long> selectedList = new ArrayList<Long>();
				for (int pos = getListView().getCount() - 1; pos >= 0; pos--) {
					if (mAdapter2.isSelected(pos)) {
						cursor.moveToPosition(pos);
						long id = cursor.getLong(cursor.getColumnIndex(NoteBookMetaData.NoteBook._ID));
						selectedList.add(id);
					}
				}
				
				String deleteMessage = "是否删除这" + selectedList.size() + "项";

				new AlertDialog.Builder(mContext)
						.setTitle(R.string.menu_delete)
						.setMessage(deleteMessage)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										for (int i = 0; i < selectedList.size(); i++) {
											Uri uri = Uri
													.parse(NoteBookMetaData.NoteBook.CONTENT_URI
															+ "/" + selectedList.get(i));
											getContentResolver()
											.delete(uri, null, null);
										}
//										// bug: When search string is not empty, the list can not update. Why?
										if (mIsSearchMode && !TextUtils.isEmpty(mSearchString)) {
											getLoaderManager().restartLoader(0, null, NoteLoader.this);
										}
									}
								}).setNegativeButton(android.R.string.cancel, null)
						.create().show();
				break;

			default:
				Toast.makeText(NoteLoader.this, "Clicked " + item.getTitle(),
                        Toast.LENGTH_SHORT).show();
				break;
			}
			return true;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// 最先调用的
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// 自定义ActionBar菜单
			View customView = inflater.inflate(R.layout.listview_actionbar_edit,
					null);
			mode.setCustomView(customView);
			mSelectBtn = (Button) customView.findViewById(R.id.select_button);
			mSelectBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (null == mSelectPopupMenu) {
						// 创建"全选/取消全选"的弹出菜单
						 mSelectPopupMenu = createSelectPopupMenu(mSelectBtn);
						 updateSelectPopupMenu();
						 mSelectPopupMenu.show();
					} else {
						// Update
						updateSelectPopupMenu();
						mSelectPopupMenu.show();
					}
				}
			});
			MenuInflater menuInflater = mode.getMenuInflater();
			menuInflater.inflate(R.menu.actionbar_menu, menu);
			setSubtitle(mode);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mAdapter2.unSelectedAll();
			mAdapter2.setMode(NoteUtil.MODE_NORMAL);
			
			if (actionMode != null) {
				actionMode = null;
			}
			
			if (mSelectPopupMenu != null) {
				mSelectPopupMenu.dismiss();
				mSelectPopupMenu = null;
			}
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			mAdapter2.setMode(NoteUtil.MODE_MENU);
			actionMode = mode;
			int selectedCount = mAdapter2.getCheckedItemCount();
			switch (selectedCount) {
			case 0:
				menu.findItem(R.id.actionbar_edit).setEnabled(false);
				menu.findItem(R.id.actionbar_delete).setEnabled(false);
				break;
			case 1:
				menu.findItem(R.id.actionbar_edit).setEnabled(true);
				menu.findItem(R.id.actionbar_delete).setEnabled(true);
				break;

			default:
				
				break;
			}
			return true;
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			currentPosition = position;
			mAdapter2.setChecked(position);
			setSubtitle(mode);
		}
		
		private void setSubtitle(ActionMode mode) {
			final int checkedCount = mAdapter2.getCheckedItemCount();
			mSelectBtn.setText("" + checkedCount + " items selected");
			Menu menu = mode.getMenu();
			if (checkedCount == 0) {
				menu.findItem(R.id.actionbar_edit).setEnabled(false);
				menu.findItem(R.id.actionbar_delete).setEnabled(false);
			}else if (checkedCount == 1) {
				menu.findItem(R.id.actionbar_edit).setEnabled(true);
				menu.findItem(R.id.actionbar_delete).setEnabled(true);
			}else {
				menu.findItem(R.id.actionbar_edit).setEnabled(false);
				menu.findItem(R.id.actionbar_delete).setEnabled(true);
			}
		}
		
		private PopupMenu createSelectPopupMenu(View anchorView) {
	        final PopupMenu popupMenu = new PopupMenu(mContext, anchorView);
	        popupMenu.inflate(R.menu.select_popup_menu);
	        popupMenu.setOnMenuItemClickListener(this);
	        return popupMenu;
	    }
		
		private void updateSelectPopupMenu(){
			if (mSelectPopupMenu == null) {
	            mSelectPopupMenu = createSelectPopupMenu(mSelectBtn);
	            return;
	        }
			final Menu menu = mSelectPopupMenu.getMenu();
			int selectedCount = mAdapter2.getCheckedItemCount();
			if (getListView().getCount() == 0) {
				menu.findItem(R.id.select).setEnabled(false);
			}else {
				if (getListView().getCount() != selectedCount) {
					menu.findItem(R.id.select).setTitle(R.string.seleteall);
					mSelectedAll = true;
				}else {
					menu.findItem(R.id.select).setTitle(R.string.cancelall);
					mSelectedAll = false;
				}
			}
		}

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			switch (item.getItemId()) {
			case R.id.select:
				if (mSelectedAll) {
					mAdapter2.selectAll();
				}else {
					mAdapter2.unSelectedAll();
				}
				
				int selectedCount = mAdapter2.getCheckedItemCount();
	            mSelectBtn.setText("" + selectedCount + " items selected");
	           
				updateSelectPopupMenu();
				
				if (actionMode != null) {
					actionMode.invalidate();
				}
				
				break;

			default:
				break;
			}
			return true;
		}
		
	}
}
