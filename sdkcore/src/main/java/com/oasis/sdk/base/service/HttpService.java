package com.oasis.sdk.base.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import c.mpayments.android.PurchaseResponse;

import com.android.base.http.CallbackResultForActivity;
import com.android.base.http.OasisSdkHttpClient;
import com.android.base.upload.MultipartEntity;
import com.android.base.upload.MultipartRequest;
import com.mopub.volley.DefaultRetryPolicy;
import com.mopub.volley.Response.ErrorListener;
import com.mopub.volley.Response.Listener;
import com.mopub.volley.RetryPolicy;
import com.mopub.volley.VolleyError;
import com.oasis.sdk.base.Exception.OasisSdkDataErrorException;
import com.oasis.sdk.base.Exception.OasisSdkException;
import com.oasis.sdk.base.communication.RequestEntity;
import com.oasis.sdk.base.entity.ControlInfo;
import com.oasis.sdk.base.entity.MemberBaseInfo;
import com.oasis.sdk.base.entity.PayConfigInfo;
import com.oasis.sdk.base.entity.PayHistoryInfoDetail;
import com.oasis.sdk.base.entity.PayHistoryList;
import com.oasis.sdk.base.entity.PayInfoDetail;
import com.oasis.sdk.base.entity.PayInfoList;
import com.oasis.sdk.base.entity.PhoneInfo;
import com.oasis.sdk.base.entity.QuestionInfo;
import com.oasis.sdk.base.entity.QuestionInfoLog;
import com.oasis.sdk.base.entity.QuestionList;
import com.oasis.sdk.base.entity.QuestionType;
import com.oasis.sdk.base.entity.RecentUser;
import com.oasis.sdk.base.entity.RecentUserGameInfo;
import com.oasis.sdk.base.entity.RecentUserList;
import com.oasis.sdk.base.entity.ReportMdataInfo;
import com.oasis.sdk.base.entity.UserInfo;
import com.oasis.sdk.base.json.JsonParser;
import com.oasis.sdk.base.utils.ApplicationContextManager;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.Constant;
import com.oasis.sdk.base.utils.MD5Encrypt;
import com.oasis.sdk.base.utils.SystemCache;
import com.oasis.sdk.pay.googleplay.utils.Purchase;

/**
 * http service
 * 处理请求参数
 * 处理请求结果
 * @author Xdb
 * 
 */
public class HttpService {
	private static final String SPLITCHAR = "OASUSER";// 分隔符，避免截取出错
	private final static String TAG = "OAS_HttpService";
	private final static HttpService HTTP_SERVICE = new HttpService();

	private HttpService() {
	}

	/**
	 * @return 返回逻辑的实例.
	 */
	public static HttpService instance() {

		return HTTP_SERVICE;
	}
	
	public void getUserListForRecently(final int page, final CallbackResultForActivity callback){
		StringBuilder url = new StringBuilder("a=Login&m=UserLoginInfo");
		url.append("&page="+page);
		url.append("&sign="+MD5Encrypt.StringToMD5(BaseUtils.getMobileCode()+SystemCache.GAMECODE+SystemCache.PUBLICKEY));
		new OasisSdkHttpClient(getNewestUrl(url.toString()), null, new OasisSdkHttpClient.Callback() { 
			
			@Override
			public void handleResultData(String result) {
				boolean status = true;
				JSONObject o = null;
				try {
					o = new JSONObject(result);
					if(!o.has("status") || !"ok".equalsIgnoreCase(o.getString("status")))
						status = false;
				} catch (JSONException e) {
				}finally{
					if(status){
						RecentUserList rul;
						try {
							rul = parseData(o.getJSONObject("retinfo"));
							callback.success(rul, "", "");
						} catch (JSONException e) {
							callback.fail("-1", "");
						}
						
					}else
						callback.fail("-1", "");
				}
				
			}
			
			@Override
			public void handleErorrData(VolleyError error) {
				callback.excetpion(new OasisSdkException(error.getMessage()));
			}
		}).submitGet();
	}
	private RecentUserList parseData(JSONObject result){
		RecentUserList rul = new RecentUserList();
		if(result == null || !result.has("historylist"))
			return rul;
		
		try {
			if(result.has("onepageitems"))
				rul.pageSize = result.getInt("onepageitems");
			if(result.has("nowpage"))
				rul.page = result.getInt("nowpage");
			if(result.has("totalpage"))
				rul.pageCount = result.getInt("totalpage");
			
			if(result.has("historylist")){
				rul.recentUser = new ArrayList<RecentUser>();
				JSONArray userList = result.getJSONArray("historylist");
				int arrLen = userList.length();
				for (int i = 0; i < arrLen; i++) {
					JSONObject user = userList.getJSONObject(i);
					RecentUser ru = new RecentUser();
					ru.uid = user.getString("uid");
					if(user.has("third_uid"))
						ru.third_uid = user.getString("third_uid");
					ru.loginType = user.getInt("user_type");
					ru.platform = user.getString("platform");
					ru.username = user.getString("uname");
					ru.oasnickname = user.getString("nick_name");
					ru.time = user.getString("update_time");
					
					ru.list = new ArrayList<RecentUserGameInfo>();
					if(user.has("roleinfo")){
						JSONArray roleList = user.getJSONArray("roleinfo");
						int roleLen = roleList.length();
						for (int j = 0; j < roleLen; j++) {
							JSONObject role = roleList.getJSONObject(j);
							RecentUserGameInfo rugi = new RecentUserGameInfo();
							rugi.level = role.getString("role_level");
							rugi.roleId = role.getString("role_id");
							rugi.roleName = role.getString("role_name");
							rugi.serverId = role.getString("server_id");
							rugi.serverName = role.getString("server_name");
							ru.list.add(rugi);
						}
					}
					
					rul.recentUser.add(ru);
				}
				
			}
			
		} catch (Exception e) {
			BaseUtils.logDebug(TAG, "历史账号Json解析失败。\n"+result.toString());
		}
				
		return rul;
	}
	/**
	 * 使用最近登录账号进行登录
	 * @return
	 * @throws OasisSdkException
	 */	
	public void loginWithRecentlyUser(final CallbackResultForActivity callback){
		StringBuilder url = null;
		final String recentlyuserinfos = (String) BaseUtils.getSettingKVPfromSysCache(Constant.SHAREDPREFERENCES_RECENTLYUSERINFOS, "");// usertype/uid/oastoken/username/password/platform/oasNickName
		if (!TextUtils.isEmpty(recentlyuserinfos)) {
			url = new StringBuilder("a=Login&m=AutoLogin");
			String[] userinfo = recentlyuserinfos.split(SPLITCHAR);

			int userType = Integer.valueOf(userinfo[0]);
			String oasToken = userinfo[2];
			url.append("&usertype=" + userType);
			url.append("&uid=" + userinfo[1]);
			url.append("&oas_token=" + oasToken);

			if (userType == 3) {
				url.append("&platform="
						+ (userinfo.length > 5 ? userinfo[5] : ""));
				url.append("&platform_token="
						+ (userinfo.length > 4 ? userinfo[4] : ""));
			}

			url.append("&sign="
					+ MD5Encrypt.StringToMD5(BaseUtils.getMobileCode()
							+ SystemCache.GAMECODE + userType + oasToken
							+ SystemCache.PUBLICKEY));
		} else {
			url = new StringBuilder("a=Login&m=UserLoginJustify");
			url.append("&sign="
					+ MD5Encrypt.StringToMD5(BaseUtils.getMobileCode()
							+ SystemCache.GAMECODE + SystemCache.PUBLICKEY));
		}
		
		String urls = getNewestUrl(url.toString());
		
		BaseUtils.logDebug(TAG, "loginWithRecentlyUser====="+urls);
		
		new OasisSdkHttpClient(urls, null, new OasisSdkHttpClient.Callback() {
			
			@Override
			public void handleResultData(String result) {
				try {
					JSONObject o = new JSONObject(result);
					if(SystemCache.userInfo == null)
						SystemCache.userInfo = new UserInfo();
					if("ok".equalsIgnoreCase(o.getString("status"))){
						
						SystemCache.userInfo.setUidOld(SystemCache.userInfo.getUid());
						
						// 登录成功，缓存用户信息
						SystemCache.userInfo.setStatus("ok");
						SystemCache.userInfo.setUid(o.getString("uid"));
						SystemCache.userInfo.setToken(o.getString("token"));
						SystemCache.userInfo.setType(o.getInt("type"));
						if(o.has("user_type"))
							SystemCache.userInfo.setLoginType(o.getInt("user_type"));				
						SystemCache.userInfo.setError("");
						SystemCache.userInfo.setErr_msg("");
						
						if(o.has("tiplogin"))
							SystemCache.userInfo.setTiplogin(o.getString("tiplogin"));
						else
							SystemCache.userInfo.setTiplogin("");
						
						if(o.has("tip_perfect_userinfo"))
							SystemCache.userInfo.setTip_perfect_userinfo(o.getString("tip_perfect_userinfo"));
						else
							SystemCache.userInfo.setTip_perfect_userinfo("");
						
						try {// 解析OG开关信息
							JSONObject offres = o.getJSONObject("onoff_res");
							SystemCache.controlInfo.setOg_onoff_control(offres.getString("og_onoff_control"));
							SystemCache.controlInfo.setCharge_onoff_control(offres.getString("charge_onoff_control"));
							SystemCache.controlInfo.setForum_onoff_control(offres.getString("forum_onoff_control"));
							SystemCache.controlInfo.setSwitching_onoff_control(offres.getString("switching_onoff_control"));
							SystemCache.controlInfo.setReg_onoff_control(offres.getString("reg_onoff_control"));
							SystemCache.controlInfo.setShare_onoff_control(offres.getString("share_onoff_control"));
							SystemCache.controlInfo.setCustom_onoff_control(offres.getString("custom_onoff_control"));
							SystemCache.controlInfo.setUserinfo_onoff_control(offres.getString("userinfo_onoff_control"));
							SystemCache.controlInfo.setHistory_logininfo_control(offres.getString("history_logininfo_control"));
							if(offres.has("charge_condition_res")){
								offres = offres.getJSONObject("charge_condition_res");
								SystemCache.controlInfo.setNetwork_condition(offres.getString("network_condition"));
								SystemCache.controlInfo.setPc_charge_condition(offres.getString("pc_charge_condition"));
								SystemCache.controlInfo.setEpin_onoff_control(offres.getString("Epin_exchange_condition"));
							}
							
						} catch (Exception e) {}
						
						// 获取充值状态
						int chargeable = 0;
						if(o.has("mb_charge_status")){
							if(!"yes".equals(o.getString("mb_charge_status"))){
								chargeable = 1;
							}
						}
						if(chargeable == 0 && o.has("uid_charge_status")){
							if(!"yes".equals(o.getString("uid_charge_status"))){
								chargeable = 2;
							}
						}
						SystemCache.userInfo.setChargeable(chargeable);
						
						if(o.has("platform"))
							SystemCache.userInfo.setPlatform(o.getString("platform"));
												
						String[] userinfo = recentlyuserinfos.split(SPLITCHAR);
						String oasNickName = "";
						String username = "";
						
						if(userinfo != null){// 设备上某游戏第一次登录时，没有昵称的情况，进行统一处理
							// usertype/uid/oastoken/username/password/platform/oasNickName
							if(userinfo.length >= 4)
								username = userinfo[3];
							if(userinfo.length >= 7)
								oasNickName = userinfo[6];
							
							if(oasNickName == null){
								oasNickName = "";
							}
							
						}
						String uname = "";
						String nick_name = "";
						if(o.has("uname"))
							uname = o.getString("uname");
						if(o.has("nick_name"))
							nick_name = o.getString("nick_name");
						
						if(SystemCache.userInfo.loginType == 2 && !TextUtils.isEmpty(uname) && !"null".equals(uname)){// OAS
							SystemCache.userInfo.setUsername(uname);
							SystemCache.userInfo.oasnickname = "";
						}else if(SystemCache.userInfo.loginType == 3 && !TextUtils.isEmpty(uname) && !"null".equals(uname)
								&& MemberBaseInfo.USER_GOOGLE.equalsIgnoreCase(SystemCache.userInfo.platform)){// Google
							SystemCache.userInfo.setUsername(uname);
							SystemCache.userInfo.oasnickname = "";
						}else if(SystemCache.userInfo.loginType == 3 && !TextUtils.isEmpty(nick_name) && !"null".equals(nick_name)
								&& MemberBaseInfo.USER_FACEBOOK.equalsIgnoreCase(SystemCache.userInfo.platform)){// Facebook
							SystemCache.userInfo.setUsername("");
							SystemCache.userInfo.oasnickname = nick_name;
						}else {
							if(!MemberBaseInfo.USER_FACEBOOK.equals(username) && !MemberBaseInfo.USER_GOOGLE.equals(username)
									&& !MemberBaseInfo.USER_OASIS.equals(username) && !MemberBaseInfo.USER_NONE.equals(username))
								SystemCache.userInfo.setUsername("null".equals(username)?"":username);
							
							SystemCache.userInfo.oasnickname = oasNickName;
						}
						
						cacheUserInfo(result);
						cacheUserInfo(SystemCache.userInfo.loginType>0?SystemCache.userInfo.loginType:1, SystemCache.userInfo.uid, SystemCache.userInfo.token, SystemCache.userInfo.username, userinfo.length>4?userinfo[4]:"", SystemCache.userInfo.platform, SystemCache.userInfo.oasnickname);
						
						if(callback != null)
							callback.success(SystemCache.userInfo, "0", "");
					}else{
						// 登录失败，在当前缓存上更新状态
						SystemCache.userInfo.setStatus("fail");
						
						String errorCode = o.getString("error");
						SystemCache.userInfo.setError(errorCode);
						SystemCache.userInfo.setErr_msg(o.getString("err_msg"));
						
						if("-40".equals(errorCode) && o.has("retinfo"))
							SystemCache.userInfo.recentUserList = parseData(o.getJSONObject("retinfo"));
						else if("-30".equals(errorCode) || "-31".equals(errorCode)){
							RecentUserList list = new RecentUserList();
							RecentUser re = new RecentUser();
							if(o.has("uid"))
								re.uid = o.getString("uid");
							if(o.has("third_uid"))
								re.third_uid = o.getString("third_uid");

							if(o.has("user_type"))
								re.loginType = o.getInt("user_type");
							else{
								String[] userinfo = recentlyuserinfos.split(SPLITCHAR);
								if(userinfo != null && userinfo.length > 0 && !TextUtils.isEmpty(userinfo[0]))
									re.loginType = Integer.valueOf(userinfo[0]);
							}
							
							if(o.has("platform"))
								re.platform = o.getString("platform");
							
							if(o.has("uname"))
								re.username = o.getString("uname");
							re.time = ""+System.currentTimeMillis()/1000;
							
							list.page = 1;
							list.pageCount = 1;
							list.pageSize = 1;									
							list.recentUser = new ArrayList<RecentUser>();
							list.recentUser.add(re);
							SystemCache.userInfo.recentUserList = list;
						}else if("-13".equals(errorCode) || "-14".equals(errorCode)){
							SystemCache.bindInfo = new UserInfo();
							SystemCache.bindInfo.error = SystemCache.userInfo.error;
							if(o.has("uid"))
								SystemCache.bindInfo.uid = o.getString("uid");
							if(o.has("user_type"))
								SystemCache.bindInfo.loginType = o.getInt("user_type");
							if(o.has("platform"))
								SystemCache.bindInfo.platform = o.getString("platform");
							if(o.has("uname"))
								SystemCache.bindInfo.username = o.getString("uname");
							if(o.has("nick_name"))
								SystemCache.bindInfo.oasnickname = o.getString("nick_name");
						}

						callback.fail("-1", "");
					}
					
				} catch (JSONException e) {
					Log.e("HttpService", "Result not json. Init SystemCache.userInfo fail!");
					if(callback != null)
						callback.excetpion(e);
				}
			}
			
			@Override
			public void handleErorrData(VolleyError error) {
				if(callback != null)
					callback.excetpion(new Exception(error.getMessage()+"Network is error!"));
			}
		}).submitGet();
	} 
	/**
	 * 注册新用户
	 * @param username	
	 * @param password	
	 * @return  成功	{status:"ok",uid:"20000000012345678",type:”2”,token:"690c122e35e2681fb34f9fef236396d0"}；type=1 是免注册用户，type=2 是正常用户
	 * 			失败     {status:"fail",error:"错误编号",err_msg:”错误描述”}
	 * @throws JSONException 
	 * @throws OasisSdkException 
	 * @throws OasisSdkDataErrorException 
	 * @throws UnsupportedEncodingException 
	 */
	public String register(String username, String password) 
			throws OasisSdkException, OasisSdkDataErrorException{
		
		StringBuffer url = new StringBuffer("a=Regist&m=NewUser");
		
//		url.append("&game_code="+SystemCache.GAMECODE);
//		url.append("&mobile_code="+BaseUtils.getMobileCode());			
		url.append("&username="+username);			
		try {
			url.append("&password="+URLEncoder.encode(password, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			url.append("&password="+password);
		}

		url.append("&sign="+MD5Encrypt.StringToMD5(BaseUtils.getMobileCode()+SystemCache.GAMECODE+username+password+SystemCache.PUBLICKEY));
				
		
		String res = loginAndRegist(2, MemberBaseInfo.USER_OASIS, username, password, username, url);
		BaseUtils.cacheLog(2, "Regist-NewUser" +
				"<br>Request Parameter:imei="+BaseUtils.getMobileCode()+";username="+username+";password="+password+
				("ok".equalsIgnoreCase(SystemCache.userInfo.status)?("<br>Response Result:Regist Success!uid="+SystemCache.userInfo.uid+";token="+SystemCache.userInfo.token+";type="+SystemCache.userInfo.type)
						:("<br>Response Result:Regist Fail!code="+SystemCache.userInfo.error+";msg="+SystemCache.userInfo.err_msg)));
		return res;
	}

	/**
	 * 登录和注册
	 * @param userType  		1:匿名登录,使用手机唯一码登录
								2:输入OAS平台账号和密码注册和登录
								3:使用第三方账号登录
	 * @param platform			平台类型
	 * @param username			usertype=1时，传手机唯一码
								usertype=2时，玩家输入的OAS平台账号（邮箱格式）
								usertype=3时，传入第三方的平台代码，例如facebook、twiiter、google
	 * @param password			usertype=2时，玩家输入的密码，
								usertype=3时，传入第三方的平台token
	 * @param oasNickName		昵称							
	 * @param validateID		需要验证的uid						
	 * @return  成功	{status:"ok",uid:"20000000012345678",type:”2”,token:"690c122e35e2681fb34f9fef236396d0"}；type=1 是免注册用户，type=2 是正常用户
	 * 			失败     {status:"fail",error:"错误编号",err_msg:”错误描述”}
	 * 				err_msg:	-1	签名未通过
	 * 							-2	OAS用户名或密码错误
	 * 							-3	注册的username已经存在
	 * 							-4	未知错误
	 * 							-5	oas_token过期
	 * @throws OasisSdkException 
	 * @throws OasisSdkDataErrorException 
	 */
	public String login(int userType, String platform, String username, String password, String oasNickName, String validateID) 
			throws OasisSdkException, OasisSdkDataErrorException{
		
		StringBuffer url = new StringBuffer("a=Login&m=UserLogin");
		
//		url.append("&game_code="+SystemCache.GAMECODE);
		url.append("&usertype="+userType);
//		url.append("&mobile_code="+BaseUtils.getMobileCode());
		
		if(userType == 3){
			url.append("&platform="+platform);
			url.append("&platform_token="+password);
			if(!TextUtils.isEmpty(validateID))
				url.append("&third_uid="+validateID);

			if(MemberBaseInfo.USER_FACEBOOK.equals(platform)){
				try {
					url.append("&nick_name="+URLEncoder.encode(oasNickName, "UTF-8"));
				} catch (Exception e) {
				}
				url.append("&uname="+"");				
			}else if(MemberBaseInfo.USER_GOOGLE.equals(platform)){
				url.append("&nick_name="+"");
				url.append("&uname="+username);				
			}
			url.append("&sign="+MD5Encrypt.StringToMD5(BaseUtils.getMobileCode()+SystemCache.GAMECODE+userType+platform+SystemCache.PUBLICKEY));
		}else if(userType == 2){
			url.append("&username="+username);
			try {
				url.append("&password="+URLEncoder.encode(password, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				url.append("&password="+password);
			}
			url.append("&nick_name="+"");
			url.append("&uname="+username);
			url.append("&sign="+MD5Encrypt.StringToMD5(BaseUtils.getMobileCode()+SystemCache.GAMECODE+userType+username+password+SystemCache.PUBLICKEY));
		}else if(userType == 1){
			url.append("&sign="+MD5Encrypt.StringToMD5(BaseUtils.getMobileCode()+SystemCache.GAMECODE+userType+SystemCache.PUBLICKEY));
			
		}
		
		String res = loginAndRegist(userType, platform, username, password, oasNickName, url);
		BaseUtils.cacheLog(2, "Login-"+
				(userType==1?"Anonymous<br>Request Parameter:imei="+BaseUtils.getMobileCode()
						:userType==2?"OG<br>Request Parameter:imei="+BaseUtils.getMobileCode()+";username="+username+";password="+password
								:"Other Plat<br>Request Parameter:imei="+BaseUtils.getMobileCode()+";platform=facebook;FBToken="+password) +
				("ok".equalsIgnoreCase(SystemCache.userInfo.status)?("<br>Response Result:Login Success!uid="+SystemCache.userInfo.uid+";token="+SystemCache.userInfo.token+";type="+SystemCache.userInfo.type)
						:("<br>Response Result:Login Fail!code="+SystemCache.userInfo.error+";msg="+SystemCache.userInfo.err_msg)));
		return res;
	}
	private String loginAndRegist(int userType, String platform, String username, String password, String oasNickName, StringBuffer url) throws OasisSdkException, OasisSdkDataErrorException{
		String result = "";
		
		try {
			result = HttpDao.instance().submit(getNewUrl(url.toString()));
		} catch (Exception e1) {
			throw new OasisSdkException(e1.getMessage());
		}
		
		try {
			JSONObject o = new JSONObject(result);
			if(SystemCache.userInfo == null)
				SystemCache.userInfo = new UserInfo();
			
			if("ok".equalsIgnoreCase(o.getString("status"))){
				SystemCache.userInfo.setUidOld(SystemCache.userInfo.getUid());
				
				// 登录成功，缓存用户信息
				SystemCache.userInfo.setStatus("ok");
				SystemCache.userInfo.setUid(o.getString("uid"));
				SystemCache.userInfo.setToken(o.getString("token"));
				SystemCache.userInfo.setType(o.getInt("type"));
				if(o.has("user_type"))
					SystemCache.userInfo.setLoginType(o.getInt("user_type"));				
				SystemCache.userInfo.setError("");
				SystemCache.userInfo.setErr_msg("");
				
				if(o.has("tiplogin"))
					SystemCache.userInfo.setTiplogin(o.getString("tiplogin"));
				else
					SystemCache.userInfo.setTiplogin("");
				
				if(o.has("tip_perfect_userinfo"))
					SystemCache.userInfo.setTip_perfect_userinfo(o.getString("tip_perfect_userinfo"));
				else
					SystemCache.userInfo.setTip_perfect_userinfo("");
				
				try {// 解析OG开关信息
					JSONObject offres = o.getJSONObject("onoff_res");
					SystemCache.controlInfo.setOg_onoff_control(offres.getString("og_onoff_control"));
					SystemCache.controlInfo.setCharge_onoff_control(offres.getString("charge_onoff_control"));
					SystemCache.controlInfo.setForum_onoff_control(offres.getString("forum_onoff_control"));
					SystemCache.controlInfo.setSwitching_onoff_control(offres.getString("switching_onoff_control"));
					SystemCache.controlInfo.setReg_onoff_control(offres.getString("reg_onoff_control"));
					SystemCache.controlInfo.setShare_onoff_control(offres.getString("share_onoff_control"));
					SystemCache.controlInfo.setCustom_onoff_control(offres.getString("custom_onoff_control"));
					SystemCache.controlInfo.setUserinfo_onoff_control(offres.getString("userinfo_onoff_control"));
					SystemCache.controlInfo.setHistory_logininfo_control(offres.getString("history_logininfo_control"));
					if(offres.has("charge_condition_res")){
						offres = offres.getJSONObject("charge_condition_res");
						SystemCache.controlInfo.setNetwork_condition(offres.getString("network_condition"));
						SystemCache.controlInfo.setPc_charge_condition(offres.getString("pc_charge_condition"));
						SystemCache.controlInfo.setEpin_onoff_control(offres.getString("Epin_exchange_condition"));
					}
					
				} catch (Exception e) {}
				
				// 获取充值状态
				int chargeable = 0;
				if(o.has("mb_charge_status")){
					if(!"yes".equals(o.getString("mb_charge_status"))){
						chargeable = 1;
					}
				}
				if(chargeable == 0 && o.has("uid_charge_status")){
					if(!"yes".equals(o.getString("uid_charge_status"))){
						chargeable = 2;
					}
				}
				SystemCache.userInfo.setChargeable(chargeable);
				
				if(o.has("platform"))
					SystemCache.userInfo.setPlatform(o.getString("platform"));
				else
					SystemCache.userInfo.setPlatform(platform);
				
				if(userType == 3){// 第三方账号登录
					SystemCache.userInfo.setPlatform_token(password);// 第三方token
				}else if(userType == 1 && SystemCache.userInfo.loginType == 3){// 匿名方式、第三方账号已关联该设备   
					SystemCache.userInfo.setPlatform_token("");
				}else{
					SystemCache.userInfo.setPlatform_token("");// 第三方token
				}
				String uname = "";
				String nick_name = "";
				if(o.has("uname"))
					uname = o.getString("uname");
				if(o.has("nick_name"))
					nick_name = o.getString("nick_name");
				
				if(SystemCache.userInfo.loginType == 2 && !TextUtils.isEmpty(uname) && !"null".equals(uname) ){// OAS
					SystemCache.userInfo.setUsername(uname);
					SystemCache.userInfo.oasnickname = "";
				}else if(SystemCache.userInfo.loginType == 3 && MemberBaseInfo.USER_GOOGLE.equalsIgnoreCase(SystemCache.userInfo.platform) 
						&& !TextUtils.isEmpty(uname) && !"null".equals(uname) ){// Google
					SystemCache.userInfo.setUsername(uname);
					SystemCache.userInfo.oasnickname = "";
				}else if(SystemCache.userInfo.loginType == 3 && MemberBaseInfo.USER_FACEBOOK.equalsIgnoreCase(SystemCache.userInfo.platform) 
						&& !TextUtils.isEmpty(nick_name) && !"null".equals(nick_name) ){// Facebook
					SystemCache.userInfo.setUsername("");
					SystemCache.userInfo.oasnickname = nick_name;
				}else{
					if(!MemberBaseInfo.USER_FACEBOOK.equals(username) && !MemberBaseInfo.USER_GOOGLE.equals(username)
							&& !MemberBaseInfo.USER_OASIS.equals(username) && !MemberBaseInfo.USER_NONE.equals(username))
						SystemCache.userInfo.setUsername("null".equals(username)?"":username);
					SystemCache.userInfo.oasnickname = oasNickName;
				}
				
				cacheUserInfo(result);
				cacheUserInfo(SystemCache.userInfo.loginType>0?SystemCache.userInfo.loginType:userType, SystemCache.userInfo.uid, SystemCache.userInfo.token, username, password, SystemCache.userInfo.platform, oasNickName);
			}else{
				// 登录失败，在当前缓存上更新状态
				SystemCache.userInfo.setStatus("fail");
//				if(o.has("platform"))
//					SystemCache.userInfo.setPlatform(o.getString("platform"));
//				else
//					SystemCache.userInfo.setPlatform(platform);
//				
//				if(userType == 3){// 第三方账号登录
//					SystemCache.userInfo.setPlatform_token(password);// 第三方token
//				}
				
				SystemCache.userInfo.setError(o.getString("error"));
				SystemCache.userInfo.setErr_msg(o.getString("err_msg"));
				if("-13".equals(SystemCache.userInfo.error) || "-14".equals(SystemCache.userInfo.error)){
					SystemCache.bindInfo = new UserInfo();
					SystemCache.bindInfo.error = SystemCache.userInfo.error;
					if(o.has("uid"))
						SystemCache.bindInfo.uid = o.getString("uid");
					if(o.has("user_type"))
						SystemCache.bindInfo.loginType = o.getInt("user_type");
					if(o.has("platform"))
						SystemCache.bindInfo.platform = o.getString("platform");
					if(o.has("uname"))
						SystemCache.bindInfo.username = o.getString("uname");
					if(o.has("nick_name"))
						SystemCache.bindInfo.oasnickname = o.getString("nick_name");
				}
			}
			
		} catch (JSONException e) {
			Log.e("HttpService", "Result not json. Init SystemCache.userInfo fail!");
			throw new OasisSdkDataErrorException("Login fail. Return data format error.");
		}
		
		if(!TextUtils.isEmpty(result) && result.contains("error"))// 服务器返回的数据有问题
			return result;
		
		return result;
	}
	public void parseUserInfo(String json){
		try {
			UserInfo user = new UserInfo();
			JSONObject o = new JSONObject(json);
			
			// 登录成功，缓存用户信息
			user.setStatus("ok");
			user.setUid(o.getString("uid"));
			user.setToken(o.getString("token"));
			user.setType(o.getInt("type"));
			if(o.has("user_type"))
				user.setLoginType(o.getInt("user_type"));				
			user.setError("");
			user.setErr_msg("");
			
			if(o.has("tiplogin"))
				user.setTiplogin(o.getString("tiplogin"));
			else
				user.setTiplogin("");
			
			if(o.has("tip_perfect_userinfo"))
				user.setTip_perfect_userinfo(o.getString("tip_perfect_userinfo"));
			else
				user.setTip_perfect_userinfo("");
			
			try {// 解析OG开关信息
				JSONObject offres = o.getJSONObject("onoff_res");
				ControlInfo control = new ControlInfo();
				control.setOg_onoff_control(offres.getString("og_onoff_control"));
				control.setCharge_onoff_control(offres.getString("charge_onoff_control"));
				control.setForum_onoff_control(offres.getString("forum_onoff_control"));
				control.setSwitching_onoff_control(offres.getString("switching_onoff_control"));
				control.setReg_onoff_control(offres.getString("reg_onoff_control"));
				control.setShare_onoff_control(offres.getString("share_onoff_control"));
				control.setCustom_onoff_control(offres.getString("custom_onoff_control"));
				control.setUserinfo_onoff_control(offres.getString("userinfo_onoff_control"));
				control.setHistory_logininfo_control(offres.getString("history_logininfo_control"));
				if(offres.has("charge_condition_res")){
					offres = offres.getJSONObject("charge_condition_res");
					control.setNetwork_condition(offres.getString("network_condition"));
					control.setPc_charge_condition(offres.getString("pc_charge_condition"));
					control.setEpin_onoff_control(offres.getString("Epin_exchange_condition"));
				}
				SystemCache.controlInfo = control;
			} catch (Exception e) {}
			
			// 获取充值状态
			int chargeable = 0;
			if(o.has("mb_charge_status")){
				if(!"yes".equals(o.getString("mb_charge_status"))){
					chargeable = 1;
				}
			}
			if(chargeable == 0 && o.has("uid_charge_status")){
				if(!"yes".equals(o.getString("uid_charge_status"))){
					chargeable = 2;
				}
			}
			user.setChargeable(chargeable);
			
			if(o.has("platform"))
				user.setPlatform(o.getString("platform"));
			
			String uname = "";
			String nick_name = "";
			if(o.has("uname"))
				uname = o.getString("uname");
			if(o.has("nick_name"))
				nick_name = o.getString("nick_name");
			
			if(user.loginType == 2 && !TextUtils.isEmpty(uname) && !"null".equals(uname)){// OAS
				user.setUsername(uname);
				user.oasnickname = "";
			}else if(user.loginType == 3 && MemberBaseInfo.USER_GOOGLE.equalsIgnoreCase(user.platform)
					&& !TextUtils.isEmpty(uname) && !"null".equals(uname)){// Google
				user.setUsername(uname);
				user.oasnickname = "";
			}else if(user.loginType == 3 && MemberBaseInfo.USER_FACEBOOK.equalsIgnoreCase(user.platform)
					&& !TextUtils.isEmpty(nick_name) && !"null".equals(nick_name)){// Facebook
				user.setUsername("");
				user.oasnickname = nick_name;
			}else{
				String recentlyuserinfos = (String) BaseUtils.getSettingKVPfromSysCache(Constant.SHAREDPREFERENCES_RECENTLYUSERINFOS, "");// usertype/uid/oastoken/username/password/platform/oasNickName
				if(!TextUtils.isEmpty(recentlyuserinfos)){
					String[] recent = recentlyuserinfos.split(SPLITCHAR);
					if(user.loginType == 3 && recent.length>4){// 第三方账号登录
						user.setPlatform_token(recent[4]);// 第三方token
					}
					
					user.setUsername(recent.length>3?recent[3]:"");
					
					user.oasnickname = recent.length>6?recent[6]:"";
				}
			}
			
			SystemCache.userInfo = user;
		} catch (Exception e) {
		}
	}
	/**
	 * 缓存用户信息
	 * @param userType  		1:匿名登录,使用手机唯一码登录
								2:输入OAS平台账号和密码注册和登录
								3:使用第三方账号登录
	 * @param username			usertype=1时，传手机唯一码
								usertype=2时，玩家输入的OAS平台账号（邮箱格式）
								usertype=3时，传入第三方的平台代码，例如facebook、twiiter、google
	 * @param password			usertype=2时，玩家输入的密码，
								usertype=3时，传入第三方的平台token
	 * @param platform			登录平台
	 * @param oasNickName			oas昵称
	 */
	private void cacheUserInfo(int userType, String uid, String token, String username, String password, String platform, String oasNickName){
		// 保存最近登录用户的信息，作为下次登录验证的凭据
		StringBuffer buf = new StringBuffer();
		buf.append(userType);
		buf.append("OASUSER");
		buf.append(uid);
		buf.append("OASUSER");
		buf.append(token);
		buf.append("OASUSER");
		buf.append(username);
		buf.append("OASUSER");
		buf.append("");// V3.3.0 开始，此处不再保存密码或token，避免信息泄露
		buf.append("OASUSER");
		buf.append(platform);
		buf.append("OASUSER");
		buf.append(oasNickName);
		buf.append("OASUSER");
		BaseUtils.saveSettingKVPtoSysCache(Constant.SHAREDPREFERENCES_RECENTLYUSERINFOS, buf.toString());
		
		if(userType==2 )// 非OAS账号登录或有异常，直接返回结果，不保存OAS用户信息
			BaseUtils.cacheUserInfo(username, password);
	}
	private void cacheUserInfo(String json){
		BaseUtils.saveSettingKVPtoSysCache(Constant.SHAREDPREFERENCES_CURRENTUSERINFOS, json);
	}
	/**
	 * 登录论坛
	 * @return
	 * @throws OasisSdkException
	 */
	public String loginToForum() throws OasisSdkException{
		if(SystemCache.userInfo == null)
			return "";
		StringBuffer url = new StringBuffer("http://wap.tr.forum.oasgames.com/ucp.php?mode=loginapi");
		url. append("&username="+SystemCache.userInfo.token);
		url. append("&password=");
		url. append("&anonymousflag=yes");
		url. append("&gamecode="+SystemCache.GAMECODE);
		url. append("&uid="+SystemCache.userInfo.uid);
		url.append("&lang="+PhoneInfo.instance().locale);
		String res = HttpDao.instance().submit(new RequestEntity(url.toString()));
		String jumpurl = "";
		try {
			JSONObject json = new JSONObject(res);
			if(0 == json.getInt("retcode")){
				jumpurl = json.getString("jumpurl");
			}
		} catch (JSONException e) {
			jumpurl = "";
		}
		return jumpurl;
	}
	/**
	 * 关联用户
	 * @param usertype	usertype=1  关联到一个新的OAS账号
						usertype=2  关联到一个已存在的OAS账号
						usertype=3  关联到第三方账号
	 * @param platfrom	平台类型
	 * @param username	玩家输入的用户名
						usertype=1时  新账号
						usertype=2时	旧账号
						usertype=3时，传入第三方的平台代码，例如facebook、twiiter、google
	 * @param password	玩家输入的密码
						usertype=1时	新密码
						usertype=2时	旧密码
						usertype=3时，传入第三方的平台token
						
	 * @param oasNickName	昵称（FB昵称，其他平台账号没有该值）
	 * @return	成功：{status:"ok",uid:"20000000012345678",type:”2”,token:"690c122e35e2681fb34f9fef236396d0"}
				失败：{status:"fail",error:"错误编号",err_msg:”错误描述”}
				err_msg	-1	sign验证失败
						-2	玩家输入的用户名和密码 有错误
						-3	账号已关联过，不能重复关联
						-4	参数不能为空
	 * @throws OasisSdkException
	 * @throws OasisSdkDataErrorException 
	 */
	public String bindUser(int usertype, String platfrom, String username, String password, String oasNickName) 
			throws OasisSdkException, OasisSdkDataErrorException{
		StringBuffer url = new StringBuffer("a=Relation&m=UserRela");
		
//		url.append("&mobile_code="+BaseUtils.getMobileCode());
		
//		url.append("&game_code="+SystemCache.GAMECODE);
		url.append("&relation_type="+usertype);
//		url.append("&uid="+SystemCache.userInfo.uid);
		url.append("&oas_token="+SystemCache.userInfo.token);
		if(usertype == 3){
			url.append("&platform="+platfrom);
			url.append("&platform_token="+password);
			if(MemberBaseInfo.USER_FACEBOOK.equals(platfrom)){
				try {
					url.append("&nick_name="+URLEncoder.encode(oasNickName, "UTF-8"));
				} catch (Exception e) {
				}
				url.append("&uname="+"");				
			}else if(MemberBaseInfo.USER_GOOGLE.equals(platfrom)){
				url.append("&nick_name="+"");
				url.append("&uname="+username);				
			}
			
		}else{
			url.append("&username="+username);
			try {
				url.append("&password="+URLEncoder.encode(password, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				url.append("&password="+password);
			}
			url.append("&nick_name="+"");
			url.append("&uname="+username);
		}
		
		url.append("&sign="+MD5Encrypt.StringToMD5(SystemCache.GAMECODE+SystemCache.userInfo.token+usertype+SystemCache.PUBLICKEY));
		String res = bindUser(usertype, platfrom, username, password, oasNickName, url);
		BaseUtils.cacheLog(2, "Relation-"+
				(usertype==1?"New OG<br>Request Parameter:imei="+BaseUtils.getMobileCode()+";username="+username+";password="+password
						:usertype==2?"Old OG<br>Request Parameter:imei="+BaseUtils.getMobileCode()+";username="+username+";password="+password
								:"Other Plat<br>Request Parameter:imei="+BaseUtils.getMobileCode()+";platform=facebook;FBToken="+password) +
				("ok".equalsIgnoreCase(SystemCache.userInfo.status)?("<br>Response Result:Relation Success!uid="+SystemCache.userInfo.uid+",token="+SystemCache.userInfo.token+",type="+SystemCache.userInfo.type)
						:("<br>Response Result:Relation Fail!code="+SystemCache.bindInfo.error+";msg="+SystemCache.bindInfo.err_msg)));
		return res;
	}
	private String bindUser(int usertype, String platform, String username, String password, String oasNickName, StringBuffer url) throws OasisSdkException, OasisSdkDataErrorException{
		String result = "";
		
		try {
			result = HttpDao.instance().submit(getNewUrl(url.toString()));
		} catch (Exception e1) {
			throw new OasisSdkException(e1.getMessage());
		}
		try {
			JSONObject o = new JSONObject(result);
			
			if("ok".equalsIgnoreCase(o.getString("status"))){
				SystemCache.bindInfo = null;
				
				if(SystemCache.userInfo == null)
					SystemCache.userInfo = new UserInfo();
				// 绑定成功，更新用户信息，如果绑定失败，不更新缓存
				SystemCache.userInfo.setStatus("ok");
				SystemCache.userInfo.setUid(o.getString("uid"));
				SystemCache.userInfo.setToken(o.getString("token"));
				SystemCache.userInfo.setType(o.getInt("type"));
				if(o.has("user_type"))
					SystemCache.userInfo.setLoginType(o.getInt("user_type"));
				SystemCache.userInfo.setError("");
				SystemCache.userInfo.setErr_msg("");
				
				if(usertype == 3){
					SystemCache.userInfo.setPlatform(platform);
					SystemCache.userInfo.setPlatform_token(password);// 第三方token
				}else{
					SystemCache.userInfo.setPlatform("");
					SystemCache.userInfo.setPlatform_token("");// 第三方token
				}
				String uname = "";
				String nick_name = "";
				if(o.has("uname"))
					uname = o.getString("uname");
				if(o.has("nick_name"))
					nick_name = o.getString("nick_name");
				
				if((usertype == 1 || usertype == 2) && !TextUtils.isEmpty(uname) && !"null".equals(uname)){// OAS
					SystemCache.userInfo.setUsername(uname);
					SystemCache.userInfo.oasnickname = "";
				}else if(usertype == 3 && MemberBaseInfo.USER_GOOGLE.equalsIgnoreCase(SystemCache.userInfo.platform) 
						&& !TextUtils.isEmpty(uname) && !"null".equals(uname)){// Google
					SystemCache.userInfo.setUsername(uname);
					SystemCache.userInfo.oasnickname = "";
				}else if(usertype == 3 && MemberBaseInfo.USER_FACEBOOK.equalsIgnoreCase(SystemCache.userInfo.platform) 
						&& !TextUtils.isEmpty(nick_name) && !"null".equals(nick_name)){// Facebook
					SystemCache.userInfo.setUsername("");
					SystemCache.userInfo.oasnickname = nick_name;
				}else{					
					SystemCache.userInfo.setUsername(username);
					SystemCache.userInfo.oasnickname = oasNickName;
				}
				cacheUserInfo(SystemCache.userInfo.loginType>0?SystemCache.userInfo.loginType:usertype, SystemCache.userInfo.uid, SystemCache.userInfo.token, username, password, platform, oasNickName);
			}else{
				SystemCache.bindInfo = new UserInfo();
				
				if(usertype == 3){
					SystemCache.bindInfo.setPlatform(platform);
					SystemCache.bindInfo.setPlatform_token(password);// 第三方token
					SystemCache.bindInfo.setUsername(username);
					SystemCache.bindInfo.setOasnickname(oasNickName);
				}
				SystemCache.bindInfo.setError(o.getString("error"));
				SystemCache.bindInfo.setErr_msg(o.getString("err_msg"));
				SystemCache.bindInfo.setRelation_type(""+usertype);
				if("-8".equals(SystemCache.bindInfo.getError())){
					SystemCache.bindInfo.setUid_from(o.getString("uid_from"));
					SystemCache.bindInfo.setUid_to(o.getString("uid_to"));
				}
				if("-13".equals(SystemCache.bindInfo.error) || "-15".equals(SystemCache.bindInfo.error)
						 || "-16".equals(SystemCache.bindInfo.error)){
					
					if(o.has("uid"))// TODO uid需要根据error类型做不同的处理
						SystemCache.bindInfo.uid = o.getString("uid");
					if(o.has("user_type"))
						SystemCache.bindInfo.loginType = o.getInt("user_type");
					if(o.has("platform"))
						SystemCache.bindInfo.platform = o.getString("platform");
					if(o.has("uname"))
						SystemCache.bindInfo.username = o.getString("uname");
					if(o.has("nick_name"))
						SystemCache.bindInfo.oasnickname = o.getString("nick_name");
				}
			}
		} catch (JSONException e) {
			Log.e("HttpService", "Init SystemCache.userInfo fail!");
			throw new OasisSdkDataErrorException("Bind failed");
		}
		return result;
	}
	
	/**
	 * 修改密码
	 * @throws OasisSdkException
	 * @throws OasisSdkDataErrorException 
	 */
	public Object[] modifyPwd(String password, String newpassword, String newpassword_repeat) throws OasisSdkException, OasisSdkDataErrorException{
		StringBuffer url = new StringBuffer("a=Login&m=Updatepasswd");
//		url.append("&game_code="+SystemCache.GAMECODE);
		url.append("&username="+SystemCache.userInfo.username);
		try {
			url.append("&password="+URLEncoder.encode(password, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			url.append("&password="+password);
		}
		try {
			url.append("&newpassword="+URLEncoder.encode(newpassword, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			url.append("&newpassword="+newpassword);
		}
		try {
			url.append("&newpassword_repeat="+URLEncoder.encode(newpassword_repeat, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			url.append("&newpassword_repeat="+newpassword_repeat);
		}
		url.append("&sign="+MD5Encrypt.StringToMD5(SystemCache.userInfo.username+password+SystemCache.PUBLICKEY));
		String result = "";
		try {
			result = HttpDao.instance().submit(getNewUrl(url.toString()));
		} catch (Exception e1) {
			throw new OasisSdkException(e1.getMessage());
		}
		try {
			JSONObject o = new JSONObject(result);
			if("ok".equalsIgnoreCase(o.getString("status"))){
				SystemCache.userInfo.setToken(o.getString("token"));// 此处要更新原有token，修改密码会导致旧token失效。
				cacheUserInfo(SystemCache.userInfo.loginType, SystemCache.userInfo.uid, SystemCache.userInfo.token, 
						SystemCache.userInfo.username, newpassword, SystemCache.userInfo.platform, SystemCache.userInfo.oasnickname);
				return new Object[]{true, 0, ""};
			}else
				return new Object[]{false, o.getInt("error"), o.getString("err_msg")};
		} catch (JSONException e) {
			Log.e("HttpService", "modifyPwd() fail!");
			throw new OasisSdkDataErrorException("数据异常");
		}
		
	}
	/**
	 * 提交服务器验证支付是否成功，(发游戏币)
	 * @param orderId			oas订单id
	 * @param purchaseToken		交易token
	 * @param productID			应用内商品id
	 * @param ext				扩展参数
	 * @throws OasisSdkException
	 * 
	 * return int  
	 * 			1000000:成功，
	 * 			1000001:验证信息错误（key无效）
	 * 			1000002:该购买交易成功并且已发钻成功
	 * 			1000003:支付完成，验证连接失败-连接超时或者无法连接上Google服务器，
	 * 			1000004：ProductID错误—一般不会出现BUG
	 * 			1000005:支付成功，但是发钻不成功，
	 * 			1000100:未知错误，
	 */
	public int checkPurchaseForGoogle(Purchase p, String separate) throws OasisSdkException{
		String[] info = p.getDeveloperPayload().split(separate);
		StringBuffer url = new StringBuffer("a=Pay&m=convertRequest");
		url.append("&actname=rechargeGoogle");
		url.append("&reqmethod=get");
		url.append("&localsign="+MD5Encrypt.StringToMD5(PhoneInfo.instance().softwareType+"rechargeGoogle"+"get"+SystemCache.PUBLICKEY));
		
		url.append("&game_code="+SystemCache.GAMECODE);
		url.append("&order_id="+p.getOrderId());
		url.append("&token="+p.getToken());
		url.append("&product_id="+p.getSku());
		url.append("&uid="+info[0]);
		url.append("&sid="+info[1]);
		if(info.length >= 6 && ("android".equalsIgnoreCase(info[5]) || "all".equalsIgnoreCase(info[5]) || "test".equalsIgnoreCase(info[5]) ))
			url.append("&stype="+info[5]);
		else
			url.append("&stype="+SystemCache.userInfo.serverType);
		if(info.length >= 7)
			url.append("&oas_orderid="+info[6]);
		
		url.append("&roleid="+info[2]);
		url.append("&ext="+info[3]);
		url.append("&trace_signture="+URLEncoder.encode(p.getSignature()));
		url.append("&trace_data="+p.getOriginalJson());
		url.append("&sign="+MD5Encrypt.StringToMD5(SystemCache.GAMECODE+SystemCache.PAYKEY+p.getOrderId()+p.getToken()+p.getSku()+info[0]+info[1]+info[2]));
		String result = HttpDao.instance().submit(new RequestEntity(getNewestUrl(url.toString(), false), true));
		BaseUtils.logError("OAS-HttpService", "checkPurchaseForGoogle() return result:"+result);
		try {
			JSONObject o = new JSONObject(result);
			
//			if("ok".equalsIgnoreCase(o.getString("status"))){
//				return 0;
//			}else{
				String errorCode = o.getString("code");
				
				BaseUtils.logError(TAG, "发钻请求结果：OasisOrderid="+ (info.length>=7?info[6]:"") +", GoogleOrderid="+p.getOrderId()+", uid="+info[0]+", sid="+info[1]+", roleid="+info[2]+", ext="+info[3]+", Result Code="+errorCode+"");
				
				if(TextUtils.isEmpty(errorCode)){
					return 1000100;
				}
				return Integer.valueOf(errorCode);
//			}
		} catch (JSONException e) {
			return 1000100;// 请求发送成功，但服务器返回格式不正确，处理为1000100，失败
		}
	}
	public int checkPurchaseForGoogleBySandBox(Purchase p) throws OasisSdkException{
		StringBuffer url = new StringBuffer("a=Recharge&m=Androidpay");
//		url.append("&game_code="+SystemCache.GAMECODE);
		url.append("&order_id="+p.getOrderId());
		url.append("&token="+p.getToken());
		url.append("&product_id="+p.getSku());
//		url.append("&uid="+SystemCache.userInfo.uid);
		url.append("&sid="+SystemCache.userInfo.serverID);
		url.append("&stype="+SystemCache.userInfo.serverType);
		
		url.append("&roleid="+SystemCache.userInfo.roleID);
		url.append("&ext=");
		url.append("&trace_signture=");
		url.append("&trace_data=");
//		url.append("&mobile_code="+BaseUtils.getMobileCode());
		url.append("&sign="+MD5Encrypt.StringToMD5(SystemCache.GAMECODE+p.getSku()+SystemCache.userInfo.uid+SystemCache.PAYKEY));
		String result = HttpDao.instance().submit(getNewUrl(url.toString()));
		try {
			JSONObject o = new JSONObject(result);
//	oas_msg		
//			if("ok".equalsIgnoreCase(o.getString("status"))){
//				return 0;
//			}else{
			String errorCode = o.getString("code");
			BaseUtils.cacheLog(2, "Google-Payment-sandbox" +
					"<br>Request Parameter:product_id="+p.getSku()+",uid="+SystemCache.userInfo.uid+",sid="+SystemCache.userInfo.serverID+",stype="+SystemCache.userInfo.serverType+",roleid="+SystemCache.userInfo.roleID+
					"<br>Response Result:code="+errorCode+","+o.getString("oas_msg"));
			if(TextUtils.isEmpty(errorCode)){
				return 1000100;
			}
			return Integer.valueOf(errorCode);
//			}
		} catch (JSONException e) {
			return 1000100;// 请求发送成功，但服务器返回格式不正确，处理为1000100，失败
		}
	}
	/**
	 * 获取第三方交易套餐
	 * @throws OasisSdkException 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getPayKindsInfo(final CallbackResultForActivity callback ){
		
//		StringBuffer url = new StringBuffer("http://pay.oasgames.com/payment/api/getPackageInfo.php?");// 2.4以前使用此地址
//		StringBuffer url = new StringBuffer("http://pay.oasgames.com/payment/api/getPackageInfo_v2.php?");// 2.5开始使用此地址
		StringBuffer url = new StringBuffer("a=Pay&m=convertRequest");// 2.5开始使用此地址
		
		url.append("&actname=getPackageInfo_v2");
		url.append("&reqmethod=get");
		url.append("&localsign="+MD5Encrypt.StringToMD5(PhoneInfo.instance().softwareType+"getPackageInfo_v2"+"get"+SystemCache.PUBLICKEY));
		
		String country = PhoneInfo.instance().ipToCountry;
		if(TextUtils.isEmpty(country))
			country = PhoneInfo.instance().iso2Country;// 来自sim卡
		
		if(TextUtils.isEmpty(country))
			country = Locale.getDefault().getISO3Country();// 通过本地语言获取
//		country = "DEU";//"CHN";//"BRA";//"DE";//"MYS";//
		url.append("&gid="+SystemCache.GAMECODE);
		url.append("&country=" + country);// BRA 
		url.append("&ipCountry=" + PhoneInfo.instance().ipToCountry);// 来自IP
		url.append("&simCountry=" + PhoneInfo.instance().iso2Country);// 来自sim卡
		url.append("&localeCountry=" + Locale.getDefault().getISO3Country());// 通过本地语言获取
		url.append("&order=ASC&type=android"); 
		url.append("&oaskey="+MD5Encrypt.StringToMD5(MD5Encrypt.StringToMD5(SystemCache.GAMECODE+country+SystemCache.PAYKEY)+SystemCache.PAYKEY));
		
		new OasisSdkHttpClient(getNewestUrl(url.toString()), null, new OasisSdkHttpClient.Callback() {
			
			@Override
			public void handleResultData(String res) {
				try {
					JSONObject o = new JSONObject(res);
					if("ok".equalsIgnoreCase(o.getString("status"))){
						ArrayList<PayInfoList> list = new ArrayList<PayInfoList>();
						JSONArray result = new JSONArray(o.getString("result"));
						int resultLen = result.length();
						for (int i = 0; i < resultLen; i++) {
							PayInfoList payInfoList = new PayInfoList();
							JSONObject payObj = result.getJSONObject(i);
							Iterator it = payObj.keys();
							while (it.hasNext()) {
								String type = (String) it.next();
								Object value = payObj.get(type);
								if(type.equals("pay_way")){
									payInfoList.setPay_way(String.valueOf(value));
								}else if(type.equals("price")){
									JSONArray childPayJson = new JSONArray(String.valueOf(value));
									int childLen = childPayJson.length();
									List childList = new ArrayList();
									for (int j = 0; j < childLen; j++) {
										PayInfoDetail payDetails = new PayInfoDetail();
										JsonParser.newInstance().parserJson2Obj(childPayJson.get(j).toString(), payDetails);
										childList.add(payDetails);
									}
									payInfoList.setList(childList);
								}else if(type.equals("pay_way_config")){
									String config = String.valueOf(value);
									if(!TextUtils.isEmpty(config)){
										PayConfigInfo configInfo = new PayConfigInfo();
										JsonParser.newInstance().parserJson2Obj(config, configInfo);
										payInfoList.setPay_way_config(configInfo);
									}
								}
							}
							list.add(payInfoList);
						}
						if(list != null && !list.isEmpty())
							SystemCache.payInfoLists  = list;
						if(callback != null)
							callback.success(list, "1", "");
					}else{
						if(callback != null)
							callback.fail("", "");
					}
				} catch (JSONException e) {
					Log.e("HttpService", "getPayKindsInfo() fail!");
					if(callback != null)
						callback.fail("", "");
				} catch (OasisSdkException e) {
					if(callback != null)
						callback.fail("", "");
				}
				
			}
			
			@Override
			public void handleErorrData(VolleyError error) {
				if(callback != null)
					callback.excetpion(new OasisSdkException(""));
			}
		}).submitPost();
	}
	/**
	 * 交易前，先下订单
	 * @param productid 	应用内商品id
	 * @param ext		 	扩展参数
	 * 
	 * @throws OasisSdkException 
	 */
	public String sendOrder(String productid, String ext) throws OasisSdkException{
		StringBuffer url = new StringBuffer("a=Pay&m=convertRequest");
//		String uid = "200000031029748";
//		String sid = "1";
//		String stype = "all";
//		String gamecode = "mloru";
//		String roleID = "2";
//		productid = "prop.mloru.300.10382";
//		url.append("gid="+gamecode);
//		url.append("&sid="+sid);
//		url.append("&stype="+stype);
//		url.append("&uid="+uid);
//		url.append("&pid="+productid);
//		url.append("&roleid="+roleID);
//		url.append("&ext="+ext);
//		
//		url.append("&oaskey="+MD5Encrypt.StringToMD5(MD5Encrypt.StringToMD5(gamecode+sid+uid+productid+roleID+SystemCache.PAYKEY)+SystemCache.PAYKEY));
		
		url.append("&actname=getPlacedOrder");
		url.append("&reqmethod=get");
		url.append("&localsign="+MD5Encrypt.StringToMD5(PhoneInfo.instance().softwareType+"getPlacedOrder"+"get"+SystemCache.PUBLICKEY));
		
		url.append("&gid="+SystemCache.GAMECODE);
		url.append("&sid="+SystemCache.userInfo.serverID);
		url.append("&stype="+SystemCache.userInfo.serverType);
		url.append("&uid="+SystemCache.userInfo.uid);
		url.append("&pid="+productid);
		url.append("&roleid="+SystemCache.userInfo.roleID);
		try {
			url.append("&ext="+URLEncoder.encode(ext, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			url.append("&ext="+URLEncoder.encode(ext));
		}
		
		url.append("&oaskey="+MD5Encrypt.StringToMD5(MD5Encrypt.StringToMD5(SystemCache.GAMECODE+SystemCache.userInfo.serverID+SystemCache.userInfo.uid+productid+SystemCache.userInfo.roleID+SystemCache.PAYKEY)+SystemCache.PAYKEY));
		String result = HttpDao.instance().submit(new RequestEntity(getNewestUrl(url.toString())));
		try {
			JSONObject o = new JSONObject(result);
			
			if("ok".equalsIgnoreCase(o.getString("status"))){
				return o.getString("result");
			}
		} catch (JSONException e) {
			Log.e("HttpService", "sendOrder() fail!");
			return "";
		}
		Log.e("HttpService", "sendOrder() fail!"+result);
		return "";
	}
	public int checkPurchaseForInfobip(PurchaseResponse purchase, String productID, String orderId) throws OasisSdkException{
		StringBuffer url = new StringBuffer("a=Pay&m=convertRequest");
		
		url.append("&actname=rechargeInfobip");
		url.append("&reqmethod=get");
		url.append("&localsign="+MD5Encrypt.StringToMD5(PhoneInfo.instance().softwareType+"rechargeInfobip"+"get"+SystemCache.PUBLICKEY));
		
		url.append("&game_code="+SystemCache.GAMECODE);
		url.append("&order_id="+orderId);
		url.append("&product_id="+productID);
		url.append("&uid="+SystemCache.userInfo.uid);
		url.append("&sid="+SystemCache.userInfo.serverID);
		url.append("&stype="+SystemCache.userInfo.serverType);
		url.append("&purchase_id="+purchase.getTransactionId());
		url.append("&roleid="+SystemCache.userInfo.roleID);
		
		url.append("&currency="+purchase.getCurrency());
		url.append("&itemamount="+purchase.getItemAmount());
		url.append("&price="+purchase.getPrice());
		
		url.append("&sign="+MD5Encrypt.StringToMD5(SystemCache.GAMECODE+orderId+productID+SystemCache.userInfo.uid+SystemCache.userInfo.serverID+purchase.getTransactionId()+SystemCache.userInfo.roleID+SystemCache.PAYKEY));
		
		String result = HttpDao.instance().submit(new RequestEntity(getNewestUrl(url.toString())));
		try {
			JSONObject o = new JSONObject(result);
			String errorCode = o.getString("code");
			
			if(TextUtils.isEmpty(errorCode)){
				return 1000100;
			}
			return Integer.valueOf(errorCode);
		} catch (JSONException e) {
			Log.e("HttpService", "checkPurchaseForInfobip() fail!");
			return 1000100;
		}
	}
	/**
	 * 扫描结束后，根据扫描结果请求更新充值界面
	 * @param code	扫描结果码
	 * @return
	 * @throws OasisSdkException
	 */
	public boolean toPcRecharge(String code) throws OasisSdkException{

		StringBuffer url = new StringBuffer("a=pay&m=setPayWish");
		url.append("&gamecode="+SystemCache.GAMECODE);
		url.append("&sid="+SystemCache.userInfo.serverID);
		url.append("&uid="+SystemCache.userInfo.uid);
		url.append("&roleid="+SystemCache.userInfo.roleID);
		url.append("&wcode="+code);
		url.append("&token="+SystemCache.userInfo.token);
		url.append("&sign="+MD5Encrypt.StringToMD5(code+SystemCache.userInfo.uid+SystemCache.GAMECODE+SystemCache.userInfo.serverID+SystemCache.userInfo.roleID+SystemCache.userInfo.token+SystemCache.PAYKEY));
		
		String result = HttpDao.instance().submit(getNewUrl(url.toString()));
		try {
			JSONObject o = new JSONObject(result);
			if("ok".equalsIgnoreCase(o.getString("status"))){
				return true;
			}
		} catch (JSONException e) {
			Log.e("HttpService", "toPcRecharge() fail!");
			return false;
		} 
		return false;
	} 
	/**
	 * 将 FB请求发送到服务器
	 * @param requestID
	 * @param ids
	 * @param objectID
	 * @param type
	 * @return
	 * @throws OasisSdkException
	 */
	public boolean setFbRequest(String requestID, String ids, String objectID, int type) throws OasisSdkException{
//		http://apisdk.mobile.oasgames.com/2.9/?a=fbrequest&m=uRequest&serverid=1&requestid=2&objectid=3&uid_from=123&uid_to=456&type=1
		StringBuffer url = new StringBuffer("a=fbrequest&m=uRequest");
		url.append("&gamecode="+SystemCache.GAMECODE);
		url.append("&uid_from="+SystemCache.userInfo.uid);
		url.append("&roleid="+SystemCache.userInfo.roleID);
		url.append("&serverid="+SystemCache.userInfo.serverID);
		url.append("&requestid="+requestID);
		url.append("&objectid="+objectID);
		url.append("&uid_to="+ids);
		url.append("&type="+type);
		
		String result = HttpDao.instance().submit(getNewUrl(url.toString()));
		try {
			JSONObject o = new JSONObject(result);
			if("ok".equalsIgnoreCase(o.getString("status"))){
				return true;
			}
		} catch (JSONException e) {
			Log.e("HttpService", "setFbRequest fail!");
			return false;
		} 
		return false;
	}
	public boolean getFbRequestForGift(String requestID) throws OasisSdkException{
//		http://apisdk.mobile.oasgames.com/2.9/?a=fbrequest&m=uRequest&serverid=1&requestid=2,3,1&uid_to=456
		StringBuffer url = new StringBuffer("a=fbrequest&m=uResponse");
		url.append("&gamecode="+SystemCache.GAMECODE);
		url.append("&uid_to="+SystemCache.userInfo.uid);
		url.append("&roleid="+SystemCache.userInfo.roleID);
		url.append("&serverid="+SystemCache.userInfo.serverID);
		url.append("&requestid="+requestID);
		
		String result = HttpDao.instance().submit(getNewUrl(url.toString()));
		try {
			JSONObject o = new JSONObject(result);
			if("ok".equalsIgnoreCase(o.getString("status"))){
				return true;
			}
		} catch (JSONException e) {
			Log.e("HttpService", "getFbRequestForGift() fail!");
			return false;
		} 
		return false;
	}
	

	/**
	 * 支付信息日志
	 * @param page 页数
	 * @param page_size 每页记录数
	 * @throws JSONException 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public PayHistoryList paymentLog(int page, int page_size) throws OasisSdkException, JSONException{
		StringBuffer url = new StringBuffer("a=Pay&m=convertRequest");
		url.append("&actname=getRechargeOrdersV2");
		url.append("&reqmethod=get");
		url.append("&localsign="+MD5Encrypt.StringToMD5(PhoneInfo.instance().softwareType+"getRechargeOrdersV2"+"get"+SystemCache.PUBLICKEY));
		
		String gamecode = SystemCache.GAMECODE;//"mlocpt";//"msfen";//"msfen";//mlobr		mlocpt		mloctr
		String uid = SystemCache.userInfo.uid;//"200000031029748";//"200000042906158";//"200000080566844";//
//		url.append("&uid="+uid);
//		url.append("&game_code="+gamecode);
		url.append("&ostype="+PhoneInfo.instance().softwareType);
		url.append("&page="+page);
		url.append("&page_size="+page_size);
		url.append("&msg=getRecharge");
		
		url.append("&token="+MD5Encrypt.StringToMD5(uid+gamecode+page+"d9411ce0301eb928632daacf1431ec9f"+page_size));
//		String res = HttpDao.instance().submit(new RequestEntity(Constant.BASEURL_TEST+url.toString()));
		String res = HttpDao.instance().submit(new RequestEntity(getNewestUrl(url.toString())));
		JSONObject json = new JSONObject(res);
		if("ok".equalsIgnoreCase(json.getString("status"))){
			PayHistoryList list = new PayHistoryList();
			list.setGame_code(json.getString("game_code"));
			list.setPage(json.getInt("page"));
			list.setPage_size(json.getInt("page_size"));
			try {
				list.setMsg((List)JsonParser.newInstance().parserJSON2ObjList(json.getJSONArray("msg").toString(), new PayHistoryInfoDetail()));
			} catch (Exception e) {
				throw new OasisSdkException(e.getMessage());
			}
			return list;
		}
		return null;
	}
	
	/**
	 * 玩家登录游戏服事件接收接口
	 * 为OAS服务器发送玩家数据
	 * @return
	 * @throws OasisSdkException
	 */
	public void game_play_log() throws OasisSdkException{
		StringBuffer url = new StringBuffer("a=Login&m=ReportInfo");
		
		url.append("&server_id="+SystemCache.userInfo.serverID);
		url.append("&server_type="+SystemCache.userInfo.serverType);
		try {
			url.append("&server_name="+URLEncoder.encode(SystemCache.userInfo.serverName, "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		url.append("&role_id="+SystemCache.userInfo.roleID);
		try {
			url.append("&role_name="+URLEncoder.encode(SystemCache.userInfo.gameNickname, "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String uname = "";
		String nick_name = "";
		if(SystemCache.userInfo.loginType == 1){
			uname = "";
			nick_name = "";					
		}else if(SystemCache.userInfo.loginType == 2){
			uname = SystemCache.userInfo.username;
			nick_name = "";
		}else if(SystemCache.userInfo.loginType == 3 && MemberBaseInfo.USER_GOOGLE.equals(SystemCache.userInfo.platform)){
			uname = SystemCache.userInfo.username;
			nick_name = "";
		}else if(SystemCache.userInfo.loginType == 3 && MemberBaseInfo.USER_FACEBOOK.equals(SystemCache.userInfo.platform)){
			uname = "";
			nick_name = SystemCache.userInfo.oasnickname;
		}
		try {
			url.append("&uname="+URLEncoder.encode(uname, "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			url.append("&nick_name="+URLEncoder.encode(nick_name, "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		BaseUtils.logDebug(TAG, "Report Info:"+url.toString());
		url.append(PhoneInfo.instance().toString());
		
		url.append("&sign="+MD5Encrypt.StringToMD5(BaseUtils.getMobileCode() + SystemCache.GAMECODE+SystemCache.PUBLICKEY));
		
		HttpDao.instance().submit(getNewUrl(url.toString()));
	}
	/**
	 * 根据IP获取国家或地区
	 * @return
	 * @throws OasisSdkException
	 */
	public void getConutryCodeByIP(final CallbackResultForActivity callback) {
		new OasisSdkHttpClient("http://ipinfo.io/json", null, new OasisSdkHttpClient.Callback() {
			
			@Override
			public void handleResultData(String result) {
				try {
					JSONObject o = new JSONObject(result);

					PhoneInfo.instance().setIpToCountry(o.getString("country"));
					callback.success("", "", "");
				} catch (JSONException e) {
					BaseUtils.logDebug("getConutryCodeByIp()", "Get country by ipinfo is failed.");
					callback.fail("", "");
				} 
			}
			
			@Override
			public void handleErorrData(VolleyError error) {
				callback.excetpion(null);
			}
		}).submitGet();		
	}
	/**
	 * 根据Gamecode获取Mdata相关的配置信息
	 * @throws OasisSdkException 
	 */
	public void getAdjustConfigInfos() throws OasisSdkException{
		StringBuffer url = new StringBuffer("a=token&m=getInfo");
		
		url.append("&game_code="+SystemCache.GAMECODE);
		url.append("&os=android");
		
		String res = HttpDao.instance().submit(getNewUrl(url.toString()));
		try {
			JSONObject o = new JSONObject(res);
			if("ok".equalsIgnoreCase(o.getString("status"))){
				Map<String, String> map = JsonParser.newInstance().parserJSON2Map(o.getJSONObject("val").toString(), "adjust");
				if(map != null)
					SystemCache.adjustEventMap = map;
			}
		} catch (JSONException e) {
			
		} 
		
	}
	/**
	 * 向MData发送数据
	 * @param info
	 * @throws OasisSdkException 
	 */
	public void sendToMdataInfo(ReportMdataInfo info) throws OasisSdkException{
//		RequestEntity re = new RequestEntity("http://10.1.9.135/r2.php");
		String main = "us.";
		if("tr.".equals(PhoneInfo.instance().getIpToCountryWithHttp()) || 
				"us.".equals(PhoneInfo.instance().getIpToCountryWithHttp()) || 
				"br.".equals(PhoneInfo.instance().getIpToCountryWithHttp()) || 
				"cn.".equals(PhoneInfo.instance().getIpToCountryWithHttp()) ){
			main = PhoneInfo.instance().getIpToCountryWithHttp();
		}
			
//		RequestEntity re = new RequestEntity("http://"+PhoneInfo.instance().getIpToCountryWithHttp()+"r2.trtromg.com/r2.php");
		RequestEntity re = new RequestEntity("http://"+main+"mdata.cool/mdata.php");
		
		re.xml = info.content;
		
		
		
//		BaseUtils.logDebug("HttpService_mdata", "MData request url:"+re.url);
//		BaseUtils.logDebug("HttpService_mdata", "MData request xmlxml:"+re.xml);
		HttpDao.instance().submit(re);
//		BaseUtils.logDebug("HttpService_mdata", "MData result:"+HttpDao.instance().submit(re));
	}
//	/**
//	 * 获取是否需要跟踪的状态
//	 * @return
//	 */
//	public boolean isTrack(){
//		if(!SystemCache.NetworkisAvailable)// 如果网络不可用，直接返回true，表示对该设备进行跟踪。
//			return true;
//		StringBuffer res = new StringBuffer();
//		HttpURLConnection conn;
//		StringBuilder entityBuilder = new StringBuilder("&game_code=" + SystemCache.GAMECODE + 
//				"&mobile_code=" + BaseUtils.getMobileCode() +
//				"&sign=" + MD5Encrypt.StringToMD5(SystemCache.GAMECODE+BaseUtils.getMobileCode()+SystemCache.PUBLICKEY)
//				+ PhoneInfo.instance().toString());
//		try {
////			byte[] entity = entityBuilder.toString().getBytes();
//			URL url = new URL(Constant.BASEURL_2_5 + "a=Login&m=ReportStatus"+entityBuilder.toString());
//			BaseUtils.logDebug(TAG, "请求URL="+Constant.BASEURL_2_5 + "a=Login&m=ReportStatus"+entityBuilder.toString());
//			conn = (HttpURLConnection) url.openConnection();
//			conn.setConnectTimeout(2000);
//			conn.setReadTimeout(2000);
//			conn.setRequestMethod("POST");
//			conn.setDoOutput(true);//允许输出数据
//			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
////			conn.setRequestProperty("Content-Length", String.valueOf(entity.length));
////			OutputStream outStream = conn.getOutputStream();
////			outStream.write(entity);
////			outStream.flush();
////			outStream.close();
//			if(conn.getResponseCode() == 200){
//				InputStream ins = conn.getInputStream();
//				BufferedReader rd = new BufferedReader(new InputStreamReader(ins));
//				String line ;
//				  while ((line = rd.readLine()) != null) {   
//					  res.append(line);
//		            }   
//			}
//			BaseUtils.logDebug(TAG, "请求结果="+res.toString());
//			JSONObject json = new JSONObject(res.toString());
//			if(!"ok".equalsIgnoreCase(json.getString("status"))){
//				return true;
//			}else{
//				if(!"y".equalsIgnoreCase(json.getString("report_status")))
//					return false;
//				else
//					return true;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return true;
//		}
//	}
	/**
	 * 获取最新的信息   （目前 只获取 客户回复的数量）
	 * @param callback
	 */
	public void getNewsInfo(final CallbackResultForActivity callback){
		if(SystemCache.userInfo == null 
				|| SystemCache.controlInfo == null
				|| !SystemCache.controlInfo.getCustom_onoff_control())
			return;
		StringBuffer url = new StringBuffer("a=Custom&m=GetNewreplyinfo");
		String gamecode = SystemCache.GAMECODE;//"mihtr";//
		String uid = SystemCache.userInfo.uid;//"200000053568227";//
		
//		url.append("&game_code="+gamecode);
//		url.append("&uid="+uid);
		url.append("&oas_token="+SystemCache.userInfo.token);
		url.append("&sign="+MD5Encrypt.StringToMD5(gamecode+uid+SystemCache.userInfo.token+SystemCache.PUBLICKEY));
		
		new OasisSdkHttpClient(getNewestUrl(url.toString()), null, new OasisSdkHttpClient.Callback() {
			
			@Override
			public void handleResultData(String result) {
				
				try {
					JSONObject json = new JSONObject(result);
					if(SystemCache.userInfo != null && "ok".equalsIgnoreCase(json.getString("status"))){
						if(json.getString("reply_status").startsWith("y") || json.getString("reply_status").startsWith("Y"))
							SystemCache.userInfo.isShowCustomerNewsFlag = true;
						else
							SystemCache.userInfo.isShowCustomerNewsFlag = false;
						if(callback != null)
							callback.success(json.get("reply_status"), "0", "success");
					}else{
						if(callback != null)
							callback.fail("-1", json.getString("msg"));
					}
				} catch (Exception e) {
					if(callback != null)
						callback.excetpion(new OasisSdkException(e.getMessage()));
				}
			}
			
			@Override
			public void handleErorrData(VolleyError error) {
				if(callback != null)
					callback.excetpion(new OasisSdkException(error.getMessage()));
			}
		}).submitGet();
	}
	/**
	 * 获取客服问题列表
	 * @param questionStatus
	 * @param page
	 * @param page_size
	 * @return
	 * @throws OasisSdkException
	 * @throws JSONException
	 */
	public void getCustomerServiceQuestionList(int questionStatus, int page, int page_size, final CallbackResultForActivity callback){
		StringBuffer url = new StringBuffer("a=Custom&m=GetQuestionList");
		String gamecode = SystemCache.GAMECODE;//"mloen";//
		String uid = SystemCache.userInfo.uid;//"200000052423938";//
		
//		url.append("&game_code="+gamecode);
//		url.append("&uid="+uid);
		url.append("&CurPage="+page);
		url.append("&every_page_count="+page_size);
		url.append("&QuestionStatus="+questionStatus);
		url.append("&oas_token="+SystemCache.userInfo.token);
		url.append("&sign="+MD5Encrypt.StringToMD5(gamecode+uid+SystemCache.userInfo.token+SystemCache.PUBLICKEY));
		
//		http://apisdk.mobile.oasgames.com/3.1/?a=custom&m=GetQuestionList&uid=200000052423938&game_code=mloen&every_page_count=2&QuestionStatus=1
		new OasisSdkHttpClient(getNewestUrl(url.toString()), null, new OasisSdkHttpClient.Callback() {
			
			@Override
			public void handleResultData(String result) {
				QuestionList list = null;
				JSONObject json = null;
				String status = null;
				String errorMsg = null;
				try {
					json = new JSONObject(result);
					status = json.getString("status");
					if("ok".equalsIgnoreCase(status)){
						list = new QuestionList();
						list.setCurPage(""+json.getInt("CurPage"));
						list.setTotalPage(""+json.getInt("TotalPage"));
//						list.setPage_size(json.getInt("every_page_count"));
						list.setQuestionStatus(""+json.getInt("QuestionStatus"));
						if(json.has("question_list"))
							list.setQuestion_list((List)JsonParser.newInstance().parserJSON2ObjList(json.getJSONArray("question_list").toString(), new QuestionInfo()));
					}else{
						errorMsg = json.getString("msg");
					}
				} catch (Exception e) {
					callback.fail("-1", e.getMessage());
				}
				
				if("ok".equalsIgnoreCase(status)){
					callback.success(list, "0", "success");
				}else{
					callback.fail("-1", errorMsg);
				}
				
			}
			
			@Override
			public void handleErorrData(VolleyError error) {
				callback.excetpion(new OasisSdkException(error.getMessage()));
			}
		}).submitGet();
	}
	/**
	 * 获取问题类型
	 * @param callback
	 */
	public void getCustomerServiceQuestionType(final CallbackResultForActivity callback){
		StringBuffer url = new StringBuffer("a=Custom&m=GetQuestionType");
		String gamecode = SystemCache.GAMECODE;//"mloen";//
		
//		url.append("&game_code="+gamecode);
		url.append("&sign="+MD5Encrypt.StringToMD5(gamecode+SystemCache.PUBLICKEY));
		
		new OasisSdkHttpClient(getNewestUrl(url.toString()), null, new OasisSdkHttpClient.Callback() {
			
			@Override
			public void handleResultData(String result) {
				List<Object> list = null;
				
				try {
					JSONObject json = new JSONObject(result);
					if("ok".equalsIgnoreCase(json.getString("status"))){
						list = JsonParser.newInstance().parserJSON2ObjList(json.getString("question_type"), new QuestionType());
						callback.success(list, "0", "success");
					}else{
						callback.fail("-1", json.has("msg")?json.getString("msg"):"Not error message!");
					}
				} catch (Exception e) {
					callback.fail("-1", e.getMessage());
				}
			}
			
			@Override
			public void handleErorrData(VolleyError error) {
				callback.excetpion(new OasisSdkException(error.getMessage()));
			}
		}).submitGet();
	}
	/**
	 * 创建问题
	 * @param callback
	 */
	public void createQuestion(String questionTypeId, final CallbackResultForActivity callback){
		StringBuffer url = new StringBuffer("a=Custom&m=CreateNewquestion");
		String gamecode = SystemCache.GAMECODE;//"mloen";//
		
//		url.append("&game_code="+gamecode);
//		url.append("&uid="+SystemCache.userInfo.uid);
		if(SystemCache.userInfo.loginType == 1)
			url.append("&nickname=sdk_user_android");
		else if(SystemCache.userInfo.loginType == 2)
			url.append("&nickname="+((TextUtils.isEmpty(SystemCache.userInfo.username))?SystemCache.userInfo.oasnickname:SystemCache.userInfo.username));
		else if(SystemCache.userInfo.loginType == 3){
			if(TextUtils.isEmpty(SystemCache.userInfo.oasnickname))
				url.append("&nickname="+SystemCache.userInfo.platform);
			else
				url.append("&nickname="+SystemCache.userInfo.oasnickname);
		}
		
		url.append("&QuestionTypeid="+questionTypeId);
		url.append("&sid="+SystemCache.userInfo.serverID);
		url.append("&role_id="+SystemCache.userInfo.roleID);
		url.append("&role_name="+SystemCache.userInfo.gameNickname);
		url.append("&oas_token="+SystemCache.userInfo.token);
		url.append("&sign="+MD5Encrypt.StringToMD5(gamecode+SystemCache.userInfo.uid+SystemCache.userInfo.token+SystemCache.PUBLICKEY));
		
//		http://apisdk.mobile.oasgames.com/3.1/?a=custom&m=CreateNewquestion&uid=200000052423938&game_code=mloen&nickname=xxx&QuestionTypeid=2
		new OasisSdkHttpClient(getNewestUrl(url.toString()), null, new OasisSdkHttpClient.Callback() {
			
			@Override
			public void handleResultData(String result) {
				
				try {
					JSONObject json = new JSONObject(result);
					if("ok".equalsIgnoreCase(json.getString("status"))){
						callback.success(json.getString("qid"), "0", "success");
					}else{
						callback.fail("-1", json.has("msg")?json.getString("msg"):"Not error message!");
					}
				} catch (Exception e) {
					callback.fail("-1", e.getMessage());
				}
			}
			
			@Override
			public void handleErorrData(VolleyError error) {
				callback.excetpion(new OasisSdkException(error.getMessage()));
			}
		}).submitGet();
	}
	/**
	 * 根据问题id，查询交流记录
	 * @return
	 */
	public void getQuestionDetail(String questionId, String baseID, int page_type, int pageSize, final CallbackResultForActivity callback) {
		StringBuffer url = new StringBuffer("a=Custom&m=GetQuestionInfo");
		String uid = SystemCache.userInfo.uid;//"200000053568227";//
//		url.append("&game_code="+SystemCache.GAMECODE);
		url.append("&qid="+questionId);
//		url.append("&uid="+uid);
		url.append("&bench_qid="+baseID);
		url.append("&page_type="+page_type);
		url.append("&every_page_count="+pageSize);
		url.append("&oas_token="+SystemCache.userInfo.token);
		url.append("&sign="+MD5Encrypt.StringToMD5(questionId+uid+SystemCache.userInfo.token+SystemCache.PUBLICKEY));
		
		new OasisSdkHttpClient(getNewestUrl(url.toString()), null, new OasisSdkHttpClient.Callback() {
			
			@Override
			public void handleResultData(String result) {
				
				List<Object> list = null;
				
				try {
					JSONObject json = new JSONObject(result);
					if("ok".equalsIgnoreCase(json.getString("status"))){
						if(json.has("question_info"))
							list = JsonParser.newInstance().parserJSON2ObjList(json.getString("question_info"), new QuestionInfoLog());
						else
							list = new ArrayList<Object>();
						callback.success(list, "0", "success");
					}else{
						callback.fail("-1", json.has("msg")?json.getString("msg"):"Not error message!");
					}
				} catch (Exception e) {
					callback.excetpion(new OasisSdkException(e.getMessage()));
				}
			}
			
			@Override
			public void handleErorrData(VolleyError error) {
				callback.excetpion(new OasisSdkException(error.getMessage()));
			}
		}).submitGet();
	}
	/**
	 * 回复文字
	 * @param qid
	 * @param content
	 * @return
	 * @throws OasisSdkException
	 * @throws JSONException 
	 * @throws UnsupportedEncodingException 
	 */
	public void publishQuestionByWord(QuestionInfoLog info) throws OasisSdkException, JSONException, UnsupportedEncodingException{
		StringBuffer url = new StringBuffer("a=Custom&m=UserAddquestion");
		url.append("&qid="+info.qid);
		url.append("&uid="+info.uid);
		url.append("&game_code="+SystemCache.GAMECODE);
		url.append("&content_type=1");
		url.append("&content="+URLEncoder.encode(info.content, "UTF-8"));
		url.append("&nickname="+info.nickname);
		url.append("&oas_token="+SystemCache.userInfo.token);
		url.append("&sign="+MD5Encrypt.StringToMD5(info.qid+info.uid+SystemCache.userInfo.token+SystemCache.PUBLICKEY));
		String result = HttpDao.instance().submit(getNewUrl(url.toString()));
		JSONObject json = new JSONObject(result);
		info.setBench_qid(json.getString("bench_qid"));
		info.setStatus(json.getString("status")); 
		info.setCreate_time(json.getString("create_time"));
		JsonParser.newInstance().parserJson2Obj(result, info);
		
	}
	/**
	 * 回复图片
	 * @param qid
	 * @param content
	 * @return
	 * @throws OasisSdkException
	 * @throws JSONException 
	 */
	public void publishQuestionByImg(final QuestionInfoLog info, Listener<String> listener, ErrorListener error){
		
		MultipartRequest multipartRequest = new MultipartRequest(
                getNewestUrl("a=Custom&m=UserAddimgquestion"), listener, error);
		multipartRequest.setRetryPolicy(new RetryPolicy() {
			
			@Override
			public void retry(VolleyError arg0) throws VolleyError {
				BaseUtils.logError("", arg0.getMessage());
			}
			
			@Override
			public int getCurrentTimeout() {
				return 0;
			}
			
			@Override
			public int getCurrentRetryCount() {
				return 1;
			}
		});

        // 添加header
        multipartRequest.addHeader("header-name", "value");

        // 通过MultipartEntity来设置参数
        MultipartEntity multi = multipartRequest.getMultiPartEntity();
        // 文本参数
        multi.addStringPart("game_code", SystemCache.GAMECODE);
        multi.addStringPart("qid", info.qid);
        multi.addStringPart("uid", info.uid);
        multi.addStringPart("imgtype", info.local_img_url.substring(info.local_img_url.lastIndexOf(".")+1));
        multi.addStringPart("nickname", info.nickname);
        multi.addStringPart("oas_token", SystemCache.userInfo.token);
        multi.addStringPart("sign", MD5Encrypt.StringToMD5(info.qid+info.uid+SystemCache.userInfo.token+SystemCache.PUBLICKEY));
        

        Bitmap bitmap = BitmapFactory.decodeFile(info.local_img_url);
        multi.addStringPart("imgsize", "0");
        // 直接从上传Bitmap
//        multi.addBinaryPart("imgcontent", Bitmap2Bytes(bitmap));
        multi.addStringPart("imgcontent", BaseUtils.Bitmap2Base64String(bitmap));
        // 上传文件
//        multi.addFilePart("imgcontent", new File(info.local_img_url));
        // 将请求添加到队列中
        ApplicationContextManager.getInstance().getVolleyRequestQueue().add(multipartRequest);
        
        bitmap.recycle();
        bitmap = null;
	}
	/**
	 * 向某问题，回复文字或图片
	 * @param qid
	 * @param content
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws OasisSdkException
	 * @throws JSONException 
	 */
	public void publishQuestionByReplay(final QuestionInfoLog info, Listener<String> listener, ErrorListener error) {
		String url = "";
		if(info.content_type.equals("1"))
			url = getNewestUrl("a=Custom&m=UserAddquestion");
		else if(info.content_type.equals("2"))
			url = getNewestUrl("a=Custom&m=UserAddimgquestion");
		
		MultipartRequest multipartRequest = new MultipartRequest(
				url, listener, error);
		multipartRequest.setRetryPolicy(new DefaultRetryPolicy(60 * 1000, 0, 1.0f));// 超时设置为60秒，以免上传图片超时，导致重发
		
		// 添加header
		multipartRequest.addHeader("header-name", "value");
		
		// 通过MultipartEntity来设置参数
		MultipartEntity multi = multipartRequest.getMultiPartEntity();
		// 文本参数
		multi.addStringPart("game_code", SystemCache.GAMECODE);
		multi.addStringPart("qid", info.qid);
		multi.addStringPart("uid", info.uid);
		multi.addStringPart("nickname", info.nickname);
		multi.addStringPart("oas_token", SystemCache.userInfo.token);
		multi.addStringPart("sign", MD5Encrypt.StringToMD5(info.qid+info.uid+SystemCache.userInfo.token+SystemCache.PUBLICKEY));
		
		if(info.content_type.equals("1")){
			multi.addStringPart("content_type", "1");
			try {
				multi.addStringPart("content", new String(info.content.getBytes(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				error.onErrorResponse(new VolleyError(e.getMessage()));
			}
		}else if(info.content_type.equals("2")){
			multi.addStringPart("imgtype", info.local_img_url.substring(info.local_img_url.lastIndexOf(".")+1));
//			Bitmap bitmap = BitmapFactory.decodeFile(info.local_img_url);
			multi.addStringPart("imgsize", "0");
			// 直接从上传Bitmap
//        multi.addBinaryPart("imgcontent", Bitmap2Bytes(bitmap));
//			multi.addStringPart("imgcontent", BaseUtils.Bitmap2Base64String(bitmap));
			if(null != info.content && !TextUtils.isEmpty(info.content))
				try {
					multi.addStringPart("content", new String(info.content.getBytes(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {}
			multi.addStringPart("imgcontent", BaseUtils.Bitmap2Base64String(info.local_img_url));
			// 上传文件
//        multi.addFilePart("imgcontent", new File(info.local_img_url));
		}
		multipartRequest.setShouldCache(false);
		// 将请求添加到队列中
		ApplicationContextManager.getInstance().getVolleyRequestQueue().add(multipartRequest);
		
	}
	public void getEpinImages(final CallbackResultForActivity callback){
		StringBuffer url = new StringBuffer("a=Pay&m=convertRequest");
		url.append("&actname=epinPaymentConfig");
		url.append("&reqmethod=get");
		url.append("&localsign="+MD5Encrypt.StringToMD5(PhoneInfo.instance().softwareType+"epinPaymentConfig"+"get"+SystemCache.PUBLICKEY));
		
		new BasesDao().post(getNewestUrl(url.toString()), null, new Listener<String>() {
			@Override
			public void onResponse(String arg0) {
				String datas = "";
				try {
					JSONObject o = new JSONObject(arg0);
					if(!"ok".equalsIgnoreCase(o.getString("status"))){
						callback.fail("", "");
						return;
					}
					datas = o.getJSONArray("data").toString();
				} catch (Exception e) {
					callback.fail("", "");
					return;
				}
				callback.success(datas, "", "");
				
			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				callback.equals(new OasisSdkException(""));
			}
		});
	}
	public void postEpinCode(String code, final CallbackResultForActivity callback){
		StringBuffer url = new StringBuffer("a=Pay&m=convertRequest");
		url.append("&actname=epinPaymentApi");
		url.append("&reqmethod=post");
		url.append("&localsign="+MD5Encrypt.StringToMD5(PhoneInfo.instance().softwareType+"epinPaymentApi"+"post"+SystemCache.PUBLICKEY));
		
		Map<String, String> paras = new HashMap<String,String>();
		paras.put("userid", SystemCache.userInfo.uid);
		paras.put("game_code", SystemCache.GAMECODE);
		paras.put("server_id", SystemCache.userInfo.serverID);
		paras.put("charge_code", code);
		paras.put("sign", MD5Encrypt.StringToMD5(SystemCache.userInfo.uid+SystemCache.GAMECODE+SystemCache.userInfo.serverID+code+"I24RM2ht6WQuu4jyyNN5eAxAFQDdvK97ge"));
		new BasesDao().post(getNewestUrl(url.toString()), paras, new Listener<String>() {
			@Override
			public void onResponse(String arg0) {
				String game_coins = "";
				try {
					JSONObject o = new JSONObject(arg0);
					if(!"ok".equalsIgnoreCase(o.getString("status"))){
						callback.fail("", "");
						return;
					}
					game_coins = o.getString("game_coins");
				} catch (Exception e) {
					callback.fail("", "");
					return;
				}
				callback.success(game_coins, "", "");
				
			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				callback.excetpion(new OasisSdkException(""));
			}
		});
	}
	public String updatePersonalInfo(){
		return getNewestUrl("a=Login&m=PerfectUserinfo&oas_token="+SystemCache.userInfo.token+"&sign="+MD5Encrypt.StringToMD5(SystemCache.GAMECODE+SystemCache.userInfo.token+SystemCache.PUBLICKEY));
	}
	public String getBackPWD(String uname){
		if(uname == null || TextUtils.isEmpty(uname))
			uname = "";
		else{
			try {
				uname = URLEncoder.encode(uname, "UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
		}
		return getNewestUrl("a=Login&m=GetbackPwd&oas_token="+SystemCache.userInfo.token+"&username="+uname+"&sign="+MD5Encrypt.StringToMD5(SystemCache.GAMECODE+SystemCache.userInfo.token+SystemCache.PUBLICKEY));
	}
	/**
	 * 账号或设备被封禁时，收集用户的反馈信息
	 * @param email		回访时的邮箱（玩家邮箱）
	 * @param descrip	反馈信息的描述
	 * @param callback	回调
	 */
	public void feedback(String email, String descrip, final CallbackResultForActivity callback){
		StringBuffer url = new StringBuffer("a=Custom&m=ForbiddenUserAsk");
		int ask_type = 0; // 0:uid封禁 1:设备封禁
		if(SystemCache.bindInfo != null){
			if("-13".equals(SystemCache.bindInfo.error))
				ask_type = 1;
			else if("-14".equals(SystemCache.bindInfo.error))
				ask_type = 0;
		}
		url.append("&ask_type="+ask_type);
		if(SystemCache.bindInfo != null && !TextUtils.isEmpty(SystemCache.bindInfo.uid))
			url.append("&uid="+SystemCache.bindInfo.uid);
		url.append("&contact_email="+email);
		try {
			url.append("&desc="+URLEncoder.encode(descrip, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			url.append("&desc="+descrip);
		}
		url.append("&sign="+MD5Encrypt.StringToMD5(PhoneInfo.instance().locale+ask_type+SystemCache.GAMECODE+SystemCache.PUBLICKEY));
		new OasisSdkHttpClient(getNewestUrl(url.toString(), false), null, new OasisSdkHttpClient.Callback(){

			@Override
			public void handleResultData(String result) {
				BaseUtils.logDebug(TAG, "feedback result:"+result);
				String askid = "";
				try {
					JSONObject o = new JSONObject(result);
					if(!"ok".equalsIgnoreCase(o.getString("status"))){
						callback.fail("", "");
						return;
					}
					askid = o.getString("askid");
				} catch (Exception e) {
					callback.fail("", "");
					return;
				}
				callback.success(askid, "", "");
			}

			@Override
			public void handleErorrData(VolleyError error) {
				callback.excetpion(new OasisSdkException(""));
			}
			
		}).submitPost();
	}
	/**
	 * 为消息推送，上报DeviceToken
	 * @param deviceToken
	 * @param callback
	 */
	public void setDeviceTokenForPushMessages(String deviceToken, final CallbackResultForActivity callback){
		StringBuffer url = new StringBuffer("a=Push&m=userSubscribe");
		url.append("&devicetoken="+deviceToken);
		url.append("&sign="+MD5Encrypt.StringToMD5(BaseUtils.getMobileCode()+deviceToken+SystemCache.PUBLICKEY));
		new OasisSdkHttpClient(getNewestUrl(url.toString(), false), null, new OasisSdkHttpClient.Callback(){

			@Override
			public void handleResultData(String result) {
				BaseUtils.logDebug(TAG, "setDeviceTokenForPushMessages result:"+result);
				String endpointARN = "";
				try {
					JSONObject o = new JSONObject(result);
					if(!"ok".equalsIgnoreCase(o.getString("status"))){
						callback.fail("", "");
						return;
					}
					endpointARN = o.getString("val");
				} catch (Exception e) {
					callback.fail("", "");
					return;
				}
				callback.success(endpointARN, "", "");
			}

			@Override
			public void handleErorrData(VolleyError error) {
				callback.excetpion(new OasisSdkException(""));
			}
			
		}).submitPost();
	}
	private RequestEntity getNewUrl(String url){
		RequestEntity requestEntity = new RequestEntity(getNewestUrl(url));
		
		return requestEntity;
	}
	private String getNewestUrl(String url){
//		String fullUrl = (BaseUtils.isSandBox()?Constant.BASEURL_SANDBOX:BaseUtils.isTestMode()?Constant.BASEURL_TEST:Constant.BASEURL) + url + PhoneInfo.instance().toString();
//		BaseUtils.logDebug(TAG, fullUrl);
//		return fullUrl;
		return getNewestUrl(url, true);
	}
	private String getNewestUrl(String url, boolean isNeedUserInfo){
		String fullUrl = (BaseUtils.isSandBox()?Constant.BASEURL_SANDBOX:BaseUtils.isTestMode()?Constant.BASEURL_TEST:Constant.BASEURL) + url + PhoneInfo.instance().toString(isNeedUserInfo);
		BaseUtils.logDebug(TAG, fullUrl);
		return fullUrl;
	}
		
}
