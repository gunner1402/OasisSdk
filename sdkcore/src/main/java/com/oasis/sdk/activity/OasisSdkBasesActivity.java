package com.oasis.sdk.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.oasis.sdk.OASISPlatform;
import com.oasis.sdk.base.utils.ApplicationContextManager;
import com.oasis.sdk.base.utils.BaseUtils;

public class OasisSdkBasesActivity extends Activity {

//	public static final int WAITDAILOG_OPEN = -1;
//	public static final int WAITDAILOG_CLOSE = -2;
	
	public static final int HANDLER_RESULT = 0;
	public static final int HANDLER_SUCECCES = 1;
	public static final int HANDLER_FAIL = 2;
	public static final int HANDLER_EXCEPTION = 3;
	public static final int HANDLER_ERROR = 4;
	public static final int HANDLER_EXCEPTION_NETWORK = 5;
	public static final int HANDLER_FINISH = 6;
	
	private View wait_dialog;
	private boolean isPageClose = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		String languageToLoad = "zh";
//		String coun = "cn";
//		Locale locale = new Locale(languageToLoad, coun);
////		Locale.setDefault(locale);
//		Configuration config = getResources().getConfiguration();
//		DisplayMetrics metrics = getResources().getDisplayMetrics();
////		config.locale = Locale.SIMPLIFIED_CHINESE;
//		config.locale = locale;
//		getResources().updateConfiguration(config, metrics);
		//不显示程序的标题栏
        requestWindowFeature( Window.FEATURE_NO_TITLE );
       
        //不显示系统的标题栏          
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                              WindowManager.LayoutParams.FLAG_FULLSCREEN );
        
//        if(TextUtils.isEmpty(SystemCache.packageName))// 如果为空，重新获取packageName
//        	SystemCache.packageName = getApplicationContext().getPackageName();
        
//		if(null == wait_dialog)
//			wait_dialog = BaseUtils.createWaitDialog(this, -1);

        checkUserInfoIsNull();
	}

	private void openWaitDialog(){
		if(null == wait_dialog){
			wait_dialog = getLayoutInflater().inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_waiting_anim"), null);
			wait_dialog.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
				}
			});
			addContentView(wait_dialog, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//			wait_dialog = BaseUtils.createWaitDialog(this, -1);
		}
		if(!wait_dialog.isShown())
			wait_dialog.setVisibility(View.VISIBLE);
		
//		if(!isPageClose)
//			wait_dialog.show();
	}
	
	private void closeWaitDialog(){
		if(wait_dialog != null)
			wait_dialog.setVisibility(View.INVISIBLE);
	}

	public void setWaitScreen(boolean type){
		if(isPageClose){
			return;
		}
		if(type)
			openWaitDialog();
		else
			closeWaitDialog();
	}
	/**
	 * 检测当前页面是否被关闭
	 * @return 返回true为已关闭，返回false为未关闭
	 */
	public boolean isPageClose(){
		return isPageClose;
	}
	public void initHead(boolean isShowView, OnClickListener backListener, boolean isNeedLogo, String title,boolean showback,boolean showrighticon,int resid){
		View head = findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head"));
		if(head == null)
			return;
		if(isShowView)
			head.setVisibility(View.VISIBLE);
		else
			head.setVisibility(View.GONE);
		
		View back = findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_back"));
		if(showback){
			back.setVisibility(View.VISIBLE);
			if(null != backListener)
				back.setOnClickListener(backListener);
			else
				back.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						finish();
					}
				});
		}else{
			back.setVisibility(View.GONE);
		}
		
		TextView tv_logo = (TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_logo"));
		if(isNeedLogo){
			tv_logo.setVisibility(View.VISIBLE);
		}else
			tv_logo.setVisibility(View.GONE);
		
		TextView tv_title = (TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_title"));
		tv_title.setText(title);
		
		if(showrighticon){
			findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_function")).setVisibility(View.VISIBLE);
			if(resid!=0)
				findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_function_icon")).setBackgroundResource(resid);
		}else{
			findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_function")).setVisibility(View.GONE);
		}
		
//		if(showback){
//			findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_back")).setVisibility(View.VISIBLE);
//		}else{
//			findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_back")).setVisibility(View.GONE);	
//		}
//		if(showrighticon){
//			findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_function")).setVisibility(View.VISIBLE);
//			if(resid!=0)
//			findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_function_icon")).setBackgroundResource(resid);
//		}else{
//			findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_function")).setVisibility(View.GONE);
//		}
//		initHead(isShowView, backListener, isNeedLogo, title);
	}
	
	public void initHead(boolean isShowView, OnClickListener backListener, boolean isNeedLogo, String title){
		
		
		initHead(isShowView, null, isNeedLogo, title, true, false, 0);
		
//		View head = findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head"));
//		if(head == null)
//			return;
//		if(isShowView)
//			head.setVisibility(View.VISIBLE);
//		else
//			head.setVisibility(View.GONE);
//		View back = findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_back"));
//		if(null != backListener)
//			back.setOnClickListener(backListener);
//		else
//			back.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View arg0) {
//					finish();
//				}
//			});
//		
//		TextView tv_logo = (TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_logo"));
//		if(isNeedLogo){
//			tv_logo.setVisibility(View.VISIBLE);
//		}else
//			tv_logo.setVisibility(View.GONE);
//		
//		TextView tv_title = (TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_title"));
//		tv_title.setText(title);
	}
	public void setHeadTitle(String title){
		TextView tv_title = (TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_title"));
		tv_title.setText(title);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
//		BaseUtils.logError("---", "-----onResume");
		checkUserInfoIsNull();
		OASISPlatform.trackOnResume(this);
		isPageClose = false;
	}
	@Override
	protected void onPause() {
		super.onPause();
		OASISPlatform.trackOnPause(this);
	}
	@Override
	protected void onDestroy() {
		isPageClose = true;
		super.onDestroy();
	}
	
	private void checkUserInfoIsNull(){
		if(BaseUtils.isLogin())
			return;
//		BaseUtils.logError("---", "-----checkUserInfoIsNull");
		BaseUtils.parseJsonToUserinfo();
	}
}
