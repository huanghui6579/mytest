package com.example.chat.service;

import java.util.List;

import org.jivesoftware.smack.AbstractXMPPConnection;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import com.example.chat.fragment.ContactFragment.LoadDataBroadcastReceiver;
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
	public static final String FLAG_SYNC = "flag_sync";
	/**
	 * 同步更新所有好友到本地数据库的标识
	 */
	public static final int FLAG_SYNC_FRENDS = 1;
	
	private MainBinder mBinder = new MainBinder();
	
	private UserManager userManager = UserManager.getInstance();
	
	private Handler mHandler = null;
	
	private HandlerThread mHandlerThread = null;
	
	/**
	 * 初始化当前用户的个人信息
	 */
	public void initCurrentUser(final Personal person) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Personal localPerson = userManager.getLocalSelftInfo(person);
				if (localPerson == null) {	//本地没有个人信息，则从服务器上同步
					//从网上同步个人信息
					AbstractXMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
					XmppUtil.syncPersonalInfo(connection, person);
					userManager.saveOrUpdateCurrentUser(person);
				} else {	//本地有个人信息，则只需改变状态就行了
					userManager.updatePersonStatus(person);
				}
			}
		}).start();
	}
	
	@Override
	public void onCreate() {
		if (mHandlerThread == null) {
			mHandlerThread = new HandlerThread(this.getClass().getCanonicalName());
			mHandlerThread.start();
			mHandler = new Handler(mHandlerThread.getLooper());
		}
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			int flag = intent.getIntExtra(FLAG_SYNC, 0);
			switch (flag) {
			case FLAG_SYNC_FRENDS:	//从服务器上同步所有的好友列表到本地
//				new Thread(new SyncFriendsTask()).start();
				mHandler.post(new SyncFriendsTask());
				break;

			default:
				break;
			}
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
			AbstractXMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
			//1、先从服务器上获取所有的好友列表
			List<User> users = XmppUtil.getFriends(connection);
			//2、更新本地数据库
			userManager.updateFriends(users);
			
			Intent intent = new Intent(LoadDataBroadcastReceiver.ACTION_USER_LIST);
			sendBroadcast(intent);
			
			//3、更新好友的头像等基本信息
			users = XmppUtil.syncFriendsVcard(connection, users);
			userManager.updateFriends(users);
			intent = new Intent(LoadDataBroadcastReceiver.ACTION_USER_INFOS);
			sendBroadcast(intent);
		}
		
	}
	
	public class MainBinder extends Binder {
		public CoreService getService() {
			return CoreService.this;
		}
	}

}
