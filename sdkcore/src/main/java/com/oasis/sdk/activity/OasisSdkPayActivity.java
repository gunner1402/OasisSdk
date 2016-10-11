package com.oasis.sdk.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.base.http.CallbackResultForActivity;
import com.oasis.sdk.OASISPlatformConstant;
import com.oasis.sdk.OasisCallback;
import com.oasis.sdk.base.Exception.OasisSdkException;
import com.oasis.sdk.base.entity.PayInfoDetail;
import com.oasis.sdk.base.entity.PayInfoList;
import com.oasis.sdk.base.entity.PhoneInfo;
import com.oasis.sdk.base.list.PayPriceListAdapter;
import com.oasis.sdk.base.list.PayWayListAdapter;
import com.oasis.sdk.base.report.ReportUtils;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.SystemCache;
/**
 * 第三方支付套餐页
 * @author xdb
 *
 */
public class OasisSdkPayActivity extends OasisSdkBaseActivity  {
	public static final String TAG = OasisSdkPayActivity.class.getName();
	public static String PAYTYPE_GOOGLE = "mob_google";
	public static String PAYTYPE_INFOBIP = "mob_infobip";
	public static String PAYTYPE_MOPAY = "mob_mopay";
	public static String PAYTYPE_SKRILL = "mob_skrill";
	public static String PAYTYPE_BOACOMPRA = "mob_boacompra";
	public static String PAYTYPE_PAYBYME = "mob_paybyme";
	public static String PAYTYPE_PAYMENTWALL = "mob_paymentwall";
	public static String PAYTYPE_WORLDPAY = "worldpay";
	public static String PAYTYPE_PAYPAL = "mob_paypal";

	final int[] pow = new int[]{1, 3, 9, 27, 81, 243, 729, 2187, 6561, 19683, 59049, 177147, 531441, 1594323, 4782969};
	
	RelativeLayout layout_guide;
	boolean showGuideFlag = false;
	
	TextView btn_reload;// 重新获取套餐
	LinearLayout layout_notice;// 提示信息
	LinearLayout btn_chargePC;// PC充值按钮
	Button btn_pay;//支付按钮
	
//    String ext = "";// 游戏需要透传的扩展参数
    String productID;// Google 商品ID
    String revValue;// 收入
    
	MyHandler myHandler;
	
	GridView gridView;
	PayPriceListAdapter adapter;
	ListView listView;
	PayWayListAdapter paywayAdapter;
	
	public PayInfoDetail selectedPayInfo = null;
	public PayInfoList curPayInfoList = null;
	
	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_pay"));
		myHandler = new MyHandler(this);
		
		initHead(true, null, true, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_head_title_charge")));
		init();

        setWaitScreen(true);
		loadData();
		
	}
	private void init(){
		productID = getIntent().getStringExtra("inAppProductID");
        revValue = getIntent().getStringExtra("revenue");
//		ext = getIntent().getStringExtra("ext");
		
		layout_guide = (RelativeLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_guide_pay"));
		layout_guide.setVisibility(View.INVISIBLE);
		layout_guide.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				layout_guide.setVisibility(View.INVISIBLE);
			}
		});
		
		btn_chargePC = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_function"));
		if(SystemCache.controlInfo.getPc_charge_condition() && BaseUtils.checkCameraDevice(this))
			btn_chargePC.setVisibility(View.VISIBLE);
		btn_chargePC.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(OasisSdkPayActivity.this, OasisSdkCaptureActivity.class));
				myHandler.sendEmptyMessage(102);// 标记为 访问过PC充值，以后将不再显示充值引导信息
			}
		});
		
		btn_reload = (TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_reload"));
		btn_reload.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setWaitScreen(true);
				layout_notice.setVisibility(View.INVISIBLE);
				loadData();
			}
		});
		
		layout_notice = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_notice"));
		btn_pay = (Button) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_btn"));
		btn_pay.setVisibility(View.INVISIBLE);
		
		listView = (ListView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_listview"));
		gridView = (GridView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_gridview"));

		paywayAdapter = new PayWayListAdapter(this, SystemCache.payInfoLists==null?new ArrayList<PayInfoList>():SystemCache.payInfoLists, 1, null);
		listView.setAdapter(paywayAdapter);
		
		adapter = new PayPriceListAdapter(this, (SystemCache.payInfoLists==null||SystemCache.payInfoLists.size()<=0||SystemCache.payInfoLists.get(0).list==null)?new ArrayList<PayInfoDetail>():SystemCache.payInfoLists.get(0).list, 1, null);
		gridView.setAdapter(adapter);
		
		if(paywayAdapter.getCount() <= 0)
			listView.setVisibility(View.INVISIBLE);
		if(adapter.getCount() <= 0)
			gridView.setVisibility(View.INVISIBLE);
	}
	
	private void loadData(){
		myHandler.sendEmptyMessage(101);
		HttpService.instance().getPayKindsInfo(new CallbackResultForActivity() {
			
			@Override
			public void success(Object data, String statusCode, String msg) {
				myHandler.sendEmptyMessage(HANDLER_RESULT);
			}
			
			@Override
			public void fail(String statusCode, String msg) {
				myHandler.sendEmptyMessage(HANDLER_RESULT);
			}
			
			@Override
			public void excetpion(Exception e) {
				myHandler.sendEmptyMessage(HANDLER_RESULT);
			}
		});
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				myHandler.sendEmptyMessage(101);
//				try {
////					if(SystemCache.payInfoLists == null || SystemCache.payInfoLists.size()<=0)
//					SystemCache.payInfoLists = HttpService.instance().getPayKindsInfo();
////					PayInfoList list = new PayInfoList();
////					list.pay_way = "mob_google";
////					list.list = new ArrayList<PayInfoDetail>();
////					List<PayInfoList> payinfo = new ArrayList<PayInfoList>();
////					payinfo.add(list);
////					SystemCache.payInfoLists = payinfo;
//				} catch (Exception e) {
////					myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
//				}
//
//				
//			}
//		}).start();
	}
	
	public void onClickToPay(View v){
		if(selectedPayInfo == null){
			BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_notice_null")));
			return;
		}
		if(SystemCache.userInfo == null || TextUtils.isEmpty(SystemCache.userInfo.serverID) || TextUtils.isEmpty(SystemCache.userInfo.roleID)){
			BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_menu_notice_relogin")));
			// 缺少用户信息
			return;
		}
		if(SystemCache.userInfo != null && SystemCache.userInfo.chargeable != 0){
			BaseUtils.showDisableDialog(this.getApplicationContext(), SystemCache.userInfo.chargeable==1?"oasisgames_sdk_login_notice_11":"oasisgames_sdk_login_notice_12");
			return;
		}
		
		setWaitScreen(true);
		// 第三方支付，先获取ext，在进行下单操作
		if (SystemCache.oasisInterface != null) {
			SystemCache.oasisInterface
					.getExtendValue(new MyOasisCallbackForExt());
		} else {
			BaseUtils
					.logError(TAG,
							"请先调用OASISPlatform。setOASISPlatformInterfaceImpl()完成接口的初始化");
			myHandler.sendEmptyMessage(100);
		}
			
	}
	
	class MyOasisCallbackForExt implements OasisCallback{

		@Override
		public void success(final String result) {

//			if(selectedPayInfo.pay_way.equals("mob_google")){
//				setWaitScreen(false);
//				startActivityForResult(new Intent().setClass(OasisSdkPayActivity.this, GooglePlayBillingActivity.class)
//						.putExtra("inAppProductID", selectedPayInfo.price_product_id)
//						.putExtra("revenue", selectedPayInfo.amount_show)
//						.putExtra("ext", result), OASISPlatformConstant.REQUEST_CODE_GOOGLEPAY);
//				return;
//			}
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// 获取ext成功，继续下单操作
					Message msg = new Message();
					msg.what = 100;
					try {
						msg.obj = HttpService.instance().sendOrder(selectedPayInfo.id, (TextUtils.isEmpty(result)?"":result));
					} catch (OasisSdkException e) {
						msg.obj = "";
					}
					myHandler.sendMessage(msg);
				}
			}).start();
			
		}

		@Override
		public void error(String result) {
			BaseUtils.logError(TAG, "获取ext失败，请游戏方研发检查接口.\n返回结果："+result);
			myHandler.sendEmptyMessage(100);
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == OASISPlatformConstant.REQUEST_CODE_GOOGLEPAY){// 回传Google返回的数据
			setResult(resultCode, data);
		}/*else if(requestCode == MobiamoPayment.ACTIVITY_MOBIAMO) {
	        if(data != null) {
	            MobiamoResponse response = (MobiamoResponse) data.getSerializableExtra(ResponseKey.RESPONSE_MESSAGE);
//	            txtPrice.setText(response.getPrice());
//	            txtMessage.setText(response.getMessage());
//	            txtProductId.setText(response.getProductId());
//	            txtProductName.setText(response.getProductName());
//	            txtTransactionStatus.setText(response.getStatus());
	            switch (resultCode) {
	                case ResponseCode.SUCCESSFUL:
	                	System.out.println("getPrice="+response.getPrice()+"\n"+
	                			"getMessage="+response.getMessage()+"\n"+
	                			"getProductId="+response.getProductId()+"\n"+
	                			"getProductName="+response.getProductName()+"\n"+
	                			"getStatus="+response.getStatus()+"\n"+
	                			"getTransactionId="+response.getTransactionId()+"\n"
	                			);
	                	response.getStatus();
	                    break;
	                case ResponseCode.FAILED:
	                    break;
	                case ResponseCode.CANCEL:
	                    break;
	            }
	        }
		}*/
	}
	
	public void updateUI(PayInfoDetail info){
		selectedPayInfo = info;
		adapter.notifyDataSetChanged();
	}
	public void updatePayWay(PayInfoList info){
		if(curPayInfoList == info)
			return;
		
		selectedPayInfo = null;// 清空
		curPayInfoList = info;

		paywayAdapter.notifyDataSetChanged();
		
		adapter.data = info.list;
		if(TextUtils.isEmpty(productID) || !PAYTYPE_GOOGLE.equals(info.pay_way))
			updateUI(info.list.get(0));
		else{
			for (PayInfoDetail detail : info.list) {
				if(productID.equals(detail.price_product_id)){
					updateUI(detail);
					break;
				}
			}
		}
	}
	public static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkPayActivity> mOuter;

		public MyHandler(OasisSdkPayActivity activity) {
			mOuter = new WeakReference<OasisSdkPayActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			OasisSdkPayActivity outer = mOuter.get();
			if (outer != null) {
				switch (msg.what) {
				case HANDLER_RESULT:
					outer.setWaitScreen(false);
					PayInfoList GooglePayInfo = null;
					if(SystemCache.payInfoLists != null ){
						List<PayInfoList> list = SystemCache.payInfoLists;
						for (PayInfoList i : list) {// 过滤没有套餐的支付渠道
							if(i.list == null || i.list.size() <= 0)
								SystemCache.payInfoLists.remove(i);
							else{
								if(PAYTYPE_GOOGLE.equals(i.pay_way)){
									GooglePayInfo = i;
								}
							}
						}
					}
					
					if(SystemCache.payInfoLists != null && SystemCache.payInfoLists.size() > 0){
						
						outer.paywayAdapter.data = SystemCache.payInfoLists;
						
						outer.adapter.data = GooglePayInfo == null ? SystemCache.payInfoLists.get(0).list : GooglePayInfo.list;
						
						outer.updatePayWay(GooglePayInfo == null ? SystemCache.payInfoLists.get(0) : GooglePayInfo);

						outer.listView.setVisibility(View.VISIBLE);
						outer.gridView.setVisibility(View.VISIBLE);
						
						outer.layout_notice.setVisibility(View.INVISIBLE);
						outer.btn_pay.setVisibility(View.VISIBLE);
					}else{
						outer.listView.setVisibility(View.INVISIBLE);
						outer.gridView.setVisibility(View.INVISIBLE);
						// 无可支付渠道提示
						outer.layout_notice.setVisibility(View.VISIBLE);
						outer.btn_pay.setVisibility(View.INVISIBLE);
					}
					
					if(!outer.showGuideFlag){// 本次还没有展示信息，即展示引导信息
						outer.showGuideFlag = true;
						boolean flag = (Boolean) BaseUtils.getSettingKVPfromSysCache("OASIS_GUIDE_PAY_COUNT_FLAG", false);
						if(!flag){
							int count = (Integer) BaseUtils.getSettingKVPfromSysCache("OASIS_GUIDE_PAY_COUNT", 1);
							for (int i = 0; i < outer.pow.length; i++) {
								int pow = outer.pow[i];
								if(pow == count){
									if(SystemCache.controlInfo.getPc_charge_condition() && BaseUtils.checkCameraDevice(outer))// PC充值开关为开，并且摄像头可用的情况下，才显示引导信息
										outer.layout_guide.setVisibility(View.VISIBLE); 
									break;
								}
								if(pow > count)// 当前计数 比 pow某位置数小时，跳出循环，减少循环次数
									break;
								
							}
						}
					}
					
					break;
				case HANDLER_SUCECCES:
					outer.setResult(RESULT_OK, null);
					outer.finish();
					
					break;
				case HANDLER_EXCEPTION:
					outer.setWaitScreen(false);
					BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_error_getinfos_fail")));
					break;
				case 100:
					outer.setWaitScreen(false);
					if(TextUtils.isEmpty((String)msg.obj)){
						BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_btn_error1")));
					}else{
						
						try {
							
							List<String> parameters = new ArrayList<String>();
							parameters.add("\"uid\":\""+SystemCache.userInfo.uid+"\"");
							parameters.add("\"roleid\":\""+SystemCache.userInfo.roleID+"\"");
							parameters.add("\"serverid\":\""+SystemCache.userInfo.serverID+"\"");
							parameters.add("\"servertype\":\""+SystemCache.userInfo.serverType+"\"");
							parameters.add("\"product_id\":\""+outer.selectedPayInfo.id+"\"");
							parameters.add("\"payment_channal\":\""+outer.selectedPayInfo.pay_way+"\"");
							parameters.add("\"cost\":\""+outer.selectedPayInfo.amount+"\"");
							parameters.add("\"currency\":\""+outer.selectedPayInfo.currency+"\"");
							parameters.add("\"value\":\""+outer.selectedPayInfo.game_coins+"\"");
							parameters.add("\"oas_order_id\":\""+(String)msg.obj+"\"");
							parameters.add("\"third_party_orderid\":\"\"");
							parameters.add("\"result_code\":\"ok\"");
							parameters.add("\"isreport\":\""+(PhoneInfo.instance().isTrackAble()?"Y":"N")+"\"");
							
							List<String> status = new ArrayList<String>();
							status.add("\"event_type\":\"order\"");
							ReportUtils.add(ReportUtils.DEFAULTEVENT_ORDER, parameters, status);
						} catch (Exception e) {
							Log.e(TAG, outer.selectedPayInfo.pay_way + ReportUtils.DEFAULTEVENT_ORDER + "-> send mdata fail.");
						}
						
						outer.selectedPayInfo.setOrderId((String) msg.obj);
						Intent in = new Intent();
						in.putExtra("payInfo", outer.selectedPayInfo);
						/*if(PAYTYPE_MOPAY.equals(outer.selectedPayInfo.pay_way)){
							in.setClass(outer, OasisSdkPayMopayActivity.class);
						}else */if(PAYTYPE_INFOBIP.equals(outer.selectedPayInfo.pay_way)){
							in.setClass(outer, OasisSdkPayInfobipActivity.class);
						}else if(PAYTYPE_SKRILL.equals(outer.selectedPayInfo.pay_way)){
							if(null != outer.curPayInfoList)
								in.putExtra("payConfig", outer.curPayInfoList.pay_way_config);
							in.setClass(outer, OasisSdkPaySkrillActivity.class);
						}else if(PAYTYPE_GOOGLE.equals(outer.selectedPayInfo.pay_way)){
							in.putExtra("inAppProductID", outer.selectedPayInfo.price_product_id);
							in.putExtra("revenue", outer.selectedPayInfo.amount_show);
							in.putExtra("oasOrderid", outer.selectedPayInfo.orderId);
							in.setClass(outer, GooglePlayBillingActivity.class);
						}else if(PAYTYPE_BOACOMPRA.equals(outer.selectedPayInfo.pay_way)){
							if(null != outer.curPayInfoList)
								in.putExtra("payConfig", outer.curPayInfoList.pay_way_config);
							in.setClass(outer, OasisSdkPayBoacompraActivity.class);
						}else if(PAYTYPE_WORLDPAY.equals(outer.selectedPayInfo.pay_way)){
							if(null != outer.curPayInfoList)
								in.putExtra("payConfig", outer.curPayInfoList.pay_way_config);
							in.setClass(outer, OasisSdkPayBoacompraActivity.class);// WorldPay使用Boacompra的Activity
						}else if(PAYTYPE_PAYPAL.equals(outer.selectedPayInfo.pay_way)){
							if(null != outer.curPayInfoList)
								in.putExtra("payConfig", outer.curPayInfoList.pay_way_config);
							in.setClass(outer, OasisSdkPayPaypalActivity.class);
						}
						/*else if(PAYTYPE_PAYMENTWALL.equals(outer.selectedPayInfo.pay_way)){
//							if(null != outer.curPayInfoList)
//								in.putExtra("payConfig", outer.curPayInfoList.getPay_way_config());
//							in.setClass(outer, OasisSdkPaymentWallActivity.class);
							outer.toPaymentWall();
							return;
						}else if(PAYTYPE_PAYBYME.equals(outer.selectedPayInfo.pay_way)){
							outer.toPayByMe();
							return;
						}*/else{
							return;
						}
						
						outer.startActivity(in);
					}
					break;
				case 101:
						int count = (Integer) BaseUtils.getSettingKVPfromSysCache("OASIS_GUIDE_PAY_COUNT", 0);
						BaseUtils.saveSettingKVPtoSysCache("OASIS_GUIDE_PAY_COUNT", count+1);
					break;
				case 102:
						BaseUtils.saveSettingKVPtoSysCache("OASIS_GUIDE_PAY_COUNT_FLAG", true);
					try {// 发送Mdata信息
						ReportUtils.add(ReportUtils.DEFAULTEVENT_CLICK_PCPAY, new ArrayList<String>(), new ArrayList<String>());
					} catch (Exception e) {
					}
					break;
				case 103:
					if(outer.adapter.getCount() > 0){
						List<PayInfoDetail> list = outer.adapter.data;
						outer.adapter = new PayPriceListAdapter(outer, list, 1, null);
						outer.gridView.setAdapter(outer.adapter);
						outer.updateUI(outer.selectedPayInfo);
					}
					

					break;
				default:
					
					break;
				}
			}
		}
	}
	public void toPayByMe(){
		BaseUtils.showMsg(this, "待开发");
//		PaymentInfo pbm = new PaymentInfo(PaymentInfo.PBM_LAYOUT_DARK, 
//				"oasisgames", "onmTNc", Integer.parseInt(selectedPayInfo.price_original), "title", "description", "content", 2);
//		pbm.setItemImage(BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "ic_launcher")));
//		pbm.setSupportURL("");// company.oasgames.com
//		pbm.setSmsSuccessMessage("Your transaction has been successfully completed");
//		pbm.setData("data");// Additional data which can be sent by SMS and will be redirected to your server side system by PaybyMe 
//		pbm.setOrderID(selectedPayInfo.orderId);
//		pbm.setSubCompany("OASIS GAMES");// 
//		PBMPaymentManager.purchase(this, pbm, new PaymentResponse() {
//			
//			@Override
//			public void paymentSucceeded() {
//				BaseUtils.showMsg(OasisSdkPayActivity.this, "paymentSucceeded");// 支付成功
//			}
//			
//			@Override
//			public void paymentFailed(String arg0) {
//				BaseUtils.showMsg(OasisSdkPayActivity.this, "paymentFailed");// 支付失败
//			}
//		});
	}
	public void toPaymentWall(){
//		MobiamoPayment request = new MobiamoPayment();
//	    request.setApplicationKey("1e2050dc784ef1558b4b0d34d3abee22");
//	    request.setProductId("1001");
//	    request.setProductName("Golden helmet");
//	    request.setUid(SystemCache.userInfo.uid);//YOUR_USER_ID_HERE
////	    request.setAmount("23.23");
////        request.setCurrency("USD");
////        request.setParameter(new TreeMap<K, V>());
////        request.setSign(sign)
//        Intent intent = MobiamoPayment.createIntent(request, getApplicationContext());
//        startActivityForResult(intent, MobiamoPayment.ACTIVITY_MOBIAMO);
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(showGuideFlag && layout_guide.isShown()){// 如果是引导页，先关闭引导页
					layout_guide.setVisibility(View.INVISIBLE);
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onResume() {
		super.onResume();
		myHandler.sendEmptyMessageDelayed(103, 300);
		
	}
}
