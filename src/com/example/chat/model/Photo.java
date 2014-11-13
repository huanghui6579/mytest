package com.example.chat.model;

/**
 * 相片实体
 * @author huanghui1
 * @version 1.0.0
 * @update 2014年11月13日 下午5:58:41
 */
public class Photo {
	/**
	 * 文件名称
	 */
	private String fileName;
	/**
	 * 文件的全路径
	 */
	private String filePath;
	/**
	 * 该图片是否被选中
	 */
	private boolean checked = false;
	/**
	 * 该文件所在的父目录名称
	 */
	private String parentName;
	/**
	 * 文件的大小
	 */
	private long size;

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

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
}
