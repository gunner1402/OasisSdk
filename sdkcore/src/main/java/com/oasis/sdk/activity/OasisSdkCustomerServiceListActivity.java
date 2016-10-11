package com.oasis.sdk.activity;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.base.http.CallbackResultForActivity;
import com.oasis.sdk.base.entity.QuestionList;
import com.oasis.sdk.base.entity.QuestionType;
import com.oasis.sdk.base.list.QuestionListAdapter;
import com.oasis.sdk.base.list.QuestionTypeAdapter;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.BaseUtils;
/**
 * 客服问题列表
 * @author xdb
 */
public class OasisSdkCustomerServiceListActivity extends OasisSdkBaseActivity {

	final String TAG = OasisSdkCustomerServiceListActivity.class.getName();
	final int TIMES = 30000;// 30秒
	final int PAGESIZE = 50;// 2015-10-14 将默认每页10调整为50条
	final int FUNC_ING = 1;
	final int FUNC_ED = 2;
	
	int loadIndex = 0;
	
	int curFuncType = FUNC_ING;
	QuestionList data_ing, data_ed;

	TextView tv_ing, tv_ed, view_empty;
	ListView lv_all;
	QuestionListAdapter adapter;
	LinearLayout footview; 
	
	List<QuestionType> questionType;
	AlertDialog questionTypeDialog;
	
	MyHandler myHandler;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(BaseUtils.getResourceValue(this, "layout", "oasisgames_sdk_customer_service_list"));
		initHead(true, null, false, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_head_title_customer")));
		
		myHandler = new MyHandler(this);
		
		init();

	}

	/**
	 * 切换问题类型，并加载相应的数据
	 */
	private void changeFuncView(int type){
		// 更新 解决中、已关闭 的样式
		if(FUNC_ING == type){
			tv_ing.setTextColor(getResources().getColor(BaseUtils.getResourceValue("color", "oasisgames_sdk_color_font_FFFFFF")));
			tv_ing.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_common_tab_bg_focus"));
			tv_ed.setTextColor(getResources().getColor(BaseUtils.getResourceValue("color", "oasisgames_sdk_color_font_666666")));
			tv_ed.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_common_tab_bg_unfocus"));
			
		}else if(FUNC_ED == type){
			tv_ing.setTextColor(getResources().getColor(BaseUtils.getResourceValue("color", "oasisgames_sdk_color_font_666666")));
			tv_ing.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_common_tab_bg_unfocus"));
			tv_ed.setTextColor(getResources().getColor(BaseUtils.getResourceValue("color", "oasisgames_sdk_color_font_FFFFFF")));
			tv_ed.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_common_tab_bg_focus"));
		}
		
		curFuncType = type;
	}
	
	public void loadQuestionList(){
		if(loadIndex > 0)
			return;
		loadIndex = 0;
		HttpService.instance().getCustomerServiceQuestionList(FUNC_ING, 1, PAGESIZE, new MyQuestionListCallback());
		HttpService.instance().getCustomerServiceQuestionList(FUNC_ED, 1, PAGESIZE, new MyQuestionListCallback());
	}
	
	class MyQuestionListCallback implements CallbackResultForActivity{

		@Override
		public void success(Object data, String statusCode, String msg) {
//			PayHistoryList list = (PayHistoryList) data;
//			
//			for (PayHistoryInfoDetail iter : list.msg) {
//				System.out.println(iter.pay_way + "  "+iter.currency +"  "+iter.game_coins);
//			}
			
			QuestionList qlist = (QuestionList) data;
			if(qlist == null){
//				List<QuestionInfo> list = new ArrayList<QuestionInfo>();
//				adapter = new QuestionListAdapter(OasisSdkCustomerServiceListActivity.this, list, 0, null);
				
				BaseUtils.logDebug(TAG, "加载数据失败 ");
				return ;
			}
			
			int dataType = Integer.valueOf(qlist.QuestionStatus);
			if(dataType == FUNC_ING){
				data_ing = qlist;
			}else if(dataType == FUNC_ED){
				data_ed = qlist;
			}

			loadIndex ++;
			
			if(loadIndex >= 2){
				setWaitScreen(false);
	//			changeFuncView(dataType);
				updateAdapter(dataType);
				loadIndex = 0;
				
				myHandler.sendEmptyMessageDelayed(0, TIMES);
			}
			
		}

		@Override
		public void excetpion(Exception e) {
			loadIndex ++;
			
			if(loadIndex >= 2){
				setWaitScreen(false);
				BaseUtils.showMsg2(OasisSdkCustomerServiceListActivity.this, "oasisgames_sdk_login_notice_autologin_exception");// 网络异常
				loadIndex = 0;
				myHandler.sendEmptyMessageDelayed(0, TIMES);
			}
		}

		@Override
		public void fail(String statusCode, String msg) {
			loadIndex ++;
			
			if(loadIndex >= 2){
				setWaitScreen(false);
				BaseUtils.showMsg2(OasisSdkCustomerServiceListActivity.this, "oasisgames_sdk_error_exception");
				loadIndex = 0;
				myHandler.sendEmptyMessageDelayed(0, TIMES);
			}
		}
		
	}
	static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkCustomerServiceListActivity> mOuter;

		public MyHandler(OasisSdkCustomerServiceListActivity activity) {
			mOuter = new WeakReference<OasisSdkCustomerServiceListActivity>(activity);
		}
		@Override
		public void dispatchMessage(Message msg) {
			if(msg.what == 0 && mOuter.get() != null && !mOuter.get().isPageClose())
				mOuter.get().loadQuestionList();
		}
	}
	class MyQuestionTypeCallback implements CallbackResultForActivity{
		
		@SuppressWarnings("unchecked")
		@Override
		public void success(Object data, String statusCode, String msg) {
			questionType = (List<QuestionType>) data;
			showQuestionTypeWindow();
			setWaitScreen(false);
		}
		
		@Override
		public void excetpion(Exception e) {
//			showQuestionTypeWindow();
			setWaitScreen(false);
			BaseUtils.showMsg2(OasisSdkCustomerServiceListActivity.this, "oasisgames_sdk_login_notice_autologin_exception");// 网络异常，请稍后重试
		}
		
		@Override
		public void fail(String statusCode, String msg) {
			setWaitScreen(false);
			BaseUtils.showMsg2(OasisSdkCustomerServiceListActivity.this, "oasisgames_sdk_error_exception");// 发生异常，请稍后重试
		}
	}
	class CreateQuestionCallback implements CallbackResultForActivity{
		// 根据问题类型，创建问题，获取问题id
		@Override
		public void success(Object data, String statusCode, String msg) {
			// 创建qid成功后
			setWaitScreen(false);
			
			startActivity(new Intent(OasisSdkCustomerServiceListActivity.this.getApplicationContext(), OasisSdkCustomerServiceQuestionLogActivity.class).putExtra("qid", (String)data).putExtra("questiontype", "new"));
		}
		
		@Override
		public void excetpion(Exception e) {
			setWaitScreen(false);
			BaseUtils.showMsg2(OasisSdkCustomerServiceListActivity.this, "oasisgames_sdk_login_notice_autologin_exception");// 网络异常，请稍后重试
		}
		
		@Override
		public void fail(String statusCode, String msg) {
			setWaitScreen(false);
			BaseUtils.showMsg2(OasisSdkCustomerServiceListActivity.this, "oasisgames_sdk_error_exception");// 发生异常，请稍后重试
		}
	}
	/**
	 * 更新adapter，刷新UI
	 * @param type
	 */
	private void updateAdapter(int type){
		if(curFuncType == FUNC_ING){
			if(data_ing != null)
				adapter = new QuestionListAdapter(OasisSdkCustomerServiceListActivity.this, data_ing.question_list, Integer.valueOf(data_ing.TotalPage), footview);
			else
				adapter = new QuestionListAdapter(OasisSdkCustomerServiceListActivity.this, null, 0, footview);
			lv_all.setAdapter(adapter);
		}else if(curFuncType == FUNC_ED){
			if(data_ed != null)
				adapter = new QuestionListAdapter(OasisSdkCustomerServiceListActivity.this, data_ed.question_list, Integer.valueOf(data_ed.TotalPage), footview);
			else
				adapter = new QuestionListAdapter(OasisSdkCustomerServiceListActivity.this, null, 0, footview);
			lv_all.setAdapter(adapter);
		}
		
		lv_all.setEmptyView(view_empty);
		adapter.notifyDataSetChanged();
	}
	/**
	 * 显示问题类型，供用户选择
	 * @param data
	 */
	private void showQuestionTypeWindow(){
		if(questionType == null || questionType.size() <= 0){
			BaseUtils.showMsg(this, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_customer_notice14")));
			return;
		}
		questionTypeDialog = new AlertDialog.Builder(this).show();
		questionTypeDialog.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_dialog_list"));

		LinearLayout buttons = (LinearLayout) questionTypeDialog.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_list_buttons"));
		buttons.getChildAt(0).setVisibility(View.GONE);
		buttons.getChildAt(1).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				questionTypeDialog.dismiss();//关闭 问题类型选择框
			}
		});
		ListView content = (ListView) questionTypeDialog.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_list_content"));
		
		QuestionTypeAdapter typeAdapter = new QuestionTypeAdapter(this, questionType, 1, null);
		content.setAdapter(typeAdapter);
		content.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				setWaitScreen(true);
				questionTypeDialog.dismiss();//先关闭 问题类型选择框
				HttpService.instance().createQuestion(questionType.get(position).id, new CreateQuestionCallback());
			}
		});
		
		
		questionTypeDialog.show();
				
	}
	private void init(){
		LinearLayout btn_chargePC = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_function"));
		btn_chargePC.setVisibility(View.VISIBLE);
		btn_chargePC.getChildAt(0).setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_customer_service_edit"));
		btn_chargePC.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadQuestionType();
			}
		});
		
		tv_ing = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_list_func_ing"));
		tv_ed = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_list_func_ed"));
		view_empty = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_list_empty"));
		
		lv_all = (ListView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_list_question"));
		
//		footview = new LinearLayout(OasisSdkCustomerServiceListActivity.this);
//		lv_all.addFooterView(footview);
		lv_all.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(adapter == null || position >= adapter.getCount()
						|| null == adapter.getItem(position)){
					return;
				}
				
				startActivity(new Intent(OasisSdkCustomerServiceListActivity.this.getApplicationContext(), OasisSdkCustomerServiceQuestionLogActivity.class).putExtra("qid", adapter.getItem(position).qid).putExtra("questiontype", "old").putExtra("questionstatus", curFuncType));
				
//				com.mopub.volley.toolbox.NetworkImageView imgView = (com.mopub.volley.toolbox.NetworkImageView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_list_item_img_details"));
//				imgView.setVisibility(View.VISIBLE);
//				imgView.setOnClickListener(new OnClickListener() {
//					
//					@Override
//					public void onClick(View v) {
//						v.setVisibility(View.INVISIBLE);
//					}
//				});
//				imgView.setImageUrl(list.get(position), mImageLoader);
			}
		});
//		
//		//get the app's available memory given by system,note that its unit is MB 
//		int memClass = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass(); 
//		System.out.println("memClass = "+memClass);
//		//or you can get the memory value by this 
//		memClass = (int) Runtime.getRuntime().maxMemory() ; 
//		System.out.println("memClass = "+memClass);
//		// Use 1/8th of the available memory for this memory cache. 
//		int cacheSize = memClass /(1024 * 1024 * 4); 
//		System.out.println("cacheSize = "+cacheSize);
//		mImageLoader = new ImageLoader(SystemCache.volleyRequestQueue, new BitmapLruCache(cacheSize)); 
		
	}
	private void loadQuestionType(){
		if(questionType == null || questionType.size() <= 0){
			setWaitScreen(true);
			HttpService.instance().getCustomerServiceQuestionType(new MyQuestionTypeCallback());
		}else{
			showQuestionTypeWindow();
		}
	}
	public void funcClickIng(View v){
		if(curFuncType == FUNC_ING)
			return;
		changeFuncView(FUNC_ING);
		updateAdapter(FUNC_ING);
	}
	public void funcClickEd(View v){
		if(curFuncType == FUNC_ED)
			return;

		changeFuncView(FUNC_ED);
		updateAdapter(FUNC_ED);
	}
	@Override
	protected void onResume() {
		super.onResume();
//		data_ing = null;
//		data_ed = null;
//		adapter = null;
		
		if(questionTypeDialog != null && questionTypeDialog.isShowing())
			questionTypeDialog.dismiss();
		
		setWaitScreen(true);
		if(myHandler != null && myHandler.hasMessages(0))
			myHandler.removeMessages(0);
		loadQuestionList();// 默认加载第一页(含 解决中、已关闭)
	}

	@Override
	protected void onPause() {
		if(myHandler != null && myHandler.hasMessages(0))
			myHandler.removeMessages(0);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if(myHandler != null && myHandler.hasMessages(0))
			myHandler.removeMessages(0);
		super.onDestroy();
	}
	
	public final static String[] imageThumbUrls = new String[] {
		"http://www.yzdsb.com.cn/pic/0/11/65/53/11655321_951970.jpg",
		"http://sd.china.com.cn/uploadfile/2015/0623/20150623021220623.jpg",
		"https://www.baidu.com/img/bd_logo1.png", 
		"http://img.my.csdn.net/uploads/201407/26/1406383299_1976.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383291_6518.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383291_8239.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383290_9329.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383290_1042.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383275_3977.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383265_8550.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383264_3954.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383264_4787.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383264_8243.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383248_3693.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383243_5120.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383242_3127.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383242_9576.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383242_1721.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383219_5806.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383214_7794.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383213_4418.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383213_3557.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383210_8779.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383172_4577.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383166_3407.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383166_2224.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383166_7301.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383165_7197.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383150_8410.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383131_3736.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383130_5094.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383130_7393.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383129_8813.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383100_3554.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383093_7894.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383092_2432.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383092_3071.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383091_3119.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383059_6589.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383059_8814.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383059_2237.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383058_4330.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383038_3602.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382942_3079.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382942_8125.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382942_4881.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382941_4559.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382941_3845.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382924_8955.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382923_2141.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382923_8437.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382922_6166.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382922_4843.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382905_5804.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382904_3362.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382904_2312.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382904_4960.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382900_2418.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382881_4490.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382881_5935.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382880_3865.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382880_4662.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382879_2553.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382862_5375.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382862_1748.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382861_7618.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382861_8606.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382861_8949.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382841_9821.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382840_6603.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382840_2405.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382840_6354.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382839_5779.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382810_7578.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382810_2436.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382809_3883.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382809_6269.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382808_4179.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382790_8326.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382789_7174.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382789_5170.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382789_4118.jpg", /*
		"http://img.my.csdn.net/uploads/201407/26/1406382788_9532.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382767_3184.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382767_4772.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382766_4924.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382766_5762.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382765_7341.jpg", */};
}