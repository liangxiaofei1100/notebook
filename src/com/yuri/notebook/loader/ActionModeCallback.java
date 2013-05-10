package com.yuri.notebook.loader;

import com.yuri.notebook.R;

import android.content.Context;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

/**Selection action mode call back */
public class ActionModeCallback implements Callback, OnMenuItemClickListener{
	/**
	 *  用户弹出复制，剪切等菜单的
	 */
	private PopupMenu mEditPopupMenu = null;
	/**
	 * 用户弹出全选和取消全选的菜单的
	 */
	private PopupMenu mSelectPopupMenu = null;
	private boolean mSelectedAll = true;
	/**
	 * 显示选中多少个，以及全选和取消全选
	 */
    private Button mSelectBtn = null;
    
    private Context mContext = null;
    
    public ActionModeCallback(Context context){
    	this.mContext = context;
    }

    @Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		// 最先调用的
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// 自定义ActionBar菜单
		View customView = inflater.inflate(R.layout.listview_actionbar_edit,
				null);
		mode.setCustomView(customView);
		mSelectBtn = (Button) customView.findViewById(R.id.select_button);
		mSelectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null == mSelectPopupMenu) {
					// 创建"全选/取消全选"的弹出菜单
					 mSelectPopupMenu = createSelectPopupMenu(mSelectBtn);
				} else {
					// Update
					updateSelectPopupMenu();
					mSelectPopupMenu.show();
				}
			}
		});
		MenuInflater menuInflater = mode.getMenuInflater();
		menuInflater.inflate(R.menu.actionbar_menu, menu);
		return true;
	}
    
	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private PopupMenu createSelectPopupMenu(View anchorView) {
        final PopupMenu popupMenu = new PopupMenu(mContext, anchorView);
        popupMenu.inflate(R.menu.select_popup_menu);
        popupMenu.setOnMenuItemClickListener(this);
        return popupMenu;
    }
	
	private void updateSelectPopupMenu(){
		if (mSelectPopupMenu == null) {
            mSelectPopupMenu = createSelectPopupMenu(mSelectBtn);
            return;
        }
		final Menu menu = mSelectPopupMenu.getMenu();
//		int selectedCount = getListView().getCheckedItemCount();
		if (mSelectedAll) {
			menu.findItem(R.id.select).setTitle(R.string.cancelall);
			mSelectedAll = true;
		}else {
			menu.findItem(R.id.select).setTitle(R.string.seleteall);
			mSelectedAll = false;
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

}
