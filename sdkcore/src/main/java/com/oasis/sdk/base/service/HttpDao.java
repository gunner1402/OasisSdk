package com.oasis.sdk.base.service;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.oasis.sdk.base.Exception.OasisSdkException;
import com.oasis.sdk.base.communication.HttpClient;
import com.oasis.sdk.base.communication.RequestEntity;
import com.oasis.sdk.base.communication.ResponseEntity;
import com.oasis.sdk.base.json.JsonParser;
import com.oasis.sdk.base.utils.SystemCache;

/**
 * http DAO
 * 
 * @author Xdb
 * 
 */
public class HttpDao {
	private final static HttpDao HTTP_DAO = new HttpDao();

	private HttpDao() {
	}

	/**
	 * @return 返回逻辑的实例.
	 */
	public static HttpDao instance() {

		return HTTP_DAO;
	}

	/**
	 * 只负责想服务器发请求，不处理返回数据
	 * @param requestEntity
	 * @throws UnsupportedEncodingException
	 * @throws OasisSdkException
	 * @throws JSONException
	 */
	public String submit(RequestEntity requestEntity) throws OasisSdkException {
		return doPostRequest(requestEntity);
	}
	/**
	 * 提交请求，并返回ResourcesEntity集合
	 * @param requestEntity
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws JSONException
	 * @throws OasisSdkException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("rawtypes")
	public List submitToList(RequestEntity requestEntity, String tagName, Object obj) throws JSONException, OasisSdkException, IllegalAccessException, InstantiationException {
		String XML = doPostRequest(requestEntity);
		checkErrorStatus(XML, obj);
		JSONObject j = new JSONObject(XML);
		j.getJSONArray(tagName);
		List list = JsonParser.newInstance().parserJSON2ObjList(j.getJSONArray(tagName).toString(), obj);
		return list;
	}
	
	/**
	 * 检查状态并设置 pageNo、pageSize等公共属性
	 * @param json
	 * @param obj
	 * @throws JSONException
	 * @throws OasisSdkException
	 */
	private void checkErrorStatus(String json, Object obj) throws JSONException, OasisSdkException{
		checkErrorStatus(json);
		JsonParser.newInstance().parserJson2Obj(json, obj);
	}
	/**
	 * 提交请求，并返回 Entity对象
	 * @param requestEntity
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws JSONException
	 * @throws OasisSdkException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void submitToObj(RequestEntity requestEntity, Object obj) throws  OasisSdkException, JSONException{
		String XML = doPostRequest(requestEntity);
		checkErrorStatus(XML);
		JsonParser.newInstance().parserJson2Obj(XML, obj);
	}
	
	
	@SuppressWarnings({ "static-access", "rawtypes" })
	public void submitToObj(RequestEntity requestEntity, Object obj, Object childObj, String tagName) throws OasisSdkException, JSONException, IllegalAccessException, InstantiationException{
		String XML = doPostRequest(requestEntity);
		checkErrorStatus(XML);
		JsonParser.newInstance().parserJson2Obj(XML, obj);
		JSONObject j = new JSONObject(XML);
		List list = JsonParser.newInstance().parserJSON2ObjList(j.getJSONArray(tagName).toString(), childObj);
		JsonParser.newInstance().setValue("list", list, obj);
	}
	
	/**
	 * 检查服务器返回串是否正常  status = 0 表示 成功，其余值表示出错
	 * @param json 
	 * @return
	 * @throws JSONException
	 * @throws OasisSdkException
	 */
	private boolean checkErrorStatus(String json) throws JSONException, OasisSdkException{
		JSONObject j = new JSONObject(json);
		String status = j.getString("status");
		if(!"OK".equals(status)&&!"ok".equals(status)){
//			if("1".equals(status)){
//				SystemCache.appActivity.startService(new Intent(SystemCache.appActivity, TokenExceptionService.class));
//			}
//			throw new OasisSdkException(Constant.errorMsg.get(status));
			throw new OasisSdkException(j.getString("errmsg"));
		}
		return false;
	}
	/**
	 * post方式获取
	 * @param requestEntity
	 * @return
	 * @throws HttpTimeOutException
	 * @throws UnsupportedEncodingException
	 * @throws OasisSdkException
	 */
	private String doPostRequest(RequestEntity requestEntity) throws OasisSdkException {
		if(!SystemCache.NetworkisAvailable){
			throw new OasisSdkException("当前网络不可用");
		}
		ResponseEntity res = HttpClient.newInstance().post(requestEntity);
		

		return res.getStringContent();
	}

	/**
	 * get方式获取
	 * @param requestEntity
	 * @return
	 * @throws OasisSdkException
	 * @throws UnsupportedEncodingException
	 */
	public String doGetRequest(RequestEntity requestEntity) throws OasisSdkException {
		if(!SystemCache.NetworkisAvailable){
			throw new OasisSdkException("当前网络不可用");
		}
		ResponseEntity res = HttpClient.newInstance().doGet(requestEntity);

		return res.getStringContent();
	}
}
