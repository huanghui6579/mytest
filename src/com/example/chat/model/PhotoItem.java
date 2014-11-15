package com.example.chat.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 相片实体
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年11月13日 下午5:58:41
 */
public class PhotoItem implements Parcelable {
	/**
	 * 文件的全路径
	 */
	private String filePath;
	/**
	 * 文件的大小
	 */
	private long size;
	
	private long time;

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(filePath);
		dest.writeLong(size);
		dest.writeLong(time);
	}
	
	@Override
	public String toString() {
		return "PhotoItem [filePath=" + filePath + ", size=" + size + ", time="
				+ time + "]";
	}

	public PhotoItem() {
	}
	
	public PhotoItem(Parcel in) {
		filePath = in.readString();
		size = in.readLong();
		time = in.readLong();
	}
	
	public static final Parcelable.Creator<PhotoItem> CREATOR = new Creator<PhotoItem>() {
		
		@Override
		public PhotoItem[] newArray(int size) {
			return new PhotoItem[size];
		}
		
		@Override
		public PhotoItem createFromParcel(Parcel source) {
			return new PhotoItem(source);
		}
	};
}
