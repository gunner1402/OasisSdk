package com.oasis.sdk.base.entity;

import java.io.Serializable;

/**
 * Facebook 好友详细信息
 * @author Administrator
 *
 */
public class FriendInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String id;
	
	public String name;
	
	public String picture;


	public void setId(String id) {
		this.id = id;
	}


	public void setName(String name) {
		this.name = name;
	}

	
	public void setPicture(String picture) {
		this.picture = picture;
	}

	

}
