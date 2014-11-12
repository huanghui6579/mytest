package com.example.chat.manage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.example.chat.ChatApplication;
import com.example.chat.model.MsgThread;
import com.example.chat.model.NewFriendInfo;
import com.example.chat.model.Personal;
import com.example.chat.model.User;
import com.example.chat.model.UserVcard;
import com.example.chat.provider.Provider;
import com.example.chat.util.SystemUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 业务逻辑层
 * @author coolpad
 *
 */
public class UserManager {
	private static UserManager instance = null;
	
	private Context mContext;
	
	private UserManager() {
		mContext = ChatApplication.getInstance();
	}
	
	/**
	 * 获取单例的实例
	 * @return
	 */
	public static UserManager getInstance() {
		if (instance == null) {
			synchronized (UserManager.class) {
				if (instance == null) {
					instance = new UserManager();
				}
			}
		}
		return instance;
	}
	
	/**
	 * 从本地数据库中获取当前用户的好友列表
	 * @return 好友列表
	 */
	public List<User> getFriends() {
		List<User> users = null;
		Cursor cursor = mContext.getContentResolver().query(Provider.UserColumns.CONTENT_URI, null, null, null, null);
		if (cursor != null) {
			users = new ArrayList<User>();
			while(cursor.moveToNext()) {
				User user = new User();
				user.setId(cursor.getInt(cursor.getColumnIndex(Provider.UserColumns._ID)));
				user.setUsername(cursor.getString(cursor.getColumnIndex(Provider.UserColumns.USERNAME)));
				user.setNickname(cursor.getString(cursor.getColumnIndex(Provider.UserColumns.NICKNAME)));
				user.setMode(cursor.getString(cursor.getColumnIndex(Provider.UserColumns.MODE)));
				user.setPhone(cursor.getString(cursor.getColumnIndex(Provider.UserColumns.PHONE)));
				user.setResource(cursor.getString(cursor.getColumnIndex(Provider.UserColumns.RESOURCE)));
				user.setEmail(cursor.getString(cursor.getColumnIndex(Provider.UserColumns.EMAIL)));
				user.setFullPinyin(cursor.getString(cursor.getColumnIndex(Provider.UserColumns.FULLPINYIN)));
				user.setFullPinyin(cursor.getString(cursor.getColumnIndex(Provider.UserColumns.FULLPINYIN)));
				user.setShortPinyin(cursor.getString(cursor.getColumnIndex(Provider.UserColumns.SHORTPINYIN)));
				user.setSortLetter(cursor.getString(cursor.getColumnIndex(Provider.UserColumns.SORTLETTER)));
				user.setJID(user.initJID(user.getUsername()));
				Cursor cardCursor = mContext.getContentResolver().query(Provider.UserVcardColumns.CONTENT_URI, new String[] {Provider.UserVcardColumns._ID, Provider.UserVcardColumns.NICKNAME, Provider.UserVcardColumns.ICONPATH, Provider.UserVcardColumns.ICONHASH}, Provider.UserVcardColumns.USERID + " = ?", new String[] {String.valueOf(user.getId())}, null);
				if (cardCursor != null && cardCursor.moveToFirst()) {	//查询该好友对应的电子名片
					UserVcard uCard = new UserVcard();
					uCard.setId(cardCursor.getInt(cardCursor.getColumnIndex(Provider.UserVcardColumns._ID)));
					uCard.setUserId(user.getId());
					uCard.setNickname(cardCursor.getString(cardCursor.getColumnIndex(Provider.UserVcardColumns.NICKNAME)));
					uCard.setIconPath(cardCursor.getString(cardCursor.getColumnIndex(Provider.UserVcardColumns.ICONPATH)));
					uCard.setIconHash(cardCursor.getString(cardCursor.getColumnIndex(Provider.UserVcardColumns.ICONHASH)));
					user.setUserVcard(uCard);
				}
				if (cardCursor != null) {
					cardCursor.close();
				}
				users.add(user);
			}
			cursor.close();
			Collections.sort(users, new User());
		}
		return users;
	}
	
	/**
	 * 更新本地数据库的所有好友信息，将网络上的好友同步到本地数据库
	 * @param list
	 */
	public void updateFriends(List<User> list) {
		if (list != null && list.size() > 0) {
			for (User user : list) {
				saveOrUpdateFriend(user);
			}
		}
	}
	
	/**
	 * 清除本地数据库中所有的好友
	 * @param 是否成功删除所有的数据
	 */
	public boolean clearFriends() {
		int count = mContext.getContentResolver().delete(Provider.UserColumns.CONTENT_URI, null, null);
		return count > 0;
	}
	
	/**
	 * 保存或更新好友信息
	 * @update 2014年10月23日 下午7:34:16
	 * @param user
	 * @return
	 */
	public User saveOrUpdateFriend(User user) {
		Cursor cursor = mContext.getContentResolver().query(Provider.UserColumns.CONTENT_URI, new String[] {Provider.UserVcardColumns._ID}, Provider.UserColumns.USERNAME + " = ?", new String[] {user.getUsername()}, null);
		ContentValues userVaules = initUserContentVaules(user);
		if (cursor != null && cursor.moveToFirst()) {	//有好友，就更新
			//1、更新好友表
			user.setId(cursor.getInt(cursor.getColumnIndex(Provider.UserVcardColumns._ID)));
			mContext.getContentResolver().update(Uri.withAppendedPath(Provider.UserColumns.CONTENT_URI, String.valueOf(user.getId())), userVaules, null, null);
			//2、更新好友名片表
			UserVcard uCard = user.getUserVcard();
			if (uCard != null) {	//有名片就插入或更新
//				ContentValues cardValues = initUserVcardContentVaules(uCard);
//				mContext.getContentResolver().update(Uri.withAppendedPath(Provider.UserVcardColumns.CONTENT_URI, String.valueOf(uCard.getId())), cardValues, null, null);
				uCard = saveOrUpdateUserVacard(uCard);
				user.setUserVcard(uCard);
			} else {	//没有名片就查询
				uCard = new UserVcard();
				Cursor cardCursor = mContext.getContentResolver().query(Provider.UserVcardColumns.CONTENT_URI, new String[] {Provider.UserVcardColumns._ID, Provider.UserVcardColumns.NICKNAME, Provider.UserVcardColumns.ICONHASH, Provider.UserVcardColumns.ICONPATH}, Provider.UserVcardColumns.USERID + " = ?", new String[] {String.valueOf(user.getId())}, null);
				if (cardCursor != null && cardCursor.moveToFirst()) {
					uCard.setId(cardCursor.getInt(cardCursor.getColumnIndex(Provider.UserVcardColumns._ID)));
					uCard.setUserId(user.getId());
					uCard.setNickname(cardCursor.getString(cardCursor.getColumnIndex(Provider.UserVcardColumns.NICKNAME)));
					uCard.setIconHash(cardCursor.getString(cardCursor.getColumnIndex(Provider.UserVcardColumns.ICONHASH)));
					uCard.setIconPath(cardCursor.getString(cardCursor.getColumnIndex(Provider.UserVcardColumns.ICONPATH)));
					user.setUserVcard(uCard);
				}
				if (cardCursor != null) {
					cardCursor.close();
				}
			}
		} else {	//添加好友
			Uri uri = mContext.getContentResolver().insert(Provider.UserColumns.CONTENT_URI, userVaules);
			if (uri != null) {
				user.setId(Integer.parseInt(uri.getLastPathSegment()));
			}
			//添加好友名片
			UserVcard uCard = user.getUserVcard();
			if (uCard != null) {
				saveOrUpdateUserVacard(uCard);
			}
		}
		if (cursor != null) {
			cursor.close();
		}
		return user;
	}
	
	/**
	 * 本地添加好友
	 * @update 2014年11月12日 下午4:01:23
	 * @param user
	 * @return
	 */
	public User addFriend(User user) {
		ContentValues userVaules = initUserContentVaules(user);
		Uri uri;
		try {
			uri = mContext.getContentResolver().insert(Provider.UserColumns.CONTENT_URI, userVaules);
			if (uri != null) {
				user.setId(Integer.parseInt(uri.getLastPathSegment()));
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		//添加好友名片
		UserVcard uCard = user.getUserVcard();
		if (uCard != null) {
			saveOrUpdateUserVacard(uCard);
		}
		return user;
	}
	
	/**
	 * 删除指定的用户
	 * @update 2014年11月12日 下午7:24:54
	 * @param user
	 * @return
	 */
	public boolean deleteUser(User user) {
		MsgManager msgManager = MsgManager.getInstance();
		boolean flag = false;
		if (user == null) {
			return false;
		}
		int ucount = mContext.getContentResolver().delete(ContentUris.withAppendedId(Provider.UserColumns.CONTENT_URI, user.getId()), null, null);
		if (ucount > 0) {	//删除成功
			UserVcard ucard = user.getUserVcard();
			//删除好友的电子名片
			if (ucard != null) {	//有电子名片
				int vcount = mContext.getContentResolver().delete(ContentUris.withAppendedId(Provider.UserVcardColumns.CONTENT_URI, user.getId()), null, null);
				if (vcount > 0) {
					//删除本地头像
					String iconPath = ucard.getIconPath();
					if (iconPath != null) {
						//删除头像文件
						SystemUtil.deleteFile(iconPath);
					}
					//查询和自己有没有会话，群聊不算
					MsgThread msgThread = msgManager.getMsgThreadIdByMembers(Arrays.asList(user));
					if (msgThread != null) {	//有会话
						msgManager.deleteMsgThreadById(msgThread.getId());
					}
					flag = true;
				}
			}
		}
		return flag;
	}
	
	/**
	 * 更新用户信息，只更新nickname、uservcard等基本信息
	 * @update 2014年11月11日 下午9:51:42
	 * @param user
	 * @return
	 */
	public User updateSimpleUser(User user) {
		ContentValues values = new ContentValues();
		values.put(Provider.UserColumns.NICKNAME, user.getNickname());
		values.put(Provider.UserColumns.EMAIL, user.getEmail());
		values.put(Provider.UserColumns.PHONE, user.getPhone());
		mContext.getContentResolver().update(ContentUris.withAppendedId(Provider.UserColumns.CONTENT_URI, user.getId()), values, null, null);
		//更新电子名片
		UserVcard uCard = user.getUserVcard();
		if (uCard != null) {
			uCard = saveOrUpdateUserVacard(uCard);
			user.setUserVcard(uCard);
		}
		return user;
	}
	
	/**
	 * 更新或保存好友的名片
	 * @update 2014年10月23日 下午8:20:43
	 * @param uCard
	 * @return
	 */
	public UserVcard saveOrUpdateUserVacard(UserVcard uCard) {
		Cursor cursor = mContext.getContentResolver().query(Provider.UserVcardColumns.CONTENT_URI, new String[] {Provider.UserVcardColumns._ID}, Provider.UserVcardColumns.USERID + " = ?", new String[] {String.valueOf(uCard.getUserId())}, null);
		ContentValues cardVaules = initUserVcardContentVaules(uCard);
		if (cursor != null && cursor.moveToFirst()) {	//已经有名片数据了，就更新
			uCard.setId(cursor.getInt(cursor.getColumnIndex(Provider.UserVcardColumns._ID)));
			mContext.getContentResolver().update(Uri.withAppendedPath(Provider.UserVcardColumns.CONTENT_URI, String.valueOf(uCard.getId())), cardVaules, null, null);
		} else {	//添加名片
			Uri uri = mContext.getContentResolver().insert(Provider.UserVcardColumns.CONTENT_URI, cardVaules);
			if (uri != null) {
				uCard.setId(Integer.parseInt(uri.getLastPathSegment()));
			}
		}
		if (cursor != null) {
			cursor.close();
		}
		return uCard;
	}
	
	/**
	 * 组装user表的键值对
	 * @update 2014年10月23日 下午8:14:28
	 * @param user
	 * @return
	 */
	private ContentValues initUserContentVaules(User user) {
		ContentValues userVaules = new ContentValues();
		userVaules.put(Provider.UserColumns.USERNAME, user.getUsername());
		userVaules.put(Provider.UserColumns.NICKNAME, user.getNickname());
		userVaules.put(Provider.UserColumns.EMAIL, user.getEmail());
		userVaules.put(Provider.UserColumns.PHONE, user.getPhone());
		userVaules.put(Provider.UserColumns.RESOURCE, user.getResource());
		userVaules.put(Provider.UserColumns.STATUS, user.getStatus());
		userVaules.put(Provider.UserColumns.MODE, user.getMode());
		userVaules.put(Provider.UserColumns.FULLPINYIN, user.getFullPinyin());
		userVaules.put(Provider.UserColumns.SHORTPINYIN, user.getShortPinyin());
		userVaules.put(Provider.UserColumns.SORTLETTER, user.getSortLetter());
		return userVaules;
	}
	
	/**
	 * 组装好友名片的键值对
	 * @update 2014年10月23日 下午8:14:48
	 * @param uCard
	 * @return
	 */
	private ContentValues initUserVcardContentVaules(UserVcard uCard) {
		ContentValues cardValues = new ContentValues();
		cardValues.put(Provider.UserVcardColumns.USERID, uCard.getUserId());
		cardValues.put(Provider.UserVcardColumns.NICKNAME, uCard.getNickname());
		cardValues.put(Provider.UserVcardColumns.REALNAME, uCard.getRealName());
		cardValues.put(Provider.UserVcardColumns.MOBILE, uCard.getMobile());
		cardValues.put(Provider.UserVcardColumns.PROVINCE, uCard.getProvince());
		cardValues.put(Provider.UserVcardColumns.CITY, uCard.getCity());
		cardValues.put(Provider.UserVcardColumns.STREET, uCard.getStreet());
		cardValues.put(Provider.UserVcardColumns.ZIPCODE, uCard.getZipCode());
		cardValues.put(Provider.UserVcardColumns.EMAIL, uCard.getEmail());
		cardValues.put(Provider.UserVcardColumns.ICONPATH, uCard.getIconPath());
		cardValues.put(Provider.UserVcardColumns.ICONHASH, uCard.getIconHash());
		return cardValues;
	}
	
	/**
	 * 根据用户id获取用户信息
	 * @update 2014年10月31日 下午10:00:48
	 * @param userId 用户的id
	 * @return
	 */
	public User getUserById(int userId) {
		User user = null;
		String[] projection = {
				Provider.UserColumns.USERNAME,
				Provider.UserColumns.NICKNAME,
		};
		Cursor cursor = mContext.getContentResolver().query(Provider.UserColumns.CONTENT_URI, projection, Provider.UserColumns._ID + " = ?", new String[] {String.valueOf(userId)}, null);
		if (cursor != null && cursor.moveToFirst()) {
			user = new User();
			user.setId(userId);
			user.setUsername(cursor.getString(cursor.getColumnIndex(Provider.UserColumns.USERNAME)));
			user.setNickname(cursor.getString(cursor.getColumnIndex(Provider.UserColumns.NICKNAME)));
			
			UserVcard uCard = getSimpleUserVcardByUserId(userId);
			
			user.setUserVcard(uCard);
		}
		if (cursor != null) {
			cursor.close();
		}
		return user;
	}
	
	/**
	 * 根据用户名获取用户的信息
	 * @update 2014年10月31日 下午10:00:48
	 * @param username 用户名
	 * @return
	 */
	public User getUserByUsername(String username) {
		User user = null;
		String[] projection = {
				Provider.UserColumns._ID,
				Provider.UserColumns.NICKNAME,
		};
		Cursor cursor = mContext.getContentResolver().query(Provider.UserColumns.CONTENT_URI, projection, Provider.UserColumns.USERNAME + " = ?", new String[] {username}, null);
		if (cursor != null && cursor.moveToFirst()) {
			user = new User();
			user.setId(cursor.getInt(cursor.getColumnIndex(Provider.UserColumns._ID)));
			user.setUsername(username);
			user.setNickname(cursor.getString(cursor.getColumnIndex(Provider.UserColumns.NICKNAME)));
			
			UserVcard uCard = getSimpleUserVcardByUserId(user.getId());
			
			user.setUserVcard(uCard);
		}
		if (cursor != null) {
			cursor.close();
		}
		return user;
	}
	
	/**
	 * 获取指定用户的名片
	 * @update 2014年10月24日 下午3:36:39
	 * @param user
	 * @return
	 */
	public UserVcard getSimpleUserVcardByUserId(int userId) {
		UserVcard uCard = null;
		String[] projection = {
				Provider.UserVcardColumns._ID,
				Provider.UserVcardColumns.NICKNAME,
				Provider.UserVcardColumns.ICONPATH,
				Provider.UserVcardColumns.ICONHASH
		};
		Cursor cursor = mContext.getContentResolver().query(Provider.UserVcardColumns.CONTENT_URI, projection, Provider.UserVcardColumns.USERID + " = ?", new String[] {String.valueOf(userId)}, null);
		if (cursor != null && cursor.moveToFirst()) {
			uCard = new UserVcard();
			uCard.setId(cursor.getInt(cursor.getColumnIndex(Provider.UserVcardColumns._ID)));
			uCard.setUserId(userId);
			uCard.setNickname(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.NICKNAME)));
			uCard.setIconPath(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.ICONPATH)));
			uCard.setIconHash(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.ICONHASH)));
		}
		if (cursor != null) {
			cursor.close();
		}
		return uCard;
	}
	
	/**
	 * 获取指定用户的名片
	 * @update 2014年10月24日 下午3:36:39
	 * @param user
	 * @return
	 */
	public UserVcard getUserVcardById(int cardId) {
		UserVcard uCard = null;
		Cursor cursor = mContext.getContentResolver().query(Uri.withAppendedPath(Provider.UserVcardColumns.CONTENT_URI, String.valueOf(cardId)), null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			uCard = new UserVcard();
			uCard.setId(cardId);
			uCard.setUserId(cursor.getInt(cursor.getColumnIndex(Provider.UserVcardColumns.USERID)));
			uCard.setNickname(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.NICKNAME)));
			uCard.setRealName(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.REALNAME)));
			uCard.setEmail(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.EMAIL)));
			uCard.setMobile(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.MOBILE)));
			uCard.setProvince(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.PROVINCE)));
			uCard.setCity(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.CITY)));
			uCard.setStreet(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.STREET)));
			uCard.setZipCode(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.ZIPCODE)));
			uCard.setIconPath(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.ICONPATH)));
			uCard.setIconHash(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.ICONHASH)));
		}
		if (cursor != null) {
			cursor.close();
		}
		return uCard;
	}
	
	/**
	 * 获取指定用户的名片
	 * @update 2014年10月24日 下午3:36:39
	 * @param user
	 * @return
	 */
	public UserVcard getUserVcardByUserId(int userId) {
		UserVcard uCard = null;
		Cursor cursor = mContext.getContentResolver().query(Provider.UserVcardColumns.CONTENT_URI, null, Provider.UserVcardColumns.USERID + " = ?", new String[] {String.valueOf(userId)}, null);
		if (cursor != null && cursor.moveToFirst()) {
			uCard = new UserVcard();
			uCard.setId(cursor.getInt(cursor.getColumnIndex(Provider.UserVcardColumns._ID)));
			uCard.setUserId(userId);
			uCard.setNickname(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.NICKNAME)));
			uCard.setRealName(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.REALNAME)));
			uCard.setEmail(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.EMAIL)));
			uCard.setMobile(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.MOBILE)));
			uCard.setProvince(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.PROVINCE)));
			uCard.setCity(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.CITY)));
			uCard.setStreet(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.STREET)));
			uCard.setZipCode(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.ZIPCODE)));
			uCard.setIconPath(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.ICONPATH)));
			uCard.setIconHash(cursor.getString(cursor.getColumnIndex(Provider.UserVcardColumns.ICONHASH)));
		}
		if (cursor != null) {
			cursor.close();
		}
		return uCard;
	}
	
	/**
	 * 加载本地好友的信息
	 * @update 2014年10月24日 下午4:47:38
	 * @param username
	 * @return
	 */
	public User loadLocalFriend(String username) {
		User user = null;
		String[] projection = {
				Provider.UserColumns._ID,
				Provider.UserColumns.NICKNAME,
				Provider.UserColumns.EMAIL,
				Provider.UserColumns.PHONE
		};
		Cursor cursor = mContext.getContentResolver().query(Provider.UserColumns.CONTENT_URI, projection, Provider.UserColumns.USERNAME + " = ?", new String[] {username}, null);
		if (cursor != null && cursor.moveToFirst()) {	//能够给加载到本地好友的数据
			user = new User();
			user.setId(cursor.getInt(cursor.getColumnIndex(Provider.UserColumns._ID)));
			user.setUsername(username);
			user.setNickname(cursor.getString(cursor.getColumnIndex(Provider.UserColumns.NICKNAME)));
			user.setPhone(cursor.getString(cursor.getColumnIndex(Provider.UserColumns.PHONE)));
			user.setEmail(cursor.getString(cursor.getColumnIndex(Provider.UserColumns.EMAIL)));
			user.setJID(user.initJID(user.getUsername()));
			
			//加载好友的本地电子名片
			UserVcard uCard = getUserVcardByUserId(user.getId());
			if (uCard != null) {
				user.setUserVcard(uCard);
			}
		}
		if (cursor != null) {
			cursor.close();
		}
		return user;
	}
	
	/**
	 * 是否是本地好友
	 * @update 2014年10月24日 下午8:51:13
	 * @param username
	 * @return
	 */
	public boolean isLocalFriend(String username) {
		boolean flag = false;
		Cursor cursor = mContext.getContentResolver().query(Provider.UserColumns.CONTENT_URI, new String[] {Provider.UserColumns._ID}, Provider.UserColumns.USERNAME + " = ?", new String[] {username}, null);
		if (cursor != null && cursor.moveToFirst()) {
			int id = cursor.getInt(0);
			if (id > 0) {
				flag = true;
			}
		}
		if (cursor != null) {
			cursor.close();
		}
		return flag;
	}
	
	/**
	 * 获取个人本地数据库里的信息，一般用于首次登录同步数据的判断
	 * @update 2014年10月24日 下午7:21:42
	 * @param temp
	 * @return
	 */
	public Personal getLocalSelftInfo(Personal person) {
		Personal temp = null;
		Cursor cursor = mContext.getContentResolver().query(Provider.PersonalColums.CONTENT_URI, null, Provider.PersonalColums.USERNAME + " = ?", new String[] {person.getUsername()}, null);
		if (cursor != null && cursor.moveToFirst()) {
			temp = person;
			temp.setId(cursor.getInt(cursor.getColumnIndex(Provider.PersonalColums._ID)));
			temp.setNickname(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.NICKNAME)));
			temp.setEmail(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.EMAIL)));
			temp.setPhone(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.PHONE)));
			temp.setProvince(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.PROVINCE)));
			temp.setCity(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.CITY)));
			temp.setStreet(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.STREET)));
			temp.setRealName(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.REALNAME)));
			temp.setZipCode(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.ZIPCODE)));
			temp.setIconPath(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.ICONPATH)));
			temp.setIconHash(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.ICONHASH)));
		}
		if (cursor != null) {
			cursor.close();
		}
		return temp;
	}
	
	/**
	 * 更新个人信息
	 * @update 2014年10月24日 下午7:36:18
	 * @param person
	 */
	public void updatePersonInfo(Personal person) {
		ContentValues values = initPersonalContentVaules(person);
		mContext.getContentResolver().update(Uri.withAppendedPath(Provider.PersonalColums.CONTENT_URI, String.valueOf(person.getId())), values, null, null);
	}
	
	/**
	 * 更新个人的状态，一般用于个人的状态发生变化时调用，如刚登录、下载等等
	 * @update 2014年10月24日 下午7:38:41
	 * @param person
	 */
	public void updatePersonStatus(Personal person) {
		ContentValues values = new ContentValues();
		values.put(Provider.PersonalColums.STATUS, person.getStatus());
		values.put(Provider.PersonalColums.MODE, person.getMode());
		values.put(Provider.PersonalColums.RESOURCE, person.getResource());
		mContext.getContentResolver().update(Provider.PersonalColums.CONTENT_URI, values, Provider.PersonalColums.USERNAME + " = ?", new String[] {person.getUsername()});
	}
	
	/**
	 * 初始化PersonalContentVaules数据
	 * @update 2014年10月24日 下午7:13:50
	 * @param personal
	 * @return
	 */
	private ContentValues initPersonalContentVaules(Personal person) {
		ContentValues values = new ContentValues();
		values.put(Provider.PersonalColums.USERNAME, person.getUsername());
		values.put(Provider.PersonalColums.NICKNAME, person.getNickname());
		values.put(Provider.PersonalColums.PASSWORD, person.getPassword());
		values.put(Provider.PersonalColums.REALNAME, person.getRealName());
		values.put(Provider.PersonalColums.EMAIL, person.getEmail());
		values.put(Provider.PersonalColums.PHONE, person.getPhone());
		values.put(Provider.PersonalColums.MODE, person.getMode());
		values.put(Provider.PersonalColums.PROVINCE, person.getProvince());
		values.put(Provider.PersonalColums.RESOURCE, person.getResource());
		values.put(Provider.PersonalColums.STATUS, person.getStatus());
		values.put(Provider.PersonalColums.CITY, person.getCity());
		values.put(Provider.PersonalColums.STREET, person.getStreet());
		values.put(Provider.PersonalColums.ZIPCODE, person.getZipCode());
		values.put(Provider.PersonalColums.ICONPATH, person.getIconPath());
		values.put(Provider.PersonalColums.ICONHASH, person.getIconHash());
		return values;
	}
	
	/**
	 * 初始化当前用户的个人信息，用户刚登录或者注册
	 * @param person
	 */
	public Personal saveOrUpdateCurrentUser(final Personal person) {
		Cursor cursor = mContext.getContentResolver().query(Provider.PersonalColums.CONTENT_URI, null, Provider.PersonalColums.USERNAME + " = ?", new String[] {person.getUsername()}, null);
		ContentValues values = initPersonalContentVaules(person);
		if (cursor != null && cursor.moveToFirst()) {	//有数据，直接赋值返回
			mContext.getContentResolver().update(Provider.PersonalColums.CONTENT_URI, values, Provider.PersonalColums.USERNAME + " = ?", new String[] {person.getUsername()});
		} else {	//没有数据，插入数据
			Uri uri = mContext.getContentResolver().insert(Provider.PersonalColums.CONTENT_URI, values);
			person.setId(Integer.parseInt(uri.getLastPathSegment()));
		}
		if (cursor != null) {
			cursor.close();
		}
		return person;
	}
	
	/**
	 * 将cursor转换为对象
	 * @update 2014年11月10日 下午10:13:11
	 * @param cursor
	 * @return
	 */
	private NewFriendInfo cursoToInfo(Cursor cursor) {
		NewFriendInfo newFriendInfo = new NewFriendInfo();
		newFriendInfo.setId(cursor.getInt(cursor.getColumnIndex(Provider.NewFriendColumns._ID)));
		newFriendInfo.setFriendStatus(NewFriendInfo.FriendStatus.valueOf(cursor.getInt(cursor.getColumnIndex(Provider.NewFriendColumns.FRIEND_STATUS))));
		newFriendInfo.setCreationDate(cursor.getLong(cursor.getColumnIndex(Provider.NewFriendColumns.CREATION_DATE)));
		newFriendInfo.setContent(cursor.getString(cursor.getColumnIndex(Provider.NewFriendColumns.CONTENT)));
		newFriendInfo.setFrom(cursor.getString(cursor.getColumnIndex(Provider.NewFriendColumns.FROM_USER)));
		newFriendInfo.setTo(cursor.getString(cursor.getColumnIndex(Provider.NewFriendColumns.TO_USER)));
		newFriendInfo.setIconHash(cursor.getString(cursor.getColumnIndex(Provider.NewFriendColumns.ICON_HASH)));
		newFriendInfo.setIconPath(cursor.getString(cursor.getColumnIndex(Provider.NewFriendColumns.ICON_PATH)));
		int userId = cursor.getInt(cursor.getColumnIndex(Provider.NewFriendColumns.USER_ID));
		User user = getUserById(userId);
		newFriendInfo.setUser(user);
		return newFriendInfo;
	}
	
	/**
	 * 获取新的朋友列表信息
	 * @update 2014年11月10日 下午2:48:16
	 * @return
	 */
	public List<NewFriendInfo> getNewFriendInfos() {
		List<NewFriendInfo> list = null;
		Cursor cursor = mContext.getContentResolver().query(Provider.NewFriendColumns.CONTENT_URI, null, null, null, null);
		if (cursor != null) {
			list = new ArrayList<>();
			while (cursor.moveToNext()) {
				NewFriendInfo newFriendInfo = cursoToInfo(cursor);
				
				list.add(newFriendInfo);
			}
			
			cursor.close();
		}
		return list;
	}
	
	/**
	 * 根据新的新的朋友信息id获得其详细信息
	 * @update 2014年11月10日 下午10:04:20
	 * @param infoId
	 * @return
	 */
	public NewFriendInfo getNewFriendInfoById(int infoId) {
		Uri uri = ContentUris.withAppendedId(Provider.NewFriendColumns.CONTENT_URI, infoId);
		return getNewFriendInfoByUri(uri);
	}
	
	/**
	 * 根据新的新的朋友信息uri获得其详细信息
	 * @update 2014年11月11日 下午3:53:35
	 * @param uri
	 * @return
	 */
	public NewFriendInfo getNewFriendInfoByUri(Uri uri) {
		NewFriendInfo newInfo = null;
//		Uri uri = ContentUris.withAppendedId(Provider.NewFriendColumns.CONTENT_URI, infoId);
		Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			newInfo = cursoToInfo(cursor);
		}
		if (cursor != null) {
			cursor.close();
		}
		return newInfo;
	}
	
	/**
	 * 根据新的新的朋友信息id获得其详细信息
	 * @update 2014年11月10日 下午10:04:20
	 * @param from
	 * @param to
	 * @return
	 */
	public NewFriendInfo getNewFriendInfoByAccounts(String from, String to) {
		NewFriendInfo newInfo = null;
		Cursor cursor = mContext.getContentResolver().query(Provider.NewFriendColumns.CONTENT_URI, null, Provider.NewFriendColumns.FROM_USER + " = ? and " + Provider.NewFriendColumns.TO_USER + " = ?", new String[] {from, to}, null);
		if (cursor != null && cursor.moveToFirst()) {
			newInfo = cursoToInfo(cursor);
		}
		if (cursor != null) {
			cursor.close();
		}
		return newInfo;
	}
	
	/**
	 * 初始化新的朋友的数据库值
	 * @update 2014年11月10日 下午3:18:40
	 * @param newInfo
	 * @return
	 */
	private ContentValues initNewFriendSimpleContentValues(NewFriendInfo newInfo) {
		ContentValues values = new ContentValues();
		values.put(Provider.NewFriendColumns.FRIEND_STATUS, newInfo.getFriendStatus().ordinal());
		values.put(Provider.NewFriendColumns.CONTENT, newInfo.getContent());
		return values;
	}
	
	/**
	 * 初始化新的朋友的数据库值
	 * @update 2014年11月10日 下午3:18:40
	 * @param newInfo
	 * @return
	 */
	private ContentValues initNewFriendContentValues(NewFriendInfo newInfo) {
		ContentValues values = new ContentValues();
		values.put(Provider.NewFriendColumns.CREATION_DATE, newInfo.getCreationDate());
		values.put(Provider.NewFriendColumns.FRIEND_STATUS, newInfo.getFriendStatus().ordinal());
		values.put(Provider.NewFriendColumns.CONTENT, newInfo.getContent());
		values.put(Provider.NewFriendColumns.FROM_USER, newInfo.getFrom());
		values.put(Provider.NewFriendColumns.TO_USER, newInfo.getTo());
		values.put(Provider.NewFriendColumns.ICON_HASH, newInfo.getIconHash());
		values.put(Provider.NewFriendColumns.ICON_PATH, newInfo.getIconPath());
		User user = newInfo.getUser();
		int id = 0;
		if (user != null) {
			id = user.getId();
		}
		values.put(Provider.NewFriendColumns.USER_ID, id);
		return values;
	}
	
	/**
	 * 
	 * @update 2014年11月10日 下午3:24:54
	 * @param newInfo
	 * @return
	 */
	public NewFriendInfo addNewFriendInfo(NewFriendInfo newInfo) {
		if (newInfo == null) {
			return null;
		}
		ContentValues values = initNewFriendContentValues(newInfo);
		Uri uri = mContext.getContentResolver().insert(Provider.NewFriendColumns.CONTENT_URI, values);
		if (uri != null) {
			int id = Integer.parseInt(uri.getLastPathSegment());
			newInfo.setId(id);
		}
		return newInfo;
	}
	
	/**
	 * 保存或添加新的朋友信息
	 * @update 2014年11月10日 下午10:02:52
	 * @param newInfo
	 * @return
	 */
	public NewFriendInfo saveOrUpdateNewFriendInfo(NewFriendInfo newInfo) {
		if (newInfo == null) {
			return null;
		}
		NewFriendInfo temp = getNewFriendInfoByAccounts(newInfo.getFrom(), newInfo.getTo());
		if (temp == null) {	//不存在，则添加
			newInfo = addNewFriendInfo(newInfo);
		} else {	//更新
			newInfo.setId(temp.getId());
			User user = newInfo.getUser();
			if (user == null) {
				newInfo.setUser(temp.getUser());
			}
			newInfo = updateNewFriendInfo(newInfo);
		}
		return newInfo;
	}
	
	/**
	 * 根据id删除新的朋友信息
	 * @update 2014年11月10日 下午3:29:47
	 * @param infoId
	 * @return 是否删除成功
	 */
	public boolean deleteNewFriendInfo(int infoId) {
		Uri uri = ContentUris.withAppendedId(Provider.NewFriendColumns.CONTENT_URI, infoId);
		int count = mContext.getContentResolver().delete(uri, null, null);
		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 更新新的朋友的信息的状态
	 * @update 2014年11月10日 下午3:17:14
	 * @param newInfo
	 * @return
	 */
	public NewFriendInfo updateNewFriendInfoState(NewFriendInfo newInfo) {
		if (newInfo == null) {
			return null;
		}
		ContentValues values = initNewFriendSimpleContentValues(newInfo);
		Uri uri = ContentUris.withAppendedId(Provider.NewFriendColumns.CONTENT_URI, newInfo.getId());
		mContext.getContentResolver().update(uri, values, null, null);
		return newInfo;
	}
	
	/**
	 * 更新新的朋友的信息
	 * @update 2014年11月10日 下午3:17:14
	 * @param newInfo
	 * @return
	 */
	public NewFriendInfo updateNewFriendInfo(NewFriendInfo newInfo) {
		if (newInfo == null) {
			return null;
		}
		ContentValues values = initNewFriendContentValues(newInfo);
		Uri uri = ContentUris.withAppendedId(Provider.NewFriendColumns.CONTENT_URI, newInfo.getId());
		mContext.getContentResolver().update(uri, values, null, null);
		return newInfo;
	}
}