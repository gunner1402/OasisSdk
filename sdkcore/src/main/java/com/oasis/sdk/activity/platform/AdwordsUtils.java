package com.oasis.sdk.activity.platform;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.util.Log;

import com.google.ads.conversiontracking.AdWordsAutomatedUsageReporter;
import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.oasis.sdk.activity.platform.entity.AdwordsEntity;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.Constant;
import com.oasis.sdk.base.utils.SystemCache;

public class AdwordsUtils {
	public static final String TAG = "TRACK_AdwordsUtils";
	private AdwordsEntity ae;
	private Context c;
	
	private static final String LAST_RECORDED_VERSION_KEY = "last_recorded_app_version";
	
	public AdwordsUtils(Activity c){

		this.c = c.getApplicationContext();

		ae = AdwordsEntity.getInfoByGameCode(c);

		if (ae != null) {
			AdWordsConversionReporter.registerReferrer(this.c, c.getIntent()
					.getData());// deep link
			AdWordsAutomatedUsageReporter.enableAutomatedUsageReporting(this.c,
					ae.conVersionID);
			try {
				PackageInfo packageInfo = this.c.getPackageManager().getPackageInfo(
						this.c.getPackageName(), 0);
				int currentAppVersion = packageInfo.versionCode;
				int lastRecordedAppVersion = (Integer) BaseUtils.getSettingKVPfromSysCache(
						LAST_RECORDED_VERSION_KEY,  -1);				
				if (currentAppVersion > lastRecordedAppVersion) {
					// reportWithConversionId(
					// "1038185027",
					// "aqUCHIerhAgQw-SF7wM",
					// "0", // The value of your conversion; can be modified to
					// a transaction-specific value.
					// true);
//					reportWithConversionId();
					AdWordsConversionReporter.reportWithConversionId(this.c, ae.conVersionID, ae.conVersionLable, ae.conVersionValue, false);

					BaseUtils.saveSettingKVPtoSysCache(LAST_RECORDED_VERSION_KEY,
							currentAppVersion);
				}

				BaseUtils.logDebug(TAG, "Track:Adwords is running..... ConversionId="
						+ ae.conVersionID);
				
			} catch (NameNotFoundException e) {
				Log.w(TAG, e.getMessage());
			}
		}
		
	}
	/**
	 * 记录转换数据
	 * @param conversionID 			an ID that identifies your conversion
	 * @param conVersionLable		an alphanumeric label that identifies your conversion
	 * @param value					the value of your conversion (must be specified in the currency of your AdWords account)
	 * @param f						a boolean to indicate whether the conversion should fire only once or should fire multiple times
	 */
	public void reportWithConversionId(){
		if(ae != null)
			AdWordsConversionReporter.reportWithConversionId(this.c, ae.conVersionID, ae.conVersionLable, ae.conVersionValue, TextUtils.isEmpty(ae.flag)?true:Boolean.valueOf(ae.flag));
	}
	

//	proguard.cfg:已更新到文件中

	
}
