package com.example.chat.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 表情的分类，如经典表情、大表情等
 * 
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月27日 下午5:57:55
 */
public class EmojiType implements Parcelable {
	/**
	 * 表情
	 */
	public static final int OPT_EMOJI = 1;
	/**
	 * 本地表情管理
	 */
	public static final int OPT_MANAGE = 2;
	/**
	 * 表情添加
	 */
	public static final int OPT_ADD = 3;
	
	private int resId;
	private String fileName;
	private String description;
	private int optType = OPT_EMOJI;

	public int getResId() {
		return resId;
	}

	public void setResId(int resId) {
		this.resId = resId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getOptType() {
		return optType;
	}

	public void setOptType(int optType) {
		this.optType = optType;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(resId);
		dest.writeString(fileName);
		dest.writeString(description);
		dest.writeInt(optType);
	}
	
	public EmojiType() {
	}
	
	public EmojiType(Parcel in) {
		resId = in.readInt();
		fileName = in.readString();
		description = in.readString();
		optType = in.readInt();
	}
	
	public static final Parcelable.Creator<EmojiType> CREATOR = new Creator<EmojiType>() {

		@Override
		public EmojiType createFromParcel(Parcel source) {
			return new EmojiType(source);
		}

		@Override
		public EmojiType[] newArray(int size) {
			return new EmojiType[size];
		}
	};

}
