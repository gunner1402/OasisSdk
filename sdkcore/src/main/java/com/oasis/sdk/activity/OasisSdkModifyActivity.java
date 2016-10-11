package com.oasis.sdk.activity;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oasis.sdk.base.Exception.OasisSdkDataErrorException;
import com.oasis.sdk.base.Exception.OasisSdkException;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.BaseUtils;
/**
 * 修改密码
 * @author Administrator
 *
 */
public class OasisSdkModifyActivity extends OasisSdkBaseActivity {
	public static final String TAG = OasisSdkModifyActivity.class.getName();
	
	EditText et_oldpw, et_newpw, et_newrepw;
	LinearLayout ll_oldpw, ll_newpw, ll_newrepw;
	TextView tv_submit, tv_c_oldpw, tv_c_newpw, tv_c_newrepw;
	
	String oldpw = "", newpw = "", newrepw = "";
	
	
	// 声明一个Handler对象
	public MyHandler myHandler = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_modify"));
		
		myHandler = new MyHandler(this);
		
		initHead(true, null, false, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pcenter_notice_3")));
		
		init();
		
		setOnClickListener();
		
		setWaitScreen(false);
	}
	private void init(){
		et_oldpw = (EditText)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_modify_oldpw"));
		et_newpw = (EditText)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_modify_newpw"));
		et_newrepw = (EditText)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_modify_newrepw"));
		
		ll_oldpw = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_modify_oldpw_clean"));
		ll_newpw = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_modify_newpw_clean"));
		ll_newrepw = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_modify_newrepw_clean"));
		
		tv_c_oldpw = (TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_modify_oldpw_clean_img"));
		tv_c_newpw = (TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_modify_newpw_clean_img"));
		tv_c_newrepw = (TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_modify_newrepw_clean_img"));
		
		tv_submit = (TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_modify_submit"));
	}
	private void setOnClickListener(){
		et_oldpw.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				if(arg0.length() > 0){
					ll_oldpw.setVisibility(View.VISIBLE);
					tv_c_oldpw.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_common_input_bg_clean_blue"));
				}
				else
					ll_oldpw.setVisibility(View.INVISIBLE);
			}
		});
		
		ll_oldpw.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				et_oldpw.setText("");
			}
		});
		et_newpw.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				if(arg0.length() > 0){
					ll_newpw.setVisibility(View.VISIBLE);
					tv_c_newpw.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_common_input_bg_clean_blue"));
				}else
					ll_newpw.setVisibility(View.INVISIBLE);
			}
		});
		ll_newpw.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				et_newpw.setText("");
			}
		});
		et_newrepw.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				if(arg0.length() > 0){
					ll_newrepw.setVisibility(View.VISIBLE);
					tv_c_newrepw.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_common_input_bg_clean_blue"));
				}else
					ll_newrepw.setVisibility(View.INVISIBLE);
			}
		});
		ll_newrepw.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				et_newrepw.setText("");
			}
		});
		tv_submit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(checkUserInfo())
					modify();
			}
		});
	}
	
	
	/**
	 * 各数据项合法性验证
	 * @return
	 */
	private boolean checkUserInfo(){
		oldpw = et_oldpw.getText().toString().trim();
		newpw = et_newpw.getText().toString().trim();
		newrepw = et_newrepw.getText().toString().trim();
		if(TextUtils.isEmpty(oldpw)){
			BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_modify_4")));// 请输入原密码
			return false;
		}
//		if(oldpw.length() < 6 || oldpw.length() > 20){
//			BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_password_notice_error")));
//			return false;
//		}
		if(TextUtils.isEmpty(newpw)){
			BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_modify_5")));// 请输入新密码
			return false;
		}
		if(newpw.length() < 6 || newpw.length() > 20){
			BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_password_notice_error")));
			return false;
		}
		if(!BaseUtils.regexSpecilChar(newpw)){
			BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_password_notice_error2")));
			return false;
		}
		if(!newpw.equals(newrepw)){
			BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_modify_6")));
			return false;
		}

		return true;
	}
	private void modify(){
		setWaitScreen(true);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Object[] res = HttpService.instance().modifyPwd(oldpw, newpw, newrepw);
					if((Boolean)res[0])
						myHandler.sendEmptyMessage(HANDLER_RESULT);
					else{
						Message msg = new Message();
						msg.what = HANDLER_FAIL;
						msg.arg1 = (Integer) res[1];
						myHandler.sendMessage(msg);
					}
				} catch (OasisSdkException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION_NETWORK);
				} catch (OasisSdkDataErrorException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				}
			}
		}).start();
	}
	static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkModifyActivity> mOuter;

		public MyHandler(OasisSdkModifyActivity activity) {
			mOuter = new WeakReference<OasisSdkModifyActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			OasisSdkModifyActivity outer = mOuter.get();
			if (outer != null) {
				switch (msg.what) {
				
				case HANDLER_RESULT:
					outer.setWaitScreen(false);
					outer.myHandler.sendEmptyMessage(HANDLER_SUCECCES);
					break;
				case HANDLER_SUCECCES:
					BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_modify_7")));
					outer.finish();
					break;
				case HANDLER_FAIL:
					if(msg.arg1 == -4)
						BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_modify_8")));
					else 
						BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_common_errorcode_negative_999"))+"  "+msg.arg1);
					outer.setWaitScreen(false);
					break;
				case HANDLER_EXCEPTION:
					outer.setWaitScreen(false);
					BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_error_exception")));
					break;
				case HANDLER_EXCEPTION_NETWORK:
					outer.setWaitScreen(false);
					BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_autologin_exception")));
					break;
				
				default:
					
					break;
				}
			}
		}
	}

}
