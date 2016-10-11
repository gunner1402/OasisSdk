package com.oasis.sdk.pay.googleplay.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.json.JSONException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.oasis.sdk.base.entity.PhoneInfo;
import com.oasis.sdk.base.report.ReportUtils;
import com.oasis.sdk.base.utils.ApplicationContextManager;
import com.oasis.sdk.base.utils.BaseUtils;

public class GoogleBillingUtils {
//	private static final String TAG = GoogleBillingUtils.class.getName();
    public static final String SEPARATE = "oasis";// 验证字符串的分隔符，使用单个字符担心有冲突，所以自定义
    public static final String ORDER_STATUS_FINISHED = "1";
    public static final String ORDER_STATUS_UNFINISHED = "0";
	
	private static final String TABLENAME = "googleorder";
	public static final String COLUMNS_ID = "orderid";
	public static final String COLUMNS_DATA = "orderdata";
	public static final String COLUMNS_SIGN = "ordersign";
	public static final String COLUMNS_TIME = "createtime";
	public static final String COLUMNS_STATUS = "status";
	public static final String COLUMNS_EXT1 = "ext1";
	public static final String COLUMNS_EXT2 = "ext2";
	private static final String[] COLUMNS = new String[]{"orderid", "orderdata", "ordersign", "createtime", "status", "ext1", "ext2"};
	
	public static Timer GoogleBillingTimer = new Timer();
	
	public static Purchase getPurchaseByOrderid(String orderid) throws JSONException, NullPointerException{
		Cursor cur = ApplicationContextManager.getInstance().getDBHelper().loadByWhere(TABLENAME, COLUMNS, COLUMNS_ID+"=?", new String[]{orderid});
		if(cur.getCount()<=0){
			cur.close();
			return null;
		}
		int nameColumn = cur.getColumnIndex(GoogleBillingUtils.COLUMNS_DATA);
	    int phoneColumn = cur.getColumnIndex(GoogleBillingUtils.COLUMNS_SIGN);
	    String data = cur.getString(nameColumn);
	    String sign = cur.getString(phoneColumn);
	    Purchase p = new Purchase("inapp", data, sign);
	    cur.close();
	    return p;
	}
	/**
	 * 根据状态，获取支付成功但未处理的订单
	 * @return
	 * @throws JSONException
	 */
	public static List<Purchase> getPurchasedListByStatus(String status) throws JSONException{
		List<Purchase> list = new ArrayList<Purchase>();
		Cursor cur = ApplicationContextManager.getInstance().getDBHelper().loadByWhere(TABLENAME, COLUMNS, "status=?", new String[]{status});
		if(cur.getCount()<=0){
			cur.close();
			return list;
		}
		for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext())
		{
			int nameColumn = cur.getColumnIndex(GoogleBillingUtils.COLUMNS_DATA);
			int phoneColumn = cur.getColumnIndex(GoogleBillingUtils.COLUMNS_SIGN);
			String data = cur.getString(nameColumn);
			String sign = cur.getString(phoneColumn);
			Purchase p = new Purchase("inapp", data, sign);
			list.add(p);
		}
		cur.close();
		return list;
	}
	/**
	 * 获取所有支付成功但未处理的订单
	 * @return
	 * @throws JSONException
	 */
	public static List<Purchase> getPurchasedList() throws JSONException{
		List<Purchase> list = new ArrayList<Purchase>();
		Cursor cur = ApplicationContextManager.getInstance().getDBHelper().loadByWhere(TABLENAME, COLUMNS, "", null);
		if(cur.getCount()<=0){
			cur.close();
			return list;
		}
		for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext())
		{
		    int nameColumn = cur.getColumnIndex(GoogleBillingUtils.COLUMNS_DATA);
		    int phoneColumn = cur.getColumnIndex(GoogleBillingUtils.COLUMNS_SIGN);
		    String data = cur.getString(nameColumn);
		    String sign = cur.getString(phoneColumn);
		    Purchase p = new Purchase("inapp", data, sign);
		    list.add(p);
		}
		cur.close();
		return list;
	}
	/**
	 * 检查当前订单是否已入库
	 * @param p	订单信息
	 * @return 订单存在 true
	 */
	public static boolean checkPurchaseIsExist(Purchase p){
		Cursor cur = ApplicationContextManager.getInstance().getDBHelper().loadByWhere(TABLENAME, new String[]{COLUMNS_ID}, COLUMNS_ID+"=?", new String[]{p.mOrderId});
		if(cur.getCount()<=0){
			cur.close();
			return false;
		}else{
			int index = cur.getColumnIndex(GoogleBillingUtils.COLUMNS_ID);
			String orderid = cur.getString(index);
			if(!TextUtils.isEmpty(orderid) && orderid.equals(p.mOrderId)){
				cur.close();
				return true;
			}
			cur.close();
			return false;
		}
	}
	/**
	 * 添加支付成功但未处理的订单
	 * @return
	 * @throws JSONException
	 */
	public static long addPurchase(Purchase p) {
		ContentValues c = new ContentValues();
		c.put(COLUMNS_ID, p.mOrderId);
		c.put(COLUMNS_SIGN, p.mSignature);
		c.put(COLUMNS_DATA, p.mOriginalJson);
		c.put(COLUMNS_TIME, System.nanoTime());
		c.put(COLUMNS_STATUS, GoogleBillingUtils.ORDER_STATUS_UNFINISHED);
		c.put(COLUMNS_EXT1, "");
		c.put(COLUMNS_EXT2, "");
		return ApplicationContextManager.getInstance().getDBHelper().insert(TABLENAME, c);
	}
	public static boolean updatePurchase(Purchase p ){
		ContentValues c = new ContentValues();
		c.put(COLUMNS_STATUS, GoogleBillingUtils.ORDER_STATUS_FINISHED);
		
		return ApplicationContextManager.getInstance().getDBHelper().update(TABLENAME, c, COLUMNS_ID +"=?", new String[]{p.getOrderId()});
	}
	/**
	 * 删除支付成功但未处理的订单
	 * @return
	 * @throws JSONException
	 */
	public static int deletePurchase(String orderid) {
		
		return ApplicationContextManager.getInstance().getDBHelper().delete(TABLENAME, COLUMNS_ID+"=?", new String[]{orderid});
	}
	
	public static void getIdThread(final Context c) {
		 new Thread(new Runnable() {
			
			@Override
			public void run() {
				Info adInfo = null;
				  try {
				    adInfo = AdvertisingIdClient.getAdvertisingIdInfo(c);
				 
				  } catch (IOException e) {
				    // Unrecoverable error connecting to Google Play services (e.g.,
				    // the old version of the service doesn't support getting AdvertisingId).
				  
				  } catch (GooglePlayServicesNotAvailableException e) {
				    // Google Play services is not available entirely.
				  } catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (GooglePlayServicesRepairableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(adInfo != null){
					String id = adInfo.getId();
					if(!TextUtils.isEmpty(id))
						PhoneInfo.instance().setAdid(id);
//					final boolean isLAT = adInfo.isLimitAdTrackingEnabled();
					
				}
				
			}
		}).start();
		  
	}

}
