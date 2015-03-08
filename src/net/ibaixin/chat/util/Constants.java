package net.ibaixin.chat.util;

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
//	public static final String SERVER_HOST = "192.168.0.104";
//	public static final String SERVER_HOST = "172.16.45.16";
//	public static final String SERVER_HOST = "10.2.33.237";
//	public static final String SERVER_HOST = "192.168.42.25";
//	public static final String SERVER_HOST = "123.56.116.45";
//	public static final String SERVER_HOST = "www.ibaixin.net";
	public static final String SERVER_HOST = "192.168.0.103";
//	public static final String SERVER_HOST = "192.168.1.184";
//	public static final String SERVER_NAME = "tiger.com";
//	public static final String SERVER_NAME = "huanghui4.com";
//	public static final String SERVER_NAME = "ibaixin.net";
//	public static final String SERVER_NAME = "localhost-pc.com";
	public static final String SERVER_NAME = "home-pc.com";
	public static final int SERVER_PORT = 5222;
	/** web登录cookie*/
	public static String WEB_COOKIE = null;
	/**
	 * 用户的登录该系统的客户端类型，如Android、iphone、web等
	 */
	public static final String CLIENT_RESOURCE = SystemUtil.getPhoneModel();
	
	public static final String SETTTING_LOGIN = "settting_login";
	public static final String USER_ACCOUNT = "user_account";
	public static final String USER_PASSWORD = "user_password";
	public static final String USER_ISFIRST = "user_isfirst";
	public static final String USER_RESOURCE = "user_resource";
	
	public static final String NAME_SERVER_HOST = "name_server_host";
	public static final String NAME_SERVER_PORT = "name_server_port";
	public static final String NAME_SERVER_NAME = "name_server_name";
	
	/**
	 * 地理位置的分隔符，为“;”
	 */
	public static final String SPLITE_TAG_LOCATION = ";";
	
	/**
	 * 文件传输时，消息的类型分隔符，为“,”
	 */
	public static final String SPLITE_TAG_MSG_TYPE = ",";
	
	/**
	 * 会话的时间格式化模板
	 */
	public static final String DATEFORMA_TPATTERN_THREAD = "MM月dd日 HH:mm";
	
	/**
	 * 相册提示时间
	 */
	public static final String DATEFORMA_TPATTERN_ALBUM_TIP = "yyyy-MM-dd";
	
	/**
	 * 默认的标准换格式时间
	 */
	public static final String DATEFORMA_TPATTERN_DEFAULT = "yyyy-MM-dd HH:mm:ss";
	
	public static final String MIME_IMAGE = "image";
	public static final String MIME_VIDEO = "video";
	public static final String MIME_AUDIO = "audio";
	public static final String MIME_TEXT = "text";
	public static final String MIME_APK = "apk";
	public static final String MIME_FILE = "file";
	
	/**
	 * 复制文本到剪贴板的标签
	 */
	public static final String LABEL_COPY = "label_copy";
	
	/**
	 * 默认的聊天消息分页加载的页面大小
	 */
	public static final int PAGE_SIZE_MSG = 20;
	
	/**
	 * 表情每页的数量21
	 */
	public static final int PAGE_SIZE_EMOJI = 21;
	
	/**
	 * 默认的相册图片选择数量最多为9张
	 */
	public static final int ALBUM_SELECT_SIZE = 9;
	
	/**
	 * 系统默认手动生成的缩略图缩放参造宽度100
	 */
	public static final int IMAGE_THUMB_WIDTH = 100;
	
	/**
	 * 系统默认手动生成的缩略图缩放参造高度100
	 */
	public static final int IMAGE_THUMB_HEIGHT = 100;
	
	/**
	 * 地理位置截图的默认缩略图的宽度：200
	 */
	public static final int IMAGE_LOCATION_THUMB_WIDTH = 200;
	
	/**
	 * 地理位置截图的默认缩略图的宽度：200
	 */
	public static final int IMAGE_LOCATION_THUMB_HEIGHT = 200;
	
	/**
	 * 录音最小的长度为1秒
	 */
	public static final int COICE_RECORD_MIN_LENGTH = 1;
	
	/**
	 * 【1】成功的结果码
	 */
	public static final int MSG_SUCCESS = 0x000001;
	/**
	 * 【2】失败的结果码
	 */
	public static final int MSG_FAILED = 0x000002;
	/**
	 * 【3】显示好友头像，主要在UserInfoActivity中用到
	 */
	public static final int MSG_SHOW_USR_ICON = 0x000003;
	/**
	 * 【4】服务器连接不可用
	 */
	public static final int MSG_CONNECTION_UNAVAILABLE = 0x000004;
	/**
	 * 【5】发送添加好友的请求
	 */
	public static final int MSG_SEND_ADD_FRIEND_REQUEST = 0x000005;
	/**
	 * 【6】网络请求的地址不对
	 */
	public static final int MSG_REQUEST_ADDRESS_FAILED = 0x000006;
	/**
	 * 【7】用户已经登录过了
	 */
	public static final int MSG_REQUEST_ALREADY_LOGIN = 0x000007;
	/**
	 * 【8】服务器没有响应
	 */
	public static final int MSG_NO_RESPONSE = 0x000008;
	
	/**
	 * 【9】接收聊天消息
	 */
	public static final int MSG_RECEIVE_CHAT_MSG = 0x000009;
	
	/**
	 * 【10】改变聊天消息的发送状态
	 */
	public static final int MSG_MODIFY_CHAT_MSG_SEND_STATE = 0x00000A;
	
	/**
	 * 【11】聊天会话置顶成功
	 */
	public static final int MSG_THREAD_TOP_SUCCESS = 0x00000B;
	
	/**
	 * 【12】聊天会话置顶失败
	 */
	public static final int MSG_THREAD_TOP_FAILED = 0x00000C;
}
