package com.example.chat.activity;

import com.example.chat.ChatApplication;
import com.example.chat.util.Constants;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

/**
 * 所有Activity的父类
 * @author huanghui1
 *
 */
public abstract class BaseActivity extends FragmentActivity {
	protected Context mContext;
	protected ProgressDialog pDialog;
	protected SharedPreferences preferences;
	protected ChatApplication application;
	
	protected static String TAG = null; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = this;
		
		TAG = this.getClass().getCanonicalName();
		
		application = ChatApplication.getInstance();
		
		application.addActivity(this);
		
		pDialog = new ProgressDialog(mContext);
		
		preferences = getSharedPreferences(Constants.SETTTING_LOGIN, Context.MODE_PRIVATE);
		
		setContentView(getContentView());
		
		initView();
		
		addListener();
		
		initData();
	}
	
	/**
	 * 获得界面的布局文件id
	 * @return 布局文件id
	 */
	protected abstract int getContentView();
	
	/**
	 * 初始化界面
	 */
	protected abstract void initView();
	
	protected abstract void initData();
	
	/**
	 * 为控件注册监听器
	 */
	protected abstract void addListener();
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:	//返回
			finish();
			return true;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 显示加载对话框
	 * @update 2014年10月9日 下午9:39:57
	 * @param title
	 * @param message
	 */
	public void showLoadingDialog(String title, String message) {
		pDialog.setTitle(title);
		pDialog.setMessage(message);
		pDialog.show();
	}
	
	/**
	 * 显示加载对话框
	 * @update 2014年10月9日 下午9:39:57
	 * @param titleResId
	 * @param message
	 */
	public void showLoadingDialog(int titleResId, String message) {
		pDialog.setTitle(titleResId);
		pDialog.setMessage(message);
		pDialog.show();
	}
	
	public void hideLoadingDialog() {
		if(pDialog != null && pDialog.isShowing()) {
			pDialog.dismiss();
		}
	}
	
	@Override
	protected void onDestroy() {
		application.removeActivity(this);
		super.onDestroy();
	}
}
