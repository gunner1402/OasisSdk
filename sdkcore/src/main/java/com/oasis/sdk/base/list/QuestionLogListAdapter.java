package com.oasis.sdk.base.list;

import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mopub.volley.Response;
import com.mopub.volley.VolleyError;
import com.mopub.volley.toolbox.ImageRequest;
import com.oasis.sdk.activity.OasisSdkCustomerServiceQuestionLogActivity;
import com.oasis.sdk.base.entity.QuestionInfoLog;
import com.oasis.sdk.base.utils.ApplicationContextManager;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.InternationalUtil;

/**
 * 问题详情列表 适配器
 * 
 * @author Administrator
 *
 */
public class QuestionLogListAdapter extends BaseListAdapter<QuestionInfoLog> {
	View mViewNotice;
	View mViewRight;
	View mViewLeft;
	OasisSdkCustomerServiceQuestionLogActivity c;

	public QuestionLogListAdapter(Activity activity, List<QuestionInfoLog> data, int count, LinearLayout footerView) {
		super(activity, data, count, footerView,
				BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_listview_foot_more"), 0);
		this.c = (OasisSdkCustomerServiceQuestionLogActivity) activity;

		mViewNotice = c.getLayoutInflater().inflate(
				BaseUtils.getResourceValue("layout", "oasisgames_sdk_customer_service_q_details_item_notice"), null);
		mViewRight = c.getLayoutInflater().inflate(
				BaseUtils.getResourceValue("layout", "oasisgames_sdk_customer_service_q_details_item_right"), null);
		mViewLeft = c.getLayoutInflater().inflate(
				BaseUtils.getResourceValue("layout", "oasisgames_sdk_customer_service_q_details_item_left"), null);
	}

	@Override
	public void loadMore() {
		// no more
		// c.loadData();
	}

	@Override
	@TargetApi(VERSION_CODES.HONEYCOMB)
	public View getRowView(int position, View convertView, ViewGroup parent) {
		ViewHoder holder = null;

		final QuestionInfoLog info = getItem(position);
		if (convertView == null) {
			holder = new ViewHoder();
			convertView = c.getLayoutInflater().inflate(
					BaseUtils.getResourceValue("layout", "oasisgames_sdk_customer_service_q_details_item"), null);

			holder.viewNotice = convertView.findViewById(
					BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_q_details_item_notice"));
			holder.viewLeft = convertView.findViewById(
					BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_q_details_item_left"));
			holder.viewRight = convertView.findViewById(
					BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_q_details_item_right"));

			holder.noticeTitle = (TextView) convertView.findViewById(
					BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_q_details_item_notice_title"));

			holder.usernameLeft = (TextView) convertView.findViewById(
					BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_q_details_item_username_left"));
			holder.wordLeft = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id",
					"oasisgames_sdk_customer_service_q_details_item_content_word_left"));
			holder.imageLeft = (ImageView) convertView.findViewById(BaseUtils.getResourceValue("id",
					"oasisgames_sdk_customer_service_q_details_item_content_image_local_left"));

			holder.usernameRight = (TextView) convertView.findViewById(
					BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_q_details_item_username_right"));
			holder.wordRight = (TextView) convertView.findViewById(BaseUtils.getResourceValue("id",
					"oasisgames_sdk_customer_service_q_details_item_content_word_right"));
			holder.imageRight = (ImageView) convertView.findViewById(BaseUtils.getResourceValue("id",
					"oasisgames_sdk_customer_service_q_details_item_content_image_local_right"));

			holder.statusLeft = (LinearLayout) convertView.findViewById(BaseUtils.getResourceValue("id",
					"oasisgames_sdk_customer_service_q_details_item_content_status_left"));
			holder.statusRight = (LinearLayout) convertView.findViewById(BaseUtils.getResourceValue("id",
					"oasisgames_sdk_customer_service_q_details_item_content_status_right"));
			convertView.setTag(holder);
		} else {
			holder = (ViewHoder) convertView.getTag();
		}

		if (c.LOGTYPE_NOTICE.equals(info.content_type)) {
			holder.viewNotice.setVisibility(View.VISIBLE);
			holder.viewLeft.setVisibility(View.GONE);
			holder.viewRight.setVisibility(View.GONE);

			// Notice
			if (info.content.startsWith(c.NOTICETAG_CLOSED)) {
				int index = c.NOTICETAG_CLOSED.length();
				String s = info.content.substring(index);
				holder.noticeTitle.setText(s);
			} else
				holder.noticeTitle
						.setText(InternationalUtil.InternationalDateFormat(c, Long.valueOf(info.content) * 1000));

			return convertView;
		}

		if (c.USER_CUSTOMER.equals(info.usertype)) {

			holder.viewNotice.setVisibility(View.GONE);
			holder.viewLeft.setVisibility(View.VISIBLE);
			holder.viewRight.setVisibility(View.GONE);

			holder.usernameLeft.setText(info.custom_nickname);
			if (c.LOGTYPE_WORD.equals(info.content_type)) {
				holder.wordLeft.setText(info.content);
				holder.wordLeft.setLongClickable(true);
				if (Build.VERSION.SDK_INT > VERSION_CODES.HONEYCOMB)
					holder.wordLeft.setTextIsSelectable(true);
				holder.wordLeft.setVisibility(View.VISIBLE);

				holder.imageLeft.setVisibility(View.GONE);
			}
			if (c.LOGTYPE_IMG.equals(info.content_type)) {
				holder.wordLeft.setVisibility(View.GONE);
				holder.imageLeft.setVisibility(View.VISIBLE);
				if (info.img_url != null && !info.img_url.isEmpty()
						&& (info.img_url.startsWith("http:") || info.img_url.startsWith("https:"))) {
					loadImage(c, info.img_url, holder.imageLeft);
				} else {
					Bitmap map = BaseUtils.getSmallBitmap(info.local_img_url,
							c.getResources().getDisplayMetrics().widthPixels,
							c.getResources().getDisplayMetrics().heightPixels);

					if (map != null) {
						holder.imageLeft.setImageBitmap(map);
						holder.imageLeft.postInvalidate();
					}
				}
				holder.imageLeft.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						c.showBigImage(info);
					}
				});
			}

		}

		if (c.USER_PLAYER.equals(info.usertype)) {

			holder.viewNotice.setVisibility(View.GONE);
			holder.viewLeft.setVisibility(View.GONE);
			holder.viewRight.setVisibility(View.VISIBLE);

			holder.usernameRight
					.setText(c.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_customer_notice13")));
			if (c.LOGTYPE_WORD.equals(info.content_type)) {
				holder.wordRight.setText(info.content);
				holder.wordRight.setLongClickable(true);
				if (Build.VERSION.SDK_INT > VERSION_CODES.HONEYCOMB)
					holder.wordRight.setTextIsSelectable(true);

				holder.wordRight.setVisibility(View.VISIBLE);
				holder.imageRight.setVisibility(View.GONE);
			}

			if (c.LOGTYPE_IMG.equals(info.content_type)) {
				holder.wordRight.setVisibility(View.GONE);
				holder.imageRight.setVisibility(View.VISIBLE);
				if (info.img_url != null && !info.img_url.isEmpty()
						&& (info.img_url.startsWith("http:") || info.img_url.startsWith("https:"))) {
					loadImage(c, info.img_url, holder.imageRight);
				} else {
					Bitmap map = BaseUtils.getSmallBitmap(info.local_img_url,
							c.getResources().getDisplayMetrics().widthPixels,
							c.getResources().getDisplayMetrics().heightPixels);

					if (map != null) {
						holder.imageRight.setImageBitmap(map);
						holder.imageRight.postInvalidate();
					}
				}
				holder.imageRight.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						c.showBigImage(info);
					}
				});
			}
			if (QuestionInfoLog.status_ok.equals(info.status))
				holder.statusRight.setVisibility(View.INVISIBLE);
			else {
				holder.statusRight.setVisibility(View.VISIBLE);
				if (QuestionInfoLog.status_fail.equals(info.status)) {
					holder.statusRight.getChildAt(1).setVisibility(View.GONE);
					holder.statusRight.getChildAt(0).setVisibility(View.VISIBLE);
					holder.statusRight.getChildAt(0).setBackgroundResource(
							BaseUtils.getResourceValue("drawable", "oasisgames_sdk_customer_send_status"));
					holder.statusRight.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							info.status = QuestionInfoLog.status_sending;
							((LinearLayout) v).getChildAt(1).setVisibility(View.VISIBLE);
							((LinearLayout) v).getChildAt(0).setVisibility(View.GONE);
							c.publish(info);
						}
					});
				}
				if (QuestionInfoLog.status_sending.equals(info.status)) {
					holder.statusRight.getChildAt(1).setVisibility(View.VISIBLE);
					holder.statusRight.getChildAt(0).setVisibility(View.GONE);
				}
			}
		}

		return convertView;

	}

	private void loadImage(Activity c, String url, final ImageView imgView) {
		ImageRequest iq = new ImageRequest(url,

		new Response.Listener<Bitmap>() {
			@Override
			public void onResponse(Bitmap arg0) {
				if (arg0 != null) {
					imgView.setImageBitmap(arg0);
					imgView.postInvalidate();
				}
			}
		}, c.getResources().getDisplayMetrics().widthPixels, // DisplayUtil.dip2px(200,
																// BaseUtils.getDensity()),
																// //
																// 以布局文件为准，200dip
				c.getResources().getDisplayMetrics().heightPixels, // DisplayUtil.dip2px(200,
																	// BaseUtils.getDensity()),
				Config.ARGB_8888, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
					}

				});
		ApplicationContextManager.getInstance().getVolleyRequestQueue().add(iq);
	}

	static class ViewHoder {
		View viewNotice;
		View viewLeft;
		View viewRight;

		TextView noticeTitle;

		TextView usernameLeft;
		TextView wordLeft;
		ImageView imageLeft;

		TextView usernameRight;
		TextView wordRight;
		ImageView imageRight;

		LinearLayout statusLeft;
		LinearLayout statusRight;
	}
}
