package com.yuri.notebook.login;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yuri.notebook.R;
import com.yuri.notebook.loader.NoteLoader;
import com.yuri.notebook.login.LockPatternView.Cell;
import com.yuri.notebook.login.LockPatternView.DisplayMode;
import com.yuri.notebook.login.LockPatternView.OnPatternListener;
import com.yuri.notebook.utils.NoteUtil;

public class LockPatternActivity extends Activity implements OnClickListener, OnPatternListener, OnLongClickListener {

	private LockPatternView lockPatternView;
	private LockPatternUtils lockPatternUtils;
	
	private SharedPreferences sp = null;
	
	private Button leftButton,rightButton;
	private TextView tipText;
	private LinearLayout buttonLayout;
	
	private boolean isFirst = true;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.pattern_main);
		lockPatternView = (LockPatternView) findViewById(R.id.lpv_lock);
		
		sp = getSharedPreferences(NoteUtil.SHARED_NAME, Activity.MODE_PRIVATE);
		isFirst = sp.getBoolean(NoteUtil.PATTERN_INIT_KEY, true);
		
		setTitle(R.string.pattern_title);

		lockPatternUtils = new LockPatternUtils(this);
		lockPatternView.setOnPatternListener(this);
		
		tipText = (TextView)findViewById(R.id.hidetext);
		tipText.setOnLongClickListener(this);
		
		buttonLayout = (LinearLayout)findViewById(R.id.button_layout);
		
		//button
		leftButton = (Button)findViewById(R.id.left_button);
		leftButton.setEnabled(false);
		leftButton.setOnClickListener(this);
		rightButton = (Button)findViewById(R.id.right_button);
		rightButton.setEnabled(false);
		rightButton.setOnClickListener(this);
		
		if (isFirst) {
			tipText.setText(R.string.lock_msg_init);
			buttonLayout.setVisibility(View.VISIBLE);
		}else {
			tipText.setText(R.string.lock_draw_tip);
			buttonLayout.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.left_button:
			lockPatternView.clearPattern();
			
			leftButton.setEnabled(false);
			rightButton.setEnabled(false);
			
			tipText.setText(R.string.lock_draw_tip);
			
			lockPatternView.setEnabled(true);
			break;
		case R.id.right_button:
			lockPatternView.clearPattern();
			
			isFirst = false;
			buttonLayout.setVisibility(View.INVISIBLE);
			
			Toast.makeText(LockPatternActivity.this, R.string.pattern_set_success, Toast.LENGTH_LONG)
			.show();
			
			tipText.setText(R.string.lock_draw_tip);
			
			lockPatternView.setEnabled(true);
			
			//save the status
			Editor editor = sp.edit();
			editor.putBoolean(NoteUtil.PATTERN_INIT_KEY, false);
			editor.commit();
			break;

		default:
			break;
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		//reset password
		new AlertDialog.Builder(this)
			.setTitle(R.string.pa_reset_pw)
			.setMessage(R.string.reset_confirm)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					lockPatternView.clearPattern();
					lockPatternUtils.clearLock();
					
					Editor editor = sp.edit();
					editor.putBoolean(NoteUtil.PATTERN_INIT_KEY, true);
					editor.commit();
				}
			})
			.setNegativeButton(android.R.string.cancel, null)
			.create().show();
		return true;
	}

	//LockPatternView listening start
	@Override
	public void onPatternStart() {
		//手指刚开始划
		if (isFirst) {
			tipText.setText(R.string.lock_draw_tip2);
			leftButton.setEnabled(false);
			rightButton.setEnabled(false);
		}
	}

	@Override
	public void onPatternCleared() {
	}

	@Override
	public void onPatternCellAdded(List<Cell> pattern) {
		//当划到一个点时
	}

	@Override
	public void onPatternDetected(List<Cell> pattern) {
		//手指松开时
		if(!isFirst){
			int result = lockPatternUtils.checkPattern(pattern);
			if (result!= 1) {
				if(result==0){
					lockPatternView.setDisplayMode(DisplayMode.Wrong);
//					Toast.makeText(LockPatternActivity.this, R.string.pattern_set_error, Toast.LENGTH_LONG)
//					.show();
					tipText.setText(R.string.lock_msg_retry);
				}else{
					lockPatternView.clearPattern();
					Toast.makeText(LockPatternActivity.this, R.string.pattern_set_tip, Toast.LENGTH_LONG)
					.show();
				}
			} else {
				Intent intent = new Intent();
//				intent.setClass(LockPatternActivity.this, HomePageActivity.class);
				intent.setClass(LockPatternActivity.this, NoteLoader.class);
				startActivity(intent);
				finish();
			}
		}else{
			//第一次登陆，设置密码
			//save temp
			lockPatternView.setEnabled(false);
			
			leftButton.setEnabled(true);
			rightButton.setEnabled(true);
			
			tipText.setText(R.string.lock_msg_new);
			
			lockPatternUtils.saveLockPattern(pattern);
			
		}
	}
	//LockPatternView listening end
}
