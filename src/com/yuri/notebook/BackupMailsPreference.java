package com.yuri.notebook;

import com.yuri.notebook.utils.NoteUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

/**@unuse*/
public class BackupMailsPreference extends Preference implements Preference.OnPreferenceChangeListener, OnClickListener{
	private TextView mMailText;
	private TextView mSummaryText;
	private SharedPreferences sp;
	private String mMail;

	public BackupMailsPreference(Context context) {
		super(context);
		init(context);
	}
	
	public BackupMailsPreference(Context context, String name) {
        super(context);
        init(context);
    }
	
	private void init(Context context){
		sp = context.getSharedPreferences(NoteUtil.SHARED_NAME, Context.MODE_PRIVATE);
		setLayoutResource(R.layout.preference_mail);
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mMail = sp.getString(NoteUtil.MAIL, null);
		
		View mailView = view.findViewById(R.id.name_pref);
		mailView.setOnClickListener(this);
		mMailText = (TextView) mailView.findViewById(R.id.title);
		mMailText.setText(R.string.mail_backup);
		
		mSummaryText = (TextView) mailView.findViewById(R.id.summary);
		if (mMail == null) {
			mSummaryText.setText(R.string.mail_not_setting);
		}else {
			mSummaryText.setText(mMail);
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		callChangeListener(preference);
		return false;
	}

	@Override
	public void onClick(View v) {
		if (v != null) {
			Context context = getContext();
			if (context != null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				final View view = inflater.inflate(R.layout.mail_setting, null);
				final EditText editText = (EditText)view.findViewById(R.id.mail_add);
				int title_resid;
				if (mMail == null) {
					title_resid = R.string.mail_setting;
				}else {
					title_resid = R.string.mail_modify;
					editText.setText(mMail);
				}
				
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(title_resid);
                builder.setView(view);
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            	mMail = editText.getText().toString().trim();
                            	Editor editor = sp.edit();
                            	editor.putString(NoteUtil.MAIL, mMail);
                            	editor.commit();
                            	mSummaryText.setText(mMail);
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, null);
                AlertDialog dialog = builder.create();
                dialog.show();
			}
		}
	}
	

}
