package com.yuri.notebook.activitys;

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
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.yuri.notebook.NoteManager;
import com.yuri.notebook.R;
import com.yuri.notebook.bean.Note;
import com.yuri.notebook.db.MetaData;
import com.yuri.notebook.utils.DateFormatUtils;
import com.yuri.notebook.utils.NoteUtil;

public class CheckNoteActivity extends Activity {
	private static final String TAG = "CheckNoteActivity";
	private TextView mContentTv;
	private TextView mGroupTv;
	private TextView mDateTv;

	private long itemId;

	private NoteManager mNoteManager;

	private Note mNoteBook;
	
	private long mTime = 0;
	
	private SharedPreferences sp = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.query);

		// 标题栏返回
		NoteUtil.setShowTitleBackButton(CheckNoteActivity.this);
		setTitle(R.string.check_title);
		
		mContentTv = (TextView) findViewById(R.id.note_content);
		mGroupTv = (TextView) findViewById(R.id.tv_group);
		mDateTv = (TextView) findViewById(R.id.tv_time);
		
		Intent intent = this.getIntent();
		itemId = intent.getLongExtra(NoteUtil.ITEM_ID_INDEX, -1);

		mNoteBook = NoteManager.getNotesFromId(itemId, this);
		mContentTv.setText(mNoteBook.getContent());
		
		String group = mNoteBook.getGroup();
		String groupWork = getResources().getString(R.string.type_work);
		String groupPersonal = getResources().getString(R.string.type_personal);
		String groupFamily = getResources().getString(R.string.type_family);
		String groupStudy = getResources().getString(R.string.type_study);
		if (group.equals(groupWork)) {
			mGroupTv.setTextColor(getResources().getColor(R.color.work));
		} else if (group.equals(groupPersonal)) {
			mGroupTv.setTextColor(getResources().getColor(R.color.personal));
		} else if (group.equals(groupFamily)) {
			mGroupTv.setTextColor(getResources().getColor(R.color.family));
		} else if (group.equals(groupStudy)) {
			mGroupTv.setTextColor(getResources().getColor(R.color.study));
		} else {
			mGroupTv.setTextColor(getResources().getColor(R.color.none));
		}
		mGroupTv.setText(group);
		
		DateFormatUtils dateFormatUtils = new DateFormatUtils(getApplicationContext(), System.currentTimeMillis());
		mDateTv.setText(dateFormatUtils.getDateFormatString(mNoteBook.getTime()));
//		sp = getSharedPreferences(NoteUtil.SHARED_NAME, MODE_PRIVATE);
//		int font_size = sp.getInt(NoteUtil.FONT_SET, 1);
//		NoteUtil.setFontSize(contentText, font_size);
//		int color = sp.getInt(NoteUtil.COLOR_SET, 3);
//		NoteUtil.setBackgroundColor(contentText, color);
		setResult(RESULT_CANCELED);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private Intent createShareIntent(){
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, mContentTv.getText().toString().trim() + "\n" + "BY XG NOTE!");
		intent.setType("*/*");
		return intent;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.check_note_menu, menu);
		MenuItem menuItem = menu.findItem(R.id.menu_share);
		ShareActionProvider actionProvider = (ShareActionProvider) menuItem.getActionProvider();
		actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		actionProvider.setShareIntent(createShareIntent());
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_edit:
			Intent intent = new Intent(CheckNoteActivity.this, EditNoteActivity.class);
			intent.putExtra(NoteUtil.ITEM_ID_INDEX, itemId);
			startActivity(intent);
			this.finish();
			break;
		case R.id.menu_delete:
			new AlertDialog.Builder(CheckNoteActivity.this).setTitle(R.string.menu_delete)
					.setMessage(R.string.delete_msg_2)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Uri uri = Uri.parse(MetaData.NoteColumns.CONTENT_URI + "/" + itemId);
							getContentResolver().delete(uri, null, null);
							
							setResult(RESULT_OK);
							CheckNoteActivity.this.finish();
						}
					}).setNegativeButton(android.R.string.cancel, null).create().show();
			break;
		case R.id.menu_share:
//			Uri smsToUri = Uri.parse("smsto:");// 联系人地址
//
//        	Intent mIntent = new Intent(android.content.Intent.ACTION_SENDTO,smsToUri);
//        	//String noteContent=NoteEdit.getBodyContent();
//        	mIntent.putExtra("sms_body", contentText.getText()+"\n" +
//        			"由“XG笔记”发送");// 短信的内容
//
//        	startActivity(mIntent);
			Intent mIntent  = new Intent();
			mIntent.setAction(Intent.ACTION_SEND);
			mIntent.setType("*/*");
			startActivity(mIntent);
			
        	break;
		case android.R.id.home:
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
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
