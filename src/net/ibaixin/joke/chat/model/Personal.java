package net.ibaixin.joke.chat.model;

import net.ibaixin.joke.chat.util.Constants;

import org.jxmpp.util.XmppStringUtils;

import android.text.TextUtils;

/**
 * 个人信息
 * 
 * @author coolpad
 *
 */
public class Personal {
	/**
	 * 主键id
	 */
	private int id;
	/**
	 * 个人的账号
	 */
	private String username;
	/**
	 * 个人的密码
	 */
	private String password;
	/**
	 * 个人的昵称
	 */
	private String nickname;
	/**
	 * 个人真实姓名
	 */
	private String realName;
	/**
	 * 个人邮箱
	 */
	private String email;
	/**
	 * 个人手机号码
	 */
	private String phone;
	/**
	 * 个人的登录资源，像QQ一样，用什么设备登录的，如Android、iPhone、web
	 */
	private String resource;
	/**
	 * 在个人状态的基础上的签名，在线时标记“吃饭中”等动态信息
	 */
	private String status;
	/**
	 * 个人登录的状态，如隐身、在线、空闲等等
	 */
	private String mode;
	/**
	 * 个人具体的街道地址
	 */
	private String street;
	/**
	 * 个人所在的城市
	 */
	private String city;
	/**
	 * 个人所在的省份
	 */
	private String province;
	/**
	 * 个人所在地址的邮编
	 */
	private String zipCode;
	/**
	 * 个人的头像本地存储路径
	 */
	private String iconPath;
	/**
	 * 头像的hash值，通过本地hash值与服务器的hash对比来判断用不用重新更新头像
	 */
	private String iconHash;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}
	
	public String getIconHash() {
		return iconHash;
	}

	public void setIconHash(String iconHash) {
		this.iconHash = iconHash;
	}

	/**
	 * 获得用户的jid
	 * @return
	 */
	public String getJID() {
		return username + "@" + Constants.SERVER_NAME;
	}
	
	@Override
	public String toString() {
		return "Personal [id=" + id + ", username=" + username + ", password="
				+ password + ", nickname=" + nickname + ", realName="
				+ realName + ", email=" + email + ", phone=" + phone
				+ ", resource=" + resource + ", status=" + status + ", mode="
				+ mode + ", street=" + street + ", city=" + city
				+ ", province=" + province + ", zipCode=" + zipCode
				+ ", iconPath=" + iconPath + ", iconHash=" + iconHash + "]";
	}

	public String getFullJID() {
		/*if (TextUtils.isEmpty(resource)) {
			return getJID();
		} else {
			return getJID() + "/" + resource;
		}*/
		return XmppStringUtils.completeJidFrom(username, Constants.SERVER_NAME, resource);
	}
	
	/**
	 * 判断该类是否为空
	 * @update 2015年1月21日 下午5:52:47
	 * @return
	 */
	public boolean isEmpty() {
		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
			return true;
		} else {
			return false;
		}
	}
}
