package com.example.chat;

import java.util.LinkedList;

import com.example.chat.util.XmppConnectionManager;

import android.app.Activity;
import android.app.Application;

/**
 * 应用程序入口
 * @author huanghui1
 *
 */
public class ChatApplication extends Application {
	private LinkedList<Activity> activities = new LinkedList<>();
	
	private static ChatApplication instance;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
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
