package com.yuri.notebook.activitys;

import com.yuri.notebook.utils.NoteUtil;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class NoteSettingActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		NoteUtil.setShowTitleBackButton(NoteSettingActivity.this);
		
		getFragmentManager().beginTransaction().replace(android.R.id.content, new NoteSettingFragment()).commit();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (android.R.id.home == item.getItemId()) {
			this.finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
