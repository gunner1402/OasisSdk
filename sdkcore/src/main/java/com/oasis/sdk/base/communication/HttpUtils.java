package com.oasis.sdk.base.communication;

import java.net.SocketTimeoutException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import com.oasis.sdk.base.utils.SystemCache;

/**
 * HttpClient的管理类. 主要是获取从连接池中获取http连接的HttpClient对象.
 */
public class HttpUtils {

	private static HttpClient HTTP_CLIENT;
	private static HttpClient HTTP_CLIENT_PROXY;
	private static int connectTimeout = 20 * 1000;// 设置连接超时
	private static int soTimeout = 20 * 1000;// 设置接收超时
	private static HttpHost WAP_PROXY = new HttpHost("10.0.0.172", 80, "http");

	/**
	 * 实例化一个HttpClient对象,单例处理的.
	 * 
	 * @return
	 */
	private synchronized static HttpClient INSTANCE() {

		if (HTTP_CLIENT == null) {

			HttpParams params = new BasicHttpParams();
			ConnManagerParams.setMaxTotalConnections(params, 10);
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpConnectionParams.setConnectionTimeout(params, connectTimeout);// 设置连接超时
			HttpConnectionParams.setSoTimeout(params, soTimeout);// 设置接收超时

			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

			ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

			HTTP_CLIENT = new DefaultHttpClient(cm, params);
		}
		return HTTP_CLIENT;
	}

	/**
	 * 实例化一个HttpClient代理对象,单例处理的.
	 * 
	 * @return
	 */
	private synchronized static HttpClient InstanceProxy() {

		if (HTTP_CLIENT_PROXY == null) {

			HttpParams params = new BasicHttpParams();
			ConnManagerParams.setMaxTotalConnections(params, 10);
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpConnectionParams.setConnectionTimeout(params, connectTimeout);// 设置连接超时
			HttpConnectionParams.setSoTimeout(params, soTimeout);// 设置接收超时

			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

			ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

			HTTP_CLIENT_PROXY = new DefaultHttpClient(cm, params);

			HTTP_CLIENT_PROXY.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, WAP_PROXY);
		}
		return HTTP_CLIENT_PROXY;
	}

	/**
	 * 执行httpPost请求
	 * 
	 * @param hp
	 * @param context
	 * @return HttpResponse .
	 * @throws Exception
	 */
	public static HttpResponse doHttpPost(HttpPost hp, HttpContext context) throws Exception, ConnectionPoolTimeoutException, SocketTimeoutException {

		HttpClient hc = INSTANCE();
		HttpClient hc_proxy = InstanceProxy();
		HttpResponse response;
		if ("cmwap".equals(SystemCache.NetworkExtraInfo)){
			response = hc_proxy.execute(hp, context);
		}else{
			response = hc.execute(hp, context);
		}
		return response;
	}
	
	
	/**
	 * 改变接收超时时间.
	 * 
	 * @param soTimeout
	 */
	public static void changeSoTimeout(int soTimeout) {
		HttpConnectionParams.setSoTimeout(INSTANCE().getParams(), soTimeout);
		HttpConnectionParams.setSoTimeout(InstanceProxy().getParams(), soTimeout);
	}

}
