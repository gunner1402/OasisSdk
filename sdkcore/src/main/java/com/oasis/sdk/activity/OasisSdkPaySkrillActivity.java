package com.oasis.sdk.activity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.oasis.sdk.base.entity.PayConfigInfo;
import com.oasis.sdk.base.entity.PayInfoDetail;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.SystemCache;
/**
 * skrill
 * @author Administrator
 *
 */
public class OasisSdkPaySkrillActivity extends OasisSdkBaseActivity  {
	private static X509TrustManager xtm = new X509TrustManager() {
		public void checkClientTrusted(X509Certificate[] chain, String authType) {}
		    public void checkServerTrusted(X509Certificate[] chain, String authType) {
		    }
		    public X509Certificate[] getAcceptedIssuers() {
		        return null;
		       }
		   }; 
	private static  X509TrustManager[] xtmArray = new X509TrustManager[] { xtm };  
    private static final AllowAllHostnameVerifier HOSTNAME_VERIFIER = new AllowAllHostnameVerifier();   
  
    private static final int SOCKET_TIMEOUT = 3000;  
    
	PayInfoDetail payInfo;
	PayConfigInfo payConfig;
	WebView webView;
	MyHandler mHandler;
	String path = "https://www.moneybookers.com/app/payment.pl";
	String path2 = "";
	
	Boolean pageIsClose = false;
	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_pay_skrill"));
		
		mHandler = new MyHandler(this);
		
		webView = (WebView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_skrill_webview"));

		payInfo = (PayInfoDetail) getIntent().getExtras().get("payInfo");
		payConfig = (PayConfigInfo) getIntent().getExtras().get("payConfig");
		
//		webView.loadUrl(url);
		webView.getSettings().setJavaScriptEnabled(true);//可用JS
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
				if(!pageIsClose)
					setWaitScreen(true);
//				System.out.println("----"+view.getUrl());
				super.onPageStarted(view, url, favicon);
			}
			
		});
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(payConfig == null || TextUtils.isEmpty(payConfig.email_receive)){
					mHandler.sendEmptyMessageDelayed(HANDLER_FAIL, 1000);//延迟1秒执行
					return;
				}
				StringBuffer res = new StringBuffer();
				HttpsURLConnection conn;
				StringBuilder entityBuilder = new StringBuilder("&pay_to_email=" + payConfig.email_receive + //email_receive  oasismobile_br@oasgame.com
						"&language=" + Locale.getDefault().getLanguage() +
						"&amount=" + payInfo.amount +
						"&currency=" + payInfo.currency +
//						"&prepare_only=1" +
						"&ondemand_note=" +
						
						"&return_url=" + //http://pay.oasgames.com/payways/mob_skrill/pay_success.php
						"&return_url_target=3" +
						"&status_url=http://pay.oasgames.com/payways/mob_skrill/rechargeSkrill.php" +
						"&status_url2=mailto:" + payConfig.email_notification + //email_notification pay_skrill@oasgame.com
						"&recipient_description=OASIS GAMES LIMITED" +
						"&transaction_id=" + payInfo.orderId +
						
						"&pay_from_email=" +
						"&merchant_fields=game_code" +
						"&game_code=" + SystemCache.GAMECODE +
						"&payment_methods=ACC");
				try {
					byte[] entity = entityBuilder.toString().getBytes();
					URL url = new URL(path);
					conn = (HttpsURLConnection) url.openConnection();
					if (conn instanceof HttpsURLConnection) {   
					     // Trust all certificates   
					     SSLContext context = SSLContext.getInstance("TLS");   
					     context.init(new KeyManager[0], xtmArray, new SecureRandom());   
					     SSLSocketFactory socketFactory = context.getSocketFactory();   
					     ((HttpsURLConnection) conn).setSSLSocketFactory(socketFactory);   
					     ((HttpsURLConnection) conn).setHostnameVerifier(HOSTNAME_VERIFIER);   
					 }
					conn.setConnectTimeout(5 * 1000);
					conn.setRequestMethod("POST");
					conn.setDoOutput(true);//允许输出数据
					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					conn.setRequestProperty("Content-Length", String.valueOf(entity.length));
					OutputStream outStream = conn.getOutputStream();
					outStream.write(entity);
					outStream.flush();
					outStream.close();
					if(conn.getResponseCode() == 200){
						InputStream ins = conn.getInputStream();
						BufferedReader rd = new BufferedReader(new InputStreamReader(ins));   
						String line ;
						  while ((line = rd.readLine()) != null) {   
							  res.append(line);
				            }   
					}
					
					
					if(res.length()>0){
						path+="?sid="+res.toString();
						path2 = res.toString();
						mHandler.sendEmptyMessage(HANDLER_SUCECCES);
					}else{
						mHandler.sendEmptyMessage(HANDLER_FAIL);
					}
				} catch (Exception e) {
					
					e.printStackTrace();
					mHandler.sendEmptyMessage(HANDLER_FAIL);
				}	
				
				
			}
		}).start();
		setWaitScreen(true);
	}
	
	public static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkPaySkrillActivity> mOuter;

		public MyHandler(OasisSdkPaySkrillActivity activity) {
			mOuter = new WeakReference<OasisSdkPaySkrillActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			OasisSdkPaySkrillActivity outer = mOuter.get();
			if (outer != null) {
				switch (msg.what) {
				case HANDLER_SUCECCES:
//					outer.webView.loadUrl(outer.path);
					outer.webView.loadData(outer.path2, "text/html", "utf-8");
					break;
				case HANDLER_FAIL:
					outer.setWaitScreen(false);
					BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_error_fail")));
					outer.finish();
					break;
				default:
					
					break;
				}
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		CookieSyncManager.createInstance(this.getApplicationContext()); 
		CookieSyncManager.getInstance().startSync(); 
		CookieManager.getInstance().removeSessionCookie();
		
		webView.clearCache(true); 
		webView.clearHistory();
		
		pageIsClose = true;
		super.onDestroy();
	}
	
}
