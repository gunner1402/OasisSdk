package com.oasis.sdk.activity.platform;

import static com.adjust.sdk.Constants.ENCODING;
import static com.adjust.sdk.Constants.MALFORMED;
import static com.adjust.sdk.Constants.REFERRER;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.oasis.sdk.base.entity.PhoneInfo;

public class TrackReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		/**------------2014-09-03 删除 sponsorpay引用------------**/
//		com.sponsorpay.advertiser.InstallReferrerReceiver spInstallReferrerReceiver = new com.sponsorpay.advertiser.InstallReferrerReceiver();
//		spInstallReferrerReceiver.onReceive(context, intent);
		
		com.google.ads.conversiontracking.InstallReceiver googleInstallReceiver = new com.google.ads.conversiontracking.InstallReceiver();
		googleInstallReceiver.onReceive(context, intent);
		
		com.adjust.sdk.AdjustReferrerReceiver adjustReceiver = new com.adjust.sdk.AdjustReferrerReceiver();
		adjustReceiver.onReceive(context, intent);
		
		/**------------2016-04-08 删除 Inmobi引用------------**/
//		new IMAdTrackerReceiver().onReceive(context, intent);
		
        String rawReferrer = intent.getStringExtra(REFERRER);
        if (null == rawReferrer) {
            return;
        }

        String referrer;
        try {
            referrer = URLDecoder.decode(rawReferrer, ENCODING);
        } catch (UnsupportedEncodingException e) {
            referrer = MALFORMED;
        }

        PhoneInfo.instance().setReferrer(referrer);
       
		// Google Analytics
//        new CampaignTrackingReceiver().onReceive(context, intent);
	}

}
