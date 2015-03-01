package net.ibaixin.chat.model;

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
	private String nickname;
	/**
	 * 用户的真实姓名
	 */
	private String realName;
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

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
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

	public String getIconHash() {
		return iconHash;
	}

	public void setIconHash(String iconHash) {
		this.iconHash = iconHash;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String toString() {
		return "UserVcard [id=" + id + ", userId=" + userId + ", nickname="
				+ nickname + ", realName=" + realName + ", email=" + email
				+ ", street=" + street + ", city=" + city + ", province="
				+ province + ", zipCode=" + zipCode + ", mobile=" + mobile
				+ ", iconPath=" + iconPath + ", iconHash=" + iconHash + "]";
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(userId);
		dest.writeString(nickname); 
//		dest.writeString(realName); 
//		dest.writeString(email);
//		dest.writeString(street);
//		dest.writeString(city);
//		dest.writeString(province);
//		dest.writeString(zipCode);
//		dest.writeString(mobile);
		dest.writeString(iconPath);
		dest.writeString(iconHash);
	}
	
	public UserVcard() {}
	
	public UserVcard(Parcel in) {
		id = in.readInt();
		userId = in.readInt();
		nickname = in.readString();
//		realName = in.readString();
//		email = in.readString();
//		street = in.readString();
//		city = in.readString();
//		province = in.readString();
//		zipCode = in.readString();
//		mobile = in.readString();
		iconPath = in.readString();
		iconHash = in.readString();
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
