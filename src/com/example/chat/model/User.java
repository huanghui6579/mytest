package com.example.chat.model;

import java.util.Comparator;

import opensource.jpinyin.PinyinFormat;
import opensource.jpinyin.PinyinHelper;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * 用户实体
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月9日 下午9:19:08
 */
public class User implements Parcelable, Comparator<User> {
	public static final String TAG_OTHER = "#";
	
	/**
	 * 主键id
	 */
	private int id;
	/**
	 * 用户的完整openfire标识，格式为xxx@domain，如admin@localhost.com
	 */
	private String JID;
	/**
	 * 用户的账号，不含有@以及之后的后缀，如admin
	 */
	private String username;
	/**
	 * 用户的邮箱
	 */
	private String email;
	/**
	 * 用户昵称
	 */
	private String nickname;
	/**
	 * 用户的手机号码
	 */
	private String phone;
	/**
	 * 用户登录的资源，像QQ一样，用什么设备登录的，如Android、iPhone、web
	 */
	private String resource;
	/**
	 * 在用户状态的基础上的签名，在线时标记“吃饭中”等动态信息
	 */
	private String status;
	/**
	 * 用户登录的状态，如隐身、在线、空闲等等
	 */
	private String mode;
	/**
	 * 昵称的全拼，如张三的全拼:zhangsan
	 */
	private String fullPinyin;
	/**
	 * 昵称的简拼，如张三的简拼：zs
	 */
	private String shortPinyin;
	/**
	 * 用户的电子名片
	 */
	private UserVcard userVcard;
	
	/**
	 * 名字拼音的首字母，大写的
	 */
	private String sortLetter;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

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

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public UserVcard getUserVcard() {
		return userVcard;
	}

	public void setUserVcard(UserVcard userVcard) {
		this.userVcard = userVcard;
	}

	/**
	 * 获取名字拼音的首字母大写
	 * @update 2014年10月11日 下午9:51:07
	 * @return
	 */
	public String getSortLetter() {
//		if (TextUtils.isEmpty(sortLetter)) {
//			String sp = getShortPinyin().toUpperCase(Locale.getDefault());
//			return String.valueOf(sp.charAt(0));
//		}
		return sortLetter;
	}

	public void setSortLetter(String sortLetter) {
		this.sortLetter = sortLetter;
	}

	/**
	 * 获取用户的名称，默认是显示昵称，如没有昵称，则显示用户名
	 * @update 2014年10月11日 下午9:34:23
	 * @return
	 */
	public String getName() {
		if (TextUtils.isEmpty(nickname)) {
			return username;
		}
		return nickname;
	}

	public String getFullPinyin() {
		if(TextUtils.isEmpty(fullPinyin)) {
			if(!TextUtils.isEmpty(nickname)) {
				fullPinyin = PinyinHelper.getShortPinyin(nickname);
			} else {
				fullPinyin = username;
			}
		}
		return fullPinyin;
	}

	public String getShortPinyin() {
		if(TextUtils.isEmpty(shortPinyin)) {
			if(!TextUtils.isEmpty(nickname)) {
				shortPinyin = PinyinHelper.convertToPinyinString(nickname, "", PinyinFormat.WITHOUT_TONE);
			} else {
				shortPinyin = username;
			}
		}
		return shortPinyin;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(JID);
		dest.writeString(username);
		dest.writeString(email);
		dest.writeString(nickname);
		dest.writeString(phone);
		dest.writeString(resource);
		dest.writeString(status);
		dest.writeString(mode);
		dest.writeString(fullPinyin);
		dest.writeString(shortPinyin);
		dest.writeParcelable(userVcard, flags);
	}
	
	public User(Parcel in) {
		id = in.readInt();
		JID = in.readString();
		username = in.readString();
		email = in.readString();
		nickname = in.readString();
		phone = in.readString();
		resource = in.readString();
		status = in.readString();
		mode = in.readString();
		fullPinyin = in.readString();
		shortPinyin = in.readString();
		userVcard = in.readParcelable(null);
	}
	
	public User() {
	}

	public static final Parcelable.Creator<User> CREATOR = new Creator<User>() {
		
		@Override
		public User[] newArray(int size) {
			return new User[size];
		}
		
		@Override
		public User createFromParcel(Parcel source) {
			return new User(source);
		}
	};

	@Override
	public int compare(User lhs, User rhs) {
		if (TAG_OTHER.equals(lhs.getSortLetter())) {
			return 1;
		} else if (TAG_OTHER.equals(rhs.getSortLetter())) {
			return -1;
		} else {
			return lhs.getSortLetter().compareTo(rhs.getSortLetter());
		}
	}
}
