package com.oasis.sdk.base.entity;

import java.io.Serializable;

/**
 * 支付套餐配置信息
 * @author xdb
 *
 */
public class PayConfigInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1598605839985469091L;
	
	public String id;		//套餐id
	public String game_code;
	public String pay_way_code;
	public String country_code2;
	public String country_code3;
	public String language;
	public String currency;
	public String project_id;
	public String project_key;
	public String project_type;
	public String email_receive;
	public String email_support;
	public String email_notification;
	public String merchant_name;
	public String merchant_pwd;
	public String action_address;
	public String action_callback;
	public String action_success;
	public String config_1;
	public String config_2;
	public String config_3;
	
	public void setId(String id) {
		this.id = id;
	}
	public void setGame_code(String game_code) {
		this.game_code = game_code;
	}
	public void setPay_way_code(String pay_way_code) {
		this.pay_way_code = pay_way_code;
	}
	public void setCountry_code2(String country_code2) {
		this.country_code2 = country_code2;
	}
	public void setCountry_code3(String country_code3) {
		this.country_code3 = country_code3;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public void setProject_id(String project_id) {
		this.project_id = project_id;
	}
	public void setProject_key(String project_key) {
		this.project_key = project_key;
	}
	public void setProject_type(String project_type) {
		this.project_type = project_type;
	}
	public void setEmail_receive(String email_receive) {
		this.email_receive = email_receive;
	}
	public void setEmail_support(String email_support) {
		this.email_support = email_support;
	}
	public void setEmail_notification(String email_notification) {
		this.email_notification = email_notification;
	}
	public void setMerchant_name(String merchant_name) {
		this.merchant_name = merchant_name;
	}
	public void setMerchant_pwd(String merchant_pwd) {
		this.merchant_pwd = merchant_pwd;
	}
	public void setAction_address(String action_address) {
		this.action_address = action_address;
	}
	public void setAction_callback(String action_callback) {
		this.action_callback = action_callback;
	}
	public void setAction_success(String action_success) {
		this.action_success = action_success;
	}
	public void setConfig_1(String config_1) {
		this.config_1 = config_1;
	}
	public void setConfig_2(String config_2) {
		this.config_2 = config_2;
	}
	public void setConfig_3(String config_3) {
		this.config_3 = config_3;
	}
	
	
	
}
