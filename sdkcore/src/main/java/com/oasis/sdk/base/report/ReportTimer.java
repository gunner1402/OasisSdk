package com.oasis.sdk.base.report;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.FileUtils;
import com.oasis.sdk.base.utils.SystemCache;

public class ReportTimer extends TimerTask {

	final String TAG = ReportTimer.class.getSimpleName();
	@Override
	public void run() {
		
		ReportUtils.lastReport();
		
		/** 2016-04-19 注释以下代码，防止给服务端造成访问压力 **/
//		if(SystemCache.controlInfo != null && SystemCache.controlInfo.getCustom_onoff_control())// 客服开关为打开时，获取客服回复的状态
//			HttpService.instance().getNewsInfo(null);
		
		if(SystemCache.logListsSD != null){
			synchronized (SystemCache.logListsSD) {// 同步
				if(SystemCache.logListsSD != null && SystemCache.logListsSD.size() > 0){				
					List<String> log = new ArrayList<String>(SystemCache.logListsSD);
					SystemCache.logListsSD.clear();
					for (String str : log) {
						FileUtils.writeLogToStore(str);					
					}
				}
			}
		}
		try {// 利用定时机会，判断userinfo是否被回收，如果被回收，从新解析
			if(!BaseUtils.isLogin())
				BaseUtils.parseJsonToUserinfo();			
		} catch (Exception e) {
		}
	}

	
}
