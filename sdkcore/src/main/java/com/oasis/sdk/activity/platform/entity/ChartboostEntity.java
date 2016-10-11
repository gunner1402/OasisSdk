package com.oasis.sdk.activity.platform.entity;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.oasis.sdk.base.utils.BaseUtils;

public class ChartboostEntity {

	public String appID;
	public String signature;

	public void setAppID(String appID) {
		this.appID = appID;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public static ChartboostEntity getInfoByGameCode(Activity c) {
		ChartboostEntity cbe = null;

		String appid = null;
		String sign = null;
		try {
			appid = c.getApplicationContext().getResources().getString(
					BaseUtils.getResourceValue("string", "chartboost_appid"));
			sign = c.getApplicationContext().getResources().getString(
					BaseUtils.getResourceValue("string",
							"chartboost_appsignature"));
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (appid == null || sign == null || TextUtils.isEmpty(appid) || TextUtils.isEmpty(sign)) {
				Log.e("ChartboostEntity",
						"Please setup chartboost_appid and chartboost_appsignature in trackinfo.xml");
				return null;
			}
			
		}


		cbe = new ChartboostEntity();
		cbe.setAppID(appid);
		cbe.setSignature(sign);

		return cbe;

	}

}
