package com.oasis.sdk.base.entity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.text.TextUtils;

import com.oasis.sdk.OASISPlatformConstant;
import com.oasis.sdk.base.report.ReportUtils;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.Constant;
import com.oasis.sdk.base.utils.MD5Encrypt;
import com.oasis.sdk.base.utils.SystemCache;

/**
 * 数据上报信息
 * @author Administrator
 *
 */
public class ReportMdataInfo extends ReportInfo{

	public List<String> params;	//上报APP自定义参数
	public List<String> status;	//上报APP自定义参数
	
	public Map<String, String> paramsMap;	//上报APP自定义参数
	public Map<String, String> statusMap;	//上报APP自定义参数
	
	public String content;// 上报内容
	
	public ReportMdataInfo(String eventName, List<String> params, List<String> status) {
		super.type = OASISPlatformConstant.REPORT_TYPE_MDATA;
		super.eventName = eventName;
		super.createTime = System.currentTimeMillis();
		this.params = params;
		this.status = status;
		
		this.content = getMdataJsonInfo();
	}
	public ReportMdataInfo(String eventName, Map<String, String> params, Map<String, String> status) {
		super.type = OASISPlatformConstant.REPORT_TYPE_MDATA;
		super.eventName = eventName;
		super.createTime = System.currentTimeMillis();
		this.paramsMap = params;
		this.statusMap = status;

		this.content = getMdataJsonInfo();
	}
	
	private String getMdataJsonInfo(){
		
//		o.put("channel", SystemCache.setting.getString("referrer", "OasisGameSDK"));
//		o.put("subchannel", SystemCache.setting.getString("referrer", "OasisGameSDK"));
//		o.put("referrer", SystemCache.setting.getString("referrer", "OasisGameSDK"));
//		o.put("directed", 0);
//		o.put("promotion_string", "");
		StringBuffer o = new StringBuffer("{");
		
		o.append("\"appid\":\""+PhoneInfo.instance().mdataAppID+"\"");
		if(ReportUtils.DEFAULTEVENT_INIT.equals(eventName))
			o.append(",\"uuid\":\""+MD5Encrypt.StringToMD5(BaseUtils.getMobileCode())+"\"");
		else if(ReportUtils.DEFAULTEVENT_SETUSERINFOROLEID.equals(eventName))
			o.append(",\"uuid\":\""+SystemCache.userInfo.roleID+"\"");
		else
			o.append(",\"uuid\":\""+(SystemCache.userInfo!=null&&!TextUtils.isEmpty(SystemCache.userInfo.uid)?SystemCache.userInfo.uid:MD5Encrypt.StringToMD5(BaseUtils.getMobileCode()))+"\"");
		o.append(",\"udid\":\""+BaseUtils.getMobileCode()+"\"");
		o.append(",\"event\":\""+eventName+"\"");
		o.append(",\"server_id\":\""+(SystemCache.userInfo!=null&&!TextUtils.isEmpty(SystemCache.userInfo.serverID)?SystemCache.userInfo.serverID:"")+"\"");
		o.append(",\"__time_shift\":\""+(createTime - System.currentTimeMillis())/1000+"\"");// 转换为秒
		
		
		o.append(",\"locale\":\""+PhoneInfo.instance().locale+"\"");
		o.append(",\"version\":\""+PhoneInfo.instance().softwareVersion+"\"");
		o.append(",\"country\":\""+PhoneInfo.instance().getIpToCountry()+"\"");
		o.append(",\"os\":\""+"android"+"\"");
		o.append(",\"browser\":\"\"");
		o.append(",\"screen\":\""+PhoneInfo.instance().screen+"\"");
		
		StringBuffer paramStr = new StringBuffer("{");
		paramStr.append("\"sdk_version\":" + "\""+ Constant.SDKVERSION + "\",");
		paramStr.append("\"game_version\":" + "\""+ PhoneInfo.instance().bundleversion + "\"");
		if(params != null){
			for (String str : params) {
				paramStr.append(",");
				paramStr.append(str);
			}
		}else{
			if(paramsMap != null){
				for (Entry<String, String> iter : paramsMap.entrySet()) {
					paramStr.append(",");
					paramStr.append("\""+iter.getKey()+"\":" + "\""+ iter.getValue() + "\"");
				}
			}
		}
		paramStr.append("}");
		o.append(",\"params\":"+paramStr.toString());
		

		StringBuffer statusStr = new StringBuffer("{");
		statusStr.append("\"sdk_version\":" + "\""+ Constant.SDKVERSION + "\",");
		statusStr.append("\"game_version\":" + "\""+ PhoneInfo.instance().bundleversion + "\"");
		if(status != null){
			for (String str : status) {
				statusStr.append(",");
				statusStr.append(str);
			}
		}else{
			if(statusMap != null){
				for (Entry<String, String> iter : statusMap.entrySet()) {
					statusStr.append(",");
					statusStr.append("\""+iter.getKey()+"\":" + "\""+ iter.getValue() + "\"");
				}
			}
		}
		// 2016-05-04 status增加 phonebrand
		String phonebrand = PhoneInfo.instance().brand+",";
		try {
			phonebrand += URLEncoder.encode(PhoneInfo.instance().model, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			phonebrand += PhoneInfo.instance().model;
		}
		statusStr.append(",\"phonebrand\":"+ "\""+ phonebrand + "\"");
		
		statusStr.append("}");
		o.append(",\"status\":"+statusStr.toString());
		
		StringBuilder userinfo = new StringBuilder("{");
		userinfo.append("\"reg_lang\":" + "\""+ PhoneInfo.instance().locale + "\"");
		userinfo.append("}");
		o.append(",\"user_info\":"+userinfo.toString());
		
		o.append("}");
		
		BaseUtils.logDebug("Mdata", o.toString());
		return o.toString();
	
	
}
}
