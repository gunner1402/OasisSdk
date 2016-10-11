package com.oasis.sdk.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.oasis.sdk.base.entity.PayConfigInfo;
import com.oasis.sdk.base.entity.PayInfoDetail;
import com.oasis.sdk.base.utils.BaseUtils;
/**
 * 支付-Boacompra\WorldPay
 * @author xdb
 *
 */
public class OasisSdkPayBoacompraActivity extends OasisSdkBaseActivity {
	public final String TAG = OasisSdkPayBoacompraActivity.class.getName();
    
	PayInfoDetail payInfo;
	PayConfigInfo payConfig;
	private String url;

	WebView webView;
	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_pay_boacompra"));
		
		initHead(true, null, true, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_head_title_charge")));// 借用套餐界面的标题
		
		setWaitScreen(true);
		
		createWebView();
		
	}
	
	private void createWebView(){
		webView = new WebView(this);
			webView.getSettings().setJavaScriptEnabled(true);//可用JS
			webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
			webView.setWebViewClient(new WebViewClient(){
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					
					view.loadUrl(url);// 使用当前WebView处理跳转
					return true;//true表示此事件在此处被处理，不需要再广播
				}
				@Override	//转向错误时的处理
				public void onReceivedError(WebView view, int errorCode,
						String description, String failingUrl) {
				}
				public void onPageFinished(WebView view, String url) {
					setWaitScreen(false);
				};
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					setWaitScreen(true);
					super.onPageStarted(view, url, favicon);
				}
				
			});
			webView.setWebChromeClient(new WebChromeClient() {
			    @Override
			    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
			    	return super.onJsAlert(view, url, message, result);
//			        AlertDialog.Builder b2 = new AlertDialog.Builder(MainActivity.this)
//			                .setTitle("R.string.title").setMessage(message)
//			                .setPositiveButton("ok",
//			                        new AlertDialog.OnClickListener() {
//			                            @Override
//			                            public void onClick(DialogInterface dialog,
//			                                    int which) {
//			                                result.confirm();
//			                                // MyWebView.this.finish();
//			                            }
//			                        });
	//
//			        b2.setCancelable(false);
//			        b2.create();
//			        b2.show();
//			        return true;
			    }
			});
		payInfo = (PayInfoDetail) getIntent().getExtras().get("payInfo");
		payConfig = (PayConfigInfo) getIntent().getExtras().get("payConfig");
		if("worldpay".equals(payInfo.pay_way))
			url = "http://pay.oasgames.com/payways/worldpay/redirectmob.php?oid="+payInfo.orderId;// 通过pay_way区分渠道
		else
			url = "https://pay.oasgames.com/payways/"+payInfo.pay_way+"/redirect.php?oid="+payInfo.orderId;// 通过pay_way区分渠道
		webView.loadUrl(url);
		
		((LinearLayout)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_boacompra_webview"))).addView(webView);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		webView = null;
		super.onDestroy();
	}
}
