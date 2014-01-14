package com.yuri.notebook.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;

import com.yuri.notebook.R;
import com.yuri.notebook.R.string;
import com.yuri.notebook.db.NoteMetaData;
import com.yuri.notebook.db.NoteMetaData.Note;

public class NoteManager {
	
	private Context mContext;
	private static NoteManager mNoteBookManager;
	
	public static boolean isFirst = true;

	public static NoteManager getInstance(Context context){
		if (mNoteBookManager == null) {
			mNoteBookManager = new NoteManager(context);
		}
		
		return mNoteBookManager;
	}
	
	private NoteManager(Context context){
		mContext = context;
	}
	
	public static Notes getNotesFromId(long id, Context context){
		Notes noteBook = new Notes(id);
		Uri uri = Uri.parse(NoteMetaData.Note.CONTENT_URI + "/" + id);
		Cursor cur = null;
		try {
			cur = context.getContentResolver().query(uri, NoteUtil.COLUMNS, null, null, null);
			
			if (cur.moveToFirst()) {
				do {
					noteBook.setTitle(cur.getString(cur.getColumnIndex(Note.TITLE)));
					noteBook.setContent(cur.getString(cur.getColumnIndex(Note.CONTENT)));
					noteBook.setGroup(cur.getString(cur.getColumnIndex(Note.GROUP)));
					noteBook.setTime(cur.getLong(cur.getColumnIndex(Note.TIME)));
				} while (cur.moveToNext());
			}
		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			if (cur != null) {
				cur.close();
			}
		}
		
		return noteBook;
	}
	
	public static void backAlert(final Activity activity){
		new AlertDialog.Builder(activity)
		.setMessage(R.string.back_msg)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				activity.finish();
			}
		})
		.setNegativeButton(android.R.string.cancel, null)
		.create().show();
	}
	
	//解压文件 （对复杂的文件目录进行解压）
    //参数：File zipFile 表示 需要被解压的zip文件     例如：new File("/root/abc/111.zip")
    //参数：String descDir 表示 解压后文件的保存路径   例如："/root/abc"
    public static  String unZipFiles(File zipFile,String descDir)throws IOException{ 
    	
        File pathFile = new File(descDir); 
        if(!pathFile.exists()){ 
            pathFile.mkdirs(); 
        } 
        ZipFile zip = new ZipFile(zipFile); 
        for(Enumeration entries = zip.entries();entries.hasMoreElements();){ 
            ZipEntry entry = (ZipEntry)entries.nextElement(); 
            String zipEntryName = entry.getName(); 
            InputStream in = zip.getInputStream(entry); 
            String outPath = (descDir+zipEntryName).replaceAll("\\*", "/");; 
            //判断路径是否存在,不存在则创建文件路径 
            File file = new File(outPath.substring(0, outPath.lastIndexOf('/'))); 
            if(!file.exists()){ 
                file.mkdirs(); 
            } 
            //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压 
            if(new File(outPath).isDirectory()){ 
                continue; 
            } 
             
            OutputStream out = new FileOutputStream(outPath); 
            byte[] buf1 = new byte[1024]; 
            int len; 
            while((len=in.read(buf1))>0){ 
                out.write(buf1,0,len); 
            } 
            in.close(); 
            out.close(); 
            } 
        
        return descDir;
    } 
    
    /**
     * 递归删除文件和文件夹
     * @param file    要删除的根目录
     */
	public static void recursionDeleteFile(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}
		if (file.isDirectory()) {
			File[] childFile = file.listFiles();
			if (childFile == null || childFile.length == 0) {
				file.delete();
				return;
			}
			for (File f : childFile) {
				recursionDeleteFile(f);
			}
			file.delete();
		}
	}
}
