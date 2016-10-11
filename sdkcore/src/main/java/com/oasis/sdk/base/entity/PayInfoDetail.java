package com.oasis.sdk.base.entity;

import java.io.Serializable;

/**
 * 支付套餐详细信息
 * @author xdb
 *
 */
public class PayInfoDetail implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1598605839985469091L;
	
	public String id;			//套餐id
	public String orderId;		//OAS订单id
	public String amount;		//价格
	public String amount_show;	//显示价格
	public String fb_credit;			//
	public String game_coins;			//游戏币
	public String game_coins_show;		//显示游戏币
	public String currency;				//货币
	public String currency_show;		//显示货币
	public String game_code;
	public String pay_way;				//支付渠道
	public String country;				//国家，按照该国家获取套餐
	public String price_original;		//原始价格
	public String price_now;			//当前价格
	public String price_discount;		//折扣价格
	public String price_type;			//
	public String price_product_id;		// Google商品ID
	public String best;					//推荐
	public String is_vip;				//

	public void setId(String id) {
		this.id = id;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public void setFb_credit(String fb_credit) {
		this.fb_credit = fb_credit;
	}
	public void setGame_coins(String game_coins) {
		this.game_coins = game_coins;
	}
	public void setGame_coins_show(String game_coins_show) {
		this.game_coins_show = game_coins_show;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public void setCurrency_show(String currency_show) {
		this.currency_show = currency_show;
	}
	public void setAmount_show(String amount_show) {
		this.amount_show = amount_show;
	}
	public void setGame_code(String game_code) {
		this.game_code = game_code;
	}
	public void setPay_way(String pay_way) {
		this.pay_way = pay_way;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public void setPrice_original(String price_original) {
		this.price_original = price_original;
	}
	public void setPrice_now(String price_now) {
		this.price_now = price_now;
	}
	public void setPrice_discount(String price_discount) {
		this.price_discount = price_discount;
	}
	public void setPrice_type(String price_type) {
		this.price_type = price_type;
	}
	public void setBest(String best) {
		this.best = best;
	}
	public void setPrice_product_id(String price_product_id) {
		this.price_product_id = price_product_id;
	}
	public void setIs_vip(String is_vip) {
		this.is_vip = is_vip;
	}
	
	
}
