package com.oasis.sdk.base.entity;

import java.util.List;

/**
 * 支付套餐集合
 * @author Administrator
 *
 */
public class PayInfoList {
	public String pay_way;
	public PayConfigInfo pay_way_config;
	public List<PayInfoDetail> list;
	
	
	public void setPay_way(String pay_way) {
		this.pay_way = pay_way;
	}
	public void setPay_way_config(PayConfigInfo pay_way_config) {
		this.pay_way_config = pay_way_config;
	}
	public void setList(List list) {
		this.list = list;
	}
	
}
