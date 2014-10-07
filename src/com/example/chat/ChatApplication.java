package com.example.chat;

import android.app.Application;

/**
 * 应用程序入口
 * @author huanghui1
 *
 */
public class ChatApplication extends Application {
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
}
