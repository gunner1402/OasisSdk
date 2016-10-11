package com.oasis.sdk.base.entity;



/**
 * 用户咨询 问题信息
 * @author Administrator
 *
 */
public class QuestionInfo {
	public String qid;// 主键
	public String content;// 标题
	public String reply_unread_count;// 未查看消息数
	public String uid;// 用户id
	public String nickname;// 用户名
	public String question_type_name;// 问题类型名称
	public String create_time;// 时间戳
	public String getQid() {
		return qid;
	}
	public void setQid(String qid) {
		this.qid = qid;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public void setReply_unread_count(String reply_unread_count) {
		this.reply_unread_count = reply_unread_count;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public void setQuestion_type_name(String question_type_name) {
		this.question_type_name = question_type_name;
	}
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
	
	
	
}
