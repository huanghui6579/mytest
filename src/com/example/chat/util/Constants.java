package com.example.chat.util;

/**
 * 一些常量
 * @author Administrator
 * @version 2014年10月7日 上午10:15:33
 */
public class Constants {
	private Constants() {}
	
	/*
	 * <!-- <string name="server_host">192.168.0.102</string> -->
    <string name="server_host">172.16.45.16</string>
    <integer name="server_port">5222</integer>
    <!-- <string name="server_name">tiger.com</string> -->
    <string name="server_name">huanghui4.com</string>
	 */
//	public static final String SERVER_HOST = "192.168.0.102";
//	public static final String SERVER_HOST = "172.16.45.16";
	public static final String SERVER_HOST = "192.168.1.50";
//	public static final String SERVER_NAME = "tiger.com";
//	public static final String SERVER_NAME = "huanghui4.com";
	public static final String SERVER_NAME = "localhost-pc.com";
	public static final int SERVER_PORT = 5222;
	
	public static final String SETTTING_LOGIN = "settting_login";
	public static final String USER_ACCOUNT = "user_account";
	public static final String USER_PASSWORD = "user_password";
	public static final String USER_ISFIRST = "user_isfirst";
	public static final String USER_RESOURCE = "user_resource";
	
	public static final String NAME_SERVER_HOST = "name_server_host";
	public static final String NAME_SERVER_PORT = "name_server_port";
	public static final String NAME_SERVER_NAME = "name_server_name";
	
	/**
	 * 【1】显示好友头像，主要在UserInfoActivity中用到
	 */
	public static final int MSG_SHOW_USR_ICON = 0x00001;
	/**
	 * 【2】服务器连接不可用
	 */
	public static final int MSG_CONNECTION_UNAVAILABLE = 0x00002;
	/**
	 * 【3】发送添加好友的请求
	 */
	public static final int MSG_SEND_ADD_FRIEND_REQUEST = 0x00003;
}
