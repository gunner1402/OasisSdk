/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oasis.sdk.activity;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.base.http.CallbackResultForActivity;
import com.mopub.volley.DefaultRetryPolicy;
import com.mopub.volley.Response;
import com.mopub.volley.VolleyError;
import com.mopub.volley.toolbox.ImageRequest;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.ApplicationContextManager;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.DisplayUtil;


/**
 * Epin 支付
 * @author xdb
 */
public class OasisSdkPayEpinActivity extends OasisSdkBaseActivity {
	
    // Debug tag, for logging
    static final String TAG = OasisSdkPayEpinActivity.class.getSimpleName();

    Handler mHandler = null;
    EditText et_code;
    LinearLayout ll_clean;
    LinearLayout ll_images;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_pay_epin"));
        
        initHead(true, null, true, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pcenter_notice_12")));
        
        et_code = (EditText)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_epin_edittext"));
        et_code.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(s.length() > 0)
					ll_clean.setVisibility(View.VISIBLE);
				else
					ll_clean.setVisibility(View.GONE);
			}
		});
        ll_clean = (LinearLayout)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_epin_clean"));
        ll_clean.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onClickToClean(v);
			}
		});
        
        ll_images = (LinearLayout)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_epin_img"));
        mHandler = new MyHandler(this);
        mHandler.sendEmptyMessageDelayed(100, 2000);
        
    }
    public void onClickToGet(View view){
//    	BaseUtils.showMsg(this, "开始领取"+BaseUtils.getDensity());
    	if(et_code.length() > 0 )
    		check();
    }
    private void onClickToClean(View view){
    	et_code.setText("");
    }
    
    private void check(){
    	// 关闭可能显示的键盘
    	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
    	imm.hideSoftInputFromWindow(et_code.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); 
    	String code = et_code.getText().toString().trim();
    	if(TextUtils.isEmpty(code)){
    		BaseUtils.showMsg(this, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_epin_notice_5")));
    		return;
    	}
    	setWaitScreen(true);
    	HttpService.instance().postEpinCode(code.toUpperCase(), new MyCallback(this));
    }
    class MyCallback implements CallbackResultForActivity{
    	OasisSdkPayEpinActivity activity;
    	public MyCallback(OasisSdkPayEpinActivity activity) {
			this.activity = activity;
		}
    	@Override
    	public void success(Object data, String statusCode, String msg) {
    		setWaitScreen(false);
    		showResultDialog((String)data);
    	}
    	@Override
    	public void fail(String statusCode, String msg) {
    		setWaitScreen(false);
    		showResultDialog("");
    	}
    	@Override
    	public void excetpion(Exception e) {
    		setWaitScreen(false);
//    		showResultDialog("");
    		BaseUtils.showMsg(activity, activity.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_autologin_exception")));
    	}
    }
    private void showResultDialog(final String res){
    	final AlertDialog d = new AlertDialog.Builder(this).create();
		d.show();
		d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_dialog_notitle"));
		d.setCanceledOnTouchOutside(false);
		
		TextView tv_content = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_content"));
		String content = getResources().getString(BaseUtils.getResourceValue("string", TextUtils.isEmpty(res)?"oasisgames_sdk_epin_notice_3":"oasisgames_sdk_epin_notice_4"));
		content = content.replace("DIAMOND", res);
		tv_content.setText(content);
		
		TextView tv_sure = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_sure"));
		tv_sure.setText(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_common_btn_sure"))); 
		tv_sure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(!TextUtils.isEmpty(res))
					et_code.setText("");
				d.dismiss();
			}
		});
		TextView tv_cancle = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_cancle"));
		tv_cancle.setVisibility(View.GONE);
		
		TextView tv_text = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_text"));
		tv_text.setVisibility(View.GONE);
		
    }
    
    public static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkPayEpinActivity> mOuter;

		public MyHandler(OasisSdkPayEpinActivity activity) {
			mOuter = new WeakReference<OasisSdkPayEpinActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			OasisSdkPayEpinActivity outer = mOuter.get();
			if (outer != null) {
				switch (msg.what) {
				case HANDLER_SUCECCES:
					
						
					break;
				case HANDLER_FAIL:
					break;
				case HANDLER_EXCEPTION:
					// 弹出框提示，询问用户是否重试
//					outer.setWaitScreen(false);
					break;
				case HANDLER_ERROR:
					// 发生错误，无法继续执行
					break;
				case HANDLER_RESULT:
					outer.close();
					break;
				case 20:// pending状态，提示用户等待确认
					BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pay_error_success2")));
					break;
				case 100:
					outer.getImagesUrl();
					break;
				}
			}
		}
    }
    private void getImagesUrl(){
    	HttpService.instance().getEpinImages(new GetImagesUrlCallback());
    }
    class GetImagesUrlCallback implements CallbackResultForActivity{
    	@Override
    	public void success(Object data, String statusCode, String msg) {
    		try {
    			List<String> images = new ArrayList<String>();
				JSONArray array = new JSONArray((String)data);
				int count = array.length();
				for (int i = 0; i < count; i++) {
					JSONObject o = (JSONObject)array.get(i);
					if(o.has("img_url")){
						String imgUrl = o.getString("img_url");
						if(!TextUtils.isEmpty(imgUrl))
							images.add(imgUrl);
					}
				}
				initImage(images);
			} catch (JSONException e) {
			}
    	}
    	@Override
    	public void fail(String statusCode, String msg) {
    	}
    	@Override
    	public void excetpion(Exception e) {
    	}
    }
    private void initImage(List<String> imgUrls){
    	if(imgUrls == null || imgUrls.size() <= 0)
    		return;
    	
    	int size = imgUrls.size();
    	if(size > 4)// 至多显示4个
    		size = 4;
    	Rect r = new Rect();
    	et_code.getGlobalVisibleRect(r);
    	
    	int width = r.right - r.left;
    	int singWidth = width/size;
    	
    	int imgWidth = DisplayUtil.dip2px(70, BaseUtils.getDensity());
    	int imgHeight = DisplayUtil.dip2px(30, BaseUtils.getDensity());
    	    	
    	double l = singWidth/(double)imgWidth;
    	if(l <= 1){
    		imgWidth = singWidth;
    	}else{
			l = Math.round(l * 100) * 0.01d;// 取2位小数
	    	
	    	imgWidth *= l;
	    	imgHeight *= l;
    	}
    	    	
    	for (int i = 0; i < size; i++) {
    		// 每个图片 105*45
    		final ImageView img = new ImageView(this.getApplicationContext());
    		img.setLayoutParams(new LayoutParams(imgWidth, imgHeight));
    		img.setImageResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_payway_mob_epin"));
    		ImageRequest iq = new ImageRequest(imgUrls.get(i), 
    				new Response.Listener<Bitmap>() {

    					@Override
    					public void onResponse(final Bitmap arg0) {
    						if(arg0 == null)
    							return;
    						img.setImageBitmap(arg0);
    						img.postInvalidate();
    					}
    				}, 
    				imgWidth, // 根据屏幕尺寸获取图片
    				imgHeight, 
    				Config.ARGB_8888, 
    				new Response.ErrorListener() {

    					@Override
    					public void onErrorResponse(VolleyError arg0) {
    						
    						setWaitScreen(false);
    					}
    					
    				});
    		iq.setRetryPolicy(new DefaultRetryPolicy(60000, 2, 1));
    		ApplicationContextManager.getInstance().getVolleyRequestQueue().add(iq);
    		
    		ll_images.addView(img);
		}
    	if(ll_images.getChildCount() > 0 ){
    		ll_images.setVisibility(View.VISIBLE);
    	}
    }
    
    void close(){
    	setWaitScreen(false);
        finish();
    }
}
