package com.oasis.sdk.activity.platform;

import android.app.Activity;

import com.chartboost.sdk.Chartboost;
import com.oasis.sdk.activity.platform.entity.ChartboostEntity;
import com.oasis.sdk.base.utils.BaseUtils;

/**
 * Minimum API level 9 (Android OS 2.3)
 * @author Administrator
 *
 */

public class ChartboostUtils {
	public static final String TAG = "TRACK_ChartboostUtils";
	private static Activity c;
	ChartboostEntity cbe;
	
	public ChartboostUtils (Activity activity){
		c = activity;
		
		
		cbe = ChartboostEntity.getInfoByGameCode(activity);

		if (cbe != null) {
			// Configure Chartboost
			Chartboost.startWithAppId(activity, cbe.appID, cbe.signature);
			Chartboost.onCreate(activity);
			BaseUtils.logDebug(TAG, "Track:Chartboost is running..... AppId=" + cbe.appID);
		}
		
	}
	
	public void onStart(){
		if (cbe != null) 
			Chartboost.onStart(c);
	    
	    BaseUtils.logDebug(TAG, "Chartboost OnStart()");
	}
	public void onResume(){
		if (cbe != null) 
			Chartboost.onResume(c);
	}
	public void onPause() {
		if (cbe != null) 
			Chartboost.onPause(c);
	}
	public void onStop() {
		if (cbe != null) 
			Chartboost.onStop(c);
	}
	public void onDestroy() {
		if (cbe != null) 
			Chartboost.onDestroy(c);
	}
	/**
	 * If an interstitial is on screen, close it. Otherwise continue as normal. 
	 * @return
	 */
	public boolean onBackPressed() { 
		if (cbe != null) 
			return Chartboost.onBackPressed();
		else
			return false;
	}

//	proguard.cfg: 已配置到文档中

	
}
