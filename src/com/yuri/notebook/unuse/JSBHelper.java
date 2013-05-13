package com.yuri.notebook.unuse;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**@unuse
 * @deprecated
 * */
public class JSBHelper extends SQLiteOpenHelper {

	public JSBHelper(Context context, String name) {
		super(context, name, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table loginuser(ID INTEGER PRIMARY KEY,NAME varchar(20),TEXT varchar(20),TIME varchar(20))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}

}
