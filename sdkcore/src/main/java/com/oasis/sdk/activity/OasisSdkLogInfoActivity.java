package com.oasis.sdk.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.Constant;
import com.oasis.sdk.base.utils.SystemCache;
/**
 * 日志信息
 * @author xdb
 *
 */
public class OasisSdkLogInfoActivity extends OasisSdkBaseActivity {
	public static final String TAG = OasisSdkLogInfoActivity.class.getSimpleName();
	TextView tv_baseInfo, tv_logInfo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_log"));
		
		TextView btn_unbind = (TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_log_unbind"));
		btn_unbind.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 弹出提示
				showUnbindNotice();
			}
		});
		((TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_log_close"))).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		TextView tv_baseInfo = (TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_log_baseinfo"));
		StringBuffer baseInfo = new StringBuffer();
		baseInfo.append("<B>SDK Version</B>:"+Constant.SDKVERSION);
		baseInfo.append("<br><B>IMEI</B>:"+BaseUtils.getMobileCode());
		if(SystemCache.localInfo != null){
			baseInfo.append("<br><B>UID</B>:"+SystemCache.localInfo.uid);
			baseInfo.append("<br><B>UserName</B>:");
			baseInfo.append("<br><B>RoleID</B>:");
			baseInfo.append("<br><B>ServerID</B>:");
			baseInfo.append("<br><B>ServerName</B>:");
			baseInfo.append("<br><B>ServerType</B>:");
			
		}else{
			baseInfo.append("<br><B>UID</B>:"+SystemCache.userInfo.uid);
			baseInfo.append("<br><B>UserName</B>:"+SystemCache.userInfo.username);
			baseInfo.append("<br><B>RoleID</B>:"+SystemCache.userInfo.roleID);
			baseInfo.append("<br><B>ServerID</B>:"+SystemCache.userInfo.serverID);
			baseInfo.append("<br><B>ServerName</B>:"+SystemCache.userInfo.serverName);
			baseInfo.append("<br><B>ServerType</B>:"+SystemCache.userInfo.serverType);
			
		}
		tv_baseInfo.setText(Html.fromHtml(baseInfo.toString()));
		
		TextView tv_logInfo = (TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_log_loginfo"));
		StringBuffer logInfo = null;
		for (String str : SystemCache.logLists) {
			if(logInfo == null){
				logInfo = new StringBuffer();
			}else{
				logInfo.append("<br>");	
			}
			logInfo.append(str);			
		}
		if(logInfo != null)
//			tv_logInfo.setText(logInfo.toString());
			tv_logInfo.setText(Html.fromHtml(logInfo.toString()));
		else
			tv_logInfo.setText("Don't log");
		
		setWaitScreen(false);
	}
	private void showUnbindNotice(){
		final AlertDialog d = new AlertDialog.Builder(this).create();
		d.show();
		d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_dialog_notitle"));
		d.setCanceledOnTouchOutside(false);
		
		TextView tv_content = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_content"));
		tv_content.setText(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_sandbox_unbind_notice1")));
		
		TextView tv_sure = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_sure"));
		tv_sure.setText(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_sandbox_unbind_notice2")));
			
		tv_sure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				d.dismiss();
				BaseUtils.clearALL();
				BaseUtils.clearUserinfo();
				SystemCache.isExit = true;
				finish();
			}
		});
		TextView tv_cancle = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_cancle"));
		tv_cancle.setText(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_common_btn_cancle")));
		tv_cancle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});
		
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
