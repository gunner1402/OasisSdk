package com.oasis.sdk;

import com.oasis.sdk.base.entity.FBPageInfo;
import com.oasis.sdk.base.entity.UserInfo;

/**
 * 游戏客户端，需要实现该接口，完成游戏端的退出操作
 * @author Administrator
 *
 */
public interface OASISPlatformInterface {
	
	/**
	 * 1、登录成功后，加载游戏
	 * 2、切换账号成功后，重新加载游戏
	 * @param userInfo
	 */
	public void reloadGame(UserInfo userInfo);
	
	/**
	 * 支付结果回调
	 * @param paymentWay	支付方式
	 * @param paymentCode	支付结果码
	 * @param errorMessage	错误信息
	 */
	public void paymentCallback(String paymentWay, int paymentCode, String errorMessage);
	
	/**
	 * Facebook 操作结果回调 （含 分享、邀请、赠送、索要等）
	 * @param action		请求动作
	 * @param resultCode	请求结果码
	 * @param id			请求成功后产生的id	
	 */
	public void fbRequestCallback(int action, int resultCode, String id);
	/**
	 * Facebook 好友列表 （含 可邀请好友、玩该游戏的好友）
	 * @param type			好友类型
	 * @param resultCode	结果码
	 * @param FBPageInfo	Facebook好友列表集合
	 */
	public void fbFriendsListCallback(int type, int resultCode, FBPageInfo info);
	
	/**
	 * 获取支付时所需的扩展参数 ext，此参数将在发钻时有OAS Server传递给Game Server
	 * @return ext
	 */
	public void getExtendValue(OasisCallback callback);
	
}
