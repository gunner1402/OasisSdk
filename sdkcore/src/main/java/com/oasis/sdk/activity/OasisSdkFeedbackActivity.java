package com.oasis.sdk.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.base.http.CallbackResultForActivity;
import com.oasis.sdk.base.entity.MemberBaseInfo;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.SystemCache;
/**
 * 被封禁时，提供反馈机制
 * @author xdb
 *
 */
public class OasisSdkFeedbackActivity extends OasisSdkBaseActivity {
	public static final String TAG = OasisSdkFeedbackActivity.class.getName();
	
	int type;// 来源类型	0：反馈结束退出应用	1：反馈结束关闭当前界面，不影响其他操作
	String error;// -13:设备被封 	-14：UID被封
	private TextView tv_notice;
	
	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_feedback"));
		
		String sFinal = "";
		if("-14".equals(SystemCache.bindInfo.error) || "-15".equals(SystemCache.bindInfo.error)
				|| "-16".equals(SystemCache.bindInfo.error)){
			String baseStr = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_feedback_notice_account"));
			String username = "";
			if(MemberBaseInfo.USER_OASIS.equals(SystemCache.bindInfo.platform)
					|| MemberBaseInfo.USER_GOOGLE.equals(SystemCache.bindInfo.platform))
				username = SystemCache.bindInfo.username;
			else if(MemberBaseInfo.USER_FACEBOOK.equals(SystemCache.bindInfo.platform))
				username = SystemCache.bindInfo.oasnickname;
			else if(MemberBaseInfo.USER_NONE.equals(SystemCache.bindInfo.platform))
				username = getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_feedback_notice_account1"));
			
			if(!TextUtils.isEmpty(username))
				username = "<font color='#00afd9'>"+username+"</font>";						
			sFinal = String.format(baseStr, username);
		}else if("-13".equals(SystemCache.bindInfo.error)){
			sFinal = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_feedback_notice_device"));
		}
		tv_notice = (TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_feedback_view_notice"));
		tv_notice.setText(Html.fromHtml(sFinal));
		
		type = getIntent().getIntExtra("type", 0);
	}

	public void buttonOnClick(View v) {
		if (v.getId() == BaseUtils.getResourceValue("id", "oasisgames_sdk_feedback_view_submit")){// 反馈信息提交
			String email = ((EditText)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_feedback_view_email"))).getText().toString();
			String descrip = ((EditText)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_feedback_view_discrip"))).getText().toString();
			if(email == null || TextUtils.isEmpty(email) || !BaseUtils.regexEmail(email)){
				BaseUtils.showMsg(this, getResources().getString(
						BaseUtils.getResourceValue("string", "oasisgames_sdk_feedback_submit_error_email")));
				return;
			}
			if(descrip == null || TextUtils.isEmpty(descrip)){
				BaseUtils.showMsg(this, getResources().getString(
						BaseUtils.getResourceValue("string", "oasisgames_sdk_feedback_submit_error_descrip")));
				return;
			}
			
			// 关闭可能显示的键盘
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if (v != null)
				imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			
			setWaitScreen(true);
			HttpService.instance().feedback(email, descrip, new CallbackResultForActivity() {
				
				@Override
				public void success(Object data, String statusCode, String msg) {
					setWaitScreen(false);
					findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_feedback_view")).setVisibility(View.INVISIBLE);
					
					final AlertDialog d = new AlertDialog.Builder(OasisSdkFeedbackActivity.this).create();
					d.show();
					d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_dialog_notitle"));
					
					TextView tv_content = (TextView) d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_content"));
					TextView btn_sure = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_sure"));
					
					tv_content.setText(BaseUtils.getResourceValue("string", "oasisgames_sdk_feedback_submit_success"));
					btn_sure.setText(BaseUtils.getResourceValue("string", "oasisgames_sdk_common_btn_sure"));
				
					btn_sure.setOnClickListener(new View.OnClickListener(){
						
						@Override
						public void onClick(View v) {
							d.dismiss();
							if("-13".equals(SystemCache.userInfo.error) || type == 0 )
								SystemCache.isExit = true;
					    	finish();
						}
					});
					TextView btn_cancle = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_cancle"));
					btn_cancle.setVisibility(View.GONE);
				}
				
				@Override
				public void fail(String statusCode, String msg) {
					setWaitScreen(false);
					BaseUtils.showMsg(OasisSdkFeedbackActivity.this, getResources().getString(
							BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_autologin_exception")));
				}
				
				@Override
				public void excetpion(Exception e) {
					setWaitScreen(false);
					BaseUtils.showMsg(OasisSdkFeedbackActivity.this, getResources().getString(
							BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_autologin_exception")));
				}
			});
			return;
		}
		if(v.getId() == BaseUtils.getResourceValue("id", "oasisgames_sdk_feedback_view_close")){
			if("-13".equals(SystemCache.userInfo.error) || type == 0 )
				SystemCache.isExit = true;
				
			finish();
			return;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if("-13".equals(SystemCache.userInfo.error) || type == 0 )
				SystemCache.isExit = true;
				
			finish();
		}
		
		return true;
	}
	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
}
