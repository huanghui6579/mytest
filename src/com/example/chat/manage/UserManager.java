package com.example.chat.manage;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.example.chat.ChatApplication;
import com.example.chat.model.Personal;
import com.example.chat.model.User;
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
				
				users.add(user);
			}
			cursor.close();
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
				ContentResolver cr = mContext.getContentResolver();
				ContentValues values = new ContentValues();
				values.put(Provider.UserColumns.USERNAME, user.getUsername());
				values.put(Provider.UserColumns.NICKNAME, user.getNickname());
				values.put(Provider.UserColumns.EMAIL, user.getEmail());
				values.put(Provider.UserColumns.PHONE, user.getPhone());
				values.put(Provider.UserColumns.RESOURCE, user.getResource());
				values.put(Provider.UserColumns.STATUS, user.getStatus());
				values.put(Provider.UserColumns.MODE, user.getMode());
				values.put(Provider.UserColumns.FULLPINYIN, user.getFullPinyin());
				values.put(Provider.UserColumns.SHORTPINYIN, user.getShortPinyin());
				values.put(Provider.UserColumns.SORTLETTER, user.getSortLetter());
				int count = cr.update(Provider.UserColumns.CONTENT_URI, values, "username = ?", new String[] {user.getUsername()});
				if (count <= 0) {	//本身该条记录不存在就新增，若存在就直接更新
					cr.insert(Provider.UserColumns.CONTENT_URI, values);
				}
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
	 * 初始化当前用户的个人信息，用户刚登录或者注册
	 * @param person
	 */
	public void initCurrentUser(final Personal person) {
		
		Cursor cursor = mContext.getContentResolver().query(Provider.PersonalColums.CONTENT_URI, null, "username = ?", new String[] {person.getUsername()}, null);
		if (cursor != null && cursor.moveToFirst()) {	//有数据，直接赋值返回
			ContentValues values = new ContentValues();
			values.put(Provider.PersonalColums.MODE, person.getMode());
			values.put(Provider.PersonalColums.STATUS, person.getStatus());
			values.put(Provider.PersonalColums.RESOURCE, person.getResource());
			mContext.getContentResolver().update(Provider.PersonalColums.CONTENT_URI, values, "username = ?", new String[] {person.getUsername()});
			
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
			Uri uri = mContext.getContentResolver().insert(Provider.PersonalColums.CONTENT_URI, values);
			person.setId(Integer.parseInt(uri.getLastPathSegment()));
		}
	}
}