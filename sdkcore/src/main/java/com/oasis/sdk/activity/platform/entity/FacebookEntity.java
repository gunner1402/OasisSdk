package com.oasis.sdk.activity.platform.entity;

import org.json.JSONException;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.oasis.sdk.base.Exception.OasisSdkException;


public class FacebookEntity {
	
	public String appID;
	public void setAppID(String appID) {
		this.appID = appID;
	}

	public static FacebookEntity getInfoByGameCode(Activity c) throws JSONException, OasisSdkException{
		FacebookEntity re = null;
		ApplicationInfo info = null;
		try {
			info = ((Activity) c).getPackageManager().getApplicationInfo(((Activity) c).getPackageName(), PackageManager.GET_META_DATA);
			Bundle b = info.metaData;
			if(b != null && b.containsKey("com.facebook.sdk.ApplicationId")){
				String appid = b.getString("com.facebook.sdk.ApplicationId");
				
				if(TextUtils.isEmpty(appid)){
					Log.e("FacebookEntity", "Please setup com.facebook.sdk.ApplicationId in AndroidMainfest.xml，For example：<meta-data android:name=\"com.facebook.sdk.ApplicationId\" android:value=\"请替换为facebook APP ID\"/>;Facebook Appid 请配置到string.xml.");
					return null;
				}
				
				re = new FacebookEntity();
				re.setAppID(appid);
				
				return re;
			}
			return null;
		} catch (Exception e) {
			Log.e("FacebookEntity", "Get facebook info is fail, please check AndroidMainfest.xml.For example：<meta-data android:name=\"com.facebook.sdk.ApplicationId\" android:value=\"请替换为facebook APP ID\"/>");
			return null;
		}
		
	}
	
	
	
}
