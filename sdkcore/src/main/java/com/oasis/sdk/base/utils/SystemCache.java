package com.oasis.sdk.base.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;
import android.webkit.WebView;

import com.oasis.sdk.OASISPlatformConstant;
import com.oasis.sdk.OASISPlatformInterface;
import com.oasis.sdk.OASISPlatfromMenu;
import com.oasis.sdk.base.entity.ControlInfo;
import com.oasis.sdk.base.entity.PayInfoList;
import com.oasis.sdk.base.entity.UserInfo;
import com.oasis.sdk.base.report.ReportUtils;
import com.oasis.sdk.pay.googleplay.utils.GoogleBillingUtils;

/**
 * 共用系统数据缓存.
 * 
 * @author xdb
 * 
 */
public class SystemCache {
	public static int SCREENROTATION = 1;// 竖屏
	
	/**
	 * SDK request response 信息打印
	 */
	public static boolean SDKMODE_SANDBOX_REQEUST_RESPONSE = false;
	/**
	 * SDK 沙盒模式
	 */
	public static boolean OASISSDK_ENVIRONMENT_SANDBOX = false;
	/**
	 * SDK 测试模式
	 */
	public static boolean OASISSDK_ENVIRONMENT_TEST = false;
	
	/**
	 * 游戏模式
	 */
	public static String OASISSDK_GAMEMODE = OASISPlatformConstant.GAMEMODE_ONLINE;

	/**
	 * 游戏code
	 */
	public static String GAMECODE;
	/**
	 * 公共key
	 */
	public static String PUBLICKEY;
	/**
	 * 支付key
	 */
	public static String PAYKEY;
	/**
	 * 退出时，游戏端处理类
	 */
	public static OASISPlatformInterface oasisInterface;
	/**
	 * 菜单布局
	 */
	public static OASISPlatfromMenu menu;

	/**
	 * 登录用户信息，游戏端调用个体UserInfo获取该对象
	 */
	public static UserInfo userInfo;
	public static UserInfo bindInfo;
	/**
	 * 离线模式下，本地登录用户信息
	 * 该信息将在用户正式登录成功后，清空该对象
	 */
	public static UserInfo localInfo;
//	public static GraphUser fbInfo;
	/**
	 * 控制开关信息
	 */
	public static ControlInfo controlInfo = new ControlInfo();
	
	public static boolean NetworkisAvailable = true;

	public static String NetworkExtraInfo = "";

//	/**
//	 * 数据库操作类
//	 */
//	public static DBHelper dbHelper;
	public static SharedPreferences setting;
	public static SharedPreferences.Editor settingEditor;
	
	/**
	 * 支付套餐信息
	 */
	public static List<PayInfoList> payInfoLists;
	/**
	 * Adjust事件对应关系
	 */
	public static Map<String, String> adjustEventMap = new HashMap<String, String>();
	
	/**
	 * 退出应用的标志
	 */
	public static boolean isExit = false;
	
	/**
	 * 运行日志信息-缓存
	 */
	public static List<String> logLists = null;
	/**
	 * 游戏调用SDK的日志信息-将存入SD卡
	 */
	public static List<String> logListsSD = null;
	/**
	 * 论坛 WebView
	 */
	public static WebView luntan = null;
	
//	public static RequestQueue volleyRequestQueue;
	
	/**
	 * 清缓存
	 */
	public static void clear(){
		GAMECODE = null;
		if(userInfo != null)
			userInfo.serverID = "";
		PUBLICKEY = null;
		PAYKEY = null;
		menu = null;
		userInfo = null;
		localInfo = null;
		bindInfo = null;
//		fbInfo = null;
		controlInfo = new ControlInfo();
		if(payInfoLists != null)
			payInfoLists = null;
		
		SystemCache.menu = null;
		ReportUtils.cancelReport();
		if(null != GoogleBillingUtils.GoogleBillingTimer)
			GoogleBillingUtils.GoogleBillingTimer.cancel();
		
		isExit = false;
		logLists = null;
		logListsSD = null;
		
		FileUtils.deleteFileOnAppStartOrDestory();
		
		luntan = null;
		if(setting != null && settingEditor != null)
			BaseUtils.saveSettingKVPtoSysCache(Constant.SHAREDPREFERENCES_CURRENTUSERINFOS, "");// 清空当前的登录信息		
		
	}

}