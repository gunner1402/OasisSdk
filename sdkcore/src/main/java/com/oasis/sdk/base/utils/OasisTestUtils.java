/**
 * 应用工具类
 */
package com.oasis.sdk.base.utils;

import android.app.Activity;

import com.oasis.sdk.base.Exception.OasisSdkException;
import com.oasis.sdk.base.service.HttpService;

/**
 * @author xdb
 * 
 */
public class OasisTestUtils {
	
	public static boolean getGift(Activity a, String requestIDs){
		try {
			
			Boolean tag = HttpService.instance().getFbRequestForGift(requestIDs);
			return tag;
			
		} catch (OasisSdkException e) {
		}
		
		return false;
	}

}
