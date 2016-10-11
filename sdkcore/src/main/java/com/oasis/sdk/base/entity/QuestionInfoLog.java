package com.oasis.sdk.base.entity;


/**
 * 用户咨询问题 时 与客服的交流日志
 * @author Administrator
 *
 */
public class QuestionInfoLog {
	public static String status_ok = "ok";// 成功
	public static String status_fail = "fail";// 失败
	public static String status_sending = "sending";// 发送中
	
	public String temp_benchid;// 临时id
	public String bench_qid;// 回复id
	public String qid;// 问题id
	public String content;// 文字内容
	public String uid;// 用户id
	public String nickname;// 用户名
	public String customid;// 客服id
	public String custom_nickname;// 客服昵称
	public String create_time;// 时间戳
	public String content_type;//日志类型    文字信息、图片信息 
	public String usertype;// 用户类型  区分玩家与客服
	public String img_url;// 图片链接
	public String local_img_url;// 本地图片链接
	
	public String status = status_ok;// 状态
	
	public void setTemp_benchid(String temp_benchid) {
		this.temp_benchid = temp_benchid;
	}
	public void setBench_qid(String bench_qid) {
		this.bench_qid = bench_qid;
	}
	public void setQid(String qid) {
		this.qid = qid;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public void setNickname(String uickname) {
		this.nickname = uickname;
	}
	public void setCustomid(String customid) {
		this.customid = customid;
	}
	public void setCustom_nickname(String custom_nickname) {
		this.custom_nickname = custom_nickname;
	}
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
	public void setContent_type(String content_type) {
		this.content_type = content_type;
	}
	public void setUsertype(String usertype) {
		this.usertype = usertype;
	}
	public void setImg_url(String img_url) {
		this.img_url = img_url;
	}
	
	public void setLocal_img_url(String local_img_url) {
		this.local_img_url = local_img_url;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	

	
}
