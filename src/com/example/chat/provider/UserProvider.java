package com.example.chat.provider;

import java.util.HashMap;
import java.util.Map;

import com.example.chat.db.DatabaseHelper;
import com.example.chat.util.Log;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * 用户数据库provider
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月13日 上午11:16:06
 */
public class UserProvider extends ContentProvider {
	private static final int USERS = 1;
	private static final int USER_ID = 2;
	private static final int USER_CONDITION = 3;
	
	private static final int USER_VCARDS = 4;
	private static final int USER_VCARD_ID = 5;

	private static final UriMatcher mUriMatcher;
	
	private static Map<String, String> mUserProjectionMap = null;
	private static Map<String, String> mUserVcardProjectionMap = null;
	
	private DatabaseHelper mDBHelper;
	
	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(Provider.AUTHORITY, "users", USERS);
		mUriMatcher.addURI(Provider.AUTHORITY, "users/#", USER_ID);
		mUriMatcher.addURI(Provider.AUTHORITY, "users/search/*", USER_CONDITION);
		mUriMatcher.addURI(Provider.AUTHORITY, "userVcards", USER_VCARDS);
		mUriMatcher.addURI(Provider.AUTHORITY, "userVcards/#", USER_VCARD_ID);
		
		mUserProjectionMap = new HashMap<String, String>();
		mUserProjectionMap.put(Provider.UserColumns._ID, Provider.UserColumns._ID);
		mUserProjectionMap.put(Provider.UserColumns.JID, Provider.UserColumns.JID);
		mUserProjectionMap.put(Provider.UserColumns.USERNAME, Provider.UserColumns.USERNAME);
		mUserProjectionMap.put(Provider.UserColumns.PASSWORD, Provider.UserColumns.PASSWORD);
		mUserProjectionMap.put(Provider.UserColumns.EMAIL, Provider.UserColumns.EMAIL);
		mUserProjectionMap.put(Provider.UserColumns.NICKNAME, Provider.UserColumns.NICKNAME);
		mUserProjectionMap.put(Provider.UserColumns.PHONE, Provider.UserColumns.PHONE);
		mUserProjectionMap.put(Provider.UserColumns.RESOURCE, Provider.UserColumns.RESOURCE);
		mUserProjectionMap.put(Provider.UserColumns.STATUS, Provider.UserColumns.STATUS);
		mUserProjectionMap.put(Provider.UserColumns.FULLPINYIN, Provider.UserColumns.FULLPINYIN);
		mUserProjectionMap.put(Provider.UserColumns.SHORTPINYIN, Provider.UserColumns.SHORTPINYIN);
		mUserProjectionMap.put(Provider.UserColumns.SORTLETTER, Provider.UserColumns.SORTLETTER);
		
		mUserVcardProjectionMap = new HashMap<String, String>();
		mUserVcardProjectionMap.put(Provider.UserVcardColumns._ID, Provider.UserVcardColumns._ID);
		mUserVcardProjectionMap.put(Provider.UserVcardColumns.USERID, Provider.UserVcardColumns.USERID);
		mUserVcardProjectionMap.put(Provider.UserVcardColumns.NICKNAME, Provider.UserVcardColumns.NICKNAME);
		mUserVcardProjectionMap.put(Provider.UserVcardColumns.FIRSTNAME, Provider.UserVcardColumns.FIRSTNAME);
		mUserVcardProjectionMap.put(Provider.UserVcardColumns.MIDDLENAME, Provider.UserVcardColumns.MIDDLENAME);
		mUserVcardProjectionMap.put(Provider.UserVcardColumns.LASTNAME, Provider.UserVcardColumns.LASTNAME);
		mUserVcardProjectionMap.put(Provider.UserVcardColumns.EMAIL, Provider.UserVcardColumns.EMAIL);
		mUserVcardProjectionMap.put(Provider.UserVcardColumns.CITY, Provider.UserVcardColumns.CITY);
		mUserVcardProjectionMap.put(Provider.UserVcardColumns.PROVINCE, Provider.UserVcardColumns.PROVINCE);
		mUserVcardProjectionMap.put(Provider.UserVcardColumns.ZIPCODE, Provider.UserVcardColumns.ZIPCODE);
		mUserVcardProjectionMap.put(Provider.UserVcardColumns.MOBILE, Provider.UserVcardColumns.MOBILE);
		mUserVcardProjectionMap.put(Provider.UserVcardColumns.ICONPATH, Provider.UserVcardColumns.ICONPATH);
	}

	@Override
	public boolean onCreate() {
		mDBHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String orderBy = null;
		switch (mUriMatcher.match(uri)) {
		case USERS:		//查询所有用户
		case USER_ID:	//根据id查找用户
		case USER_CONDITION:
			qb.setTables(Provider.UserColumns.TABLE_NAME);
			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = Provider.UserColumns.DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}
			break;
		case USER_VCARDS:	//查询所有的名片
		case USER_VCARD_ID:	//根据id查找名片
			qb.setTables(Provider.UserVcardColumns.TABLE_NAME);
			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = Provider.UserVcardColumns.DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}
			break;
		default:
			break;
		}
		switch (mUriMatcher.match(uri)) {
		case USERS:
			qb.setProjectionMap(mUserProjectionMap);
			break;
		case USER_ID:
			qb.setProjectionMap(mUserProjectionMap);
			qb.appendWhere(Provider.UserColumns._ID + " = " + uri.getLastPathSegment());
			break;
		case USER_CONDITION:
			qb.setProjectionMap(mUserProjectionMap);
			String condition = uri.getLastPathSegment();
			StringBuilder sb = new StringBuilder();
			sb.append(Provider.UserColumns.JID).append(" like '").append(condition).append("%' or ")
				.append(Provider.UserColumns.USERNAME).append(" like '").append(condition).append("%' or ")
				.append(Provider.UserColumns.EMAIL).append(" like '").append(condition).append("%' or ")
				.append(Provider.UserColumns.NICKNAME).append(" like '").append(condition).append("%' ");
			qb.appendWhere(sb.toString());
			break;
		case USER_VCARDS:
			qb.setProjectionMap(mUserVcardProjectionMap);
			break;
		case USER_VCARD_ID:
			qb.setProjectionMap(mUserVcardProjectionMap);
			qb.appendWhere(Provider.UserVcardColumns.USERID + " = " + uri.getLastPathSegment());
			break;

		default:
			break;
		}
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case USERS:
		case USER_VCARDS:
		case USER_CONDITION:
			return Provider.CONTENT_TYPE;
		case USER_ID:
		case USER_VCARD_ID:
			return Provider.CONTENT_ITEM_TYPE;
		default:
			break;
		}
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		ContentValues cv = null;
		if (values != null) {
			cv = new ContentValues(values);
		} else {
			cv = new ContentValues();
		}
		String tableName = "";
		String nullColumn = "";
		switch (mUriMatcher.match(uri)) {
		case USERS:
			tableName = Provider.UserColumns.TABLE_NAME;
			nullColumn = Provider.UserColumns.USERNAME;
			
			if (!cv.containsKey(Provider.UserColumns.JID)) {
				cv.put(Provider.UserColumns.JID, "");
			}
			
			if (!cv.containsKey(Provider.UserColumns.USERNAME)) {
				cv.put(Provider.UserColumns.USERNAME, "");
			}
			
			if (!cv.containsKey(Provider.UserColumns.PASSWORD)) {
				cv.put(Provider.UserColumns.PASSWORD, "");
			}
			break;
		case USER_VCARDS:
			tableName = Provider.UserVcardColumns.TABLE_NAME;
			nullColumn = Provider.UserVcardColumns.USERID;
			
			if (!cv.containsKey(Provider.UserVcardColumns.USERID)) {
				cv.put(Provider.UserVcardColumns.USERID, "");
			}
			break;
		default:
			break;
		}
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			long rowId = db.insert(tableName, nullColumn, cv);
			if (rowId > 0) {
				Uri noteUri = ContentUris.withAppendedId(uri, rowId);
				getContext().getContentResolver().notifyChange(noteUri, null);
				db.setTransactionSuccessful();
				return noteUri;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(e.getMessage());
		} finally {
			db.endTransaction();
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		int count = 0;
		try {
			db.beginTransaction();
			switch (mUriMatcher.match(uri)) {
			case USERS:
				count = db.delete(Provider.UserColumns.TABLE_NAME, selection, selectionArgs);
				break;
			case USER_ID:
				count = db.delete(Provider.UserColumns.TABLE_NAME, Provider.UserColumns._ID + " = " + uri.getLastPathSegment() + (TextUtils.isEmpty(selection) ? "" : " and (" + selection + ")"), selectionArgs);
				break;
			case USER_VCARDS:
				count = db.delete(Provider.UserVcardColumns.TABLE_NAME, selection, selectionArgs);
				break;
			case USER_VCARD_ID:
				count = db.delete(Provider.UserVcardColumns.TABLE_NAME, Provider.UserVcardColumns.USERID + " = " + uri.getLastPathSegment() + (TextUtils.isEmpty(selection) ? "" : " and (" + selection + ")"), selectionArgs);
				break;
			default:
				break;
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(e.getMessage());
		} finally {
			db.endTransaction();
		}
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		int count = 0;
		db.beginTransaction();
		try {
			switch (mUriMatcher.match(uri)) {
			case USERS:
				count = db.update(Provider.UserColumns.TABLE_NAME, values, selection, selectionArgs);
				break;
			case USER_ID:
				count = db.update(Provider.UserColumns.TABLE_NAME, values, Provider.UserColumns._ID + " = " + uri.getLastPathSegment() + (TextUtils.isEmpty(selection) ? "" : " and (" + selection + ")"), selectionArgs);
				break;
			case USER_VCARDS:
				count = db.update(Provider.UserVcardColumns.TABLE_NAME, values, selection, selectionArgs);
				break;
			case USER_VCARD_ID:
				count = db.update(Provider.UserVcardColumns.TABLE_NAME, values, Provider.UserVcardColumns.USERID + " = " + uri.getLastPathSegment() + (TextUtils.isEmpty(selection) ? "" : " and (" + selection + ")"), selectionArgs);
				break;
			default:
				break;
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(e.getMessage());
		} finally {
			db.endTransaction();
		}
		return count;
	}

}
