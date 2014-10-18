package com.example.chat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.example.chat.manage.UserManager;
import com.example.chat.model.Personal;

/**
 * 核心的service服务，主要用来同步联系人数据
 * @author coolpad
 *
 */
public class CoreService extends Service {
	private MainBinder mBinder = new MainBinder();
	
	private UserManager userManager = UserManager.getInstance();
	
	/**
	 * 初始化当前用户的个人信息
	 */
	public void initCurrentUser(final Personal person) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				userManager.initCurrentUser(person);
			}
		}).start();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	public class MainBinder extends Binder {
		public CoreService getService() {
			return CoreService.this;
		}
	}

}
