package com.yuri.notebook.utils;

import android.util.Log;

public class LogUtils {
	private static final boolean DEBUG = true;
	private static final String TAG = "Yuri/";
	
	public static void i(String tag, String msg){
		if(DEBUG){
			Log.i(TAG + tag, msg);
		}
	}
	
	public static void d(String tag, String msg){
		if(DEBUG){
			Log.d(TAG + tag, msg);
		}
	}
	
	public static void v(String tag, String msg){
		if(DEBUG){
			Log.v(TAG + tag, msg);
		}
	}
	
	public static void e(String tag, String msg){
		if(DEBUG){
			Log.e(TAG + tag, msg);
		}
	}
}
