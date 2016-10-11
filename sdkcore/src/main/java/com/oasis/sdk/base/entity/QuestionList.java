package com.oasis.sdk.base.entity;

import java.util.List;

/**
 * 问题列表 ，分为 “解决中”，“已关闭”
 * @author Administrator
 *
 */
public class QuestionList {

	public String QuestionStatus;//问题类型
	public String CurPage;// 第几页
	public String pageSize;// 每页记录数
	public String TotalPage;// 总页数
	
	public List<QuestionInfo> question_list;
	
	
	public void setQuestionStatus(String questionStatus) {
		QuestionStatus = questionStatus;
	}

	public void setCurPage(String curPage) {
		CurPage = curPage;
	}

	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}

	public void setTotalPage(String totalPage) {
		TotalPage = totalPage;
	}

	public void setQuestion_list(List<QuestionInfo> question_list) {
		this.question_list = question_list;
	}
}
