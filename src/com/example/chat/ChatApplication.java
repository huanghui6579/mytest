package com.example.chat;

import java.util.LinkedList;

import com.example.chat.model.Personal;
import com.example.chat.model.SystemConfig;
import com.example.chat.util.Constants;
import com.example.chat.util.SystemUtil;
import com.example.chat.util.XmppConnectionManager;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 应用程序入口
 * @author huanghui1
 *
 */
public class ChatApplication extends Application {
	private LinkedList<Activity> activities = new LinkedList<>();
	
	private static SystemConfig systemConfig;
	
	private static ChatApplication instance;
	
	private SharedPreferences preferences;
	
	private static Personal currentUser = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		
		preferences = getSharedPreferences(Constants.SETTTING_LOGIN, Context.MODE_PRIVATE);
		systemConfig = new SystemConfig();
		currentUser = new Personal();
		
		initSystemConfig();
	}
	
	/**
	 * 初始化系统配置
	 * @update 2014年10月9日 上午8:17:20
	 */
	private void initSystemConfig() {
		systemConfig.setAccount(preferences.getString(Constants.USER_ACCOUNT, null));
		systemConfig.setPassword(preferences.getString(Constants.USER_PASSWORD, null));
		systemConfig.setFirstLogin(preferences.getBoolean(Constants.USER_ISFIRST, true));
		systemConfig.setResource(preferences.getString(Constants.USER_RESOURCE, SystemUtil.getPhoneModel()));
		systemConfig.setHost(preferences.getString(Constants.NAME_SERVER_HOST, Constants.SERVER_HOST));
		systemConfig.setPort(preferences.getInt(Constants.NAME_SERVER_PORT, Constants.SERVER_PORT));
		systemConfig.setServerName(preferences.getString(Constants.NAME_SERVER_NAME, Constants.SERVER_NAME));
	}
	
	/**
	 * 保存系统配置信息
	 * @update 2014年10月9日 上午8:22:30
	 * @param config
	 */
	public void saveSystemConfig() {
		Editor editor = preferences.edit();
		editor.putString(Constants.USER_ACCOUNT, systemConfig.getAccount());
		editor.putString(Constants.USER_PASSWORD, systemConfig.getPassword());
		editor.putBoolean(Constants.USER_ISFIRST, systemConfig.isFirstLogin());
		editor.putString(Constants.USER_RESOURCE, systemConfig.getResource());
		editor.putString(Constants.NAME_SERVER_HOST, systemConfig.getHost());
		editor.putString(Constants.NAME_SERVER_NAME, systemConfig.getServerName());
		editor.putInt(Constants.NAME_SERVER_PORT, systemConfig.getPort());
		editor.commit();
	}
	
	/**
	 * systemconfig
	 * @update 2014年10月9日 上午8:17:49
	 * @return
	 */
	public SystemConfig getSystemConfig() {
		return systemConfig;
	}
	
	public Personal getCurrentUser() {
		return currentUser;
	}
	
	/**
	 * 获得全局的application
	 * @return 全局的application
	 */
	public static ChatApplication getInstance() {
		return instance;
	}
	
	/**
	 * 添加Activity的队列中，用于软件的退出
	 * @update 2014年10月8日 下午10:22:30
	 * @param activity
	 */
	public void addActivity(Activity activity) {
		activities.add(activity);
	}
	
	/**
	 * 退出应用应用程序
	 * @update 2014年10月8日 下午10:30:04
	 */
	public void exit() {
		XmppConnectionManager.getInstance().disconnect();
		for(Activity activity : activities) {
			activity.finish();
		}
		System.exit(0);
	}
	
	/**
	 * 将Activity从队列中移除
	 * @update 2014年10月8日 下午10:23:15
	 * @param activity
	 */
	public void removeActivity(Activity activity) {
		activities.remove(activity);
	}
}
