package com.oasis.sdk.base.list;

import java.util.List;

import com.oasis.sdk.activity.OasisSdkLoginActivity;
import com.oasis.sdk.base.entity.RecentUserGameInfo;
import com.oasis.sdk.base.list.LoginHistoryAdapter.ViewHolder;
import com.oasis.sdk.base.utils.BaseUtils;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HistoryRoleInfoAdapter extends BaseListAdapter {
	OasisSdkLoginActivity c;
	public HistoryRoleInfoAdapter(Activity activity, List<RecentUserGameInfo> data, int count, LinearLayout footerView) {
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
			convertView = c.getLayoutInflater().inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_login_history_roleinfo_item"), null);
			holder = new ViewHolder();
			holder.role_server_id = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_role_server_id"));
			holder.role_level = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_role_level"));
			holder.role_name = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_role_name"));
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		RecentUserGameInfo roleInfo = (RecentUserGameInfo) getItem(position);
		if(!TextUtils.isEmpty(roleInfo.serverName)){
			holder.role_server_id.setText(roleInfo.serverName + "("+roleInfo.serverId+")");
		}else{
			holder.role_server_id.setText("-");
		}
		if(!TextUtils.isEmpty(roleInfo.roleName)){
			holder.role_name.setText(roleInfo.roleName);
		}else{
			holder.role_name.setText("-");
		}
		if(!TextUtils.isEmpty(roleInfo.level)){
			holder.role_level.setText(roleInfo.level);
		}else{
			holder.role_level.setText("-");
		}
		return convertView;
	}
//	oasisgames_sdk_login_history_roleinfo_item
	static class ViewHolder{
		TextView role_server_id,role_level,role_name;
	}
}
