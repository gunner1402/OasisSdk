package com.oasis.sdk.base.entity;

import java.util.Map;

import android.text.TextUtils;

import com.oasis.sdk.OASISPlatformConstant;

/**
 * 数据上报信息
 * @author Administrator
 *
 */
public class ReportAdjustInfo extends ReportInfo{
	public static String EVENTNAME_REVENUE = "revenue";
	public double incent;				//收入，美分
	public String currency;				//货币
	
	public ReportAdjustInfo(String eventName, double incent, String currency, Map<String, String> params) {
		super.type = OASISPlatformConstant.REPORT_TYPE_ADJUST;
		if(incent > 0 && TextUtils.isEmpty(eventName))
			super.eventName = EVENTNAME_REVENUE;
		else
			super.eventName = eventName;
		super.params = params;
		this.incent = incent;
		this.currency = currency;
		super.createTime = System.currentTimeMillis();
	}
}
