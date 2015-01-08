package com.example.chat.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ChatMessageListener;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jxmpp.util.XmppStringUtils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import com.example.chat.ChatApplication;
import com.example.chat.R;
import com.example.chat.activity.ChatActivity;
import com.example.chat.fragment.ContactFragment.LoadDataBroadcastReceiver;
import com.example.chat.listener.ChatRostListener;
import com.example.chat.manage.MsgManager;
import com.example.chat.manage.UserManager;
import com.example.chat.model.MsgInfo;
import com.example.chat.model.MsgInfo.SendState;
import com.example.chat.model.MsgPart;
import com.example.chat.model.MsgSenderInfo;
import com.example.chat.model.MsgThread;
import com.example.chat.model.Personal;
import com.example.chat.model.User;
import com.example.chat.util.Constants;
import com.example.chat.util.Log;
import com.example.chat.util.MimeUtils;
import com.example.chat.util.SystemUtil;
import com.example.chat.util.XmppConnectionManager;
import com.example.chat.util.XmppUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;

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
	
	private IBinder mBinder = new MainBinder();
	
	private UserManager userManager = UserManager.getInstance();
	private MsgManager msgManager = MsgManager.getInstance();
	
	private MyHandler mHandler = null;
	
	private HandlerThread mHandlerThread = null;
	private static RosterListener mRosterListener;
	private static FileTransferListener mFileTransferListener;
	private static ChatManagerListener mChatManagerListener;
	private static ChatMessageListener mChatMessageListener;
	private static ChatManager mChatManager;
	private static FileTransferManager mFileTransferManager;
	AbstractXMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
	
	private ImageLoader mImageLoader = ImageLoader.getInstance();
	
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
				//发送广播到对应的界面处理
				break;

			default:
				break;
			}
		}
	}
	
	
	@Override
	public void onCreate() {
		if (mHandlerThread == null) {
			mHandlerThread = new HandlerThread(this.getClass().getCanonicalName());
			mHandlerThread.start();
			mHandler = new MyHandler(mHandlerThread.getLooper());
		}
		if (connection != null) {
			if (mRosterListener == null) {
				mRosterListener = new ChatRostListener();
				Log.d("------------connection-----------" + connection.toString());
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
				Personal localPerson = userManager.getLocalSelfInfo(person);
				if (localPerson == null) {	//本地没有个人信息，则从服务器上同步
					//从网上同步个人信息
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
		SystemUtil.getCachedThreadPool().execute(new SendMsgTask(senderInfo));
	}
	
	/**
	 * 根据消息类型来设置会话最后一条消息的内容
	 * @update 2014年11月20日 下午3:07:22
	 * @param msgType 消息类型
	 * @param msgInfo 消息实体
	 * @return
	 */
	private String getSnippetContentByMsgType(MsgInfo.Type msgType, MsgInfo msgInfo) {
		String snippetContent = null;
		switch (msgType) {
		case TEXT:
			snippetContent = msgInfo.getContent();
			break;
		case IMAGE:
			snippetContent = getString(R.string.msg_thread_snippet_content_image);
			break;
		case AUDIO:
			snippetContent = getString(R.string.msg_thread_snippet_content_audio);
			break;
		case FILE:
			snippetContent = getString(R.string.msg_thread_snippet_content_file);
			break;
		case LOCATION:
			snippetContent = getString(R.string.msg_thread_snippet_content_location);
			break;
		case VIDEO:
			snippetContent = getString(R.string.msg_thread_snippet_content_video);
			break;
		case VCARD:
			snippetContent = getString(R.string.msg_thread_snippet_content_vcard);
			break;

		default:
			snippetContent = msgInfo.getContent();
			break;
		}
		return snippetContent;
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
//			try {
				msgInfo = msgManager.addMsgInfo(msgInfo);
				senderInfo.msgThread.setSnippetId(msgInfo.getId());
				String snippetContent = getSnippetContentByMsgType(msgType, msgInfo);
				senderInfo.msgThread.setSnippetContent(snippetContent);
				senderInfo.msgThread.setModifyDate(System.currentTimeMillis());
				senderInfo.msgThread = msgManager.updateMsgThread(senderInfo.msgThread);
				if (MsgInfo.Type.TEXT == msgType || MsgInfo.Type.LOCATION == msgType) {	//文本消息或者地理位置消息
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
					if (msgInfo.getMsgType() == MsgInfo.Type.IMAGE) {	//图片类型
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
							fileTransfer.sendStream(new FileInputStream(sendFile), msgPart.getFileName(), sendFile.length(), msgPart.getFileName());
//							fileTransfer.sendFile(sendFile, msgPart.getFileName());
							while (!fileTransfer.isDone()) {	//传输完毕
//								Log.d("-------------fileTransfer.getStatus()----------------" + fileTransfer.getStatus());
								if (fileTransfer.getStatus() == FileTransfer.Status.in_progress) {
									Log.d("----FileTransferManager------" + fileTransfer.getStatus() + "--" + fileTransfer.getProgress());
								}
							}
							Log.d("-------发送完毕------");
							msgInfo.setSendState(SendState.SUCCESS);
						} catch (FileNotFoundException e) {
							msgInfo.setSendState(SendState.FAILED);
							e.printStackTrace();
						}
					}
				}//XMPPException | SmackException | 
//			} catch (Exception e) {
//				msgInfo.setSendState(SendState.FAILED);
//				e.printStackTrace();
//			}
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
					Intent intent = new Intent(ChatActivity.MsgProcessReceiver.ACTION_PROCESS_MSG);
					intent.putExtra(ChatActivity.ARG_MSG_INFO, msgInfo);
					sendBroadcast(intent);
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

		@Override
		public void processMessage(Chat chat, Message message) {
			SystemUtil.getCachedThreadPool().execute(new ProcessMsgTask(chat, message));
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
				String snippetContent = getSnippetContentByMsgType(msgType, msgInfo);
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
		msgInfo.setContent(request.getDescription());
		msgInfo.setCreationDate(System.currentTimeMillis());
		msgInfo.setFromUser(fromUser);
		
		//TODO 类型匹配
		//获得文件的后缀名，不包含".",如mp3
		String subfix = SystemUtil.getFileSubfix(request.getFileName()).toLowerCase(Locale.getDefault());;
		//获得文件的mimetype，如image/jpeg
		String mimeType = MimeUtils.guessMimeTypeFromExtension(subfix);
		mimeType = (mimeType == null) ? request.getMimeType() : mimeType;
		
		MsgInfo.Type msgType = SystemUtil.getMsgInfoType(subfix, mimeType);
		
		msgInfo.setMsgType(msgType);
		msgInfo.setRead(false);
		msgInfo.setSubject(null);
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
//		public static final String ACTION_SEND_CHAT_MSG = "com.example.chat.SEND_CHAT_MSG";
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
	
}
