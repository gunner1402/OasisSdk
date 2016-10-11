package com.oasis.sdk.activity;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.SystemCache;
/**
 * 论坛
 * @author xdb
 *
 */
public class OasisSdkWebActivity extends OasisSdkBaseActivity implements OnTouchListener, OnGestureListener {
	public final String TAG = OasisSdkWebActivity.class.getName();
    
	MyHandler myHandler;
	ProgressBar progressBar;
	private LinearLayout layout, layout_fuc;
	private TextView tv_back, tv_forward;
	private String url;

	Boolean isShow = true; 
	
	WebView webView;
	GestureDetector mGestureDetector;
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_forum"));
		myHandler = new MyHandler(this);
		
//		initHead(true, null, true, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pcenter_notice_1")));// 论坛
		progressBar = (ProgressBar) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_forum_progressbar"));
		layout = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_forum_webview"));
		layout_fuc = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_forum_fuc"));
		tv_back = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_forum_fuc_back"));
		tv_forward = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_forum_fuc_forward"));
		
		initGesture();

		setWaitScreen(true);
		
		setUserInfo();
		
		initLayout();
		
	}

	@SuppressLint("NewApi")
	private void initLayout(){
		layout_fuc.addOnLayoutChangeListener(new OnLayoutChangeListener() {
				
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
					int oldBottom) {
				if(oldBottom!=0&&bottom!=0&&(oldBottom-bottom)>0){
					hideView();
				}
			}
		});
		
	}
	private void initGesture() {
		mGestureDetector = new GestureDetector(this, (OnGestureListener) this);
	}
	private void setUserInfo(){
		
		createWebView();
		layout.removeAllViews();
		layout.addView(webView);
		
	}
	
	private void createWebView(){
		findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_forum_mini")).setVisibility(View.GONE);
		
		webView = new WebView(this);
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
					BaseUtils.logError(TAG, errorCode +  description);
				}
				public void onPageFinished(WebView view, String url) {
					setWaitScreen(false);
					reSetup();// 根据webView的状态，设置按钮的状态
//					oldScrollY = 0;
					showView();
					
					progressBar.setVisibility(View.INVISIBLE);
				};
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					progressBar.setVisibility(View.VISIBLE);
					progressBar.setProgress(0);
				}
				
			});
			webView.setWebChromeClient(new WebChromeClient(){
				@Override
				public void onProgressChanged(WebView view, int newProgress) {
					progressBar.setProgress(newProgress);
					super.onProgressChanged(view, newProgress);
				}
			});
		int type = getIntent().getIntExtra("type", -1);
		String uname = getIntent().getStringExtra("uname");
		switch (type) {
		case 1:
			url = HttpService.instance().updatePersonalInfo();
			setCookie();
			break;
		case 2:
			url = HttpService.instance().getBackPWD(uname);
			break;

		default:
			url = "www.oasgames.com";
			break;
		}
		webView.loadUrl(url);
		reSetup();// 根据webView的状态，设置按钮的状态
	}
	private void setCookie(){
		//setcookie("oas_user", $oas_token, time() + 3600 * 24 * 7, "/", "oasgames.com");
		
		BaseUtils.logDebug(TAG, "--------"+SystemCache.userInfo.token);
		long time = System.currentTimeMillis()/1000 + 3600 * 24 * 7;
		
		CookieManager cookieManager = CookieManager.getInstance();
		
		BaseUtils.logDebug(TAG, "--------"+cookieManager.getCookie(url));
		
//		cookieManager.setAcceptThirdPartyCookies(webView, true);
		cookieManager.setAcceptCookie(true);
		cookieManager.setCookie("oasgames.com", "oas_user="+SystemCache.userInfo.token+";Max-Age="+time+";Domain=oasgames.com;Path=/");//cookies是在HttpClient中获得的cookie
		
		BaseUtils.logDebug(TAG, cookieManager.getCookie(url));

	}
	public static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkWebActivity> mOuter;

		public MyHandler(OasisSdkWebActivity activity) {
			mOuter = new WeakReference<OasisSdkWebActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			OasisSdkWebActivity outer = mOuter.get();
			if (outer != null) {
				switch (msg.what) {
								
				default:
					
					break;
				}
			}
		}
	}
	private void reSetup(){
		if(webView != null && webView.canGoBack())
			tv_back.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_forum_fuc_back_able_selector"));
		else
			tv_back.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_forum_fuc_back_unable"));
		
		if(webView != null && webView.canGoForward())
			tv_forward.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_forum_fuc_forward_able_selector"));
		else
			tv_forward.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_forum_fuc_forward_unable"));
	}
	public void onButtonClick_back(View v){
		webView.goBack();
		reSetup();// 根据webView的状态，设置按钮的状态
	}
	public void onButtonClick_forward(View v){
		webView.goForward();
		reSetup();// 根据webView的状态，设置按钮的状态
	}
	public void onButtonClick_refresh(View v){
		webView.reload();
	}
	public void onButtonClick_mini(View v){
		finish();
	}
	public void onButtonClick_exit(View v){
		finish();
	}
	
	public void showView() {
		if(isShow)
			return;
		layout_fuc.clearAnimation();
		TranslateAnimation animation  = new TranslateAnimation(0, 0, layout_fuc.getHeight(), 0);
		animation.setInterpolator(new OvershootInterpolator());
		animation.setDuration(500);
		animation.setStartOffset(0);
		animation.setFillAfter(false);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				layout_fuc.setVisibility(View.VISIBLE);
				isShow = true;
			}
		});
		
		layout_fuc.startAnimation(animation);
	}
	public void hideView() {
		if(!isShow)
			return;
		layout_fuc.clearAnimation();
		TranslateAnimation animation  = new TranslateAnimation(0, 0, 0, layout_fuc.getHeight());
		animation.setInterpolator(new OvershootInterpolator());
		animation.setDuration(500);
		animation.setStartOffset(0);
		animation.setFillAfter(false);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				layout_fuc.setVisibility(View.GONE);
				isShow = false;
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
			}
		});
		
		layout_fuc.startAnimation(animation);
	}
	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		if(layout != null)
			layout.removeAllViews();
		webView = null;
		super.onDestroy();
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}
	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}
	@Override
	public void onLongPress(MotionEvent e) {}
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		hideKeyBoard(webView);
		if (distanceY > 0) {
			hideView();
		} else {
			showView();
		}
		return false;
	}
	@Override
	public void onShowPress(MotionEvent e) {
		
	}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		mGestureDetector.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}
	public void hideKeyBoard(View v){
		InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (im.isActive()) {
			im.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
		}
	}
}
