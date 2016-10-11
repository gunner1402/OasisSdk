package com.oasis.sdk.base.entity;

import java.net.URLEncoder;

import android.text.TextUtils;

import com.oasis.sdk.OASISPlatformConstant.Language;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.Constant;
import com.oasis.sdk.base.utils.SystemCache;

public class PhoneInfo {
	 
	private final static PhoneInfo PHONEINFO = new PhoneInfo();

	private PhoneInfo() {
	}

	/**
	 * @return 返回逻辑的实例.
	 */
	public static PhoneInfo instance() {

		return PHONEINFO;
	}
	/**  
	   * 唯一的设备ID：   
	   * GSM手机的 IMEI 和 CDMA手机的 MEID.    
	   * Return null if device ID is not available.   
	   */   
	public String deviceId;
	/**
	 * 手机系统类型
	 */
	public String softwareType;
	/**  
	   * 设备的软件版本号：   
	   * 例如：the IMEI/SV(software version) for GSM phones.   
	   * Return null if the software version is not available.    
	   */  
	public String softwareVersion;
	/**   
	   * 手机号：   
	   * GSM手机的 MSISDN.   
	   * Return null if it is unavailable.    
	   */  
	public String line1Number;
	public String line2Number;
	
	 /**
	   * 当前使用的网络类型：   
	   * 例如：
	   * NETWORK_TYPE_UNKNOWN  网络类型未知  0   
	     NETWORK_TYPE_GPRS     GPRS网络  1   
	     NETWORK_TYPE_EDGE     EDGE网络  2   
	     NETWORK_TYPE_UMTS     UMTS网络  3     
	     NETWORK_TYPE_CDMA     CDMA网络,IS95A 或 IS95B.  4   
	     NETWORK_TYPE_EVDO_0   EVDO网络, revision 0.  5   
	     NETWORK_TYPE_EVDO_A   EVDO网络, revision A.  6   
	     NETWORK_TYPE_1xRTT    1xRTT网络  7
	     NETWORK_TYPE_HSDPA    HSDPA网络  8    
	     NETWORK_TYPE_HSUPA    HSUPA网络  9   
	     NETWORK_TYPE_HSPA     HSPA网络  10    
	     NETWORK_TYPE_IDEN     iDen网络  11    
	     NETWORK_TYPE_EVDO_B   EVDO revision B网络  12    
	     NETWORK_TYPE_LTE      LTE网络  13    
	     NETWORK_TYPE_EHRPD    eHRPD网络  14    
	     NETWORK_TYPE_HSPAP    HSPA+网络  15    
	     NETWORK_TYPE_GSM      HSPA网络  16   
	   */    
	public String networkType;//int  
	
	/**  
	   * 唯一的用户ID：   
	   * 例如：IMSI(国际移动用户识别码) for a GSM phone.   
	   * 需要权限：READ_PHONE_STATE   
	   */    
	public String subscriberId;
	
	/**
	 * 手机型号
	 */
	public String model;
	
	/**
	 * 手机品牌
	 */
	public String brand;
	/**
	 * 2位国家代码(来自sim卡)
	 */
	public String iso2Country;
//	model = android.os.Build.MODEL;   // 手机型号
//	sdk=android.os.Build.VERSION.SDK;    // SDK号
//	release=android.os.Build.VERSION.RELEASE;  // android系统版本号

	/**
	 * 包名
	 */
	public String bundleid;
	/**
	 * 版本号  2.4.5
	 */
	public String bundleversion;
	/**
	 * 版本号
	 */
	public String bundleversioncode;
	
	/**
	 * 当前用户是否参与跟踪      true:跟踪     false:不跟踪
	 */
	public boolean isTrackAble;
	
	/**
	 * 参数配置文件 signkey， 登录时提交给server
	 */
	public String signKey;
	
	/**
	 * 当前使用语言 指定的地区
	 */
	public String lang_area;
	/**
	 * 设备中的Google账号
	 */
	public String googleAccount;
	

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public void setSoftwareType(String softwareType) {
		this.softwareType = softwareType;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public void setLine1Number(String line1Number) {
		this.line1Number = line1Number;
	}

	public void setLine2Number(String line2Number) {
		this.line2Number = line2Number;
	}

	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}

	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public void setIso2Country(String iso2Country) {
		this.iso2Country = iso2Country;
	}

	public String toString(){
		return toString(true);
	}
	public String toString(boolean isNeedUser){
		StringBuffer sb = new StringBuffer();
		sb.append("&game_code="+SystemCache.GAMECODE);
		sb.append("&mobile_code="+BaseUtils.getMobileCode());
		if(isNeedUser && SystemCache.userInfo != null && !TextUtils.isEmpty(SystemCache.userInfo.uid) && !"null".equals(SystemCache.userInfo.uid))
			sb.append("&uid="+SystemCache.userInfo.uid);
		sb.append("&phone="+ (TextUtils.isEmpty(line1Number)?TextUtils.isEmpty(line2Number)?"":line2Number:line1Number));
		sb.append("&phonebrand="+brand);
		try {
			sb.append("&phonemodel="+URLEncoder.encode(model, "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		sb.append("&ostype="+softwareType);
		sb.append("&osversion="+softwareVersion);
		sb.append("&bundleid="+bundleid);// 包名
		sb.append("&bundleversion="+bundleversion);// 2.4.5
		sb.append("&bundleversioncode="+bundleversioncode);// versioncode
		sb.append("&sdkversion="+Constant.SDKVERSION);// versioncode
		sb.append("&isreport="+(isTrackAble()?"Y":"N"));// 是否跟踪的状态
		sb.append("&signkey="+signKey);// 配置文件signkey
		sb.append("&androidid="+androidID_normal);// android id
		try {
			sb.append("&referrer="+URLEncoder.encode(referrer, "UTF-8"));// 推广渠道信息
		} catch (Exception e) {
//			e.printStackTrace();
			sb.append("&referrer=");
		}
		sb.append("&adid="+adid);// 广告id
		sb.append("&lang="+getLocale());// 游戏当前使用语言
		
		return sb.toString();
	}
	
	public String channel;// 渠道
	public String mdataAppID;// mData App id
	public String androidID;// android id（通过RC4加密）
	public String androidID_normal;// android id
	public String adid;// Google 广告id
	public String ipToCountry;//根据ip获取地区
	public String event;// 事件
	public String locale;// 语言代码
	public String browser;
	public String screen;// 屏幕分辨率
	public String density;// 设备分辨率
	public String referrer;// 推广渠道信息

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public void setMdataAppID(String mdataAppID) {
		this.mdataAppID = mdataAppID;
	}

	public void setAndroidID(String androidID) {
		this.androidID = androidID;
	}

	public void setAndroidID_normal(String androidID_normal) {
		this.androidID_normal = androidID_normal;
	}

	public void setAdid(String adid) {
		this.adid = adid;
	}

	public String getIpToCountry() {
		if(TextUtils.isEmpty(ipToCountry))
			return "";
		return ipToCountry;
	}
	public String getIpToCountryWithHttp() {
		if(TextUtils.isEmpty(ipToCountry))
			return "";
		return ipToCountry.toLowerCase()+".";
	}

	public void setIpToCountry(String ipToCountry) {
		this.ipToCountry = ipToCountry;
	}

	public void setEvent(String event) {
		this.event = event;
	}
	public String checkLocale(Language lang){
//		Locale locale = Locale.getDefault();
		String curLang = "en";
		switch (lang) {
		case KO:// 韩语
			curLang = "ko";
//			locale = Locale.KOREA;
			break;
		case FR:// 法语
			curLang = "fr";
//			locale = Locale.FRANCE;
			break;
		case ZH:// 中文
			curLang = "zh";
//			locale = Locale.SIMPLIFIED_CHINESE;
			break;
		case IT:// 意大利语
			curLang = "it";
//			locale = Locale.ITALY;
			break;
		case RU:// 俄语
			curLang = "ru";
//			locale = new Locale(curLang, "RU");
			break;
		case EN:// 英语（默认）
			curLang = "en";
//			locale = Locale.ENGLISH;
			break;
		case EL:// 希腊语
			curLang = "el";
			break;
		case DE:// 德语
			curLang = "de";
//			locale = Locale.GERMANY;
			break;
		case SV:// 瑞典语
			curLang = "sv";
//			locale = new Locale(curLang);
			break;
		case NL:// 荷兰语
			curLang = "nl";
//			locale = new Locale(curLang);
			break;
		case PL:// 波兰语
			curLang = "pl";
//			locale = new Locale(curLang);
			break;
		case ES:// 西班牙语
			curLang = "es";
//			locale = new Locale(curLang);
			break;
		case TR:// 土耳其语
			curLang = "tr";
//			locale = new Locale(curLang);
			break;
		case PT:// 葡语
			curLang = "pt";
//			locale = new Locale(curLang);
			break;

		default:// 英语（默认）
			curLang = "en";
//			locale = Locale.ENGLISH;
			break;
		}
		
		return curLang;
	}
	public String getLocale() {
		if("zh".equals(locale) && "TW".equals(lang_area)){
			return "tw";
		}
		return locale;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}

	public void setScreen(String screen) {
		this.screen = screen;
	}

	public void setDensity(String density) {
		this.density = density;
	}

	public void setBundleid(String bundleid) {
		this.bundleid = bundleid;
	}

	public void setBundleversion(String bundleversion) {
		this.bundleversion = bundleversion;
	}

	public void setBundleversioncode(String bundleversioncode) {
		this.bundleversioncode = bundleversioncode;
	}

	public boolean isTrackAble() {
//		return isTrackAble;
		return true;// 默认为true ， 即跟踪
	}

	public void setTrackAble(boolean isTrackAble) {
		this.isTrackAble = isTrackAble;
	}

	public void setSignKey(String signKey) {
		this.signKey = signKey;
	}

	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}

	
}
