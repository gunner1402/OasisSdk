package com.oasis.sdk.base.service;

import java.util.Map;

import com.android.base.upload.MultipartEntity;
import com.android.base.upload.MultipartRequest;
import com.mopub.volley.DefaultRetryPolicy;
import com.mopub.volley.Response;
import com.oasis.sdk.base.utils.ApplicationContextManager;

/**
 * http DAO
 * 
 * @author Xdb
 * 
 */
public class BasesDao {
	final String TAG = BasesDao.class.getSimpleName();

	public void post(String url, Map<String, String> paras, Response.Listener<String> listener, Response.ErrorListener error){
		MultipartRequest multipartRequest = new MultipartRequest(
				url, listener, error);
		multipartRequest.setRetryPolicy(new DefaultRetryPolicy(30 * 1000, 0, 1.0f));

		// 添加header
		multipartRequest.addHeader("header-name", "value");

		// 通过MultipartEntity来设置参数
		MultipartEntity multi = multipartRequest.getMultiPartEntity();

		// 文本参数
		if(paras != null && paras.size() >0) {
			StringBuffer sbuf = new StringBuffer(url);
			for (Map.Entry<String, String> en : paras.entrySet()) {
				multi.addStringPart(en.getKey(), en.getValue());
				sbuf.append("&"+en.getKey()+"="+en.getValue());
			}
//			BaseUtils.logError(TAG, sbuf.toString());
		}
		multipartRequest.setShouldCache(false);
		// 将请求添加到队列中
		ApplicationContextManager.getInstance().getVolleyRequestQueue().add(multipartRequest);
	}
	
}
