package com.oasis.sdk.base.communication;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.text.TextUtils;

import com.oasis.sdk.base.Exception.OasisSdkException;

/**
 * Http操作的类
 * 
 */
public class HttpClient {

	private HttpClient() {

	}

	private static HttpClient hc = null;

	public static HttpClient newInstance() {
		if (hc == null) {
			hc = new HttpClient();
		}
		return hc;
	}

	/**
	 * 同步执行post.
	 * 
	 * @param requestEntity
	 * @return
	 * @throws HttpTimeOutException
	 * @throws OasisSdkException 
	 */
	public ResponseEntity post(RequestEntity requestEntity) throws OasisSdkException {
		
		ResponseEntity responseEntity = doPost(requestEntity);
		return responseEntity;
	}
	

	public ResponseEntity doGet(RequestEntity requestEntity){
		ResponseEntity responseEntity = new ResponseEntity();
		try {
			HttpGet httpGet = new HttpGet(requestEntity.url);
			   //第二步，使用execute方法发送HTTP GET请求，并返回HttpResponse对象
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
			   if (httpResponse.getStatusLine().getStatusCode() == 200)
			   {
			        //第三步，使用getEntity方法活得返回结果
				   HttpEntity entity = httpResponse.getEntity();
					if (entity != null) {
						byte[] bytes = EntityUtils.toByteArray(entity);
						responseEntity.setContent(bytes);
					}
			    }
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return responseEntity;

	}

	/**
	 * 执行post请求.
	 * 
	 * @param requestEntity
	 * @return ResponseEntity
	 * @throws HttpTimeOutException
	 * @throws OasisSdkException 
	 */
	private ResponseEntity doPost(RequestEntity requestEntity) throws OasisSdkException {
		ResponseEntity responseEntity = new ResponseEntity();
		if (null == requestEntity) {
			return responseEntity;
		}
		HttpPost hp = null;
		try {
			hp = new HttpPost(requestEntity.url);
			HttpContext context = new BasicHttpContext();

			if(null != requestEntity.xml && !"".equals(requestEntity.xml)){
				HttpEntity he = new StringEntity(requestEntity.xml, "utf-8");
				hp.setEntity(he);
			}
			hp.addHeader("Content-Type", "application/json");
			hp.addHeader("Content-Type", "text/plain");
			HttpResponse response = HttpUtils.doHttpPost(hp, context);
			if (response == null) {
				return responseEntity;
			}
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				byte[] bytes = EntityUtils.toByteArray(entity);
				responseEntity.setContent(bytes);
			}
		} catch (ConnectionPoolTimeoutException e) {
			throw new OasisSdkException("通信超时，请检查网络");
		} catch (SocketTimeoutException e) {
			throw new OasisSdkException("连接超时，请检查网络");
		} catch (Exception e) {
			if(!TextUtils.isEmpty(e.getMessage()) && (e.getMessage().contains("timed out") || e.getMessage().contains("refused"))){
				throw new OasisSdkException("未开启服务，请联系软件提供商");
			}else{
				throw new OasisSdkException(e.getMessage());
			}
		}  finally {
			if (hp != null) {
			}
		}
		return responseEntity;
	}

	
}
