package com.oasis.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.os.Bundle;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.oasis.sdk.base.utils.ApplicationContextManager;
import com.oasis.sdk.base.utils.BaseUtils;

public class OASISApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        
		ApplicationContextManager.createContextmanger(this);
		
		try {
			String appToken = getApplicationContext().getResources().getString(
					BaseUtils.getResourceValue("string", "adjust_app_token"));
			String sdkEnvironment = getApplicationContext().getResources().getString(
					BaseUtils.getResourceValue("string", "oasis_sdk_Environment"));
			String appEnvironment = (OASISPlatformConstant.ENVIRONMENT_SANDBOX.equals(sdkEnvironment)) ? AdjustConfig.ENVIRONMENT_SANDBOX
					: AdjustConfig.ENVIRONMENT_PRODUCTION;
			AdjustConfig config = new AdjustConfig(getApplicationContext(), appToken, appEnvironment);
			config.setEventBufferingEnabled(false);
			Adjust.onCreate(config);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
	}
	private static final class AdjustLifecycleCallbacks implements ActivityLifecycleCallbacks {
        @Override
        public void onActivityResumed(Activity activity) {
            Adjust.onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Adjust.onPause();
        }

		@Override
		public void onActivityCreated(Activity activity,
				Bundle savedInstanceState) {
		}

		@Override
		public void onActivityStarted(Activity activity) {
		}

		@Override
		public void onActivityStopped(Activity activity) {
		}

		@Override
		public void onActivitySaveInstanceState(Activity activity,
				Bundle outState) {
		}

		@Override
		public void onActivityDestroyed(Activity activity) {
		}

        
    }
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		BaseUtils.setCurrentLangusge(this);
	}
}
