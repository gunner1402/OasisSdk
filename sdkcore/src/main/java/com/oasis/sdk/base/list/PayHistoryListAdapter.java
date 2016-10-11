package com.oasis.sdk.base.list;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oasis.sdk.activity.OasisSdkPayHistoryActivity;
import com.oasis.sdk.base.entity.PayHistoryInfoDetail;
import com.oasis.sdk.base.entity.PhoneInfo;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.InternationalUtil;

/**
 * 支付套餐列表
 * @author Administrator
 *
 */
public class PayHistoryListAdapter extends BaseListAdapter<PayHistoryInfoDetail>{
	OasisSdkPayHistoryActivity c;
	public PayHistoryListAdapter(Activity activity, List<PayHistoryInfoDetail> data,
			int count, LinearLayout footerView) {
		super(activity, data, count, footerView);
		this.c = (OasisSdkPayHistoryActivity)activity;
	}

	@Override
	public void loadMore() {
		c.loadData();
	}

	@Override
	public View getRowView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(null == convertView){
			convertView = c.getLayoutInflater().inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_pay_history_item"), null);
			holder = new ViewHolder();
			holder.copy = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_history_item_copy"));
			holder.orderid = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_history_item_orderid"));
			holder.price = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_history_item_price"));
			holder.coins = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_history_item_coins"));
			holder.payway = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_history_item_payway"));
			holder.orderStatus = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_history_item_orderstatus"));
			holder.time = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_history_item_time"));
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		final PayHistoryInfoDetail info = getItem(position);
		String ordernum = c.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_history_8"));
		holder.orderid.setText(ordernum+": "+(TextUtils.isEmpty(info.third_orderid)?info.oas_orderid:info.third_orderid));
		
//		Configuration conf = c.getResources().getConfiguration();
		Locale locale = new Locale(PhoneInfo.instance().locale);
		NumberFormat nf = NumberFormat.getNumberInstance(locale);
		
		holder.copy.setOnClickListener(new OnClickListener() {
			
			@SuppressWarnings("deprecation")
			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@Override
			public void onClick(View v) {
				// 为了兼容低版本我们这里使用旧版的android.text.ClipboardManager，虽然提示deprecated，但不影响使用。
		        ClipboardManager cm = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
		        // 将文本内容放到系统剪贴板里。
		        cm.setText((TextUtils.isEmpty(info.third_orderid)?info.oas_orderid:info.third_orderid));
		        BaseUtils.showMsg(c, c.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_history_10")));
			}
		});
		try {
			holder.price.setText(": "+info.currency + nf.format(Double.valueOf(info.amount)));
		} catch (Exception e) {
			holder.price.setText(": "+info.currency + info.amount);
		}
		String status = c.getString(BaseUtils.getResourceValue("string", "0".equals(info.sendcoins_flag)?"oasisgames_sdk_pay_history_12":"oasisgames_sdk_pay_history_11"));
		status = ": " + ("0".equals(info.sendcoins_flag)?"<font color='#FF9900'>":"<font color='#009900'>")+status+"</font>";
		holder.orderStatus.setText(Html.fromHtml(status));
		holder.payway.setText(": "+info.pay_way_display);
		int addCoins = 0;
		try {
			if(!TextUtils.isEmpty(info.rewards)){
				addCoins = Integer.parseInt(info.rewards);
			}
		} catch (NumberFormatException e) {
			addCoins = 0;
		}
		int baseCoins = 0;
		try {
			if(!TextUtils.isEmpty(info.game_coins)){
				baseCoins = Integer.parseInt(info.game_coins);
			}
		} catch (NumberFormatException e) {
			baseCoins = 0;
		}
		holder.coins.setText(": "+nf.format(baseCoins) + (addCoins>0?("   + "+nf.format(addCoins)):""));
		
		if(!TextUtils.isEmpty(info.placed_time)){
			long time = Long.valueOf(info.placed_time)*1000;
			holder.time.setText(": "+InternationalUtil.InternationalDateFormat(c, time));
		}
		return convertView;
	}

	static class ViewHolder{
		TextView copy;					// 复制
		TextView orderid;				// 订单号
		TextView price;					// 套餐价格
		TextView coins;					// 获得商品数量
		TextView payway;				// 支付渠道
		TextView orderStatus;			// 订单状态
		TextView time;					// 支付日期
	}
}
