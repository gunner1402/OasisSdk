package com.oasis.sdk.base.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Facebook 好友列表的翻页信息
 * @author Administrator
 *
 */
public class FBPageInfo implements Serializable{
	private static final long serialVersionUID = 1L;

	public int limit;//每次获取记录数

//	public int total_count ;// 好友总数（包含未玩过该应用的好友）
	
	public List<FriendInfo> data;
	
	public boolean hasNext;

	public boolean hasPrevious;
	
//	public int getTotal_count() {
//		return total_count;
//	}
//
//	public void setTotal_count(int total_count) {
//		this.total_count = total_count;
//	}


	public void setData(List<FriendInfo> data) {
		this.data = data;
	}

	public boolean isHasNext() {
		return hasNext;
	}

	public void setHasNext(boolean hasNext) {
		this.hasNext = hasNext;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public boolean isHasPrevious() {
		return hasPrevious;
	}

	public void setHasPrevious(boolean hasPrevious) {
		this.hasPrevious = hasPrevious;
	}
	
	
//	public String next;


}
