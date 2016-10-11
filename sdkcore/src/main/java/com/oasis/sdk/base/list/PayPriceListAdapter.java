package com.oasis.sdk.base.list;

import java.text.NumberFormat;
import java.util.List;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oasis.sdk.activity.OasisSdkPayActivity;
import com.oasis.sdk.base.entity.PayInfoDetail;
import com.oasis.sdk.base.utils.BaseUtils;

/**
 * 支付套餐列表
 * @author Administrator
 *
 */
public class PayPriceListAdapter extends BaseListAdapter<PayInfoDetail>{
	OasisSdkPayActivity c;
	public PayPriceListAdapter(Activity activity, List<PayInfoDetail> data,
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
		ViewHolder holder = null;
		if(null == convertView){
			convertView = c.getLayoutInflater().inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_pay_item"), null);
			holder = new ViewHolder();
			holder.price = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_item_price"));
			holder.coins = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_item_d"));
			holder.addCoinsLayout = (LinearLayout) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_item_addcoins"));
			holder.addCoins = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_item_d2"));
			holder.connect = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_item_connect"));
			holder.hot = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_item_hot"));
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		final PayInfoDetail info = getItem(position);
		if(c.selectedPayInfo == info){
			holder.price.setTextColor(Color.parseColor("#00aed9"));
			holder.coins.setTextColor(Color.parseColor("#00aed9"));
			holder.addCoins.setTextColor(Color.parseColor("#00aed9"));
			convertView.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_pay_item_bg_focus"));
		}else{
			holder.price.setTextColor(Color.parseColor("#666666"));
			holder.coins.setTextColor(Color.parseColor("#666666"));
			holder.addCoins.setTextColor(Color.parseColor("#666666"));
			convertView.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_pay_item_bg_unfocus"));
		}
		Configuration conf = c.getResources().getConfiguration();
		NumberFormat nf = NumberFormat.getNumberInstance(conf.locale);
		try {
			holder.price.setText(info.currency_show + nf.format(Double.valueOf(info.amount_show)));
		} catch (Exception e) {
			holder.price.setText(info.currency_show + info.amount_show);
		}
		int addCoins = 0;
		try {
			if(!TextUtils.isEmpty(info.price_discount)){
				addCoins = Integer.parseInt(info.price_discount);
			}
		} catch (NumberFormatException e) {
			addCoins = 0;
		}
		try {
			holder.coins.setText(nf.format(Long.valueOf(info.game_coins_show)));			
		} catch (Exception e) {
			holder.coins.setText(info.game_coins_show);
		}
		
		if(addCoins > 0){
			holder.addCoins.setText(nf.format(addCoins));
			holder.addCoinsLayout.setVisibility(View.VISIBLE);
			holder.connect.setVisibility(View.VISIBLE);
		}else{
			holder.addCoinsLayout.setVisibility(View.GONE);
			holder.connect.setVisibility(View.INVISIBLE);
		}
		
		if(info.best.startsWith("y") || info.best.startsWith("Y"))
			holder.hot.setVisibility(View.VISIBLE);
		else
			holder.hot.setVisibility(View.INVISIBLE);
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				c.updateUI(info);
			}
		});
		
		return convertView;
	}

	static class ViewHolder{
		TextView price;					// 套餐价格
		TextView coins;					// 蓝钻
		LinearLayout addCoinsLayout;	// 赠送钻石布局
		TextView addCoins;				// 赠送钻石
		TextView connect;				// 蓝钻与红钻之间的链接符
		TextView hot;					// 推荐套餐
	}
}
