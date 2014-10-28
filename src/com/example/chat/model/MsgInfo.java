package com.example.chat.model;

/**
 * 聊天消息实体类
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月28日 下午9:25:58
 */
public class MsgInfo {
	/**
	 * 主键
	 */
	private int id;
	/**
	 * 会话id
	 */
	private String threadID;
	/**
	 * 发送人jid,格式为xxx@domain或者xxx@domain/resource
	 */
	private String fromJid;
	/**
	 * 收件人jid,格式为xxx@domain或者xxx@domain/resource
	 */
	private String toJid;
	/**
	 * 消息内容
	 */
	private String content;
	/**
	 * 消息主题
	 */
	private String subject;
	
	/**
	 * 消息创建时间
	 */
	private long creationDate;
	
	/**
	 * 是否是进来的消息
	 */
	private boolean isComming;
	
	/**
	 * 消息的类型，默认是文本消息
	 */
	private Type msgType = Type.TEXT;
	
	/**
	 * 消息的分类，主要有:
	 * <ul>
	 * 	<li>Type.TEXT -- 普通文本</li>
	 * 	<li>Type.IMAGE -- 图片</li>
	 * 	<li>Type.AUDIO -- 音频</li>
	 * 	<li>Type.VIDEO -- 视频</li>
	 * 	<li>Type.LOCATION -- 地理位置</li>
	 * 	<li>Type.VCARD -- 名片</li>
	 * 	<li>Type.FILE -- 文件</li>
	 * </ul>
	 * @author huanghui1
	 * @update 2014年10月28日 下午9:57:28
	 */
	public enum Type {
		/**
		 * 普通文本
		 */
		TEXT,
		IMAGE,
		AUDIO,
		VIDEO,
		LOCATION,
		VCARD,
		FILE;
		
		/**
		 * 将数字转换成Type
		 * @update 2014年10月28日 下午10:14:57
		 * @param value
		 * @return
		 */
		public static Type valueOf(int value) {
			switch (value) {
			case 0:
				return TEXT;
			case 1:
				return IMAGE;
			case 2:
				return AUDIO;
			case 3:
				return VIDEO;
			case 4:
				return LOCATION;
			case 5:
				return VCARD;
			case 6:
				return FILE;
			default:
				return TEXT;
			}
		}
	}
	
}
