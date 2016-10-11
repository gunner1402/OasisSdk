package com.oasis.sdk.base.communication;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class NotificationClickReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
//		Log.e("From Notification", intent.getExtras().getString("message"));
		Intent intent2 = null;
		String pkName = context.getPackageName();
		PackageManager packageManager = context.getPackageManager();   
		if(!isAppOnForeground(context, pkName)){
        	intent2 = packageManager.getLaunchIntentForPackage(pkName);
        	intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	context.startActivity(intent2);
		}
		
	}
	
	public boolean isAppOnForeground(Context context, String packageName) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		if (appProcesses == null)
			return false;
		RunningAppProcessInfo appProcess = appProcesses.get(0);
		if (appProcess!=null&&appProcess.processName.equals(packageName)
				&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
			return true;
		}

		return false;

	}

}
