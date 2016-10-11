package com.oasis.sdk;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;

import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.oasis.sdk.OASISPlatformConstant.Language;
import com.oasis.sdk.activity.GooglePlayBillingActivity;
import com.oasis.sdk.activity.OasisSdkFBFriendsActivity;
import com.oasis.sdk.activity.OasisSdkFBRequestActivity;
import com.oasis.sdk.activity.OasisSdkLoginActivity;
import com.oasis.sdk.activity.OasisSdkPayActivity;
import com.oasis.sdk.activity.OasisSdkShareActivity;
import com.oasis.sdk.activity.platform.FacebookUtils;
import com.oasis.sdk.activity.platform.GoogleUtils;
import com.oasis.sdk.base.Exception.OasisSdkException;
import com.oasis.sdk.base.communication.ConnectionChangeReceiver;
import com.oasis.sdk.base.entity.UserInfo;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.OasisTestUtils;
import com.oasis.sdk.base.utils.SystemCache;


public class OASISPlatform {
	
	private static ConnectionChangeReceiver connectionChangeReceiver;
	/**
	 * 接口类，游戏必须实现该接口的所有方法
	 * 1、用户注销
	 * 2、重新加载游戏
	 * @param impl
	 */	
	public static void setOASISPlatformInterfaceImpl(OASISPlatformInterface impl) {
		SystemCache.oasisInterface = impl;
	}

	/**
	 * 初始化，游戏启动时调用;此方法可兼容已有老用户。
	 * 警告：如果是全新方式接入OASIS SDK请使用init(Context c, HashMap<String, String> oldUsers, String freeRegistUserName)；
	 * @param c			当前Activity
	 * @param oldUsers	旧用户信息，key为账号，value为密码，密码不加密
	 * @param freeRegistUserName	免注册登录账号。一般为mac地址或deviceid
	 * @param locale	本地化配置
	 */
	public static void init(Context c, HashMap<String, String> oldUsers, String freeRegistUserName){
		BaseUtils.clearALL();
		
		// 初始化网络状态
		BaseUtils.setNetworkState(c);
		
		// 注册网络改变接收服务
		connectionChangeReceiver = new ConnectionChangeReceiver();
		BaseUtils.registerReceiver(connectionChangeReceiver, ConnectivityManager.CONNECTIVITY_ACTION, c);

		
		BaseUtils.initInfo(c, oldUsers, freeRegistUserName);
		
		BaseUtils.initOtherInfo();
		
		BaseUtils.cacheLog(1, "OASISPlatform.init<br>Request Parameter:oldUsers="+(oldUsers!=null?oldUsers.toString():"")+";freeRegistUserName="+freeRegistUserName);

	}
	/**
	 * 初始化，游戏启动时调用，
	 * 警告：如果是通过APK升级方式接入OASIS SDK请使用init(Context c, HashMap<String, String> oldUsers, String freeRegistUserName)；
	 * @param c			当前Activity
	 */
	public static void init (Context c){
		init(c, null, null);
	}
	
    /**
     * 游戏内切换服务器或角色时，清除必要数据（服ID、角色ID）
     * SDK V2.6开始请使用 cleanGameInfo(Context c)。
     */
	@Deprecated
    public static void logout(Context c){
    	BaseUtils.clearInfoForLogout();
		
		BaseUtils.cacheLog(1, "OASISPlatform.logout<br>请使用OASISPlatform.cleanGameInfo替换该方法。");
    }
    /**
     * 清除游戏信息，当玩家进行切换服务器或角色时，清除必要数据（服ID、角色ID）
     */
    public static void cleanGameInfo(Context c){
    	if(SystemCache.userInfo != null)
    		BaseUtils.cacheLog(1, "OASISPlatform.cleanGameInfo<br>ServerID="+SystemCache.userInfo.serverID+";roleID="+SystemCache.userInfo.roleID+"被清除，请使用OASISPlatform.setUserInfo重新配置");
    	BaseUtils.clearInfoForLogout();
    }
    /**
	 * 获取当前用户信息
	 * @return
	 */
	public static UserInfo getUserInfo() {
		
		return BaseUtils.getUserInfo();
	}
	/**
	 * 设置当前用户信息
	 * @param serverid 游戏服id
	 * @param serverName 游戏服名称
	 * @param serverType 游戏服类型
	 * @param username 当前用户名
	 * @param roleid 角色id
	 */
	public static void setUserInfo(String serverid, String serverName, String serverType, String username, String roleid){
		BaseUtils.cacheLog(1, "OASISPlatform.setUserInfo<br>Request Parameter:"+(getUserInfo()!=null?"UID="+getUserInfo().uid:"")+"ServerID="+serverid+";ServerName="+serverName+";ServerType="+serverType+";roleID="+roleid+";username="+username);
		BaseUtils.setUserInfo(serverid, serverName, serverType, username, roleid);
	}

	/**
	 * 清除数据，退出游戏时调用
	 * @param gameCode
	 */
	public static void destroy(Context c){
		// 清理本次可能使用到的第三方数据
		FacebookUtils.logout();
		GoogleUtils.instance((Activity) c).clear();
		
		BaseUtils.clearALL();
		
		BaseUtils.unRegisterReceiver(connectionChangeReceiver, c);
		
	}
		
	/**
	 * 显示OG助手，
	 * @param activity 		主Activity（每次传递都是同一对象）
	 * @param showLocation 	默认显示位置：	1:LEFTTOP(0,0)、
	 * 									2:LEFTCENTER(0,屏幕高度/2)、
	 * 									3:LEFTBOTTOM(0,屏幕高度),
	 * 									4:RIGHTTOP(屏幕宽度,0),
	 * 									5:RIGHTCENTER(屏幕宽度,屏幕高度/2),
	 * 									6:RIGHTBOTTOM(屏幕宽度,屏幕高度)
	 * @param isShow 			OG助手显示标志	true显示，false不显示
	 * @return
	 */
	public static void showMenu(Activity activity, int showLocation, boolean isShow){
		BaseUtils.showMenu(activity, showLocation, isShow);
		BaseUtils.cacheLog(1, "OASISPlatform.showMenu<br>Request Parameter:showLocation="+showLocation+";type="+isShow);
	}
	/**
	 * 显示OG助手，
	 * SDK V2.6 推荐使用showMenu(Activity activity, int showLocation)，
	 * @param activity 		主Activity（每次传递都是同一对象）
	 * @return
	 */
	@Deprecated
	public static void showMenu(Activity activity){
		showMenu(activity, 1, true);
	}
	
	/*----------------------------注册相关操作    开始--------------------------------*/
	/**
	 * 注册OAS账号
	 * @return
	 * @throws OasisSdkException 
	 * @throws OasisSdkDataErrorException 
	 */
//	public static String regist(String username, String password) throws OasisSdkException, OasisSdkDataErrorException {
//		BaseUtils.cacheLog(1, "OASISPlatform.regist<br>Request Parameter:username="+username+";password="+password);
//		
//		return HttpService.instance().register(username, password);
//	}
	
	/*----------------------------注册相关操作    结束--------------------------------*/
	/*----------------------------登录相关操作    开始--------------------------------*/
	/**
	 * 自动登录（type只能为-1）
	 * @param c
	 * @param type 	-1:自动登录
	 */
	public static void login(Activity c, int type){
		BaseUtils.cacheLog(1, "OASISPlatform.login<br>自动登录");
		HashMap<String, String> para = new HashMap<String, String>();
		para.put("uitype", "-1");
		startActivity(c, OasisSdkLoginActivity.class, para);
	}
	/**
	 * 打开切换账号界面
	 * @param c
	 */
	public static void switchUser(Activity c){
		BaseUtils.cacheLog(1, "OASISPlatform.switchUser<br>显示切换账号UI界面");
		HashMap<String, String> para = new HashMap<String, String>();
		para.put("uitype", "9");
		startActivity(c, OasisSdkLoginActivity.class, para);
	}

	/**
	 * 登录
	 * @param loginType		登录类型	1:匿名登录
	 * 								2:OAS账号登录
	 * 								3:第三方账号登录
	 * @param username		用户名		loginType=1:null
	 * 								loginType=2:OAS 账号
	 * 								loginType=3:第三方账号类型（目前只支持Facebook）
	 * @param password		密码		loginType=1:null
	 * 								loginType=2:OAS密码
	 * 								loginType=3:第三方账号token
	 * @return json
	 * @throws OasisSdkException
	 */
//	public static String login(String loginType, String username, String password) throws OasisSdkException{
//		BaseUtils.cacheLog(1, "OASISPlatform.login<br>Request Parameter:loginType="+loginType+";username="+username+";password="+password);
//		if(OASISPlatformConstant.LOGIN_TYPE_ANONYMOUS.equals(loginType)){
//			return HttpService.instance().login(1, "", "");
//		}else if(OASISPlatformConstant.LOGIN_TYPE_OASIS.equals(loginType)){
//			return HttpService.instance().login(2, username, password);
//		}else /*if(OASISPlatformConstant.LOGIN_TYPE_FACEBOOK.equals(loginType)){
//			return HttpService.instance().login(3, username, password);
//		}else if(OASISPlatformConstant.LOGIN_TYPE_GOOGLE.equals(loginType))*/{
//			return HttpService.instance().login(3, username, password);
//		}/*else{
//			throw new OasisSdkException("Login fail.The parameter is incorrect.");
//		}*/
//		
//	}
	/**
	 * 使用最近登录账号进行登录
	 * @return
	 * @throws OasisSdkException 
	 */
//	public static String loginWithRecentlyUser() throws OasisSdkException{
//		BaseUtils.cacheLog(1, "OASISPlatform.loginWithRecentlyUser<br>使用最近登录成功账号进行登录");
//		return HttpService.instance().loginWithRecentlyUser();
//	}
	
	/*----------------------------登录相关操作    结束--------------------------------*/

	
	/*----------------------------支付相关操作   开始--------------------------------*/
	/**
	 * 打开Google支付页面
	 * @param c
	 * @param requestCode		请求码
	 * @param productID			商品id
	 * @param gameServerID		游戏服id
	 * @param ext				扩展参数,此参数将透传
	 * @throws OasisSdkException
	 */
	public static void toGoogleBillPayPage(Activity c, int requestCode, String productID, double revenue, String ext){
		if(!SystemCache.NetworkisAvailable){
			// 提示“网络连接中，请稍后重试”
			BaseUtils.showMsg(c, c.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_common_net_disable")));
			return;
		}
		if(SystemCache.userInfo == null){
			// 未登录成功，提示“网络连接中，请稍后重试”，并执行一次自动登录
			BaseUtils.showMsg(c, c.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_common_net_exception")));
			login(c, -1);
			return;
		}
		if(SystemCache.userInfo != null && SystemCache.userInfo.chargeable != 0){
			BaseUtils.showDisableDialog(c, SystemCache.userInfo.chargeable == 1?"oasisgames_sdk_login_notice_11":"oasisgames_sdk_login_notice_12");
			return;
		}		
		BaseUtils.cacheLog(1, "OASISPlatform.toGoogleBillPayPage<br>Request Parameter:requestCode="+requestCode+";productID="+productID+";revenue="+revenue+";ext="+ext
				+";serverID="+SystemCache.userInfo.serverID+";roleID="+SystemCache.userInfo.roleID);
		HashMap<String, String> para = new HashMap<String, String>();
		para.put("inAppProductID", productID);
		if(revenue > 0)
			para.put("revenue", Double.toString(revenue));
		para.put("ext", ext);
		if(BaseUtils.checkGoogleisAble(c)){
			startActivity(c, OasisSdkPayActivity.class, para);
			return;
		}
		startActivityForResult(c, requestCode, GooglePlayBillingActivity.class, para);
	}
	/*----------------------------支付相关操作    结束--------------------------------*/
	
	/*----------------------------Facebook分享相关操作   开始--------------------------------*/
	/**
	 * Facebook分享  链接分享
	 * @param c
	 * @param requestCode
	 * @param link
	 * @param picture
	 * @param name
	 * @param caption
	 * @param description
	 */
	public static void shareByFacebook(Activity c, String link, String picture, String name, String caption, String description){
		BaseUtils.cacheLog(1, "OASISPlatform.shareByFacebook<br>Request Parameter:link="+link+";picture="+picture+";name="+name+";caption="+caption+";description="+description);
		Intent in = new Intent();
		in.setClass(c, OasisSdkShareActivity.class);
		in.putExtra("link", link);
		in.putExtra("picture", picture);
		in.putExtra("name", name);
		in.putExtra("caption", caption);
		in.putExtra("description", description);
		c.startActivity(in);
	}
	/**
	 * 获取Facebook好友
	 * @param c
	 * @param requestCode	请求码
	 * @param limit			每页获取条数
	 * @param type			true：下一页	false：上一页
	 */
	public static void getFriends(Activity c, int limit, boolean type){
		if(limit < 0 || limit > 500){
			Log.e("OASISPlatform", "limit 必须为1-500之间的正整数！");
			return;
		}
		BaseUtils.cacheLog(1, "OASISPlatform.getFriends<br>Request Parameter:limit="+limit+";type="+(type?"Next":"Previous"));
		Intent in = new Intent();
		in.setClass(c, OasisSdkFBFriendsActivity.class);
		in.putExtra("limit", limit);
		in.putExtra("type", type);
		in.putExtra("requestCode", OASISPlatformConstant.REQUEST_CODE_FACEBOOK_GETFRIENDS);
		c.startActivity(in);
	}
	/**
	 * 获取Facebook可邀请的好友
	 * @param c
	 * @param requestCode	请求码
	 * @param limit			每页获取条数
	 * @param type			true：下一页	false：上一页
	 */
	public static void getInvitableFriends(Activity c, int limit, boolean type){
		if(limit < 0 || limit > 500){
			Log.e("OASISPlatform", "limit 必须为1-500之间的正整数！");
			return;
		}
		BaseUtils.cacheLog(1, "OASISPlatform.getInvitableFriends<br>Request Parameter:limit="+limit+";type="+(type?"Next":"Previous"));
		Intent in = new Intent();
		in.setClass(c, OasisSdkFBFriendsActivity.class);
		in.putExtra("limit", limit);
		in.putExtra("type", type);
		in.putExtra("requestCode", OASISPlatformConstant.REQUEST_CODE_FACEBOOK_GETINVITABLEFRIENDS);
		c.startActivity(in);
	}
	/**
	 * 给好友发送请求
	 * @param c				
	 * @param actionType	请求动作类型		1：邀请   2：赠送    3：索要
	 * @param objectID		赠送或索要的对象id，由OAS提前在FB后台配置 	actionType为2、3时必传
	 * @param uids			接收好友（多个好友以英文逗号分隔）
	 * @param message		请求内容
	 */
	public static void setAppRequest(Activity c, int actionType, String objectID, String uids, String message){
		BaseUtils.cacheLog(1, "OASISPlatform.setAppRequest<br>Request Parameter:actionType="+(actionType==1?"Invite":actionType==2?"Send":"Askfor")+";objectID="+objectID+";uids="+uids+";message="+message);
		Intent in = new Intent();
		in.setClass(c, OasisSdkFBRequestActivity.class);
		in.putExtra("actionType", actionType);
		in.putExtra("objectID", objectID);
		in.putExtra("uids", uids);
		in.putExtra("message", message);
		c.startActivity(in);
	}
	/**
	 * 分享图片
	 * @param imagePath 图片完整路径
	 */
	public static void uploadImage(Activity c, String imagePath){
		if(!ShareDialog.canShow(SharePhotoContent.class)){
			BaseUtils.logDebug("OASIS　SDK", "没有安装FB，无法分享图片");
			BaseUtils.showMsg2(c, "oasisgames_sdk_share_notapp");
			return;
		}
		BaseUtils.cacheLog(1, "OASISPlatform.uploadImage<br>Request Parameter:");
		
		Intent in = new Intent();
		in.setClass(c, OasisSdkShareActivity.class);
		in.putExtra("bitmaps", imagePath);
		in.putExtra("action", 1);
		c.startActivity(in);
	}
//	/**
//	 * 创建Facebook Banner 广告
//	 * @param c				当前Activity
//	 * @param placementID	定位id
//	 * @param adsize		广告尺寸
//	 * @return
//	 */
//	public static AdView createAdViewBanner(Activity c, String placementID, AdSize adsize){
//		AdView adview = new AdView(c, placementID, adsize);
//		
//		return adview;
//	}
//	/**
//	 * 创建Facebook Interstitial  广告
//	 * @param c				当前Activity
//	 * @param placementID	定位id
//	 * @return
//	 */
//	public static InterstitialAd createAdViewInterstitial(Activity c, String placementID){
//		InterstitialAd adview = new InterstitialAd(c, placementID);
//		
//		return adview;
//	}
//	/**
//	 * 创建Facebook NativeAd  广告
//	 * @param c				当前Activity
//	 * @param placementID	定位id
//	 * @return
//	 */
//	public static NativeAd createAdViewNative(Activity c, String placementID){
//		NativeAd adview = new NativeAd(c, placementID);
//		
//		return adview;
//	}
	/*----------------------------Facebook分享相关操作    结束--------------------------------*/

	/*----------------------------跟踪相关操作   开始--------------------------------*/
	public static void trackOnCreate(Activity c){
		BaseUtils.trackOnCreate(c);
	}
	public static void trackOnStart(Activity c){
		BaseUtils.trackOnStart(c);
	}
	public static void trackOnRestart(Activity c){
		BaseUtils.trackOnRestart(c);
	}
	public static void trackOnResume(Activity c){
		BaseUtils.trackOnResume(c);
	}
	public static void trackOnPause(Activity c){
		BaseUtils.trackOnPause(c);
	}
	public static void trackOnStop(Activity c){
		BaseUtils.trackOnStop(c);
	}
	public static void trackOnDestroy(Activity c){
		BaseUtils.trackOnDestroy(c);
	}
	public static void trackEvent(Activity c, String eventToken, Map<String, String> parameters){
		BaseUtils.cacheLog(1, "OASISPlatform.trackEvent<br>Request Parameter:eventToken="+eventToken+";parameters="+(parameters!=null?parameters.toString():""));
		BaseUtils.trackEvent(c, eventToken, parameters);
	}
	/**
	 * Add tracking of custom events.跟踪单个事件
	 * @param c				当前activity
	 * @param trackType		跟踪类型（）
	 * @param eventName		事件名称
	 * @param parameters	事件级参数
	 * @param status		APP级参数
	 */
	public static void trackEvent(Activity c, int trackType, String eventName, Map<String, String> parameters, Map<String, String> status){
		BaseUtils.cacheLog(1, "OASISPlatform.trackEvent<br>Request Parameter:eventToken="+eventName+
				";trackType="+trackType+
				";parameters="+(parameters!=null?parameters.toString():"") +
				";status="+(status!=null?status.toString():""));
		BaseUtils.trackEvent(c, trackType, eventName, parameters, status);
		
	}
	public static void trackRevenue(Activity c, String eventToken, double amountInCents, String currency, Map<String, String> parameters){
		BaseUtils.cacheLog(1, "OASISPlatform.trackRevenue<br>Request Parameter:eventToken="+eventToken+";parameters="+(parameters!=null?parameters.toString():"")+";amountInCents="+amountInCents);
		BaseUtils.trackRevenue(c, eventToken, amountInCents, currency, parameters);
	}
	public static void trackDeepLink(Activity c, Uri link){
		BaseUtils.trackDeepLink(c, link);
	}
	/*----------------------------跟踪相关操作    结束--------------------------------*/

	/**
	 * 设置SDK语言
	 * @param c			上下文
	 * @param language	语言
	 */
	public static void setLanguage(Context c, Language language){
		BaseUtils.changeLanguge(c, language);
	}
	
	private static void startActivityForResult(Activity c, int requestCode,Class<?> cls, HashMap<String, String> para){
		Intent in = new Intent();
		in.setClass(c, cls);
		if(null != para){
			for (String key : para.keySet()) {
				in.putExtra(key, para.get(key));
			}
		}
		c.startActivityForResult(in, requestCode);
	}
	private static void startActivity(Activity c, Class<?> cls, HashMap<String, String> para){
		Intent in = new Intent();
		in.setClass(c, cls);
		if(null != para){
			for (String key : para.keySet()) {
				in.putExtra(key, para.get(key));
			}
		}
		c.startActivity(in);
	}
	
	public static boolean test(Activity a, String uri){
		return OasisTestUtils.getGift(a, uri);
	}
}
