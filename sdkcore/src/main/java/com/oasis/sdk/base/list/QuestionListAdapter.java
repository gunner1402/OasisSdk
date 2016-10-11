package com.oasis.sdk.base.list;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oasis.sdk.activity.OasisSdkCustomerServiceListActivity;
import com.oasis.sdk.base.entity.QuestionInfo;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.InternationalUtil;

/**
 * 问题列表 适配器
 * 
 * @author Administrator
 *
 */
public class QuestionListAdapter extends BaseListAdapter<QuestionInfo> {
	OasisSdkCustomerServiceListActivity c;

	public QuestionListAdapter(Activity activity, List<QuestionInfo> data, int count, LinearLayout footerView) {
		super(activity, data, count, footerView,
				BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_listview_foot_more"), 0);
		this.c = (OasisSdkCustomerServiceListActivity) activity;
	}

	@Override
	public void loadMore() {
		// no more
		// c.loadData();
	}

	@Override
	public View getRowView(int position, View convertView, ViewGroup parent) {
		ViewHoder holder = null;
		if (null == convertView) {
			convertView = c.getLayoutInflater()
					.inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_customer_service_list_item"), null);
			holder = new ViewHoder();
			// hoder.price = (ImageView)
			// convertView.findViewById(BaseUtils.getResourceValue("id",
			// "oasisgames_sdk_customer_service_list_item_img"));
			// holder.price2 = (com.mopub.volley.toolbox.NetworkImageView)
			// convertView.findViewById(BaseUtils.getResourceValue("id",
			// "oasisgames_sdk_customer_service_list_item_img"));
			holder.title = (TextView) convertView
					.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_list_item_title"));
			holder.time = (TextView) convertView
					.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_list_item_time"));
			holder.num = (TextView) convertView
					.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_list_item_other"));

			convertView.setTag(holder);
		} else {
			holder = (ViewHoder) convertView.getTag();
		}

		final QuestionInfo info = getItem(position);

		// 方式1：采用ImageView
		// ImageListener listener = ImageLoader.getImageListener(hoder.price,
		// android.R.drawable.ic_menu_rotate, android.R.drawable.ic_delete);
		// c.mImageLoader.get(url, listener);

		// 方式2：采用NetworkImageView
		// holder.price2.setDefaultImageResId(android.R.drawable.ic_menu_rotate);
		// holder.price2.setErrorImageResId(android.R.drawable.ic_delete);
		// holder.price2.setImageUrl(url, c.mImageLoader);

		String type = "[" + info.question_type_name + "]";
		String title = info.content;
		// if(position % 2 == 0) {
		// title += title;
		// }
		holder.title.setText(type + title);
		SpannableStringBuilder builder = new SpannableStringBuilder(holder.title.getText().toString());
		ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.parseColor("#00aed9"));
		ForegroundColorSpan whiteSpan = new ForegroundColorSpan(Color.parseColor("#666666"));

		builder.setSpan(redSpan, 0, type.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		builder.setSpan(whiteSpan, type.length(), type.length() + (TextUtils.isEmpty(title) ? 0 : title.length()),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		holder.title.setText(builder);

		// String text = "<h3 class=\"t c-gap-bottom-small\"> <a
		// href=\"http://www.baidu.com/link?url=zzgueloCsQ21_4voaXXPK8_SxqXSvNBY57NM7esESAnhQ8e56TWF_UyEimN3YdZaNgkVBpvOtzBA7tmOOfclLK&amp;wd=&amp;eqid=be170e7c00000a860000000455939bfe\"
		// target=\"_blank\">..._百度百科</a>"
		// +"</h3><p> 英文中的省略号、运算符等含义，表示无奈，也可当作省略符使用。1、英文中的省略号。
		// 2、DOS和UNIX中表示父目录的语法。单个圆点表示当前目录。 3、在Windows操...</p><p>"
		// +" <a class=\"c-gap-right-small op-se-listen-recommend\"
		// href=\"http://www.baidu.com/link?url=4uuAW1RM6F6UEjRZE1uTp_Pfoou6mK_wjQeRLiELfoa7UsS4YYBspKpc-qWG2apIOfpq_Qa-PnMLy41fcDUxwa\"
		// target=\"_blank\" title=\"用法\">用法</a> <a class=\"c-gap-right-small
		// op-se-listen-recommend\"
		// href=\"http://www.baidu.com/link?url=4uuAW1RM6F6UEjRZE1uTp_Pfoou6mK_wjQeRLiELfoa7UsS4YYBspKpc-qWG2apI5-JfXXpEBrP2eFD3sYoxp_\"
		// target=\"_blank\" title=\"C语言中的元运算符\">C语言中的元运算符</a> <a
		// class=\"c-gap-right-small op-se-listen-recommend\"
		// href=\"http://www.baidu.com/link?url=4uuAW1RM6F6UEjRZE1uTp_Pfoou6mK_wjQeRLiELfoa7UsS4YYBspKpc-qWG2apI3ljpM_D5jzEpuQgbYJf_2q\"
		// target=\"_blank\" title=\"C++中的元运算符\">C++中的元运算符</a> <a
		// class=\"c-gap-right-small op-se-listen-recommend\"
		// href=\"http://www.baidu.com/link?url=4uuAW1RM6F6UEjRZE1uTp_Pfoou6mK_wjQeRLiELfoa7UsS4YYBspKpc-qWG2apIbkV4u32C9lFk2twRyq3Uua\"
		// target=\"_blank\" title=\"Java中可变参数的符号\">Java中可变参数的符号</a>"
		// +"</p><img src=\"https://www.baidu.com/img/bd_logo1.png\" alt=\"\"
		// border=\"0\" />"
		// +"</p><img
		// src=\"http://www.yzdsb.com.cn/pic/0/11/65/53/11655321_951970.jpg\"
		// alt=\"\" border=\"0\" />";
		// //http://www.yzdsb.com.cn/pic/0/11/65/53/11655321_951970.jpg
		// holder.title.setText(Html.fromHtml(text));


			holder.time.setText(InternationalUtil.InternationalDateFormat(c, Long.valueOf(info.create_time) * 1000));
		if (TextUtils.isEmpty(info.reply_unread_count) || Integer.valueOf(info.reply_unread_count) <= 0) {
			holder.num.setVisibility(View.INVISIBLE);
		} else {
			holder.num.setVisibility(View.VISIBLE);
			holder.num.setText(info.reply_unread_count);
		}
		return convertView;
	}

	static class ViewHoder {
		ImageView price;
		com.mopub.volley.toolbox.NetworkImageView price2;
		TextView title, time, num;
	}
}
