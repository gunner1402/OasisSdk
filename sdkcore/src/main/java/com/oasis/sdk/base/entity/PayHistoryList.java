package com.oasis.sdk.base.entity;

import java.util.List;

/**
 * 支付历史记录集合
 * @author Administrator
 *
 */
public class PayHistoryList {
	public int page;
	public int page_size;
	public String game_code;
	public String uid;
	public List<PayHistoryInfoDetail> msg;
	public void setPage(int page) {
		this.page = page;
	}
	public void setPage_size(int page_size) {
		this.page_size = page_size;
	}
	public void setGame_code(String game_code) {
		this.game_code = game_code;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public void setMsg(List<PayHistoryInfoDetail> msg) {
		this.msg = msg;
	}
	
	
}
