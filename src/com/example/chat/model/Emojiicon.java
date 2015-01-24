package com.example.chat.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * emoji表情的实体类
 * @author huanghui1
 * @version 1.0.0
 * @update 2015年1月23日 上午9:16:30
 */
public class Emojiicon implements Parcelable {
	/**
	 * 表情的资源id
	 */
	private int icon;
	/**
	 * 表情的ASCII码
	 */
	private char value;
	/**
	 * 表情的对应的字符串值
	 */
	private String emoji;

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public char getValue() {
		return value;
	}

	public void setValue(char value) {
		this.value = value;
	}

	public String getEmoji() {
		return emoji;
	}

	public void setEmoji(String emoji) {
		this.emoji = emoji;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(icon);
		dest.writeCharArray(new char[] {value});
		dest.writeString(emoji);
	}
	
	public Emojiicon() {}
	
	public Emojiicon(Parcel in) {
		icon = in.readInt();
	}
}
