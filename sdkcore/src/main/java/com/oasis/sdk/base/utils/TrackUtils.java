package com.oasis.sdk.base.utils;

import android.app.Activity;

import com.oasis.sdk.activity.platform.AdwordsUtils;
import com.oasis.sdk.activity.platform.ChartboostUtils;
import com.oasis.sdk.activity.platform.GoogleUtils;
import com.oasis.sdk.base.entity.PhoneInfo;

public class TrackUtils {
	private static ChartboostUtils chartboost;
	private static GoogleUtils google;
	private static AdwordsUtils adwords;
	
	public static void onCreate(Activity c){
		PhoneInfo.instance().setMdataAppID(c.getString(BaseUtils.getResourceValue("string", "mdata_appid")));
		
		if(chartboost == null)
			chartboost = new ChartboostUtils(c);
		
		if(google == null)
			google = GoogleUtils.instance(c);
		if(adwords == null)
			adwords = new AdwordsUtils(c);
		
		BaseUtils.cacheLog(1, "Track_onCreate done.");
		
	}
	public static void onStart(Activity c){
		if(chartboost != null)
			chartboost.onStart();
		if(google != null)
			google.onStart();

		BaseUtils.cacheLog(1, "Track_onStart done.");
	}
	public static void onResume(Activity c){
		
		BaseUtils.cacheLog(1, "Track_onResume done.");
	}
	public static void onPause(Activity c){
		
		BaseUtils.cacheLog(1, "Track_onPause done.");
	}
	public static void onStop(Activity c){
		if(chartboost != null)
			chartboost.onStop();
		if(google != null)
			google.onStop();
		
		BaseUtils.cacheLog(1, "Track_onStop done.");
	}
	public static void onDestroy(Activity c){
		if(chartboost != null)
			chartboost.onDestroy();
		
		BaseUtils.cacheLog(1, "Track_onDestroy done.");
	}
	public static boolean onBackPressed(Activity c){
		if(chartboost != null)
			return chartboost.onBackPressed();
		
		return false;
	}
	
}
