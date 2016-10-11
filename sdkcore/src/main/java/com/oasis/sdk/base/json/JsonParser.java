package com.oasis.sdk.base.json;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import android.text.TextUtils;

import com.oasis.sdk.base.Exception.OasisSdkException;

public class JsonParser {

	private JsonParser() {

	}

	private static JsonParser parser = null;

	public static JsonParser newInstance() {
		if (parser == null) {
			parser = new JsonParser();
		}
		return parser;
	}
	/**
	 * json解析方法，将json文档转换成对象，要求对象的属性命名等同于json节点名称.
	 * 
	 * @param json
	 *            json文本
	 * @param object
	 *            需要转换的目标对象
	 * @throws OasisSdkException
	 * @throws JSONException
	 */
	@SuppressWarnings("rawtypes")
	public void parserJson2Obj(String json, Object object) throws JSONException, OasisSdkException {
		JSONObject arr = new JSONObject(json);

		if (object == null)
			throw new OasisSdkException("Object is null,please init Object");

				Iterator it = arr.keys();
				while (it.hasNext()) {
					String type = (String) it.next();
					Object value = arr.get(type);
					if(null != value && !"null".equals(value) && !"".equals(value))
						setValue(type, String.valueOf(value), object);
				}
			
	}
	/**
	 * json解析方法，将json文档转换成对象，要求对象的属性命名等同于json节点名称.
	 * 
	 * @param json
	 *            json文本
	 * @param object
	 *            需要转换的目标对象
	 * @param TagName
	 *            需要转换对象对应于XML中的节点
	 * @throws OasisSdkException
	 * @throws JSONException
	 */
	@SuppressWarnings("rawtypes")
	public void parserJSON2Obj(String json, Object object, String TagName) throws JSONException, OasisSdkException {
		JSONArray arr = new JSONArray(json);

		if (object == null)
			throw new OasisSdkException("Object is null,please init Object");

		int length = arr.length();
		if (null == TagName || TextUtils.isEmpty(TagName)) {
			for (int j = 0; j < length; j++) {
				JSONObject categoryJson = arr.getJSONObject(j);
				Iterator it = categoryJson.keys();
				while (it.hasNext()) {
					String type = (String) it.next();
					Object value = categoryJson.get(type);
					if(null != value && !"null".equals(value) && !"".equals(value))
						setValue(type, String.valueOf(value), object);
				}
			}
		} else {
			for (int i = 0; i < length; i++) {

				JSONObject jobj = arr.getJSONObject(i);
				JSONArray sub_arr = jobj.getJSONArray(TagName);
				int subLength = sub_arr.length();
				for (int j = 0; j < subLength; j++) {
					JSONObject categoryJson = sub_arr.getJSONObject(j);
					Iterator it = categoryJson.keys();
					while (it.hasNext()) {
						String type = (String) it.next();
						Object value = categoryJson.get(type);
						if(null != value && !"null".equals(value) && !"".equals(value))
							setValue(type, String.valueOf(value), object);
					}
				}
			}
		}
	}

	/**
	 * json解析方法，将json文档转换成对象的Vector集合，对于循环出现的节点使用，要求对象的属性命名等同于json节点名称.
	 * 从JSONObject 开始 ，通过tagName，解析JSONArray
	 * 
	 * @param json
	 *            json文本
	 * @param obj
	 *            需要转换的目标对象
	 * @param TagName
	 *            需要转换对象对应于XML中的节点名称(每个循环节点的根节点名称)
	 * @return Vector
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws OasisSdkException
	 * @throws JSONException
	 */
	@SuppressWarnings("rawtypes")
	public List<Object> parserJSONObj2ObjList(String json, Object object, String TagName) throws JSONException,
			OasisSdkException, IllegalAccessException, InstantiationException {
		List<Object> vector = null;
		JSONObject arr = new JSONObject(json);

		if (object == null)
			throw new OasisSdkException("Object is null,please init Object");

		vector = new ArrayList<Object>();
			JSONArray sub_arr = arr.getJSONArray(TagName);
			int subLength = sub_arr.length();
			for (int j = 0; j < subLength; j++) {
				object = object.getClass().newInstance();
				JSONObject categoryJson = sub_arr.getJSONObject(j);
				Iterator it = categoryJson.keys();
				while (it.hasNext()) {
					String type = (String) it.next();
					Object value = categoryJson.get(type);
					if(null != value && !"null".equals(value) && !"".equals(value))
						setValue(type, String.valueOf(value), object);
				}
				vector.add(object);
			}
		return vector;
	}
	/**
	 * json解析方法，将json文档转换成对象的Vector集合，对于循环出现的节点使用，要求对象的属性命名等同于json节点名称.
	 * 
	 * @param json
	 *            json文本
	 * @param obj
	 *            需要转换的目标对象
	 * @param TagName
	 *            需要转换对象对应于json中的节点名称(每个循环节点的根节点名称)
	 * @return Vector
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws OasisSdkException
	 * @throws JSONException
	 */
	@SuppressWarnings("rawtypes")
	public List<Object> parserJSON2ObjList(String json, Object object, String TagName) throws JSONException,
			OasisSdkException, IllegalAccessException, InstantiationException {
		List<Object> vector = null;
		JSONArray arr = new JSONArray(json);

		if (object == null)
			throw new OasisSdkException("Object is null,please init Object");

		vector = new ArrayList<Object>();
		int length = arr.length();
		for (int i = 0; i < length; i++) {

			JSONObject jobj = arr.getJSONObject(i);
			JSONArray sub_arr = jobj.getJSONArray(TagName);
			int subLength = sub_arr.length();
			for (int j = 0; j < subLength; j++) {
				object = object.getClass().newInstance();
				JSONObject categoryJson = sub_arr.getJSONObject(j);
				Iterator it = categoryJson.keys();
				while (it.hasNext()) {
					String type = (String) it.next();
					Object value = categoryJson.get(type);
					if(null != value && !"null".equals(value) && !"".equals(value))
						setValue(type, String.valueOf(value), object);
				}
				vector.add(object);
			}
		}
		return vector;
	}

	/**
	 * 解析json为List
	 * @param json
	 * @param object
	 * @return
	 * @throws JSONException
	 * @throws OasisSdkException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("rawtypes")
	public List<Object> parserJSON2ObjList(String json, Object object) throws JSONException,
			OasisSdkException, IllegalAccessException, InstantiationException {
		List<Object> vector = null;
		JSONArray arr = new JSONArray(json);

		if (object == null)
			throw new OasisSdkException("Object is null,please init Object");

		vector = new ArrayList<Object>();
		int length = arr.length();
		for (int i = 0; i < length; i++) {

			JSONObject jobj = arr.getJSONObject(i);
			object = object.getClass().newInstance();

			Iterator it = jobj.keys();
			while (it.hasNext()) {
				String type = (String) it.next();
				Object value = jobj.get(type);
				if(null != value && !"null".equals(value) && !"".equals(value))
					setValue(type, String.valueOf(value), object);
			}
			vector.add(object);
		}
		return vector;
	}

	/**
	 * json解析方法，将json文档转换成HashMap，转换后格式Map<tagname,tagvalue>.
	 * 
	 * @param json
	 *            json文本
	 * @param tagNames
	 *            [] 需要转换的节点的父节点名称数组
	 * @return Table 如果没有匹配节点return null
	 * @throws JSONException
	 * @throws OasisSdkException
	 */
	public Hashtable<String, String> parserJSON2Map(String json, String[] tagNames) throws JSONException, OasisSdkException {
		Hashtable<String, String> table = null;
		if (null == tagNames || tagNames.length <= 0)
			throw new OasisSdkException("tag name is null");
		table = new Hashtable<String, String>();
		initHashtable(table, tagNames);
		if(!json.startsWith("["))
			json = "["+json;
		if(!json.endsWith("]"))
			json += "]";
		JSONArray arr = new JSONArray(json);
		int length = arr.length();
		for (int i = 0; i < length; i++) {
				JSONObject jobj = arr.getJSONObject(i);
	
				for (String tag : tagNames) {
					Object value = jobj.get(tag);
					if (null != value && !value.equals(""))
						table.put(tag, value.toString());
				}
		}
		return table.size() == 0 ? null : table;
	}

	/**
	 * 将制定tag下得json转换为map
	 * @param json
	 * @param tagName
	 * @return
	 * @throws JSONException
	 * @throws OasisSdkException
	 */
	public Map<String, String> parserJSON2Map(String json, String tagName) throws JSONException, OasisSdkException {

		Map<String, String> map = null;
		JSONObject arr = new JSONObject(json);
		if(arr.has(tagName)){
			map = new HashMap<String, String>();
			JSONObject jobj = arr.getJSONObject(tagName);
			Iterator<String> iter = jobj.keys();
			for (Iterator iterator = iter; iterator.hasNext();) {
				String key = (String) iterator.next();
				map.put(key, (String)jobj.get(key));
			}
			
		}
		
		return map;
	}

	private void initHashtable(Hashtable<String, String> table, String[] tagNames) {
		for (int i = 0; i < tagNames.length; i++) {
			table.put(tagNames[i], "");
		}
		return;
	}

	/**
	 * json解析方法，将json文档转换成对象的list集合，对于循环出现的节点使用，要求对象的属性命名等同于json节点名称.
	 * 
	 * @param json
	 *            json文本
	 * @param obj
	 *            需要转换的目标对象
	 * @param TagName
	 *            需要转换对象对应于XML中的节点名称(每个循环节点的根节点名称)
	 * @return Vector 如果没有匹配节点return null
	 * @throws OasisSdkException
	 * @throws JSONException
	 */
	@SuppressWarnings("rawtypes")
	public Vector<Hashtable<String, String>> parserJSON2MapList(String json, String TagName)
			throws XmlPullParserException, IOException, IllegalAccessException, InstantiationException, OasisSdkException,
			JSONException {
		Hashtable<String, String> table = null;
		Vector<Hashtable<String, String>> vector = new Vector<Hashtable<String, String>>();// 初始化vector

		JSONArray arr = new JSONArray(json);
		int length = arr.length();
		for (int i = 0; i < length; i++) {

			JSONObject jobj = arr.getJSONObject(i);

			JSONArray sub_arr = jobj.getJSONArray(TagName);
			int subLength = sub_arr.length();
			for (int j = 0; j < subLength; j++) {
				JSONObject categoryJson = sub_arr.getJSONObject(j);
				Iterator it = categoryJson.keys();

				table = new Hashtable<String, String>();
				while (it.hasNext()) {
					String type = (String) it.next();
					Object value = categoryJson.get(type);
					if(null != value && !"null".equals(value) && !"".equals(value))
						table.put(type, (String) categoryJson.get(type));
				}
				vector.add(table);
			}
		}

		return vector;
	}

	/**
	 * 对象赋值方法.
	 * 
	 * @param fieldName
	 *            属性名称
	 * @param fieldValue
	 *            属性值
	 * @param obj
	 *            所属对象
	 * @throws OasisSdkException
	 */
	private static void setValue(String fieldName, String fieldValue, Object obj) throws OasisSdkException {

		if (fieldValue == null || fieldValue.length() == 0)
			return;

		Class<? extends Object> cls = obj.getClass();

		String methodName = getMethodName(fieldName);
		Method method;

		try {
			method = cls.getDeclaredMethod(methodName, String.class);
			method.invoke(obj, new Object[] { fieldValue });
		} catch (SecurityException e) {
			throw new OasisSdkException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new OasisSdkException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new OasisSdkException(e.getMessage());
		} catch (InvocationTargetException e) {
			throw new OasisSdkException(e.getMessage());
		} catch (NoSuchMethodException e) {
			Class<?> superclass = cls.getSuperclass();
			try {
				method = superclass.getDeclaredMethod(methodName, String.class);
				method.invoke(obj, new Object[] { fieldValue });
			} catch (SecurityException ex) {
				throw new OasisSdkException(ex.getMessage());
			} catch (NoSuchMethodException ex) {
				return;
			} catch (IllegalArgumentException ex) {
				throw new OasisSdkException(ex.getMessage());
			} catch (IllegalAccessException ex) {
				throw new OasisSdkException(ex.getMessage());
			} catch (InvocationTargetException ex) {
				throw new OasisSdkException(ex.getMessage());
			}
		}
	}

	/**
	 * 根据属性名得到相应的set方法名.
	 * 
	 * @param fieldName
	 *            属性名
	 * @return
	 */
	private static String getMethodName(String fieldName) {
		char[] charArray = fieldName.toCharArray();
		if (charArray[0] >= 'a' && charArray[0] <= 'z')
			charArray[0] = (char) (charArray[0] - 32);
		return "set" + new String(charArray);
	}

	public static void setValue(String fieldName, Object list, Object obj) throws OasisSdkException{
		if (list == null)
			return;

		Class<? extends Object> cls = obj.getClass();

		String methodName = getMethodName(fieldName);
		Method method;

		try {
			method = cls.getDeclaredMethod(methodName, List.class);
			method.invoke(obj, list);
		} catch (SecurityException e) {
			throw new OasisSdkException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new OasisSdkException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new OasisSdkException(e.getMessage());
		} catch (InvocationTargetException e) {
			throw new OasisSdkException(e.getMessage());
		} catch (NoSuchMethodException e) {
			Class<?> superclass = cls.getSuperclass();
			try {
				method = superclass.getDeclaredMethod(methodName, List.class);
				method.invoke(obj, list);
			} catch (SecurityException ex) {
				throw new OasisSdkException(ex.getMessage());
			} catch (NoSuchMethodException ex) {
				return;
			} catch (IllegalArgumentException ex) {
				throw new OasisSdkException(ex.getMessage());
			} catch (IllegalAccessException ex) {
				throw new OasisSdkException(ex.getMessage());
			} catch (InvocationTargetException ex) {
				throw new OasisSdkException(ex.getMessage());
			}
		}
	}
}
