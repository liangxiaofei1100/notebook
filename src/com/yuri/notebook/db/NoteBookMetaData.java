package com.yuri.notebook.db;

import android.net.Uri;
import android.provider.BaseColumns;

public class NoteBookMetaData{
	public static final String DATABASE_NAME = "notebook.db";
	public static final int DATABASE_VERSION = 1;

	public static final String AUTHORITY = "com.yuri.notebook.db.notebookprovider";
	
	/**profiles table*/
	public static final class NoteBook implements BaseColumns{
		public static final String TABLE_NAME = "notebooks";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/notebooks");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/notebooks";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/notebooks";
		
		public static final String TABLE_MAIL = "mails";
		public static final Uri MAIL_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/mails");
		public static final String MAIL_CONTENT_TYPE = "vnd.android.cursor.dir/mails";
		public static final String MAIL_CONTENT_TYPE_ITEM = "vnd.android.cursor.item/mails";
		
		/**order by _id ASC*/
		public static final String SORT_ORDER_DEFAULT = _ID + "ASC"; 
		
		//items
		/**note title. Type:String*/
		public static final String TITLE = "title";
		/**note content. Type:String*/
		public static final String CONTENT = "content";
		/**note create time. Type:Long*/
		public static final String TIME = "time";
		
		//mail table
		/**mail.Type:String*/
		public static final String MAIL_ADDRESS = "mail";
	}
}
