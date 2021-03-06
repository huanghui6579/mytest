package net.ibaixin.chat.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import net.ibaixin.chat.ChatApplication;
import net.ibaixin.chat.R;
import net.ibaixin.chat.activity.ChatActivity;
import net.ibaixin.chat.activity.MainActivity;
import net.ibaixin.chat.fragment.ContactFragment.LoadDataBroadcastReceiver;
import net.ibaixin.chat.listener.ChatRostListener;
import net.ibaixin.chat.manage.MsgManager;
import net.ibaixin.chat.manage.UserManager;
import net.ibaixin.chat.model.MsgInfo;
import net.ibaixin.chat.model.MsgInfo.SendState;
import net.ibaixin.chat.model.MsgPart;
import net.ibaixin.chat.model.MsgSenderInfo;
import net.ibaixin.chat.model.MsgThread;
import net.ibaixin.chat.model.Personal;
import net.ibaixin.chat.model.User;
import net.ibaixin.chat.util.Constants;
import net.ibaixin.chat.util.Log;
import net.ibaixin.chat.util.MimeUtils;
import net.ibaixin.chat.util.SystemUtil;
import net.ibaixin.chat.util.XmppConnectionManager;
import net.ibaixin.chat.util.XmppUtil;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ChatMessageListener;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jxmpp.util.XmppStringUtils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;

/**
 * 核心的service服务，主要用来同步联系人数据
 * @author coolpad
 */
public class CoreService extends Service {
	public static final String FLAG_SYNC = "flag_sync";
	public static final String FLAG_RECEIVE_OFFINE_MSG = "flag_receive_offine_msg";
	
	/**
	 * 聊天新消息的通知id
	 */
	public static final int NOTIFY_ID_CHAT_MSG = 100;
	
	/**
	 * 同步更新所有好友到本地数据库的标识
	 */
	public static final int FLAG_SYNC_FRENDS = 1;
	/**
	 * 接收离线消息
	 */
	public static final int FLAG_RECEIVE_OFFINE = 2;
	
	private IBinder mBinder = new MainBinder();
	
	private UserManager userManager = UserManager.getInstance();
	private MsgManager msgManager = MsgManager.getInstance();
	
	private MyHandler mHandler = null;
	
	private HandlerThread mHandlerThread = null;
	private static RosterListener mRosterListener;
	private static FileTransferListener mFileTransferListener;
	private static ChatManagerListener mChatManagerListener;
	private static MyChatMessageListener mChatMessageListener;
	private static ChatManager mChatManager;
	private static OfflineMessageManager mOfflineMessageManager;
	private static FileTransferManager mFileTransferManager;
	AbstractXMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
	
	private ImageLoader mImageLoader = ImageLoader.getInstance();
	
	private ActivityManager mActivityManager;
	
	private Context mContext;
	
	private NotificationManager mNotificationManager;
	
//	SendChatMessageReceiver chatMessageReceiver;
	
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
				String contentTitle = null;
				String contentText = null;
				int msgCount = msg.arg1;
				Intent resultIntent = new Intent(mContext, ChatActivity.class);
				if (msgInfo != null) {
					resultIntent.putExtra(ChatActivity.ARG_THREAD_ID, msgInfo.getThreadID());
					contentTitle = msgInfo.getFromUser();
					contentText = msgInfo.getContent();
				} else {
					contentTitle = getString(R.string.notification_batch_promtp_title);
					contentText = getString(R.string.notification_batch_promtp_content, msgCount);
					resultIntent.setClass(mContext, MainActivity.class);
					resultIntent.putExtra(MainActivity.ARG_SYNC_FRIENDS, false);
					resultIntent.putExtra(MainActivity.ARG_INIT_POSITION, true);
				}
				// 100 毫秒延迟后，震动 200 毫秒，暂停 100 毫秒后，再震动 300 毫秒
//				long[] vibrate = {100,200,100,300};
				//发送广播到对应的界面处理
				NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplication());
				builder.setSmallIcon(R.drawable.ic_launcher)
						.setAutoCancel(true)
						.setShowWhen(true)
						.setDefaults(Notification.DEFAULT_ALL)
						.setAutoCancel(true)
						.setTicker(getString(R.string.notification_new_msg_title, msgCount))
						.setContentTitle(contentTitle)
						.setContentText(contentText);

//				TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
//				stackBuilder.addParentStack(MainActivity.class);
//				stackBuilder.addNextIntent(resultIntent);
				PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				builder.setContentIntent(resultPendingIntent);
				if (mNotificationManager == null) {
					mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);;
				}
				// mId allows you to update the notification later on.
				mNotificationManager.notify(NOTIFY_ID_CHAT_MSG, builder.build());
				break;

			default:
				break;
			}
		}
	}
	
	
	@Override
	public void onCreate() {
		mContext = this;
		if (mHandlerThread == null) {
			mHandlerThread = new HandlerThread(this.getClass().getCanonicalName());
			mHandlerThread.start();
			mHandler = new MyHandler(mHandlerThread.getLooper());
		}
		if (connection != null) {
			if (mRosterListener == null) {
				mRosterListener = new ChatRostListener();
				connection.getRoster().addRosterListener(mRosterListener);
				ChatRostListener.hasRosterListener = true;
			}
			
			if (mFileTransferListener == null) {
				mFileTransferManager = FileTransferManager.getInstanceFor(connection);
				mFileTransferListener = new MyFileTransferListener();
				mFileTransferManager.addFileTransferListener(mFileTransferListener);
			}
			
			if (mChatMessageListener == null) {
				mChatMessageListener = new MyChatMessageListener();
			}
			
			if (mChatManager == null) {
				mChatManager = ChatManager.getInstanceFor(connection);
				mChatManagerListener = new MyChatManagerListener();
				mChatManager.addChatListener(mChatManagerListener);
			}
			
			if (mOfflineMessageManager == null) {
				mOfflineMessageManager = new OfflineMessageManager(connection);
			}
		}
		
		
//		SystemUtil.getCachedThreadPool().execute(new ReceiveMessageTask());
		
		//注册发送消息的广播
//		chatMessageReceiver = new SendChatMessageReceiver();
//		IntentFilter intentFilter = new IntentFilter(SendChatMessageReceiver.ACTION_SEND_CHAT_MSG);
//		registerReceiver(chatMessageReceiver, intentFilter);
		
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
			int syncFlag = intent.getIntExtra(FLAG_SYNC, 0);
			int offineFlag = intent.getIntExtra(FLAG_RECEIVE_OFFINE_MSG, 0);
			//同步好友列表
			switch (syncFlag) {
			case FLAG_SYNC_FRENDS:	//从服务器上同步所有的好友列表到本地
//				new Thread(new SyncFriendsTask()).start();
				mHandler.post(new SyncFriendsTask());
				break;

			default:
				break;
			}
			if (offineFlag == FLAG_RECEIVE_OFFINE) {	//登录成功后接受离线消息
				SystemUtil.getCachedThreadPool().execute(new HandleOffineMsgTask());
			}
		}
		
		return Service.START_REDELIVER_INTENT;
	}
	
	@Override
	public void onDestroy() {
//		if (mConnectionListener != null) {
//			connection.removeConnectionListener(mConnectionListener);
//		}
		super.onDestroy();
	}
	
	/**
	 * 初始化当前用户的个人信息
	 */
	public void initCurrentUser(final Personal person) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (!person.isEmpty()) {
					Personal localPerson = userManager.getLocalSelfInfo(person);
					if (localPerson == null) {	//本地没有个人信息，则从服务器上同步
						//从网上同步个人信息
						XmppUtil.syncPersonalInfo(connection, person);
						userManager.saveOrUpdateCurrentUser(person);
					} else {	//本地有个人信息，则只需改变状态就行了
						userManager.updatePersonStatus(person);
					}
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
		SystemUtil.getCachedThreadPool().execute(new SendMsgTask(senderInfo));
	}
	
	/**
	 * 发送消息的任务线程
	 * @author huanghui1
	 * @update 2014年11月17日 上午9:05:04
	 */
	class SendMsgTask implements Runnable {
		private MsgSenderInfo senderInfo;

		public SendMsgTask(MsgSenderInfo senderInfo) {
			this.senderInfo = senderInfo;
		}

		@Override
		public void run() {
			MsgInfo msgInfo =  senderInfo.msgInfo;
			String fromJid = msgInfo.getFromUser();
			String toJid = msgInfo.getToUser();
			//将请完整的还原为账号
			msgInfo.setFromUser(XmppStringUtils.parseLocalpart(fromJid));
			msgInfo.setToUser(XmppStringUtils.parseLocalpart(toJid));
			MsgInfo.Type msgType = msgInfo.getMsgType();
			try {
				msgInfo = msgManager.addMsgInfo(msgInfo);
				senderInfo.msgThread.setSnippetId(msgInfo.getId());
				String snippetContent = msgManager.getSnippetContentByMsgType(msgType, msgInfo);
				senderInfo.msgThread.setSnippetContent(snippetContent);
				senderInfo.msgThread.setModifyDate(System.currentTimeMillis());
				senderInfo.msgThread = msgManager.updateMsgThread(senderInfo.msgThread);
				if (MsgInfo.Type.TEXT == msgType) {	//文本消息
					if (senderInfo.chat != null) {
						try {
							senderInfo.chat.sendMessage(senderInfo.msgInfo.getContent());
							msgInfo.setSendState(SendState.SUCCESS);
						} catch (NotConnectedException | XMPPException e) {
							msgInfo.setSendState(SendState.FAILED);
							e.printStackTrace();
						}
					} else {
						msgInfo.setSendState(SendState.FAILED);
					}
				} else {	//非文本消息，则以附件形式发送
					MsgPart msgPart = msgInfo.getMsgPart();// 创建文件传输管理器
					
//					String to = msgInfo.getToJid() + "/Spark 2.6.3";
//					String to = msgInfo.getToJid() + "/Android";
					OutgoingFileTransfer fileTransfer = mFileTransferManager.createOutgoingFileTransfer(toJid);
					
					File sendFile = null;
					if (msgType == MsgInfo.Type.IMAGE) {	//图片类型
						if (senderInfo.originalImage) {	//原图发送
							sendFile = new File(msgPart.getFilePath());
						} else {
							sendFile = DiskCacheUtils.findInCache(Scheme.FILE.wrap(msgPart.getFilePath()), mImageLoader.getDiskCache());
						}
					} else {
						sendFile = new File(msgPart.getFilePath());
					}
					if (sendFile.exists()) {
						try {
							StringBuilder description = new StringBuilder();
							if (msgType == MsgInfo.Type.LOCATION) {	//地理位置信息
								description.append(msgInfo.getContent()).append(Constants.SPLITE_TAG_MSG_TYPE).append(msgInfo.getSubject());
							} else {
								description.append(msgPart.getFileName());
							}
							description.append(Constants.SPLITE_TAG_MSG_TYPE).append(msgType.ordinal());
							fileTransfer.sendStream(new FileInputStream(sendFile), msgPart.getFileName(), sendFile.length(), description.toString());
//							fileTransfer.sendFile(sendFile, msgPart.getFileName());
							while (!fileTransfer.isDone()) {	//传输完毕
//								Log.d("-------------fileTransfer.getStatus()----------------" + fileTransfer.getStatus());
//								if (fileTransfer.getStatus() == FileTransfer.Status.error) {
//									msgInfo.setSendState(SendState.FAILED);
//									Log.d("----FileTransferManager------" + fileTransfer.getStatus() + "--" + fileTransfer.getProgress());
//								}
								switch (fileTransfer.getStatus()) {
								case complete:
									msgInfo.setSendState(SendState.SUCCESS);
									break;
								case error:
								case cancelled:
									msgInfo.setSendState(SendState.FAILED);
									break;
								default:
									break;
								}
							}
							Log.d("-------发送完毕------");
//							msgInfo.setSendState(SendState.SUCCESS);
						} catch (FileNotFoundException e) {
							msgInfo.setSendState(SendState.FAILED);
							e.printStackTrace();
						}
					}
				}//XMPPException | SmackException | 
			} catch (Exception e) {
				msgInfo.setSendState(SendState.FAILED);
				e.printStackTrace();
			}
			msgInfo = msgManager.updateMsgInfo(msgInfo);
			senderInfo.handler.sendEmptyMessage(Constants.MSG_MODIFY_CHAT_MSG_SEND_STATE);
		}
		
	}
	
	/**
	 * 接收openfie的消息
	 * @author huanghui1
	 * @update 2014年11月1日 下午5:24:55
	 */
	class ReceiveMessageTask implements Runnable {

		@Override
		public void run() {
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
		boolean notify = true;

		public ProcessMsgTask(Chat chat, Message message) {
			super();
			this.chat = chat;
			this.message = message;
		}

		public ProcessMsgTask(Chat chat, Message message, boolean notify) {
			super();
			this.chat = chat;
			this.message = message;
			this.notify = notify;
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
					Intent intent = new Intent(ChatActivity.MsgProcessReceiver.ACTION_PROCESS_MSG);
					intent.putExtra(ChatActivity.ARG_MSG_INFO, msgInfo);
					sendBroadcast(intent);
				}
				if (notify && msgInfo != null) {
					if (!isChatActivityOnTop()) {
						android.os.Message msg = mHandler.obtainMessage();
						msg.obj = msgInfo;
						msg.arg1 = 1;
						msg.what = Constants.MSG_RECEIVE_CHAT_MSG;
						mHandler.sendMessage(msg);
					}
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
			msgInfo.setMsgType(net.ibaixin.chat.model.MsgInfo.Type.TEXT);
			msgInfo.setRead(false);
			msgInfo.setSubject(message.getSubject());
			msgInfo.setSendState(SendState.SUCCESS);
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
			if (connection != null && connection.isAuthenticated()) {
				//1、先从服务器上获取所有的好友列表
				List<User> users = XmppUtil.getFriends(connection);
				
				//2、更新本地数据库
				userManager.updateFriends(users);
				
				Intent intent = new Intent(LoadDataBroadcastReceiver.ACTION_USER_LIST);
				sendBroadcast(intent);
				
				//接收离线消息
				SystemUtil.getCachedThreadPool().execute(new HandleOffineMsgTask());
				
				//同步好友的电子名片信息
				SystemUtil.getCachedThreadPool().execute(new SyncFriendVcardTask(users));
				
			}
		}
		
	}
	
	/**
	 * 同步好友的电子名片信息
	 * @author tiger
	 * @update 2015年3月1日 下午6:13:36
	 *
	 */
	class SyncFriendVcardTask implements Runnable {
		List<User> users;

		public SyncFriendVcardTask(List<User> users) {
			super();
			this.users = users;
		}

		@Override
		public void run() {
			if (SystemUtil.isNotEmpty(users)) {
				//3、更新好友的头像等基本信息
				users = XmppUtil.syncFriendsVcard(connection, users);
				userManager.updateFriends(users);
				Intent intent = new Intent(LoadDataBroadcastReceiver.ACTION_USER_INFOS);
				sendBroadcast(intent);
			}
		}
		
	}
	
	
	/**
	 * 处理离线消息的任务
	 * @author huanghui1
	 * @update 2015年2月27日 下午5:17:15
	 */
	class HandleOffineMsgTask implements Runnable {

		@Override
		public void run() {
			AbstractXMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
			if (connection != null && connection.isConnected()) {
				//1、先从服务器上获取所有的好友列表
				try {
					if (mOfflineMessageManager != null && mOfflineMessageManager.supportsFlexibleRetrieval()) {
						int msgCount = mOfflineMessageManager.getMessageCount();
						if (msgCount > 0) {	//有离线消息
							if (msgCount > 1) {	//有多条离线消息
								if (mChatMessageListener != null) {
									mChatMessageListener.setNotify(false);
								}
							}
							//获取离线消息
							List<Message> offineMessges = mOfflineMessageManager.getMessages();
							
							//保存离线消息
							if (SystemUtil.isNotEmpty(offineMessges)) {
								mOfflineMessageManager.deleteMessages();	//上报服务器已获取，需删除服务器备份，不然下次登录会重新获取
								
								if (msgCount > 1 && !isChatActivityOnTop()) {	//聊天界面不在栈顶时才发送通知
									//离线消息处理完毕后再一起通知，避免过频繁的通知
									android.os.Message msg = mHandler.obtainMessage();
									msg.arg1 = msgCount;
									msg.what = Constants.MSG_RECEIVE_CHAT_MSG;
									mHandler.sendMessage(msg);
								}
							}
						}
					}
				} catch (NoResponseException | XMPPErrorException
						| NotConnectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					//上报自己的状态为登录状态
					Presence presence = new Presence(Presence.Type.available);
					presence.setStatus("在线");
					presence.setPriority(1);
					presence.setMode(Presence.Mode.available);
					connection.sendPacket(presence);
				} catch (NotConnectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			if (mChatMessageListener != null) {
				mChatMessageListener.setNotify(true);
			}
		}
		
	}
	
	//初始化messageListener
	public void initMessageListener() {
		if (mChatMessageListener == null) {
			synchronized (CoreService.class) {
				if (mChatMessageListener == null) {
					mChatMessageListener = new MyChatMessageListener();
					if (mChatManagerListener != null) {
						mChatManager.addChatListener(mChatManagerListener);
					}
				}
			}
		}
	}
	
	/**
	 * 聊天消息管理器
	 * @author huanghui1
	 * @update 2014年11月10日 下午6:16:07
	 */
	public class MyChatManagerListener implements ChatManagerListener {

		@Override
		public void chatCreated(Chat chat, boolean createdLocally) {
			if (!createdLocally) {
				chat.addMessageListener(mChatMessageListener);
			}
		}
	}
	
	/**
	 * 消息监听器
	 * @author huanghui1
	 * @update 2014年11月20日 下午8:41:17
	 */
	public class MyChatMessageListener implements ChatMessageListener {
		/**
		 * 是否通知,默认为true
		 */
		private boolean notify = true;
		
		public void setNotify(boolean notify) {
			this.notify = notify;
		}

		@Override
		public void processMessage(Chat chat, Message message) {
			SystemUtil.getCachedThreadPool().execute(new ProcessMsgTask(chat, message, notify));
		}
		
	}
	
	/**
	 * 文件接收的监听器
	 * @author huanghui1
	 * @update 2014年11月20日 下午2:32:01
	 */
	class MyFileTransferListener implements FileTransferListener {

		@Override
		public void fileTransferRequest(FileTransferRequest request) {
			MsgInfo msgInfo = processFileMessage(request);
			msgInfo = msgManager.addMsgInfo(msgInfo);
			int threadId = msgInfo.getThreadID();
			MsgThread msgThread = msgManager.getThreadById(threadId);
			if (msgThread != null) {
				msgThread.setModifyDate(System.currentTimeMillis());
				msgThread.setSnippetId(msgInfo.getId());
				MsgInfo.Type msgType = msgInfo.getMsgType();
				String snippetContent = msgManager.getSnippetContentByMsgType(msgType, msgInfo);
				msgThread.setSnippetContent(snippetContent);
				msgManager.updateMsgThread(msgThread);
				
				Intent intent = new Intent(ChatActivity.MsgProcessReceiver.ACTION_PROCESS_MSG);
				intent.putExtra(ChatActivity.ARG_MSG_INFO, msgInfo);
				sendBroadcast(intent);
				
				SystemUtil.getCachedThreadPool().execute(new ReceiveFileTask(request, msgInfo));
			}
		}
		
	}
	
	/**
	 * 处理文件类型的消息
	 * @update 2014年11月20日 下午2:57:06
	 * @param request
	 * @return
	 */
	private MsgInfo processFileMessage(FileTransferRequest request) {
		//获得发送人的账号，不包含完整的jid
		String fromUser = SystemUtil.unwrapJid(request.getRequestor());
		MsgInfo msgInfo = new MsgInfo();
		msgInfo.setComming(true);
		String description = request.getDescription();
		String desc = description;
		int type = -1;
		if (!TextUtils.isEmpty(description)) {
			int index = description.lastIndexOf(Constants.SPLITE_TAG_MSG_TYPE);
			if (index != -1) {
				desc = description.substring(0, index);
				try {
					type = Integer.parseInt(description.substring(index + 1));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
		msgInfo.setCreationDate(System.currentTimeMillis());
		msgInfo.setFromUser(fromUser);
		String mimeType = null;
		String subJect = null;
		if (type == MsgInfo.Type.LOCATION.ordinal()) {	//地理位置的消息
			msgInfo.setMsgType(MsgInfo.Type.LOCATION);
			mimeType = Constants.MIME_IMAGE;
			String[] array = desc.split(Constants.SPLITE_TAG_MSG_TYPE);
			if (SystemUtil.isNotEmpty(array)) {
				try {
					desc = array[0];
					subJect = array[1];
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {	//普通的文件
			//TODO 类型匹配
			//获得文件的后缀名，不包含".",如mp3
			String subfix = SystemUtil.getFileSubfix(request.getFileName()).toLowerCase(Locale.getDefault());;
			//获得文件的mimetype，如image/jpeg
			mimeType = MimeUtils.guessMimeTypeFromExtension(subfix);
			mimeType = (mimeType == null) ? request.getMimeType() : mimeType;
			
			MsgInfo.Type msgType = SystemUtil.getMsgInfoType(subfix, mimeType);
			
			msgInfo.setMsgType(msgType);
		}
		msgInfo.setContent(desc);
		msgInfo.setRead(false);
		msgInfo.setSubject(subJect);
		msgInfo.setSendState(SendState.SUCCESS);
		msgInfo.setToUser(ChatApplication.getInstance().getCurrentAccount());
		
		int threadId = msgManager.getThreadIdByMembers(fromUser);	//查找本地会话，如果没有就创建
		if (threadId > 0) {
			msgInfo.setThreadID(threadId);
		}
		
		//设置附件
		MsgPart msgPart = new MsgPart();
		msgPart.setCreationDate(System.currentTimeMillis());
		msgPart.setFileName(request.getFileName());
		msgPart.setSize(request.getFileSize());
		msgPart.setMimeTye(mimeType);
		String savePath = SystemUtil.generateChatAttachFilePath(threadId, msgPart.getFileName());
		msgPart.setFilePath(savePath);
		
		msgInfo.setMsgPart(msgPart);
		
		return msgInfo;
	}
	
	/**
	 * 根据通知id清除通知栏
	 * @update 2015年3月3日 下午2:05:56
	 * @param nofifyId 通知的id
	 */
	public void clearNotify(int nofifyId) {
		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);;
		}
		mNotificationManager.cancel(nofifyId);
	}
	
	/**
	 * 清除全部通知
	 * @update 2015年3月3日 下午2:07:11
	 */
	public void clearAllNotify() {
		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);;
		}
		mNotificationManager.cancelAll();
	}
	
	/**
	 * 接收文件的线程
	 * @author huanghui1
	 * @update 2014年11月20日 下午2:47:03
	 */
	class ReceiveFileTask implements Runnable {
		private FileTransferRequest request;
		private MsgInfo msgInfo;

		public ReceiveFileTask(FileTransferRequest request, MsgInfo msgInfo) {
			super();
			this.request = request;
			this.msgInfo = msgInfo;
		}

		@Override
		public void run() {
			IncomingFileTransfer fileTransfer = request.accept();
			File saveFile = new File(msgInfo.getMsgPart().getFilePath());
			if (!saveFile.getParentFile().exists()) {
				saveFile.getParentFile().mkdirs();
			}
			try {
				fileTransfer.recieveFile(saveFile);
			} catch (SmackException | IOException e) {
				e.printStackTrace();
			}
			Intent intent = new Intent(ChatActivity.MsgProcessReceiver.ACTION_REFRESH_MSG);
			sendBroadcast(intent);
		}
		
	}
	
//	/**
//	 * 发送聊天消息的广播
//	 * @author huanghui1
//	 * @update 2014年11月17日 下午8:20:57
//	 */
//	public class SendChatMessageReceiver extends BroadcastReceiver {
//		public static final String ACTION_SEND_CHAT_MSG = "net.ibaixin.chat.SEND_CHAT_MSG";
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if (ACTION_SEND_CHAT_MSG.equals(intent.getAction())) {	//发送消息的广播
//				MsgInfo msgInfo = intent.getParcelableExtra(ChatActivity1.ARG_MSG_INFO);
//				if (msgInfo != null) {
////					sendChatMsg(senderInfo);
//				}
//			}
//		}
//		
//	}
	
	public class MainBinder extends Binder {
		public CoreService getService() {
			return CoreService.this;
		}
	}
	
	/**
	 * 判断聊天界面是否在栈顶
	 * @update 2015年2月28日 上午11:47:36
	 * @return
	 */
	public boolean isChatActivityOnTop() {
		if (mActivityManager == null) {
			mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		}
		RunningTaskInfo info = mActivityManager.getRunningTasks(1).get(0);
		String className = info.topActivity.getClassName();
		if (!TextUtils.isEmpty(className)) {
			return ChatActivity.class.getCanonicalName().equals(className);
		}
		return false;
	}
	
}
