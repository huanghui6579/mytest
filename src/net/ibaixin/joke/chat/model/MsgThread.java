package net.ibaixin.joke.chat.model;

import java.util.Comparator;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 消息的会话，一个会话包含多条消息，一个人与另一个人之间只有一个会话
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月30日 下午4:50:41
 */
public class MsgThread implements Parcelable, Comparator<MsgThread> {
	/**
	 * 主键
	 */
	private int id;
	/**
	 * 会话的名称，一般以对方的昵称为名
	 */
	private String msgThreadName;
	/**
	 * 会话的图标
	 */
	private Bitmap icon;
	/**
	 * 该会话内未读消息的数量
	 */
	private int unReadCount;
	
	/**
	 * 最后更新时间
	 */
	private long modifyDate;
	
	/**
	 * 最后一条消息的id
	 */
	private int snippetId;
	
	/**
	 * 最后一条消息的内容，主要用户会话列表的展示
	 */
	private String snippetContent;
	
	/**
	 * 该会话里的成员，<b>自己除外</b>
	 */
	private List<User> members;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMsgThreadName() {
		return msgThreadName;
	}

	public void setMsgThreadName(String msgThreadName) {
		this.msgThreadName = msgThreadName;
	}

	public Bitmap getIcon() {
		return icon;
	}

	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}

	public int getUnReadCount() {
		return unReadCount;
	}

	public void setUnReadCount(int unReadCount) {
		this.unReadCount = unReadCount;
	}

	public long getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(long modifyDate) {
		this.modifyDate = modifyDate;
	}

	public int getSnippetId() {
		return snippetId;
	}

	public void setSnippetId(int snippetId) {
		this.snippetId = snippetId;
	}

	public String getSnippetContent() {
		return snippetContent;
	}

	public void setSnippetContent(String snippetContent) {
		this.snippetContent = snippetContent;
	}

	public List<User> getMembers() {
		return members;
	}

	public void setMembers(List<User> members) {
		this.members = members;
	}

	@Override
	public String toString() {
		return "MsgThread [id=" + id + ", msgThreadName=" + msgThreadName
				+ ", icon=" + icon + ", unReadCount=" + unReadCount
				+ ", modifyDate=" + modifyDate + ", snippetId=" + snippetId
				+ ", snippetContent=" + snippetContent + ", members=" + members
				+ "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(msgThreadName);
	}
	
	public MsgThread(Parcel in) {
		id = in.readInt();
		msgThreadName = in.readString();
	}
	
	public MsgThread() {
	}
	
	public static final Parcelable.Creator<MsgThread> CREATOR = new Creator<MsgThread>() {
		
		@Override
		public MsgThread[] newArray(int size) {
			return new MsgThread[size];
		}
		
		@Override
		public MsgThread createFromParcel(Parcel source) {
			return new MsgThread(source);
		}
	};

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		MsgThread other = (MsgThread) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public int compare(MsgThread lhs, MsgThread rhs) {
		long ltime = lhs.getModifyDate();
		long rtime = rhs.getModifyDate();
		if (ltime > rtime) {
			return -1;
		} else if (ltime < rtime) {
			return 1;
		} else {
			return 0;
		}
	}

}
