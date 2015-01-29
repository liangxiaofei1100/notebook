package com.yuri.notebook;

import java.io.File;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DbHelper {
	private static final String TAG = DbHelper.class.getSimpleName();

	public SQLiteDatabase getReadDb() {
		File file = new File("/data/data/com.yuri.notebook/files/database/notebook.db");
		if (file.exists() && file.isFile()) {
			try {
				return SQLiteDatabase.openDatabase("/data/data/com.yuri.notebook/files/database/notebook.db", null,
						SQLiteDatabase.OPEN_READWRITE);
			} catch (Exception e) {
				Log.e(TAG, "getReadDb.error:" + e.toString());
				e.printStackTrace();
			}
		}
		return null;
	}

}
