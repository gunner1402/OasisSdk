/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oasis.sdk.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.oasis.sdk.OASISPlatformConstant;
import com.oasis.sdk.OasisCallback;
import com.oasis.sdk.base.Exception.OasisSdkException;
import com.oasis.sdk.base.entity.PayInfoDetail;
import com.oasis.sdk.base.entity.PayInfoList;
import com.oasis.sdk.base.entity.PhoneInfo;
import com.oasis.sdk.base.entity.ReportAdjustInfo;
import com.oasis.sdk.base.report.ReportUtils;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.MD5Encrypt;
import com.oasis.sdk.base.utils.SystemCache;
import com.oasis.sdk.pay.googleplay.utils.GoogleBillingUtils;
import com.oasis.sdk.pay.googleplay.utils.IabHelper;
import com.oasis.sdk.pay.googleplay.utils.IabHelper.QueryInventoryFinishedListener;
import com.oasis.sdk.pay.googleplay.utils.IabResult;
import com.oasis.sdk.pay.googleplay.utils.Inventory;
import com.oasis.sdk.pay.googleplay.utils.Purchase;
import com.oasis.sdk.pay.googleplay.utils.SkuDetails;


/**
 * Example game using in-app billing version 3.
 *
 * Before attempting to run this sample, please read the README file. It
 * contains important information on how to set up this project.
 *
 * All the game-specific logic is implemented here in MainActivity, while the
 * general-purpose boilerplate that can be reused in any app is provided in the
 * classes in the util/ subdirectory. When implementing your own application,
 * you can copy over util/*.java to make use of those utility classes.
 *
 * This game is a simple "driving" game where the player can buy gas
 * and drive. The car has a tank which stores gas. When the player purchases
 * gas, the tank fills up (1/4 tank at a time). When the player drives, the gas
 * in the tank diminishes (also 1/4 tank at a time).
 *
 * The user can also purchase a "premium upgrade" that gives them a red car
 * instead of the standard blue one (exciting!).
 *
 * The user can also purchase a subscription ("infinite gas") that allows them
 * to drive without using up any gas while that subscription is active.
 *
 * It's important to note the consumption mechanics for each item.
 *
 * PREMIUM: the item is purchased and NEVER consumed. So, after the original
 * purchase, the player will always own that item. The application knows to
 * display the red car instead of the blue one because it queries whether
 * the premium "item" is owned or not.
 *
 * INFINITE GAS: this is a subscription, and subscriptions can't be consumed.
 *
 * GAS: when gas is purchased, the "gas" item is then owned. We consume it
 * when we apply that item's effects to our app's world, which to us means
 * filling up 1/4 of the tank. This happens immediately after purchase!
 * It's at this point (and not when the user drives) that the "gas"
 * item is CONSUMED. Consumption should always happen when your game
 * world was safely updated to apply the effect of the purchase. So,
 * in an example scenario:
 *
 * BEFORE:      tank at 1/2
 * ON PURCHASE: tank at 1/2, "gas" item is owned
 * IMMEDIATELY: "gas" is consumed, tank goes to 3/4
 * AFTER:       tank at 3/4, "gas" item NOT owned any more
 *
 * Another important point to notice is that it may so happen that
 * the application crashed (or anything else happened) after the user
 * purchased the "gas" item, but before it was consumed. That's why,
 * on startup, we check if we own the "gas" item, and, if so,
 * we have to apply its effects to our world and consume it. This
 * is also very important!
 *
 * @author Bruno Oliveira (Google)
 */
public class GooglePlayBillingActivity extends OasisSdkBaseActivity {
    // Debug tag, for logging
    static final String TAG = "GooglePlayBilling";

    private String base64EncodedPublicKey = "";
   
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    // 验证google play 是否可用
    static final int RC_VERIFYGOOGLEPLAY  = 10002;
   
    // The helper object
    IabHelper mHelper;
    
    String productID;
    String revenue;
    String oasOrderid = "";
    String ext="";// 游戏需要透传的扩展参数
    MyHandler myHandler ;
	List<Purchase> oldOrderList = null;//未完成订单
	List<String> handedOrderIDS = new ArrayList<String>();//已处理过得未完成订单号

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_pay_google"));
        
        myHandler = new MyHandler(this);

        /* base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
         * (that you got from the Google Play developer console). This is not your
         * developer public key, it's the *app-specific* public key.
         *
         * Instead of just storing the entire literal string here embedded in the
         * program,  construct the key at runtime from pieces or
         * use bit manipulation (for example, XOR with some other string) to hide
         * the actual key.  The key itself is not secret information, but we don't
         * want to make it easy for an attacker to replace the public key with one
         * of their own and then fake messages from the server.
         */
//        String base64EncodedPublicKey = "CONSTRUCT_YOUR_KEY_AND_PLACE_IT_HERE";
        
        if(TextUtils.isEmpty(base64EncodedPublicKey)){
        	ApplicationInfo appInfo;
			try {
				appInfo = getPackageManager().getApplicationInfo(getPackageName(),PackageManager.GET_META_DATA);
				base64EncodedPublicKey = appInfo.metaData.getString("com.googleplay.ApplicationId");
			} catch (NameNotFoundException e) {
				BaseUtils.logDebug(TAG, "Please put your app's public key in AndroidManifest.xml.");
			}
        }
        if(TextUtils.isEmpty(base64EncodedPublicKey)){
        	BaseUtils.logError(TAG, "Please put your app's public key in AndroidManifest.xml.");
        	complain("Please put your app's public key in AndroidManifest.xml.");
        	return;
        }

        productID = getIntent().getStringExtra("inAppProductID");
        ext = getIntent().getStringExtra("ext");
        oasOrderid = getIntent().getStringExtra("oasOrderid");
        if(TextUtils.isEmpty(productID)){
        	BaseUtils.logError(TAG, "Please put product id.");
        	complain("Please put product id.");
        	return;
        }
        
        if(SystemCache.userInfo == null || TextUtils.isEmpty(SystemCache.userInfo.serverID) || TextUtils.isEmpty(SystemCache.userInfo.roleID)){
        	BaseUtils.logError(TAG, "Please put game serverid or roleid.");
        	complain("Please put game server id.");
        	return;
        }

//        revenue = getRevenueAndCurrency();// 根据productid获取该套餐的金额及货币
//        if(TextUtils.isEmpty(revenue)){
//        	BaseUtils.logError(TAG, "This product id does not exist, please contact customer support");
//        	setResult(OASISPlatformConstant.RESULT_EXCEPTION);
//        	Message msg = new Message();
//        	msg.what = 0;
//        	msg.obj = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_google_notice_2")); 
//            myHandler.sendMessage(msg);
//        	return;
//        }
        setWaitScreen(true);

        if(SystemCache.OASISSDK_ENVIRONMENT_SANDBOX){
        	initSandBox();
        	//1 弹窗选择 支付完成或支付失败 两种模式
        	//2 失败时，提示“充值失败”，并退出支付
        	//3 成功时，开始发钻请求。
        	return;
        }
        // Create the helper, passing it our context and the public key to verify signatures with
        BaseUtils.logDebug(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this.getApplicationContext(), base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(SystemCache.OASISSDK_ENVIRONMENT_SANDBOX);

    	int isGoolgePlayAvail = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getApplicationContext());
        if(isGoolgePlayAvail == ConnectionResult.SUCCESS){
        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        BaseUtils.logDebug(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
            	if (isPageClose()) {
            		isPageCloseHandler();
            		return;
				}
                BaseUtils.logDebug(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                	BaseUtils.logError(TAG, "Problem setting up in-app billing: " + IabHelper.getResponseDesc(result.getResponse()));
                	Message msg = new Message();
                	msg.what = 0;
                	msg.obj = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_google_notice_1")); 
                    myHandler.sendMessage(msg);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // IAB is fully set up. Now, Start purchase.
                try {
					oldOrderList = GoogleBillingUtils.getPurchasedList();
				} catch (JSONException e) {
					e.printStackTrace();
				}
//                queryInventory();
                checkALLOrder(0);
            }
        });
        }else{

        	Dialog d = GooglePlayServicesUtil.getErrorDialog(isGoolgePlayAvail, this, RC_VERIFYGOOGLEPLAY);
        	d.setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface arg0) {
					BaseUtils.logError(TAG, "GooglePlayServicesUtil.showErrorDialogFragment");
					arg0.dismiss();
					
					myHandler.sendEmptyMessageDelayed(-1, 500);
				}
			});
        	d.show();

        }
    }
    /**
     * 检查所有未完成订单
     */
    private void checkALLOrder(final int index){
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				if(oldOrderList == null || oldOrderList.size() <= 0
						|| index >= oldOrderList.size()){
					myHandler.sendEmptyMessage(101);//开始新支付流程
					return;
				}
				
				Purchase purchase = oldOrderList.get(index);
				if(handedOrderIDS.contains(purchase.getOrderId())){// 如果包含该orderid，表示本次已处理过该订单
					Message msg = new Message();
					msg.what = 98;
					msg.arg1 = index;
					myHandler.sendMessage(msg);//准备检测以一个旧订单
					return;
				}else
					handedOrderIDS.add(purchase.getOrderId());

				Message msg = null;
				
				msg = new Message();
	            msg.what = -2;
	            msg.obj = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_order_notice_old"));// 旧订单处理提醒
	            myHandler.sendMessage(msg);
				int res = -1;
				try {
					res = HttpService.instance().checkPurchaseForGoogle(purchase, GoogleBillingUtils.SEPARATE);
					
					msg = new Message();
					switch (res) {
					case 1000000://交易成功且发钻成功
					case 1000002://该购买交易已发钻成功，因客户端未消费成功，所以重复2次验证
					case 1000006://支付完成，Google验证未通过。（如无效订单等）
					case 1000001://sign验证错误，只限开发阶段错误，所以消费掉该订单。
						msg.what = 99;
						msg.obj = purchase;
						msg.arg1 = res;
						myHandler.sendMessage(msg);
						break;
						
					default:
						msg.what = 98;
						msg.arg1 = index;
						myHandler.sendMessage(msg);//准备检测以一个旧订单
						break;
					}
					
				} catch (OasisSdkException e) {// 异常处理，继续循环下一条
					msg = new Message();
					msg.what = 98;
					msg.arg1 = index;
					myHandler.sendMessage(msg);//准备检测以一个旧订单
				}finally{
					try {
						String[] info = purchase.getDeveloperPayload().split(GoogleBillingUtils.SEPARATE);

						// 向Mdata发送数据
						List<String> parameters = new ArrayList<String>();
						parameters.add("\"uid\":\""+info[0]+"\"");
						parameters.add("\"roleid\":\""+info[2]+"\"");
						parameters.add("\"serverid\":\""+info[1]+"\"");
						if(info.length >= 6 && ("android".equalsIgnoreCase(info[5]) || "all".equalsIgnoreCase(info[5]) || "test".equalsIgnoreCase(info[5]) ))
							parameters.add("\"servertype\":\""+info[5]+"\"");
						else
							parameters.add("\"servertype\":\"\"");
						parameters.add("\"product_id\":\""+purchase.getSku()+"\"");
						parameters.add("\"payment_channal\":\"mob_google\"");
						String re = info[4];
						if(re != null && !TextUtils.isEmpty(re)){
							String[] rev = re.split("_");
							parameters.add("\"cost\":\""+(rev.length>0?rev[0]:"")+"\"");
							parameters.add("\"currency\":\""+(rev.length>1?rev[1]:"")+"\"");
						}else{
							parameters.add("\"cost\":\"\"");
							parameters.add("\"currency\":\"\"");							
						}
						parameters.add("\"value\":\"\"");
						if(info.length >= 7)
							parameters.add("\"oas_order_id\":\""+info[6]+"\"");
						else
							parameters.add("\"oas_order_id\":\"\"");
						parameters.add("\"third_party_orderid\":\""+purchase.getOrderId()+"\"");
						parameters.add("\"result_code\":\""+res+"\"");
						parameters.add("\"isreport\":\""+(PhoneInfo.instance().isTrackAble()?"Y":"N")+"\"");
						
						List<String> status = new ArrayList<String>();
						status.add("\"event_type\":\"order\"");
						ReportUtils.add(ReportUtils.DEFAULTEVENT_ORDER_REPORT_OLD_LOCAL, parameters, status);
					} catch (Exception e) {
						BaseUtils.logError(TAG, "Google play billing send mdata fail.");
					}
				}//end try
			}//end run
		}).start();
    }
    private void queryInventory(){
    	if (isPageClose()) {
    		isPageCloseHandler();
    		return;
		}
    	List<String> moreSkus = new ArrayList<String>();
        moreSkus.add(productID);
        mHelper.queryInventoryAsync(true, moreSkus, new QueryInventoryFinishedListener(){

			@Override
			public void onQueryInventoryFinished(IabResult result, Inventory inv) {
				if (isPageClose()) {
            		isPageCloseHandler();
            		return;
				}
				// Is it a failure?
	            if (result.isFailure() || inv == null) {
	            	BaseUtils.logError(TAG, "Failed to query inventory: " + IabHelper.getResponseDesc(result.getResponse()));
	            	Message msg = new Message();
                	msg.what = 0;
                	msg.obj = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_google_notice_2")); 
                    myHandler.sendMessage(msg);
	                return;
	            }

	            SkuDetails sku = inv.getSkuDetails(productID);
	            if(sku == null || TextUtils.isEmpty(sku.getPrice())){
	            	BaseUtils.logError(TAG, "Don't find SkuDetails by " + productID);
	            	Message msg = new Message();
                	msg.what = 0;
                	msg.obj = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_google_notice_2")); 
                    myHandler.sendMessage(msg);
                    return;
	            }
	            
	            final Purchase p = inv.getPurchase(productID);
	            if(p == null || (p != null
						&& p.getPurchaseState() != IabHelper.BILLING_RESPONSE_RESULT_OK 
						&& p.getPurchaseState() != IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED)){

	            	if(p != null && p.getPurchaseState() == IabHelper.BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED){
	 	            	inv.erasePurchase(productID);
	            	}
	            	BaseUtils.logDebug(TAG, "Old purchase is null. Start purchase.");
	            	
	            	startPurchase();
                    return;
	            }
	            //提示正在处理旧订单
	            Message msg = new Message();
	            msg.what = -2;
	            msg.obj = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_order_notice_old"));
	            myHandler.sendMessage(msg);
				new Thread(new Runnable() {

					@Override
					public void run() {
					
						BaseUtils.logDebug(TAG, "Old purchase info:"+p.toString());
						int res = -1;
						try {
							res = HttpService.instance().checkPurchaseForGoogle(p, GoogleBillingUtils.SEPARATE);
						} catch (OasisSdkException e) {
							
						}finally{
							try {
								String[] info = p.getDeveloperPayload().split(GoogleBillingUtils.SEPARATE);

								// 向Mdata发送数据
								List<String> parameters = new ArrayList<String>();
								parameters.add("\"uid\":\""+info[0]+"\"");
								parameters.add("\"roleid\":\""+info[2]+"\"");
								parameters.add("\"serverid\":\""+info[1]+"\"");
								if(info.length >= 6 && ("android".equalsIgnoreCase(info[5]) || "all".equalsIgnoreCase(info[5]) || "test".equalsIgnoreCase(info[5]) ))
									parameters.add("\"servertype\":\""+info[5]+"\"");
								else
									parameters.add("\"servertype\":\"\"");
								parameters.add("\"product_id\":\""+p.getSku()+"\"");
								parameters.add("\"payment_channal\":\"mob_google\"");
								String re = info[4];
								if(re != null && !TextUtils.isEmpty(re)){
									String[] rev = re.split("_");
									parameters.add("\"cost\":\""+(rev.length>0?rev[0]:"")+"\"");
									parameters.add("\"currency\":\""+(rev.length>1?rev[1]:"")+"\"");
								}else{
									parameters.add("\"cost\":\"\"");
									parameters.add("\"currency\":\"\"");							
								}
								parameters.add("\"value\":\"\"");
								if(info.length >= 7)
									parameters.add("\"oas_order_id\":\""+info[6]+"\"");
								else
									parameters.add("\"oas_order_id\":\"\"");
								parameters.add("\"third_party_orderid\":\""+p.getOrderId()+"\"");
								parameters.add("\"result_code\":\""+res+"\"");
								parameters.add("\"isreport\":\""+(PhoneInfo.instance().isTrackAble()?"Y":"N")+"\"");
								
								List<String> status = new ArrayList<String>();
								status.add("\"event_type\":\"order\"");
								ReportUtils.add(ReportUtils.DEFAULTEVENT_ORDER_REPORT_OLD_GOOGLE, parameters, status);
							} catch (Exception e) {
								BaseUtils.logError(TAG, "Google play billing send mdata fail.");
							}
							
							BaseUtils.logDebug(TAG, "Old purchase handle.purchase.orderID=" + p.getOrderId()+", Code="+res);
							if(isPageClose()){
								isPageCloseHandler();
								return;
							}
							Message msg = new Message();
							if (res == 1000000
									|| res == 1000002
									|| res == 1000006) {
								msg.what = 100;
								msg.obj = p;
								msg.arg1 = -1;
								myHandler.sendMessage(msg);
								
							}else{
								BaseUtils.logError(TAG, "旧订单再次请求发钻失败。GoogleOrderid="+p.getOrderId()+", Result Code="+res+"");
								//补钻发生异常，提示“支付失败”，退出支付
								msg.what = 0;
	                        	msg.obj = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_error_fail")); 
	                            myHandler.sendMessage(msg);
							}
						}
					
					}
				}).start();

			}
		});
    }
    /**
     * 开始新的支付
     */
    private void startPurchase(){
    	runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if(TextUtils.isEmpty(oasOrderid)){// 没有OAS orderid 时，立即请求
		    		if(!TextUtils.isEmpty(ext)){
		    			sendOrder();
		    			return;
		    		}
		    		if (SystemCache.oasisInterface != null) {
		    			SystemCache.oasisInterface
		    					.getExtendValue(new MyOasisCallbackForExt());
		    		} else {
		    			BaseUtils
		    					.logError(TAG,
		    							"请先调用OASISPlatform。setOASISPlatformInterfaceImpl()完成接口的初始化");
		    			
		    			myHandler.sendEmptyMessage(HANDLER_FAIL);
		    		}
		    	}else
		    		launchPurchaseFlow();
			}
		});
    	
    }
    class MyOasisCallbackForExt implements OasisCallback{

		@Override
		public void success(final String result) {
			ext = result;
			sendOrder();// 成功获取ext后，向支付平台下单
		}

		@Override
		public void error(String result) {
			BaseUtils.logError(TAG, "获取ext失败，请游戏方研发检查接口.\n返回结果："+result);
			myHandler.sendEmptyMessage(HANDLER_FAIL);
		}
		
	}
    private void sendOrder(){
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(SystemCache.payInfoLists == null || SystemCache.payInfoLists.isEmpty()){
					try {// 如果直接从商城进入支付，可能套餐信息为空，再次从新获取一次
						HttpService.instance().getPayKindsInfo(null);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				// 获取ext成功，向支付平台下单
				try {
					int index = productID.lastIndexOf("_");
					String id = productID.substring(index+1);
					oasOrderid = HttpService.instance().sendOrder(id, (TextUtils.isEmpty(ext)?"":ext));
					if(TextUtils.isEmpty(oasOrderid)){
						BaseUtils.logError(TAG, "支付平台下单失败：productID="+productID+", serverID="+SystemCache.userInfo.serverID+", roleID="+SystemCache.userInfo.roleID+", ext="+ext);
						myHandler.sendEmptyMessage(HANDLER_FAIL);
						return;
					}
					try {
						List<String> parameters = new ArrayList<String>();
						parameters.add("\"uid\":\""+SystemCache.userInfo.uid+"\"");
						parameters.add("\"roleid\":\""+SystemCache.userInfo.roleID+"\"");
						parameters.add("\"serverid\":\""+SystemCache.userInfo.serverID+"\"");
						parameters.add("\"servertype\":\""+SystemCache.userInfo.serverType+"\"");
						parameters.add("\"product_id\":\""+productID+"\"");
						parameters.add("\"payment_channal\":\"google\"");
						if(revenue != null && !TextUtils.isEmpty(revenue)){
							String[] re = revenue.split("_");
							parameters.add("\"cost\":\""+(re.length>0?re[0]:"")+"\"");
							parameters.add("\"currency\":\""+(re.length>1?re[1]:"")+"\"");
						}else{
							parameters.add("\"cost\":\"\"");
							parameters.add("\"currency\":\"\"");
						}
						parameters.add("\"value\":\"\"");
						parameters.add("\"oas_order_id\":\""+oasOrderid+"\"");
						parameters.add("\"third_party_orderid\":\"\"");
						parameters.add("\"result_code\":\"ok\"");
						parameters.add("\"isreport\":\""+(PhoneInfo.instance().isTrackAble()?"Y":"N")+"\"");
						
						List<String> status = new ArrayList<String>();
						status.add("\"event_type\":\"order\"");
						ReportUtils.add(ReportUtils.DEFAULTEVENT_ORDER, parameters, status);
					} catch (Exception e) {
						Log.e(TAG, ReportUtils.DEFAULTEVENT_ORDER + "-> send mdata fail.");
					}
					startPurchase();
				} catch (OasisSdkException e) {
					BaseUtils.logError(TAG, "支付平台下单失败：productID="+productID+", serverID="+SystemCache.userInfo.serverID+", roleID="+SystemCache.userInfo.roleID+", ext="+ext);
					myHandler.sendEmptyMessage(HANDLER_FAIL);
				}
			}
		}).start();
    }
    private String getRevenueAndCurrency(){
    	String revenue = "";
    	if(SystemCache.payInfoLists != null)
        for (PayInfoList payInfos : SystemCache.payInfoLists) {
			if("mob_google".equals(payInfos.pay_way)){
				for (PayInfoDetail detail : payInfos.list) {
					if(productID.equals(detail.price_product_id)){
						revenue = detail.amount + "_" + detail.currency;
						break;
					}
				}
				break;
			}
		}
        return revenue;
    }
    // 向google提交支付请求
    private void launchPurchaseFlow() {
    	if (isPageClose()) {
    		isPageCloseHandler();
    		return;
		}
        setWaitScreen(true);

        // V3.3之前，只有收入，货币默认为USD；
        // V3.3之后，为收入_货币 ，货币实时从支付套餐获取，这种形式便于Adjust上报及统计
        if(revenue == null || TextUtils.isEmpty(revenue))
        	revenue = getRevenueAndCurrency();
        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
       
        String payload = SystemCache.userInfo.uid+GoogleBillingUtils.SEPARATE+SystemCache.userInfo.serverID+GoogleBillingUtils.SEPARATE+SystemCache.userInfo.roleID+GoogleBillingUtils.SEPARATE+ext+GoogleBillingUtils.SEPARATE+revenue+GoogleBillingUtils.SEPARATE+SystemCache.userInfo.serverType+GoogleBillingUtils.SEPARATE+oasOrderid+GoogleBillingUtils.SEPARATE+MD5Encrypt.StringToMD5(SystemCache.PAYKEY+SystemCache.GAMECODE+SystemCache.userInfo.serverID+SystemCache.userInfo.uid+productID+(TextUtils.isEmpty(ext)?"":ext)+oasOrderid);

		BaseUtils.logDebug(TAG, "Start purchase "+productID);
		mHelper.launchPurchaseFlow(this, productID, RC_REQUEST,
				mPurchaseFinishedListener, payload);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BaseUtils.logDebug(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            BaseUtils.logDebug(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
    	if(p == null)
    		return false;
        String payload = p.getDeveloperPayload();
        if(TextUtils.isEmpty(payload))
        	return false;
        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */
        payload = payload.substring(payload.lastIndexOf(GoogleBillingUtils.SEPARATE)+GoogleBillingUtils.SEPARATE.length());
        
        if(MD5Encrypt.StringToMD5(SystemCache.PAYKEY+SystemCache.GAMECODE+SystemCache.userInfo.serverID+SystemCache.userInfo.uid+productID+(TextUtils.isEmpty(ext)?"":ext)+oasOrderid).equals(payload))
        	return true;
        
        return false;
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            BaseUtils.logDebug(TAG, "Purchase finished: " + result.toString() + ", purchase: " + purchase);
            boolean verify = verifyDeveloperPayload(purchase);
            // if we were disposed of in the meantime, quit.
            if (mHelper == null){// 页面被退出，此时如果支付成功，保存该订单
            	if(result.isSuccess()){
            		checkAndAddPurchase(purchase);
            		final Purchase pur = purchase;
            		if(verify){// 验证通过向支付平台申请发钻，做二次验证 
            			new Thread(new Runnable() {
							
							@Override
							public void run() {
								try {
									HttpService.instance().checkPurchaseForGoogle(pur, GoogleBillingUtils.SEPARATE);
								} catch (OasisSdkException e) {
									e.printStackTrace();
								}								
							}
						}).start();
            		}
            	}
            	return;
            }

            if (result.isFailure()) {
            	BaseUtils.logError(TAG, "Error purchasing: " + IabHelper.getResponseDesc(result.getResponse()));
            	if(result.getResponse() == -1005){// User canceled
            		myHandler.sendEmptyMessage(-1);
            		setResultInfo(OASISPlatformConstant.RESULT_CANCLE, "User canceled");
            		return;
            	}
            	Message msg = new Message();
            	msg.what = 0;
            	msg.obj = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_google_notice_3")); 
                myHandler.sendMessage(msg);
                return;
            }
            try {
				String[] info = purchase.getDeveloperPayload().split(GoogleBillingUtils.SEPARATE);

				// 向Mdata发送数据
				List<String> parameters = new ArrayList<String>();
				parameters.add("\"uid\":\""+info[0]+"\"");
				parameters.add("\"roleid\":\""+info[2]+"\"");
				parameters.add("\"serverid\":\""+info[1]+"\"");
				if(info.length >= 6 && ("android".equalsIgnoreCase(info[5]) || "all".equalsIgnoreCase(info[5]) || "test".equalsIgnoreCase(info[5]) ))
					parameters.add("\"servertype\":\""+info[5]+"\"");
				else
					parameters.add("\"servertype\":\"\"");
				parameters.add("\"product_id\":\""+purchase.getSku()+"\"");
				parameters.add("\"payment_channal\":\"mob_google\"");
				String re = info[4];
				if(re != null && !TextUtils.isEmpty(re)){
					String[] rev = re.split("_");
					parameters.add("\"cost\":\""+(rev.length>0?rev[0]:"")+"\"");
					parameters.add("\"currency\":\""+(rev.length>1?rev[1]:"")+"\"");
				}else{
					parameters.add("\"cost\":\"\"");
					parameters.add("\"currency\":\"\"");							
				}
				parameters.add("\"value\":\"\"");
				if(info.length >= 7)
					parameters.add("\"oas_order_id\":\""+info[6]+"\"");
				else
					parameters.add("\"oas_order_id\":\"\"");
				parameters.add("\"third_party_orderid\":\""+purchase.getOrderId()+"\"");
				parameters.add("\"result_code\":\""+result.isSuccess()+"_"+verify+"\"");
				parameters.add("\"isreport\":\""+(PhoneInfo.instance().isTrackAble()?"Y":"N")+"\"");
				
				List<String> status = new ArrayList<String>();
				status.add("\"event_type\":\"order\"");
				ReportUtils.add(ReportUtils.DEFAULTEVENT_PAID_MONEY, parameters, status);// 付款成功，向Mdata上报
			} catch (Exception e) {
				BaseUtils.logError(TAG, "Google play billing send mdata fail.");
			}
            if (!verify) {
            	Message msg = new Message();
            	msg.what = 0;
            	msg.obj = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_google_notice_4")); 
                myHandler.sendMessage(msg);
                BaseUtils.logError(TAG, "Error purchasing. Authenticity verification failed.");
                return;
            }

            BaseUtils.logDebug(TAG, "Purchase successful."+purchase.toString());
            
            checkAndAddPurchase(purchase);// 增加catch，避免异常造成崩溃				
			
            check(purchase);

        }
    };

    /**
	 * 检查当前订单是否已入库，没有入库时，插入
	 * @param p	订单信息
	 * @return
	 */
	public void checkAndAddPurchase(Purchase p){
		try {
			long id = 0;
			if(GoogleBillingUtils.checkPurchaseIsExist(p)){
				BaseUtils.logError(TAG, "支付订单保存至数据库成功1。");
			}else{
				id = GoogleBillingUtils.addPurchase(p);
				if(id > 0)
					BaseUtils.logError(TAG, "支付订单保存至数据库成功2。");
				else
					BaseUtils.logError(TAG, "支付订单保存至数据库失败。");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}


    /**
     * 消费掉旧订单
     * */
    private void consumeOldOrder(Purchase purchase, final int code, final int isEnd){
    	mHelper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
    		public void onConsumeFinished(Purchase p, IabResult result) {
    			BaseUtils.logDebug(TAG, "consumeOldOrder finished. Purchase: " + p + ", result: " + result);
    			
    			
    			// if we were disposed of in the meantime, quit.
    			if (mHelper == null) return;
    			
    			Message msg = null;
    			if (result.isSuccess()) {
    				BaseUtils.logDebug(TAG, "consumeOldOrder successful. Provisioning. Purchase.orderID="+p.getOrderId());
//                  case 1000000://交易成功且发钻成功
//    				case 1000002://该购买交易已发钻成功，因客户端未消费成功，所以重复2次验证
    				
					if(GoogleBillingUtils.deletePurchase(p.getOrderId()) > 0){
						if(code == 1000000 || code == 1000002){ // 发钻成功后，才能跟踪支付
	    					msg = new Message();
	    					msg.what = 103;//提示用户
	    					msg.obj = p;
	    					myHandler.sendMessage(msg);
	    					
	    					msg = new Message();
	    					msg.what = 102;// 加跟踪
	    					msg.obj = p;
	    					msg.arg1 = code;
	    					myHandler.sendMessage(msg);
	    				}	
					}else{
						BaseUtils.logError(TAG, "delete by orderid="+p.getOrderId());
					}
    			}else{
    				if(result.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED){
    					if(GoogleBillingUtils.deletePurchase(p.getOrderId()) > 0){
//    						有可能已跟踪过了，所以此处不宜再跟踪
//							msg = new Message();
//	    					msg.what = 102;// 加跟踪
//	    					msg.obj = p;
//	    					myHandler.sendMessage(msg);
						}else{
							BaseUtils.logError(TAG, "delete by orderid="+p.getOrderId());
						}
    				}
    			}
    			
    			myHandler.sendEmptyMessage(98);//再次检测旧订单
    		}
    	});
    }
    private void consume(Purchase purchase, final int code){
    	mHelper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
            public void onConsumeFinished(Purchase p, IabResult result) {
                BaseUtils.logDebug(TAG, "Consumption finished. Purchase: " + p + ", result: " + result);

                // if we were disposed of in the meantime, quit.
                if (mHelper == null) return;

                if (result.isFailure()) {
                	BaseUtils.logError(TAG, "Error while consuming: " + IabHelper.getResponseDesc(result.getResponse()));
                	if(code == -1){// 旧订单消费失败，退出当前支付
                		if(result.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED){// 消费状态为 item not owned,开始查询库存执行当前用户的支付操作。
//                			queryInventory();
                        	myHandler.sendEmptyMessage(101);
                			return ;
                		}
                		//消费失败，提示用户“支付失败”，同时退出支付
	                	Message msg = new Message();
	                	msg.what = 0;
	                	msg.obj = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_error_fail")); 
	                    myHandler.sendMessage(msg);
	                	return;
                	}
                }
                
                if (result.isSuccess()) {
                    BaseUtils.logDebug(TAG, "Consumption successful. Provisioning. Purchase.orderID="+p.getOrderId());
//                  case 1000000://交易成功且发钻成功
//    				case 1000002://该购买交易已发钻成功，因客户端未消费成功，所以重复2次验证
                    
					if(GoogleBillingUtils.deletePurchase(p.getOrderId()) > 0){// 消费成功，删除数据库记录
						if(code == 1000000 || code == 1000002){ // 发钻成功后，才能跟踪支付
							Message msg = new Message();
							msg = new Message();
	    					msg.what = 102;// 加跟踪
	    					msg.obj = p;
	    					msg.arg1 = code;
	    					myHandler.sendMessage(msg);
						}
					}else{
						BaseUtils.logError(TAG, "delete by orderid="+p.getOrderId());
					}
                }
                if(code == -1){// 旧订单消费成功，继续支付当前订单
//                	launchPurchaseFlow();
//                	queryInventory();
                	if(isPageClose()){
                		isPageCloseHandler();
                		return;
                	}
                	myHandler.sendEmptyMessage(101);
                	return;
                }
                Message msg = new Message();
                msg.what = HANDLER_SUCECCES;
                msg.arg1 = code;
                myHandler.sendMessage(msg);
                BaseUtils.logDebug(TAG, "End consumption flow.");
            }
        });
    }
    /**
     * 二次服务器验证
     */
    private void check(final Purchase purchase){
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				Message msg = new Message();
				int res = -1;
				try {
					res = HttpService.instance().checkPurchaseForGoogle(purchase, GoogleBillingUtils.SEPARATE);
					switch (res) {
					case 1000000://交易成功且发钻成功
					case 1000002://该购买交易已发钻成功，因客户端未消费成功，所以重复2次验证
					case 1000006://支付完成，Google验证未通过。（如无效订单等）
					case 1000001://sign验证错误，只限开发阶段错误，所以消费掉该订单。
			            BaseUtils.logDebug(TAG, "Code="+res+"; Start consume."+purchase.toString());
						
						msg.what = 100;
						msg.arg1 = res;
						msg.obj = purchase;
						myHandler.sendMessage(msg);
						break;
						
					case 1000100://未知异常
					case 1000004://ProductID错误—一般不会出现BUG
						checkAndAddPurchase(purchase);
						myHandler.sendEmptyMessage(HANDLER_FAIL);
						break;
						
					case 1000003://支付完成，验证连接失败-连接超时或者无法连接上Google服务器，
					case 1000005://支付成功，但是发钻不成功，
					default:
						msg.what = HANDLER_EXCEPTION;
						msg.obj = purchase;
						myHandler.sendMessage(msg);// retry
						break;
					}
				} catch (OasisSdkException e) {// 请求oas服务器2次验证抛异常，允许用户重试
					msg.what = HANDLER_EXCEPTION;
					msg.obj = purchase;
					myHandler.sendMessage(msg);// retry
				}finally{
					try {
						String[] info = purchase.getDeveloperPayload().split(GoogleBillingUtils.SEPARATE);

						// 向Mdata发送数据
						List<String> parameters = new ArrayList<String>();
						parameters.add("\"uid\":\""+info[0]+"\"");
						parameters.add("\"roleid\":\""+info[2]+"\"");
						parameters.add("\"serverid\":\""+info[1]+"\"");
						if(info.length >= 6 && ("android".equalsIgnoreCase(info[5]) || "all".equalsIgnoreCase(info[5]) || "test".equalsIgnoreCase(info[5]) ))
							parameters.add("\"servertype\":\""+info[5]+"\"");
						else
							parameters.add("\"servertype\":\"\"");
						parameters.add("\"product_id\":\""+purchase.getSku()+"\"");
						parameters.add("\"payment_channal\":\"mob_google\"");
						String re = info[4];
						if(re != null && !TextUtils.isEmpty(re)){
							String[] rev = re.split("_");
							parameters.add("\"cost\":\""+(rev.length>0?rev[0]:"")+"\"");
							parameters.add("\"currency\":\""+(rev.length>1?rev[1]:"")+"\"");
						}else{
							parameters.add("\"cost\":\"\"");
							parameters.add("\"currency\":\"\"");							
						}
						parameters.add("\"value\":\"\"");
						if(info.length >= 7)
							parameters.add("\"oas_order_id\":\""+info[6]+"\"");
						else
							parameters.add("\"oas_order_id\":\"\"");
						parameters.add("\"third_party_orderid\":\""+purchase.getOrderId()+"\"");
						parameters.add("\"result_code\":\""+res+"\"");
						parameters.add("\"isreport\":\""+(PhoneInfo.instance().isTrackAble()?"Y":"N")+"\"");
						
						List<String> status = new ArrayList<String>();
						status.add("\"event_type\":\"order\"");
						ReportUtils.add(ReportUtils.DEFAULTEVENT_ORDER_REPORTED, parameters, status);
					} catch (Exception e) {
						BaseUtils.logError(TAG, "Google play billing send mdata fail.");
					}
				}
			}
		}).start();
    	
//    	* 			1000001:验证信息错误（key无效）
//   	 * 			1000002:该购买交易已发钻成功
//   	 * 			1000003:支付完成，验证连接失败-连接超时或者无法连接上Google服务器，
//   	 * 			1000004：ProductID错误—一般不会出现BUG
//   	 * 			1000005:支付成功，但是发钻不成功，
//   	 * 			1000100:未知错误，
    }
    public static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<GooglePlayBillingActivity> mOuter;

		public MyHandler(GooglePlayBillingActivity activity) {
			mOuter = new WeakReference<GooglePlayBillingActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			GooglePlayBillingActivity outer = mOuter.get();
			if (outer != null) {
				switch (msg.what) {
				case HANDLER_SUCECCES:
					if(msg.arg1 == 1000000 || msg.arg1 == 1000002){// 验证成功，并发钻成功
						BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_error_success")));
						outer.setResultInfo(OASISPlatformConstant.RESULT_SUCCESS, "验证成功，并发钻成功");
					}else if(msg.arg1 == 1000006){// 商品交易订单Google验证未通过
						BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_google_notice_4")));
						outer.setResultInfo(OASISPlatformConstant.RESULT_EXCEPTION, "商品交易订单Google验证未通过");
					}
					break;
				case HANDLER_FAIL:
					BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_error_fail")));
					outer.setResultInfo(OASISPlatformConstant.RESULT_FAIL, "支付失败");
				 	
					break;
				case HANDLER_EXCEPTION:
					outer.setWaitScreen(false);
					outer.checkAndAddPurchase((Purchase)msg.obj);
					outer.alert((Purchase)msg.obj);
					// 提示用户重试
					break;
				case 0:
					outer.complain((String)msg.obj);
					break;
				case 98:
					outer.checkALLOrder(msg.arg1+1);
					break;
				case 99:
					outer.consumeOldOrder((Purchase) msg.obj, msg.arg1, msg.arg2);
					break;
				case 100:
					outer.checkAndAddPurchase((Purchase)msg.obj);
					outer.consume((Purchase) msg.obj, msg.arg1);
					break;
				case 101:
					outer.queryInventory();
					break;
				case 102:
					Purchase  p = (Purchase) msg.obj;
					String[] info = p.getDeveloperPayload().split(GoogleBillingUtils.SEPARATE);
					if(info != null && info.length >= 5 && !TextUtils.isEmpty(info[4])){
						String[] rev = info[4].split("_");// info[4]包含2种类型：9.99或9.99_RUB
						double revenue = 0;
						try {
							revenue = Double.parseDouble(rev[0]);
						} catch (Exception e) {
							revenue = 0;
						}
						
						String currency = "";
						if(rev.length > 1){
							currency = rev[1];
						}else{
							// 兼容历史数据
							// 如果info[4]中不包含货币，根据p.getSku从当前缓存套餐信息中获取
							if(SystemCache.payInfoLists != null)
							for (PayInfoList payInfos : SystemCache.payInfoLists) {
								if("mob_google".equals(payInfos.pay_way)){
									for (PayInfoDetail detail : payInfos.list) {
										if(p.getSku().equals(detail.price_product_id)){// 使用sku匹配 product id
											currency = detail.currency;
											break;
										}
									}
									break;
								}
							}//end for
						}//end else
							
						if(revenue > 0 && !TextUtils.isEmpty(currency)){// 收入大于0 且 货币不为空时，上报Adjust
							BaseUtils.trackRevenue(outer, ReportAdjustInfo.EVENTNAME_REVENUE+"_"+currency, revenue, currency, null);
						}
						
						 // 向Mdata发送支付成功的事件
		                try {
		                	List<String> parameters = new ArrayList<String>();
							parameters.add("\"uid\":\""+info[0]+"\"");
							parameters.add("\"roleid\":\""+info[2]+"\"");
							parameters.add("\"serverid\":\""+info[1]+"\"");
							if(info.length >= 6 && ("android".equalsIgnoreCase(info[5]) || "all".equalsIgnoreCase(info[5]) || "test".equalsIgnoreCase(info[5]) ))
								parameters.add("\"servertype\":\""+info[5]+"\"");
							else
								parameters.add("\"servertype\":\""+SystemCache.userInfo.serverType+"\"");
							parameters.add("\"product_id\":\""+p.getSku()+"\"");
							parameters.add("\"payment_channal\":\"mob_google\"");
							parameters.add("\"cost\":\""+revenue+"\"");
							parameters.add("\"currency\":\""+currency+"\"");
							parameters.add("\"value\":\"\"");
							if(info.length >= 7)
								parameters.add("\"oas_order_id\":\""+info[6]+"\"");
							else
								parameters.add("\"oas_order_id\":\"\"");
							
							parameters.add("\"third_party_orderid\":\""+p.getOrderId()+"\"");
							parameters.add("\"result_code\":\""+""+msg.arg1+"\"");
							parameters.add("\"isreport\":\""+(PhoneInfo.instance().isTrackAble()?"Y":"N")+"\"");
							
							List<String> status = new ArrayList<String>();
							status.add("\"event_type\":\"paid\"");
							status.add("\"isreport\":\""+(PhoneInfo.instance().isTrackAble()?"Y":"N")+"\"");
							ReportUtils.add(ReportUtils.DEFAULTEVENT_PAID, parameters, status);
						} catch (Exception e) {
							BaseUtils.logError(TAG, "Google play billing send mdata fail.");
						}
		            	
					}
					break;
				case 103:
					BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_error_success")));
					break;
				case -1:
					outer.close();
					break;
				case -2:
					BaseUtils.showMsg(outer, (String)msg.obj);
					break;
				case -10000:
					outer.setWaitScreen(false);
					switch (msg.arg1) {
					case 1000000:
						outer.setResultInfo(OASISPlatformConstant.RESULT_SUCCESS, "充值成功且发钻成功");
						outer.close();
						break;
					case 1000001:
						outer.setResultInfo(OASISPlatformConstant.RESULT_FAIL, "sign验证错误");
						outer.close();
						break;
					case 1000002:
						outer.setResultInfo(OASISPlatformConstant.RESULT_FAIL, "参数不能为空");
						outer.close();
						break;
					case 1000003:
						outer.setResultInfo(OASISPlatformConstant.RESULT_FAIL, "product_id套餐未配置");
						outer.close();
						break;
					case 1000004://游戏方sign验证错误
					case 1000005://游戏方验证用户不存在
					case 1000006://游戏币数量不能小于1
					case 1000007://游戏方验证订单已存在
					case 1000008://游戏方验证IP限制
					case 1000009://游戏方验证 服务器编号错误
					case 1000010://游戏方验证 参数个数不对
					case 1000011://游戏方增加游戏币失败
						outer.setResultInfo(OASISPlatformConstant.RESULT_FAIL, "支付成功，但是发钻不成功.Error:"+msg.arg1);
						outer.close();
						break;
					case 1000100://未知错误
						outer.setResultInfo(OASISPlatformConstant.RESULT_FAIL, "未知错误");
						outer.close();
						break;

					default:
						break;
					}
					break;
				default:
					
					break;
				}
			}
		}
	}
    private void setResultInfo(int statusCode, String errorMessage){
    	if(SystemCache.oasisInterface != null)
    		SystemCache.oasisInterface.paymentCallback("google", statusCode, errorMessage);
    	else
    		Log.e(TAG, "OASISPlatformInterface 未初始化，无法回调paymentCallback。");
        close();
    }
    @Override
    protected void onResume() {
    	super.onResume();
    	
    }
    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        BaseUtils.logDebug(TAG, "Destroying helper.");
        if (mHelper != null) {
        	try {				
        		mHelper.dispose();
			} catch (Exception e) {
				BaseUtils.logError(TAG, "Google onDestroy() exception:"+e.getMessage());
			}
            mHelper = null;
        }
    }

    void complain(String message) {
        BaseUtils.logError(TAG, "**** TrivialDrive Error: " + message);
//        alert(message);
        BaseUtils.showMsg(this.getApplicationContext(), message);
        close();
    }

    void close(){
    	setWaitScreen(false);
        finish();
    }
    /**
     * 弹框提醒用户重试尝试（请求发钻重试）
     * @param purchase
     */
    void alert(final Purchase purchase) {
    	final AlertDialog d = new AlertDialog.Builder(this).create();
		d.show();
		d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_dialog_notitle"));
		d.setCanceledOnTouchOutside(false);
		d.setCancelable(false);
		TextView retry = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_sure"));
		retry.setText(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_google_notice_alert_retry")));
		retry.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
				// 重试
				d.dismiss();
				setWaitScreen(true);
				check(purchase);
			}
		});
		TextView close = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_cancle"));
		close.setText(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_google_notice_alert_close")));
		close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				d.dismiss();
				setResultInfo(OASISPlatformConstant.RESULT_EXCEPTION_GOOGLEPAY_EXCEPTION, "支付成功，但用户不再尝试发钻");
				close();
			}
		});
    	
		TextView content = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_content"));
		content.setText(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_google_notice_alert_content")));
		
    }

    /**
     * 当页面被用户关闭后，不再做其他操作
     */
    private void isPageCloseHandler(){
    	myHandler.sendEmptyMessage(-1);
    	setResultInfo(OASISPlatformConstant.RESULT_CANCLE, "用户取消操作");
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		return true;
	}
    
    private void initSandBox(){
    	findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_google_sandbox")).setVisibility(View.VISIBLE);
    	findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_google_sandbox_close")).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResultInfo(OASISPlatformConstant.RESULT_CANCLE, "用户取消操作");
				close();
			}
		});
    	((TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_google_sandbox_content"))).setText(getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_sandbox_google_notice1"))+"Google Play");
    	findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_google_sandbox_pay_success")).setOnClickListener(new OnClickListener() {
    		
    		@Override
    		public void onClick(View v) {
    			//请求发钻
    			setWaitScreen(true);
    			new Thread(new Runnable() {
					
					@Override
					public void run() {
						int res = 0;
						Purchase p = new Purchase(productID, "");
						try {
							res = HttpService.instance().checkPurchaseForGoogleBySandBox(p);
						} catch (OasisSdkException e) {
							res = 1000100;
						}
						Message msg = new Message();
						msg.what = -10000;
						msg.arg1 = res;
						myHandler.sendMessage(msg);
					}
				}).start();
    		}
    	});
    	findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_google_sandbox_pay_fail")).setOnClickListener(new OnClickListener() {
    		
    		@Override
    		public void onClick(View v) {
    			BaseUtils.showMsg(GooglePlayBillingActivity.this.getApplicationContext(), getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_google_notice_3")));
    			setResultInfo(OASISPlatformConstant.RESULT_FAIL, "支付失败");
    			close();
    		}
    	});
    	
    	setWaitScreen(false);
    }
}
