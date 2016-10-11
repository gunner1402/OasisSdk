package com.oasis.sdk.base.communication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.SystemCache;

public class ConnectionChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				BaseUtils.setNetworkState(context);
				
				if(SystemCache.NetworkisAvailable && !BaseUtils.isOnLine() && !BaseUtils.isLogin() && SystemCache.localInfo != null){
					// 1网络可用  2单机游戏  3没有登录成功  4执行过登录操作（CP第一次必须调用登录接口）
					
					try {
						HttpService.instance().loginWithRecentlyUser(null);
						((Activity)context).runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								if(BaseUtils.isLogin()){
									SystemCache.localInfo = null;// 确认登录成功后，清除本次本地用户登录信息
									SystemCache.oasisInterface.reloadGame(SystemCache.userInfo);
								}	
							}
						});
						
					} catch (Exception e) {}
				}

				if(SystemCache.NetworkisAvailable && (SystemCache.adjustEventMap == null || SystemCache.adjustEventMap.isEmpty())){
					try {
						HttpService.instance().getAdjustConfigInfos();
					} catch (Exception e) {
					}
				}
				if(SystemCache.NetworkisAvailable){// 网络环境改变时，获取套餐
					BaseUtils.getPayInfo();
				}
			}
		}).start();

	}

	
}
