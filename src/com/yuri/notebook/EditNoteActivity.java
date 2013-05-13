package com.yuri.notebook;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.yuri.notebook.db.NoteBookMetaData;
import com.yuri.notebook.utils.NoteManager;
import com.yuri.notebook.utils.NoteUtil;
import com.yuri.notebook.utils.Notes;

public class EditNoteActivity extends Activity {
	private EditText titleEdit,contentEdit;
	private TextView titleText,contentText;
//	String time;
	long time;
	long itemId;
	
	private Notes mNoteBook;
	
	private SharedPreferences sp = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_note);
		
		// 标题栏返回
		NoteUtil.setShowTitleBackButton(EditNoteActivity.this);
				
		setTitle(R.string.edit_note);
//		titleText = (TextView)findViewById(R.id.textbiaoti);
//		titleText.setVisibility(View.GONE);
		titleEdit = (EditText)findViewById(R.id.editbiaoti);
//		contentText = (TextView)findViewById(R.id.content_text);
//		contentText.setVisibility(View.GONE);
		contentEdit = (EditText)findViewById(R.id.content_edit);
		contentEdit.setSelection(contentEdit.getText().length());
		
		Intent intent = this.getIntent();
		itemId = intent.getLongExtra(NoteUtil.ITEM_ID_INDEX, -1);
		mNoteBook = NoteManager.getNotesFromId(itemId,this);
		
		setTitle(R.string.edit);
		titleEdit.setText(mNoteBook.getTitle());
		
		contentEdit.setText(mNoteBook.getContent());
		contentEdit.setHint(R.string.add_note_tip);
		
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
		// TODO Auto-generated method stub
		MenuItem add = menu.add(0, NoteUtil.MENU_SAVE, 0, R.string.menu_save).setIcon(R.drawable.ic_select_all); 
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM); 

		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case NoteUtil.MENU_SAVE:
//			String time = NoteUtil.getTime();
			long time = System.currentTimeMillis();
			String contentStr = contentEdit.getText().toString().trim();//
			ContentValues values = new ContentValues();
			// values.put(NoteBookMetaData.NoteBook.TITLE, titleStr);
			values.put(NoteBookMetaData.NoteBook.CONTENT, contentStr);
			values.put(NoteBookMetaData.NoteBook.TIME, time);

			Uri uri = Uri.parse(NoteBookMetaData.NoteBook.CONTENT_URI + "/" + itemId);
			getContentResolver().update(uri, values, null, null);

			Intent intent = new Intent();
			intent.putExtra("time", time);
			setResult(RESULT_OK,intent);
			EditNoteActivity.this.finish();
			break;
		case android.R.id.home://
			//点击左上角应用图标，返回，默认情况下，图标的ID是android.R.id.home
			NoteManager.backAlert(EditNoteActivity.this);
			break;
		}
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			NoteManager.backAlert(EditNoteActivity.this);
		}
		return super.onKeyDown(keyCode, event);
	}
}
