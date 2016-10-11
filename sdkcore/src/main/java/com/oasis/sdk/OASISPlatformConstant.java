package com.oasis.sdk;

/**
 * oas SDK 常量
 * @author xdb
 *
 */
public class OASISPlatformConstant {
	
	/**
	 * 登录  匿名
	 */
	public static final String LOGIN_TYPE_ANONYMOUS = "anonymous";
	/**
	 * 登录 OASIS
	 */
	public static final String LOGIN_TYPE_OASIS = "oasis";
	/**
	 * 登录 FACEBOOK
	 */
	public static final String LOGIN_TYPE_FACEBOOK = "facebook";
	/**
	 * 登录GOOGLE
	 */
	public static final String LOGIN_TYPE_GOOGLE = "google";
	/**
	 * 产品环境、正式环境
	 */
	public static final String ENVIRONMENT_PRODUCTION = "production";
	/**
	 * 沙箱环境
	 */
	public static final String ENVIRONMENT_SANDBOX = "sandbox";
	/**
	 * 在线游戏
	 */
	public static final String GAMEMODE_ONLINE = "online";
	/**
	 * 离线游戏
	 */
	public static final String GAMEMODE_OFFLINE = "offline";
	/**
	 * 用户类型-单机离线
	 */
	public static final int USER_TYPE_OFFLINE = 100;
	
	/**
	 * Google 支付请求码
	 */
	public static final int REQUEST_CODE_GOOGLEPAY = 100001;
	/**
	 * Facebook 分享请求码
	 */
	public static final int REQUEST_CODE_SHARE_FACEBOOK = 100002;
	/**
	 * Facebook 获取一起玩游戏的好友
	 */
	public static final int REQUEST_CODE_FACEBOOK_GETFRIENDS = 100100;
	/**
	 * Facebook 获取可邀请的好友
	 */
	public static final int REQUEST_CODE_FACEBOOK_GETINVITABLEFRIENDS = 100101;
	/**
	 * Facebook 给好友发送请求
	 */
	public static final int REQUEST_CODE_FACEBOOK_REQUEST = 100102;
	
	/**
	 * 支付成功，但发钻失败，用户不愿意重试的情况下得结果码
	 */
	public static final int RESULT_EXCEPTION_GOOGLEPAY_EXCEPTION = 11;

	/**
	 * 成功
	 */
	public static final int RESULT_SUCCESS = -1;
	/**
	 * 失败
	 */
	public static final int RESULT_FAIL = 0;
	/**
	 * 异常
	 */
	public static final int RESULT_EXCEPTION = 1;
	/**
	 * 取消
	 */
	public static final int RESULT_CANCLE = 2;
	/**
	 * 操作结果未知（此code一般由server to server通知）
	 */
	public static final int RESULT_PENDING = 3;
	
	/**
	 * 上报所有平台（含 adust、mdata 及未来所有平台）
	 */
	public static final int REPORT_TYPE_ALL = 0;
	/**
	 * 上报到Adjust
	 */
	public static final int REPORT_TYPE_ADJUST = 1;
	/**
	 * 上报到Mdata
	 */
	public static final int REPORT_TYPE_MDATA = 2;
	
	/**
	 * SDK 目前支持的语言
	 * @author xiaodongbing
	 *
	 */
	public enum Language{
		PT, TR, ES, PL, NL, SV, DE, EL, EN, RU, IT, FR, KO, ZH // 共14种语言
	}
}
