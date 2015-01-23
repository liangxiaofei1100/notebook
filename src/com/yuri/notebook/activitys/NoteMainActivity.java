package com.yuri.notebook.activitys;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.frontia.Frontia;
import com.baidu.frontia.FrontiaData;
import com.baidu.frontia.FrontiaQuery;
import com.yuri.notebook.R;
import com.yuri.notebook.adapter.DrawerAdapter;
import com.yuri.notebook.adapter.NoteAdapter;
import com.yuri.notebook.bean.Note;
import com.yuri.notebook.db.MetaData;
import com.yuri.notebook.db.MetaData.NoteColumns;
import com.yuri.notebook.net.FrontiaManager;
import com.yuri.notebook.utils.Constants;
import com.yuri.notebook.utils.LogUtils;
import com.yuri.notebook.utils.NoteUtil;

public class NoteMainActivity extends ListActivity implements
		OnItemClickListener, OnItemLongClickListener, OnClickListener,
		OnQueryTextListener, LoaderCallbacks<Cursor>,
		OnAttachStateChangeListener {
	private static final String TAG = "NoteLoader";
	// 用户显示数据
	private ListView mListView;
	private TextView mTipsView;
	private ImageView mAddTipsBtn;
	private TextView mSearchResultEmptyTextView;

	private NoteAdapter mAdapter;

	// 临时性保存数据
	public static List<Note> mList = new ArrayList<Note>();

	private Context mContext;

	// 记录退出的时间点
	private long mExitTime = 0;

	private SearchView mSearchView;
	private String mSearchString;
	private boolean mIsSearchMode = false;

	private TextView mCountView;

	private String mSort = NoteColumns.SORT_ORDER_DEFAULT;

	private SharedPreferences sp = null;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private DrawerAdapter mDrawerAdapter;

	private ActionBarDrawerToggle mDrawerToggle;
	private List<String> mGroupList = new ArrayList<String>();
	private String GROUP_ALL;
	private String mGroup;

	private ProgressBar mLoadingBar;

	private ModeCallBack mCallBack;

	private static final int DRAWER_OPEN = 0;
	private static final int DRAWER_CLOSE = 2;
	private int mDrawerPreStatus = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.homepage);

		Frontia.init(getApplicationContext(), Constants.BAIDU_API_KEY);

		GROUP_ALL = getString(R.string.type_all);
		mGroup = GROUP_ALL;
		getActionBar().setTitle(mGroup);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		// 侧滑菜单主要布局
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setOnItemClickListener(this);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.about, R.string.about_author) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				mDrawerPreStatus = DRAWER_OPEN;
				invalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				mDrawerPreStatus = DRAWER_CLOSE;
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		initGroup();
		mDrawerAdapter.setSelectPosition(0);

		ActionBar actionBar = getActionBar();
		ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(this).inflate(
				R.layout.homepage_actionbar, null);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(viewGroup, new ActionBar.LayoutParams(
				ActionBar.LayoutParams.WRAP_CONTENT,
				ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL
						| Gravity.RIGHT));

		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		mCountView = (TextView) viewGroup.findViewById(R.id.note_count);

		mContext = this;
		mListView = getListView();
		mListView.setOnItemClickListener(this);
		// long click show edit popmenu
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		mCallBack = new ModeCallBack();
		mListView.setMultiChoiceModeListener(mCallBack);

		mAdapter = new NoteAdapter(this, null,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		mListView.setAdapter(mAdapter);

		mTipsView = (TextView) findViewById(R.id.tips);
		mAddTipsBtn = (ImageView) findViewById(R.id.add_tips_btn);
		mAddTipsBtn.setOnClickListener(this);

		mLoadingBar = (ProgressBar) findViewById(R.id.bar_loading);

		mSearchResultEmptyTextView = (TextView) findViewById(R.id.tv_homepage_search_result_empty);

		sp = getSharedPreferences(NoteUtil.SHARED_NAME, MODE_PRIVATE);
		mSort = sp
				.getString(NoteUtil.LIST_SORT, NoteColumns.SORT_ORDER_DEFAULT);

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.note_loader, menu);

		mSearchView = (SearchView) menu.findItem(R.id.menu_note_loader_search)
				.getActionView();
		mSearchView.setOnQueryTextListener(this);
		mSearchView.addOnAttachStateChangeListener(this);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.menu_note_loader_add).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_note_loader_search).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_note_sort_by_default).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_note_sort_by_date).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_note_sort_by_type).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.menu_note_loader_add:
			addNewNote();
			break;
		case R.id.menu_note_loader_setting:
			intent = new Intent();
			intent.setClass(NoteMainActivity.this, NoteSettingActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_note_sort_by_default:
			mSort = NoteColumns.SORT_ORDER_DEFAULT;
			restartLoader("menu_note_sort_by_default");

			Editor editor = sp.edit();
			editor.putString(NoteUtil.LIST_SORT, mSort);
			editor.commit();
			break;
		case R.id.menu_note_sort_by_date:
			mSort = NoteColumns.SORT_ORDER_TIME;
			restartLoader("menu_note_sort_by_date");

			Editor editor2 = sp.edit();
			editor2.putString(NoteUtil.LIST_SORT, mSort);
			editor2.commit();
			break;
		case R.id.menu_note_sort_by_type:
			mSort = NoteColumns.SORT_ORDER_GROUP;
			restartLoader("menu_note_sort_by_type");

			Editor editor3 = sp.edit();
			editor3.putString(NoteUtil.LIST_SORT, mSort);
			editor3.commit();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private void addNewNote() {
		Intent intent = new Intent(NoteMainActivity.this,
				EditNoteActivity.class);
		startActivity(intent);
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
			LogUtils.d(TAG, "onItem.posititon=" + position);
		switch (parent.getId()) {
		case R.id.left_drawer:
			if (mAdapter.isMode(NoteUtil.MODE_MENU)) {
				mCallBack.actionMode.finish();
			}
			mDrawerAdapter.setSelectPosition(position);
			mDrawerAdapter.notifyDataSetChanged();

			mGroup = mGroupList.get(position);
			restartLoader("left_drawer");
			getActionBar().setTitle(mGroup);

			mDrawerLayout.closeDrawer(mDrawerList);
			break;
		default:
			openNote(id);
			break;
		}
	}

	// use action mode instead of context menu

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

	private void initGroup() {
		mGroupList.clear();
		mGroupList.add(GROUP_ALL);

		String[] groups = getResources().getStringArray(R.array.group_list);
		for (String group : groups) {
			mGroupList.add(group);
		}

		mDrawerAdapter = new DrawerAdapter(getApplicationContext(), mGroupList);
		mDrawerList.setAdapter(mDrawerAdapter);
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
			addNewNote();
		}
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		LogUtils.d(TAG, "onQueryTextChange:" + newText);
		if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
			return false;
		}
		mSearchString = !TextUtils.isEmpty(newText) ? newText : null;
		mAdapter.setSearchString(newText);
		restartLoader("onQueryTextChange");
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// Don't care about this.
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		LogUtils.d(TAG, "onCreateLoader");
		mLoadingBar.setVisibility(View.VISIBLE);

		Uri uri = null;
		String selection = null;
		String[] selectionArgs = null;
		if (mSearchString == null) {
			uri = MetaData.NoteColumns.CONTENT_URI;
			if (!GROUP_ALL.equals(mGroup)) {
				selection = NoteColumns.GROUP + "=?";
				selectionArgs = new String[] { mGroup };
			}
		} else {
			uri = Uri.withAppendedPath(MetaData.NoteColumns.CONTENT_FILTER_URI,
					Uri.encode(mSearchString));
		}
		return new CursorLoader(this, uri, NoteUtil.COLUMNS, selection,
				selectionArgs, mSort);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		LogUtils.d(TAG, "onLoadFinished. count = " + cursor.getCount());
		// TODO for compatibility, init mList.
		mAdapter.swapCursor(cursor);
		updateTipsView(mAdapter.getCount());
		mCountView.setText(cursor.getCount() + "");
		mLoadingBar.setVisibility(View.GONE);
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
		mAdapter.swapCursor(null);

	}

	@Override
	public void onViewAttachedToWindow(View v) {
		mIsSearchMode = true;
		updateTipsView(mAdapter.getCount());
	}

	@Override
	public void onViewDetachedFromWindow(View v) {
		mIsSearchMode = false;
		if (!TextUtils.isEmpty(mSearchString)) {
			mSearchString = null;
			mAdapter.setSearchString(null);
			restartLoader("onViewDetachedFromWindow");
		}

		updateTipsView(mAdapter.getCount());
	}

	private void restartLoader(String test) {
		LogUtils.d(TAG, "restartLoader:" + test);
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			restartLoader("onActivityResult");
		}
	}

	@Override
	protected void onDestroy() {
		// Cursor cursor = mAdapter2.getCursor();
		// if (cursor != null) {
		// cursor.close();
		// mAdapter2.swapCursor(null);
		// }
		super.onDestroy();
	}

	/**
	 * Listview Selection Action Mode
	 */
	private class ModeCallBack implements MultiChoiceModeListener,
			OnMenuItemClickListener {
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
		public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
			// TODO Auto-generated method stub
			final Cursor cursor = mAdapter.getCursor();
			switch (item.getItemId()) {
			case R.id.actionbar_edit:
				cursor.moveToPosition(currentPosition);
				long index = cursor.getLong(cursor
						.getColumnIndex(MetaData.NoteColumns._ID));
				Intent intent = new Intent(NoteMainActivity.this,
						EditNoteActivity.class);
				intent.putExtra(NoteUtil.ITEM_ID_INDEX, index);
				startActivity(intent);
				mode.finish();
				break;
			case R.id.actionbar_delete:
				final ArrayList<Long> selectedList = new ArrayList<Long>();
				final List<String> objectIdStrings = new ArrayList<String>();
				for (int pos = getListView().getCount() - 1; pos >= 0; pos--) {
					if (mAdapter.isSelected(pos)) {
						cursor.moveToPosition(pos);
						long id = cursor.getLong(cursor
								.getColumnIndex(MetaData.NoteColumns._ID));
						String objectId = cursor.getString(cursor.getColumnIndex(NoteColumns.OBJECT_ID));
						selectedList.add(id);
						objectIdStrings.add(objectId);
					}
				}

				new AlertDialog.Builder(mContext)
						.setTitle(R.string.menu_delete)
						.setMessage(
								getString(R.string.delete_msg_multi,
										selectedList.size()))
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// add this code,fix a bug when delete
										// the last item
										mode.finish();
										for (int i = 0; i < selectedList.size(); i++) {
											Uri uri = Uri
													.parse(MetaData.NoteColumns.CONTENT_URI
															+ "/"
															+ selectedList
																	.get(i));
											getContentResolver().delete(uri,
													null, null);
										}
										// // bug: When search string is not
										// empty, the list can not update. Why?
										if (mIsSearchMode
												&& !TextUtils
														.isEmpty(mSearchString)) {
											restartLoader("menu_delete");
										}
										
										//delete cloud data
										new FrontiaManager().delete(null, objectIdStrings);
									}
								})
						.setNegativeButton(android.R.string.cancel, null)
						.create().show();
				break;
			case R.id.actionbar_backup:
				FrontiaManager frontiaManager = new FrontiaManager();
				
				cursor.moveToFirst();
				Note note = null;
				do {
					note = Note.getNoteFromCursor(cursor);
					frontiaManager.insertData(note, null);
				} while (cursor.moveToNext());
				break;

			default:
				Toast.makeText(NoteMainActivity.this,
						"Clicked " + item.getTitle(), Toast.LENGTH_SHORT)
						.show();
				break;
			}
			return true;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			LogUtils.d(TAG, "onCreateActionMode");
			// 最先调用的
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// 自定义ActionBar菜单
			View customView = inflater.inflate(
					R.layout.listview_actionbar_edit, null);
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
			LogUtils.i(TAG, "onDestroyActionMode");
			mAdapter.unSelectedAll();
			mAdapter.setMode(NoteUtil.MODE_NORMAL);

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
			mAdapter.setMode(NoteUtil.MODE_MENU);
			actionMode = mode;
			int selectedCount = mAdapter.getCheckedItemCount();
			switch (selectedCount) {
			case 0:
				menu.findItem(R.id.actionbar_edit).setEnabled(false);
				menu.findItem(R.id.actionbar_delete).setEnabled(false);
				menu.findItem(R.id.actionbar_backup).setEnabled(true);
				break;
			case 1:
				menu.findItem(R.id.actionbar_edit).setEnabled(true);
				menu.findItem(R.id.actionbar_delete).setEnabled(true);
				menu.findItem(R.id.actionbar_backup).setEnabled(true);
				break;

			default:

				break;
			}
			return true;
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				return;
			}
			currentPosition = position;
			LogUtils.d(TAG, "position=" + position);
			mAdapter.setChecked(position);
			setSubtitle(mode);
		}

		private void setSubtitle(ActionMode mode) {
			final int checkedCount = mAdapter.getCheckedItemCount();
			mSelectBtn.setText(getString(R.string.selected_msg, checkedCount));
			Menu menu = mode.getMenu();
			if (checkedCount == 0) {
				menu.findItem(R.id.actionbar_edit).setEnabled(false);
				menu.findItem(R.id.actionbar_delete).setEnabled(false);
				menu.findItem(R.id.actionbar_backup).setEnabled(false);
			} else if (checkedCount == 1) {
				menu.findItem(R.id.actionbar_edit).setEnabled(true);
				menu.findItem(R.id.actionbar_delete).setEnabled(true);
				menu.findItem(R.id.actionbar_backup).setEnabled(true);
			} else {
				menu.findItem(R.id.actionbar_edit).setEnabled(false);
				menu.findItem(R.id.actionbar_delete).setEnabled(true);
				menu.findItem(R.id.actionbar_backup).setEnabled(true);
			}
		}

		private PopupMenu createSelectPopupMenu(View anchorView) {
			final PopupMenu popupMenu = new PopupMenu(mContext, anchorView);
			popupMenu.inflate(R.menu.select_popup_menu);
			popupMenu.setOnMenuItemClickListener(this);
			return popupMenu;
		}

		private void updateSelectPopupMenu() {
			if (mSelectPopupMenu == null) {
				mSelectPopupMenu = createSelectPopupMenu(mSelectBtn);
				return;
			}
			final Menu menu = mSelectPopupMenu.getMenu();
			int selectedCount = mAdapter.getCheckedItemCount();
			if (getListView().getCount() == 0) {
				menu.findItem(R.id.select).setEnabled(false);
			} else {
				if (getListView().getCount() != selectedCount) {
					menu.findItem(R.id.select).setTitle(R.string.seleteall);
					mSelectedAll = true;
				} else {
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
					mAdapter.selectAll();
				} else {
					mAdapter.unSelectedAll();
				}

				setSubtitle(actionMode);

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
