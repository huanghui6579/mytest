package com.example.chat.model;

/**
 * 用户实体
 * 
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月9日 下午9:19:08
 */
public class User {
	private String JID;
	private String username;
	private String password;
	private String email;
	private String nickname;
	private String phone;
	private String resource;
	private String status;

	public String getJID() {
		return JID;
	}

	public void setJID(String jID) {
		JID = jID;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
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
}
