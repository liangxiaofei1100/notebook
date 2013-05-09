package com.yuri.notebook;

import java.util.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

import com.yuri.notebook.db.NoteBookMetaData;
import com.yuri.notebook.utils.NoteManager;
import com.yuri.notebook.utils.NoteUtil;

public class AppendNoteActivity extends Activity implements OnFocusChangeListener {
	private static final String TAG = "AppendNoteActivity";
	private EditText titleEdit,contentEdit;
	Calendar calendar;
//	String time;
	long time;
	Intent intent;
	
	private SharedPreferences sp = null;
	// 设置字体，0：较大字体；1：中等字体；2：较小字体
	private int mFontSize;
	// 设置背景颜色：0：灰色；1：浅黄色；2：粉红色；3：蓝色
	private int mColorType;
	
	private boolean hasTitleEditFoucsed = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_note);
		
		// 标题栏返回
		NoteUtil.setShowTitleBackButton(AppendNoteActivity.this);
		        
		setTitle(R.string.add_note);
		
		sp = getSharedPreferences(NoteUtil.SHARED_NAME, MODE_PRIVATE);
		
		titleEdit = (EditText)findViewById(R.id.editbiaoti);
		titleEdit.setOnFocusChangeListener(this);
		
		contentEdit = (EditText) findViewById(R.id.content_edit);
		contentEdit.setFocusable(true);
		contentEdit.requestFocus();
		contentEdit.addTextChangedListener(watcher);
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
			
//			time = NoteUtil.getTime();
			//use new way
			time = System.currentTimeMillis();
			String titleStr = titleEdit.getText().toString().trim();
			String contentStr = contentEdit.getText().toString().trim();
			
			if (titleStr == null || titleStr.equals("")) {
				NoteUtil.showToast(AppendNoteActivity.this, R.string.title_cannot_null);
			}else {
				ContentValues values = new ContentValues();
				values.put(NoteBookMetaData.NoteBook.TITLE,titleStr);
				values.put(NoteBookMetaData.NoteBook.CONTENT,contentStr);
				values.put(NoteBookMetaData.NoteBook.TIME, time);
				getContentResolver().insert(NoteBookMetaData.NoteBook.CONTENT_URI, values);
				
				AppendNoteActivity.this.finish();
			}
			break;
		case android.R.id.home://
			//点击左上角应用图标，返回，默认情况下，图标的ID是android.R.id.home
			NoteManager.backAlert(AppendNoteActivity.this);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			NoteManager.backAlert(AppendNoteActivity.this);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private TextWatcher watcher = new TextWatcher(){
		 
	    @Override
	    public void afterTextChanged(Editable s) {
	    	if (!hasTitleEditFoucsed) {
	    		String title = contentEdit.getText().toString();
		    	if (title.indexOf("\n") == -1) {
				}else {
					title = title.substring(0, title.indexOf("\n"));
				}
		    	titleEdit.setText(title);
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
		if (titleEdit == v) {
			if (titleEdit.getText().toString().equals("")) {
				hasTitleEditFoucsed = false;
			}else {
				if (hasFocus) {
					hasTitleEditFoucsed = true;
				}
			}
			
		}
	}
	
}
