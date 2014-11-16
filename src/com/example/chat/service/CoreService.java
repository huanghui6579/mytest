package com.example.chat.service;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;

import com.example.chat.ChatApplication;
import com.example.chat.R;
import com.example.chat.fragment.ContactFragment.LoadDataBroadcastReceiver;
import com.example.chat.manage.MsgManager;
import com.example.chat.manage.UserManager;
import com.example.chat.model.HeadIcon;
import com.example.chat.model.MsgInfo;
import com.example.chat.model.MsgSenderInfo;
import com.example.chat.model.MsgThread;
import com.example.chat.model.NewFriendInfo;
import com.example.chat.model.UserVcard;
import com.example.chat.model.MsgInfo.SendState;
import com.example.chat.model.NewFriendInfo.FriendStatus;
import com.example.chat.model.Personal;
import com.example.chat.model.User;
import com.example.chat.util.Constants;
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
	private static PacketListener mChatPacketListener;
	private static ChatManager mChatManager;
	
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
	
	/**
	 * 发送消息的线程
	 * @author Administrator
	 * @update 2014年11月16日 下午5:35:14
	 * @param msgInfo
	 */
	public void sendChatMsg(MsgSenderInfo senderInfo) {
		
	}
	
	class SendMsgTask implements Runnable {
		private MsgSenderInfo senderInfo;

		public SendMsgTask(MsgSenderInfo senderInfo) {
			this.senderInfo = senderInfo;
		}

		@Override
		public void run() {
			try {
				senderInfo.msgInfo = msgManager.addMsgInfo(senderInfo.msgInfo);
				senderInfo.msgThread.setSnippetId(senderInfo.msgInfo.getId());
				senderInfo.msgThread.setSnippetContent(senderInfo.msgInfo.getContent());
				senderInfo.msgThread.setModifyDate(System.currentTimeMillis());
				senderInfo.msgThread = msgManager.updateMsgThread(senderInfo.msgThread);
				if (senderInfo.msgInfo != null) {
					if (senderInfo.chat == null) {
						senderInfo.chat = createChat(connection);
					}
					if (senderInfo.chat != null) {
						senderInfo.chat.sendMessage(senderInfo.msgInfo.getContent());
						senderInfo.msgInfo.setSendState(SendState.SUCCESS);
					} else {
						senderInfo.msgInfo.setSendState(SendState.FAILED);
					}
				} else {
					return;
				}
			} catch (NotConnectedException | XMPPException e) {
				senderInfo.msgInfo.setSendState(SendState.FAILED);
				e.printStackTrace();
			}
			senderInfo.msgInfo = msgManager.updateMsgInfo(senderInfo.msgInfo);
			mHandler.sendEmptyMessage(Constants.MSG_MODIFY_CHAT_MSG_SEND_STATE);
		}
		
	}
	
	@Override
	public void onCreate() {
		if (mHandlerThread == null) {
			mHandlerThread = new HandlerThread(this.getClass().getCanonicalName());
			mHandlerThread.start();
			mHandler = new MyHandler(mHandlerThread.getLooper());
		}
		PacketFilter packetFilter = new OrFilter(new PacketTypeFilter(IQ.class), new PacketTypeFilter(Presence.class));
		if (mChatPacketListener == null) {
			mChatPacketListener = new ChatPacketListener();
			AbstractXMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
			connection.addPacketListener(mChatPacketListener, packetFilter);
		}
		SystemUtil.getCachedThreadPool().execute(new ReceiveMessageTask());
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
	
	@Override
	public void onDestroy() {
		AbstractXMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
		connection.removeConnectionListener(new ChatConnectionListener());
		super.onDestroy();
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
			connection.addConnectionListener(new ChatConnectionListener());
//			packetCollector.nextResult();
			if (connection.isAuthenticated()) {	//是否登录
				initChatManager(connection);
			} else {	//重新登录
				
			}
		}
		
	}
	
	/**
	 * 初始化ChatManager
	 * @author Administrator
	 * @update 2014年11月16日 下午5:41:28
	 */
	private void initChatManager(AbstractXMPPConnection connection) {
		if (mChatManager == null) {
			synchronized (CoreService.class) {
				mChatManager = ChatManager.getInstanceFor(connection);
				mChatManager.addChatListener(new MyChatManagerListener());
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
				msgInfo = msgManager.addMsgInfo(msgInfo);
				int threadId = msgInfo.getThreadID();
				MsgThread msgThread = msgManager.getThreadById(threadId);
				if (msgThread != null) {
					msgThread.setModifyDate(System.currentTimeMillis());
					msgThread.setSnippetId(msgInfo.getId());
					msgThread.setSnippetContent(msgInfo.getContent());
					msgManager.updateMsgThread(msgThread);
				}
				if (msgInfo != null) {
					android.os.Message msg = mHandler.obtainMessage();
					msg.obj = msgInfo;
					msg.what = Constants.MSG_RECEIVE_CHAT_MSG;
					mHandler.sendMessage(msg);
				}
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
			String from = SystemUtil.unwrapJid(message.getFrom());
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setComming(true);
			msgInfo.setContent(message.getBody());
			msgInfo.setCreationDate(System.currentTimeMillis());
			msgInfo.setFromUser(from);
			msgInfo.setMsgType(com.example.chat.model.MsgInfo.Type.TEXT);
			msgInfo.setRead(false);
			msgInfo.setSubject(message.getSubject());
			msgInfo.setSendState(null);
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
	
	/**
	 * 接收消息的监听器
	 * @author huanghui1
	 * @update 2014年11月10日 下午6:10:15
	 */
	public class ChatPacketListener implements PacketListener {

		@Override
		public void processPacket(Packet packet) throws NotConnectedException {
			//TODO 
			if (packet instanceof Presence) {
				Presence presence = (Presence) packet;
				SystemUtil.getCachedThreadPool().execute(new HandlePresenceTask(presence));
			}
		}
		
	}
	
	/**
	 * 聊天消息监听器
	 * @author huanghui1
	 * @update 2014年11月10日 下午6:16:07
	 */
	public class MyChatManagerListener implements ChatManagerListener {

		@Override
		public void chatCreated(Chat chat, boolean createdLocally) {
			if (!createdLocally) {
				chat.addMessageListener(new MessageListener() {
					
					@Override
					public void processMessage(Chat chat, Message message) {
						SystemUtil.getCachedThreadPool().execute(new ProcessMsgTask(chat, message));
					}
				});
			}
		}
		
	}
	
	/**
	 * 处理添加好友请求等任务
	 * @author huanghui1
	 * @update 2014年11月10日 下午8:57:51
	 */
	class HandlePresenceTask implements Runnable {
		private Presence presence;
		
		private AbstractXMPPConnection connection = XmppConnectionManager.getInstance().getConnection();

		public HandlePresenceTask(Presence presence) {
			super();
			this.presence = presence;
		}

		@Override
		public void run() {
			Collection<PacketExtension> extensions = presence.getExtensions();
			boolean isEmpty = SystemUtil.isEmpty(extensions);
			Presence.Type type = presence.getType();
			/*
			 *  •	available: 表示处于在线状态
				•	unavailable: 表示处于离线状态
				•	subscribe: 表示发出添加好友的申请
				•	unsubscribe: 表示发出删除好友的申请
				•	unsubscribed: 表示拒绝添加对方为好友
				•	error: 表示presence信息报中包含了一个错误消息。

			 */
			switch (type) {
			case subscribe:	//添加好友的申请
				NewFriendInfo newInfo = new NewFriendInfo();
				String from = SystemUtil.unwrapJid(presence.getFrom());
				String to = SystemUtil.unwrapJid(presence.getTo());
				newInfo.setFriendStatus(FriendStatus.ACCEPT);
				newInfo.setFrom(from);
				newInfo.setTo(to);
				newInfo.setTitle(from);
				newInfo.setContent(getString(R.string.contact_friend_add_request));
				newInfo.setCreationDate(System.currentTimeMillis());
				if (!isEmpty) {
					String hash = null;
					for (PacketExtension packetExtension : extensions) {
						if (packetExtension instanceof DefaultPacketExtension) {
							DefaultPacketExtension defaultPacketExtension = (DefaultPacketExtension) packetExtension;
							Collection<String> names = defaultPacketExtension.getNames();
							if (names.contains("hash")) {
								hash = defaultPacketExtension.getValue("hash");
							}
							
						}
					}
					if (!TextUtils.isEmpty(hash)) {	//有图像
						//根据对方用户账号查询本地的图片
						File icon = SystemUtil.generateIconFile(from);
						String savePath = icon.getAbsolutePath();
						boolean needSave = false;	//是否需要保存图像
						if (icon.exists()) {	//文件已经存在，则判断hash，看是否需要更新图像
							String oldHash = SystemUtil.getFileHash(icon);
							if (!oldHash.equals(hash)) {	//需要更新图像
								needSave = true;
							}
						} else {	//图像不存在，则存储到储存卡里
							needSave = true;
							savePath = null;
						}
						if (needSave) {
							HeadIcon headIcon = XmppUtil.downloadUserIcon(connection, from);
							if (headIcon != null) {	//图像获取成功
								savePath = headIcon.getFilePath();
								hash = headIcon.getHash();
							} else {
								savePath = null;
								hash = null;
							}
						}
						newInfo.setIconHash(hash);
						newInfo.setIconPath(savePath);
					}
				}
				//查看本地是否有该好友，对方此时没有好友我
				User user = userManager.getUserByUsername(from);
				if (user != null) {	//如果存在本地好友
					UserVcard uCrad = user.getUserVcard();
					String fIconHash = newInfo.getIconHash();
					String fIconPath = newInfo.getIconPath();
					String uIconHash = null;
					boolean neddUpdate = false;
					if (uCrad != null) {	//本地好友有名片信息
						uIconHash = uCrad.getIconHash();
						if (!TextUtils.isEmpty(fIconPath)) {	//对方有头像信息
							if (TextUtils.isEmpty(uIconHash) || !fIconPath.equals(uIconHash)) {	//此时本地好友没有头像信息，或者头像没有更细下来
								//本地好友有头像信息，但需要对比一下头像是否已经改变，如果改变，则需要更新
								uIconHash = fIconHash;
								uCrad.setIconHash(uIconHash);
								uCrad.setIconHash(fIconPath);
								neddUpdate = true;
							}
						} else {	//对方没有头像
							if (!TextUtils.isEmpty(uIconHash)) {	//但本地有头像，更新本地头像
								String uIconPath = uCrad.getIconPath();
								uIconHash = null;
								uCrad.setIconHash(null);
								uCrad.setIconPath(null);
								//删除本地图像
								SystemUtil.deleteFile(uIconPath);
								neddUpdate = true;
							}
						}
					} else {	//本地好友没有名片，则新建名片
						if (!TextUtils.isEmpty(fIconHash)) {	//对方有图像
							uCrad = new UserVcard();
							uCrad.setIconHash(fIconHash);
							uCrad.setIconPath(fIconPath);
							user.setUserVcard(uCrad);
							neddUpdate = true;
						}
					}
					if (neddUpdate) {
						user = userManager.updateSimpleUser(user);
					}
					newInfo.setUser(user);
					newInfo.setContent(user.getName());
				}
				//如果本地有该好友，则看要不要更新头像
				newInfo = userManager.saveOrUpdateNewFriendInfo(newInfo);
				break;

			default:
				break;
			}
		}
		
	}
	
	public class MainBinder extends Binder {
		public CoreService getService() {
			return CoreService.this;
		}
	}
	
}
