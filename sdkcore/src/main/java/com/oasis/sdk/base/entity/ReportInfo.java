package com.oasis.sdk.base.entity;

import java.util.Map;

/**
 * 数据上报信息
 * @author Administrator
 *
 */
public class ReportInfo {
	
	public int type;					//上报类型. 1:Adjust  2:Mdata
	public String eventName;			//上报事件名称
	public Map<String, String> params;	//上报Event参数
	public long createTime;				//创建时间
	
	
	
}
