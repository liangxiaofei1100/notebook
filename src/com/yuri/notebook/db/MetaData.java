package com.yuri.notebook.db;

import android.net.Uri;
import android.provider.BaseColumns;

public class MetaData{
	public static final String DATABASE_NAME = "notebook.db";
	public static final int DATABASE_VERSION = 3;

	public static final String AUTHORITY = "com.yuri.notebook.db.notebookprovider";
	
	/**profiles table*/
	public static final class NoteColumns2 implements BaseColumns{
		public static final String TABLE_NAME = "notebook";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/notebook");
		public static final Uri CONTENT_FILTER_URI = Uri.parse("content://" + AUTHORITY + "/notebook_filter");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/notebook";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/notebook";
		
		//items
		/**note title. Type:String*/
		public static final String OBJECT_ID = "objectId";
		/**note content. Type:String*/
		public static final String CONTENT = "content";
		/**note create time. Type:Long*/
		public static final String TIME = "time";
		/**note group. Type:String*/
		public static final String GROUP = "note_group";//不能用group，保留字
		
		/**order by _id DESC*/
		public static final String SORT_ORDER_DEFAULT = _ID + " DESC"; 
		/**order by time DESC*/
		public static final String SORT_ORDER_TIME = TIME + " DESC"; 
		/**order by group DESC*/
		public static final String SORT_ORDER_GROUP = GROUP + " ASC";
	}
	
	/**profiles table*/
	public static final class NoteColumns implements BaseColumns{
		public static final String TABLE_NAME = "note";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/note");
		public static final Uri CONTENT_FILTER_URI = Uri.parse("content://" + AUTHORITY + "/note_filter");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/note";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/note";
		
		//items
		/**note title. Type:String*/
		public static final String OBJECT_ID = "objectId";
		/**note content. Type:String*/
		public static final String CONTENT = "content";
		/**note create time. Type:Long*/
		public static final String TIME = "time";
		/**note group. Type:String*/
		public static final String GROUP = "note_group";//不能用group，保留字
		
		/**order by _id DESC*/
		public static final String SORT_ORDER_DEFAULT = _ID + " DESC"; 
		/**order by time DESC*/
		public static final String SORT_ORDER_TIME = TIME + " DESC"; 
		/**order by group DESC*/
		public static final String SORT_ORDER_GROUP = GROUP + " ASC";
	}
}
