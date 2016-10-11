package com.oasis.sdk.base.communication;

import android.util.Log;

import com.oasis.sdk.base.utils.SystemCache;



/**
 * 存放http请求结果的实体类.
 */
public class ResponseEntity {

	public byte[] content;

	/**
	 * @return the content
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(byte[] content) {
		this.content = content;
	}

	/**
	 * 返回String类型的数据内容.
	 * 
	 * @return String
	 */
	public String getStringContent() {
		if(null != content){
			if(SystemCache.SDKMODE_SANDBOX_REQEUST_RESPONSE)
				Log.d("HttpResponseEntity", "请求结果="+new String(content));
			return new String(content);
		}else
			return "";
	}

}