package com.oasis.sdk.base.entity;

public class PageInfo {
	public int page;
	public int pageSize;
	public int pageCount;

	public boolean isNext(){
		if(pageCount != 0 && page < pageCount)
			return true;
		return false;
	}
	
	public boolean isEnd(){
		if(pageCount != 0 && page == pageCount)
			return true;
		return false;
	}
}
