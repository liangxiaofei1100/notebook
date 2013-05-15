package com.yuri.notebook;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.EditText;

import com.yuri.notebook.db.NoteMetaData;
import com.yuri.notebook.utils.NoteManager;
import com.yuri.notebook.utils.NoteUtil;
import com.yuri.notebook.utils.Notes;

public class EditNoteActivity extends Activity {
	private EditText mTitleEdit,mContentEdit;

	private long mTime;
	private String mTitle = "";
	private String mContent = "";
	
	long itemId;
	
	private Notes mNoteBook;
	
	private SharedPreferences sp = null;
	
	private View mCustomView ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.add_note);
		
		mCustomView = getLayoutInflater().inflate(R.layout.editnote_custom_title, null);
		final ActionBar bar = getActionBar();
		 bar.setCustomView(mCustomView,
	                new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		 
		 //设置标题栏返回
		int flags = ActionBar.DISPLAY_HOME_AS_UP;
		int change = bar.getDisplayOptions() ^ flags;
		bar.setDisplayOptions(change, flags);
		
		//设置自定义标题栏
		bar.setDisplayOptions(bar.getDisplayOptions() ^ ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
				
//		titleEdit = (EditText)findViewById(R.id.editbiaoti);
		mTitleEdit = (EditText) mCustomView.findViewById(R.id.title_edit);
		mTitleEdit.setBackgroundColor(Color.TRANSPARENT);
		mContentEdit = (EditText)findViewById(R.id.content_edit);
		mContentEdit.setBackgroundColor(Color.TRANSPARENT);
		mContentEdit.requestFocus();
		mContentEdit.setSelection(mContentEdit.getText().length());
		
		Intent intent = this.getIntent();
		itemId = intent.getLongExtra(NoteUtil.ITEM_ID_INDEX, -1);
		mNoteBook = NoteManager.getNotesFromId(itemId,this);
		
		mTitleEdit.setText(mNoteBook.getTitle());
		mContentEdit.setText(mNoteBook.getContent());
		
//		sp = getSharedPreferences(NoteUtil.SHARED_NAME, MODE_PRIVATE);
//		
//		int font_size = sp.getInt(NoteUtil.FONT_SET, 1);
//		NoteUtil.setFontSize(contentEdit, font_size);
//		
//		int color = sp.getInt(NoteUtil.COLOR_SET, 3);
//		NoteUtil.setBackgroundColor(contentEdit, color);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem add = menu.add(0, NoteUtil.MENU_SAVE, 0, R.string.menu_save).setIcon(R.drawable.ic_select_all); 
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM); 

		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case NoteUtil.MENU_SAVE:
			mTime = System.currentTimeMillis();
			mTitle = mTitleEdit.getText().toString().trim();
			if (mTitle.equals("")) {
				NoteUtil.showToast(EditNoteActivity.this, R.string.title_cannot_null);
			}else {
				mContent = mContentEdit.getText().toString().trim();
				ContentValues values = new ContentValues();
				values.put(NoteMetaData.Note.TITLE, mTitle);
				values.put(NoteMetaData.Note.CONTENT, mContent);
				values.put(NoteMetaData.Note.TIME, mTime);
				
				Uri uri = Uri.parse(NoteMetaData.Note.CONTENT_URI + "/" + itemId);
				getContentResolver().update(uri, values, null, null);
				
				Intent intent = new Intent();
				intent.putExtra("time", mTime);
				setResult(RESULT_OK,intent);
				EditNoteActivity.this.finish();
			}

			break;
		case android.R.id.home://
			//点击左上角应用图标，返回，默认情况下，图标的ID是android.R.id.home
			noteFinish();
			
			break;
		}
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			noteFinish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void noteFinish(){
		mTitle = mTitleEdit.getText().toString().trim();
		mContent = mContentEdit.getText().toString().trim();
		if (mNoteBook.getTitle().equals(mTitle) && mNoteBook.getContent().equals(mContent)) {
			this.finish();
		}else {
			NoteManager.backAlert(EditNoteActivity.this);
		}
	}
}
