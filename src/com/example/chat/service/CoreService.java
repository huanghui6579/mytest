package com.example.chat.service;

import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;

import com.example.chat.ChatApplication;
import com.example.chat.fragment.ContactFragment.LoadDataBroadcastReceiver;
import com.example.chat.manage.MsgManager;
import com.example.chat.manage.UserManager;
import com.example.chat.model.MsgInfo;
import com.example.chat.model.Personal;
import com.example.chat.model.User;
import com.example.chat.util.Constants;
import com.example.chat.util.Log;
import com.example.chat.util.SystemUtil;
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
	private MsgManager msgManager = MsgManager.getInstance();
	
	private MyHandler mHandler = null;
	
	private HandlerThread mHandlerThread = null;
	
	private class MyHandler extends Handler {
		
		public MyHandler() {
			super();
		}
		
		public MyHandler(Callback callback) {
			super(callback);
		}

		public MyHandler(Looper looper, Callback callback) {
			super(looper, callback);
		}

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Constants.MSG_RECEIVE_CHAT_MSG:	//接收聊天消息
				MsgInfo msgInfo = (MsgInfo) msg.obj;
				//发送广播到对应的界面处理
				break;

			default:
				break;
			}
		}
	}
	
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
			mHandler = new MyHandler(mHandlerThread.getLooper());
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
			SystemUtil.getCachedThreadPool().execute(new ReceiveMessageTask());
			//监听消息
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
	 * 接收openfie的消息
	 * @author huanghui1
	 * @update 2014年11月1日 下午5:24:55
	 */
	class ReceiveMessageTask implements Runnable {

		@Override
		public void run() {
			AbstractXMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
			if (connection.isAuthenticated()) {	//是否登录
				ChatManager chatManager = ChatManager.getInstanceFor(connection);
				chatManager.addChatListener(new ChatManagerListener() {
					
					@Override
					public void chatCreated(Chat chat, boolean createdLocally) {
						if (!createdLocally) {
							chat.addMessageListener(new MessageListener() {
								
								@Override
								public void processMessage(Chat chat, Message message) {
									
								}
							});
						}
					}
				});
			} else {	//重新登录
				
			}
		}
		
	}
	
	/**
	 * 处理消息的后台线程，主要是将消息存入数据库
	 * @author huanghui1
	 * @update 2014年11月3日 下午10:40:19
	 */
	class ProcessMsgTask implements Runnable {
		Chat chat;
		Message message;

		public ProcessMsgTask(Chat chat, Message message) {
			super();
			this.chat = chat;
			this.message = message;
		}

		@Override
		public void run() {
			MsgInfo msgInfo = processMsg(message);
			if (msgInfo != null) {
				android.os.Message msg = mHandler.obtainMessage();
				msg.obj = msgInfo;
				msg.what = Constants.MSG_RECEIVE_CHAT_MSG;
				mHandler.sendMessage(msg);
			}
		}
		
	}
	
	/**
	 * 处理聊天消息
	 * @update 2014年11月1日 下午5:46:44
	 * @param message
	 */
	private MsgInfo processMsg(Message message) {
		if (Type.chat == message.getType()) {	//聊天信息
			String from = message.getFrom().split("@")[0];
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setComming(true);
			msgInfo.setContent(message.getBody());
			msgInfo.setCreationDate(System.currentTimeMillis());
			msgInfo.setFromUser(from);
			msgInfo.setMsgType(com.example.chat.model.MsgInfo.Type.TEXT);
			msgInfo.setRead(false);
			msgInfo.setSubject(message.getSubject());
			msgInfo.setToUser(ChatApplication.getInstance().getCurrentAccount());
			int threadId = msgManager.getThreadIdByMembers(from);	//查找本地会话，如果没有就创建
			if (threadId > 0) {
				msgInfo.setThreadID(threadId);
			}
			return msgInfo;
		}
		return null;
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
