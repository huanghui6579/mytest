package com.example.chat.model;

/**
 * 新的朋友实体
 * @author Administrator
 * @update 2014年11月9日 下午2:30:42
 * @version 1.0.0
 */
public class NewFriendInfo {
	/**
	 * 主键
	 */
	private int id;
	/**
	 * 用户实体信息
	 */
	private User user;
	/**
	 * 好友请求的状态，默认是UNADD
	 */
	private FriendStatus friendStatus = FriendStatus.UNADD;
	/**
	 * 描述信息
	 */
	private String content;
	/**
	 * 创建时间
	 */
	private long creationDate;

	/**
	 * 好友请求的状态，目前分为四种：<br />
	 * <ul>
	 * 	<li>UNADD--未添加，此时是陌生人</li>
	 * 	<li>ADDED--已经添加，此时已经是好友</li>
	 * 	<li>VERIFYING--自己像对方发送添加好友的请求，对方还没有答应或回应/li>
	 * 	<li>ACCEPT--对方向自己发送添加好友的请求，自己还没有答应或者回应</li>
	 * </ul>
	 * @author Administrator
	 * @update 2014年11月9日 下午2:45:44
	 * @version 1.0.0
	 *
	 */
	enum FriendStatus {
		/**
		 * 未添加，此时是陌生人
		 */
		UNADD,
		/**
		 * 已经添加，此时已经是好友
		 */
		ADDED,
		/**
		 * 自己像对方发送添加好友的请求，对方还没有答应或回应
		 */
		VERIFYING,
		/**
		 * 对方向自己发送添加好友的请求，自己还没有答应或者回应
		 */
		ACCEPT;
		
		public static FriendStatus valueOf(int value) {
			switch (value) {
			case 0:
				return UNADD;
			case 1:
				return ADDED;
			case 2:
				return VERIFYING;
			case 3:
				return ACCEPT;

			default:
				return UNADD;
			}
		}
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public FriendStatus getFriendStatus() {
		return friendStatus;
	}

	public void setFriendStatus(FriendStatus friendStatus) {
		this.friendStatus = friendStatus;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}
	
}
