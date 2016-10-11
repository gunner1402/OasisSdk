package com.android.base.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import com.mopub.volley.AuthFailureError;
import com.mopub.volley.DefaultRetryPolicy;
import com.mopub.volley.Request.Method;
import com.mopub.volley.Response;
import com.mopub.volley.VolleyError;
import com.mopub.volley.toolbox.JsonObjectRequest;
import com.mopub.volley.toolbox.StringRequest;
import com.oasis.sdk.base.utils.ApplicationContextManager;

public class OasisSdkHttpClient {
	private String url;
	private Map<String, String> param;
	private Callback callback;
	public OasisSdkHttpClient(String url, Map<String, String> param, Callback callback) {
		this.url = url;
		this.param = param;
		this.callback = callback;
	}
	
	public void submitPost(){
		StringRequest request = new StringRequest(Method.POST, url, new Response.Listener<String>() {

			@Override
			public void onResponse(String response) {
				callback.handleResultData(response);
			}
			
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				callback.handleErorrData(error);
			}
			
		}){
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				if(param != null && !param.isEmpty()){
					HashMap<String, String> hashMap = new HashMap<String, String>();
					for (Entry<String, String> iter : param.entrySet()) {
						hashMap.put(iter.getKey(), iter.getValue());
					}
	                return hashMap;
				}
				else
					return super.getParams();
			}
			@Override  
	        public Map<String, String> getHeaders() throws AuthFailureError {  //设置头信息  
	          Map<String, String> map = new HashMap<String, String>();  
	          map.put("Accept", "application/json");
	          map.put("Content-Type", "application/x-www-form-urldecoded");  
	          return map;  
	        }  

		};
		request.setRetryPolicy(new DefaultRetryPolicy(30 * 1000, 0, 1.0f));
		ApplicationContextManager.getInstance().getVolleyRequestQueue().add(request);
	}
	public void submitPost_json(){
		JsonObjectRequest request = new JsonObjectRequest(Method.POST, url, null, new Response.Listener<JSONObject>() {
			
			@Override
			public void onResponse(JSONObject arg0) {
				callback.handleResultData(arg0.toString());
			}
			
		}, new Response.ErrorListener() {
			
			@Override
			public void onErrorResponse(VolleyError error) {
				callback.handleErorrData(error);
			}
			
		}){
//			@Override
//			protected Map<String, String> getParams() throws AuthFailureError {
//				if(param != null && !param.isEmpty()){
//					HashMap<String, String> hashMap = new HashMap<String, String>();
//					for (Entry<String, String> iter : param.entrySet()) {
//						hashMap.put(iter.getKey(), iter.getValue());
//					}
//	                return hashMap;
//				}
//				else
//					return super.getParams();
//			}
//			@Override  
//	        public Map<String, String> getHeaders() throws AuthFailureError {  //设置头信息  
//	          Map<String, String> map = new HashMap<String, String>();  
//	          map.put("Accept", "application/json");
//	          map.put("Content-Type", "application/x-www-form-urldecoded");  
//	          return map;  
//	        }  

		};
		ApplicationContextManager.getInstance().getVolleyRequestQueue().add(request);
	}
	public void submitGet(){
//		final long l = new Date().getTime();
		StringRequest request = new StringRequest(Method.GET, url, new Response.Listener<String>() {
			
			@Override
			public void onResponse(String response) {
//				BaseUtils.logError("OASResponse", "请求地址："+url+"\n"+"请求结果："+response);
//				long e = new Date().getTime() - l;
//				if(e > 6000)
//					BaseUtils.logError("OasisSdkHttpClient", "耗时:"+e+" ms"+" "+ url+"\n"+response);
//				else if(e > 3000 && e <= 6000)
//					BaseUtils.logWarn("OasisSdkHttpClient", "耗时:"+e+" ms"+" "+ url+"\n"+response);
//				else
//					BaseUtils.logDebug("OasisSdkHttpClient", "耗时:"+e+" ms"+" "+ url);
				callback.handleResultData(response);
			}
			
		}, new Response.ErrorListener() {
			
			@Override
			public void onErrorResponse(VolleyError error) {
				callback.handleErorrData(error);
			}
			
		}){
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				return param;
			}
		};
		request.setRetryPolicy(new DefaultRetryPolicy(30 * 1000, 0, 1.0f));
//		request.setRetryPolicy(new RetryPolicy() {
//			
//			@Override
//			public void retry(VolleyError arg0) throws VolleyError {
//			}
//			
//			@Override
//			public int getCurrentTimeout() {
//				return 30000;
//			}
//			
//			@Override
//			public int getCurrentRetryCount() {
//				return 1;
//			}
//		});
//		request.setShouldCache(false);
		
		ApplicationContextManager.getInstance().getVolleyRequestQueue().add(request);
		
	}
	
	public interface Callback {
		/**
		 * 处理正确结果
		 * @param result
		 */
		abstract void handleResultData(String result);
		
		/**
		 * 处理错误结果
		 * @param result
		 */
		abstract void handleErorrData(VolleyError error);
	}
}