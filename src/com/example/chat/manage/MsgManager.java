package com.example.chat.manage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.example.chat.ChatApplication;
import com.example.chat.model.MsgInfo;
import com.example.chat.model.MsgInfo.SendState;
import com.example.chat.model.MsgInfo.Type;
import com.example.chat.model.MsgPart;
import com.example.chat.model.MsgThread;
import com.example.chat.model.User;
import com.example.chat.provider.Provider;
import com.example.chat.util.Constants;
import com.example.chat.util.SystemUtil;

/**
 * 聊天相关的业务逻辑层
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月31日 上午9:44:30
 */
public class MsgManager {
	private static MsgManager instance = null;
	private Context mContext = ChatApplication.getInstance();
	
	private UserManager userManager = UserManager.getInstance();
	
	private MsgManager() {}
	
	public static MsgManager getInstance() {
		if (instance == null) {
			synchronized (MsgManager.class) {
				if (instance == null) {
					instance = new MsgManager();
				}
			}
		}
		return instance;
	}
	
	/**
	 * 根据id获取会话信息
	 * @update 2014年10月31日 上午10:55:25
	 * @param id
	 * @return
	 */
	public MsgThread getThreadById(int id) {
		Uri uri = ContentUris.withAppendedId(Provider.MsgThreadColumns.CONTENT_URI, id);
		Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
		MsgThread mt = null;
		if (cursor != null && cursor.moveToFirst()) {
			mt = new MsgThread();
			mt.setId(id);
			mt.setMsgThreadName(cursor.getString(cursor.getColumnIndex(Provider.MsgThreadColumns.MSG_THREAD_NAME)));
			mt.setModifyDate(cursor.getLong(cursor.getColumnIndex(Provider.MsgThreadColumns.MODIFY_DATE)));
			mt.setSnippetId(cursor.getInt(cursor.getColumnIndex(Provider.MsgThreadColumns.SNIPPET_ID)));
			mt.setSnippetContent(cursor.getString(cursor.getColumnIndex(Provider.MsgThreadColumns.SNIPPET_CONTENT)));
			mt.setUnReadCount(cursor.getInt(cursor.getColumnIndex(Provider.MsgThreadColumns.UNREAD_COUNT)));
			String memberIds = cursor.getString(cursor.getColumnIndex(Provider.MsgThreadColumns.MEMBER_IDS));
			
			List<User> members = getMemebersByMemberIds(memberIds);
			mt.setMembers(members);
		}
		if (cursor != null) {
			cursor.close();
		}
		return mt;
	}
	
	/**
	 * 根据uri获取会话信息
	 * @update 2014年10月31日 上午10:55:25
	 * @param uri
	 * @return
	 */
	public MsgThread getThreadByUri(Uri uri) {
		int id = Integer.parseInt(uri.getLastPathSegment());
		return getThreadById(id);
	}
	
	/**
	 * 根据聊天的参与成员获取对应的会话
	 * @update 2014年10月31日 上午10:00:59
	 * @param members
	 * @return
	 */
	public MsgThread getThreadByMember(User member) {
		if (member == null) {
			return null;
		}
		String memberIds = String.valueOf(member.getId());
		MsgThread mt = null;
		Cursor cursor = mContext.getContentResolver().query(Provider.MsgThreadColumns.CONTENT_URI, null, Provider.MsgThreadColumns.MEMBER_IDS + " = ?", new String[] {memberIds}, null);
		if (cursor != null && cursor.moveToFirst()) {
			mt = new MsgThread();
			mt.setId(cursor.getInt(cursor.getColumnIndex(Provider.MsgThreadColumns._ID)));
			mt.setMsgThreadName(cursor.getString(cursor.getColumnIndex(Provider.MsgThreadColumns.MSG_THREAD_NAME)));
			mt.setMembers(Arrays.asList(member));
		}
		if (cursor != null) {
			cursor.close();
		}
		return mt;
	}
	
	/**
	 * 根据会话的成员获得会话的id，该实体中只包含msgThreadId
	 * @update 2014年11月12日 下午7:44:45
	 * @param members
	 * @return
	 */
	public MsgThread getMsgThreadIdByMembers(List<User> members) {
		if (SystemUtil.isEmpty(members)) {
			return null;
		}
		String memberIds = getMemberIds(members);
		MsgThread msgThread = null;
		Cursor cursor = mContext.getContentResolver().query(Provider.MsgThreadColumns.CONTENT_URI, new String[] {Provider.MsgThreadColumns._ID}, Provider.MsgThreadColumns.MEMBER_IDS + " = ?", new String[] {memberIds}, null);
		if (cursor != null && cursor.moveToFirst()) {
			msgThread = new MsgThread();
			msgThread.setId(cursor.getInt(0));
		}
		if (cursor != null) {
			cursor.close();
		}
		return msgThread;
	}
	
	/**
	 * 初始化MsgThread的数据源
	 * @update 2014年10月31日 下午2:06:34
	 * @param msgThread
	 * @return
	 */
	private ContentValues initMsgThreadVaule(MsgThread msgThread) {
		ContentValues values = new ContentValues();
		String memberIds = getMemberIds(msgThread.getMembers());
		String threadNme = msgThread.getMsgThreadName();
		if (TextUtils.isEmpty(threadNme)) {
			threadNme = getMsgThreadName(msgThread.getMembers());
		}
		values.put(Provider.MsgThreadColumns.MSG_THREAD_NAME, threadNme);
		values.put(Provider.MsgThreadColumns.MEMBER_IDS, memberIds);
		values.put(Provider.MsgThreadColumns.UNREAD_COUNT, msgThread.getUnReadCount());
		values.put(Provider.MsgThreadColumns.SNIPPET_ID, msgThread.getSnippetId());
		String snippetContent = msgThread.getSnippetContent();
		if (snippetContent == null) {
			snippetContent = "";
		}
		values.put(Provider.MsgThreadColumns.SNIPPET_CONTENT, snippetContent);
		long time = msgThread.getModifyDate();
		if (time <= 0) {
			time = System.currentTimeMillis();
		}
		values.put(Provider.MsgThreadColumns.MODIFY_DATE, time);
		return values;
	}
	
	/**
	 * 根据成员列表获取成员id的字符串
	 * @update 2014年10月31日 下午2:09:36
	 * @param members
	 * @return
	 */
	private String getMemberIds(List<User> members) {
		String memberIds = null;
		if (members.size() == 0) {	//只有一个成员
			memberIds = String.valueOf(members.get(0).getId());
		} else {
			StringBuilder sb = new StringBuilder();
			for (User user : members) {
				sb.append(user.getId()).append(";");
			}
			//去除最后一个多余的分隔符
			sb.deleteCharAt(sb.length() - 1);
			memberIds = sb.toString();
		}
		return memberIds;
	}
	
	/**
	 * 根据成员获取会话名称
	 * @update 2014年10月31日 下午2:44:52
	 * @param members
	 * @return
	 */
	private String getMsgThreadName(List<User> members) {
		if (members == null || members.size() == 0) {
			return null;
		}
		int size = members.size();
		String threadName = null;
		if (size == 0) {	//只有一个人
			threadName = members.get(0).getName();
		} else if (size <= 3) {	//少于3个人
			StringBuilder sb = new StringBuilder();
			for (User user : members) {
				sb.append(user.getName()).append("、");
			}
			sb.deleteCharAt(sb.length() - 1);
			threadName = sb.toString();
		} else {	//是列出前三个人的名称作为会话名称
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 4; i++) {
				User user = members.get(i);
				sb.append(user.getName()).append("、");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("...");
			threadName = sb.toString();
		}
		return threadName;
	}
	
	/**
	 * 创建一个会话
	 * @update 2014年10月31日 下午2:55:27
	 * @param msgThread
	 * @return 创建后的会话
	 */
	public MsgThread createMsgThread(MsgThread msgThread) {
		if (msgThread == null) {
			return null;
		}
		Uri uri = mContext.getContentResolver().insert(Provider.MsgThreadColumns.CONTENT_URI, initMsgThreadVaule(msgThread));
		if (uri != null) {
			int threadId = Integer.parseInt(uri.getLastPathSegment());  	
			msgThread.setId(threadId);
		}
		return msgThread;
	}
	
	/**
	 * 根据会话id获取该会话内的所有消息，每个msginfo值包含id和msgType
	 * @update 2014年11月12日 下午7:59:37
	 * @param threadId
	 * @return
	 */
	public List<MsgInfo> getMsgInfoIdsByThreadId(int threadId) {
		Cursor cursor = mContext.getContentResolver().query(Provider.MsgInfoColumns.CONTENT_URI, new String[] {Provider.MsgInfoColumns._ID, Provider.MsgInfoColumns.MSG_TYPE}, Provider.MsgInfoColumns.THREAD_ID + " = ?", new String[] {String.valueOf(threadId)}, null);
		List<MsgInfo> list = null;
		if (cursor != null) {
			list = new ArrayList<>();
			while (cursor.moveToNext()) {
				MsgInfo msgInfo = new MsgInfo();
				msgInfo.setId(cursor.getInt(0));
				msgInfo.setMsgType(MsgInfo.Type.valueOf(cursor.getInt(1)));
				list.add(msgInfo);
			}
		}
		return list;
	}
	
	/**
	 * 根据会话id删除指定的会话
	 * @update 2014年11月12日 下午7:37:41
	 * @param threadId
	 * @return
	 */
	public boolean deleteMsgThreadById(int threadId) {
		boolean falg = false;
		int count = mContext.getContentResolver().delete(ContentUris.withAppendedId(Provider.MsgThreadColumns.CONTENT_URI, threadId), null, null);
		if (count > 0) {	//删除成功
			//删除会话中的消息
			//查找该会话中的消息
			List<MsgInfo> msgInfos = getMsgInfoIdsByThreadId(threadId);
			if (!SystemUtil.isEmpty(msgInfos)) {	//该会话有消息
				//删除消息
				for (MsgInfo msgInfo : msgInfos) {
					deleteMsgInfoById(msgInfo);
				}
			}
			falg = true;
		}
		return falg;
	}
	
	/**
	 * 获得所有的会话列表
	 * @update 2014年10月31日 下午9:09:11
	 * @return 所有的会话列表
	 */
	public List<MsgThread> getMsgThreadList() {
		List<MsgThread> list = null;
		Cursor cursor = mContext.getContentResolver().query(Provider.MsgThreadColumns.CONTENT_URI, null, null, null, null);
		if (cursor != null) {
			list = new ArrayList<>();
			while (cursor.moveToNext()) {
				MsgThread msgThread = new MsgThread();
				msgThread.setId(cursor.getInt(cursor.getColumnIndex(Provider.MsgThreadColumns._ID)));
				msgThread.setModifyDate(cursor.getLong(cursor.getColumnIndex(Provider.MsgThreadColumns.MODIFY_DATE)));
				msgThread.setMsgThreadName(cursor.getString(cursor.getColumnIndex(Provider.MsgThreadColumns.MSG_THREAD_NAME)));
				msgThread.setSnippetId(cursor.getInt(cursor.getColumnIndex(Provider.MsgThreadColumns.SNIPPET_ID)));
				msgThread.setSnippetContent(cursor.getString(cursor.getColumnIndex(Provider.MsgThreadColumns.SNIPPET_CONTENT)));
				msgThread.setUnReadCount(cursor.getInt(cursor.getColumnIndex(Provider.MsgThreadColumns.UNREAD_COUNT)));
				String memberIds = cursor.getString(cursor.getColumnIndex(Provider.MsgThreadColumns.MEMBER_IDS));
				
				List<User> members = getMemebersByMemberIds(memberIds);
				msgThread.setMembers(members);
				list.add(msgThread);
			}
			cursor.close();
		}
		return list;
	}
	
	/**
	 * 根据聊天成员的id获取成员信息
	 * @update 2014年10月31日 下午9:54:50
	 * @param memberIds
	 * @return
	 */
	public List<User> getMemebersByMemberIds(String memberIds) {
		if (TextUtils.isEmpty(memberIds)) {
			return null;
		}
		List<User> list = new ArrayList<>();
		if (memberIds.contains(";")) {	///有多个成员
			String[] ids = memberIds.split(";");
			for (String id : ids) {
				User user = userManager.getUserById(Integer.parseInt(id));
				list.add(user);
			}
		} else {
			User user = userManager.getUserById(Integer.parseInt(memberIds));
			list.add(user);
		}
		return list;
	}
	
	/**
	 * 根据聊天的参与成员获取对应的会话
	 * @update 2014年10月31日 上午10:00:59
	 * @param members
	 * @return
	 */
	public MsgThread getThreadByMembers(List<User> members) {
		if (members == null || members.size() == 0) {
			return null;
		}
		
		String memberIds = getMemberIds(members);
		
		MsgThread mt = null;
		Cursor cursor = mContext.getContentResolver().query(Provider.MsgThreadColumns.CONTENT_URI, null, Provider.MsgThreadColumns.MEMBER_IDS + " = ?", new String[] {memberIds}, null);
		if (cursor != null && cursor.moveToFirst()) {
			mt = new MsgThread();
			mt.setId(cursor.getInt(cursor.getColumnIndex(Provider.MsgThreadColumns._ID)));
			mt.setMsgThreadName(cursor.getString(cursor.getColumnIndex(Provider.MsgThreadColumns.MSG_THREAD_NAME)));
			mt.setMembers(members);
		}
		if (cursor != null) {
			cursor.close();
		}
		return mt;
	}
	
	/**
	 * 根据消息id获取的附件信息
	 * @update 2014年10月31日 上午11:54:18
	 * @param msgId
	 * @return
	 */
	public MsgPart getMsgPartByMsgId(int msgId) {
		Uri uri = ContentUris.withAppendedId(Provider.MsgPartColumns.CONTENT_URI, msgId);
		Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
		MsgPart msgPart = null;
		if (cursor != null && cursor.moveToFirst()) {
			msgPart = new MsgPart();
			msgPart.setId(cursor.getInt(cursor.getColumnIndex(Provider.MsgPartColumns._ID)));
			msgPart.setFileName(cursor.getString(cursor.getColumnIndex(Provider.MsgPartColumns.FILE_NAME)));
			msgPart.setFilePath(cursor.getString(cursor.getColumnIndex(Provider.MsgPartColumns.FILE_PATH)));
			msgPart.setCreationDate(cursor.getLong(cursor.getColumnIndex(Provider.MsgPartColumns.CREATION_DATE)));
			msgPart.setMimeTye(cursor.getString(cursor.getColumnIndex(Provider.MsgPartColumns.MIME_TYE)));
			msgPart.setSize(cursor.getLong(cursor.getColumnIndex(Provider.MsgPartColumns.SIZE)));
			msgPart.setMsgId(msgId);
		}
		if (cursor != null) {
			cursor.close();
		}
		return msgPart;
	}
	
	/**
	 * 根据消息id获取附件的本地存储路径，该附件实体值包含附件的路径filePath
	 * @update 2014年11月12日 下午8:29:18
	 * @param msgId
	 * @return
	 */
	public MsgPart getMsgPartPathByMsgId(int msgId) {
		Uri uri = ContentUris.withAppendedId(Provider.MsgPartColumns.CONTENT_URI, msgId);
		Cursor cursor = mContext.getContentResolver().query(uri, new String[] {Provider.MsgPartColumns.FILE_PATH}, null, null, null);
		MsgPart msgPart = null;
		if (cursor != null && cursor.moveToFirst()) {
			msgPart = new MsgPart();
			msgPart.setFilePath(cursor.getString(0));
		}
		if (cursor != null) {
			cursor.close();
		}
		return msgPart;
	}
	
	/**
	 * 通过会话的id查询该会话下的聊天消息
	 * @update 2014年10月31日 上午11:23:57
	 * @param threadId 消息会话的id
	 * @param pageOffset 开始的查询索引
	 * @return
	 */
	public List<MsgInfo> getMsgInfosByThreadId(int threadId, int pageOffset) {
		List<MsgInfo> list = null;
		String sortOrder = Provider.MsgInfoColumns.DEFAULT_SORT_ORDER + " limit " + Constants.PAGE_SIZE_MSG + " Offset " + pageOffset;
		Cursor cursor = mContext.getContentResolver().query(Provider.MsgInfoColumns.CONTENT_URI, null, Provider.MsgInfoColumns.THREAD_ID + " = ?", new String[] {String.valueOf(threadId)}, sortOrder);
		if (cursor != null) {
			list = new ArrayList<>();
			while (cursor.moveToNext()) {
				MsgInfo msg = new MsgInfo();
				msg.setId(cursor.getInt(cursor.getColumnIndex(Provider.MsgInfoColumns._ID)));
				msg.setThreadID(threadId);
				msg.setFromUser(cursor.getString(cursor.getColumnIndex(Provider.MsgInfoColumns.FROM_USER)));
				msg.setToUser(cursor.getString(cursor.getColumnIndex(Provider.MsgInfoColumns.TO_USER)));
				msg.setContent(cursor.getString(cursor.getColumnIndex(Provider.MsgInfoColumns.CONTENT)));
				msg.setSubject(cursor.getString(cursor.getColumnIndex(Provider.MsgInfoColumns.SUBJECT)));
				msg.setCreationDate(cursor.getLong(cursor.getColumnIndex(Provider.MsgInfoColumns.CREATIO_NDATE)));
				msg.setComming(cursor.getInt(cursor.getColumnIndex(Provider.MsgInfoColumns.IS_COMMING)) == 0 ? false : true);
				msg.setRead(cursor.getInt(cursor.getColumnIndex(Provider.MsgInfoColumns.IS_READ)) == 0 ? false : true);
				msg.setMsgType(Type.valueOf(cursor.getInt(cursor.getColumnIndex(Provider.MsgInfoColumns.MSG_TYPE))));
				msg.setSendState(SendState.valueOf(cursor.getInt(cursor.getColumnIndex(Provider.MsgInfoColumns.SEND_STATE))));
				
				Type msgType = msg.getMsgType();
				//如果消息不是文本类型，则加载附件
				if (msgType != Type.TEXT && msgType != Type.LOCATION) {	//加载附件
					MsgPart msgPart = getMsgPartByMsgId(msg.getId());
					msg.setMsgPart(msgPart);
				}
				list.add(msg);
			}
			cursor.close();
		}
		return list;
	}
	
	/**
	 * 根据msgid获得消息对象
	 * @update 2014年11月6日 下午9:08:40
	 * @param msgId
	 * @return
	 */
	public MsgInfo getMsgInfoById(int msgId) {
		if (msgId <= 0) {
			return null;
		}
		Uri uri = ContentUris.withAppendedId(Provider.MsgInfoColumns.CONTENT_URI, msgId);
		Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
		MsgInfo msg = null;
		if (cursor != null && cursor.moveToFirst()) {
			msg = new MsgInfo();
			msg.setId(msgId);
			msg.setThreadID(cursor.getInt(cursor.getColumnIndex(Provider.MsgInfoColumns.THREAD_ID)));
			msg.setFromUser(cursor.getString(cursor.getColumnIndex(Provider.MsgInfoColumns.FROM_USER)));
			msg.setToUser(cursor.getString(cursor.getColumnIndex(Provider.MsgInfoColumns.TO_USER)));
			msg.setContent(cursor.getString(cursor.getColumnIndex(Provider.MsgInfoColumns.CONTENT)));
			msg.setSubject(cursor.getString(cursor.getColumnIndex(Provider.MsgInfoColumns.SUBJECT)));
			msg.setCreationDate(cursor.getLong(cursor.getColumnIndex(Provider.MsgInfoColumns.CREATIO_NDATE)));
			msg.setComming(cursor.getInt(cursor.getColumnIndex(Provider.MsgInfoColumns.IS_COMMING)) == 0 ? false : true);
			msg.setRead(cursor.getInt(cursor.getColumnIndex(Provider.MsgInfoColumns.IS_READ)) == 0 ? false : true);
			msg.setMsgType(Type.valueOf(cursor.getInt(cursor.getColumnIndex(Provider.MsgInfoColumns.MSG_TYPE))));
			msg.setSendState(SendState.valueOf(cursor.getInt(cursor.getColumnIndex(Provider.MsgInfoColumns.SEND_STATE))));
			
			Type msgType = msg.getMsgType();
			//如果消息不是文本类型，则加载附件
			if (msgType != Type.TEXT && msgType != Type.LOCATION) {	//加载附件
				MsgPart msgPart = getMsgPartByMsgId(msg.getId());
				msg.setMsgPart(msgPart);
			}
		}
		if (cursor != null) {
			cursor.close();
		}
		return msg;
	}
	
	/**
	 * 根据uri获得消息信息
	 * @update 2014年11月6日 下午9:15:45
	 * @param uri
	 * @return
	 */
	public MsgInfo getMsgInfoByUri(Uri uri) {
		if (uri == null) {
			return null;
		}
		int msgId = Integer.parseInt(uri.getLastPathSegment());
		return getMsgInfoById(msgId);
	}
	
	/**
	 * 根据会话成员id查询会话的id
	 * @update 2014年11月4日 下午9:35:56
	 * @param memberIds 会话成员id
	 * @return
	 */
	public int getThreadIdByMembers(int... memberIds) {
		int tid = -1;
		//是否是多个人
		boolean multi = true;
		if (memberIds != null && memberIds.length > 0) {
			String strIds = null;
			if (memberIds.length == 1) {
				strIds = String.valueOf(memberIds[0]);
			} else {
				multi = false;
				StringBuilder sb = new StringBuilder();
				for (int id : memberIds) {
					sb.append(id).append(";");
				}
				sb.deleteCharAt(sb.length() - 1);
				strIds = sb.toString();
			}
			Cursor cursor = mContext.getContentResolver().query(Provider.MsgThreadColumns.CONTENT_URI, new String[] {Provider.MsgThreadColumns._ID}, Provider.MsgThreadColumns.MEMBER_IDS + " = ?", new String[] {strIds}, null);
			if (cursor != null && cursor.moveToFirst()) {	//有该会话
				tid = cursor.getInt(0);
			} else {	//没有改会话，则创建一个会话
				MsgThread msgThread = new MsgThread();
				List<User> list = new LinkedList<>();
				if (multi) {	//是多个人
					for (int id : memberIds) {
						User u = userManager.getUserById(id);
						list.add(u);
					}
				} else {
					User u = userManager.getUserById(memberIds[0]);
					list.add(u);
				}
				msgThread.setMembers(list);
				msgThread = createMsgThread(msgThread);
				tid = msgThread.getId();
			}
			if (cursor != null) {
				cursor.close();
			}
		}
		return tid;
	}
	
	/**
	 * 根据会话成员id查询会话的id
	 * @update 2014年11月4日 下午9:35:56
	 * @param memAccounts 会话成员账号，即用户名
	 * @return
	 */
	public int getThreadIdByMembers(String... memAccounts) {
		int tid = -1;
		//是否是多个人
		List<User> members = new LinkedList<>();
		if (memAccounts != null && memAccounts.length > 0) {
			String strIds = null;
			if (memAccounts.length == 1) {
				User u = userManager.getUserByUsername(memAccounts[0]);
				strIds = String.valueOf(u.getId());
				members.add(u);
			} else {
				StringBuilder sb = new StringBuilder();
				for (String account : memAccounts) {
					//根据账号查询用户id
					User u = userManager.getUserByUsername(account);
					sb.append(u.getId()).append(";");
					members.add(u);
				}
				sb.deleteCharAt(sb.length() - 1);
				strIds = sb.toString();
			}
			Cursor cursor = mContext.getContentResolver().query(Provider.MsgThreadColumns.CONTENT_URI, new String[] {Provider.MsgThreadColumns._ID}, Provider.MsgThreadColumns.MEMBER_IDS + " = ?", new String[] {strIds}, null);
			if (cursor != null && cursor.moveToFirst()) {	//有该会话
				tid = cursor.getInt(0);
			} else {	//没有改会话，则创建一个会话
				MsgThread msgThread = new MsgThread();
				msgThread.setMembers(members);
				msgThread = createMsgThread(msgThread);
				tid = msgThread.getId();
			}
			if (cursor != null) {
				cursor.close();
			}
		}
		return tid;
	}
	
	/**
	 * 初始化msginfo的相关数据
	 * @update 2014年11月4日 下午10:43:06
	 * @param msgInfo
	 * @return
	 */
	private ContentValues initMsgInfoValues(MsgInfo msgInfo) {
		ContentValues values = new ContentValues();
		values.put(Provider.MsgInfoColumns.THREAD_ID, msgInfo.getThreadID());
		values.put(Provider.MsgInfoColumns.FROM_USER, msgInfo.getFromUser());
		values.put(Provider.MsgInfoColumns.TO_USER, msgInfo.getToUser());
		values.put(Provider.MsgInfoColumns.CONTENT, msgInfo.getContent());
		values.put(Provider.MsgInfoColumns.SUBJECT, msgInfo.getSubject());
		values.put(Provider.MsgInfoColumns.CREATIO_NDATE, msgInfo.getCreationDate());
		values.put(Provider.MsgInfoColumns.IS_COMMING, msgInfo.isComming() ? 1 : 0);
		values.put(Provider.MsgInfoColumns.IS_READ, msgInfo.isRead() ? 1 : 0);
		Type type = msgInfo.getMsgType();
		if (type == null) {
			type = Type.TEXT;
		}
		values.put(Provider.MsgInfoColumns.MSG_TYPE, type.ordinal());
		SendState sendState = msgInfo.getSendState();
		if (sendState == null) {
			sendState = SendState.SUCCESS;
		}
		values.put(Provider.MsgInfoColumns.SEND_STATE, sendState.ordinal());
		return values;
	}
	
	/**
	 * 初始化msgPart的相关数据
	 * @update 2014年11月4日 下午10:43:06
	 * @param msgPart
	 * @return
	 */
	private ContentValues initMsgPartValues(MsgPart msgPart) {
		ContentValues values = new ContentValues();
		values.put(Provider.MsgPartColumns.MSG_ID, msgPart.getMsgId());
		values.put(Provider.MsgPartColumns.FILE_NAME, msgPart.getFileName());
		values.put(Provider.MsgPartColumns.FILE_PATH, msgPart.getFilePath());
		values.put(Provider.MsgPartColumns.SIZE, msgPart.getSize());
		values.put(Provider.MsgPartColumns.CREATION_DATE, msgPart.getCreationDate());
		values.put(Provider.MsgPartColumns.MIME_TYE, msgPart.getMimeTye());
		return values;
	}
	
	/**
	 * 添加附件信息
	 * @update 2014年11月5日 上午8:34:42
	 * @param msgPart
	 * @return
	 */
	public MsgPart addMsgPart(MsgPart msgPart) {
		if (msgPart == null) {
			return null;
		}
		ContentValues partValues = initMsgPartValues(msgPart);
		Uri uri = mContext.getContentResolver().insert(Provider.MsgPartColumns.CONTENT_URI, partValues);
		if (uri != null) {
			msgPart.setId(Integer.parseInt(uri.getLastPathSegment()));
		}
		return msgPart;
	}
	
	/**
	 * 根据消息id删除该条消息，该消息实体值包含msgId和msgType
	 * @update 2014年11月12日 下午8:08:44
	 * @param msgInfo
	 * @return
	 */
	public boolean deleteMsgInfoById(MsgInfo msgInfo) {
		if (msgInfo == null) {
			return false;
		}
		int count = mContext.getContentResolver().delete(ContentUris.withAppendedId(Provider.MsgInfoColumns.CONTENT_URI, msgInfo.getId()), null, null);
		if (count > 0) {	//删除消息成功
			Type msgType = msgInfo.getMsgType();
			if (MsgInfo.Type.TEXT != msgType && MsgInfo.Type.LOCATION != msgType) {	//有附件
				//删除附件
				deleteMsgPartByMsgId(msgInfo.getId());
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 根据消息id删除消息的附件
	 * @update 2014年11月12日 下午8:14:14
	 * @param msgId
	 */
	public void deleteMsgPartByMsgId(int msgId) {
		MsgPart msgPart = getMsgPartPathByMsgId(msgId);
		if (msgPart != null) {	//有附件
			//查询该消息对应的附件
			int count = mContext.getContentResolver().delete(ContentUris.withAppendedId(Provider.MsgPartColumns.CONTENT_URI, msgId), null, null);
			if (count > 0) {	//删除成功
				//则删除本地附件
				String filePath = msgPart.getFilePath();
				SystemUtil.deleteFile(filePath);
			}
		}
	}
	
	/**
	 * 添加一条消息记录
	 * @update 2014年11月4日 下午10:41:46
	 * @param msgInfo 消息记录
	 * @return
	 */
	public MsgInfo addMsgInfo(MsgInfo msgInfo) {
		if (msgInfo == null) {
			return null;
		}
		ContentValues infoVaules = initMsgInfoValues(msgInfo);
		Uri uri = mContext.getContentResolver().insert(Provider.MsgInfoColumns.CONTENT_URI, infoVaules);
		if (uri != null) {
			String msgId = uri.getLastPathSegment();
			msgInfo.setId(Integer.parseInt(msgId));
			switch (msgInfo.getMsgType()) {
			case IMAGE:	//图片
			case AUDIO:	//音频
			case FILE:	//文件
			case VIDEO:	//视频
			case VCARD:	//电子名片
				//添加附件信息
				MsgPart msgPart = addMsgPart(msgInfo.getMsgPart());
				msgInfo.setMsgPart(msgPart);
				break;

			default:
				break;
			}
		}
		return msgInfo;
	}
	
	/**
	 * 更新该会话的最后一条消息记录
	 * @update 2014年11月7日 下午8:57:42
	 * @param msgThread
	 * @return
	 */
	public MsgThread updateSnippet(MsgThread msgThread) {
		ContentValues values = initMsgThreadVaule(msgThread);
		Uri uri = ContentUris.withAppendedId(Provider.MsgThreadColumns.CONTENT_URI, msgThread.getId());
		mContext.getContentResolver().update(uri, values, null, null);
		return msgThread;
	}
	
	/**
	 * 更新消息信息
	 * @update 2014年11月6日 下午10:09:06
	 * @param msgInfo
	 * @return
	 */
	public MsgInfo updateMsgInfo(MsgInfo msgInfo) {
		Uri uri = ContentUris.withAppendedId(Provider.MsgInfoColumns.CONTENT_URI, msgInfo.getId());
		ContentValues values = initMsgInfoValues(msgInfo);
		mContext.getContentResolver().update(uri, values, null, null);
		return msgInfo;
	}
	
	/**
	 * 更新会话的基本信息
	 * @update 2014年11月8日 上午10:44:48
	 * @param msgThread
	 * @return
	 */
	public MsgThread updateMsgThread(MsgThread msgThread) {
		Uri uri = ContentUris.withAppendedId(Provider.MsgThreadColumns.CONTENT_URI, msgThread.getId());
		ContentValues values = initMsgThreadVaule(msgThread);
		mContext.getContentResolver().update(uri, values, null, null);
		return msgThread;
	}
}
