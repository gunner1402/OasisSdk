package com.android.base.http;



public interface CallbackResultForActivity {
	/**
	 * 处理正确结果
	 * @param result
	 */
	abstract void success(Object data, String statusCode, String msg);
	
	/**
	 * 异常
	 * @param e
	 */
	abstract void excetpion(Exception e);
	
	/**
	 * 处理错误结果
	 * @param result
	 */
	abstract void fail(String statusCode, String msg);
}