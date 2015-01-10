package com.example.chat.activity;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.example.chat.ChatApplication;
import com.example.chat.util.Constants;

/**
 * 所有Activity的父类
 * @author huanghui1
 *
 */
public abstract class BaseActivity extends FragmentActivity {
	protected Context mContext;
	protected SharedPreferences preferences;
	protected ChatApplication application;
	
	/**
	 * ActionBar是否允许有返回按钮
	 */
	private boolean homeAsUpEnabled = true;
	
	protected static String TAG = null; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = this;
		
		TAG = this.getClass().getCanonicalName();
		
		application = ChatApplication.getInstance();
		
		homeAsUpEnabled = isHomeAsUpEnabled();
		
		initWidow();
		
		application.addActivity(this);
		
		preferences = getSharedPreferences(Constants.SETTTING_LOGIN, Context.MODE_PRIVATE);
		
		setContentView(getContentView());
		
		initView();
		
		addListener();
		
		initData();
	}
	
	/**
	 * 是否有允许ActionBar左上角显示返回按钮
	 * @update 2014年11月10日 下午3:48:11
	 * @return
	 */
	protected boolean isHomeAsUpEnabled() {
		return homeAsUpEnabled;
	}
	
	/**
	 * 初始化一些窗口信息
	 * @update 2014年10月10日 下午9:29:18
	 */
	protected void initWidow() {
		ActionBar actionBar = getActionBar();
		if (homeAsUpEnabled) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		} else {
			actionBar.setDisplayHomeAsUpEnabled(false);
		}
	};
	
	/**
	 * 获得界面的布局文件id
	 * @return 布局文件id
	 */
	protected abstract int getContentView();
	
	/**
	 * 初始化界面
	 */
	protected abstract void initView();
	
	/**
	 * 初始化数据
	 * @update 2015年1月5日 下午9:34:01
	 */
	protected abstract void initData();
	
	/**
	 * 为控件注册监听器
	 */
	protected abstract void addListener();
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:	//返回
			beforeBack();
			finish();
			return true;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 在按下左上角的返回按钮时，可以处理的操作，该方法在finish()前调用
	 * @update 2015年1月9日 上午9:41:44
	 */
	protected void beforeBack() {
		
	}
	
	/**
	 * 隐藏dialog
	 * @update 2014年10月10日 上午8:10:47
	 * @param pDialog
	 */
	public void hideLoadingDialog(ProgressDialog pDialog) {
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
