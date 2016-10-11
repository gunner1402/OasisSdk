package com.oasis.sdk.base.Exception;
/**
 * 数据格式异常类
 * 1、服务端返回的json格式不正确
 * 2、json缺少某属性
 * @author xdb
 *
 */
public class OasisSdkDataErrorException extends Exception {

	private static final long serialVersionUID = 5587741930897557887L;

	public OasisSdkDataErrorException(String error) {
		super(error);
	}
}
