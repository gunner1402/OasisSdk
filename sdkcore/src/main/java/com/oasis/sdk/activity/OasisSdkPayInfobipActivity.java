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
import java.util.Locale;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import c.mpayments.android.PurchaseListener;
import c.mpayments.android.PurchaseManager;
import c.mpayments.android.PurchaseRequest;
import c.mpayments.android.PurchaseResponse;
import c.mpayments.android.ServiceAvailabilityListener;
import c.mpayments.android.util.Logger;

import com.oasis.sdk.base.Exception.OasisSdkException;
import com.oasis.sdk.base.entity.PayInfoDetail;
import com.oasis.sdk.base.entity.PhoneInfo;
import com.oasis.sdk.base.entity.ReportAdjustInfo;
import com.oasis.sdk.base.report.ReportUtils;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.Constant;
import com.oasis.sdk.base.utils.SystemCache;


/**
 * Infobip 支付
 * @author xdb
 */
public class OasisSdkPayInfobipActivity extends OasisSdkBaseActivity {
	
    // Debug tag, for logging
    static final String TAG = "Pay_Infobip";

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    
    PayInfoDetail payInfo;
    String productID;
    String gameServerID;
    
    Handler mHandler = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_pay_infobip"));
        setWaitScreen(true);
        mHandler = new MyHandler(this);
        
        try {
			Class.forName("c.mpayments.android.PurchaseManager");
		} catch (Exception e) {
			e.printStackTrace();
			Message msg = new Message();
			msg.what = HANDLER_ERROR;
			msg.arg1 = BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_infobip_notice_8");
			mHandler.sendMessageDelayed(msg, 2000);
			return;
		}
        
        Logger.setDebugModeEnabled(false);

		PurchaseManager.checkServiceAvailabilityAsync(Constant.PAYINFOBIPAPPKEY, this.getApplicationContext(),
				new ServiceAvailabilityListener() {

					@Override
					public void onServiceStatusObtained(int status) {
						if (status == PurchaseManager.SERVICE_AVAILABLE) {
							// 开始支付流程
							mHandler.sendEmptyMessage(100);
						} else {
							int error = -1;
							switch (status) {
							case PurchaseManager.SIM_CARD_NOT_PRESENT:
								error = BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_infobip_notice_1");
								break;
							case PurchaseManager.IS_IN_AIRPLANE_MODE:
								error = BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_infobip_notice_2");
								break;
							case PurchaseManager.INTERNET_NOT_AVAILABLE:
								error = BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_infobip_notice_3");
								break;
							case PurchaseManager.MCC_AND_MNC_NOT_AVAILABLE:
								error = BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_infobip_notice_4");
								break;
							case PurchaseManager.SERVICE_DOES_NOT_EXIST:
								error = BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_infobip_notice_5");
								break;
							case PurchaseManager.SERVICE_DISABLED:
								error = BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_infobip_notice_6");
								break;
							case PurchaseManager.COUNTRY_NOT_SUPPORTED:
								error = BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_infobip_notice_7");
								break;
							case PurchaseManager.SERVICE_UNAVAILABLE:
								error = BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_infobip_notice_8");
								break;

							default:
								break;
							}
							
							Message msg = new Message();
							msg.what = HANDLER_ERROR;
							msg.arg1 = error;
							mHandler.sendMessageDelayed(msg, 2000);
							
						}

					}
				});
        
		payInfo = (PayInfoDetail) getIntent().getExtras().get("payInfo");
    }
    
    private void startPurchase(){
		PurchaseManager.attachPurchaseListener(new PurchaseListener() {
			@Override
			public void onPurchaseSuccess(final PurchaseResponse paramPurchaseResponse) {
				// handle purchase success
				Log.d(TAG, "onPurchaseSuccess.paramPurchaseResponse="+paramPurchaseResponse);
				
				Message msg = new Message();
				msg.what = 101;
				msg.obj = paramPurchaseResponse;
				mHandler.sendMessage(msg);
			}

			@Override
			public void onPurchasePending(PurchaseResponse paramPurchaseResponse) {
				// notification that purchase verification has begun
				Log.e(TAG, "Purchase Pending.Message:"+paramPurchaseResponse.getErrorMessage());
				Message msg = new Message();
				msg.what = 20;
				mHandler.sendMessage(msg);
				
			}

			@Override
			public void onPurchaseFailed(PurchaseResponse paramPurchaseResponse) {
				// purchase fail
				Log.e(TAG, "Purchase Failed.Message:"+paramPurchaseResponse.getErrorMessage());
				Message msg = new Message();
				msg.what = 21;
				mHandler.sendMessage(msg);
				
			}

			@Override
			public void onPurchaseCanceled(
					PurchaseResponse paramPurchaseResponse) {
				// purchase canceled by user
				Log.d(TAG, "Purchase Canceled.Message:"+paramPurchaseResponse.getErrorMessage());
				mHandler.sendEmptyMessage(HANDLER_RESULT);
			}
		});
    	PurchaseRequest pr = new PurchaseRequest(Constant.PAYINFOBIPAPPKEY);// 您的服务 ID
    	pr.setClientId(payInfo.orderId); // optional YOUR-CLIENT-ID   SystemCache.userInfo.getUid() 2014-06-09 更改为订单id，方便服务器操作
    	pr.setPackageIndex(Integer.parseInt(TextUtils.isEmpty(payInfo.price_type)?"0":payInfo.price_type));// 套餐列表索引
    	pr.setLanguageCode(Locale.getDefault().getLanguage()); // optional
    	pr.setTestModeEnabled(false);
    	PurchaseManager.startPurchase(pr, this.getApplicationContext());
//    	pr.setPrice(Double.parseDouble(payInfo.amount));// 价格
//    	pr.setInfo("Info text ...");
//    	pr.setPackageIndex(0);// 包索引
//    	pr.setPrice(1.99);// 价格
    }
    
    private void check(final PurchaseResponse paramPurchaseResponse){
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
			// 此接口服务端不会做发钻处理，服务端将接受Infobip的通知进行处理。

				Message msg = new Message();
				try {
					int res = HttpService.instance().checkPurchaseForInfobip(paramPurchaseResponse, payInfo.id,	payInfo.orderId);
					switch (res) {
					case 1000000:// 交易成功且发钻成功
					case 1000002:// 该购买交易已发钻成功，因客户端未消费成功，所以重复2次验证
//					case 1000006:// 支付完成，验证未通过。（如无效订单等）
						Log.d(TAG, "Code=" + res + "; "
								+ paramPurchaseResponse.toString());

						msg.what = HANDLER_SUCECCES;
						msg.arg1 = res;
						msg.obj = paramPurchaseResponse;
						mHandler.sendMessage(msg);
						break;

					case 1000100:// 未知异常
					case 1000001:// 验证信息错误（key无效）
					case 1000004:// ProductID错误—一般不会出现BUG
						mHandler.sendEmptyMessage(HANDLER_FAIL);
						break;

//					case 1000003:// 支付完成，验证连接失败-连接超时或者无法连接上支付平台服务器，
					case 1000005:// 支付成功，但是发钻不成功，
					default:
						msg.what = HANDLER_EXCEPTION;
						msg.obj = paramPurchaseResponse;
						mHandler.sendMessage(msg);// retry
						break;
					}

				} catch (OasisSdkException e) {// 请求oas服务器2次验证抛异常，允许用户重试
					msg.what = HANDLER_EXCEPTION;
					msg.obj = paramPurchaseResponse;
					mHandler.sendMessage(msg);// retry
				}
					
			}
		}).start();
    }
    public static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkPayInfobipActivity> mOuter;

		public MyHandler(OasisSdkPayInfobipActivity activity) {
			mOuter = new WeakReference<OasisSdkPayInfobipActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			OasisSdkPayInfobipActivity outer = mOuter.get();
			if (outer != null) {
				switch (msg.what) {
				case HANDLER_SUCECCES:
					if(null != outer.payInfo && !TextUtils.isEmpty(outer.payInfo.price_original)){
						double revenue = 0;
						try {
							revenue = Double.parseDouble(outer.payInfo.price_original);
						} catch (NumberFormatException e) {
							revenue = 0;
						}
						finally{
							if(revenue>0)
								BaseUtils.trackRevenue(outer, ReportAdjustInfo.EVENTNAME_REVENUE+"_"+outer.payInfo.currency, revenue, outer.payInfo.currency, null);	
						}
					}
					try {
						
						List<String> parameters = new ArrayList<String>();
						parameters.add("\"uid\":\""+SystemCache.userInfo.uid+"\"");
						parameters.add("\"roleid\":\""+SystemCache.userInfo.roleID+"\"");
						parameters.add("\"serverid\":\""+SystemCache.userInfo.serverID+"\"");
						parameters.add("\"servertype\":\""+SystemCache.userInfo.serverType+"\"");
						parameters.add("\"product_id\":\""+outer.payInfo.id+"\"");
						parameters.add("\"payment_channal\":\""+outer.payInfo.pay_way+"\"");
						parameters.add("\"cost\":\""+outer.payInfo.amount+"\"");
						parameters.add("\"currency\":\""+outer.payInfo.currency+"\"");
						parameters.add("\"value\":\""+outer.payInfo.game_coins+"\"");
						parameters.add("\"oas_order_id\":\""+outer.payInfo.orderId+"\"");
						parameters.add("\"third_party_orderid\":\"\"");
						parameters.add("\"result_code\":\"1000000\"");
						parameters.add("\"isreport\":\""+(PhoneInfo.instance().isTrackAble()?"Y":"N")+"\"");
						
						List<String> status = new ArrayList<String>();
						status.add("\"event_type\":\"paid\"");
						status.add("\"isreport\":\""+(PhoneInfo.instance().isTrackAble()?"Y":"N")+"\"");
						ReportUtils.add(ReportUtils.DEFAULTEVENT_PAID, parameters, status);
					} catch (Exception e) {
						Log.e(TAG, outer.payInfo.pay_way + ReportUtils.DEFAULTEVENT_PAID + "-> send mdata fail.");
					}
					outer.complain(outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_error_success")));
						
					break;
				case HANDLER_FAIL:
					outer.complain(outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_error_fail")));
					break;
				case HANDLER_EXCEPTION:
					// 弹出框提示，询问用户是否重试
//					outer.setWaitScreen(false);
					outer.alert((PurchaseResponse)msg.obj);
					break;
				case HANDLER_ERROR:
					// 发生错误，无法继续执行
					outer.complain(outer.getResources().getString(msg.arg1));
					break;
				case HANDLER_RESULT:
					outer.close();
					break;
				case 20:// pending状态，提示用户等待确认
					BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_error_success2")));
					break;
				case 21:
					outer.complain(outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_error_fail")));
					break;
				case 100:
					outer.startPurchase();
					break;
				case 101:
//					outer.setWaitScreen(true);
					outer.check((PurchaseResponse)msg.obj);// 检查订单（请求发钻）
					break;
				}
			}
		}
    }
  
    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        Log.d(TAG, "Destroying .");
    }

    void complain(String message) {
        Log.d(TAG, message);
        BaseUtils.showMsg(this, message);
        close();
    }
    void alert(final PurchaseResponse purchase) {
    	setWaitScreen(false);
    	final AlertDialog d = new AlertDialog.Builder(this).create();
		d.show();
		d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_dialog"));
		d.setCanceledOnTouchOutside(false);
		d.setCancelable(false);
		Button retry = (Button)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_sure"));
		retry.setText(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_google_notice_alert_retry")));
		retry.setOnClickListener(new View.OnClickListener(){
			
			@Override
			public void onClick(View v) {
				// 重试
				d.dismiss();
				setWaitScreen(true);
				check(purchase);
			}
		});
		Button close = (Button)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_cancle"));
		close.setText(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_google_notice_alert_close")));
		close.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				d.dismiss();
				close();
			}
		});
    	
		TextView content = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_content"));
		content.setText(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_google_notice_alert_content")));
		
    }
    
    void close(){
    	setWaitScreen(false);
        finish();
    }
}
