package com.oasis.sdk.base.list;

import java.util.List;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oasis.sdk.activity.OasisSdkLoginActivity;
import com.oasis.sdk.base.entity.MemberBaseInfo;
import com.oasis.sdk.base.entity.RecentUser;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.InternationalUtil;

public class LoginHistoryAdapter extends BaseListAdapter {
	OasisSdkLoginActivity c;
	public LoginHistoryAdapter(Activity activity, List<RecentUser> data, int count, LinearLayout footerView) {
		super(activity, data, count, footerView);
		this.c = (OasisSdkLoginActivity)activity;
	}

	@Override
	public void loadMore() {

	}

	@Override
	public View getRowView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(null == convertView){
			convertView = c.getLayoutInflater().inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_login_history_item"), null);
			holder = new ViewHolder();
			holder.history_account_icon = (ImageView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_history_icon"));
			holder.history_user_account = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_history_account"));
			holder.history_account_notice = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_history_account_notice"));
			holder.history_login_recent = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_history_recent_date"));
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		RecentUser recentUser = (RecentUser) getItem(position);
		if(MemberBaseInfo.USER_FACEBOOK.equals(recentUser.platform)){
			holder.history_account_notice.setText(c.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_history_nickname")));
			holder.history_account_icon.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_login_button_facebook"));
			holder.history_user_account.setText(TextUtils.isEmpty(recentUser.oasnickname)?MemberBaseInfo.USER_FACEBOOK:recentUser.oasnickname);
		}else if(MemberBaseInfo.USER_GOOGLE.equals(recentUser.platform)){
			holder.history_account_notice.setText(c.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_username_text"))+": ");
			holder.history_account_icon.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_login_button_google"));
			holder.history_user_account.setText(TextUtils.isEmpty(recentUser.username)?MemberBaseInfo.USER_GOOGLE:recentUser.username);
		}else if(MemberBaseInfo.USER_OASIS.equals(recentUser.platform)){
			holder.history_account_notice.setText(c.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_username_text"))+": ");
			holder.history_account_icon.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_login_button_oas"));
			holder.history_user_account.setText(TextUtils.isEmpty(recentUser.username)?MemberBaseInfo.USER_OASIS:recentUser.username);
		}else if(MemberBaseInfo.USER_NONE.equals(recentUser.platform)){
			holder.history_account_notice.setText(c.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pcenter_notice_10")));
			holder.history_account_icon.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_login_button_oas"));
			holder.history_user_account.setText(c.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pcenter_notice_10")));
		}
		holder.history_login_recent.setText(InternationalUtil.InternationalDateFormat(c, Long.parseLong(recentUser.time+"000")));
		return convertView;
	}
	static class ViewHolder{
		ImageView history_account_icon;
		TextView history_user_account,history_account_notice,history_login_recent;
	}
}
