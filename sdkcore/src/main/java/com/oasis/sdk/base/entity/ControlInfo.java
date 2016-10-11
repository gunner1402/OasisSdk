package com.oasis.sdk.base.entity;

import java.io.Serializable;

import android.content.Context;

import com.oasis.sdk.base.utils.BaseUtils;

/**
 * SDK控制开关信息
 * @author xdb
 *
 */
public class ControlInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1598605839985469091L;
	
	public String og_onoff_control;				//og按钮开关
	public String reg_onoff_control;			//注册、关联按钮开关
	public String switching_onoff_control;		//切换账号按钮开关
	public String charge_onoff_control;			//充值按钮开关
	public String share_onoff_control;			//分享按钮开关
	public String forum_onoff_control;			//论坛按钮开关
	public String network_condition;			//网络条件
	public String pc_charge_condition;			//PC充值条件
	public String custom_onoff_control;			//客服开关
	public String userinfo_onoff_control;		//账户资料开关
	public String epin_onoff_control;			//Epin码支付开关
	public String history_logininfo_control;	//登录历史账号开关
	
	public Boolean getOg_onoff_control() {
		return check(og_onoff_control);
	}
	public void setOg_onoff_control(String og_onoff_control) {
		this.og_onoff_control = og_onoff_control;
	}
	public Boolean getReg_onoff_control() {
		return check(reg_onoff_control);
	}
	public void setReg_onoff_control(String reg_onoff_control) {
		this.reg_onoff_control = reg_onoff_control;
	}
	public Boolean getSwitching_onoff_control() {
		return check(switching_onoff_control);
	}
	public void setSwitching_onoff_control(String switching_onoff_control) {
		this.switching_onoff_control = switching_onoff_control;
	}
	public Boolean getCharge_onoff_control(Context c) {
		if(check(charge_onoff_control))
			return getNetwork_condition(c);
		return false;
	}
	public void setCharge_onoff_control(String charge_onoff_control) {
		this.charge_onoff_control = charge_onoff_control;
	}
	public Boolean getShare_onoff_control() {
		return check(share_onoff_control);
	}
	public void setShare_onoff_control(String share_onoff_control) {
		this.share_onoff_control = share_onoff_control;
	}
	public Boolean getForum_onoff_control() {
		return check(forum_onoff_control);
	}
	public void setForum_onoff_control(String forum_onoff_control) {
		this.forum_onoff_control = forum_onoff_control;
	}
	public void setNetwork_condition(String network_condition) {
		this.network_condition = network_condition;
	}
	public Boolean getNetwork_condition(Context c) {
		if("1".equals(network_condition))
			return true;
		else if("2".equals(network_condition) && !BaseUtils.isWifiNetworkAvailable(c))// 非Wifi环境
			return true;
		else if("3".equals(network_condition) && BaseUtils.isWifiNetworkAvailable(c))// 必须为Wifi环境
			return true;
		
		return false;
	}
	public void setPc_charge_condition(String pc_charge_condition) {
		this.pc_charge_condition = pc_charge_condition;
	}
	
	public Boolean getCustom_onoff_control() {
		return check(custom_onoff_control);
	}
	public void setCustom_onoff_control(String custom_onoff_control) {
		this.custom_onoff_control = custom_onoff_control;
	}
	public Boolean getUserinfo_onoff_control() {
		return check(userinfo_onoff_control);
	}
	public void setUserinfo_onoff_control(String userinfo_onoff_control) {
		this.userinfo_onoff_control = userinfo_onoff_control;
	}
	
	public Boolean getEpin_onoff_control() {
		return check(epin_onoff_control);
	}
	public void setEpin_onoff_control(String epin_onoff_control) {
		this.epin_onoff_control = epin_onoff_control;
	}
	
	public boolean getHistory_logininfo_control() {
		return check(history_logininfo_control);
	}
	public void setHistory_logininfo_control(String history_logininfo_control) {
		this.history_logininfo_control = history_logininfo_control;
	}
	/**
	 * 充值开关 与 PC充值 开关 取交集
	 * @return
	 */
	public Boolean getPc_charge_condition() {
		return check(charge_onoff_control) & check(pc_charge_condition);
	}
	private boolean check(String value){
		if("1".equals(value))
			return true;
		return false;
//		return true;
	}
}
