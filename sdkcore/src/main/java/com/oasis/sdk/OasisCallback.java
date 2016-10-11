package com.oasis.sdk;

public interface OasisCallback {
	/**
	 * 处理正确结果
	 * @param result
	 */
	abstract void success(String result);
	
	/**
	 * 处理错误结果
	 * @param result
	 */
	abstract void error(String result);
}