package com.oasis.sdk.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.base.http.CallbackResultForActivity;
import com.oasis.sdk.OASISPlatformConstant;
import com.oasis.sdk.base.entity.MemberBaseInfo;
import com.oasis.sdk.base.report.ReportUtils;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.DisplayUtil;
import com.oasis.sdk.base.utils.GuideView;
import com.oasis.sdk.base.utils.SystemCache;

public class OasisSdkPersonCenterActivity extends OasisSdkBaseActivity {

	private static final int LOGINREQUESTCODE = 10000001;
	private static final int PERSON_ITEM_REGIST = 1;
	private static final int PERSON_ITEM_CHANGEUSER = 2;
	private static final int PERSON_ITEM_PASSWORD = 3;
	private static final int PERSON_ITEM_CHARGELOG = 4;
//	private static final int PERSON_ITEM_BBS = 5;
	private static final int PERSON_ITEM_CUSTOMER = 6;
	private static final int PERSON_ITEM_ACCOUNTINFO = 7;
	private static final int PERSON_ITEM_GIFT = 8;
	
	private TextView tv_pic, tv_user, tv_uid, tv_changeUser;
//	private LinearLayout btn_luntan, btn_customer, ll_list, ll_list_other;
	MyHandler myHandler;
	GuideView guide;
	View epinView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		init();
	}
	private void init(){// 根据用户类型，选择合适的布局（匿名账户即游客 需要特殊处理）
		setContentView(BaseUtils.getResourceValue("layout", (SystemCache.userInfo==null || SystemCache.userInfo.loginType==1)?"oasisgames_sdk_pcenter_guest":"oasisgames_sdk_pcenter"));
		
		initHead(true, null, false, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pcenter_notice_2")));
		
		myHandler = new MyHandler(this);
		setWaitScreen(false);
	}
	private void setUserInfo(){
		tv_pic = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pcenter_pic"));
		tv_user = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pcenter_user"));
		tv_uid = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pcenter_uid"));
		
		if(SystemCache.userInfo == null || SystemCache.userInfo.loginType == 1){
			tv_pic.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_login_button_guest"));
			tv_user.setText(getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pcenter_notice_10")));
		}else if(SystemCache.userInfo.loginType == 2){
			tv_pic.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_login_button_oas"));
			tv_user.setText(TextUtils.isEmpty(SystemCache.userInfo.username)?MemberBaseInfo.USER_OASIS:SystemCache.userInfo.username);
		}else if(SystemCache.userInfo.loginType == 3){
			if(MemberBaseInfo.USER_FACEBOOK.equalsIgnoreCase(SystemCache.userInfo.platform)){
				tv_pic.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_login_button_facebook"));
				tv_user.setText(TextUtils.isEmpty(SystemCache.userInfo.oasnickname)?MemberBaseInfo.USER_FACEBOOK:SystemCache.userInfo.oasnickname);
			}
			if(MemberBaseInfo.USER_GOOGLE.equalsIgnoreCase(SystemCache.userInfo.platform)){
				tv_pic.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_login_button_google"));
				tv_user.setText(TextUtils.isEmpty(SystemCache.userInfo.username)?MemberBaseInfo.USER_GOOGLE:SystemCache.userInfo.username);
			}
			
		}
		if(SystemCache.userInfo != null)
			tv_uid.setText("UID:"+SystemCache.userInfo.uid);
	}
	
	private void initFuc(){
		
		List<PCenterFunEntity> list = new ArrayList<PCenterFunEntity>();
		
		PCenterFunEntity en = new PCenterFunEntity();
		if(SystemCache.userInfo != null && SystemCache.userInfo.loginType == 1 && SystemCache.controlInfo.getReg_onoff_control()){
			en.id = PERSON_ITEM_REGIST;
			en.img = "oasisgames_sdk_pcenter_item_"+PERSON_ITEM_REGIST;
			en.title = "oasisgames_sdk_pcenter_notice_11";
			en.notice = "";
			list.add(en);
		}
		
		if(SystemCache.userInfo != null && SystemCache.controlInfo.getSwitching_onoff_control() && SystemCache.userInfo.loginType != 1){// 匿名账号特殊处理，此处增加判断处理
			en = new PCenterFunEntity();
			en.id = PERSON_ITEM_CHANGEUSER;
			en.img = "oasisgames_sdk_pcenter_item_"+PERSON_ITEM_CHANGEUSER;
			en.title = "oasisgames_sdk_head_title_changeuser";
			en.notice = "";
			list.add(en);
		}
		
		if(SystemCache.userInfo != null && SystemCache.userInfo.loginType == 2){
			en = new PCenterFunEntity();
			en.id = PERSON_ITEM_PASSWORD;
			en.img = "oasisgames_sdk_pcenter_item_"+PERSON_ITEM_PASSWORD;
			en.title = "oasisgames_sdk_pcenter_notice_3";
			en.notice = "";
			list.add(en);
		}
		
		en = new PCenterFunEntity();
		en.id = PERSON_ITEM_CHARGELOG;
		en.img = "oasisgames_sdk_pcenter_item_"+PERSON_ITEM_CHARGELOG;
		en.title = "oasisgames_sdk_pcenter_notice_4";
		en.notice = "";
		list.add(en);
		
		if(SystemCache.userInfo != null && SystemCache.userInfo.loginType == 1 && SystemCache.controlInfo.getCustom_onoff_control()){
			en = new PCenterFunEntity();
			en.id = PERSON_ITEM_CUSTOMER;
			en.img = "oasisgames_sdk_pcenter_item_"+PERSON_ITEM_CUSTOMER;
			en.title = "oasisgames_sdk_pcenter_notice_6";
			en.notice = "";
			if(SystemCache.userInfo.isShowCustomerNewsFlag)
				en.isTag = true;
			else
				en.isTag = false;
			list.add(en);
		}
		
		if(SystemCache.userInfo != null && SystemCache.userInfo.loginType == 1 && SystemCache.controlInfo.getEpin_onoff_control()){// 匿名账号（游客）类型时，页面特殊处理
			en = new PCenterFunEntity();
			en.id = PERSON_ITEM_GIFT;
			en.img = "oasisgames_sdk_pcenter_item_"+PERSON_ITEM_GIFT;
			en.title = "oasisgames_sdk_pcenter_notice_12";
			en.notice = "";
			list.add(en);
		}
			
		updateView(list, "oasisgames_sdk_pcenter_fuc");
		
	}
	private void initOtherFuc(){
		
		List<PCenterFunEntity> list = null;
		if(SystemCache.userInfo != null && SystemCache.userInfo.loginType == 1){// 匿名账号（游客）类型时，页面特殊处理
			updateView(list, "oasisgames_sdk_pcenter_fuc_other");
			return;
		}
		
		list = new ArrayList<PCenterFunEntity>();
		PCenterFunEntity en = new PCenterFunEntity();
		
		if(SystemCache.userInfo != null && SystemCache.controlInfo.getUserinfo_onoff_control() && SystemCache.userInfo.loginType == 2){// OAS 账号才显示
			en = new PCenterFunEntity();
			en.id = PERSON_ITEM_ACCOUNTINFO;
			en.img = "oasisgames_sdk_pcenter_item_"+PERSON_ITEM_ACCOUNTINFO;
			en.title = "oasisgames_sdk_pcenter_notice_7";
			en.notice = "";
			list.add(en);			
		}

		if(SystemCache.userInfo != null && SystemCache.controlInfo.getCustom_onoff_control()){
			en = new PCenterFunEntity();
			en.id = PERSON_ITEM_CUSTOMER;
			en.img = "oasisgames_sdk_pcenter_item_"+PERSON_ITEM_CUSTOMER;
			en.title = "oasisgames_sdk_pcenter_notice_6";
			en.notice = "";
			if(SystemCache.userInfo.isShowCustomerNewsFlag)
				en.isTag = true;
			else
				en.isTag = false;
			list.add(en);
		}

		if(SystemCache.userInfo != null && SystemCache.controlInfo.getEpin_onoff_control()){
			en = new PCenterFunEntity();
			en.id = PERSON_ITEM_GIFT;
			en.img = "oasisgames_sdk_pcenter_item_"+PERSON_ITEM_GIFT;
			en.title = "oasisgames_sdk_pcenter_notice_12";
			en.notice = "";
			list.add(en);
		}
		
		updateView(list, "oasisgames_sdk_pcenter_fuc_other");
		
	}
	
	private void updateView(List<PCenterFunEntity> list, String layoutId){
		LinearLayout layout = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", layoutId));
		if(list == null || list.size() <= 0){
			layout.setVisibility(View.GONE);
			return;
		}
		if(layout.getChildCount() > 0)
			layout.removeAllViews();
		int size = list.size();
		for (int i = 0; i < size; i++) {
			final PCenterFunEntity info = list.get(i);
			View view = null;
			if(info.id == PERSON_ITEM_REGIST)
				view = getLayoutInflater().inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_pcenter_fuc_item_connect"), null);
			else
				view = getLayoutInflater().inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_pcenter_fuc_item"), null);
			TextView img = (TextView) view.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pcenter_fuc_item_img"));
			TextView title = (TextView) view.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pcenter_fuc_item_title"));
			TextView notice = (TextView) view.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pcenter_fuc_item_notice"));
			TextView tag = (TextView) view.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pcenter_fuc_item_tag"));
			
			if(!TextUtils.isEmpty(info.img))
				img.setBackgroundResource(BaseUtils.getResourceValue("drawable", info.img));
			title.setText(getString(BaseUtils.getResourceValue("string", info.title)));
			if(!TextUtils.isEmpty(info.notice))
				notice.setText(getString(BaseUtils.getResourceValue("string", info.notice)));
			else
				notice.setText("");
			
			if(info.isTag)
				tag.setVisibility(View.VISIBLE);
			else
				tag.setVisibility(View.GONE);
			view.setTag(info.id);
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					handlerItem(info.id);
				}
			});
			layout.addView(view);
			
			if(i+1 < size){
				LinearLayout line = new LinearLayout(this.getApplicationContext());
				line.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(2, BaseUtils.getDensity())));
				line.setBackgroundResource(BaseUtils.getResourceValue("color", "oasisgames_sdk_color_list_divider_d1d1d1"));
				layout.addView(line);
			}
			
			if(info.id == PERSON_ITEM_GIFT)
				epinView = view;
		}
	}
	private void initGuestInfo(){
		if(SystemCache.userInfo.loginType == 1){
			tv_changeUser = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pcenter_fuc_changeuser"));
			if(SystemCache.controlInfo.getSwitching_onoff_control())
				tv_changeUser.setVisibility(View.VISIBLE);
			else
				tv_changeUser.setVisibility(View.INVISIBLE);
			tv_changeUser.setText(Html.fromHtml("<html><u>"+getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_head_title_changeuser"))+"</u></html>"));
			tv_changeUser.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					handlerItem(PERSON_ITEM_CHANGEUSER);
				}
			});
		}
		LinearLayout btn_guide = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_function"));
		btn_guide.getChildAt(0).setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_common_head_function"));
		if(SystemCache.userInfo.loginType == 1 && (SystemCache.controlInfo.getReg_onoff_control() || SystemCache.controlInfo.getSwitching_onoff_control()))
			btn_guide.setVisibility(View.VISIBLE);
		else
			btn_guide.setVisibility(View.INVISIBLE);
		btn_guide.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 显示引导
				if(SystemCache.userInfo.loginType == 1)
					showGuide(0);
				else
					showGuide(2);
			}
		});

		if(SystemCache.userInfo.loginType != 1 && !SystemCache.controlInfo.getEpin_onoff_control()){
			btn_guide.setVisibility(View.INVISIBLE);
			return;
		}
		
		long count = (Long) BaseUtils.getSettingKVPfromSysCache("PCENTERGUIDECOUNT", 0L);
		long count2 = (Long) BaseUtils.getSettingKVPfromSysCache("PCENTERGUIDECOUNTEPIN", 0L);
		if(count <= 0 && SystemCache.userInfo.loginType == 1)
			myHandler.sendEmptyMessageDelayed(0, 500);
		else if(count2 <= 0)// 以前没有展示过，第一次显示)
			myHandler.sendEmptyMessageDelayed(1, 500);
			
	}
	/**
	 * 显示引导信息
	 * @param index 引导索引
	 */
	private void showGuide(int index){
		final ScrollView sv = (ScrollView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pcenter_scrollview"));
		guide = new GuideView(this.getApplicationContext());
		Rect r = new Rect();
		if(index == 0){ // 关联账号
			if(SystemCache.controlInfo.getReg_onoff_control()){// 关联的开关为 开
				sv.scrollTo(0, 0);
				LinearLayout layout = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pcenter_fuc"));
				View v = layout.getChildAt(0);
				v.getGlobalVisibleRect(r);
				guide.setPoint(r, v.getHeight(), v.getWidth(),
						getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pcenter_notice_8")));
				guide.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						guide.setVisibility(View.GONE);
						showGuide(1);
					}
				});
				this.addContentView(guide, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));
			}else{
				if(SystemCache.controlInfo.getSwitching_onoff_control()){// 切换账号的开关为 开
					showGuide(1);
				}else if(SystemCache.controlInfo.getEpin_onoff_control()){// 兑换礼品（EPin）的开关为 开
					showGuide(2);
				}
			}
		}
		if(index == 1 ){ // 切换账号的引导
			if(SystemCache.controlInfo.getSwitching_onoff_control()){// 切换账号的开关为 开
				sv.scrollTo(0, sv.getHeight());
				tv_changeUser.getGlobalVisibleRect(r);
				guide.setPoint(r,
						tv_changeUser.getHeight(), tv_changeUser.getWidth(),
						getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pcenter_notice_9")));
				guide.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						guide.setVisibility(View.GONE);
						if(SystemCache.controlInfo.getEpin_onoff_control())
							showGuide(2);
						else
							sv.scrollTo(0, 0);// 结束后，滚回（0，0）位置
					}
				});
				this.addContentView(guide, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));
			}else{
				if(SystemCache.controlInfo.getEpin_onoff_control())
					showGuide(2);
				else
					sv.scrollTo(0, 0);// 结束后，滚回（0，0）位置
			}
		}
		long epinShowCount =(Long) BaseUtils.getSettingKVPfromSysCache("PCENTERGUIDECOUNTEPIN", 0L);
		if(index == 2 && SystemCache.controlInfo.getEpin_onoff_control() && epinView != null && epinShowCount <= 0){ // 兑换礼品 
			sv.scrollTo(0, sv.getHeight());
			epinView.getGlobalVisibleRect(r);
			guide.setPoint(r,
					epinView.getHeight(), epinView.getWidth(),
					getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pcenter_notice_13")));
			guide.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					guide.setVisibility(View.GONE);
					sv.scrollTo(0, 0);// 结束后，滚回（0，0）位置
				}
			});
			this.addContentView(guide, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));
			BaseUtils.saveSettingKVPtoSysCache("PCENTERGUIDECOUNTEPIN", 1L);
		}
		
		long count =(Long) BaseUtils.getSettingKVPfromSysCache("PCENTERGUIDECOUNT", 0L);
		if(count == 0){ 
			BaseUtils.saveSettingKVPtoSysCache("PCENTERGUIDECOUNT", 1L);
		}
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		init();
		setUserInfo();
		initFuc();
		initOtherFuc();
		initGuestInfo();
		HttpService.instance().getNewsInfo(new MyCallback());
		
	}
	
	class MyCallback implements CallbackResultForActivity{

		@Override
		public void success(Object data, String statusCode, String msg) {
			if(SystemCache.userInfo == null || !SystemCache.userInfo.isShowCustomerNewsFlag
					|| isPageClose())
				return;
			
			LinearLayout layout = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pcenter_fuc_other"));
			int count = layout.getChildCount();
			for (int i = 0; i < count; i++) {
				View v  = layout.getChildAt(i);
				if(v.getTag() != null && v.getTag().equals(PERSON_ITEM_CUSTOMER)){
					
					TextView tag = (TextView) v.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pcenter_fuc_item_tag"));

					if(SystemCache.userInfo.isShowCustomerNewsFlag){
						tag.setVisibility(View.VISIBLE);
					}else{
						tag.setVisibility(View.GONE);
					}
					break;
				}
			}
			
				
		}

		@Override
		public void excetpion(Exception e) {
			if(SystemCache.userInfo != null)
				SystemCache.userInfo.isShowCustomerNewsFlag = false;
		}

		@Override
		public void fail(String statusCode, String msg) {
			if(SystemCache.userInfo != null)
				SystemCache.userInfo.isShowCustomerNewsFlag = false;
		}
		
	}
	public static final int MIN_CLICK_DELAY_TIME = 1000;
	private long lastClickTime = 0;
	private int oldID = 0;
	void handlerItem(int id){
		long currentTime = Calendar.getInstance().getTimeInMillis();
		if (currentTime - lastClickTime < MIN_CLICK_DELAY_TIME && oldID == id) { // MIN_CLICK_DELAY_TIME 内，同一个操作只能触发一次
			return;
		}else{
			lastClickTime = currentTime;
			oldID = id;			
		}
		String trackName = "";
		switch (id) {
		case PERSON_ITEM_REGIST:
			// 注册
			startActivity(new Intent(this.getApplicationContext(), OasisSdkBindActivity.class));
			trackName = ReportUtils.DEFAULTEVENT_CLICK_OG_REGIST;
			break;
		case PERSON_ITEM_CHANGEUSER:
			// 切换账号
//			startActivity(new Intent(this, OasisSdkLoginActivity.class).putExtra("uitype", "4"));
			startActivityForResult(new Intent(this.getApplicationContext(), OasisSdkLoginActivity.class).putExtra("uitype", "9"), LOGINREQUESTCODE);
			trackName = ReportUtils.DEFAULTEVENT_CLICK_OG_CHANGE;
			break;
		case PERSON_ITEM_PASSWORD:
			// 修改密码
			startActivity(new Intent(this.getApplicationContext(), OasisSdkModifyActivity.class));
			break;
		case PERSON_ITEM_CHARGELOG:
			// 充值记录
			startActivity(new Intent(this.getApplicationContext(), OasisSdkPayHistoryActivity.class));
			break;
//		case PERSON_ITEM_BBS:
//			// 论坛
//			BaseUtils.showMsg(this, "论坛");
//			googleLogin();
//			break;
		case PERSON_ITEM_CUSTOMER:
			// 客服
			startActivity(new Intent(this.getApplicationContext(), OasisSdkCustomerServiceListActivity.class));
			break;
		case PERSON_ITEM_ACCOUNTINFO:
			// 账户资料
			startActivity(new Intent(this.getApplicationContext(), OasisSdkWebActivity.class).putExtra("type", 1));
			break;
		case PERSON_ITEM_GIFT:
			// 兑换礼品
			if(SystemCache.userInfo == null || TextUtils.isEmpty(SystemCache.userInfo.serverID))
				BaseUtils.showMsg(this, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pcenter_notice_14")));
			else
				startActivity(new Intent(this.getApplicationContext(), OasisSdkPayEpinActivity.class));
			break;

		default:
			break;
		}
		try {// 发送Mdata信息
			if(!TextUtils.isEmpty(trackName))
				ReportUtils.add(trackName, new ArrayList<String>(), new ArrayList<String>());
		} catch (Exception e) {
		}
	}
	
	public static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkPersonCenterActivity> mOuter;

		public MyHandler(OasisSdkPersonCenterActivity activity) {
			mOuter = new WeakReference<OasisSdkPersonCenterActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			OasisSdkPersonCenterActivity outer = mOuter.get();
			switch (msg.what) {
			case 0:
				outer.showGuide(0);
				break;
			case 1:
				outer.showGuide(2);
				break;

			default:
				break;
			}
		}
	}
	static class ViewHoder{
		TextView img;
		TextView title;
		TextView notice;
		TextView tag;
	}
	class PCenterFunEntity{
		int id;
		String img;
		String title;
		String notice;
		boolean isTag;
	}
//	private void googleLogin(){
//		setWaitScreen(true);
//		GoogleUtils.instance(this).login(this, new GoogleUtils.GoogleLoginCallback(){
//			@Override
//			public void success(Person p, String email, String token) {
//				String personName = "";
//				if(p != null)
//					personName = p.getDisplayName();
////				BaseUtils.showMsg(OasisSdkPersonCenterActivity.this, "name="+personName);
//				System.out.println("========Name="+personName +"; email="+email+";  token="+token);
//				
//				setWaitScreen(false);
//			}
//
//			@Override
//			public void exception(Exception e) {
//				
//				if(e instanceof UserRecoverableAuthException){
//					Log.e(TAG, "Google Exception:UserRecoverableAuthException ");
//					e.printStackTrace();
//				}else if(e instanceof GoogleAuthException){
//					Log.e(TAG, "Google Exception:GoogleAuthException ");
//					e.printStackTrace();
//				}else if(e instanceof IOException){
//					Log.e(TAG, "Google Exception:IOException ");
//					e.printStackTrace();
//				}
//				
//			}
//			
//		});
//	}
	@Override
	protected void onDestroy() {
//		GoogleUtils.instance(this).clear();
		super.onDestroy();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//		if(requestCode == GoogleUtils.REQUEST_CODE_RESOLVE_ERR && resultCode == Activity.RESULT_OK){
//			googleLogin();
//		}else if (requestCode == GoogleUtils.REQUEST_CODE_RESOLVE_ERR && resultCode == RESULT_CANCELED) {
//			setWaitScreen(false);
//		}

		if(requestCode == LOGINREQUESTCODE 
				&& resultCode == OASISPlatformConstant.RESULT_SUCCESS){
			setWaitScreen(false);
			finish();
		}
	}
}
