package com.yuri.notebook.login;

import com.yuri.notebook.R;
import com.yuri.notebook.loader.NoteLoader;
import com.yuri.notebook.utils.NoteUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginPasswdActivity extends Activity implements OnClickListener, OnLongClickListener{
	private static final String TAG = "LoginPasswdActivity";
	//登陆按钮，退出按钮
	private Button loginBtn,exitBtn;
	
	private static final int MENU_MODIFY_PW = 2;
	
	private static final int MODEL_PW = 0x10;
	private static int model = -1;
	
	private SharedPreferences sp = null;
	private Editor editor;
	
	private String password = null;//临时保存密码
	
	private View dialogView = null;
	
	private EditText passwdEdit = null;//登陆密码输入框
	private EditText twoedit = null;
	private EditText threeedit = null;
	private EditText fouredit = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_password);
		
		sp = getSharedPreferences(NoteUtil.SHARED_NAME, Activity.MODE_PRIVATE);
		initResource();
		
		model = MODEL_PW;
		
		editor = sp.edit();
		
		password = sp.getString(NoteUtil.PASSWORD, null);
		if (password == null) {
			//如果密码为空，则弹出对话框设定初始化密码
			initSet();
		}
	}
	
	private void initResource(){
		passwdEdit = (EditText)findViewById(R.id.password_edit);
//		NoteUtil.onFocusChange(passwdEdit, true);
		
		loginBtn = (Button)findViewById(R.id.loginButton);
		exitBtn = (Button)findViewById(R.id.exitButton);
		loginBtn.setOnClickListener(this);
		exitBtn.setOnClickListener(this);
		loginBtn.setOnLongClickListener(this);
	}
	
	public void initSet(){//初始化设定
		LayoutInflater factory = LayoutInflater.from(LoginPasswdActivity.this);
		dialogView = factory.inflate(R.layout.set_main, null);
		threeedit = (EditText)dialogView.findViewById(R.id.newpw_edit);
		fouredit = (EditText)dialogView.findViewById(R.id.confirmpw_edit);
		
		AlertDialog dlg = new AlertDialog.Builder(LoginPasswdActivity.this)
		.setTitle(R.string.pw_msg_01)
		.setView(dialogView)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String new_pw = threeedit.getText().toString();
				String confirm_pw = fouredit.getText().toString();
				if (new_pw==null || new_pw.equals("") || confirm_pw==null || confirm_pw.equals("")) {
					NoteUtil.setDialogDismiss(dialog, false);
					Toast.makeText(LoginPasswdActivity.this, R.string.pw_msg_02, Toast.LENGTH_SHORT).show();
				}else if (new_pw.equals(confirm_pw)) {
					editor.putString(NoteUtil.PASSWORD, new_pw);
					editor.commit();//save password
					
					NoteUtil.setDialogDismiss(dialog, true);
					dialog.dismiss();
					
					Toast.makeText(LoginPasswdActivity.this, R.string.pw_msg_03, Toast.LENGTH_SHORT).show();
				}else if (!new_pw.equals(confirm_pw)) {
					NoteUtil.setDialogDismiss(dialog, false);
					Toast.makeText(LoginPasswdActivity.this, R.string.pw_msg_04, Toast.LENGTH_SHORT).show();
				}
			}
		})
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				LoginPasswdActivity.this.finish();
			}
		})
		.create();
		dlg.setCancelable(false);
		dlg.show();
	}
	
	public void modifyPassword(){//密码修改
		password = sp.getString(NoteUtil.PASSWORD, null);
		LayoutInflater factory = LayoutInflater.from(LoginPasswdActivity.this);
		dialogView = factory.inflate(R.layout.modify_main, null);
		twoedit = (EditText)dialogView.findViewById(R.id.twoedit);//旧密码输入框
		threeedit = (EditText)dialogView.findViewById(R.id.threeedit);//新密码输入框
		fouredit = (EditText)dialogView.findViewById(R.id.fouredit);//确认密码输入框
		AlertDialog dlg = new AlertDialog.Builder(LoginPasswdActivity.this)
		.setTitle(R.string.pw_modify_tip)
		.setView(dialogView)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				String two = twoedit.getText().toString().trim();
				String three = threeedit.getText().toString().trim();
				String four = fouredit.getText().toString().trim();
				if (two == null || two.equals("") || three == null || three.equals("") || four == null || four.equals("")) {
					NoteUtil.setDialogDismiss(dialog, false);
					Toast.makeText(LoginPasswdActivity.this, R.string.pw_msg_02, Toast.LENGTH_SHORT).show();
				}else if (!two.equals(password)) {
					clearEditText();
					NoteUtil.setDialogDismiss(dialog, false);
					Toast.makeText(LoginPasswdActivity.this, R.string.pw_old_error, Toast.LENGTH_SHORT).show();
				}else if (!four.equals(three)) {
					clearEditText();
					NoteUtil.setDialogDismiss(dialog, false);
					Toast.makeText(LoginPasswdActivity.this, R.string.pw_msg_04, Toast.LENGTH_SHORT).show();
				}else if (two.equals(password)&&four.equals(three)) {
					editor.putString(NoteUtil.PASSWORD, three);
					editor.commit();
					NoteUtil.setDialogDismiss(dialog, true);
					dialog.dismiss();
					Toast.makeText(LoginPasswdActivity.this, R.string.pw_modify_ok, Toast.LENGTH_SHORT).show();
				}else {
					
				}
			}
		})
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				NoteUtil.setDialogDismiss(dialog, true);
				dialog.dismiss();
			}
		})
		.create();
		dlg.show();
		
	}
	
	public void clearEditText(){
		twoedit.setText("");
		threeedit.setText("");
		fouredit.setText("");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.loginButton:
			password = sp.getString(NoteUtil.PASSWORD, null);
			String pw = passwdEdit.getText().toString().trim();
			if (password.equals(pw)) {
				Intent intent = new Intent();
				intent.setClass(this, NoteLoader.class);
				startActivity(intent);
				LoginPasswdActivity.this.finish();
			}else {
				Toast.makeText(LoginPasswdActivity.this, R.string.error_password, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.exitButton:
			LoginPasswdActivity.this.finish();
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
			menu.add(0, MENU_MODIFY_PW, 1, R.string.modify);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_MODIFY_PW:
			modifyPassword();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onLongClick(View v) {
		//隐藏功能，长按登陆按钮，清空密码
		if (R.id.loginButton == v.getId()) {
			new AlertDialog.Builder(LoginPasswdActivity.this)
				.setMessage(R.string.reset_confirm)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						editor.putString(NoteUtil.PASSWORD, null);
						editor.commit();
						initSet();
						Toast.makeText(LoginPasswdActivity.this, R.string.reset_ok, Toast.LENGTH_SHORT).show();
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create().show();
		}
		return true;
	}
	
}
