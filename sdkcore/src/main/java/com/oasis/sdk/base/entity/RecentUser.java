package com.oasis.sdk.base.entity;

import java.util.List;


public class RecentUser{
	
	public String uid;				// 用户id
	public String third_uid;		// 第三方账号验证时使用的uid，可能与uid一致
	public int loginType;			// 用户登录类型
	public String platform;			// 平台类型
	public String username;			// 用户名
	public String oasnickname;		// 昵称
	public String time;				// 最近登录时间
	
	public List<RecentUserGameInfo> list;	// 该账号下 创建的游戏信息
}
