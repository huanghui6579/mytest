package com.example.chat.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.example.chat.model.Personal;
import com.example.chat.provider.Provider;

/**
 * 核心的service服务，主要用来同步联系人数据
 * @author coolpad
 *
 */
public class CoreService extends Service {
	private MainBinder mBinder = new MainBinder();
	
	/**
	 * 初始化当前用户的个人信息
	 */
	public void initCurrentUser(final Personal person) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Cursor cursor = getContentResolver().query(Provider.PersonalColums.CONTENT_URI, null, "username = ?", new String[] {person.getUsername()}, null);
				if (cursor != null && cursor.moveToFirst()) {	//有数据，直接赋值返回
					person.setId(cursor.getInt(cursor.getColumnIndex(Provider.PersonalColums._ID)));
					person.setNickname(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.NICKNAME)));
					person.setEmail(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.EMAIL)));
					person.setPhone(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.PHONE)));
					person.setRealName(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.REALNAME)));
					person.setResource(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.RESOURCE)));
					person.setMode(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.MODE)));
					person.setProvince(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.PROVINCE)));
					person.setStatus(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.STATUS)));
					person.setZipCode(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.ZIPCODE)));
					person.setCity(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.CITY)));
					person.setStreet(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.STREET)));
					person.setIconPath(cursor.getString(cursor.getColumnIndex(Provider.PersonalColums.ICONPATH)));
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
					Uri uri = getContentResolver().insert(Provider.PersonalColums.CONTENT_URI, values);
					person.setId(Integer.parseInt(uri.getLastPathSegment()));
				}
			}
		}).start();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	public class MainBinder extends Binder {
		public CoreService getService() {
			return CoreService.this;
		}
	}

}
