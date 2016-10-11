package com.oasis.sdk.activity.platform.entity;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.oasis.sdk.base.utils.BaseUtils;


public class AdwordsEntity {
	
	public String conVersionID;
	public String conVersionLable;
	public String conVersionValue;
	public String flag;

	public void setConVersionID(String conVersionID) {
		this.conVersionID = conVersionID;
	}


	public void setConVersionLable(String conVersionLable) {
		this.conVersionLable = conVersionLable;
	}


	public void setConVersionValue(String conVersionValue) {
		this.conVersionValue = conVersionValue;
	}


	public void setFlag(String flag) {
		this.flag = flag;
	}


	public static AdwordsEntity getInfoByGameCode(Activity c) {
		AdwordsEntity se = null;
	
		String id = null;
		String lable = null;
		String value = null;
		try {
			id = c.getResources().getString(
					BaseUtils.getResourceValue("string", "admob_conversion_id"));
			lable = c.getResources().getString(
					BaseUtils.getResourceValue("string",
							"admob_conversion_label"));
			value = c.getResources().getString(
					BaseUtils.getResourceValue("string",
							"admob_conversion_value"));
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (TextUtils.isEmpty(id) || TextUtils.isEmpty(lable)
					|| TextUtils.isEmpty(value)) {
				Log.e("AdmobEntity",
						"Please setup admob_conversion_id and admob_conversion_label and admob_conversion_value in trackinfo.xml");
				return null;
			}			
		}


		se = new AdwordsEntity();
		se.setConVersionID(id);
		se.setConVersionLable(lable);
		se.setConVersionValue(value);
		se.setFlag("true");

		return se;
		
	}
	
	
	
}
