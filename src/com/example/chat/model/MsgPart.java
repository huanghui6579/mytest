package com.example.chat.model;

/**
 * 消息的附件类
 * 
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年10月29日 上午10:07:09
 */
public class MsgPart {
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

	@Override
	public String toString() {
		return "MsgPart [id=" + id + ", msgId=" + msgId + ", fileName="
				+ fileName + ", filePath=" + filePath + ", size=" + size
				+ ", mimeTye=" + mimeTye + "]";
	}
}
