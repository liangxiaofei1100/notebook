package com.yuri.notebook.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.yuri.notebook.db.NoteMetaData;

public class NoteUtil {
	public static final String TAG = "NoteUtil";
	public static final boolean DEBUG = false;

	public static final int MENU_SAVE = 0x01;
	public static final int MENU_CLOSE = 0x02;
	public static final int MENU_EDIT = 0x03;
	public static final int MENU_ADD = 0x04;
	public static final int MENU_OPEN = 0x05;
	public static final int MENU_DELETE = 0x06;
	public static final int MENU_DEBUG = 0x07;
	public static final int MENU_BACKUP = 0x08;
	public static final int MENU_SELETEALL = 0x09;
	public static final int MENU_CANCELALL = 0x10;
	public static final int MENU_DELETE_MULTI = 0x11;
	public static final int MENU_SETTING = 0x12;
	public static final int MENU_EDIT_TITLE = 0x13;
	public static final int MENU_RECOVER = 0x14;
	public static final int MENU_SHARE = 0x15;
	public static final int MENU_SEARCH = 0x16;

	public static final String ITEM_ID_INDEX = "index";
	public static final String NOTEBOOK = "notebook";

	public static final String MENU_MODE = "menu_mode";

	// cloumns
	public static final String[] COLUMNS = new String[] {
			NoteMetaData.Note._ID, NoteMetaData.Note.TITLE,
			NoteMetaData.Note.CONTENT, NoteMetaData.Note.GROUP, 
			NoteMetaData.Note.TIME };

	// Service Action
	public static final String ACTION_INSERT = "com.yuri.notebook.action_insert";
	public static final String ACTION_DELETE = "com.yuri.notebook.action_delete";
	public static final String ACTION_UPDATE = "com.yuri.notebook.action_update";
	public static final String ACTION_QUERY_ALL = "com.yuri.notebook.action_query_all";
	public static final String ACTION_QUERY_SINGLE = "com.yuri.notebook.action_query_one";

	// key
	public static final String SN_KEY = "sn";
	public static final String ID_KEY = "id";
	public static final String TITLE_KEY = "title";
	public static final String CONTENT_KEY = "content";
	public static final String TIME_KEY = "time";
	public static final String ITEM_KEY = "item";

	public static final int REQUEST_EDIT = 0x01;

	// path
	public static final String DEFAULT_PATH = Environment
			.getExternalStorageDirectory() + "/Note";
	public static final String DATA_PATH = "/data/data/com.yuri.notebook/backup";
	public static final String EXTENSION_TXT = ".txt";
	public static final String EXTENSION_ZIP = ".zip";
	public static final String EXTENSION_XML = ".xml";
	public static final String UNZIP_PATH = DEFAULT_PATH + "backup/";

	public static final SimpleDateFormat FILE_FORMAT = new SimpleDateFormat(
			"yyyyMMdd_HHmm");

	// SharedPreferences
	public static final String SHARED_NAME = "note_share";
	// 判断是否设置图案密码. Type:boolean
	public static final String PATTERN_INIT_KEY = "pattern_inited";
	//设置是否需要设置密码登陆. Type：boolean
	public static final String USE_PASSWORD = "user_password";
	// 设定登陆方式. Type:int
	public static final String LOGIN_MODE = "login_mode";
	// 保存密码. Type:String
	public static final String PASSWORD = "password";
	// 备份邮件. Type:String
	public static final String MAIL = "mail";
	// 第一次启动，Type:boolean
	public static final String FIRST_START = "first_start";
	// 字体设置。Type:int
	public static final String FONT_SET = "font_set";
	// 背景设置。 Type：int
	public static final String COLOR_SET = "color_set";
	//sort 
	public static final String LIST_SORT = "list_sort";

	public static final int REQUEST_CODE_EDIT = 0x00121;

	private static final String ZIP_PW = "xiayouli";
	
	public static final String ENTER = "\n";

	public static class Color {
		public static final int COLOR_1 = 0xCDCDCD;
		public static final int GRAY = android.graphics.Color.GRAY;
		public static final int PALE_YELLOW = 0xffFFEC8B;
		public static final int PINK = 0XFFCD96CD;
		public static final int BLUE = 0XFFD1EEEE;
	}
	
	public static final int MODE_NORMAL = 0;
	public static final int MODE_MENU = 1;

	// get time
	public static String getTime() {
		String time = "";
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		long hour = calendar.get(Calendar.HOUR_OF_DAY);
		long minute = calendar.get(Calendar.MINUTE);

		time = "" + year + "-" + month + "-" + day + " / " + hour + ":"
				+ minute;
		return time;
	}

	/** 实现复制粘贴功能 */
	public static void emulateShiftHeld(KeyEvent.Callback view) {
		try {
			KeyEvent shiftPressEvent = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
					KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
			shiftPressEvent.dispatch(view);
		} catch (Exception e) {
		}
	}

	/** set dialog dismiss or not, user for AlertDialog */
	public static void setDialogDismiss(DialogInterface dialog, boolean dismiss) {
		try {
			Field field = dialog.getClass().getSuperclass()
					.getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, dismiss);
			dialog.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String BufferReaderDemo(String path) throws IOException {
		File file = new File(path);

		if (!file.exists() || file.isDirectory()) {
			throw new FileNotFoundException();
		}

		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);

		String temp = null;
		StringBuffer sb = new StringBuffer();
		temp = br.readLine();

		while (temp != null) {
			sb.append(temp + "\n");
			temp = br.readLine();
		}

		fr.close();
		br.close();

		return sb.toString();
	}

	/** 将备份的内容先写入文件 */
	public static File fileWriteDemo(String title, String content) {
		// default path
		File file = new File(NoteUtil.DEFAULT_PATH);
		if (!file.exists()) {
			boolean ret = file.mkdirs();
			if (!ret) {
				Log.e(TAG, NoteUtil.DEFAULT_PATH + " make failed");
			}
		}
		//如果title中有/字符是不能作为文件名的
		title = title.replace("/", "");

		String filePath = NoteUtil.DEFAULT_PATH + "/" + title
				+ NoteUtil.EXTENSION_TXT;
		File tempFile = new File(filePath);
		if (!tempFile.exists()) {
			try {
				tempFile.createNewFile();
			} catch (IOException e) {
				Log.e(TAG,"fileWriteDemo:createNewFile faile:" + tempFile.getAbsolutePath());
				e.printStackTrace();
				return null;
			}
		}

		FileWriter fw = null;
		BufferedWriter bw = null;

		try {
			fw = new FileWriter(tempFile);
			bw = new BufferedWriter(fw);
			bw.write(content);
			bw.newLine();
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			fw = null;
			bw = null;
		}

		return tempFile;
	}

	/**
	 * 压缩文件 （对多个文件和文件夹进行压缩） 压缩方法使用的是可变参数，可以压缩1到多个文件，可以写数组的方式或一个个写到参数列表里。
	 * 
	 * @param zip
	 *            表示 压缩后生成的zip文件 例如：new File("/root/abc/111.zip")
	 * @param srcFiles
	 *            表示 需要被压缩的文件（可以是多个）
	 *            可以利用ArrayList的toArray()来提供参数，也可以利用数组，对于单个文件则直接传进来即可。
	 * @throws IOException
	 */
	public static void ZipFiles(File zip, File... srcFiles) throws IOException {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
		ZipFiles(out, "backup", srcFiles);
		out.close();
	}

	private static void ZipFiles(ZipOutputStream out, String path,
			File... srcFiles) {
		path = path.replaceAll("\\*", "/");
		if (!path.endsWith("/")) {
			path += "/";
		}
		byte[] buf = new byte[1024];
		try {
			for (int i = 0; i < srcFiles.length; i++) {
				if (srcFiles[i].isDirectory()) {
					File[] files = srcFiles[i].listFiles();
					String srcPath = srcFiles[i].getName();
					srcPath = srcPath.replaceAll("\\*", "/");
					if (!srcPath.endsWith("/")) {
						srcPath += "/";
					}
					out.putNextEntry(new ZipEntry(path + srcPath));
					ZipFiles(out, path + srcPath, files);
				} else {
					FileInputStream in = new FileInputStream(srcFiles[i]);
					out.putNextEntry(new ZipEntry(path + srcFiles[i].getName()));
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					out.closeEntry();
					in.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void showToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	public static void showToast(Context context, int msgId) {
		Toast.makeText(context, msgId, Toast.LENGTH_SHORT).show();
	}

	public static void showLongToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	public static void showLongToast(Context context, int msgId) {
		Toast.makeText(context, msgId, Toast.LENGTH_SHORT).show();
	}

	public static void setShowTitleBackButton(Activity activity) {
		// 标题栏返回
		final ActionBar bar = activity.getActionBar();
		int flags = ActionBar.DISPLAY_HOME_AS_UP;
		int change = bar.getDisplayOptions() ^ flags;
		bar.setDisplayOptions(change, flags);
	}

	/**
	 * show IME when need by manual
	 * 
	 * @param view
	 *            the view that need show IME
	 * @param hasFoucs
	 *            show or not
	 * */
	public static void onFocusChange(final View view, boolean hasFocus) {
		final boolean isFocus = hasFocus;
		(new Handler()).postDelayed(new Runnable() {
			public void run() {
				InputMethodManager imm = (InputMethodManager) view.getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (isFocus) {
					imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				} else {
					imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
			}
		}, 500);
	}

	/**
	 * 利用扩展包 zip4j.jar 进行加密压缩
	 * 
	 * @param file
	 *            压缩后的zip文件，如： "/sdcard/test.zip"
	 * @param files
	 *            需要压缩的文件
	 */
	public static void zipForPw(File file, ArrayList<File> files) {
		try {
			ZipFile zipFile = new ZipFile(file);

			ZipParameters zipParameters = new ZipParameters();
			zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			zipParameters.setEncryptFiles(true);
			zipParameters
					.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
			// 开始写密码
			zipParameters.setPassword(ZIP_PW);

			zipFile.addFiles(files, zipParameters);
		} catch (ZipException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 利用扩展包zip4j.jar 进行解加密的压缩包
	 * 
	 * @param path
	 */
	public static void unZipForPw(String path) {
		try {
			ZipFile zipFile = new ZipFile(path);

			if (zipFile.isEncrypted()) {
				zipFile.setPassword(ZIP_PW);
			}

			File file = new File(UNZIP_PATH);
			if (!file.exists()) {
				file.mkdirs();
			}

			zipFile.extractAll(NoteUtil.UNZIP_PATH);
		} catch (ZipException e) {
			e.printStackTrace();
		}
	}
	
	public static void setFontSize(TextView view, int size){
		switch (size) {
		case 0:
			view.setTextSize(30);
			break;
		case 1:
			view.setTextSize(20);
			break;
		case 2:
			view.setTextSize(10);
			break;
		default:
			LogUtils.e(TAG, "Error size=" + size);
			break;
		}
	}
	
	public static void setBackgroundColor(TextView view, int color){
		switch (color) {
		case 0:
			view.setBackgroundColor(NoteUtil.Color.GRAY);
			break;
		case 1:
			view.setBackgroundColor(NoteUtil.Color.PALE_YELLOW);
			break;
		case 2:
			view.setBackgroundColor(NoteUtil.Color.PINK);
			break;
		case 3:
			view.setBackgroundColor(NoteUtil.Color.BLUE);
			break;
		default:
			LogUtils.e(TAG, "Error color = " + color);
			break;
		}
	}

}
