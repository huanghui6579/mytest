package com.example.chat.model;

/**
 * 相片实体
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年11月13日 下午5:58:41
 */
public class PhotoItem {
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
}
