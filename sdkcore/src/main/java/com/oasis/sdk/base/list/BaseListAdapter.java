package com.oasis.sdk.base.list;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.oasis.sdk.base.utils.BaseUtils;

/**
 * ListView使用的Adapter的基类，理论上所有的ListView Adapter都继承这个Adapter.
 * 
 * @author xdb
 * 
 * @param <T>
 */
public abstract class BaseListAdapter<T> extends BaseAdapter {

	protected Activity activity;
	public List<T> data;
	public int pages = 1;
	public int currentPage = 1;
	protected LinearLayout footerView;
	protected int moreLayout, endLayout;

	/**
	 * 构造方法.
	 * 
	 * @param activity
	 * @param data
	 *            数据
	 * @param count
	 *            总页数，不分页传1
	 * @param footerView
	 *            列表脚布局对象
	 * @param moreLayout
	 *            列表脚布局对象
	 * @param endLayout
	 *            列表脚布局对象
	 */
	public BaseListAdapter(Activity activity, List<T> data, int count, LinearLayout footerView, 
			int moreLayout, int endLayout) {
		this.activity = activity;
		this.data = data;
		this.pages = count;
		this.footerView = footerView;
		this.moreLayout = moreLayout;
		this.endLayout = endLayout;
	}
	public BaseListAdapter(Activity activity, List<T> data, int count, LinearLayout footerView) {
		this.activity = activity;
		this.data = data;
		this.pages = count;
		this.footerView = footerView;
	}

	@Override
	public T getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getCount() {
		if (null != data && data.size() > 0)
			return data.size();
		else
			return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (data.size() - 1 == position) {
			if (pages == currentPage) {
				if (null != footerView) {
					footerView.removeAllViews();
					if(endLayout != 0)
					footerView.addView(BaseUtils.getEndViewFootView(activity, endLayout), new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
				}
			} else {
				this.loadMore();
				if (null != footerView) {
					footerView.removeAllViews();
					if(moreLayout != 0)
					footerView.addView(BaseUtils.getLoadMoreFootView(activity, moreLayout), new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
				}
			}
		}
		return getRowView(position, convertView, parent);
	}

	/**
	 * 获取更多数据的方法.(子类实现)
	 */
	public abstract void loadMore();

	/**
	 * 获取每一行显示的内容.(子类实现)
	 * 
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return
	 */
	public abstract View getRowView(int position, View convertView, ViewGroup parent);
}
