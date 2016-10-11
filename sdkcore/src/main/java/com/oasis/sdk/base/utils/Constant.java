package com.oasis.sdk.base.utils;

import java.util.Hashtable;



/**
 * 常量.
 * 
 * @author xdb
 * 
 */
public class Constant {
	private static final String VERSION = "3.5";
	/**
	 * SDK 当前版本号
	 */
	public static final String SDKVERSION = VERSION + ".0";
	/**
	 * 支付接口
	 */
	public static final int ISPAY = 1;
	/**
	 * 普通接口
	 */
	public static final int ISNORMAL = 0;
	
	public static final String BASEURL = "http://apisdk.mobile.oasgames.com/"+VERSION+"/?";
	public static final String BASEURL_TEST = "http://apisdk.mobile.test.oasgames.com/"+VERSION+"/?";
	public static final String BASEURL_SANDBOX = "http://apisdk.mobile.oasgames.com/sandbox/?";
	
	/**
	 * Infobip App key
	 */
	public static final String PAYINFOBIPAPPKEY = "1be6320babcf4c356261593d34788311";
	/**
	 * Mopay App key or APPSECRET
	 */
	public static final String PAYMOPAYAPPKEY = "oaioOUFIIE";

	/**
	 * 最近登录的用户信息
	 */
	public static final String SHAREDPREFERENCES_RECENTLYUSERINFOS = "recentlyuserinfos";
	/**
	 * 当前登录的用户信息
	 */
	public static final String SHAREDPREFERENCES_CURRENTUSERINFOS = "currentuserinfos";
	
	public static final Hashtable<Integer, String> http_statuscode_errorMsg = new Hashtable<Integer, String>();
	static {
		http_statuscode_errorMsg.put(0, "未知异常(可能需要设置代理)");
		http_statuscode_errorMsg.put(400, "错误请求");
		http_statuscode_errorMsg.put(408, "Request Timeout/请求超时");
		http_statuscode_errorMsg.put(500, "Internal Server Error/内部服务器错误");
		http_statuscode_errorMsg.put(503, "Service Unavailable/服务无法获得");
		http_statuscode_errorMsg.put(504, "Gateway Timeout/网关超时");

	}
	
	public static final String[] createTables = new String[]{
		"create table if not exists googleorder (orderid varchar(100) primary key, orderdata text not null, ordersign text not null, createtime varchar not null, status varchar(10), ext1 varchar(100), ext2 text);"
	};
	public static final String[] dropTables = new String[]{
//		"drop table googleorder;"
	};
	//保存返回的endpointarn的键名
	public static final String ENDPOINT_ARN ="endpointarn";
	//保存最新一次更改的sdk语言设置的键值
	public static final String LATEST_LANGUAGE = "latestlanguage";
}