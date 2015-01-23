package com.yuri.notebook.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.baidu.frontia.Frontia;
import com.baidu.frontia.FrontiaData;
import com.baidu.frontia.FrontiaQuery;
import com.baidu.frontia.api.FrontiaStorage;
import com.baidu.frontia.api.FrontiaStorageListener.DataInfoListener;
import com.baidu.frontia.api.FrontiaStorageListener.DataInsertListener;
import com.baidu.frontia.api.FrontiaStorageListener.DataOperationListener;
import com.yuri.notebook.activitys.EditNoteActivity;
import com.yuri.notebook.activitys.NoteSettingFragment;
import com.yuri.notebook.bean.Note;
import com.yuri.notebook.utils.LogUtils;

public class FrontiaManager {
	private static final String TAG = FrontiaManager.class.getSimpleName();
	
	FrontiaStorage mFrontiaStorage = null;

	public FrontiaManager() {
		// TODO Auto-generated constructor stub
		mFrontiaStorage = Frontia.getStorage();
	}
	
	public void insertData(Note note, final Handler handler){
		JSONObject data = new JSONObject();
		try {
			data.put(Note.FRONTIA_KEY, Note.FRONTIA_VALUE);
			data.put(Note.ID, note.getId());
			data.put(Note.CONTENT, note.getContent());
			data.put(Note.GROUP, note.getGroup());
			data.put(Note.TIME, note.getTime());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		FrontiaData frontiaData = new FrontiaData();
		frontiaData.putAll(data);
		
		mFrontiaStorage.insertData(frontiaData, new DataInsertListener() {
			@Override
			public void onSuccess() {
				LogUtils.d(TAG, "insertData.onSuccess");
				
				if (handler == null) {
					return;
				}
				Message message = new Message();
				message.what = EditNoteActivity.MSG_UPLOAD_OVER;
				message.arg1 = 0;
				message.setTarget(handler);
				message.sendToTarget();
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				LogUtils.d(TAG, "insertData.onFailure.Error_code:" + arg0 + ",Error_msg:" + arg1);
				if (handler == null) {
					return;
				}
				Message message = new Message();
				message.what = EditNoteActivity.MSG_UPLOAD_OVER;
				message.arg1 = -1;
				message.setTarget(handler);
				message.sendToTarget();
			}
		});
	}
	
	
	public List<Note> queryAll(final Handler handler){
		FrontiaQuery frontiaQuery = new FrontiaQuery();
		frontiaQuery.equals(Note.FRONTIA_KEY, Note.FRONTIA_VALUE);
		return queryData(handler, frontiaQuery);
	}
	
	public Note querySingle(final Handler handler, String objectId){
		FrontiaQuery frontiaQuery = new FrontiaQuery();
		frontiaQuery.equals(Note.FRONTIA_KEY, Note.FRONTIA_VALUE);
		frontiaQuery.equals(Note.OBJECTID, objectId);
		return queryData(handler, frontiaQuery).get(0);
	}
	
	private List<Note> queryData(final Handler handler, FrontiaQuery query){
		final ArrayList<Note> resultList = new ArrayList<Note>();
		mFrontiaStorage.findData(query, new DataInfoListener() {
			@Override
			public void onSuccess(List<FrontiaData> data) {
				LogUtils.d(TAG, "queryData.onSuccess.size=" + data.size());
				JSONObject jsonObject = null;
				Note note = null;
				for (int i = 0; i < data.size(); i++) {
					jsonObject = data.get(i).toJSON();
					note = new Note();
					try {
						note.setId(jsonObject.getLong(Note.ID));
						note.setContent(jsonObject.getString(Note.CONTENT));
						note.setGroup(jsonObject.getString(Note.GROUP));
						long time = jsonObject.getLong(Note.TIME);
						note.setTime(time);
						String objectId = jsonObject.getString(Note.OBJECTID);
						if (TextUtils.isEmpty(objectId)) {
							objectId = Long.toHexString(time + new Random().nextInt(10000));
						}
						note.setObjectId(objectId);
						LogUtils.i(TAG, "find.content>>" + note.getContent());
					} catch (JSONException e) {
						e.printStackTrace();
						LogUtils.e(TAG, "queryData.error:" + e.toString());
					}
					resultList.add(note);
				}
				
				if (handler == null) {
					return;
				}
				
				Message message = new Message();
				message.what  = NoteSettingFragment.MSG_QUERY_ALL_OVER;
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("notelist", resultList);
				message.setData(bundle);
				message.setTarget(handler);
				message.sendToTarget();
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				LogUtils.d(TAG, "queryData.onFailure.Error_code:" + arg0 + ",Error_msg:" + arg1);
			}
		});
		
		return resultList;
	}
	
	public void deleteAll(Handler handler){
		FrontiaQuery frontiaQuery = new FrontiaQuery();
		frontiaQuery.equals(Note.FRONTIA_KEY, Note.FRONTIA_VALUE);
		delete(frontiaQuery, handler);
	}
	
	public void delete(Handler handler, List<String> objectIds){
		FrontiaQuery frontiaQuery = new FrontiaQuery();
		frontiaQuery.equals(Note.FRONTIA_KEY, Note.FRONTIA_VALUE);
		frontiaQuery.in(Note.OBJECTID, objectIds.toArray(new String[objectIds.size()]));
		delete(frontiaQuery, handler);
	}
	
	private void delete(FrontiaQuery query, Handler handler){
		
		mFrontiaStorage.deleteData(query, new DataOperationListener() {
			
			@Override
			public void onSuccess(long count) {
				LogUtils.d(TAG, "deleteData.onSuccess.count:" + count);
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				LogUtils.d(TAG, "deleteData.onFailure.Error_code:" + arg0 + ",Error_msg:" + arg1);
			}
		});
	}
	
	public void update(FrontiaQuery query, FrontiaData data, final Handler handler){
		mFrontiaStorage.updateData(query, data, new DataOperationListener() {
			@Override
			public void onSuccess(long count) {
				LogUtils.d(TAG, "updateData.onSuccess.count:" + count);
				if (handler == null) {
					return;
				}
				Message message = new Message();
				message.what = EditNoteActivity.MSG_UPDATE_OVER;
				message.arg1 = 0;
				message.setTarget(handler);
				message.sendToTarget();
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				LogUtils.d(TAG, "updateData.onFailure.Error_code:" + arg0 + ",Error_msg:" + arg1);
				if (handler == null) {
					return;
				}
				Message message = new Message();
				message.what = EditNoteActivity.MSG_UPDATE_OVER;
				message.arg1 = -1;
				message.setTarget(handler);
				message.sendToTarget();
			}
		});
	}
	
}
