package com.oasis.sdk.base.list;




import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.oasis.sdk.base.utils.BaseUtils;


/**
 * @author Qiwenhao
 * @create 2016-6-8
 * @version 1.0
 * @desc 自定义Listview 上拉加载更多
 */

public class LoginHistoryListView extends ListView{



	public static final int LOAD = 1;
	private boolean isLoadFull = false;//判断是否完全加载
	private boolean isLoading = false;//判断是否为加载中
	private boolean loadEnable = true;//判断是否处于可加载状态
	
	private LinearLayout footer;//尾部布局

	private int pageSize = 10;//定义一页最多加载多少
	private ProgressBar footerloading;
	private ImageView  footerend;
//	private Animation operatingAnim;
	public LoginHistoryListView(Context context) {
		super(context);
		initView(context);
	}

	public LoginHistoryListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public LoginHistoryListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	private OnLoadListener onLoadListener;
	// 加载更多监听
	public void setOnLoadListener(OnLoadListener onLoadListener) {
		this.onLoadListener=onLoadListener;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	// 初始化组件
	private void initView(Context context) {
		footer = (LinearLayout) LayoutInflater.from(context).inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_login_history_footer"), null);
		footerloading = (ProgressBar) footer.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_history_loading"));
		footerend = (ImageView) footer.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_history_loadfull"));
		this.addFooterView(footer);		

		footer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

			}
		});
		this.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				ifNeedLoad(view, scrollState);
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
	}

	public void onLoad() {
		if (onLoadListener != null) {
			onLoadListener.onLoad();
		}
		
	}
	

	// 用于加载更多结束后的回调
	public void onLoadComplete(boolean isEnd) {
		isLoading = false;
		setResultSize(isEnd);
	}

	// 根据listview滑动的状态判断是否需要加载更多
	private void ifNeedLoad(AbsListView view, int scrollState) {
		if (!loadEnable) {
			return;
		}
		try {
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
					&& view.getLastVisiblePosition() == (view.getAdapter().getCount()-1) && !isLoadFull&&!isLoading) {
				onLoad();
				isLoading = true;
			}
		} catch (Exception e) {
		}
	}
	/**
	 * 这个方法是根据结果的大小来决定footer显示的。
	 * <p>
	 * 这里假定每次请求的条数为10。如果请求到了10条。则认为还有数据。如过结果不足10条，则认为数据已经全部加载，这时footer显示已经全部加载
	 * </p>
	 * 
	 * @param resultSize
	 */
	public void setResultSize(boolean isEnd) {
		if(!isEnd){
			isLoadFull = false;
			footerloading.setVisibility(View.VISIBLE);
			footerend.setVisibility(View.GONE);
		}else{
			isLoadFull = true;
			footerloading.setVisibility(View.GONE);			
			footerend.setVisibility(View.VISIBLE);
		}
	}
	/*
	 * 定义加载更多接口
	 */
	public interface OnLoadListener {
		public void onLoad();
	}


	
}
