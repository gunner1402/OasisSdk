package com.oasis.sdk.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oasis.sdk.base.report.ReportUtils;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.SystemCache;
/**
 * 论坛
 * @author xdb
 *
 */
public class OasisSdkForumActivity extends OasisSdkBaseActivity implements OnTouchListener, OnGestureListener {
	public static final String TAG = OasisSdkForumActivity.class.getName();
	
	public ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> mUploadMessageForAndroid5;
    public final static int FILECHOOSER_RESULTCODE = 1;
    public final static int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 2;
    
	MyHandler myHandler;
	ProgressBar progressBar;
	private LinearLayout layout, layout_fuc;
	private TextView tv_back, tv_forward;
	private String url;
	Boolean isShow = true; 
	GestureDetector mGestureDetector;
	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
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
		
		if(SystemCache.luntan != null){
			setWaitScreen(false);
			setUserInfo();
		}else
			loadData();
		
	}

	private void initGesture() {
		mGestureDetector = new GestureDetector(this, (OnGestureListener) this);
	}
	private void setUserInfo(){
		
		createWebView();
		layout.removeAllViews();
		layout.addView(SystemCache.luntan);
		
	}
	
	private void createWebView(){
		if(SystemCache.luntan == null){
			SystemCache.luntan = new WebView(this);
			SystemCache.luntan.getSettings().setJavaScriptEnabled(true);//可用JS
			SystemCache.luntan.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
			SystemCache.luntan.setWebViewClient(new WebViewClient(){
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
					reSetup();// 根据SystemCache.luntan的状态，设置按钮的状态
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
			SystemCache.luntan.setWebChromeClient(new ChromeClient());
		}
		reSetup();// 根据SystemCache.luntan的状态，设置按钮的状态
	}
	class ChromeClient extends WebChromeClient{
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			progressBar.setProgress(newProgress);
			super.onProgressChanged(view, newProgress);
		}
		@Override
		public boolean onJsAlert(WebView view, String url,
				String message, JsResult result) {//允许js弹出窗口
			return super.onJsAlert(view, url, message, result);
		}
		//扩展浏览器上传文件
        //3.0++版本
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooserImpl(uploadMsg);
        }

        //3.0--版本
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooserImpl(uploadMsg);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            openFileChooserImpl(uploadMsg);
        }

        // For Android > 5.0
        @SuppressLint("Override")
		public boolean onShowFileChooser (WebView webView, ValueCallback<Uri[]> uploadMsg, FileChooserParams fileChooserParams) {
            openFileChooserImplForAndroid5(uploadMsg);
            return true;
        }

	}
	private void openFileChooserImpl(ValueCallback<Uri> uploadMsg) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_customer_notice10"))), FILECHOOSER_RESULTCODE);//"File Chooser"
    }

    private void openFileChooserImplForAndroid5(ValueCallback<Uri[]> uploadMsg) {
        mUploadMessageForAndroid5 = uploadMsg;
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_customer_notice10")));//"Image Chooser"

        startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            Uri selectedImage = intent == null || resultCode != RESULT_OK ? null: intent.getData();
            if(selectedImage == null){
            	mUploadMessage.onReceiveValue(selectedImage);
            }else{
		        String[] filePathColumn = { MediaStore.Images.Media.DATA };
		 
		        Cursor cursor = getContentResolver().query(selectedImage,
		                filePathColumn, null, null, null);
		        cursor.moveToFirst();
		 
		        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		        String picturePath = cursor.getString(columnIndex);
		        cursor.close();
	
	            mUploadMessage.onReceiveValue(Uri.parse(picturePath));
            }
            mUploadMessage = null;

        } else if (requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5){
            if (null == mUploadMessageForAndroid5)
                return;
//            Uri result = (intent == null || resultCode != RESULT_OK) ? null: intent.getData();
//            if (result != null) {
//            	String[] filePathColumn = { MediaStore.Images.Media.DATA };
//            	Cursor cursor = getContentResolver().query(result,
//		                filePathColumn, null, null, null);
//            	if(cursor == null || cursor.getCount()<=0){
//            		if(cursor != null)
//            			cursor.close();
//            		mUploadMessageForAndroid5.onReceiveValue(new Uri[]{});
//        		}else{
//        			Uri[] uri = new Uri[cursor.getCount()];
//        			int i = 0;
//        			for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
//        			{
//        				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//        				String picturePath = cursor.getString(columnIndex);
//
//    					System.out.println("picturePath="+picturePath);
//        				uri[i] = Uri.parse(picturePath);
//        				i++;
//    				}
//    		        
//    		        cursor.close();
//    		        System.out.println("uri="+uri);
//                    mUploadMessageForAndroid5.onReceiveValue(uri);
//        		}
//            } else {
//                mUploadMessageForAndroid5.onReceiveValue(new Uri[]{});
//            }
            
            Uri[] results = null;

            // Check that the response is a good one
            if(resultCode == Activity.RESULT_OK) {
                if(intent != null) {
                    String dataString = intent.getDataString();
//                    System.out.println("uri="+dataString);
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
            mUploadMessageForAndroid5.onReceiveValue(results);
            mUploadMessageForAndroid5 = null;
        }
    }

	public void loadData(){
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					url = HttpService.instance().loginToForum();
				} catch (Exception e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				}

				myHandler.sendEmptyMessage(HANDLER_RESULT);
			}
		}).start();
	}
	
	public static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkForumActivity> mOuter;

		public MyHandler(OasisSdkForumActivity activity) {
			mOuter = new WeakReference<OasisSdkForumActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			OasisSdkForumActivity outer = mOuter.get();
			if (outer != null) {
				switch (msg.what) {
				case HANDLER_RESULT:
					if(TextUtils.isEmpty(outer.url))
						sendEmptyMessage(HANDLER_EXCEPTION);
					else{
						ReportUtils.add(ReportUtils.DEFAULTEVENT_FORUM, new ArrayList<String>(), new ArrayList<String>());
						sendEmptyMessage(HANDLER_SUCECCES);
					}
					break;
				case HANDLER_SUCECCES:
					outer.setUserInfo();
					SystemCache.luntan.loadUrl(outer.url);
					break;
				case HANDLER_EXCEPTION:
					outer.setWaitScreen(false);
					SystemCache.luntan = null;
					BaseUtils.showMsg(outer, outer.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_forum_1")));
					outer.finish();
					break;
				
				default:
					
					break;
				}
			}
		}
	}
	private void reSetup(){
		if(SystemCache.luntan != null && SystemCache.luntan.canGoBack())
			tv_back.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_forum_fuc_back_able_selector"));
		else
			tv_back.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_forum_fuc_back_unable"));
		
		if(SystemCache.luntan != null && SystemCache.luntan.canGoForward())
			tv_forward.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_forum_fuc_forward_able_selector"));
		else
			tv_forward.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_forum_fuc_forward_unable"));
	}
	public void onButtonClick_back(View v){
		SystemCache.luntan.goBack();
		reSetup();// 根据SystemCache.luntan的状态，设置按钮的状态
	}
	public void onButtonClick_forward(View v){
		SystemCache.luntan.goForward();
		reSetup();// 根据SystemCache.luntan的状态，设置按钮的状态
	}
	public void onButtonClick_refresh(View v){
		SystemCache.luntan.reload();
	}
	public void onButtonClick_mini(View v){
		finish();// 最小化时，保留SystemCache.luntan
	}
	public void onButtonClick_exit(View v){
		SystemCache.luntan = null;// 退出时，清空
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
}
