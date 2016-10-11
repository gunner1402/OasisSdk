package com.oasis.sdk.base.entity;

import android.text.TextUtils;

/**
 * 用户基本信息
 * @author Administrator
 *
 */
public class UserInfo {
	
	public String status;
	/**
	 * 账号类型  
	 * 1：匿名账号；2：正式账号；999：单机账号
	 */
	public int type;
	/**
	 * 登录类型
	 *  1:匿名账号；2:OAS账号；3:Facebook账号；999：单机登录账号
	 */
	public int loginType;
	public String token;
	public String platform;
	public String platform_token;
	public String relation_type;
	public String uid_from;
	public String uid_to;
	public String uid;
	public String uidOld;		// 旧uid,用来比较当前操作是 新用户登录 还是 切换账号
	public String operation;	// 标识  1:新用户登录; 2:切换账号
	public String error;
	public String err_msg;
	public String tiplogin;		// 匿名账号 提示关联账号
	public String tip_perfect_userinfo;// 提示 完善用户资料
	
	public String serverID;		// 当前服ID
	public String serverName;	// 当前服名称
	public String serverType;	// 当前服类型
	public String roleID;		// 当前角色ID
	public String username;		// 当前登录用户名
	public String oasnickname;	// 昵称(登录账号对应的昵称或邮箱)
	public String gameNickname; // 游戏昵称\角色名
	
	public int chargeable;		// 0:正常，1:设备被封，2:uid被封
	public boolean isShowCustomerNewsFlag = false;
	
	public String friendsNext;
	public String invitableFriendsNext;
	public String friendsPrevious;
	public String invitableFriendsPrevious;
	
//	public String validateID;	// 将作登录验证的id
	
	public RecentUserList recentUserList;// 该设备上最近登录过的账号列表（仅限首页）
	public boolean isShowHistoryUserList = false;// 是否显示历史用户列表   (false:不显示，最近登录账号进行验证      true:显示该设备登录过的所有账号，由用户自主选择)
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getLoginType() {
		return loginType;
	}
	public void setLoginType(int loginType) {
		this.loginType = loginType;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getPlatform_token() {
		return platform_token;
	}
	public void setPlatform_token(String platform_token) {
		this.platform_token = platform_token;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getErr_msg() {
		return err_msg;
	}
	public void setErr_msg(String err_msg) {
		this.err_msg = err_msg;
	}
	public boolean getTiplogin() {
		if(!TextUtils.isEmpty(tiplogin) && "yes".equals(tiplogin))
			return true;
		return false;
	}
	public void setTiplogin(String tiplogin) {
		this.tiplogin = tiplogin;
	}
	public String getServerID() {
		return serverID;
	}
	public void setServerID(String serverID) {
		this.serverID = serverID;
	}
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public String getServerType() {
		return serverType;
	}
	public void setServerType(String serverType) {
		this.serverType = serverType;
	}
	public String getRoleID() {
		return roleID;
	}
	public void setRoleID(String roleID) {
		this.roleID = roleID;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String paltform) {
		this.platform = paltform;
	}
	public String getRelation_type() {
		return relation_type;
	}
	public void setRelation_type(String relation_type) {
		this.relation_type = relation_type;
	}
	public String getUid_from() {
		return uid_from;
	}
	public void setUid_from(String uid_from) {
		this.uid_from = uid_from;
	}
	public String getUid_to() {
		return uid_to;
	}
	public void setUid_to(String uid_to) {
		this.uid_to = uid_to;
	}
	public String getUidOld() {
		return uidOld;
	}
	public void setUidOld(String uidOld) {
		this.uidOld = uidOld;
	}
	public String getOperation() {
		if(TextUtils.isEmpty(uidOld))// 新用户登录
			return "1";
		else
			return "2";// 切换账号
	}
//	public void setOperation(String operation) {
//		this.operation = operation;
//	}
	public int getChargeable() {
		return chargeable;
	}
	public void setChargeable(int chargeable) {
		this.chargeable = chargeable;
	}
	public String getFriendsNext() {
		return friendsNext;
	}
	public void setFriendsNext(String friendsNext) {
		this.friendsNext = friendsNext;
	}
	public String getInvitableFriendsNext() {
		return invitableFriendsNext;
	}
	public void setInvitableFriendsNext(String invitableFriendsNext) {
		this.invitableFriendsNext = invitableFriendsNext;
	}
	public String getFriendsPrevious() {
		return friendsPrevious;
	}
	public void setFriendsPrevious(String friendsPrevious) {
		this.friendsPrevious = friendsPrevious;
	}
	public String getInvitableFriendsPrevious() {
		return invitableFriendsPrevious;
	}
	public void setInvitableFriendsPrevious(String invitableFriendsPrevious) {
		this.invitableFriendsPrevious = invitableFriendsPrevious;
	}
	public String getOasnickname() {
		return oasnickname;
	}
	public void setOasnickname(String oasnickname) {
		this.oasnickname = oasnickname;
	}
	public String getGameNickname() {
		return gameNickname;
	}
	public void setGameNickname(String gameNickname) {
		this.gameNickname = gameNickname;
	}
	public boolean getTip_perfect_userinfo() {
		if("Y".equals(tip_perfect_userinfo))
			return true;
		return false;
	}
	public void setTip_perfect_userinfo(String tip_perfect_userinfo) {
		this.tip_perfect_userinfo = tip_perfect_userinfo;
	}
	
}
