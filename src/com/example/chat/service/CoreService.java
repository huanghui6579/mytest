package com.example.chat.service;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.example.chat.manage.UserManager;
import com.example.chat.model.Personal;
import com.example.chat.model.User;
import com.example.chat.util.XmppConnectionManager;
import com.example.chat.util.XmppUtil;

/**
 * 核心的service服务，主要用来同步联系人数据
 * @author coolpad
 *
 */
public class CoreService extends Service {
	/**
	 * 同步更新所有好友到本地数据库的标识
	 */
	public static final int FLAG_SYNC_FRENDS = 1;
	
	private MainBinder mBinder = new MainBinder();
	
	private UserManager userManager = UserManager.getInstance();
	
	private Handler mHandler = new Handler();
	
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
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		switch (flags) {
		case FLAG_SYNC_FRENDS:	//从服务器上同步所有的好友列表到本地
			mHandler.post(new SyncFriendsTask());
			break;

		default:
			break;
		}
		return Service.START_REDELIVER_INTENT;
	}
	
	/**
	 * 同步所有好友列表的任务线程
	 * @author coolpad
	 *
	 */
	class SyncFriendsTask implements Runnable {

		@Override
		public void run() {
			//1、先从服务器上获取所有的好友列表
			List<User> users = XmppUtil.getFriends(XmppConnectionManager.getInstance().getConnection());
			//2、更新本地数据库
			userManager.updateFriends(users);
		}
		
	}
	
	public class MainBinder extends Binder {
		public CoreService getService() {
			return CoreService.this;
		}
	}

}
