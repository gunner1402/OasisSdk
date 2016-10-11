package com.oasis.sdk.base.list;

import java.util.List;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oasis.sdk.activity.OasisSdkPayActivity;
import com.oasis.sdk.base.entity.PayInfoList;
import com.oasis.sdk.base.utils.BaseUtils;

/**
 * 支付渠道列表
 * @author xdb
 *
 */
public class PayWayListAdapter extends BaseListAdapter<PayInfoList>{
	OasisSdkPayActivity c;
	public PayWayListAdapter(Activity activity, List<PayInfoList> data,
			int count, LinearLayout footerView) {
		super(activity, data, count, footerView);
		this.c = (OasisSdkPayActivity)activity;
	}

	@Override
	public void loadMore() {
		// no more
	}

	@Override
	public View getRowView(int position, View convertView, ViewGroup parent) {
		ViewHoder hoder = null;
		if(null == convertView){
			convertView = c.getLayoutInflater().inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_payway_item"), null);
			hoder = new ViewHoder();
			hoder.paywaybg = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_payway_item_bg"));
			hoder.isselected = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_payway_item_selected"));
			
			convertView.setTag(hoder);
		}else{
			hoder = (ViewHoder) convertView.getTag();
		}
		
		final PayInfoList info = getItem(position);
		if(null != c.curPayInfoList && c.curPayInfoList.pay_way.equals(info.pay_way)){
			hoder.isselected.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_payway_selected"));
		}else{
			if(Build.VERSION.SDK_INT >= 16)
				hoder.isselected.setBackground(null);
			else
				hoder.isselected.setBackgroundDrawable(null);
		}
		
		hoder.paywaybg.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_payway_"+info.pay_way));
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				c.updatePayWay(info);
			}
		});
		
		return convertView;
	}

	static class ViewHoder{
		TextView paywaybg;
		TextView isselected;
	}
}
