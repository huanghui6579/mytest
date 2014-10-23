package com.example.chat.manage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.example.chat.ChatApplication;
import com.example.chat.model.Personal;
import com.example.chat.model.User;
import com.example.chat.model.UserVcard;
import com.example.chat.provider.Provider;

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
				Cursor cardCursor = mContext.getContentResolver().query(Provider.UserVcardColumns.CONTENT_URI, new String[] {Provider.UserVcardColumns._ID, Provider.UserVcardColumns.ICONPATH, Provider.UserVcardColumns.ICONHASH}, Provider.UserVcardColumns.USERID + " = ?", new String[] {String.valueOf(user.getId())}, null);
				if (cardCursor != null && cardCursor.moveToFirst()) {	//查询该好友对应的电子名片
					UserVcard uCard = new UserVcard();
					uCard.setId(cardCursor.getInt(cardCursor.getColumnIndex(Provider.UserVcardColumns._ID)));
					uCard.setUserId(user.getId());
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
			if (uCard != null) {
//				ContentValues cardValues = initUserVcardContentVaules(uCard);
//				mContext.getContentResolver().update(Uri.withAppendedPath(Provider.UserVcardColumns.CONTENT_URI, String.valueOf(uCard.getId())), cardValues, null, null);
				saveOrUpdateUserVacard(uCard);
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
	 * 更新或保存好友的名片
	 * @update 2014年10月23日 下午8:20:43
	 * @param uCard
	 * @return
	 */
	public UserVcard saveOrUpdateUserVacard(UserVcard uCard) {
		Cursor cursor = mContext.getContentResolver().query(Provider.UserVcardColumns.CONTENT_URI, new String[] {Provider.UserVcardColumns._ID}, Provider.UserVcardColumns.USERID + " = ?", new String[] {Provider.UserVcardColumns.USERID}, null);
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
		cardValues.put(Provider.UserVcardColumns.NICKNAME, uCard.getNickame());
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
	 * 初始化当前用户的个人信息，用户刚登录或者注册
	 * @param person
	 */
	public void initCurrentUser(final Personal person) {
		
		Cursor cursor = mContext.getContentResolver().query(Provider.PersonalColums.CONTENT_URI, null, Provider.PersonalColums.USERNAME + " = ?", new String[] {person.getUsername()}, null);
		if (cursor != null && cursor.moveToFirst()) {	//有数据，直接赋值返回
			ContentValues values = new ContentValues();
			values.put(Provider.PersonalColums.MODE, person.getMode());
			values.put(Provider.PersonalColums.STATUS, person.getStatus());
			values.put(Provider.PersonalColums.RESOURCE, person.getResource());
			mContext.getContentResolver().update(Provider.PersonalColums.CONTENT_URI, values, Provider.PersonalColums.USERNAME + " = ?", new String[] {person.getUsername()});
			
			person.setId(cursor.getInt(cursor.getColumnIndex(Provider.PersonalColums._ID)));
			person.setNickname(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.NICKNAME)));
			person.setEmail(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.EMAIL)));
			person.setPhone(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.PHONE)));
			person.setRealName(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.REALNAME)));
			person.setProvince(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.PROVINCE)));
			person.setZipCode(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.ZIPCODE)));
			person.setCity(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.CITY)));
			person.setStreet(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.STREET)));
			person.setIconPath(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.ICONPATH)));
			person.setIconHash(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.ICONHASH)));
			cursor.close();
		} else {	//没有数据，插入数据
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
			Uri uri = mContext.getContentResolver().insert(Provider.PersonalColums.CONTENT_URI, values);
			person.setId(Integer.parseInt(uri.getLastPathSegment()));
		}
	}
}