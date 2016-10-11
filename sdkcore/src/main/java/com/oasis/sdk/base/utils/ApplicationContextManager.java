package com.oasis.sdk.base.utils;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.mopub.volley.RequestQueue;
import com.mopub.volley.toolbox.Volley;
import com.oasis.sdk.OASISPlatformConstant;
import com.oasis.sdk.base.db.DBHelper;

public class ApplicationContextManager {
	private Application application;
	private Context context;
	private static ApplicationContextManager contextManger;
	private RequestQueue volleyRequestQueue;
	private DBHelper dbHelper;
	private ApplicationContextManager(Application context){
		this.application = context;
		this.context = context.getApplicationContext();
	}
	public static ApplicationContextManager createContextmanger(Application context) {
		if(contextManger == null)
			contextManger = new ApplicationContextManager(context);
		
		return contextManger;
	}
	public static ApplicationContextManager getInstance(){
		return contextManger;
	}
	/**
	 * 初始化相关参数
	 */
	public void init(){
		        
		getVolleyRequestQueue();
		
		getDBHelper();
		
		// 从meta中获取 gamecode,publickey, paykey,这些值由游戏端在清单中配置。
				String str = null;
				try {
					str = context.getString(BaseUtils.getResourceValue("string", "oasis_sdk_gamecode"));
					if(str != null && !TextUtils.isEmpty(str)){
						SystemCache.GAMECODE = str;
					}else
						Log.e("OASSDK_INIT", "Gamecode don't setup!");
					
					str = context.getResources().getString(BaseUtils.getResourceValue("string", "oasis_sdk_publickey"));
					if(str != null && !TextUtils.isEmpty(str)){
						SystemCache.PUBLICKEY = str;
					}else
						Log.e("OASSDK_INIT", "PublicKey don't setup!");
					
					str = context.getResources().getString(BaseUtils.getResourceValue("string", "oasis_sdk_paykey"));
					if(str != null && !TextUtils.isEmpty(str)){
						SystemCache.PAYKEY = str;
					}else
						Log.e("OASSDK_INIT", "PayKey don't setup!");

					str = context.getResources().getString(BaseUtils.getResourceValue("string", "oasis_sdk_Environment"));
					if(str != null && !TextUtils.isEmpty(str)){
						if(!TextUtils.isEmpty(str) && OASISPlatformConstant.ENVIRONMENT_SANDBOX.equals(str))
							SystemCache.OASISSDK_ENVIRONMENT_SANDBOX = true;
						else if(!TextUtils.isEmpty(str) && "test".equals(str))
							SystemCache.OASISSDK_ENVIRONMENT_TEST = true;
						else{
							SystemCache.OASISSDK_ENVIRONMENT_SANDBOX = false;
							SystemCache.OASISSDK_ENVIRONMENT_TEST = false;
						}
//						SystemCache.SDKMODE_SANDBOX_REQEUST_RESPONSE = true;//SystemCache.OASISSDK_ENVIRONMENT_SANDBOX;//
					}else
						Log.e("OASSDK_INIT", "Environment don't setup!");

					str = context.getResources().getString(BaseUtils.getResourceValue("string", "oasis_sdk_GameMode"));
					if(str != null && !TextUtils.isEmpty(str)){
						if(!TextUtils.isEmpty(str) && OASISPlatformConstant.GAMEMODE_OFFLINE.equals(str))
							SystemCache.OASISSDK_GAMEMODE = OASISPlatformConstant.GAMEMODE_OFFLINE;
						else
							SystemCache.OASISSDK_GAMEMODE = OASISPlatformConstant.GAMEMODE_ONLINE;
					}else
						Log.e("OASSDK_INIT", "Environment don't setup!");
					
				} catch (Exception e1) {
					Log.e("OASSDK_INIT", "Init is fail");
				}
	}

	public Context getContext() {
		return context;
	}
	public String getPackageName() {
		return this.context.getPackageName();
	}
	public DBHelper getDBHelper() {
		if(dbHelper == null){
			dbHelper = new DBHelper(context, Constant.createTables, Constant.dropTables);
			dbHelper.open();
		}
		return dbHelper;
	}
	public RequestQueue getVolleyRequestQueue() {
		if(volleyRequestQueue == null)
			volleyRequestQueue = Volley.newRequestQueue(context);
		return volleyRequestQueue;
	}
	
	
}