package com.oasis.sdk.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.oasis.sdk.base.entity.PayHistoryInfoDetail;
import com.oasis.sdk.base.entity.PayHistoryList;
import com.oasis.sdk.base.list.PayHistoryListAdapter;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.BaseUtils;
/**
 * 支付历史记录
 * @author xdb
 *
 */
public class OasisSdkPayHistoryActivity extends OasisSdkBaseActivity  {
	public static final String TAG = OasisSdkPayHistoryActivity.class.getName();
	private static final int PAGESIZE = 10;
    
	MyHandler myHandler;
	
	private TextView tv_nodata;
	private View view_data;
	ListView listView;
	private PayHistoryList historyListEntity = new PayHistoryList();
	private PayHistoryListAdapter adapter = null;
	
//	List<PayHistoryInfoDetail> list = new ArrayList<PayHistoryInfoDetail>();
	
	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_pay_history"));
		myHandler = new MyHandler(this);
		
		initHead(true, null, false, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pcenter_notice_4")));// 充值记录
		init();
		
		setWaitScreen(true);
		
		loadData();
		
	}
	private void init(){
		view_data = findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_history_data"));
		tv_nodata = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_history_nodata"));
		
		listView = (ListView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pay_history_list"));
		if (Build.VERSION.SDK_INT >= 9) 
			listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		adapter = new PayHistoryListAdapter(this, new ArrayList<PayHistoryInfoDetail>(), 1000, null);
		listView.setAdapter(adapter);
		
	}
	
	public void loadData(){
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					historyListEntity = HttpService.instance().paymentLog(historyListEntity.page + 1, PAGESIZE);
				} catch (Exception e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				}

				myHandler.sendEmptyMessage(HANDLER_RESULT);
			}
		}).start();
	}
	
	public static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkPayHistoryActivity> mOuter;

		public MyHandler(OasisSdkPayHistoryActivity activity) {
			mOuter = new WeakReference<OasisSdkPayHistoryActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			OasisSdkPayHistoryActivity outer = mOuter.get();
			if (outer != null) {
				switch (msg.what) {
				case HANDLER_RESULT:
					
					if(outer.historyListEntity != null && outer.historyListEntity.msg != null && outer.historyListEntity.msg.size() > 0){
						
						outer.adapter.data.addAll(outer.historyListEntity.msg);
						outer.adapter.notifyDataSetChanged();
						outer.view_data.setVisibility(View.VISIBLE);
						outer.tv_nodata.setVisibility(View.GONE);
					}
					sendEmptyMessage(HANDLER_EXCEPTION);
					break;
				case HANDLER_SUCECCES:
					outer.setResult(RESULT_OK, null);
					outer.finish();
					
					break;
				case HANDLER_EXCEPTION:
					outer.setWaitScreen(false);
					if(outer.adapter.getCount() <= 0){
						outer.view_data.setVisibility(View.GONE);
						outer.tv_nodata.setVisibility(View.VISIBLE);
					}
					break;
				
				default:
					
					break;
				}
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
}
