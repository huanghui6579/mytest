package com.example.chat.activity;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NotConnectedException;

import com.example.chat.ChatApplication;
import com.example.chat.R;
import com.example.chat.model.SystemConfig;
import com.example.chat.util.Constants;
import com.example.chat.util.XmppConnectionManager;

import android.app.Activity;
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
	
	protected SystemConfig systemConfig;
	
	protected AbstractXMPPConnection connection;

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
		
		systemConfig = getSystemConfig();
		
		setContentView(getContentView());
		
		initView();
		
		addListener();
		
		initData();
	}
	
	/**
	 * 配置系统的一些信息
	 * @author Administrator
	 * @update 2014年10月7日 下午4:31:11
	 * @return
	 */
	protected SystemConfig getSystemConfig() {
		if (systemConfig == null) {
			systemConfig = new SystemConfig();
		}
		systemConfig.setAccount(preferences.getString(Constants.LOGIN_ACCOUNT, null));
		systemConfig.setPassword(preferences.getString(Constants.LOGIN_PASSWORD, null));
		systemConfig.setHost(preferences.getString(Constants.SERVER_HOST, getString(R.string.server_host)));
		systemConfig.setPort(preferences.getInt(Constants.SERVER_PORT, getResources().getInteger(R.integer.server_port)));
		systemConfig.setServerName(preferences.getString(Constants.SERVER_NAME, getString(R.string.server_name)));
		systemConfig.setFirstLogin(preferences.getBoolean(Constants.LOGIN_ISFIRST, true));
		return systemConfig;
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
	
	/**
	 * 获得连接
	 * @author Administrator
	 * @update 2014年10月7日 下午4:59:54
	 * @return
	 */
	protected AbstractXMPPConnection getConnection() {
		if (connection == null) {
			AbstractXMPPConnection temp = XmppConnectionManager.getInstance().getConnection();
			if(temp == null) {
				connection = XmppConnectionManager.getInstance().init(systemConfig);
			} else {
				connection = temp;
			}
		}
		return connection;
	}
	
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
	
	@Override
	protected void onDestroy() {
		application.removeActivity(this);
		super.onDestroy();
	}
}
