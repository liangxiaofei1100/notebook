package com.yuri.notebook.login;

import com.yuri.notebook.activitys.NoteMainActivity;
import com.yuri.notebook.utils.NoteUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class LoginActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences sp = getSharedPreferences(NoteUtil.SHARED_NAME, MODE_PRIVATE);
		//first judge whether need use password,default is false
		boolean needPasswod = sp.getBoolean(NoteUtil.USE_PASSWORD, false);
		if (needPasswod) {
			//if need use password,judge use pattern or number
			int mode  = sp.getInt(NoteUtil.LOGIN_MODE, 0);//default is 0
			Intent intent = new Intent();
			if (mode == 0) {//pattern login mode
				intent.setClass(LoginActivity.this, LockPatternActivity.class);
			}else if(mode == 1){//password login mode 
				intent.setClass(getApplicationContext(), LoginPasswdActivity.class);
			}
			
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			startActivity(intent);
		}else {
			Intent intent = new Intent();
			intent.setClass(LoginActivity.this, NoteMainActivity.class);
			startActivity(intent);
		}
		this.finish();
	}
}
