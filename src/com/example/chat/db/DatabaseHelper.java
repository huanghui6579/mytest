package com.example.chat.db;

import com.example.chat.provider.Provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库创建
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月13日 上午11:26:25
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "example_chat.db";
	private static final int DB_VERSION = 1;

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Provider.UserColumns.TABLE_NAME + " ("
				+ Provider.UserColumns.JID + " TEXT PRIMARY KEY, "
				+ Provider.UserColumns.USERNAME + " TEXT, "
				+ Provider.UserColumns.PASSWORD + " TEXT, "
				+ Provider.UserColumns.NICKNAME + " TEXT, "
				+ Provider.UserColumns.EMAIL + " TEXT, "
				+ Provider.UserColumns.PHONE + " TEXT, "
				+ Provider.UserColumns.RESOURCE + " TEXT, "
				+ Provider.UserColumns.FULLPINYIN + " TEXT, "
				+ Provider.UserColumns.SHORTPINYIN + " TEXT, "
				+ Provider.UserColumns.SORTLETTER + " TEXT);");
		
		db.execSQL("CREATE TABLE " + Provider.UserVcardColumns.TABLE_NAME + " ("
				+ Provider.UserVcardColumns.USERID + " TEXT PRIMARY KEY, "
				+ Provider.UserVcardColumns.NICKNAME + " TEXT, "
				+ Provider.UserVcardColumns.FIRSTNAME + " TEXT, "
				+ Provider.UserVcardColumns.MIDDLENAME + " TEXT, "
				+ Provider.UserVcardColumns.LASTNAME + " TEXT, "
				+ Provider.UserVcardColumns.MOBILE + " TEXT, "
				+ Provider.UserVcardColumns.EMAIL + " TEXT, "
				+ Provider.UserVcardColumns.PROVINCE + " TEXT, "
				+ Provider.UserVcardColumns.STREET + " TEXT, "
				+ Provider.UserVcardColumns.CITY + " TEXT, "
				+ Provider.UserVcardColumns.ZIPCODE + " TEXT, "
				+ Provider.UserVcardColumns.ICONPATH + " TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + Provider.UserColumns.TABLE_NAME);  
        db.execSQL("DROP TABLE IF EXISTS " + Provider.UserVcardColumns.TABLE_NAME);  
        onCreate(db); 
	}

}
