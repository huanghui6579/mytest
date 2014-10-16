package com.example.chat.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 用户名片（用户详细信息）
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月13日 上午10:55:58
 */
public class UserVcard implements Parcelable {
	private int id;
	/**
	 * 所属用户的名片，依赖于{@link User}的_ID
	 */
	private int userId;
	private String nickame;
	private String firstName;
	private String middleName;
	private String lastName;
	private String email;
	/**
	 * 街道地址
	 */
	private String street;
	private String city;
	private String province;
	private String zipCode;
	private String mobile;
	/**
	 * 头像的本地缓存路径
	 */
	private String iconPath;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getNickame() {
		return nickame;
	}

	public void setNickame(String nickame) {
		this.nickame = nickame;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(userId);
		dest.writeString(nickame);
		dest.writeString(firstName);
		dest.writeString(middleName);
		dest.writeString(lastName);
		dest.writeString(email);
		dest.writeString(street);
		dest.writeString(city);
		dest.writeString(province);
		dest.writeString(zipCode);
		dest.writeString(mobile);
		dest.writeString(iconPath);
	}
	
	public UserVcard() {}
	
	public UserVcard(Parcel in) {
		id = in.readInt();
		userId = in.readInt();
		nickame = in.readString();
		firstName = in.readString();
		middleName = in.readString();
		lastName = in.readString();
		email = in.readString();
		street = in.readString();
		city = in.readString();
		province = in.readString();
		zipCode = in.readString();
		mobile = in.readString();
		iconPath = in.readString();
	}
	
	public static final Parcelable.Creator<UserVcard> CREATOR = new Creator<UserVcard>() {
		
		@Override
		public UserVcard[] newArray(int size) {
			return new UserVcard[size];
		}
		
		@Override
		public UserVcard createFromParcel(Parcel source) {
			return new UserVcard(source);
		}
	};

}
