package net.ibaixin.joke.chat.db;

import net.ibaixin.joke.chat.provider.Provider;
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
	private static final String DB_NAME = "joke_chat.db";
	private static final int DB_VERSION = 6;

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//创建用户表
		db.execSQL("CREATE TABLE " + Provider.UserColumns.TABLE_NAME + " ("
				+ Provider.UserColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ Provider.UserColumns.USERNAME + " TEXT UNIQUE NOT NULL, "
				+ Provider.UserColumns.NICKNAME + " TEXT, "
				+ Provider.UserColumns.EMAIL + " TEXT, "
				+ Provider.UserColumns.PHONE + " TEXT, "
				+ Provider.UserColumns.RESOURCE + " TEXT, "
				+ Provider.UserColumns.STATUS + " TEXT, "
				+ Provider.UserColumns.MODE + " TEXT, "
				+ Provider.UserColumns.FULLPINYIN + " TEXT, "
				+ Provider.UserColumns.SHORTPINYIN + " TEXT, "
				+ Provider.UserColumns.SORTLETTER + " TEXT);");
		
		//创建用户名片表
		db.execSQL("CREATE TABLE " + Provider.UserVcardColumns.TABLE_NAME + " ("
				+ Provider.UserVcardColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Provider.UserVcardColumns.USERID + " INTEGER UNIQUE NOT NULL, "
				+ Provider.UserVcardColumns.NICKNAME + " TEXT, "
				+ Provider.UserVcardColumns.REALNAME + " TEXT, "
				+ Provider.UserVcardColumns.MOBILE + " TEXT, "
				+ Provider.UserVcardColumns.EMAIL + " TEXT, "
				+ Provider.UserVcardColumns.PROVINCE + " TEXT, "
				+ Provider.UserVcardColumns.STREET + " TEXT, "
				+ Provider.UserVcardColumns.CITY + " TEXT, "
				+ Provider.UserVcardColumns.ZIPCODE + " TEXT, "
				+ Provider.UserVcardColumns.ICONPATH + " TEXT, "
				+ Provider.UserVcardColumns.ICONHASH + " TEXT);");
		
		//创建个人信息表
		db.execSQL("CREATE TABLE " + Provider.PersonalColums.TABLE_NAME + " ("
				+ Provider.PersonalColums._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Provider.PersonalColums.USERNAME + " TEXT UNIQUE NOT NULL, "
				+ Provider.PersonalColums.PASSWORD + " TEXT NOT NULL, "
				+ Provider.PersonalColums.NICKNAME + " TEXT, "
				+ Provider.PersonalColums.REALNAME + " TEXT, "
				+ Provider.PersonalColums.EMAIL + " TEXT, "
				+ Provider.PersonalColums.PHONE + " TEXT, "
				+ Provider.PersonalColums.RESOURCE + " TEXT, "
				+ Provider.PersonalColums.STATUS + " TEXT, "
				+ Provider.PersonalColums.MODE + " TEXT, "
				+ Provider.PersonalColums.PROVINCE + " TEXT, "
				+ Provider.PersonalColums.STREET + " TEXT, "
				+ Provider.PersonalColums.CITY + " TEXT, "
				+ Provider.PersonalColums.ZIPCODE + " TEXT, "
				+ Provider.PersonalColums.ICONPATH + " TEXT, "
				+ Provider.PersonalColums.ICONHASH + " TEXT);");
		
		//创建聊天消息表
		db.execSQL("CREATE TABLE " + Provider.MsgInfoColumns.TABLE_NAME + " ("
				+ Provider.MsgInfoColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Provider.MsgInfoColumns.THREAD_ID + " INTEGER NOT NULL, "
				+ Provider.MsgInfoColumns.FROM_USER + " TEXT NOT NULL, "
				+ Provider.MsgInfoColumns.TO_USER + " TEXT NOT NULL, "
				+ Provider.MsgInfoColumns.CONTENT + " TEXT, "
				+ Provider.MsgInfoColumns.SUBJECT + " TEXT, "
				+ Provider.MsgInfoColumns.CREATIO_NDATE + " LONG, "
				+ Provider.MsgInfoColumns.IS_COMMING + " INTEGER, "
				+ Provider.MsgInfoColumns.IS_READ + " INTEGER, "
				+ Provider.MsgInfoColumns.MSG_TYPE + " INTEGER, "
				+ Provider.MsgInfoColumns.SEND_STATE + " INTEGER);");
		
		//创建聊天消息的附件表
		db.execSQL("CREATE TABLE " + Provider.MsgPartColumns.TABLE_NAME + " ("
				+ Provider.MsgPartColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Provider.MsgPartColumns.MSG_ID + " INTEGER NOT NULL, "
				+ Provider.MsgPartColumns.FILE_NAME + " TEXT NOT NULL, "
				+ Provider.MsgPartColumns.FILE_PATH + " TEXT NOT NULL, "
				+ Provider.MsgPartColumns.SIZE + " LONG, "
				+ Provider.MsgPartColumns.CREATION_DATE + " LONG, "
				+ Provider.MsgPartColumns.MIME_TYE + " TEXT);");
		
		//创建聊天会话表
		db.execSQL("CREATE TABLE " + Provider.MsgThreadColumns.TABLE_NAME + " ("
				+ Provider.MsgThreadColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Provider.MsgThreadColumns.MSG_THREAD_NAME + " TEXT, "
				+ Provider.MsgThreadColumns.UNREAD_COUNT + " INTEGER, "
				+ Provider.MsgThreadColumns.MODIFY_DATE + " LONG, "
				+ Provider.MsgThreadColumns.SNIPPET_ID + " INTEGER, "
				+ Provider.MsgThreadColumns.SNIPPET_CONTENT + " TEXT, "
				+ Provider.MsgThreadColumns.MEMBER_IDS + " TEXT);");
		
		//创建新的朋友列表
		db.execSQL("CREATE TABLE " + Provider.NewFriendColumns.TABLE_NAME + " ("
				+ Provider.NewFriendColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Provider.NewFriendColumns.USER_ID + " INTEGER UNIQUE NOT NULL, "
				+ Provider.NewFriendColumns.FRIEND_STATUS + " INTEGER DEFAULT 0, "
				+ Provider.NewFriendColumns.CONTENT + " TEXT, "
				+ Provider.NewFriendColumns.FROM_USER + " TEXT UNIQUE NOT NULL, "
				+ Provider.NewFriendColumns.TO_USER + " TEXT UNIQUE NOT NULL, "
				+ Provider.NewFriendColumns.ICON_HASH + " TEXT, "
				+ Provider.NewFriendColumns.ICON_PATH + " TEXT, "
				+ Provider.NewFriendColumns.CREATION_DATE + " LONG);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + Provider.UserColumns.TABLE_NAME);  
        db.execSQL("DROP TABLE IF EXISTS " + Provider.UserVcardColumns.TABLE_NAME);  
        db.execSQL("DROP TABLE IF EXISTS " + Provider.PersonalColums.TABLE_NAME);  
        db.execSQL("DROP TABLE IF EXISTS " + Provider.MsgInfoColumns.TABLE_NAME);  
        db.execSQL("DROP TABLE IF EXISTS " + Provider.MsgPartColumns.TABLE_NAME);  
        db.execSQL("DROP TABLE IF EXISTS " + Provider.MsgThreadColumns.TABLE_NAME);  
        db.execSQL("DROP TABLE IF EXISTS " + Provider.NewFriendColumns.TABLE_NAME);  
        onCreate(db);
	}

}
