package com.yuri.notebook;

import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;

import com.yuri.notebook.db.NoteMetaData;
import com.yuri.notebook.utils.NoteManager;
import com.yuri.notebook.utils.NoteUtil;

public class NewNoteActivity extends Activity implements OnFocusChangeListener {
	private static final String TAG = "AppendNoteActivity";
	private EditText mTitleEdit,mContentEdit;
	Calendar calendar;
//	String time;
	Intent intent;
	
	private SharedPreferences sp = null;
	// 设置字体，0：较大字体；1：中等字体；2：较小字体
	private int mFontSize;
	// 设置背景颜色：0：灰色；1：浅黄色；2：粉红色；3：蓝色
	private int mColorType;
	
	private boolean hasTitleEditFoucsed = false;
	
	private long mTime = 0;
	private String mTitle = "";
	private String mContent = "";
	
	private View mCustomView ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.add_note);
		
		sp = getSharedPreferences(NoteUtil.SHARED_NAME, MODE_PRIVATE);
		
		mCustomView = getLayoutInflater().inflate(R.layout.editnote_custom_title, null);
		final ActionBar bar = getActionBar();
		 bar.setCustomView(mCustomView,
	                new ActionBar.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		 
		 //设置标题栏返回
		int flags = ActionBar.DISPLAY_HOME_AS_UP;
		int change = bar.getDisplayOptions() ^ flags;
		bar.setDisplayOptions(change, flags);
		
		//设置自定义标题栏
		bar.setDisplayOptions(bar.getDisplayOptions() ^ ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
		
//		titleEdit = (EditText)findViewById(R.id.editbiaoti);
		
		mTitleEdit = (EditText) mCustomView.findViewById(R.id.title_edit);
		mTitleEdit.setBackgroundColor(Color.TRANSPARENT);
		mContentEdit = (EditText) findViewById(R.id.content_edit);
		mContentEdit.addTextChangedListener(watcher);
		mContentEdit.setBackgroundColor(Color.TRANSPARENT);
		mContentEdit.requestFocus();
//		NoteUtil.emulateShiftHeld(contentEdit);
//		readFontSize();
//		readBackgroundColor();
		
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
			NoteManager.isNeedRefresh = true;
			NoteManager.isFirst = true;
			
			//use new way
			mTime = System.currentTimeMillis();
			mTitle = mTitleEdit.getText().toString().trim();
			mContent = mContentEdit.getText().toString().trim();
			
			if (mTitle == null || mTitle.equals("")) {
				NoteUtil.showToast(NewNoteActivity.this, R.string.title_cannot_null);
			}else {
				ContentValues values = new ContentValues();
				values.put(NoteMetaData.Note.TITLE,mTitle);
				values.put(NoteMetaData.Note.CONTENT,mContent);
				values.put(NoteMetaData.Note.TIME, mTime);
				getContentResolver().insert(NoteMetaData.Note.CONTENT_URI, values);
				
				NewNoteActivity.this.finish();
			}
			break;
		case android.R.id.home://
			//点击左上角应用图标，返回，默认情况下，图标的ID是android.R.id.home
			noteFinish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void noteFinish(){
		mTitle = mTitleEdit.getText().toString().trim();
		mContent = mContentEdit.getText().toString().trim();
		if (mTitle.equals("") && mContent.equals("")) {
			this.finish();
		}else {
			NoteManager.backAlert(NewNoteActivity.this);
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			noteFinish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private TextWatcher watcher = new TextWatcher(){
		 
	    @Override
	    public void afterTextChanged(Editable s) {
	    	if (!hasTitleEditFoucsed) {
	    		String title = mContentEdit.getText().toString();
		    	if (title.indexOf("\n") == -1) {
				}else {
					title = title.substring(0, title.indexOf("\n"));
				}
		    	mTitleEdit.setText(title);
			}
	    }
	 
	    @Override
	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	    }
	 
	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count) {
	        Log.d("TAG","[TextWatcher][onTextChanged]"+s);
	    }
	     
	};
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (mTitleEdit == v) {
			if (mTitleEdit.getText().toString().equals("")) {
				hasTitleEditFoucsed = false;
			} else {
				hasTitleEditFoucsed = true;
			}
			
		}
	}
	
}
