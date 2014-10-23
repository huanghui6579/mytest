package com.example.chat.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 *
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月13日 上午11:31:24
 */
public class Provider {
	public static final String AUTHORITY = "com.example.chat.provider.user";
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.example.chat";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.example.chat";

	/**
	 * 用户的表字段
	 * @author huanghui1
	 * @update 2014年10月13日 上午11:36:35
	 */
	public static final class UserColumns implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/users");
		public static final Uri CONTENT_SEARCH_URI = Uri.parse("content://" + AUTHORITY + "/users/search");
		
		public static final String TABLE_NAME = "t_user";
        public static final String DEFAULT_SORT_ORDER = "username ASC";
        
        public static final String USERNAME = "username";
        public static final String EMAIL = "email";
        public static final String NICKNAME = "nickname";
        public static final String PHONE = "phone";
        public static final String RESOURCE = "resource";
        public static final String STATUS = "status";
        public static final String MODE = "mode";
        public static final String FULLPINYIN = "fullPinyin";
        public static final String SHORTPINYIN = "shortPinyin";
        public static final String SORTLETTER = "sortLetter";
	}
	
	/**
	 * 个人信息的表字段
	 * @author coolpad
	 *
	 */
	public static final class PersonalColums implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/personals");
		
		public static final String TABLE_NAME = "t_personal";
        public static final String DEFAULT_SORT_ORDER = "username ASC";
        
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String NICKNAME = "nickname";
        public static final String REALNAME = "realName";
        public static final String EMAIL = "email";
        public static final String PHONE = "phone";
        public static final String RESOURCE = "resource";
        public static final String STATUS = "status";
        public static final String MODE = "mode";
        public static final String STREET = "street";
        public static final String CITY = "city";
        public static final String PROVINCE = "province";
        public static final String ZIPCODE = "zipCode";
        public static final String ICONPATH = "iconPath";
        public static final String ICONHASH = "iconHash";
	}
	
	/**
	 * 用户名片表字段
	 * @author huanghui1
	 * @update 2014年10月13日 上午11:39:41
	 */
	public static final class UserVcardColumns implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/userVcards");
		
		public static final String TABLE_NAME = "t_user_vcard";
        public static final String DEFAULT_SORT_ORDER = "userId ASC";
        
        public static final String USERID = "userId";
        public static final String NICKNAME = "nickname";
        public static final String REALNAME = "realName";
        public static final String EMAIL = "email";
        public static final String STREET = "street";
        public static final String CITY = "city";
        public static final String PROVINCE = "province";
        public static final String ZIPCODE = "zipCode";
        public static final String MOBILE = "mobile";
        public static final String ICONPATH = "iconPath";
        public static final String ICONHASH = "iconHash";
	}
}
