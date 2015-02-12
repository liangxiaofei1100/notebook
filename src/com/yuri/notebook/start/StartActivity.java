package com.yuri.notebook.start;

import cn.bmob.v3.Bmob;

import com.yuri.notebook.login.LoginActivity;
import com.yuri.notebook.utils.NoteUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class StartActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sp = getSharedPreferences(NoteUtil.SHARED_NAME, MODE_PRIVATE);
		
		Bmob.initialize(getApplicationContext(), "eabe69f83c580db19198fd5885bff4cd");
		
		//暂时取消第一次启动画面
		boolean first_start = sp.getBoolean(NoteUtil.FIRST_START, false);
		Intent intent = new Intent();
		if(first_start){
			intent.setClass(this, AppIntroduce.class);
		}else {
			intent.setClass(this, LoginActivity.class);
		}
		
		startActivity(intent);
		this.finish();
	}
}
