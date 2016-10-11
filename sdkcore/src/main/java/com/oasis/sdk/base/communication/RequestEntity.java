package com.oasis.sdk.base.communication;


import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import android.util.Log;

import com.oasis.sdk.base.utils.SystemCache;

public class RequestEntity {
	
	public String url;
	
	public String xml;
	
	
	public RequestEntity() {
		super();
	}

	public RequestEntity(String url, String xml) {
		super();
		this.url = url;
		this.xml = xml;
		
	}
	public RequestEntity(String url) {
		super();
		this.url = url;
		if(SystemCache.SDKMODE_SANDBOX_REQEUST_RESPONSE)
			Log.d("HttpService", "请求URL="+this.url);
	}
	public RequestEntity(String url, boolean flag) {
		super();
		this.url = toURIStr(url);
		if(SystemCache.SDKMODE_SANDBOX_REQEUST_RESPONSE)
			Log.d("HttpService", "请求URL2="+this.url);
	}
	/**
	 * 解决url非法字符抛URISyntaxException异常
	 * @param urlStr
	 * @return
	 */
	private String toURIStr(String urlStr) {   
        try {   
            URL url = new URL(urlStr);   
            URI uri = new URI(url.getProtocol(), url.getAuthority(), url.getHost(), url.getPort(), url.getPath(),   
                              url.getQuery(), url.getRef());
            return uri.toString();   
        } catch (URISyntaxException e) {   
            e.printStackTrace();   
        } catch (Exception e) {
            e.printStackTrace();   
        }
        return urlStr;   
 }
}
