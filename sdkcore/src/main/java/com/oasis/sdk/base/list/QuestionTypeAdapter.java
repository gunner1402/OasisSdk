package com.oasis.sdk.base.list;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oasis.sdk.activity.OasisSdkCustomerServiceListActivity;
import com.oasis.sdk.base.entity.QuestionType;
import com.oasis.sdk.base.utils.BaseUtils;

/**
 * 问题类型适配器
 * @author Administrator
 *
 */
public class QuestionTypeAdapter extends BaseListAdapter<QuestionType>{
	OasisSdkCustomerServiceListActivity c;
	public QuestionTypeAdapter(Activity activity, List<QuestionType> data,
			int count, LinearLayout footerView) {
		super(activity, data, count, footerView);
		this.c = (OasisSdkCustomerServiceListActivity)activity;
	}

	@Override
	public void loadMore() {
		// no more
	}

	@Override
	public View getRowView(int position, View convertView, ViewGroup parent) {
		ViewHoder holder = null;
		if(null == convertView){
			convertView = c.getLayoutInflater().inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_customer_service_type_item"), null);
			holder = new ViewHoder();
			holder.title = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_type_item_title"));
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHoder) convertView.getTag();
		}
		
		final QuestionType qt = getItem(position);

		holder.title.setText(qt.name);
		
		return convertView;
	}

	static class ViewHoder{
		TextView title;
	}
}
