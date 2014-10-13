package com.example.chat.provider;

/**
 *
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月13日 上午11:31:24
 */
public class Provider {

	/**
	 * 用户的表字段
	 * @author huanghui1
	 * @update 2014年10月13日 上午11:36:35
	 */
	public static final class UserColumns {
		public static final String TABLE_NAME = "t_user";  
        public static final String DEFAULT_SORT_ORDER = "JID ASC";  
        
        public static final String JID = "JID";  
        public static final String USERNAME = "username";  
        public static final String PASSWORD = "password";  
        public static final String EMAIL = "email";  
        public static final String NICKNAME = "nickname";  
        public static final String PHONE = "phone";  
        public static final String RESOURCE = "resource";  
        public static final String STATUS = "status";  
        public static final String FULLPINYIN = "fullPinyin";  
        public static final String SHORTPINYIN = "shortPinyin";
        public static final String SORTLETTER = "sortLetter";
	}
	
	/**
	 * 用户名片表字段
	 * @author huanghui1
	 * @update 2014年10月13日 上午11:39:41
	 */
	public static final class UserVcardColumns {
		public static final String TABLE_NAME = "t_user_vcard";  
        public static final String DEFAULT_SORT_ORDER = "userId ASC";  
        
        public static final String USERID = "userId";  
        public static final String NICKNAME = "nickname";  
        public static final String FIRSTNAME = "firstName";  
        public static final String MIDDLENAME = "middleName";  
        public static final String LASTNAME = "lastName";  
        public static final String EMAIL = "email";  
        public static final String STREET = "street";  
        public static final String CITY = "city";  
        public static final String PROVINCE = "province";  
        public static final String ZIPCODE = "zipCode";
        public static final String MOBILE = "mobile";
        public static final String ICONPATH = "iconPath";
	}
}
