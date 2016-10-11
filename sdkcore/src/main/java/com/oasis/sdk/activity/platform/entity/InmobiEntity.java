package com.oasis.sdk.activity.platform.entity;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.oasis.sdk.base.utils.BaseUtils;


public class InmobiEntity {
	
	public String appID;
	public void setAppID(String appID) {
		this.appID = appID;
	}

	public static InmobiEntity getInfoByGameCode(Activity c){
		InmobiEntity re = null;
		
		try {
			String appid = c.getResources().getString(
					BaseUtils.getResourceValue("string", "inmobi_appid"));
			if(TextUtils.isEmpty(appid)){
					Log.e("InmobiEntity", "Please setup inmobi_appid in res/values/trackinfo.xml");
					return null;
				}
				
			re = new InmobiEntity();
			re.setAppID(appid);
			
			return re;
		} catch (Exception e) {
			Log.e("InmobiEntity", "Get inmobi info is fail, please check res/values/trackinfo.xml.For example：<string name=\"inmobi_appid\">3392856151</string>");
			return null;
		}
	}
	
	
	
}
