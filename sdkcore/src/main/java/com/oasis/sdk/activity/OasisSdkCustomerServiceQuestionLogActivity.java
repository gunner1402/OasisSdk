package com.oasis.sdk.activity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.base.http.CallbackResultForActivity;
import com.mopub.volley.DefaultRetryPolicy;
import com.mopub.volley.Response;
import com.mopub.volley.Response.ErrorListener;
import com.mopub.volley.Response.Listener;
import com.mopub.volley.VolleyError;
import com.mopub.volley.toolbox.ImageRequest;
import com.oasis.sdk.base.entity.QuestionInfoLog;
import com.oasis.sdk.base.list.MsgListView;
import com.oasis.sdk.base.list.MsgListView.OnRefreshListener;
import com.oasis.sdk.base.list.QuestionLogListAdapter;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.ApplicationContextManager;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.SystemCache;
/**
 * 客服问题详细信息列表
 * @author xdb
 */
public class OasisSdkCustomerServiceQuestionLogActivity extends OasisSdkBaseActivity {

	private static final int SPACETIME_300 = 300;// 单位 秒, 5分钟
	private final int SPACETIME_10 = 10000;// 单位 毫秒秒, 10秒 ; 回复消息每10秒刷新一次
	private static final int RESULT_LOAD_IMAGE = 12346;
	public final String USER_PLAYER = "1";
	public final String USER_CUSTOMER = "2";
	public final String LOGTYPE_NOTICE = "-1";
	public final String LOGTYPE_WORD = "1";
	public final String LOGTYPE_IMG = "2";
	final String TAG = OasisSdkCustomerServiceQuestionLogActivity.class.getName();
	public final String NOTICETAG_CLOSED = "CUSTOMERNOTICETAGCLOSED";
	final int PAGESIZE = 10;// 默认每页10
	
	Boolean isLoading = false;//是否正在加载数据
	
	String qid;// 问题id
	String questiontype;// 问题类型  新创建或者已创建
	boolean isNew = false;// 是否第一次提交会话
	int questionstatus;// 问题状态  已关闭问题不能回复、消息最后显示提示信息
	EditText et_content ;// 内容
	
	boolean isFirstLoad = true;// 是否第一次加载数据，第一次加载成功后，才开始 定时查询最新信息

	List<QuestionInfoLog> infoListServers = new ArrayList<QuestionInfoLog>();	// 缓存每次从服务器获取的数据
	List<QuestionInfoLog> infoListForShow = new ArrayList<QuestionInfoLog>();	// 手机每次显示的数据
	List<QuestionInfoLog> infoListLocal = new ArrayList<QuestionInfoLog>();		// 本地预留数据。包括：已关闭提示、时间间隔提示
	List<Boolean> isSendingStatus = new ArrayList<Boolean>();//记录玩家正在发送请求的数量，为0个时，才开启“获取新信息”的查询 
	QuestionLogListAdapter adapter;
	MsgListView listView;
	
	MyHandler myHandler;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(BaseUtils.getResourceValue(this.getApplicationContext(), "layout", "oasisgames_sdk_customer_service_q_details"));
		initHead(true, null, false, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_customer_notice4")));

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		myHandler = new MyHandler(this);
		init();
		
		if(!isNew){// 新问题 首次没有数据加载 
			listView.onRefreshStart();
			refresh();// 首次加载
		}
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK){
//			Uri selectedImage = data.getData();
//	        String[] filePathColumn = { MediaStore.Images.Media.DATA };
//	 
//	        Cursor cursor = getContentResolver().query(selectedImage,
//	                filePathColumn, null, null, null);
//	        cursor.moveToFirst();
//	 
//	        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//	        String picturePath = cursor.getString(columnIndex);
//	        cursor.close();
//
//	        System.out.println("选择图片地址："+picturePath);
//	        updateUIViewIMG(picturePath);
			ArrayList<String> list = data.getStringArrayListExtra("data");
			if(list != null){
				
				Message msg = new Message();
				msg.what = 0;
				msg.obj = list;
				myHandler.sendMessage(msg);
				
			}
		}
		
	}
	private QuestionInfoLog createObject(String type, String str){
		QuestionInfoLog logs = new QuestionInfoLog();
		long time = new Date().getTime();
		logs.temp_benchid = ""+time;
		if(type.equals(LOGTYPE_WORD)){
			logs.content = str;
			logs.content_type = LOGTYPE_WORD;// 文字类型
		}else if(type.equals(LOGTYPE_IMG)){// 图片
			logs.content = "["+getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_customer_notice16"))+"]";// 上传图片是，默认content为“图片”
			logs.local_img_url = str;
			logs.content_type = LOGTYPE_IMG;
		}else if(type.equals(LOGTYPE_NOTICE)){// 提示
			logs.content = str;
			logs.content_type = LOGTYPE_NOTICE;
		}
		logs.usertype = USER_PLAYER;// 玩家
		logs.qid = qid;
		logs.uid = SystemCache.userInfo.uid;
		if(SystemCache.userInfo.loginType == 1)
			logs.nickname = "sdk_user_android";// 与createQuestion使用同样的昵称
		else if(SystemCache.userInfo.loginType == 2)
			logs.nickname = (TextUtils.isEmpty(SystemCache.userInfo.username))?SystemCache.userInfo.oasnickname:SystemCache.userInfo.username;
		else if(SystemCache.userInfo.loginType == 3){
			if(TextUtils.isEmpty(SystemCache.userInfo.oasnickname))
				logs.nickname = SystemCache.userInfo.platform;
			else
				logs.nickname = SystemCache.userInfo.oasnickname;
		}
		logs.create_time = ""+time/1000;
		logs.status = QuestionInfoLog.status_sending;
		return logs;
	}
	/**
	 * 选择其他方式，目前只有   本地图片
	 * @param v
	 */
	public void onClickToSelectOther(View v){
		InputMethodManager imm = (InputMethodManager)et_content.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(et_content.getWindowToken(),0);  
		
		LinearLayout other = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_question_detail_other"));
		if(other.isShown())
			other.setVisibility(View.GONE);
		else
			other.setVisibility(View.VISIBLE);
	}
	/**
	 * 选择本地图片
	 * @param v
	 */
	public void onClickToSelectPic(View v){
		findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_question_detail_other")).setVisibility(View.GONE);
		
		Intent i = new Intent();
		i.setClass(this.getApplicationContext(), OasisSdkPictureListActivity.class);
		startActivityForResult(i, RESULT_LOAD_IMAGE);
		
	}
	private void updateAdapter(int flag){
		infoListForShow.clear();
		Collections.sort(infoListServers, new Comparator<QuestionInfoLog>() {
			public int compare(QuestionInfoLog lhs, QuestionInfoLog rhs) {
				
				return lhs.qid.compareTo(rhs.qid);
			};
		});
		int size = infoListServers.size();
		for (int i = 0; i < size; i++) {
			QuestionInfoLog log = infoListServers.get(i);
			if(i == 0){
				infoListForShow.add(createObject(LOGTYPE_NOTICE, log.create_time));
				infoListForShow.add(log);
				continue;
			}
			QuestionInfoLog log2 = infoListServers.get(i-1);
			if(Long.valueOf(log.create_time) - Long.valueOf(log2.create_time) > SPACETIME_300){
				infoListForShow.add(createObject(LOGTYPE_NOTICE, log.create_time));
			}
			infoListForShow.add(log);
		}
		
		Collections.sort(infoListLocal, new Comparator<QuestionInfoLog>() {
			public int compare(QuestionInfoLog lhs, QuestionInfoLog rhs) {
				
				return lhs.create_time.compareTo(rhs.create_time);
			};
		});
		infoListForShow.addAll(infoListLocal);
		
		adapter.data = infoListForShow;
		adapter.notifyDataSetChanged();
		
		if(flag >= 1)// 玩家每次新提交数据时，列表自动定位到最后一个Item
			myHandler.sendEmptyMessageDelayed(11, 1000);
	}
	
	public void loadData(String baseid){
		if(isLoading){
			return;
		}
		isLoading = true;
//		setWaitScreen(true);
		HttpService.instance().getQuestionDetail(qid, baseid, 1, PAGESIZE, new QuestionDetailsListCallback());
	}
	
	class QuestionDetailsListCallback implements CallbackResultForActivity{

		@Override
		public void success(Object data, String statusCode, String msg) {

			isLoading = false;
			listView.onRefreshComplete();
			setWaitScreen(false);
			
			List<QuestionInfoLog> list = (List<QuestionInfoLog>) data;
			if(list == null || list.size() <= 0)
				return;
			Collections.reverse(list);
			
			for (QuestionInfoLog log : list) {
				infoListServers.add(0, log);// 正序排列，使最新数据放在屏幕下方
			}
			updateAdapter(0);

			if(isFirstLoad){
				isFirstLoad = false;
				
				if(questionstatus != 2)// 该问题为“解决中”时，再执行定时查询
					myHandler.sendEmptyMessageDelayed(10, SPACETIME_10);
				
				myHandler.sendEmptyMessageDelayed(11, 500);
			}
		}

		@Override
		public void excetpion(Exception e) {
			listView.onRefreshComplete();
			setWaitScreen(false);
			isLoading = false;
		}

		@Override
		public void fail(String statusCode, String msg) {
			listView.onRefreshComplete();
			setWaitScreen(false);
			isLoading = false;
		}
		
	}
	private void loadNewData(){
		if(infoListServers.size() <= 0){
			myHandler.sendEmptyMessageDelayed(10, SPACETIME_10);
			return;
		}
		
		// 默认去最后一条
		QuestionInfoLog lastLog = infoListServers.get(infoListServers.size()-1);
		
		HttpService.instance().getQuestionDetail(qid, lastLog.bench_qid, 2, PAGESIZE, new QuestionDetailsNewsListCallback());
	}
	
	class QuestionDetailsNewsListCallback implements CallbackResultForActivity{
		
		@Override
		public void success(Object data, String statusCode, String msg) {
			List<QuestionInfoLog> list = (List<QuestionInfoLog>) data;
			if(list != null && list.size() > 0){
				infoListServers.addAll(list);
				updateAdapter(1);
			}
			myHandler.sendEmptyMessageDelayed(10, SPACETIME_10);
		}
		
		@Override
		public void excetpion(Exception e) {
			myHandler.sendEmptyMessageDelayed(10, SPACETIME_10);
		}
		
		@Override
		public void fail(String statusCode, String msg) {
			myHandler.sendEmptyMessageDelayed(10, SPACETIME_10);
		}
		
	}
	
	public void showBigImage(QuestionInfoLog infolog){
		setWaitScreen(true);
		
		String imgPath = "";
		if(!TextUtils.isEmpty(infolog.local_img_url) && new File(infolog.local_img_url).exists()){// 本地图片地址不为空，优先获取本地图片
			imgPath = infolog.local_img_url;
			try {
				final Bitmap arg0 = BitmapFactory.decodeFile(imgPath);
				
				setWaitScreen(false);
				if(arg0 == null)
					return;
				final ImageView imgView = (ImageView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_question_detail_bigimage_local"));
				imgView.setVisibility(View.VISIBLE);
				imgView.setImageBitmap(arg0);
				imgView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						imgView.setImageBitmap(null);
						imgView.setVisibility(View.GONE);
						if(arg0 != null)
							arg0.recycle();
					}
				});
				imgView.postInvalidate();
			} catch (OutOfMemoryError e) {
			}catch (Exception e) {
			}
			return;
		}
		
		if(TextUtils.isEmpty(infolog.img_url)){
			setWaitScreen(false);
			BaseUtils.logError(TAG, "IMG address is null");
			return ;
		}
		imgPath = infolog.img_url;
		
		DisplayMetrics dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();
		
		ImageRequest iq = new ImageRequest(imgPath, 
				new Listener<Bitmap>() {

					@Override
					public void onResponse(final Bitmap arg0) {
						setWaitScreen(false);
						if(arg0 == null)
							return;
						final ImageView imgView = (ImageView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_question_detail_bigimage_local"));
						imgView.setVisibility(View.VISIBLE);
						imgView.setImageBitmap(arg0);
						imgView.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								imgView.setImageBitmap(null);
								imgView.setVisibility(View.GONE);
								if(arg0 != null)
									arg0.recycle();
							}
						});
						imgView.postInvalidate();
					}
				}, 
				dm.widthPixels, // 根据屏幕尺寸获取图片
				dm.heightPixels, 
				Config.ARGB_8888, 
				new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						
						setWaitScreen(false);
					}
					
				});
		iq.setRetryPolicy(new DefaultRetryPolicy(60000, 2, 1));
		ApplicationContextManager.getInstance().getVolleyRequestQueue().add(iq);
			
			
//		第2种
//		new LoadBigImageTask().execute((infolog.img_url != null && !infolog.img_url.isEmpty())?infolog.img_url:infolog.local_img_url);
		
//		第3种
//		if(infolog.img_url != null && !infolog.img_url.isEmpty() && (infolog.img_url.startsWith("http:") || infolog.img_url.startsWith("https:"))){
//			final com.mopub.volley.toolbox.NetworkImageView imgView = (com.mopub.volley.toolbox.NetworkImageView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_question_detail_bigimage_network"));
//			imgView.setVisibility(View.VISIBLE);
//			imgView.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					imgView.setVisibility(View.GONE);
//					
//				}
//			});
//			imgView.setImageUrl(infolog.img_url, mImageLoader);
//		}else{
//			final ImageView imgView = (ImageView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_question_detail_bigimage_local"));
//			imgView.setVisibility(View.VISIBLE);
//			
//			final Bitmap map = BitmapFactory.decodeFile(infolog.local_img_url);
//			if(map != null)
//				((ImageView)imgView).setImageBitmap(map);
//			else if(drawable != null)
//				((ImageView)imgView).setImageDrawable(drawable);
//			imgView.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					imgView.setVisibility(View.GONE);
//					if(map != null)
//						map.recycle();
//				}
//			});
//		}
	}
	
	static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkCustomerServiceQuestionLogActivity> mOuter;

		public MyHandler(OasisSdkCustomerServiceQuestionLogActivity activity) {
			mOuter = new WeakReference<OasisSdkCustomerServiceQuestionLogActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				List<String> list = (List<String>) msg.obj;
				int size = list.size();
				List<QuestionInfoLog> infoList = new ArrayList<QuestionInfoLog>();
				for (int i = 0; i < size; i++) {
					String picturePath = list.get(i);
					File f = new File(picturePath);
					if(!f.isFile() || !f.exists())
						continue;
					
					QuestionInfoLog info = mOuter.get().createObject(mOuter.get().LOGTYPE_IMG, picturePath); 
					mOuter.get().infoListLocal.add(info);
					mOuter.get().updateAdapter(1);
			        
			        infoList.add(info);
				}
				Message msgQInfo = new Message();
				msgQInfo.what = 1;
				msgQInfo.obj = infoList;
				msgQInfo.arg1 = 0;// 从0开始
				msgQInfo.arg2 = size;
				mOuter.get().myHandler.sendMessage(msgQInfo);
				break;
			case 1:
				List<QuestionInfoLog> infoLists = (List<QuestionInfoLog>) msg.obj;
				mOuter.get().publish(infoLists.get(msg.arg1));
				int newIndex = msg.arg1 + 1;// 下一张图片
				if(newIndex < msg.arg2){
					Message msgQInfo2 = new Message();
					msgQInfo2.what = 1;
					msgQInfo2.obj = infoLists;
					msgQInfo2.arg1 = newIndex;
					msgQInfo2.arg2 = msg.arg2;
					mOuter.get().myHandler.sendMessageDelayed(msgQInfo2, 1200);// 延迟1秒
				}
					
				break;
			case 10:
				if(mOuter.get() != null && !mOuter.get().isPageClose() && mOuter.get().isSendingStatus.isEmpty())// 页面未关闭 且 么有正在发送的信息
					mOuter.get().loadNewData();
				break;
			case 11:
				mOuter.get().listView.setSelection(mOuter.get().adapter.data.size()-1);
				break;
			default:
				break;
			}
		}
	}
	private void init(){
		qid = getIntent().getStringExtra("qid");
		questiontype = getIntent().getStringExtra("questiontype"); 
		questionstatus = getIntent().getIntExtra("questionstatus", 0); 
		if("new".equals(questiontype)){
			isNew = true;
		}
//		if("new".equals(questiontype)){// 新问题 不显示  "+" 按钮
//			TextView other = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_question_detail_other_add"));
//			other.setVisibility(View.GONE);
//		}
		
		if(questionstatus == 2){// 已关闭问题 不能回复，估隐藏相应布局
			LinearLayout layout = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_question_detail_chart"));
			layout.setVisibility(View.GONE);
			
		}
		
		listView = (MsgListView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_question_detail_listview"));
		listView.setonRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				refresh();
			}
		});
		View headView = getLayoutInflater().inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_customer_service_q_details_item_notice"), null);
		TextView headNotice = (TextView)headView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_q_details_item_notice_title"));
		headNotice.setText(getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_customer_notice3")));// 列表头部 显示“客服人员会尽快给您答复”
		listView.addHeaderView(headView);
		
		if(questionstatus == 2){// 已关闭问题 不能回复，估隐藏相应布局
			View footView = getLayoutInflater().inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_customer_service_q_details_item_notice"), null);
			TextView footnotice = (TextView)footView.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_q_details_item_notice_title"));
			footnotice.setText(getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_customer_notice15")));// 列表头部 显示“客服人员会尽快给您答复”
			listView.addFooterView(footView);
		}
		
		adapter = new QuestionLogListAdapter(this, infoListForShow, 1, null);
		listView.setAdapter(adapter);
		
		et_content = (EditText) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_question_detail_word_edit"));
		et_content.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {// 获得焦点后，隐藏图片选择布局
				LinearLayout other = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_question_detail_other"));
				other.setVisibility(View.GONE);
			}
		});
		
		et_content.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId==EditorInfo.IME_ACTION_SEND ||(event!=null&&event.getKeyCode()== KeyEvent.KEYCODE_ENTER)) {  
					String word = v.getText().toString().trim();
					
					if(TextUtils.isEmpty(word))
						return true;
					
					InputMethodManager imm = (InputMethodManager)et_content.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);  
					imm.hideSoftInputFromWindow(et_content.getWindowToken(),0);  
					
					final QuestionInfoLog log = createObject(LOGTYPE_WORD, word);
					
					publish(log);// 开始向服务器发送消息        
					
					infoListLocal.add(log);// 先添加本地缓存
					updateAdapter(1);// 新添数据，更新Adapter，列表滑至末尾

					et_content.setText("");// 输入框重新置空
					return true;
				}      
				return false;
			}
		});
		
//		//get the app's available memory given by system,note that its unit is MB 
//				int memClass = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass(); 
//				System.out.println("memClass = "+memClass);
//				//or you can get the memory value by this 
//				memClass = (int) Runtime.getRuntime().maxMemory() ; 
//				System.out.println("memClass = "+memClass);
//				// Use 1/8th of the available memory for this memory cache. 
//				int cacheSize = memClass /(1024 * 1024 * 4); 
//				System.out.println("cacheSize = "+cacheSize);
//				mImageLoader = new ImageLoader(SystemCache.volleyRequestQueue, new BitmapLruCache(cacheSize));
				
				
	}
//	private Long compareTime(int type, QuestionInfoLog newlog){
//		int count = contentView.getChildCount();
////		if(count <= 0)
////			return type == 1?0:Long.valueOf(newlog.create_time);// 代表第一个,如果是从网络获取，第一个总是显示日期；否则不显示日期
//		
//		View v = null;
//		QuestionInfoLog oldlog = null;
//		long oldtime = 0;
//		
//		int i = 0;// 循环计数
//		if(type == 0){
//			v = contentView.getChildAt(count-1);// 最后一个
//			oldlog = (QuestionInfoLog) v.getTag();
//			oldtime = Long.valueOf(oldlog.create_time);
//		}else if(type == 1){
//			for (i = 0; i < count; i++) {
//				v = contentView.getChildAt(i);// 按顺序取一个
//				oldlog = (QuestionInfoLog) v.getTag();
//				if(!LOGTYPE_NOTICE.equals(oldlog.content_type)){
//					oldtime = Long.valueOf(oldlog.create_time);
//					break;
//				}
//			}
//		}
//		
//		long newtime = Long.valueOf(newlog.create_time);
//		
//		long abs = Math.abs(newtime-oldtime);
//		
//		if(i > 0){
//			for (int j = i - 1; j >= 0; j--) {
//				v = contentView.getChildAt(j);// 按顺序取一个
//				if(LOGTYPE_NOTICE.equals(oldlog.content_type) && !oldlog.content.startsWith(NOTICETAG_CLOSED)){// 以 NOTICETAG_CLOSED 为开头的notice，是文字提示信息，不能删掉
//					contentView.removeViewAt(j);
//				}
//			}
//		}
//		if(abs > SPACETIME_300 )
//			return oldtime;
//		return 0L;
//	}
	
	public void publish(final QuestionInfoLog info){
		isSendingStatus.add(true);
		HttpService.instance().publishQuestionByReplay(
	        	info, 
	        	new Listener<String>() {

				@Override
				public void onResponse(String arg0) {
					try {
						JSONObject json = new JSONObject(arg0);
						if(QuestionInfoLog.status_ok.equalsIgnoreCase(json.getString("status"))){
							info.setStatus(QuestionInfoLog.status_ok);
							info.setBench_qid(json.getString("bench_qid"));
							info.setCreate_time(json.getString("create_time"));
							
							if(infoListLocal.contains(info)){
			            		infoListLocal.remove(info);
			            	}
			            	infoListServers.add(info);
						}else
							info.setStatus(QuestionInfoLog.status_fail);
					} catch (JSONException e) {
						info.setStatus(QuestionInfoLog.status_fail);
					}
					updateAdapter(0);
	            	BaseUtils.logDebug(TAG, "### response : " + arg0);
	            	
	            	if(QuestionInfoLog.status_ok.equals(info.status) && "new".equals(questiontype) && info.content_type.equals(LOGTYPE_WORD)){// 新问题 不显示 按钮
						TextView other = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_question_detail_other_add"));
						other.setVisibility(View.VISIBLE);
						questiontype = "old";
					}
	            	
	            	
	            	if(!isSendingStatus.isEmpty())
	            		isSendingStatus.remove(0);
	            	
	            	if(isSendingStatus.isEmpty())
	            		myHandler.sendEmptyMessageDelayed(10, SPACETIME_10);
	            	
				}
			},
			new ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError arg0) {
					info.setStatus(QuestionInfoLog.status_fail);
	            	updateAdapter(0);
	            	
	            	if(!isSendingStatus.isEmpty())
	            		isSendingStatus.remove(0);
	            	
	            	if(isSendingStatus.isEmpty())
	            		myHandler.sendEmptyMessageDelayed(10, SPACETIME_10);
//	                BaseUtils.logError(TAG, "onErrorResponse : " + arg0);						
				}
			});
	}
	
	class LoadBigImageTask extends AsyncTask<String, Integer, Bitmap>{

		@Override
		protected Bitmap doInBackground(String... params) {
			if(params[0].startsWith("http:") || params[0].startsWith("https:")){
				
				 HttpClient httpClient = new DefaultHttpClient();
		            HttpGet httpGet = new HttpGet(params[0]);
		            Bitmap bitmap = null;
		            try {
		                HttpResponse httpResponse = httpClient.execute(httpGet);
		                if (httpResponse.getStatusLine().getStatusCode() == 200) {
		                    HttpEntity httpEntity = httpResponse.getEntity();
		                    byte[] data = EntityUtils.toByteArray(httpEntity);
		                    bitmap = BitmapFactory
		                            .decodeByteArray(data, 0, data.length);
		                }
		            } catch (Exception e) {
		            }
		            return bitmap;
			}else{
				return BitmapFactory.decodeFile(params[0]);
			}
		}

		@Override
		protected void onPostExecute(final Bitmap result) {
			if(result != null){
				final ImageView imgView = (ImageView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_question_detail_bigimage_local"));
				imgView.setVisibility(View.VISIBLE);
				
				((ImageView)imgView).setImageBitmap(result);
				imgView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						imgView.setVisibility(View.GONE);
						imgView.setImageBitmap(null);
						if(result != null)
							result.recycle();
						
					}
				});
			}
			setWaitScreen(false);
		}
		
	}
	
	private void refresh() {
		String baseId = "";
		if(infoListServers.size() > 0){
			QuestionInfoLog log = infoListServers.get(0);
			baseId = log.bench_qid;
		}
		
		loadData(baseId);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
//		if(myHandler != null && myHandler.hasMessages(10))
//			myHandler.removeMessages(10);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if(myHandler != null && myHandler.hasMessages(10))
			myHandler.removeMessages(10);
//		int count = contentView.getChildCount();
//		for (int i = 0; i < count; i++) {
//			View v = contentView.getChildAt(i);
//			QuestionInfoLog log = (QuestionInfoLog) v.getTag();
//			if(log != null && LOGTYPE_IMG.equals(log.content_type)){
//				ImageView imgView = (ImageView) v.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_customer_service_q_details_item_content_image_local"));
//				imgView.setImageDrawable(null);
//			}
//		}
		super.onDestroy();
	}
	
	
}