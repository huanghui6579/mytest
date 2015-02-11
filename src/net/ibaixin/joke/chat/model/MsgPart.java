package net.ibaixin.joke.chat.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 消息的附件类
 * 
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月29日 上午10:07:09
 */
public class MsgPart implements Parcelable, Cloneable {
	/**
	 * 主键
	 */
	private int id;
	/**
	 * 文件所属的消息，依赖于{@link MsgInfo}的id
	 */
	private int msgId;
	/**
	 * 文件名称，不含有路径名称，但含有文件的格式后缀名
	 */
	private String fileName;
	/**
	 * 文件的本地存放的全名称，由目录名和文件名组成
	 */
	private String filePath;
	/**
	 * 文件的大小
	 */
	private long size;
	/**
	 * 文件的类型
	 */
	private String mimeTye;
	
	/**
	 * 文件创建时间
	 */
	private long creationDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMsgId() {
		return msgId;
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

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

	public String getMimeTye() {
		return mimeTye;
	}

	public void setMimeTye(String mimeTye) {
		this.mimeTye = mimeTye;
	}

	public long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + msgId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MsgPart other = (MsgPart) obj;
		if (id != other.id)
			return false;
		if (msgId != other.msgId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MsgPart [id=" + id + ", msgId=" + msgId + ", fileName="
				+ fileName + ", filePath=" + filePath + ", size=" + size
				+ ", mimeTye=" + mimeTye + ", creationDate=" + creationDate
				+ "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		MsgPart part = null;
		try {
			part = (MsgPart) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return part;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(msgId);
		dest.writeString(fileName);
		dest.writeString(filePath);
		dest.writeString(mimeTye);
		dest.writeLong(size);
		dest.writeLong(creationDate);
	}
	
	public MsgPart() {
	}
	
	public MsgPart(Parcel in) {
		id = in.readInt();
		msgId = in.readInt();
		fileName = in.readString();
		filePath = in.readString();
		mimeTye = in.readString();
		size = in.readLong();
		creationDate = in.readLong();
	}
	
	public static final Parcelable.Creator<MsgPart> CREATOR = new Creator<MsgPart>() {
		
		@Override
		public MsgPart[] newArray(int size) {
			return new MsgPart[size];
		}
		
		@Override
		public MsgPart createFromParcel(Parcel source) {
			return new MsgPart(source);
		}
	};
}
