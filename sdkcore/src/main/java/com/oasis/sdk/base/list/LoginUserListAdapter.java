package com.oasis.sdk.base.list;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oasis.sdk.activity.OasisSdkLoginActivity;
import com.oasis.sdk.base.entity.MemberBaseInfo;
import com.oasis.sdk.base.utils.BaseUtils;

/**
 * 已登录用户列表，用于登录页面展示
 * @author Administrator
 *
 */
public class LoginUserListAdapter extends BaseListAdapter<MemberBaseInfo>{
	OasisSdkLoginActivity c;
	public LoginUserListAdapter(Activity activity, List<MemberBaseInfo> data,
			int count, LinearLayout footerView) {
		super(activity, data, count, footerView);
		this.c = (OasisSdkLoginActivity)activity;
	}

	@Override
	public void loadMore() {
		// no more
	}

	@Override
	public View getRowView(int position, View convertView, ViewGroup parent) {
		ViewHoder hoder = null;
		if(null == convertView){
			convertView = c.getLayoutInflater().inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_login_login_user_item"), null);
			hoder = new ViewHoder();
			hoder.name = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_user_item_name"));
			hoder.delete = (LinearLayout) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_user_item_delete"));
			
			convertView.setTag(hoder);
		}else{
			hoder = (ViewHoder) convertView.getTag();
		}
		
		final MemberBaseInfo info = getItem(position);
		hoder.name.setText(info.memberName);
		hoder.delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog(c, info);
				
			}
		});
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				c.showUserInfo(info);
			}
		});
		return convertView;
	}

	static class ViewHoder{
		TextView name;
		LinearLayout delete;
	}
	
	public static void dialog(final OasisSdkLoginActivity a, final MemberBaseInfo info) {
		final AlertDialog d = new AlertDialog.Builder(a).create();
		d.show();
		d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_dialog_notitle"));
		
		TextView content = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_content"));
		content.setText(a.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_userlist_dialog_content")));
		
		
//		String btnSure = a.getResources().getString(BaseUtils.getResourceValue(a, "string", "oasisgames_sdk_common_btn_sure"));
//		String btnCancle = a.getResources().getString(BaseUtils.getResourceValue(a, "string", "oasisgames_sdk_common_btn_cancle"));
		
		TextView sure = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_sure"));
//		sure.setText(btn1);
//		if(null != click1)
			sure.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					d.dismiss();
					Message msg = new Message();
					msg.what = 100;
					msg.obj = info;
					a.myHandler.sendMessage(msg);
				}
			});

			TextView cancle = (TextView) d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_cancle"));
		cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});
		
		
		
	}
}
