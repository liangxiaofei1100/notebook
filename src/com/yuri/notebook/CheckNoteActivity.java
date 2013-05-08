package com.yuri.notebook;

import com.yuri.notebook.db.NoteBookMetaData;
import com.yuri.notebook.utils.NoteManager;
import com.yuri.notebook.utils.NoteUtil;
import com.yuri.notebook.utils.Notes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class CheckNoteActivity extends Activity {
	private static final String TAG = "CheckNote";
	private TextView contentText;

	private int itemId;

	private NoteManager mNoteManager;

	private Notes mNoteBook;
	
	private long mTime = 0;
	private Intent mData = null;
	
	private SharedPreferences sp = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.query);

		// 标题栏返回
		NoteUtil.setShowTitleBackButton(CheckNoteActivity.this);

		contentText = (TextView) findViewById(R.id.note_content);
		Intent intent = this.getIntent();
		itemId = intent.getIntExtra(NoteUtil.ITEM_ID_INDEX, -1);

		mNoteBook = NoteManager.getNotesFromId(itemId, this);
		
//		sp = getSharedPreferences(NoteUtil.SHARED_NAME, MODE_PRIVATE);
//		int font_size = sp.getInt(NoteUtil.FONT_SET, 1);
//		NoteUtil.setFontSize(contentText, font_size);
//		
//		int color = sp.getInt(NoteUtil.COLOR_SET, 3);
//		NoteUtil.setBackgroundColor(contentText, color);
	}

	@Override
	protected void onResume() {
		super.onResume();

		setTitle(mNoteBook.getTitle());
		contentText.setText(mNoteBook.getContent());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem edit_item = menu.add(0, NoteUtil.MENU_EDIT, 1, R.string.menu_edit).setIcon(R.drawable.ic_menu_edit_current);
		MenuItem delete_item = menu.add(0, NoteUtil.MENU_DELETE, 1, R.string.menu_delete).setIcon(R.drawable.ic_menu_delete_selected);
		MenuItem share_item = menu.add(0, NoteUtil.MENU_SHARE, 1, "分享");
		edit_item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		delete_item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case NoteUtil.MENU_EDIT:
			Intent intent = new Intent(CheckNoteActivity.this, EditNoteActivity.class);
			intent.putExtra(NoteUtil.ITEM_ID_INDEX, itemId);
			startActivityForResult(intent, NoteUtil.REQUEST_EDIT);
			break;
		case NoteUtil.MENU_DELETE:
			new AlertDialog.Builder(CheckNoteActivity.this).setTitle(R.string.menu_delete)
					.setMessage(getResources().getString(R.string.delete_msg_2, mNoteBook.getTitle()))
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Uri uri = Uri.parse(NoteBookMetaData.NoteBook.CONTENT_URI + "/" + itemId);
							getContentResolver().delete(uri, null, null);
							
							NoteManager.isNeedRefresh = true;
							NoteManager.isFirst = true;
							
							CheckNoteActivity.this.finish();
						}
					}).setNegativeButton(android.R.string.cancel, null).create().show();
			break;
		case NoteUtil.MENU_SHARE:
			Uri smsToUri = Uri.parse("smsto:");// 联系人地址

        	Intent mIntent = new Intent(android.content.Intent.ACTION_SENDTO,smsToUri);
        	//String noteContent=NoteEdit.getBodyContent();
        	mIntent.putExtra("sms_body", contentText.getText()+"\n" +
        			"由“XG笔记”发送");// 短信的内容

        	startActivity(mIntent);
        	break;
		case android.R.id.home://
			// 点击左上角应用图标，返回，默认情况下，图标的ID是android.R.id.home
			setResult(RESULT_OK, mData);
			this.finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			setResult(RESULT_OK, mData);
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			// query one record
			mNoteBook = NoteManager.getNotesFromId(itemId, this);
			mData = data;
		} else if (resultCode == RESULT_CANCELED) {

		}
	}

}
