/**
 * 应用工具类
 */
package com.oasis.sdk.base.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.adjust.sdk.Adjust;
import com.android.base.http.CallbackResultForActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.oasis.sdk.OASISPlatformConstant;
import com.oasis.sdk.OASISPlatformConstant.Language;
import com.oasis.sdk.OASISPlatfromMenu;
import com.oasis.sdk.base.Exception.OasisSdkException;
import com.oasis.sdk.base.entity.MemberBaseInfo;
import com.oasis.sdk.base.entity.PayInfoList;
import com.oasis.sdk.base.entity.PhoneInfo;
import com.oasis.sdk.base.entity.UserInfo;
import com.oasis.sdk.base.report.ReportTimer;
import com.oasis.sdk.base.report.ReportUtils;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.service.RegistrationIntentService;
import com.oasis.sdk.pay.googleplay.utils.GoogleBillingTimer;
import com.oasis.sdk.pay.googleplay.utils.GoogleBillingUtils;

/**
 * @author xdb
 * 
 */
public class BaseUtils {

	/**
	 * 清楚所有缓存
	 */
	public static void clearALL(){
		if(SystemCache.menu != null)// 当应用退出时，将menu位置记录到本地
			SystemCache.menu.remenberLocation();
		
		SystemCache.clear();
	}
	
	/**
	 * 根据条件，决定是否显示OG按钮
	 */
	public static boolean showMenu(final Activity activity, final int showLocation, final boolean isShow){
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if(isShow && SystemCache.controlInfo.getOg_onoff_control()){
					if(SystemCache.menu != null){
						Log.d("OASISPlatfromMenu", "Do not add new menu. That is exist!"+SystemCache.menu.getParent().hashCode()+"    new Activity hashcode:"+activity.hashCode());
						SystemCache.menu.setVisibility(View.GONE);
						SystemCache.menu.remenberLocation();
						SystemCache.menu = null;
//						return;
					}
					SystemCache.menu = new OASISPlatfromMenu(activity, showLocation);
					activity.getWindow().addContentView(SystemCache.menu, new android.view.WindowManager.LayoutParams(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT));
					SystemCache.menu.setVisibility(View.VISIBLE);
				}else{
					if(SystemCache.menu != null){
						SystemCache.menu.setVisibility(View.GONE);
						SystemCache.menu.remenberLocation();
						SystemCache.menu = null;
					}
					
				}
			}
		});
		return true;
	}
	
	private static void showMsg(Context context, String msg, int type) {
		if (type == Toast.LENGTH_LONG)
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		else if (type == Toast.LENGTH_SHORT)
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	/**
	 * Toast显示提示信息
	 * 
	 * @param context
	 * @param msg
	 *            显示内容
	 */
	public static void showMsg(Context context, String msg) {
		showMsg(context, msg, Toast.LENGTH_LONG);
	}
	/**
	 * Toast显示提示信息
	 * 
	 * @param context
	 * @param name
	 *            词条name
	 */
	public static void showMsg2(Context context, String name){
		showMsg(context, context.getString(BaseUtils.getResourceValue("string", name)));
	}
	/**
	 * 根据code获取相应的错误信息
	 * @param c
	 * @param code	错误码
	 */
	public static void showErrorMsg(Context c, String code){
		int noticeID = getResourceValue("string", "oasisgames_sdk_common_errorcode_negative_"+Math.abs(Integer.valueOf(code))); 
		
		showMsg(c, c.getString(noticeID));
	}

	/**
	 * 当玩家点击游戏中商城时，需要判断下一个界面谁，不同的情况有不一样的处理
	 */
	public static boolean checkGoogleisAble(Context c){
		if(!SystemCache.controlInfo.getCharge_onoff_control(c) || SystemCache.payInfoLists == null || SystemCache.payInfoLists.size() <= 1)
			return false;
		
		boolean flag = false;
		for (PayInfoList list : SystemCache.payInfoLists) {
			if("mob_google".equals(list.pay_way) && list.list != null && list.list.size() > 0){
				flag = true;
				break;
			}
		}
		return flag;
	}
	/**
	 * 获取支付的相关信息， 包括支付开关、套餐信息；如果开关未打开或接口调用失败，不再获取支付套餐
	 * 
	 * 启动游戏、切换网络成功时将被调用
	 */
	public static void getPayInfo(){
		if(!SystemCache.NetworkisAvailable)
			return;
		HttpService.instance().getConutryCodeByIP(new CallbackResultForActivity() {
			
			@Override
			public void success(Object data, String statusCode, String msg) {
				HttpService.instance().getPayKindsInfo(null);
			}
			
			@Override
			public void fail(String statusCode, String msg) {
				HttpService.instance().getPayKindsInfo(null);
			}
			
			@Override
			public void excetpion(Exception e) {
				HttpService.instance().getPayKindsInfo(null);
			}
		});
		return;
	}
	public static void initInfo(Context c, HashMap<String, String> oldUsers, String notRegistUserName){

		ApplicationContextManager.getInstance().init();
		
		int rotation = ((Activity)c).getRequestedOrientation();
		SystemCache.SCREENROTATION = rotation;  
		
		//初始化手机信息，方便以后调用
		getPhoneInfo(c); 
		GoogleBillingUtils.getIdThread(c);
			
		saveSettingKVPtoSysCache(Constant.SHAREDPREFERENCES_CURRENTUSERINFOS, "");
		setDensity((Activity)c);
		
		if(oldUsers != null){ 
			List<MemberBaseInfo> list = getSPMembers();
			if(list == null || list.size() <= 0)
				for (Entry<String, String> iter : oldUsers.entrySet()) {
					cacheUserInfo(iter.getKey(), iter.getValue());
				}
		}
		
		if(!TextUtils.isEmpty(notRegistUserName)){
			String curNotRegistUserName = (String) getSettingKVPfromSysCache("notRegistUserName", "");
			if(TextUtils.isEmpty(curNotRegistUserName)){
				saveSettingKVPtoSysCache("notRegistUserName", notRegistUserName);
			}
		}
		Language latestLanguage =Language.valueOf(((String) getSettingKVPfromSysCache(Constant.LATEST_LANGUAGE, "EN")).toUpperCase()) ;
		changeLanguge(c, latestLanguage);
	}
	/**
	 * 向System.setting中保存数据,自动判断是否为空，如是空，重新获取
	 * @param key 要保存的键值对的键
	 * @param obj 要保存的键值对的值
	 */
	public static void saveSettingKVPtoSysCache(String key,Object obj){
		checkSystemCacheSetting();
		if(SystemCache.settingEditor == null)
			return;
		if(obj instanceof Boolean){
			SystemCache.settingEditor.putBoolean(key, (Boolean)obj);
		}else if(obj instanceof String){
			SystemCache.settingEditor.putString(key, (String)obj);
		}else if(obj instanceof Integer){
			SystemCache.settingEditor.putInt(key, (Integer)obj);
		}else if(obj instanceof Long){
			SystemCache.settingEditor.putLong(key, (Long)obj);
		}
		SystemCache.settingEditor.commit();
	}

	private static void checkSystemCacheSetting() {
		if(SystemCache.setting == null||SystemCache.settingEditor == null){
			SystemCache.setting = ApplicationContextManager.getInstance().getContext().getApplicationContext()
					.getSharedPreferences("oasis", Context.MODE_PRIVATE);
			SystemCache.settingEditor = SystemCache.setting.edit();
		}
	}
	/**
	 * 获取System.setting中保存的数据
	 * @param key 要获取值的键
	 * @param defvalue 默认值
	 * @return
	 */
	public static Object getSettingKVPfromSysCache(String key,Object defvalue) {
		checkSystemCacheSetting();
		if(defvalue instanceof Boolean){
			return SystemCache.setting.getBoolean(key, (Boolean) defvalue);
		}else if(defvalue instanceof String){
			return SystemCache.setting.getString(key, (String) defvalue);
		}else if(defvalue instanceof Integer){
			return SystemCache.setting.getInt(key,(Integer) defvalue);
		}else if(defvalue instanceof Long){
			return SystemCache.setting.getLong(key,(Long) defvalue);
		}
		return null;
	}


	public static void initOtherInfo(){
		
		getPayInfo();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					HttpService.instance().getAdjustConfigInfos();
				} catch (OasisSdkException e) {
					
					e.printStackTrace();
				}
				
				try {
					ReportUtils.reportTimer.schedule(new ReportTimer(), 0, 30000);
				} catch (Exception e) {
					ReportUtils.reportTimer = new Timer();
					ReportUtils.reportTimer.schedule(new ReportTimer(), 0, 30000);
				} 
				
			}
		}).start();
		
		try {
			GoogleBillingUtils.GoogleBillingTimer.schedule(new GoogleBillingTimer(), 0, 30000);
		} catch (Exception e) {
			GoogleBillingUtils.GoogleBillingTimer = new Timer();
			GoogleBillingUtils.GoogleBillingTimer.schedule(new GoogleBillingTimer(), 0, 2);
		} 
		
	}
	/**
	 * 是否为在线游戏   true：在线游戏    false：单机游戏
	 * @return
	 */
	public static Boolean isOnLine(){
		if(SystemCache.OASISSDK_GAMEMODE.equals(OASISPlatformConstant.GAMEMODE_ONLINE))
			return true;
		else
			return false;
	}
	/**
	 * 是否为沙盒模式
	 * @return
	 */
	public static Boolean isSandBox(){
		return SystemCache.OASISSDK_ENVIRONMENT_SANDBOX;
	}
	/**
	 * 是否为测试模式
	 * @return
	 */
	public static Boolean isTestMode(){
		return SystemCache.OASISSDK_ENVIRONMENT_TEST;
	}
	/**
	 * 获取手机基本信息
	 * @param c
	 * @return
	 */
	public static PhoneInfo getPhoneInfo(Context c){
		PhoneInfo info = PhoneInfo.instance();
		TelephonyManager tm = (TelephonyManager) c.getSystemService(Service.TELEPHONY_SERVICE);
		info.setDeviceId("");
		try{
			String deviceid = tm.getDeviceId();
			if(deviceid != null && !TextUtils.isEmpty(deviceid) && !deviceid.contains("000000"))// 当设备ID包含6个零以上，视为无效的设备id；获取mobileCode时，如果设备id为空，将获取ADID
				info.setDeviceId(deviceid);
		}catch (SecurityException se){
			se.printStackTrace();
		}


		info.setModel(Build.MODEL);
		info.setBrand(Build.BRAND);
		info.setSoftwareType("android");//android.os.Build.TYPE
//		info.setSoftwareVersion(tm.getDeviceSoftwareVersion());
		info.setSoftwareVersion(Build.VERSION.RELEASE);
//		info.setNetworkType(String.valueOf(tm.getNetworkType()));
//		info.setSubscriberId(tm.getSubscriberId());
//		info.setLine1Number(tm.getLine1Number());
		
		info.setAndroidID(RC4.HloveyRC4toHex(Secure.getString(c.getContentResolver(), Secure.ANDROID_ID)));
		info.setAndroidID_normal(Secure.getString(c.getContentResolver(), Secure.ANDROID_ID));
		
		if(tm.getSimState() == TelephonyManager.SIM_STATE_READY){
			info.setIso2Country(tm.getSimCountryIso());// 短信支付时需要此值获取套餐
		}
		
		/**
		 * 获取版本号
		 */
	    try {
	        PackageManager manager = c.getPackageManager();
	        PackageInfo packinfo = manager.getPackageInfo(c.getPackageName(), 0);
	        info.setBundleid(packinfo.packageName);
			info.setBundleversion(packinfo.versionName);
			info.setBundleversioncode(""+packinfo.versionCode);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		
		DisplayMetrics dm = new DisplayMetrics();  
        ((Activity)c).getWindowManager().getDefaultDisplay().getMetrics(dm);
        info.setScreen(dm.widthPixels+"_"+dm.heightPixels);
        info.setDensity(String.valueOf(dm.density));
        
        try {// 处理异常
			String str = c.getResources().getString(BaseUtils.getResourceValue("string", "oasis_sdk_signkey"));
			if(str != null && !TextUtils.isEmpty(str))
				info.setSignKey(str);
		} catch (NotFoundException e) {
			info.setSignKey("");
		}
        
//		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
//        info.setReferrer(preferences.getString("referrer", ""));
		return info;
	}

	/**
	 * 获取手机唯一码
	 * 优先顺序：	1、游戏方传来的唯一码（肯能为任意字符）
	 * 			2、DeviceID
	 * 			3、AndroidID
	 * 			4、"OAS_ANDROID_"+System.nanoTime()
	 * @return
	 */
	public static String getMobileCode(){
		String mobileCode = (String) getSettingKVPfromSysCache("notRegistUserName", "");
		
		if(TextUtils.isEmpty(mobileCode))
			mobileCode = PhoneInfo.instance().deviceId;// deviceId已过滤 多个零的情况
		
		if(TextUtils.isEmpty(mobileCode))
			mobileCode = PhoneInfo.instance().androidID;
		
		if(TextUtils.isEmpty(mobileCode))
			mobileCode = PhoneInfo.instance().adid;
		
		if(TextUtils.isEmpty(mobileCode))
			mobileCode = "OAS_ANDROID_"+System.nanoTime();
		
		saveSettingKVPtoSysCache("notRegistUserName", mobileCode);

		return mobileCode;
	}
	/**
	 * 清除当前用户信息及mobileCode;主要目的是协助测试将同一设备置为匿名用户
	 */
	public static void clearUserinfo(){

		saveSettingKVPtoSysCache("notRegistUserName", "Android_Test_"+System.currentTimeMillis());
		saveSettingKVPtoSysCache(Constant.SHAREDPREFERENCES_RECENTLYUSERINFOS, "");
		saveSettingKVPtoSysCache("members", "");
		
	}
	/**
	 * 检测网络是否可用.
	 * 
	 * @param context
	 * @return
	 */
	public static boolean networkIsAvailable(Context context) {

		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			return false;
		}
		NetworkInfo network = manager.getActiveNetworkInfo();
		if (network == null) {
			return false;
		}

		if (!network.isAvailable()) {
			return false;
		}
		return true;
	}

	/**
	 * 设置系统缓存网络状态.
	 * 
	 * @param context
	 */
	public static void setNetworkState(Context context) {
		if (!BaseUtils.networkIsAvailable(context)) {// 检测网络是否可用
			SystemCache.NetworkisAvailable = false;
			return;
		}

		SystemCache.NetworkisAvailable = true;
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnected()) {
			SystemCache.NetworkExtraInfo = "";
			return;
		}
		
		String extraInfo = "";
		try {
			extraInfo = networkInfo.getExtraInfo();
		} catch (Exception e) {
			e.printStackTrace();
			extraInfo = "";
		}
		if (TextUtils.isEmpty(extraInfo) || "null".equals(extraInfo)) {
			SystemCache.NetworkExtraInfo = "";
			return;
		}
		
		if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			if (extraInfo.contains("wap") || extraInfo.contains("WAP")) {
				SystemCache.NetworkExtraInfo = "cmwap";
				return;
			}
		}
		
		SystemCache.NetworkExtraInfo = extraInfo;
	}

	/**
	 * 检测当前可用网络是否为WIFI
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWifiNetworkAvailable(Context context) {
		boolean flag = false;
		if (SystemCache.NetworkisAvailable) {
			ConnectivityManager mConnMgr = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWifi = mConnMgr
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWifi != null && mWifi.isAvailable()) {
				if (mWifi.isConnected()) {
					flag = true;
				}
			}
		}
		return flag;
	}
	/**
	 * 检测当前可用网络是否为WIFI
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isMobileNetworkAvailable(Context context) {
		boolean flag = false;
		if (SystemCache.NetworkisAvailable) {
			ConnectivityManager mConnMgr = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mobile = mConnMgr
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mobile != null && mobile.isAvailable()) {
				if (mobile.isConnected()) {
					flag = true;
				}
			}
		}
		return flag;
	}

	/**
	 * 注册receiver.
	 * 
	 * @param receiver
	 * @param action
	 * @param context
	 */
	public static void registerReceiver(BroadcastReceiver receiver,
			String action, Context context) {
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		context.registerReceiver(receiver, filter);
	}

	/**
	 * 注销receiver.
	 * 
	 * @param receiver
	 * @param context
	 */
	public static void unRegisterReceiver(BroadcastReceiver receiver,
			Context context) {
		try {
			context.unregisterReceiver(receiver);
		} catch (Exception e) {
			Log.e("BaseUtils", e.getMessage());
		}
	}

	/**
	 * 加载更多
	 * 
	 * @return
	 */
	public static View getLoadMoreFootView(Activity activity, int layout) {
		LayoutInflater inflater = activity.getLayoutInflater();
		View view = inflater.inflate(layout, null);
		return view;
	}

	/**
	 * 最后一行
	 * 
	 * @return
	 */
	public static View getEndViewFootView(Activity activity, int layout) {
		LayoutInflater inflater = activity.getLayoutInflater();
		View view = inflater.inflate(layout, null);
		return view;

	}

	/**
	 * 没有数据
	 * 
	 * @return
	 */
	public static View getNotDataFootView(Activity activity, int layout) {
		LayoutInflater inflater = activity.getLayoutInflater();
		View view = inflater.inflate(layout, null);
		return view;

	}

	/**
	 * 判断存储卡是否存在 
	 * @return
	 */
	public static boolean checkSDCard() {

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取SD卡剩余空间.
	 * 
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static long getSDCardAvailableBlocks() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
				return sf.getAvailableBlocksLong();
			else
				return sf.getAvailableBlocks();
		} else
			return 0;
	}

	/**
	 * 
	 * 进度加载方法
	 */
	public static ProgressDialog loadProgress(Activity activity) {
		// 带进度条的对话框
		ProgressDialog mydialog = new ProgressDialog(activity);
		mydialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//		mydialog.setTitle("数据加载提示");
//		mydialog.setMessage("Loading ...");
		mydialog.setIndeterminate(true);
		mydialog.show();
		mydialog.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_waiting_anim"));
		return mydialog;
	}
	
	/**
	 * 
	 * @param activity
	 * @param layout
	 * @return
	 */
	public static AlertDialog createWaitDialog(Activity activity, int layout) {
		AlertDialog dialog_wait = new AlertDialog.Builder(activity).create();
		dialog_wait.show();
		if (layout == -1)// 默认样式
			layout = BaseUtils.getResourceValue(activity, "layout", "oasisgames_sdk_common_waiting_anim");
		dialog_wait.setContentView(layout);
		dialog_wait.setCanceledOnTouchOutside(false);
		return dialog_wait;
	}

	private static float density = 1;
	public static void setDensity(Activity a){
		DisplayMetrics outMetrics = new DisplayMetrics();
		a.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		density = outMetrics.density;
	}
	public static DisplayMetrics getDisplayMetrics(Activity a){
		DisplayMetrics outMetrics = new DisplayMetrics();
		a.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		return outMetrics;
	}
	/**
	 * 获取屏幕分辨率
	 *
	 * @return
	 */
	public static float getDensity() {
		return density;
	}

	/**
	 * 账号注册时特殊字符验证
	 * @param text
	 * @return
	 */
	public static boolean regexSpecilChar(String text){
		
		return RegexName(text, "^[^&#]+");
	}
	/**
	 * 纯数字格式验证
	 * @param text
	 * @return
	 */
	public static boolean regexNum(String text){
		
		return RegexName(text, "^[0-9]+");
	}
	/**
	 * 账号格式验证  0-9a-zA-Z 下划线
	 * @param text
	 * @return
	 */
	public static boolean regexAccount(String text){
		
		return RegexName(text, "^[a-zA-Z0-9_]+");
	}
	static String emailRegEx = "^([a-z0-9A-Z]+[-|\\._]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"; 
	/**
	 * 邮箱格式验证
	 * @param email
	 * @return
	 */
	public static boolean regexEmail(String email){
		 
	        // 1、\\w+表示@之前至少要输入一个匹配字母或数字或下划线 \\w 单词字符：[a-zA-Z_0-9]
	        // 2、(\\w+\\.)表示域名. 如新浪邮箱域名是sina.com.cn
	        // {1,3}表示可以出现一次或两次或者三次.
//	        String reg = "\\w+@(\\w+\\.){1,3}\\w+";
	        String reg = "(\\w+[-|\\.]?)+\\w+@(\\w+\\.){1,3}\\w+";
	        Pattern pattern = Pattern.compile(reg);
	        boolean flag = false;
	        if (email != null) {
	            Matcher matcher = pattern.matcher(email);
	            flag = matcher.matches();
	        }
	        return flag;
	   
//		return RegexName(email, emailRegEx);//长度过长时，容易导致APP无响应
	}
	/**
	 * 正则验证
	 * 
	 * @return
	 */
	public static boolean RegexName(String str, String eg) {
		Matcher m = Pattern.compile(eg,
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(str);
		return m.matches();
	}

	/**
	 * 通过Bitmap形式设置背景，在activity销毁时，一定调用distoryBackgroudByBitmap
	 * 
	 * @param r
	 * @param view
	 * @param imageId
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void setBackgroudByBitmap(Resources r, View view, int imageId) {
		if (null != view) {
			Bitmap bm = BitmapFactory.decodeResource(r, imageId);
			BitmapDrawable bd = new BitmapDrawable(r, bm);
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
				view.setBackground(bd);
			else
				view.setBackgroundDrawable(bd);
		}
	}

	/**
	 * 销毁Bitmap，减少内存
	 * 
	 * @param r
	 * @param view
	 */
	public static void distoryBackgroudByBitmap(Resources r, View view) {
		BitmapDrawable bd = null;
		if (null != view) {
			bd = (BitmapDrawable) view.getBackground();
			view.setBackgroundResource(0);// 别忘了把背景设为null，避免onDraw刷新背景时候出现used a
											// recycled bitmap错误
			bd.setCallback(null);
			bd.getBitmap().recycle();
		}
	}

	public static void cacheUserInfo(String memberName, String memberPwd) {
		if(TextUtils.isEmpty(memberPwd))// 密码为空，不报存账户信息 
			return;
		
		// 需要考虑更加安全的方式

		// OAS 账号，需要保存账号及密码，其余类型不用处理
		List<MemberBaseInfo> memberBaseInfos = getSPMembers();
		String userinfos = memberName + "/"+AESUtils.encrypt(memberPwd);
		if(null == memberBaseInfos || memberBaseInfos.size() <= 0){
			saveSettingKVPtoSysCache("members", userinfos);
			return;
		} 

		checkUser(memberName, memberPwd, memberBaseInfos);// 检查是否已存在相同用户信息，存在先移除该用户信息
		int index = 0;
		// 最终保存的用户信息都在list中
		for (MemberBaseInfo user : memberBaseInfos) {
			if(index >= 2)// 最多存3个，所以此处为2
				continue;
			String uname = user.memberName;
			String pwd = user.password;
			String userinfo = uname + "/" + pwd;
			if (userinfos == "") {
				userinfos = userinfo;
			} else {
				userinfos += "," + userinfo;
			}
			index ++;
		}

		saveSettingKVPtoSysCache("members", userinfos);
	}

	public static void deleteUserInfo(String memberName, String memberPwd){
		List<MemberBaseInfo> memberBaseInfos = getSPMembers();
		if(null == memberBaseInfos || memberBaseInfos.size() <= 0){
			return;
		}
		String userinfos = "";
		for (MemberBaseInfo user : memberBaseInfos) {
			
			String uname = user.memberName;
			String pwd = user.password;
			
			if(uname.equals(memberName)){
				continue;
			}
			
			String userinfo = uname + "/" + pwd;
			if ("".equals(userinfos)) {
				userinfos = userinfo;
			} else {
				userinfos += "," + userinfo;
			}
		}

		saveSettingKVPtoSysCache("members", userinfos);
	}
	// 得到用户信息
	public static List<MemberBaseInfo> getSPMembers() {
		List<MemberBaseInfo> memberBaseInfos = new ArrayList<MemberBaseInfo>();// 用于保存用户列表信息
		String userinfos = (String) getSettingKVPfromSysCache("members", "");// 取得所有用户信息
		// 获得用户字串
		if (userinfos != "")// 有数据
		{
			// name1/pwd1,name2/pwd2
			if (userinfos.contains(","))// 判断有无, 逗号代表用户每个用户分割点
			{
				String[] users = userinfos.split(",");
				for (String str : users) {
					MemberBaseInfo memberBaseInfo = new MemberBaseInfo();
					String[] user = str.split("/");
					if(user.length>=1)
						memberBaseInfo.memberName = TextUtils.isEmpty(user[0])?"":user[0];// 用户名
					if(user.length>=2)	
						memberBaseInfo.password = TextUtils.isEmpty(user[1])?"":user[1];// 密码
					memberBaseInfos.add(memberBaseInfo);
				}
			} else {
				// 没有, 代表只有一个用户
				MemberBaseInfo memberBaseInfo = new MemberBaseInfo();
				String[] user = userinfos.split("/");
				if(user.length>=1)
					memberBaseInfo.memberName = TextUtils.isEmpty(user[0])?"":user[0];// 用户名
				if(user.length>=2)	
					memberBaseInfo.password = TextUtils.isEmpty(user[1])?"":user[1];// 密码
				memberBaseInfos.add(memberBaseInfo);
			}
			return memberBaseInfos;
		} else {
//			MemberBaseInfo memberBaseInfo = new MemberBaseInfo();
//			memberBaseInfo.memberName = "user1";
//			memberBaseInfo.password ="1";
//			memberBaseInfos.add(memberBaseInfo);
//			
//			memberBaseInfo = new MemberBaseInfo();
//			memberBaseInfo.memberName = "user2";
//			memberBaseInfo.password ="2";
//			memberBaseInfos.add(memberBaseInfo);
			return memberBaseInfos;
		}
	}

	// 检查是否包含此用户名 没有包含就保存到?
	private static void checkUser(String memberName, String memberPwd,
			List<MemberBaseInfo> memberBaseInfos) {
		int position = -1;
		int num = memberBaseInfos.size();
		for (int i = 0; i <num; i++) {
			if (memberName.equals(memberBaseInfos.get(i).memberName)) {
				position = i;
				break;
			}
		}
		if (position >= 0) {// 已存在
			memberBaseInfos.remove(position);
		}
//		MemberBaseInfo memberBaseInfo = new MemberBaseInfo();
//		memberBaseInfo.memberName = memberName;
//		memberBaseInfo.password = memberPwd;
//		memberBaseInfos.add(memberBaseInfo);
	}
	
	/**
	 * 动态获取资源
	 * @param type
	 * @param name
	 * @return
	 */
	public static int getResourceValue(String type, String name){
		Class r = null;
        int id = 0;
        try
        {
        	r = Class.forName(ApplicationContextManager.getInstance().getPackageName() + ".R");

            Class[] classes = r.getClasses();
            Class desireClass = null;

            for (int i = 0; i < classes.length; i++)
            {
                if (classes[i].getName().split("\\$")[1].equals(type))
                {
                    desireClass = classes[i];
                    break;
                }
            }

            if (desireClass != null)
                id = desireClass.getField(name).getInt(desireClass);
        }catch(Exception e){
        	e.printStackTrace();
        }
		return id;
	}
	public static int getResourceValue(Context c, String type, String name){
//		if(TextUtils.isEmpty(ApplicationContextManager.getInstance().getPackageName())){
//			PackageManager manager = c.getPackageManager();
//	        try {
//				PackageInfo packinfo = manager.getPackageInfo(c.getPackageName(), 0);
//				SystemCache.packageName = packinfo.packageName;
//			} catch (NameNotFoundException e) {
//			}
//		}
		
		return getResourceValue(type, name);
	}
	public static int[] getResourceValueByStyleable(String name){
		Class r = null;
		int[] id = null;
		try
		{
			r = Class.forName(ApplicationContextManager.getInstance().getPackageName() + ".R");
			
			Class[] classes = r.getClasses();
			Class desireClass = null;
			
			for (int i = 0; i < classes.length; i++)
			{
				if (classes[i].getName().split("\\$")[1].equals("styleable"))
				{
					desireClass = classes[i];
					break;
				}
			}
			if ((desireClass != null) && (desireClass.getField(name).get(desireClass) != null) && (desireClass.getField(name).get(desireClass).getClass().isArray()))  
		        id = (int[])desireClass.getField(name).get(desireClass);
		}catch(Exception e){
			e.printStackTrace();
		}
		return id;
	}
	/**
	 * 注销时，清除服ID、角色ID
	 * @return
	 */
	public static boolean clearInfoForLogout(){
		ReportUtils.lastReport();
		if(SystemCache.userInfo != null){
			SystemCache.userInfo.setServerID("");
			SystemCache.userInfo.setRoleID("");
		}
		return true;
	}
	/**
	 * 弹窗展示退出游戏 
	 * @param c
	 */
	public static void showExitDialog(final Context c){
		final AlertDialog d = new AlertDialog.Builder(c).create();
		d.show();
		d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_dialog_notitle"));
		TextView tv_content = (TextView) d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_content"));
		tv_content.setText(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_8"));
		TextView btn_sure = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_sure"));
		btn_sure.setText(BaseUtils.getResourceValue("string", "oasisgames_sdk_common_btn_sure"));
		btn_sure.setOnClickListener(new View.OnClickListener(){
			
			@Override
			public void onClick(View v) {
				// 执行退出操作
				d.dismiss();
		    	SystemCache.isExit = true;
		    	((Activity)c).finish();
			}
		});
		TextView btn_cancle = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_cancle"));
		btn_cancle.setText(BaseUtils.getResourceValue("string", "oasisgames_sdk_common_btn_cancle"));	
		btn_cancle.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});
	}
	/**
	 * 弹窗展示 充值禁用\登录禁用 的提示 
	 * @param c
	 */
	public static void showDisableDialog(final Context c, String text){
		final AlertDialog d = new AlertDialog.Builder(c).create();
		d.show();
		d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_dialog_notitle"));
		
		TextView tv_content = (TextView) d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_content"));
		TextView btn_sure = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_sure"));
		
		tv_content.setText(BaseUtils.getResourceValue("string", text));
		btn_sure.setText(BaseUtils.getResourceValue("string", "oasisgames_sdk_common_btn_sure"));
	
		btn_sure.setOnClickListener(new View.OnClickListener(){
			
			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});
		TextView btn_cancle = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_cancle"));
		btn_cancle.setVisibility(View.GONE);
	}
	
	/**
	 * 
	 * @param c
	 */
	public static void trackOnCreate(Activity c){
		TrackUtils.onCreate(c);
		ReportUtils.add(ReportUtils.DEFAULTEVENT_INIT, new ArrayList<String>(), new ArrayList<String>());
	}
	
	/**
	 * 
	 * @param c
	 */
	public static void trackOnStart(Activity c){
		TrackUtils.onStart(c);
	}
	/**
	 * 
	 * @param c
	 */
	public static void trackOnRestart(Activity c){
		ReportUtils.add(ReportUtils.DEFAULTEVENT_RESTORE, new ArrayList<String>(), new ArrayList<String>());
	}
	/**
	 * 
	 * @param c
	 */
	public static void trackOnResume(Activity c){
		
		TrackUtils.onResume(c);

		// 登录失败时，退出整个应用，由用户自主选择
		if(SystemCache.isExit){
			c.finish();
		}
	}
	/**
	 * 
	 * @param c
	 */
	public static void trackOnPause(Activity c){
		
		TrackUtils.onPause(c);
	}
	/**
	 * 
	 * @param c
	 */
	public static void trackOnStop(Activity c){
		TrackUtils.onStop(c);
		ReportUtils.add(ReportUtils.DEFAULTEVENT_ENTERBACK, new ArrayList<String>(), new ArrayList<String>());		
	}
	/**
	 * 
	 * @param c
	 */
	public static void trackOnDestroy(Activity c){
		TrackUtils.onDestroy(c);
		
		ReportUtils.cancelReport();// 取消上报定时器
		
	}
	
	public static void trackEvent(Activity c, String eventToken, Map<String, String> parameters){
		
		ReportUtils.add(eventToken, 0.0, "", parameters);
	}
	/**
	 * Add tracking of custom events.跟踪单个事件
	 * @param c				当前activity
	 * @param trackType		跟踪类型（）
	 * @param eventName		事件名称
	 * @param parameters	事件级参数
	 * @param status		APP级参数
	 */
	public static void trackEvent(Activity c, int trackType, String eventName, Map<String, String> parameters, Map<String, String> status){
		switch (trackType) {
		case OASISPlatformConstant.REPORT_TYPE_MDATA://Mdata
			ReportUtils.add(eventName, parameters, status);
			break;
		case OASISPlatformConstant.REPORT_TYPE_ADJUST://Adjust
			ReportUtils.add(eventName, 0.0, "",parameters);
			break;
		case OASISPlatformConstant.REPORT_TYPE_ALL://ALL
		default:
			ReportUtils.add(eventName, parameters, status);
			ReportUtils.add(eventName, 0.0, "",parameters);
			break;
		}
		
	}
	
	/**
	 * Add tracking of revenue
	 * @param c				当前activity
	 * @param amountInCents	收入的金额
	 * @param eventToken	事件token
	 * @param parameters	事件参数
	 */
	public static void trackRevenue(Activity c, String eventToken, double amountInCents, String currency, Map<String, String> parameters){
		
		if (amountInCents <= 0)
			return;
		
//		amountInCents *= 100;// 由美元装换为美分
		
		ReportUtils.add(eventToken, amountInCents, currency, parameters);
	}
	/**
	 * Handle deep linking
	 * @param c
	 * @param uri
	 */
	public static void trackDeepLink(Activity c, Uri uri){
		Adjust.appWillOpenUrl(uri);
	}
	public static UserInfo getUserInfo(){
		if(isLogin()){
			BaseUtils.cacheLog(1, "OASISPlatform.getUserInfo<br>用户信息：UID="+SystemCache.userInfo.uid+";Token="+SystemCache.userInfo.token+";ServerID="+SystemCache.userInfo.serverID+";roleID="+SystemCache.userInfo.roleID);
			return SystemCache.userInfo;			
		}
		return null;
	}
	/**
	 * 因内存不足，userinfo被回收时，重新解析存储的json，并赋值给SystemCache.userinfo
	 */
	public static void parseJsonToUserinfo(){

		String info = (String) getSettingKVPfromSysCache(Constant.SHAREDPREFERENCES_CURRENTUSERINFOS, "");
		if(TextUtils.isEmpty(info))
			return;
//		BaseUtils.logError("---", "-----parseJsonToUserinfo");
		
		HttpService.instance().parseUserInfo(info);
	}
	/**
	 * 玩家每次 更改服务器或角色，需要将信息同步给SDK
	 * @param serverid
	 * @param serverName
	 * @param username
	 * @param roleid
	 */
	public static void setUserInfo(String serverid, String serverName, String serverType, String username, String roleid){
		if (SystemCache.userInfo == null) {
			return;
		}
		if (!TextUtils.isEmpty(serverid))
			SystemCache.userInfo.serverID = serverid;
		if (!TextUtils.isEmpty(serverName))
			SystemCache.userInfo.serverName = serverName;
		if (!TextUtils.isEmpty(serverType))
			SystemCache.userInfo.serverType = serverType.toLowerCase();
		if (!TextUtils.isEmpty(username))
			SystemCache.userInfo.gameNickname = username;
		if (!TextUtils.isEmpty(roleid))
			SystemCache.userInfo.roleID = roleid;

		try {
			List<String> param = new ArrayList<String>();
			param.add("\"uid\":\"" + SystemCache.userInfo.uid + "\"");
			param.add("\"roleid\":\"" + (TextUtils.isEmpty(SystemCache.userInfo.roleID) ? "" : SystemCache.userInfo.roleID) + "\"");
			param.add("\"username\":\"" + (TextUtils.isEmpty(SystemCache.userInfo.username) ? "" : SystemCache.userInfo.username) + "\"");
			param.add("\"serverid\":\"" + (TextUtils.isEmpty(SystemCache.userInfo.serverID) ? "" : SystemCache.userInfo.serverID) + "\"");
			param.add("\"servertype\":\"" + (TextUtils.isEmpty(SystemCache.userInfo.serverType) ? "" : SystemCache.userInfo.serverType) + "\"");
			param.add("\"servername\":\"" + (TextUtils.isEmpty(SystemCache.userInfo.serverName) ? "" : SystemCache.userInfo.serverName) + "\"");

			List<String> status = new ArrayList<String>();
			status.add("\"event_type\":\"setuserinfo\"");
			ReportUtils.add(ReportUtils.DEFAULTEVENT_SETUSERINFO, param, status);

			if (SystemCache.userInfo != null && SystemCache.userInfo.roleID != null && !TextUtils.isEmpty(SystemCache.userInfo.roleID)) {
				param = new ArrayList<String>();
				param.add("\"uid\":\"" + SystemCache.userInfo.uid + "\"");
				param.add("\"roleid\":\"" + (TextUtils.isEmpty(SystemCache.userInfo.roleID) ? "" : SystemCache.userInfo.roleID) + "\"");
				param.add("\"username\":\"" + (TextUtils.isEmpty(SystemCache.userInfo.username) ? "" : SystemCache.userInfo.username) + "\"");
				param.add("\"serverid\":\"" + (TextUtils.isEmpty(SystemCache.userInfo.serverID) ? "" : SystemCache.userInfo.serverID) + "\"");
				param.add("\"servertype\":\"" + (TextUtils.isEmpty(SystemCache.userInfo.serverType) ? "" : SystemCache.userInfo.serverType) + "\"");
				param.add("\"servername\":\"" + (TextUtils.isEmpty(SystemCache.userInfo.serverName) ? "" : SystemCache.userInfo.serverName) + "\"");

				status = new ArrayList<String>();
				status.add("\"event_type\":\"sdk_setuserinfo_roleid\"");
				ReportUtils.add(ReportUtils.DEFAULTEVENT_SETUSERINFOROLEID,
						param, status);
			}
		} catch (Exception e) {
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// 为服务提供数据 2014-08-13 添加
					HttpService.instance().game_play_log();
				} catch (OasisSdkException e) {

				}

			}
		}).start();
	}
	public static void changeLanguge(Context c, Language lang){
		String languge=PhoneInfo.instance().checkLocale((Language)lang);
		Locale locale = new Locale(languge);
		
		Resources res = c.getResources();
		Configuration conf = res.getConfiguration();
		DisplayMetrics dm = res.getDisplayMetrics();
		
		conf.locale=locale;
		
		res.updateConfiguration(conf, dm);
		
		// 记录phoneinfo里，以备它用
		PhoneInfo.instance().locale = locale.getLanguage();
		PhoneInfo.instance().lang_area = locale.getCountry();
		saveSettingKVPtoSysCache(Constant.LATEST_LANGUAGE,PhoneInfo.instance().locale);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
		String endpointarn = sharedPreferences.getString(Constant.ENDPOINT_ARN, null);
		String subscribeTopic = MD5Encrypt.StringToMD5(PhoneInfo.instance().bundleversion+PhoneInfo.instance().locale);
		if (TextUtils.isEmpty(endpointarn)
				|| (!TextUtils.isEmpty(endpointarn) && !endpointarn.startsWith(subscribeTopic))) {
			if (checkPlayServices(c)) {// &&TextUtils.isEmpty(endpointarn)) {
				// Start IntentService to register this application with GCM.
				Intent intent = new Intent(c, RegistrationIntentService.class);
				c.startService(intent);
			}
		}
	}
	public static void setCurrentLangusge(Application app){
		Resources res = app.getResources();
		Configuration conf = res.getConfiguration();
		DisplayMetrics dm = res.getDisplayMetrics();
		
		conf.locale=new Locale(TextUtils.isEmpty(PhoneInfo.instance().locale)?((String)getSettingKVPfromSysCache(Constant.LATEST_LANGUAGE, "en")):PhoneInfo.instance().locale);
		
		res.updateConfiguration(conf, dm);
	}
	/**
	 * 日志打印
	 * @param logLevel 日志级别
	 * @param tag		日志tag
	 * @param msg		日志消息
	 */
	private static void printLog(int logLevel, String tag, String msg){
		if(Log.DEBUG == logLevel){
			if(SystemCache.OASISSDK_ENVIRONMENT_SANDBOX || SystemCache.OASISSDK_ENVIRONMENT_TEST)
				Log.d(tag, msg);
		}else if(Log.WARN == logLevel){
			Log.w(tag, msg);
		}else if(Log.ERROR == logLevel){
			Log.e(tag, msg);
		}
	}
	public static void logDebug(String tag, String msg){
		printLog(Log.DEBUG, tag, msg);
	}
	public static void logWarn(String tag, String msg){
		printLog(Log.WARN, tag, msg);
	}
	public static void logError(String tag, String msg){
		printLog(Log.ERROR, tag, msg);
	}
	/**
	 * 是否登录成功，通过SystemCache.userInfo 判断是否登录成功
	 * @return
	 */
	public static boolean isLogin(){
		if(SystemCache.userInfo != null && !TextUtils.isEmpty(SystemCache.userInfo.uid) && !TextUtils.isEmpty(SystemCache.userInfo.token))
			return true;
		return false;
	}
	/**
	 * 同一账号 重复登录
	 * @return
	 */
	public static boolean isReLogin(){
		if(SystemCache.userInfo.uidOld!=null 
				&& SystemCache.userInfo.uid!=null 
				&& !TextUtils.isEmpty(SystemCache.userInfo.uid) 
				&& SystemCache.userInfo.uidOld.equals(SystemCache.userInfo.uid))
			return true;
		return false;
	}
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static void cacheLog(int type, String log){
		if(isSandBox()){
			if(SystemCache.logLists == null)
				SystemCache.logLists = new ArrayList<String>();
			SystemCache.logLists.add(0, "<B>【"+sdf.format(new Date())+"】【"+(type==1?"GAME":"SDK")+"】</B>"+log+"<br>");
		}
		
		if(SystemCache.logListsSD == null)
			SystemCache.logListsSD = new ArrayList<String>();
		
		if(type == 1)// 将游戏调用的接口顺序作为日志存入SD卡，通过 ReportTimer完成写文件操作
			SystemCache.logListsSD.add("【"+sdf.format(new Date())+"】"+log+"\r\n\r\n");
	}
	public static String Bitmap2Base64String(String path) {
		if(TextUtils.isEmpty(path))
			return "";
		Bitmap bm = null;
		try {
			bm = BitmapFactory.decodeFile(path);// 处理内存溢出
		} catch (OutOfMemoryError e1) {
			bm = getSmallBitmap(path, 480, 800);
		}
		if(bm == null)
			return "";
		
		String farmat = path.substring(path.lastIndexOf(".")+1);
		CompressFormat cf = CompressFormat.JPEG;
		if("jpg".equals(farmat) || "jpeg".equals(farmat)
				|| "JPG".equals(farmat) || "JPEG".equals(farmat))
			cf = CompressFormat.JPEG;
		else if("png".equals(farmat) || "PNG".equals(farmat))
			cf = CompressFormat.PNG;
			
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			int options = 100;
			bm.compress(cf, options, baos);
			double maxSize = 1024*500; // 2015-09-09将此值更新为500K  
			
			long size = baos.toByteArray().length;
			    
			if(size > maxSize){
				double f = maxSize/size;
				int v = (int)(f*100);// 得到压缩百分比
				
				if(v>0 && v<100){
					baos.reset();
					bm.compress(cf, v, baos);
				}
			}
		} catch (Exception e) {
			if(bm != null)
				bm.recycle();
			bm = null;
			return "";
		}
		if(bm != null)
			bm.recycle();
		bm = null;
		return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
	}
	public static String Bitmap2Base64String(Bitmap bm) {
		return Base64.encodeToString(Bitmap2Bytes(bm), Base64.DEFAULT);
	}
	public static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int options = 100;
		bm.compress(CompressFormat.JPEG, options, baos);
		double maxSize = 1024*1024; // 1M  
		
		long size = baos.toByteArray().length;
	        
		if(size > maxSize){
			double f = maxSize/size;
			int v = (int)(f*100);// 得到压缩百分比
			
			if(v>0 && v<100){
				baos.reset();
				bm.compress(CompressFormat.JPEG, v, baos);
			}
		}
		
		return baos.toByteArray();
	}
	//计算图片的缩放值
	public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {
	             final int heightRatio = Math.round((float) height/ (float) reqHeight);
	             final int widthRatio = Math.round((float) width / (float) reqWidth);
	             inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }
	        return inSampleSize;
	}
	// 根据路径获得图片并压缩，返回bitmap用于显示
	public static Bitmap getSmallBitmap(String filePath, int showWidth, int showHeight) {
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        Bitmap b = null;
	        try {
				b = BitmapFactory.decodeFile(filePath, options);
			} catch (OutOfMemoryError e) {
				return null;
			}

	        // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, showWidth, showHeight);

	        // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;

	    try {
			b = BitmapFactory.decodeFile(filePath, options);
		} catch (OutOfMemoryError e) {
			return null;
		}
	    return b;
	    }
	public static boolean checkCameraDevice(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}
	  /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkPlayServices(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }

}
