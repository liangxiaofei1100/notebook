package com.yuri.notebook;

import com.yuri.notebook.utils.NoteUtil;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.TextView;

public class AboutActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.about);
		
		NoteUtil.setShowTitleBackButton(AboutActivity.this);
		
		TextView mAuthorText = (TextView) findViewById(R.id.author);
		
		SharedPreferences sp = getSharedPreferences(NoteUtil.SHARED_NAME, MODE_PRIVATE);
		final Editor editor = sp.edit();
		//隐藏功能 ，长按可再次进入首次启动画面
		mAuthorText.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				editor.putBoolean(NoteUtil.FIRST_START, true);
				editor.commit();
				return true;
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (android.R.id.home == item.getItemId()) {
			this.finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
