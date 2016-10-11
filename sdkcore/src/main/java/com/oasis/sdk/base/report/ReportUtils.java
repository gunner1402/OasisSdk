package com.oasis.sdk.base.report;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Timer;

import android.text.TextUtils;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustEvent;
import com.oasis.sdk.OASISPlatformConstant;
import com.oasis.sdk.base.Exception.OasisSdkException;
import com.oasis.sdk.base.entity.PhoneInfo;
import com.oasis.sdk.base.entity.ReportAdjustInfo;
import com.oasis.sdk.base.entity.ReportInfo;
import com.oasis.sdk.base.entity.ReportMdataInfo;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.SystemCache;

public class ReportUtils {
	private static final String TAG = ReportUtils.class.getSimpleName();
	
	/**
	 * 初始化事件
	 * 主Activity的onCreate中调用
	 */
	public static final String DEFAULTEVENT_INIT = "sdk_init";
	/**
	 * 从后台恢复事件
	 * 主Activity的onRestart中调用
	 */
	public static final String DEFAULTEVENT_RESTORE = "sdk_restore";
	/**
	 * 进入后台
	 * 主Activity的onStop中调用
	 */
	public static final String DEFAULTEVENT_ENTERBACK = "sdk_enterback";
	/**
	 * 支付下单事件
	 * 调用情景：google发钻请求成功后；第三方支付下单成功后；
	 * 包含参数： uid:uid
			 roleid：角色id
			 serverid：服id
			 servertype：服类型（android/ios/all/none）
			 product_id:渠道商品id
			 payment_channal:支付渠道
			 cost:价格
			 currency:货币单位
			 value:钻石数
			 oas_order_id:OAS订单号
			 third_party_orderid:第三方订单号
			result_code:支付平台返回结果码
	 */
	public static final String DEFAULTEVENT_ORDER = "sdk_order";
	/**
	 * 支付上报从google获取的订单
	 * 调用情景：通过productid在google上查询到已支付成功的订单，向支付平台请求发钻的请求无论是否发送成功均上报
	 * 包含参数： uid:uid
			 roleid：角色id
			 serverid：服id
			 servertype：服类型（android/ios/all/none）
			 product_id:渠道商品id
			 payment_channal:支付渠道
			 cost:价格
			 currency:货币单位
			 value:钻石数
			 oas_order_id:OAS订单号
			 third_party_orderid:第三方订单号
			result_code:支付平台返回结果码
	 */
	public static final String DEFAULTEVENT_ORDER_REPORT_OLD_GOOGLE = "sdk_pay_report_old_google";
	/**
	 * 支付，上报从本地数据库获取的订单
	 * 调用情景：通过productid在本地数据库上查询到已支付成功的订单，向支付平台请求发钻的请求无论是否发送成功均上报
	 * 包含参数： uid:uid
			 roleid：角色id
			 serverid：服id
			 servertype：服类型（android/ios/all/none）
			 product_id:渠道商品id
			 payment_channal:支付渠道
			 cost:价格
			 currency:货币单位
			 value:钻石数
			 oas_order_id:OAS订单号
			 third_party_orderid:第三方订单号
			result_code:支付平台返回结果码
	 */
	public static final String DEFAULTEVENT_ORDER_REPORT_OLD_LOCAL = "sdk_pay_report_old_local";
	/**
	 * 支付，正常支付成功的订单
	 * 调用情景：支付成功后，向支付平台发送发钻的请求,无论是否发送成功均上报
	 * 包含参数： uid:uid
			 roleid：角色id
			 serverid：服id
			 servertype：服类型（android/ios/all/none）
			 product_id:渠道商品id
			 payment_channal:支付渠道
			 cost:价格
			 currency:货币单位
			 value:钻石数
			 oas_order_id:OAS订单号
			 third_party_orderid:第三方订单号
			result_code:支付平台返回结果码
	 */
	public static final String DEFAULTEVENT_ORDER_REPORTED = "sdk_pay_report";
	/**
	 * 支付成功，在google回调时且状态为 “付钱成功”
	 * 调用情景：google付钱成功后；
	 * 包含参数：  sdk_version:SDK版本
			 game_version:游戏版本
			 uid:uid
			 roleid：角色id
			 serverid：服id
			 servertype：服类型（android/ios/all/none）
			 payment_channal:支付渠道
			 cost:价格
			 currency:货币单位
			 value:钻石数
			 product_id:渠道商品id；
			 oas_order_id:OAS订单号
			 third_party_orderid:第三方订单号
			result_code:支付平台返回结果码
	 */
	public static final String DEFAULTEVENT_PAID_MONEY = "sdk_paid_money";
	/**
	 * 支付成功事件
	 * 调用情景：google发钻成功后；第三方支付成功后（Infobip\Mopay）；
	 * 包含参数：  sdk_version:SDK版本
			 game_version:游戏版本
			 uid:uid
			 roleid：角色id
			 serverid：服id
			 servertype：服类型（android/ios/all/none）
			 payment_channal:支付渠道
			 cost:价格
			 currency:货币单位
			 value:钻石数
			 product_id:渠道商品id；
			 oas_order_id:OAS订单号
			 third_party_orderid:第三方订单号
			result_code:支付平台返回结果码
	 */
	public static final String DEFAULTEVENT_PAID = "sdk_paid";
	/**
	 * OAS账号注册成功事件
	 * 参数：
		 username:用户名
 			uid:用户ID
	 */
	public static final String DEFAULTEVENT_REGISTER = "sdk_register";
	/**
	 * 登录成功事件（含正常登录、自动登录、切换账号）
	 * 参数：login_type:登录类型
			 username:用户名
			 platform:
			 uid：
	 */
	public static final String DEFAULTEVENT_LOGIN = "sdk_login";
	/**
	 * 游戏角色登录成功
	 * 在游戏方调用setUserinfo时
	 * 参数：uid:uid
			 roleid：角色id
			 username：角色名称
			 serverid：服id
			 servertype：服类型（android/ios/all/none）
			 servername:服名称
	 */
	public static final String DEFAULTEVENT_SETUSERINFO = "sdk_setuserinfo";
	/**
	 * 游戏角色登录成功
	 * 在游戏方调用setUserinfo时
	 * 参数：uid:角色id//2015-12-24 将角色id作为uid进行统计
			 roleid：角色id
			 username：角色名称
			 serverid：服id
			 servertype：服类型（android/ios/all/none）
			 servername:服名称
	 */
	public static final String DEFAULTEVENT_SETUSERINFOROLEID = "sdk_setuserinfo_roleid";
	/**
	 * 关联成功事件（非覆盖关联）
	 * 参数：type:关联类型
			 username:用户名
			 platform:
	 */
	public static final String DEFAULTEVENT_BIND = "sdk_bind";
//	/**
//	 * 覆盖成功事件
//	 * 参数：type:关联类型
//			 username:用户名
//			 platform:
//			 uid_from:
//			 uid_to：
//	 */
//	public static final String DEFAULTEVENT_BINDCOVER = "sdk_cover";
	/**
	 * 分享成功事件
	 * 参数：share_channal:分享渠道
	 */
	public static final String DEFAULTEVENT_SHARE = "sdk_share";
	/**
	 * PC充值按钮点击事件
	 */
	public static final String DEFAULTEVENT_CLICK_PCPAY = "sdk_pcpay";
	/**
	 * 论坛登录成功事件
	 */
	public static final String DEFAULTEVENT_FORUM = "sdk_forum";
	/**
	 * OG中个人中心点击事件
	 */
	public static final String DEFAULTEVENT_CLICK_OG_UCENTER = "sdk_og_ucenter";
	/**
	 * OG中注册点击事件
	 */
	public static final String DEFAULTEVENT_CLICK_OG_REGIST = "sdk_og_regist";
	/**
	 * OG中切换点击事件
	 */
	public static final String DEFAULTEVENT_CLICK_OG_CHANGE = "sdk_og_change";
	/**
	 * OG中支付点击事件
	 */
	public static final String DEFAULTEVENT_CLICK_OG_PAY = "sdk_og_pay";
	/**
	 * OG中分享点击事件
	 */
	public static final String DEFAULTEVENT_CLICK_OG_SHARE = "sdk_og_share";
	/**
	 * OG中帮助点击事件
	 */
	public static final String DEFAULTEVENT_CLICK_OG_HELP = "sdk_og_help";
	/**
	 * OG中论坛点击事件
	 */
	public static final String DEFAULTEVENT_CLICK_OG_FORUM = "sdk_og_forum";
	
	/**
	 * 跟踪广告跟踪的启动事件
	 * 参数：game_code: 游戏代码
		  track_channel: 广告跟踪渠道
		  track_channel_appid：APPid
	 */
	public static final String DEFAULTEVENT_TRACK = "sdk_track";
	/**
	 * 授权失败页面退出游戏
	 */
	public static final String DEFAULTEVENT_ACCREDIT_EXIT_GAME = "sdk_exit_game";
	/**
	 * 授权失败页面重新授权
	 */
	public static final String DEFAULTEVENT_ACCREDIT_REAUTHOR = "sdk_reauthor";
	/**
	 * 授权失败页面其他方式登录
	 */
	public static final String DEFAULTEVENT_ACCREDIT_EXCHANGE_LOGIN = "sdk_exchange_login";
	/**
	 * 上报本地Google账号信息
	 */
	public static final String DEFAULTEVENT_GOOGLE_ACCOUNT = "sdk_google_account";
	/**
	 * 数据上报定时器
	 */
	public static Timer reportTimer = new Timer();
	
	public static Queue<ReportInfo> queue = new LinkedList<ReportInfo>(); 
	
	public static void add(String eventName, Map<String, String> params, Map<String, String> status){
		synchronized (queue) {
			
			boolean isSuc = queue.offer(new ReportMdataInfo(eventName, params, status));
			if(isSuc){
				BaseUtils.logDebug(TAG, eventName + " is created success for Mdata！");
			}else{
				BaseUtils.logDebug(TAG, eventName + " is created fail for Mdata！");
			}
		}
	}
	public static void add(String eventName, List<String> params, List<String> status){
		synchronized (queue) {
			
			boolean isSuc = queue.offer(new ReportMdataInfo(eventName, params, status));
			if(isSuc){
				BaseUtils.logDebug(TAG, eventName + " is created success for Mdata！");
			}else{
				BaseUtils.logDebug(TAG, eventName + " is created fail for Mdata！");
			}
		}
	}
	public static void add(String eventName, Double incent, String currency, Map<String, String> params){
		synchronized (queue) {
			
			boolean isSuc = queue.offer(new ReportAdjustInfo(eventName, incent, currency, params));
			if(isSuc){
				BaseUtils.logDebug(TAG, eventName + " is created success for Adjust！");
			}else{
				BaseUtils.logDebug(TAG, eventName + " is created fail for Adjust！");
			}
		}
	}
	public static void cancelReport(){
		lastReport();
		if(ReportUtils.reportTimer != null){// 取消上报定时器
			ReportUtils.reportTimer.cancel();
		}
	}
	public static void lastReport(){
		do{
			 
			synchronized (ReportUtils.queue) {// 同步
				ReportInfo info = ReportUtils.queue.peek();
				if(info != null){
					if(info.type == OASISPlatformConstant.REPORT_TYPE_MDATA)
						reportMdata((ReportMdataInfo)info);					
					else if(info.type == OASISPlatformConstant.REPORT_TYPE_ADJUST){
						reportAdjust((ReportAdjustInfo)info);
					}
					
					ReportUtils.queue.poll();
					
					BaseUtils.logDebug(TAG, "ReportInfo queue poll success;eventname "+info.eventName);

				}else{
					BaseUtils.logDebug(TAG, "ReportInfo queue is null;");
				}
			}
		}while(ReportUtils.queue.peek()!=null);
	}
	private static void reportMdata(final ReportMdataInfo info){
		if(BaseUtils.isSandBox())
			return;
		/**	Mdata */
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(!TextUtils.isEmpty(PhoneInfo.instance().mdataAppID)){
					BaseUtils.logDebug(TAG, "MData queue eventname "+info.eventName);
					try {
						HttpService.instance().sendToMdataInfo(info);
					} catch (OasisSdkException e) {
						BaseUtils.logDebug(TAG, "MData send fail. Event Name:"+info.eventName);
						return;
					}								
				}else{
					BaseUtils.logDebug(TAG, "MData appid is null.");
					return;
				}
			}
		}).start();
	}
	
	private static void reportAdjust(ReportAdjustInfo adjustInfo){
		if(!PhoneInfo.instance().isTrackAble())
			return;
		/**	Adjust */
		
		if(SystemCache.adjustEventMap.containsKey(adjustInfo.eventName)){
			
			String token = SystemCache.adjustEventMap.get(adjustInfo.eventName);
			if(TextUtils.isEmpty(token))
				return;
			
			AdjustEvent e = new AdjustEvent(token);
			if(adjustInfo.incent > 0 )
				e.setRevenue(adjustInfo.incent, adjustInfo.currency);
			
			if(adjustInfo.params !=null){
				for (Entry<String, String> elem : adjustInfo.params.entrySet()) {
					e.addCallbackParameter(elem.getKey(), elem.getValue());
				}
			}
			Adjust.trackEvent(e);
		}
	}
	
}
