package com.yuri.notebook.loader;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.yuri.notebook.R;
import com.yuri.notebook.unuse.CrashHandler;

//@ReportsCrashes(formKey = "",
//				)
public class NoteApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

//		CrashHandler crashHandler = CrashHandler.getInstance();
//		crashHandler.init(this);

//		ACRA.init(this);
	}
}
