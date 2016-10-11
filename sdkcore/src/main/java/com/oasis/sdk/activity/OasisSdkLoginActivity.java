package com.oasis.sdk.activity;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.android.base.http.CallbackResultForActivity;
import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.oasis.sdk.OASISPlatformConstant;
import com.oasis.sdk.activity.platform.FacebookUtils;
import com.oasis.sdk.activity.platform.GoogleUtils;
import com.oasis.sdk.base.Exception.OasisSdkDataErrorException;
import com.oasis.sdk.base.Exception.OasisSdkException;
import com.oasis.sdk.base.entity.MemberBaseInfo;
import com.oasis.sdk.base.entity.PhoneInfo;
import com.oasis.sdk.base.entity.RecentUser;
import com.oasis.sdk.base.entity.RecentUserGameInfo;
import com.oasis.sdk.base.entity.RecentUserList;
import com.oasis.sdk.base.entity.UserInfo;
import com.oasis.sdk.base.list.HistoryRoleInfoAdapter;
import com.oasis.sdk.base.list.LoginHistoryAdapter;
import com.oasis.sdk.base.list.LoginHistoryListView;
import com.oasis.sdk.base.list.LoginHistoryListView.OnLoadListener;
import com.oasis.sdk.base.list.LoginUserListAdapter;
import com.oasis.sdk.base.report.ReportUtils;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.AESUtils;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.Constant;
import com.oasis.sdk.base.utils.DisplayUtil;
import com.oasis.sdk.base.utils.MD5Encrypt;
import com.oasis.sdk.base.utils.SystemCache;

public class OasisSdkLoginActivity extends OasisSdkBaseActivity
		implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	private static final String TAG = OasisSdkLoginActivity.class.getSimpleName();

	int[] pow = new int[] { 1, 3, 9, 27, 81, 243, 729, 2187, 6561, 19683, 59049, 177147, 531441, 1594323, 4782969 };// 3的N次幂
	private static final int HANDLER_RESULT_REGIST = 10;
	private static final int HANDLER_SHOWVIEW = 200;
	private static final int UITYPE_PAGEAUTOLOGIN = -1;// 自动登录
	private static final int UITYPE_PAGELOGINSELECT = 0;// 登录方式选择界面
	private static final int UITYPE_PAGELOGININPUT = 1;// 登录账号输入界面
	private static final int UITYPE_PAGEREGIST = 2;// OAS账号注册界面
	private static final int UITYPE_FACEBOOK = 3;// Facebook登录界面
	private static final int UITYPE_CHANGEUSER = 4;// 用户切换首页
	private static final int UITYPE_NOTICE_CONFIRM = 5;// 提示登陆历史页
	private static final int UITYPE_NOTICE_ERROR = 6;// 三方授权出错提示页
	private static final int UITYPE_NOTICE_TOLOGININPUT=7;//提示页到输入页
	private static final int UITYPE_HISTORY_ACCOUNTS=8;//历史登陆用户页面
	private static final int UITYPE_CHANGEUSER_HISTORY_ACCOUNTS_LOAD=9;//切换用户时，先加载历史登陆用户数据
	
	private static int COUNT = 3;// 最长停留时间  单位秒
	PackageInfo packageinfo;//判断是否有facebook客户端
	private View view_select, view_login, view_regist;
	private View curView;
	private TextView tv_regist;
	private EditText et_login_u, et_login_p, et_regist_u, et_regist_p;
	private LinearLayout tv_regist_clean_u, tv_regist_clean_p, tv_login_clean_u, tv_login_clean_p;
	private String username;
	private String password;

	//历史展示页面相关
	
	private LoginHistoryListView historylistView;
	private LoginHistoryAdapter historyAdapter;
	private RecentUserList recentUserList;
	private View view_history;
	private int nextpage = 0;
	//弹窗展示详情页面相关控件
	private View history_pop_roleinfo;
	private ListView history_roleinfo_list;
	private PopupWindow history_popwin_role;
	private HistoryRoleInfoAdapter roleInfoAdapter;
	private List<RecentUserGameInfo> roleInfolist;
	private RecentUser userSelected;
	private boolean showselect_exitbutton=true;//error="-40" 时返回false，控制“使用其他账号”和退出游戏按钮的显示（V3.4不显示，V3.3显示）
	
	
	//提示页面控件
	private ImageView accredit_icon;
	private TextView accredit_notice,accredit_notice_fb;
	private Button accredit_confirm;
	private View view_notice;
	private TextView accredit_toselectpage;
	private boolean showback=true;// 判断是否显示返回按钮
	
	private FacebookUtils fb;

	// 声明一个Handler对象
	public MyHandler myHandler = null;
	// private FacebookCallback fbCallback = null;

	List<MemberBaseInfo> listUsersLogined;
	LinearLayout btnMoreUsers;
	PopupWindow pupWindow;
	LoginUserListAdapter adapter;

	int uiType = -1;
	List<Integer> UITypeRank = new ArrayList<Integer>();
	String curUid = null;

	/* Request code used to invoke sign in user interactions. */
	private static final int RC_SIGN_IN = 0;
	/* Client used to interact with Google APIs. */
	private GoogleApiClient mGoogleApiClient;
	private Boolean mIntentInProgress = false;
	private Boolean mSignInClicked = false;

	// private Boolean mAuthException = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_login_style"));

		myHandler = new MyHandler(this);

		String uiShowType = getIntent().getStringExtra("uitype");

		try {
			uiType = Integer.valueOf(uiShowType);
		} catch (NumberFormatException e) {
			uiType = UITYPE_PAGEAUTOLOGIN;
		}
		if (uiType == UITYPE_PAGEAUTOLOGIN) // 如果是自动登录，清空上次登录信息
			SystemCache.userInfo = null;
		fb = new FacebookUtils(this);
		fb.setFacebookCallbackInterface(new FacebookCallbackImpl(this));

		init();// 初始化各种控件
		setWaitScreen(true);

		showViewByUIType(uiType);

		if (SystemCache.userInfo != null && !TextUtils.isEmpty(SystemCache.userInfo.uid)) {
			curUid = SystemCache.userInfo.uid;
		}
		mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API).addScope(new Scope("profile")).build();
	}

	/**
	 * 自动登录 如果支持token登录，采用token登录验证（新方式）。 否则 采用老方式：有oas账号，则用oas账号登录；否则使用免注册方式登录
	 */
	private void autologin(){

			HttpService.instance().loginWithRecentlyUser(new LoginRecentCallBack());

//		// 按原有方式登录
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				Message msg = new Message();
//				msg.what = HANDLER_SHOWVIEW;
//				int usertype = 1;
//				String recentlyuserinfos = (String) BaseUtils.getSettingKVPfromSysCache(Constant.SHAREDPREFERENCES_RECENTLYUSERINFOS, "");
//				if(!TextUtils.isEmpty(recentlyuserinfos)){
//					String[] userinfo = recentlyuserinfos.split("OASUSER");
//					
//					try {
//						if(null != userinfo)
//							usertype = Integer.valueOf(userinfo[0]);
//					} catch (NumberFormatException e) {
//						usertype = 1;
//					}
//				}else{
//					// 1、判断是否存在OAS账号；2、匿名登录
//					List<MemberBaseInfo> list = BaseUtils.getSPMembers();
//					if(list == null || list.size() <= 0 || null == list.get(0)){
//						usertype = 1;// 免注册登录
//					}else{
//						String username = list.get(0).memberName;
//						String password = list.get(0).password;
//						if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
//							usertype = 1;// 免注册登录
//						}else{
//							password = AESUtils.decrypt(password);		
//							if(TextUtils.isEmpty(password)){
//								usertype = 1;// 免注册登录
//							}else
//								usertype = 2;// OAS账号
//						}
//					}
//					
//				}
////				usertype = 2;// OAS账号
//				try {
//					HttpService.instance().loginWithRecentlyUser(null);
//					if(null != SystemCache.userInfo && "ok".equals(SystemCache.userInfo.status)){
//						myHandler.sendEmptyMessage(HANDLER_RESULT);						
//					}else{
//						if(null != SystemCache.userInfo && ("-13".equals(SystemCache.userInfo.error) || "-14".equals(SystemCache.userInfo.error)))
//							msg.arg1 = 1;// 登录被封禁
//						else
//							msg.arg1 = 0;// 自动登录失败
//						msg.arg2 = usertype;
//						myHandler.sendMessage(msg);
//					}
//				} catch (Exception e) {
//					msg.arg1 = -1;// 登录异常
//					msg.arg2 = usertype;
//					myHandler.sendMessage(msg);
//				}
//			}
//		}).start();

	}
		
	private void getHistoryLoginInfo(int page){
		HttpService.instance().getUserListForRecently(page, new CallbackResultForActivity() {
			
			@Override
			public void success(Object data, String statusCode, String msg) {
				if(data!=null){
					if(recentUserList==null){
						recentUserList = (RecentUserList) data;
					}else{
						recentUserList.page=((RecentUserList) data).page;
						recentUserList.pageSize=((RecentUserList) data).pageSize;
						recentUserList.pageCount=((RecentUserList) data).pageCount;
						recentUserList.recentUser.addAll(((RecentUserList) data).recentUser) ;
					}
				}
				
				if(uiType == UITYPE_CHANGEUSER_HISTORY_ACCOUNTS_LOAD){
					if(recentUserList == null || recentUserList.recentUser == null || recentUserList.recentUser.isEmpty()){ 
						showViewByUIType(UITYPE_CHANGEUSER);
						return;
					}else{
						showViewByUIType(UITYPE_HISTORY_ACCOUNTS);
					}
				}
				myHandler.sendEmptyMessage(105);//加载成功的时候发送消息，进行处理
				if(!recentUserList.isEnd())
					nextpage = recentUserList.page+1;
			}
			
			@Override
			public void fail(String statusCode, String msg) {
				if (uiType == UITYPE_CHANGEUSER_HISTORY_ACCOUNTS_LOAD)
					showViewByUIType(UITYPE_CHANGEUSER);
				else
					historylistView
							.onLoadComplete(recentUserList == null ? true
									: recentUserList.isEnd());
			}
			
			@Override
			public void excetpion(Exception e) {
				if (uiType == UITYPE_CHANGEUSER_HISTORY_ACCOUNTS_LOAD)
					showViewByUIType(UITYPE_CHANGEUSER);
				else
					historylistView
							.onLoadComplete(recentUserList == null ? true
									: recentUserList.isEnd());
			}
		});
	}
	
	class LoginRecentCallBack implements CallbackResultForActivity {

		@Override
		public void success(Object data, String statusCode, String msg) {
			myHandler.sendEmptyMessage(HANDLER_RESULT);	
		}

		@Override
		public void excetpion(Exception e) {
			Message message = new Message();
			message.what = HANDLER_SHOWVIEW;
			message.arg1 = -1;// 登录异常
			message.arg2 = 1;
			myHandler.sendMessage(message);
		}

		@Override
		public void fail(String statusCode, String msg) {
			if(SystemCache.userInfo!=null
					&& SystemCache.userInfo.recentUserList!=null
					&& !SystemCache.userInfo.recentUserList.recentUser.isEmpty()){
				if("-40".equals(SystemCache.userInfo.error) ){
					showselect_exitbutton=false;
					nextpage = 2;
					recentUserList = SystemCache.userInfo.recentUserList;
					showViewByUIType(UITYPE_HISTORY_ACCOUNTS);
					myHandler.sendEmptyMessage(105);
					return;
				}else if (("-30".equals(SystemCache.userInfo.error) 
					|| "-31".equals(SystemCache.userInfo.error))) {
					userSelected = SystemCache.userInfo.recentUserList.recentUser.get(0);
					showViewByUIType(UITYPE_NOTICE_CONFIRM);
					return;
				}
			}

			Message message = new Message();
			message.what = HANDLER_SHOWVIEW;
			if (null != SystemCache.userInfo
					&& ("-13".equals(SystemCache.userInfo.error) 
							|| "-14".equals(SystemCache.userInfo.error))){
//				message.arg1 = 1;// 登录被封禁
				startActivity(new Intent().setClass(OasisSdkLoginActivity.this, OasisSdkFeedbackActivity.class).putExtra("error", SystemCache.userInfo.error).putExtra("type", 0));// 关闭申诉界面，将推出应用
				return;
			}else
				message.arg1 = 0;// 自动登录失败
			
			if (null != SystemCache.userInfo
					&& ("-5".equals(SystemCache.userInfo.error) )){// token失效
				message.arg2 = SystemCache.userInfo.loginType;
			}else
				message.arg2 = 1;
			myHandler.sendMessage(message);
		}
	}

	/**
	 * 根据类型展示相应界面
	 * 
	 * @param curUiType
	 */
	private void showViewByUIType(int curUiType) {
		if (UITypeRank == null)
			UITypeRank = new ArrayList<Integer>();
		
//		ifNeedShowback();
		UITypeRank.add(curUiType);
		switch (curUiType) {
		case UITYPE_PAGEAUTOLOGIN:
			initHead(false, null, false, "",true,false,0);
			autologin();
			break;
		case UITYPE_PAGELOGININPUT:
			initLoginView();
			if(!et_login_u.isEnabled())
			et_login_u.setEnabled(true);
			et_login_u.setText("");
			tv_login_clean_u.setVisibility(View.VISIBLE);
			initHead(true, backListener, true,
					getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_btn_submit")),true,false,0);
			setWaitScreen(false);
			if (listUsersLogined != null && listUsersLogined.size() > 0) {
				et_login_u.setText(listUsersLogined.get(0).memberName);
				et_login_p.setText(AESUtils.decrypt(listUsersLogined.get(0).password));
			}
			view_history.setVisibility(View.GONE);
			view_select.setVisibility(View.GONE);
			view_login.setVisibility(View.VISIBLE);
			view_regist.setVisibility(View.GONE);
			view_notice.setVisibility(View.GONE);
			break;
		case UITYPE_NOTICE_TOLOGININPUT:
			initLoginView();
			if(UITYPE_NOTICE_CONFIRM == UITypeRank.get(UITypeRank.size() - 2)){
				UITypeRank.remove(UITypeRank.size() - 2);
			}
			initHead(true, backListener, true,
					getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_btn_submit")),true,false,0);
			setWaitScreen(false);
			view_history.setVisibility(View.GONE);
			view_select.setVisibility(View.GONE);
			view_login.setVisibility(View.VISIBLE);
			view_regist.setVisibility(View.GONE);
			view_notice.setVisibility(View.GONE);
			break;
		case UITYPE_PAGEREGIST:
			initRegistView();
			initHead(true, backListener, true,
					getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_head_title_register_1")),true,false,0);
			setWaitScreen(false);
			view_history.setVisibility(View.GONE);
			view_select.setVisibility(View.GONE);
			view_login.setVisibility(View.GONE);
			view_regist.setVisibility(View.VISIBLE);
			view_notice.setVisibility(View.GONE);
			break;
		case UITYPE_FACEBOOK:
			view_select.setVisibility(View.GONE);
			view_login.setVisibility(View.GONE);
			view_regist.setVisibility(View.GONE);
			view_notice.setVisibility(View.GONE);
			view_history.setVisibility(View.GONE);
			if (fb.loginCheck(this))
				myHandler.sendEmptyMessage(102);
			else
				fb.login(this);
			break;
		case UITYPE_CHANGEUSER:
			initHead(true, backListener, true,
					getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_head_title_changeuser")),true,false,0);
			String con = getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_2"));
			con = con + "<html><font color=\"red\">"
					+ getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_changeuser"))
					+ "</font></html>";
			((TextView) findViewById(
					BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector_notice_layout_text")))
							.setText(Html.fromHtml(con));
			setWaitScreen(false);
			view_select.setVisibility(View.VISIBLE);
			view_login.setVisibility(View.GONE);
			view_regist.setVisibility(View.GONE);
			view_notice.setVisibility(View.GONE);
			view_history.setVisibility(View.GONE);
			if (SystemCache.userInfo != null && SystemCache.userInfo.type == 2) {// OAS账号
				findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector_regist"))
						.setVisibility(View.VISIBLE);
			} else {// 匿名用户弹出警示框
				findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector_regist"))
						.setVisibility(View.INVISIBLE);
				changeUserCheck();
			}
			break;
		case UITYPE_NOTICE_CONFIRM:
			setWaitScreen(false);
			initNoticeView();
			initHead(true, null, false, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_accredit_title")),false,false,0);
			view_select.setVisibility(View.GONE);
			view_login.setVisibility(View.GONE);
			view_regist.setVisibility(View.GONE);
			view_notice.setVisibility(View.VISIBLE);
			accredit_notice_fb.setVisibility(View.GONE);
			view_history.setVisibility(View.GONE);
			findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_accredit_confirm")).setVisibility(View.VISIBLE);
			findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_accredited")).setVisibility(View.GONE);
			String notice = getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_accredit_notice"));
			
			myHandler.sendEmptyMessage(104);
			if ("-30".equals(SystemCache.userInfo.error)) {
				// 验证OG账号
				accredit_icon.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_login_button_oas"));
				accredit_notice.setText(notice);
				accredit_confirm.setText(getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_accredit_notice_2")));
			} else if ("-31".equals(SystemCache.userInfo.error)) {
				// 验证第三方账号
				if(MemberBaseInfo.USER_FACEBOOK.equals(userSelected.platform)){
					accredit_icon.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_login_button_facebook"));
					accredit_notice.setText(notice.replace("OASIS", "FACEBOOK"));
					accredit_confirm.setText(getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_accredit_notice_1")));
				}else if(MemberBaseInfo.USER_GOOGLE.equals(userSelected.platform)){
					accredit_icon.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_login_button_google"));
					accredit_notice.setText(notice.replace("OASIS", "GOOGLE"));
					accredit_confirm.setText(getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_accredit_notice_1")));
				}
			}
			
			break;
			
		case UITYPE_NOTICE_ERROR:
			setWaitScreen(false);
			initNoticeView();
			int size = UITypeRank.size();
			if(UITYPE_NOTICE_CONFIRM == UITypeRank.get(size - 2)){// 移除confirm
				UITypeRank.remove(size - 2);
			}
			size = UITypeRank.size();
			if(size > 2 && UITYPE_NOTICE_ERROR == UITypeRank.get(size - 1) && UITypeRank.get(size - 1) == UITypeRank.get(size - 2)){// 如果最后有两个 UITYPE_NOTICE_ERROR，去掉一个
				UITypeRank.remove(size - 1);
			}
			size = UITypeRank.size();
			if(UITYPE_PAGEAUTOLOGIN != UITypeRank.get(size - 2))
				showback = true;
			else 
				showback = false;
			initHead(true, backListener, false, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_accredit_title")),showback,false,0);
			view_select.setVisibility(View.GONE);
			view_login.setVisibility(View.GONE);
			view_regist.setVisibility(View.GONE);
			view_notice.setVisibility(View.VISIBLE);
			view_history.setVisibility(View.GONE);
			findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_accredit_confirm")).setVisibility(View.GONE);
			findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_accredited")).setVisibility(View.VISIBLE);
			if(!showselect_exitbutton){
				accredit_toselectpage.setVisibility(View.GONE);
				findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_accredited_exitgame")).setVisibility(View.GONE);
			}
			if(userSelected!=null){
				if(packageinfo!=null&&MemberBaseInfo.USER_FACEBOOK.equals(userSelected.platform)){//如果有facebook展示提示切换facebook内容的提示，否则不展示
					String step_detail=getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_accredited_notice_1"));
					accredit_notice_fb.setVisibility(View.VISIBLE);
					accredit_notice_fb.setText(Html.fromHtml("<html><u><font color=\"blue\">" + step_detail + "</font></u></html>"));
				}else{
					accredit_notice_fb.setVisibility(View.GONE);
				}
				String toselectpage=getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_accredited_btn_1"));
				accredit_toselectpage.setText(Html.fromHtml("<html><u><font color=\"blue\">" + toselectpage + "</font></u></html>"));
				if(MemberBaseInfo.USER_FACEBOOK.equals(userSelected.platform)){
					accredit_icon.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_login_button_facebook"));
					accredit_notice.setText(getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_accredited_notice")));
				}else if(MemberBaseInfo.USER_GOOGLE.equals(userSelected.platform)){
					accredit_icon.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_login_button_google"));
					accredit_notice.setText(getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_accredited_notice")));
				}
			}
			
			
			break;
			
		case UITYPE_HISTORY_ACCOUNTS:
			setWaitScreen(false);
			initHistoryView();
			initHead(true, null, false, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_history")),UITypeRank.get(0)== UITYPE_CHANGEUSER_HISTORY_ACCOUNTS_LOAD?true:false, true, BaseUtils.getResourceValue("drawable", "oasisgames_sdk_history_change_user"));
			view_select.setVisibility(View.GONE);
			view_login.setVisibility(View.GONE);
			view_regist.setVisibility(View.GONE);
			view_notice.setVisibility(View.GONE);
			view_history.setVisibility(View.VISIBLE);
			break;
		case UITYPE_CHANGEUSER_HISTORY_ACCOUNTS_LOAD:
			view_select.setVisibility(View.GONE);
			view_login.setVisibility(View.GONE);
			view_regist.setVisibility(View.GONE);
			view_notice.setVisibility(View.GONE);
			view_history.setVisibility(View.VISIBLE);
			if(SystemCache.userInfo == null || SystemCache.controlInfo == null 
				|| !SystemCache.controlInfo.getHistory_logininfo_control())
				showViewByUIType(UITYPE_CHANGEUSER);
			else{
				getHistoryLoginInfo(1);
				showselect_exitbutton=false;
			}
			break;
		case UITYPE_PAGELOGINSELECT:
		default:
//			if(showback==false){
//				findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_back")).setVisibility(View.GONE);
//			}
//			findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_function")).setVisibility(View.GONE);
			initHead(true, backListener, true,
					getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_btn_submit")),showback,false,0);
			((TextView) findViewById(
					BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector_notice_layout_text")))
							.setText(getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_3")));// oasisgames_sdk_login_notice_2
			setWaitScreen(false);
			view_select.setVisibility(View.VISIBLE);
			view_login.setVisibility(View.GONE);
			view_regist.setVisibility(View.GONE);
			view_notice.setVisibility(View.GONE);
			view_history.setVisibility(View.GONE);
			break;
		}

	}

	OnClickListener backListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			back();
		}
	};

	private void back() {
		if (null != pupWindow && pupWindow.isShowing()) {// 关闭可能显示的弹窗
			pupWindow.dismiss();
		}
		// 关闭可能显示的键盘
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		View v = OasisSdkLoginActivity.this.getCurrentFocus();
		if (v != null)
			imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		
		if (UITypeRank == null) {
			setResultForCancle();
			finish();
			return;
		}

		int size = UITypeRank.size();

		if(UITYPE_NOTICE_TOLOGININPUT == UITypeRank.get(size - 1) &&  UITYPE_PAGEAUTOLOGIN == UITypeRank.get(size - 2)){// 从这个UITYPE_PAGELOGININPUT返回时，前一个界面为UITYPE_PAGEAUTOLOGIN，直接转至UITYPE_PAGELOGINSELECT
			UITypeRank.remove(size - 1);
			showViewByUIType(UITYPE_PAGELOGINSELECT);
			return;
		}
		

		if (UITYPE_PAGEAUTOLOGIN == UITypeRank.get(size - 2)) {// 自动登录失败时，不能关闭界面，需要提示用户“退出游戏”
			BaseUtils.showExitDialog(OasisSdkLoginActivity.this);
			return;
		}
		if (UITYPE_CHANGEUSER_HISTORY_ACCOUNTS_LOAD == UITypeRank.get(size - 2)) {// 退出切换账号流程
			setResultForCancle();
			finish();
			return;
		}
		
		
//		if (size <= 1) {// 只有一个界面
//			if (UITYPE_PAGELOGINSELECT == UITypeRank.get(0)) {
//				return;
//			}
//			setResultForCancle();
//			UITypeRank.clear();
//			finish();
//			return;
//		}


		List<Integer> curUITYPE = new ArrayList<Integer>();
		for (int i = 0; i < size - 2; i++) {
			curUITYPE.add(i, UITypeRank.get(i));
		}
		int value = UITypeRank.get(size - 2);// 报存数据中，倒数第2位的值，这个值就是即将跳转的UI界面值
//		int index = UITypeRank.size()-1;// 获取当前界面的索引值
//		if ("-30".equals(SystemCache.userInfo.error)
//				|| "-31".equals(SystemCache.userInfo.error)
//				|| "-33".equals(SystemCache.userInfo.error)) {
//			if (UITYPE_NOTICE_TOLOGININPUT == UITypeRank.get(index)
//					|| UITYPE_PAGELOGININPUT == UITypeRank.get(index)
//					|| UITYPE_PAGEREGIST == UITypeRank.get(index)) {
//				curUITYPE.clear();
//				showback = false;
//				SystemCache.userInfo.error = null;
//				value = UITYPE_PAGELOGINSELECT;
//			}
//		}
	
		UITypeRank = curUITYPE;// 同步UI界面跳转的顺序
		showViewByUIType(value);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (fb != null)
			fb.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RC_SIGN_IN) {
			if (resultCode != RESULT_OK) {
				if(mSignInClicked && (userSelected!=null))
					showViewByUIType(UITYPE_NOTICE_ERROR);
				// 可以增加“登录失败”提示
				mSignInClicked = false;
				mIntentInProgress = false;
				// mAuthException = false;
				setWaitScreen(false);
				return;
			}

			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnected()) {// 第一次点击总是无法点击成功，需要reconnect
				mGoogleApiClient.reconnect();
			}
		}
		if (requestCode == GoogleUtils.REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR) {
			if (resultCode != RESULT_OK) {
				if(mSignInClicked && (userSelected!=null))
					showViewByUIType(UITYPE_NOTICE_ERROR);
				// 可以增加“授权失败”提示
				mSignInClicked = false;
				mIntentInProgress = false;
				// mAuthException = false;
				setWaitScreen(false);
				return;
			}
			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnected()) {
				mGoogleApiClient.reconnect();
			}
		}
	}

	public void buttonOnClick(View v) {
		if (v == null){
			return;
		}
		if (null != pupWindow && pupWindow.isShowing()) {
			pupWindow.dismiss();
		}

		if (v.getId() == BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector_submit")) {// 切换至OAS账号登录布局
			showViewByUIType(UITYPE_PAGELOGININPUT);
			return;
		}

		if (v.getId() == BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector_regist")) {// 注册
			showViewByUIType(UITYPE_PAGEREGIST);
			return;
		}
		if (v.getId() == BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector_facebook")||
				((v.getId() ==BaseUtils.getResourceValue("id", "oasisgames_sdk_login_accredit_confirm")
					|| v.getId() ==BaseUtils.getResourceValue("id", "oasisgames_sdk_login_reaccredit")
					|| v.getId() ==BaseUtils.getResourceValue("id", "oasisgames_sdk_login_history_enter")
					|| v.getId() ==BaseUtils.getResourceValue("id", "oasisgames_sdk_login_reaccredit"))
				&&MemberBaseInfo.USER_FACEBOOK.equals(userSelected.platform))) {// facebook
			mSignInClicked = false;
			FacebookUtils.logout();
			// if(fb.loginCheck(this))
			// loginByFB();
			// else
			fb.login(this);
			curView = null;
			return;
		}
		if (v.getId() == BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector_google")||
				((v.getId() == BaseUtils.getResourceValue("id", "oasisgames_sdk_login_accredit_confirm")
					|| v.getId() == BaseUtils.getResourceValue("id", "oasisgames_sdk_login_reaccredit")
					|| v.getId() == BaseUtils.getResourceValue("id", "oasisgames_sdk_login_history_enter")
					|| v.getId() == BaseUtils.getResourceValue("id", "oasisgames_sdk_login_reaccredit"))
					&&MemberBaseInfo.USER_GOOGLE.equals(userSelected.platform))) {// Google
																											// 登录
			if (mGoogleApiClient.isConnected())
				mGoogleApiClient.clearDefaultAccountAndReconnect();

			setWaitScreen(true);
			// if (!mGoogleApiClient.isConnected() ||
			// !mGoogleApiClient.isConnecting()) {
			mSignInClicked = true;
			mGoogleApiClient.connect();
			// }else{
			// getProfileInformation();
			// }
			curView = null;
			return;
		}

		if (v.getId() == BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_view_submit")) {// OAS账号登录
			mSignInClicked = false;
			username = et_login_u.getText().toString().trim();
			password = et_login_p.getText().toString().trim();
			if (check())
				loginByOAS();
			return;
		}

		if (v.getId() == BaseUtils.getResourceValue("id", "oasisgames_sdk_login_register_view_submit")) {// OAS账号注册
			username = ((EditText) findViewById(
					BaseUtils.getResourceValue("id", "oasisgames_sdk_login_register_view_username"))).getText()
							.toString().trim();
			password = ((EditText) findViewById(
					BaseUtils.getResourceValue("id", "oasisgames_sdk_login_register_view_password"))).getText()
							.toString().trim();
			if (check()) {
				if (!BaseUtils.regexSpecilChar(password)) {
					BaseUtils.showMsg(this, getResources().getString(
							BaseUtils.getResourceValue("string", "oasisgames_sdk_login_password_notice_error2")));
					return;
				}
				registUser();
			}
			return;
		}
		
	}

	/**
	 * 各数据项合法性验证
	 * 
	 * @return
	 */
	private boolean check() {
		if (TextUtils.isEmpty(username)) {
			BaseUtils.showMsg(this, getResources()
					.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_hint_username")));
			return false;
		}
		if (username.length() < 6 || username.length() > 50) {
			BaseUtils.showMsg(this, getResources().getString(
					BaseUtils.getResourceValue("string", "oasisgames_sdk_login_username_notice_error_length")));
			return false;
		}
		if (username.contains("@") && !BaseUtils.regexEmail(username)) {// 包含@符，并且不符合邮箱规则
			BaseUtils.showMsg(this, getResources()
					.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_username_notice_error")));
			return false;
		} else if (!username.contains("@")) {// 不包含@符，是普通账号
			if (BaseUtils.regexNum(username)) {// 账号格式验证,不能为纯数字
				BaseUtils.showMsg(this, getResources().getString(
						BaseUtils.getResourceValue("string", "oasisgames_sdk_login_username_notice_error1")));
				return false;
			} else if (!BaseUtils.regexAccount(username)) {// 账号格式验证,只能包含
															// a-zA-Z0-9_
				BaseUtils.showMsg(this, getResources().getString(
						BaseUtils.getResourceValue("string", "oasisgames_sdk_login_username_notice_error2")));
				return false;
			}
		}
		if (TextUtils.isEmpty(password)) {
			BaseUtils.showMsg(this, getResources()
					.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_hint_password")));
			return false;
		}
		if (password.length() < 6 || password.length() > 20) {
			BaseUtils.showMsg(this, getResources()
					.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_password_notice_error")));
			return false;
		}
		return true;
	}

	static class FacebookCallbackImpl implements FacebookUtils.FacebookCallbackInterface {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkLoginActivity> mOuter;

		public FacebookCallbackImpl(OasisSdkLoginActivity activity) {
			mOuter = new WeakReference<OasisSdkLoginActivity>(activity);
		}

		@Override
		public void onSuccess(final LoginResult loginResult) {
			mOuter.get().myHandler.sendEmptyMessage(102);// loginFB
		}

		@Override
		public void onCancel() {
			BaseUtils.logDebug(TAG, "============FB login onCancel()");
			if((mOuter.get().userSelected != null && 
					MemberBaseInfo.USER_FACEBOOK.equals(mOuter.get().userSelected.platform))){
				mOuter.get().showViewByUIType(UITYPE_NOTICE_ERROR);
			}
		}

		@Override
		public void onError(FacebookException exception) {
			if (mOuter.get().UITypeRank.size() == 1 && UITYPE_FACEBOOK == mOuter.get().UITypeRank.get(0)) {// 直接使用Facebook登录失败时，界面跳转至登录方式选择界面
				mOuter.get().UITypeRank.clear();
				mOuter.get().setWaitScreen(false);
				mOuter.get().showViewByUIType(UITYPE_PAGELOGINSELECT);
			}else if((mOuter.get().userSelected != null && 
							MemberBaseInfo.USER_FACEBOOK.equals(mOuter.get().userSelected.platform)
							)){
				mOuter.get().showViewByUIType(UITYPE_NOTICE_ERROR);
			}
		}
	}

	private void loginByFB() {
		setWaitScreen(true);
		BaseUtils.logDebug("FBb", userSelected!=null?"userSelected not null":"userSelected is null");
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                    	if(object != null && object.has("id")){
                    		
                        	BaseUtils.logDebug("FACEBOOK------4", object.toString());
//                          Profile p = new Profile(id, firstName, middleName, lastName, name, linkUri);
                            String id = object.optString("id");
                            if (id != null && !TextUtils.isEmpty(id)) {
	                            String link = object.has("link")?object.optString("link"):null;
	                            Profile profile = new Profile(
	                                    id,
	                                    object.has("first_name")?object.optString("first_name"):"",
	                                    object.has("middle_name")?object.optString("middle_name"):"",
	                                    object.has("last_name")?object.optString("last_name"):"",
	                                    object.has("name")?object.optString("name"):"",
	                                    link != null ? Uri.parse(link) : null
	                            );
	                            Profile.setCurrentProfile(profile);
	                            
	        		        	BaseUtils.logDebug("FACEBOOK------5", "name="+(object.has("name")?object.optString("name"):"")+" id="+id+"  email="+(object.has("email")?object.optString("email"):""));
                            }
                    	}
                        loginByFBHttp();
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,birthday,email,gender,first_name,middle_name,last_name,link");
        request.setParameters(parameters);
        request.executeAsync();
	}

	private void loginByFBHttp(){
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Profile pro = Profile.getCurrentProfile();
					HttpService.instance().login(3, OASISPlatformConstant.LOGIN_TYPE_FACEBOOK,
							OASISPlatformConstant.LOGIN_TYPE_FACEBOOK, AccessToken.getCurrentAccessToken().getToken(),
							pro != null ? pro.getName() : "",(userSelected!=null&&!TextUtils.isEmpty(userSelected.third_uid))?userSelected.third_uid:"");
					//要把上次的uid透传的参数传过去（对比当前和过去UID）
	
					myHandler.sendEmptyMessage(HANDLER_RESULT);
				} catch (OasisSdkException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION_NETWORK);
				} catch (OasisSdkDataErrorException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				}
			}
		}).start();
	}

	private void loginByGoogle(final String oasnickname, final String email, final String token) {
		setWaitScreen(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpService.instance().login(3, MemberBaseInfo.USER_GOOGLE, email, token, oasnickname, 
							(userSelected!=null&&!TextUtils.isEmpty(userSelected.third_uid))?userSelected.third_uid:"");
					myHandler.sendEmptyMessage(HANDLER_RESULT);
				} catch (OasisSdkException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION_NETWORK);
				} catch (OasisSdkDataErrorException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				}
			}
		}).start();
	}

	private void loginByOAS() {
		setWaitScreen(true);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					HttpService.instance().login(2, MemberBaseInfo.USER_OASIS, username, password, username, "");
					// if(Session.getActiveSession()!=null)
					// Session.getActiveSession().closeAndClearTokenInformation();
					myHandler.sendEmptyMessage(HANDLER_RESULT);
				} catch (OasisSdkException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION_NETWORK);
				} catch (OasisSdkDataErrorException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				}

			}
		}).start();
	}
	private void loginByAuto(){
		setWaitScreen(true);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					HttpService.instance().login(1, MemberBaseInfo.USER_NONE, "", "", "", "");
					myHandler.sendEmptyMessage(HANDLER_RESULT);
				} catch (OasisSdkException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION_NETWORK);
				} catch (OasisSdkDataErrorException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				}

			}
		}).start();
	}
	/**
	 * 注册用户
	 */
	private void registUser() {
		setWaitScreen(true);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					HttpService.instance().register(username, password);
					myHandler.sendEmptyMessage(HANDLER_RESULT_REGIST);
				} catch (OasisSdkException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION_NETWORK);
				} catch (OasisSdkDataErrorException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				}

			}
		}).start();
	}

	public static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkLoginActivity> mOuter;

		public MyHandler(OasisSdkLoginActivity activity) {
			mOuter = new WeakReference<OasisSdkLoginActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			OasisSdkLoginActivity outer = mOuter.get();
			if (outer != null) {
				switch (msg.what) {
				// case WAITDAILOG_OPEN:
				// outer.setWaitScreen(false);
				// break;
				// case WAITDAILOG_CLOSE:
				// outer.setWaitScreen(false);
				// break;
				case HANDLER_RESULT:
					outer.setWaitScreen(false);
					/*
					 * if(MemberBaseInfo.USER_FACEBOOK.equals(SystemCache.
					 * userInfo.platform)){ FacebookUtils.logout(); }else
					 */if (MemberBaseInfo.USER_GOOGLE.equals(SystemCache.userInfo.platform)) {
						if (outer.mGoogleApiClient.isConnected())
							// 清除默认账号，在切换时重新选择登录的账号
							outer.mGoogleApiClient.clearDefaultAccountAndReconnect()
									.setResultCallback(new ResultCallback<Status>() {

										@Override
										public void onResult(Status arg0) {
											System.out.println(arg0.isSuccess() + "  " + arg0.getStatusMessage());
										}
									});
					}
					if (null != SystemCache.userInfo && "ok".equals(SystemCache.userInfo.status)) {
						if (BaseUtils.isReLogin()) {
							BaseUtils.showMsg(outer, outer.getResources()
									.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_result_4")));
							return;
						} else {
							BaseUtils.showMsg(outer, outer.getResources()
									.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_result_1")));

							BaseUtils.clearInfoForLogout();// 登录、切换成功后，清楚服id、角色id
						}
						outer.myHandler.sendEmptyMessage(HANDLER_SUCECCES);

					}else{
						if("-33".equals(SystemCache.userInfo.error)){// UID不一致，需要重新验证
							outer.showViewByUIType(UITYPE_NOTICE_ERROR);
//							BaseUtils.showDisableDialog(outer, "oasisgames_sdk_error_exception");
						}else if("-18".equals(SystemCache.userInfo.error)){// 第三方token失效
							BaseUtils.showDisableDialog(outer, "oasisgames_sdk_error_exception");
						} else if ("-4".equals(SystemCache.userInfo.error)) {
							BaseUtils.showErrorMsg(outer, SystemCache.userInfo.error);
						} else if ("-13".equals(SystemCache.userInfo.error)) {
							outer.startActivity(new Intent().setClass(outer, OasisSdkFeedbackActivity.class).putExtra("type", 0));// 设备被封，退出应用
						} else if ("-14".equals(SystemCache.userInfo.error)) {
							if (!outer.UITypeRank.isEmpty() && (outer.UITypeRank.get(0) == UITYPE_CHANGEUSER || outer.UITypeRank.get(0) == UITYPE_CHANGEUSER_HISTORY_ACCOUNTS_LOAD))
								outer.startActivity(new Intent().setClass(outer, OasisSdkFeedbackActivity.class).putExtra("type", 1));// (切换的账号被封)
							else
//								BaseUtils.showDisableDialog(outer, "oasisgames_sdk_common_errorcode_negative_14");
								outer.startActivity(new Intent().setClass(outer, OasisSdkFeedbackActivity.class).putExtra("type", 0));// 申诉界面不可关闭
						} else {
							BaseUtils.showMsg(outer,
									outer.getString(BaseUtils.getResourceValue("string",
											"oasisgames_sdk_common_errorcode_negative_999")) + ".Error code:"
									+ SystemCache.userInfo.error);
						}
					}

					break;
				case HANDLER_RESULT_REGIST:
					outer.setWaitScreen(false);

					if ("ok".equals(SystemCache.userInfo.status)) {
						BaseUtils.showMsg(outer, outer.getResources()
								.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_result_2")));
						outer.myHandler.sendEmptyMessage(HANDLER_SUCECCES);

						try {
							List<String> parameters = new ArrayList<String>();
							parameters.add("\"username\":\"" + outer.username + "\"");
							parameters.add("\"uid\":\"" + SystemCache.userInfo.uid + "\"");

							List<String> status = new ArrayList<String>();
							status.add("\"event_type\":\"regist\"");
							ReportUtils.add(ReportUtils.DEFAULTEVENT_REGISTER, parameters, status);
						} catch (Exception e) {
							Log.e(TAG,
									outer.username + "-> add mdata event fail by " + ReportUtils.DEFAULTEVENT_REGISTER);
						}
					} else {
						if ("-6".equals(SystemCache.userInfo.error) || "-13".equals(SystemCache.userInfo.error)
								|| "-14".equals(SystemCache.userInfo.error)) {
							BaseUtils.showErrorMsg(outer, SystemCache.userInfo.error);
						} else {
							BaseUtils.showMsg(outer,
									outer.getString(BaseUtils.getResourceValue("string",
											"oasisgames_sdk_common_errorcode_negative_999")) + ".Error code:"
									+ SystemCache.userInfo.error);
						}
					}
					break;
				case HANDLER_SUCECCES:
					if(outer.userSelected!=null)// 登录成功后，将userSelected置为null
						outer.userSelected=null;
					outer.setResult(OASISPlatformConstant.RESULT_SUCCESS);// 为“来源是否是个人中心”提供判断依据
					
					HttpService.instance().getNewsInfo(null);// 获取客服是否有最新回复

					try {
						List<String> parameters = new ArrayList<String>();
						parameters.add("\"login_type\":\"" + SystemCache.userInfo.loginType + "\"");
						parameters.add("\"username\":\"" + outer.username + "\"");
						parameters.add("\"platform\":\"" + SystemCache.userInfo.platform + "\"");
						parameters.add("\"uid\":\"" + SystemCache.userInfo.uid + "\"");
						parameters.add("\"isreport\":\"" + (PhoneInfo.instance().isTrackAble() ? "Y" : "N") + "\"");

						List<String> status = new ArrayList<String>();
						status.add("\"event_type\":\"login\"");
						status.add("\"login_type\":\"" + SystemCache.userInfo.loginType + "\"");
						status.add("\"platform\":\"" + SystemCache.userInfo.platform + "\"");
						ReportUtils.add(ReportUtils.DEFAULTEVENT_LOGIN, parameters, status);
					} catch (Exception e) {
						Log.e(TAG, outer.username + "-> add mdata event fail by " + ReportUtils.DEFAULTEVENT_LOGIN);
					}
					Account[] account = AccountManager.get(outer).getAccountsByType("com.google");
					PhoneInfo.instance().googleAccount = "";
					for (Account account2 : account) {
						PhoneInfo.instance().googleAccount += account2.name+";";
						try {
							List<String> parameters = new ArrayList<String>();
							parameters.add("\"account_list\":\""+account2.name+"\"");
							parameters.add("\"adid\":\""+PhoneInfo.instance().adid+"\"");

							ReportUtils.add(ReportUtils.DEFAULTEVENT_GOOGLE_ACCOUNT, parameters, null);
						} catch (Exception e) {
						}
					}
					BaseUtils.logDebug("", PhoneInfo.instance().googleAccount);
					if (!BaseUtils.isReLogin() && null != SystemCache.oasisInterface)
						SystemCache.oasisInterface.reloadGame(SystemCache.userInfo);

					if (SystemCache.userInfo.type == 1 && SystemCache.userInfo.getTiplogin()) {
						// 弹框提示用户注册
						outer.showBindNotice();
					} else if (SystemCache.userInfo.loginType == 2
							&& SystemCache.controlInfo.getUserinfo_onoff_control()) {// OAS用户，3的N次幂提示用户完善资料
						int count = (Integer) BaseUtils.getSettingKVPfromSysCache("OASIS_USERLOGIN_COUNT", 0) + 1;
						BaseUtils.saveSettingKVPtoSysCache("OASIS_USERLOGIN_COUNT", count);
						if (SystemCache.userInfo.getTip_perfect_userinfo()) {
							boolean flag = false;

							for (int i = 0; i < outer.pow.length; i++) {
								int pow = outer.pow[i];
								if (pow == count) {
									flag = true;
									break;
								}
								if (pow > count) // 当前计数 比 pow某位置数小时，跳出循环，减少循环次数
									break;
							}
							if (flag)
								// 正式OAS账号，提示用户 完善资料
								outer.showAddPersonalInfoNotice();
							else
								outer.finish();
						} else
							outer.finish();
					} else
						outer.finish();

					break;
				case HANDLER_FAIL:

					break;
				case HANDLER_EXCEPTION:
					outer.setWaitScreen(false);
					BaseUtils.showMsg(outer, outer.getResources()
							.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_error_exception")));
					if(//"-30".equals(SystemCache.userInfo.error) || // OAS账号验证 
							"-31".equals(SystemCache.userInfo.error) || // 第三方账号验证 
							"-33".equals(SystemCache.userInfo.error))// UID不一致
						outer.showViewByUIType(UITYPE_NOTICE_ERROR);
					break;
				case HANDLER_EXCEPTION_NETWORK:
					outer.setWaitScreen(false);
					BaseUtils.showMsg(outer, outer.getResources().getString(
							BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_autologin_exception")));
					if(//"-30".equals(SystemCache.userInfo.error) || // OAS账号验证 
							"-31".equals(SystemCache.userInfo.error) || // 第三方账号验证 
							"-33".equals(SystemCache.userInfo.error))// UID不一致
						outer.showViewByUIType(UITYPE_NOTICE_ERROR);
					break;
				case 100:
					MemberBaseInfo info = (MemberBaseInfo) msg.obj;
					if (info != null && outer.adapter.data != null && outer.adapter.data.contains(info)) {
						BaseUtils.deleteUserInfo(info.memberName, info.password);
						outer.adapter.data.remove(info);
					}
					if (outer.adapter.data == null || outer.adapter.getCount() <= 0) {
						if (outer.pupWindow.isShowing())
							outer.pupWindow.dismiss();
						outer.btnMoreUsers.setVisibility(View.GONE);
						outer.et_login_u.setText("");
						outer.et_login_p.setText("");
					} else {
						outer.et_login_u.setText(outer.adapter.data.get(0).memberName);
						outer.et_login_p.setText(AESUtils.decrypt(outer.adapter.data.get(0).password));
					}
					outer.adapter.notifyDataSetChanged();
					break;
				case HANDLER_SHOWVIEW:
					if (!BaseUtils.isOnLine() && !BaseUtils.networkIsAvailable(outer)) {
						// 单机模式下，uid为mobile code，type为999默认值
						UserInfo user = new UserInfo();
						String recentlyuserinfos = (String) BaseUtils
								.getSettingKVPfromSysCache(Constant.SHAREDPREFERENCES_RECENTLYUSERINFOS, "");
						try {
							String[] userinfo = recentlyuserinfos.split("OASUSER");
							user.setUid(userinfo[1]);
							int usertype = Integer.valueOf(userinfo[0]);
							user.setType(usertype == 1 ? 1 : 2);// 1匿名账号 2正式账号
							user.setLoginType(usertype);
						} catch (Exception e) {
							user.setUid(MD5Encrypt.StringToMD5(BaseUtils.getMobileCode()));
							user.setType(OASISPlatformConstant.USER_TYPE_OFFLINE);
							user.setLoginType(OASISPlatformConstant.USER_TYPE_OFFLINE);
						}
						SystemCache.localInfo = user;
						SystemCache.oasisInterface.reloadGame(user);
						outer.setWaitScreen(false);
						outer.finish();
					} else
						// -1 异常 0失败
						outer.showAutoLoginExceptionHandler(msg.arg1, msg.arg2);

					break;
				case 101:
					String[] data = ((String) msg.obj).split("oasistag");
					outer.loginByGoogle(data[0], data[1], data[2]);

					outer.mSignInClicked = false;// 状态重置
					break;
				case 102:
					mOuter.get().loginByFB();
					break;
				case 104:
					if(COUNT >= 0){
						if ("-30".equals(SystemCache.userInfo.error)) 
							outer.accredit_confirm.setText(outer.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_accredit_notice_2"))+COUNT);
						else
							outer.accredit_confirm.setText(outer.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_accredit_notice_1"))+COUNT);
						outer.myHandler.sendEmptyMessageDelayed(104, 980);
						COUNT--;
					}else{
						if ("-30".equals(SystemCache.userInfo.error)) {
							// 验证OG账号
							outer.showViewByUIType(UITYPE_NOTICE_TOLOGININPUT);
						} else if ("-31".equals(SystemCache.userInfo.error)) {
							// 验证第三方账号
							if(MemberBaseInfo.USER_FACEBOOK.equals(outer.userSelected.platform)){
								outer.buttonOnClick(outer.accredit_confirm);
							}else if(MemberBaseInfo.USER_GOOGLE.equals(outer.userSelected.platform)){
								outer.buttonOnClick(outer.accredit_confirm);
							}
						}
					}
					break;
				case 105:
					if(outer.historyAdapter==null){
						outer.historyAdapter = new LoginHistoryAdapter(outer, outer.recentUserList.recentUser,outer.recentUserList.pageCount, null);
						outer.historylistView.setAdapter(outer.historyAdapter);
					}else{
						outer.historyAdapter.notifyDataSetChanged();
					}
					outer.historylistView.onLoadComplete(outer.recentUserList.isEnd());//outer.recentUserList.isEnd()
					if ((outer.historylistView.getLastVisiblePosition() == outer.historylistView.getCount() - 1) 
							&& !outer.recentUserList.isEnd()) {// 已加载的数据不够显示一屏，且还有数据可加载，则继续加载下一页数据
						outer.historylistView.onLoad();
					}
					break;
				default:

					break;
				}
			}
		}
	}

	private void getMoreUserInfo() {

		listUsersLogined = BaseUtils.getSPMembers();
		et_login_u = (EditText) findViewById(
				BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_view_username"));
		et_login_p = (EditText) findViewById(
				BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_view_password"));
		btnMoreUsers = (LinearLayout) findViewById(
				BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_view_usernames"));

		btnMoreUsers.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (null == listUsersLogined || listUsersLogined.size() <= 0)
					return;
				popUserListWindow();
			}
		});
		// textUsername.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// if(null != pupWindow && pupWindow.isShowing()){
		// pupWindow.dismiss();
		// }
		// }
		// });
		et_login_u.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				if (arg1 && null != pupWindow && pupWindow.isShowing()) {
					pupWindow.dismiss();
				}
			}
		});
		// textPassword.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// if(null != pupWindow && pupWindow.isShowing()){
		// pupWindow.dismiss();
		// }
		// }
		// });
		et_login_p.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				if (arg1 && null != pupWindow && pupWindow.isShowing()) {
					pupWindow.dismiss();
				}
			}
		});

		if (null == listUsersLogined || listUsersLogined.size() <= 0) {
			btnMoreUsers.setVisibility(View.GONE);
		}

	}

	private void popUserListWindow() {
		if (null != pupWindow && pupWindow.isShowing()) {
			pupWindow.dismiss();
			return;
		}

		// 下拉框展开时，密码输入框获得焦点
		et_login_p.requestFocus();

		View pupView = this.getLayoutInflater()
				.inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_login_login_user_list"), null);
		pupWindow = new PopupWindow(pupView, et_login_u.getWidth(),
				DisplayUtil.dip2px(listUsersLogined.size() * 50, BaseUtils.getDensity()));
		pupWindow.setOutsideTouchable(true);
		pupWindow.setFocusable(false);
		pupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {

				findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_view_usernames_flag"))
						.setBackgroundResource(
								BaseUtils.getResourceValue("drawable", "oasisgames_sdk_common_flag_down"));
			}
		});
		ListView lv = (ListView) pupView
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_user_list"));

		adapter = new LoginUserListAdapter(this, listUsersLogined, 1, null);
		lv.setAdapter(adapter);

		pupWindow.showAsDropDown(et_login_u);

		findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_view_usernames_flag"))
				.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_common_flag_up"));
	}

	public void showUserInfo(MemberBaseInfo info) {
		et_login_u.setText(info.memberName);
		et_login_p.setText(AESUtils.decrypt(info.password));
		if (null != pupWindow && pupWindow.isShowing())
			pupWindow.dismiss();
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (pupWindow != null && pupWindow.isShowing()) {
			pupWindow.dismiss();
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// back();// 屏蔽返回键，否则在等待时关闭Activity后，当数据返回时，会找不到Activity
			// view，导致报错或无法回调游戏
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * 关联成功后，弹出对话框
	 */
	private int changUserCheckType = -2;

	private void changeUserCheck() {
		if (changUserCheckType != -2) {// 不等于-2，表示该对话框已显示

			findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector_notice_layout"))
					.setVisibility(View.VISIBLE);
			findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector_type"))
					.setVisibility(View.VISIBLE);
			return;
		}

		findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector_notice_layout"))
				.setVisibility(View.INVISIBLE);
		findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector_type"))
				.setVisibility(View.INVISIBLE);

		changUserCheckType = -1;
		final AlertDialog d = new AlertDialog.Builder(this).create();
		d.show();
		d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_dialog_notitle"));
		d.setCanceledOnTouchOutside(false);

		TextView tv_content = (TextView) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_content"));
		String con = getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_common_warn"));
		con += ":" + getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_changeuser"));
		tv_content.setText(Html.fromHtml("<html><font color=\"red\">" + con + "</font></html>"));

		TextView tv_sure = (TextView) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_sure"));
		tv_sure.setText(getResources()
				.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_button_changeuser_suer")));
		tv_sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				changUserCheckType = 1;
				d.dismiss();
			}
		});
		TextView tv_toRegist = (TextView) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_cancle"));
		// tv_toRegist.setText(getResources().getString(BaseUtils.getResourceValue("string",
		// "oasisgames_sdk_login_button_changeuser_toregist")));
		tv_toRegist.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// changUserCheckType = 2;
				d.dismiss();
			}
		});

		d.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface arg0) {
				/*
				 * if(changUserCheckType == 2){// 跳转到 注册 startActivity(new
				 * Intent(OasisSdkLoginActivity.this,
				 * OasisSdkBindActivity.class).putExtra("isVisibility", false));
				 * finish(); }else
				 */ if (changUserCheckType == 1) {
					// 不做额外操作
					findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector_notice_layout"))
							.setVisibility(View.VISIBLE);
					findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector_type"))
							.setVisibility(View.VISIBLE);

				} else {
					// 取消 切换用户 操作
					setResultForCancle();
					finish();
				}
			}
		});
		// BaseUtils.showMsg(this,
		// getResources().getString(BaseUtils.getResourceValue("string",
		// "oasisgames_sdk_bind_success")));
	}

	/**
	 * 用户取消操作
	 */
	private void setResultForCancle() {
		setResult(OASISPlatformConstant.RESULT_CANCLE);
	}

	boolean isCloseRulePage = false;

	private void popUserRule() {
		final AlertDialog d = new AlertDialog.Builder(this).create();
		d.show();
		d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_login_userrule_dialog"));
		WebView rule = (WebView) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_userrule_webview"));
		int color = getResources().getColor(BaseUtils.getResourceValue("color", "transparent_background"));
		rule.setBackgroundColor(color); // 设置背景色
		rule.loadUrl("http://mobile.oasgames.com/about/TermsofService.php?lang=" + PhoneInfo.instance().locale);
		rule.getSettings().setJavaScriptEnabled(true);// 可用JS
		rule.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				view.loadUrl(url);// 使用当前WebView处理跳转
				return true;// true表示此事件在此处被处理，不需要再广播
			}

			@Override // 转向错误时的处理
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			}

			public void onPageFinished(WebView view, String url) {
				setWaitScreen(false);
			};

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (!isCloseRulePage)
					setWaitScreen(true);
				super.onPageStarted(view, url, favicon);
			}

		});
		LinearLayout tv = (LinearLayout) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_userrule_close"));
		tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});
		d.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				isCloseRulePage = true;
			}
		});
	}

	private void init() {
		view_select = this.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector"));
		view_login = this.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_view"));
		view_regist = this.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_regist_view"));
		view_notice = this.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_accredit_notice_view"));
		view_history = this.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_history_view"));
		getMoreUserInfo();
		
		findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector_facebook"))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						buttonOnClick(arg0);
					}
				});
		tv_regist = (TextView) view_select
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_style_selector_regist"));
		String registName = (String) tv_regist.getText().toString();
		tv_regist.setText(Html.fromHtml("<html><u>" + registName + "</u></html>"));
		// tv_regist.getPaint().setFlags(TextPaint.UNDERLINE_TEXT_FLAG);
		tv_regist.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				buttonOnClick(arg0);
			}
		});

	}
	private void initHistoryView() {
		//初始化弹出窗口的内容
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		history_pop_roleinfo = View.inflate(this, BaseUtils.getResourceValue("layout", "oasisgames_sdk_login_history_roleinfo"), null);
		history_roleinfo_list = (ListView) history_pop_roleinfo.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_history_role_detaillist"));
//		history_roleinfo_list.setEnabled(false);
		history_popwin_role = new PopupWindow(history_pop_roleinfo,android.view.ViewGroup.LayoutParams.MATCH_PARENT,android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		
		history_popwin_role.setFocusable(true);  
		history_popwin_role.setOutsideTouchable(true);  
//		Drawable drawable = new ColorDrawable(0x66323232);
//		history_popwin_role.setBackgroundDrawable(drawable);
		
		history_pop_roleinfo.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_history_cancle_icon")).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					history_popwin_role.dismiss();
				}
			});
		//点击进入游戏，事件监听
		history_pop_roleinfo.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_history_enter")).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//进入游戏处理
				if(null!=history_popwin_role && history_popwin_role.isShowing())
					history_popwin_role.dismiss();
				if(MemberBaseInfo.USER_FACEBOOK.equals(userSelected.platform) 
						|| MemberBaseInfo.USER_GOOGLE.equals(userSelected.platform)){
					buttonOnClick(v);
				}
				if(MemberBaseInfo.USER_OASIS.equals(userSelected.platform)){
					showViewByUIType(UITYPE_NOTICE_TOLOGININPUT);
				}
			}
		});
		
		historylistView = (LoginHistoryListView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_history_accountinfo"));
		historylistView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				roleInfolist = recentUserList.recentUser.get(position).list;
				userSelected = recentUserList.recentUser.get(position);
				
				if(userSelected.loginType == 1){
					loginByAuto();
					return;
				}
				
				roleInfoAdapter = new HistoryRoleInfoAdapter(OasisSdkLoginActivity.this, roleInfolist,
						recentUserList.pageCount, null);
				history_roleinfo_list.setAdapter(roleInfoAdapter);
				history_popwin_role.showAtLocation((View) historylistView.getParent(), Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM, 0, 0);
			}
		});
		historylistView.setOnLoadListener(new OnLoadListener() {
			
			@Override
			public void onLoad() {
				if(!recentUserList.isEnd()){
					getHistoryLoginInfo(nextpage);
					
				}
			}
		});
		findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_function")).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(userSelected!=null)
					userSelected=null;
				showback = true;
				showViewByUIType(UITYPE_PAGELOGINSELECT);
			}
		});
	}
	
	//打开其他应用
	public static void openOtherApp(PackageInfo pi,Context context) { 
        PackageManager packageManager = context.getPackageManager(); 
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null); 
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER); 
            resolveIntent.setPackage(pi.packageName); 
            List<ResolveInfo> apps = packageManager.queryIntentActivities(resolveIntent, 0); 
            ResolveInfo ri = apps.iterator().next(); 
            if (ri != null ) { 
                String className = ri.activityInfo.name; 
                Intent intent = new Intent(Intent.ACTION_MAIN); 
                intent.addCategory(Intent.CATEGORY_LAUNCHER); 
                ComponentName cn = new ComponentName(pi.packageName, className); 
                intent.setComponent(cn); 
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent); 
            } 
    } 

	private void initRegistView() {
		view_regist.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_register_view_submit"))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						buttonOnClick(arg0);
					}
				});

		et_regist_u = (EditText) view_regist
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_register_view_username"));
		et_regist_p = (EditText) view_regist
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_register_view_password"));
		tv_regist_clean_u = (LinearLayout) view_regist
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_register_view_username_clean"));
		tv_regist_clean_p = (LinearLayout) view_regist
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_register_view_password_clean"));

		et_regist_u.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				if (arg0.length() > 0) {
					tv_regist_clean_u.setVisibility(View.VISIBLE);
					findViewById(
							BaseUtils.getResourceValue("id", "oasisgames_sdk_login_register_view_username_clean_img"))
									.setBackgroundResource(BaseUtils.getResourceValue("drawable",
											"oasisgames_sdk_common_input_bg_clean_blue"));
				} else
					tv_regist_clean_u.setVisibility(View.INVISIBLE);
			}
		});

		tv_regist_clean_u.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				et_regist_u.setText("");
			}
		});
		et_regist_p.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				if (arg0.length() > 0) {
					tv_regist_clean_p.setVisibility(View.VISIBLE);
					findViewById(
							BaseUtils.getResourceValue("id", "oasisgames_sdk_login_register_view_password_clean_img"))
									.setBackgroundResource(BaseUtils.getResourceValue("drawable",
											"oasisgames_sdk_common_input_bg_clean_blue"));
				} else
					tv_regist_clean_p.setVisibility(View.INVISIBLE);
			}
		});
		tv_regist_clean_p.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				et_regist_p.setText("");
			}
		});
		TextView ruleContentURL = (TextView) findViewById(
				BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_view_rule_content"));
		String rule = (String) ruleContentURL.getText().toString();
		ruleContentURL.setText(Html.fromHtml("<html><u>" + rule + "</u></html>"));
		// ruleContentURL.getPaint().setFlags(TextPaint.UNDERLINE_TEXT_FLAG);
		ruleContentURL.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				popUserRule();
			}
		});
	}

	public void onClickForgetpw(View v) {
		String uname = "";
		if(et_login_u != null)
			uname = et_login_u.getText().toString().trim();
		startActivity(new Intent(this, OasisSdkWebActivity.class).putExtra("type", 2).putExtra("uname", uname));// 忘记密码
	}

	private void initNoticeView() {
		if(accredit_confirm==null)
			accredit_confirm = (Button) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_accredit_confirm"));
		if(accredit_icon==null)
			accredit_icon = (ImageView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_accredit_icon"));
		if(accredit_notice==null)
			accredit_notice = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_accredit_notice"));
		
		accredit_notice_fb = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_accredit_notice_fb"));
		try {
			packageinfo = OasisSdkLoginActivity.this.getPackageManager().getPackageInfo("com.facebook.katana", 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		accredit_toselectpage = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_accredited_toselectpage"));
		accredit_notice_fb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) { 
				  openOtherApp(packageinfo, getApplicationContext());

			}
		});
		//点击前往选择页面
		accredit_toselectpage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showback=false;
				SystemCache.userInfo.error=null;
				if(userSelected!=null){
					userSelected=null;
				}
				int size = OasisSdkLoginActivity.this.UITypeRank.size();
				if(UITYPE_NOTICE_ERROR == OasisSdkLoginActivity.this.UITypeRank.get(size-1))
					OasisSdkLoginActivity.this.UITypeRank.remove(size-1);
				showViewByUIType(UITYPE_PAGELOGINSELECT);
				ReportUtils.add(ReportUtils.DEFAULTEVENT_ACCREDIT_EXCHANGE_LOGIN,new ArrayList<String>(), new ArrayList<String>());
			}
		});
		findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_reaccredit"))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (userSelected!=null) {
					// 验证第三方账号
					if(MemberBaseInfo.USER_FACEBOOK.equals(userSelected.platform)){
						buttonOnClick(arg0);
					}else if(MemberBaseInfo.USER_GOOGLE.equals(userSelected.platform)){
						buttonOnClick(arg0);
					}
				}
				ReportUtils.add(ReportUtils.DEFAULTEVENT_ACCREDIT_REAUTHOR,new ArrayList<String>(), new ArrayList<String>());
			}
		});
		findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_accredited_exitgame"))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ReportUtils.add(ReportUtils.DEFAULTEVENT_ACCREDIT_EXIT_GAME,new ArrayList<String>(), new ArrayList<String>());
				SystemCache.isExit = true;
				finish();
			}
		});
	}
	private void initLoginView() {
		view_login.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_view_submit"))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						buttonOnClick(arg0);
					}
				});
		et_login_u = (EditText) findViewById(
				BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_view_username"));
		et_login_p = (EditText) findViewById(
				BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_view_password"));
		tv_login_clean_u = (LinearLayout) findViewById(
				BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_view_usernames_clean"));
		tv_login_clean_p = (LinearLayout) findViewById(
				BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_view_password_clean"));
		if(userSelected!=null && !TextUtils.isEmpty(userSelected.username)){
			if(!"null".equals(userSelected.username)){
			et_login_u.setText(userSelected.username);
			}
			
			et_login_u.setEnabled(false);
			tv_login_clean_u.setVisibility(View.GONE);
			btnMoreUsers.setVisibility(View.GONE);
			et_login_p.setText("");
		}

		et_login_u.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				if (arg0.length() > 0) {
					tv_login_clean_u.setVisibility(View.VISIBLE);
					findViewById(
							BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_view_usernames_clean_img"))
									.setBackgroundResource(BaseUtils.getResourceValue("drawable",
											"oasisgames_sdk_common_input_bg_clean_blue"));
				} else {
					tv_login_clean_u.setVisibility(View.GONE);
				}
			}
		});

		tv_login_clean_u.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				et_login_u.setText("");
			}
		});
		et_login_p.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				if (arg0.length() > 0) {
					tv_login_clean_p.setVisibility(View.VISIBLE);
					findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_view_password_clean_img"))
							.setBackgroundResource(BaseUtils.getResourceValue("drawable",
									"oasisgames_sdk_common_input_bg_clean_blue"));
				} else
					tv_login_clean_p.setVisibility(View.GONE);
			}
		});
		tv_login_clean_p.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				et_login_p.setText("");
			}
		});

		TextView tv_forgotpw = (TextView) findViewById(
				BaseUtils.getResourceValue("id", "oasisgames_sdk_login_login_view_forgotpw"));
		tv_forgotpw.setText(Html.fromHtml(
				"<html><u>" + getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_forgetpw_text"))
						+ "</html></u>"));
		tv_forgotpw.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onClickForgetpw(v);
			}
		});
	}

	boolean showAutoLoginExceptionHandlerFlag = false;



	/**
	 * 自动登录异常处理
	 */
	private void showAutoLoginExceptionHandler(final int exceptionType, final int userType) {
		final AlertDialog d = new AlertDialog.Builder(this).create();
		d.show();
		d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_dialog_notitle"));
		d.setCanceledOnTouchOutside(false);

		TextView tv_content = (TextView) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_content"));

		TextView tv_sure = (TextView) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_sure"));

		if (exceptionType == -1) {// 自动登录异常
			tv_content.setText(getResources().getString(
					BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_autologin_exception")));
			tv_sure.setText(getResources().getString(
					BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_autologin_exception_btn")));
		} else if (exceptionType == 0) {// 自动登录失败
			if (userType == 1) {
				tv_content.setText(getResources()
						.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_autologin_fail")));
				tv_sure.setText(getResources().getString(
						BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_autologin_exception_btn")));
			} else {
				tv_content.setText(getResources()
						.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_autologin_fail")));
				tv_sure.setText(getResources().getString(
						BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_autologin_fail_btn")));
			}
		} else if (exceptionType == 1) {// 登录被封禁
			if (SystemCache.userInfo != null && "-13".equals(SystemCache.userInfo.error))
				tv_content.setText(getResources().getString(
						BaseUtils.getResourceValue("string", "oasisgames_sdk_common_errorcode_negative_13")));// (设备被封)
			else // if("-14".equals(SystemCache.userInfo.error))
				tv_content.setText(getResources().getString(
						BaseUtils.getResourceValue("string", "oasisgames_sdk_common_errorcode_negative_14")));// (账号被封)
			tv_sure.setText(
					getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_7")));
		}

		tv_sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				d.dismiss();
				// d.cancel();

			}
		});
		TextView tv_cancle = (TextView) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_cancle"));
		tv_cancle.setVisibility(View.GONE);

		TextView tv_text = (TextView) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_text"));
		if (exceptionType == 1) {// 登录被封禁
			tv_text.setVisibility(View.GONE);
		} else
			tv_text.setVisibility(View.VISIBLE);
		tv_text.setText(Html.fromHtml("<html><u>"
				+ getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_7"))
				+ "</u></html>"));
		tv_text.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showAutoLoginExceptionHandlerFlag = true;
				d.dismiss();
				// d.cancel();
				// setWaitScreen(false);
				// SystemCache.isExit = true;
				// finish();
			}
		});

		d.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface arg0) {
				if (showAutoLoginExceptionHandlerFlag) {
					showAutoLoginExceptionHandlerFlag = false;
					setWaitScreen(false);
					SystemCache.isExit = true;
					finish();
				} else {
					if (exceptionType == 1) {// 登录被封禁
						setWaitScreen(false);
						SystemCache.isExit = true;
						finish();
					} else {
						if (exceptionType == -1 || userType == 1) {// 自动登录异常 或者
																	// 匿名账号自动登录失败
							showViewByUIType(UITYPE_PAGEAUTOLOGIN);
						} else
							showViewByUIType(UITYPE_PAGELOGINSELECT);
					}

				}
			}
		});
	}

	/**
	 * 匿名账号自动登录，提示关联操作
	 */
	private void showBindNotice() {
		final AlertDialog d = new AlertDialog.Builder(this).create();
		d.show();
		d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_dialog_notitle"));
		d.setCanceledOnTouchOutside(false);

		TextView tv_content = (TextView) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_content"));
		tv_content.setText(
				getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_6")));

		TextView tv_sure = (TextView) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_sure"));
		tv_sure.setText(
				getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_5")));
		tv_sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent()
						.setClass(OasisSdkLoginActivity.this.getApplicationContext(), OasisSdkBindActivity.class)
						.putExtra("isVisibility", false));
				d.dismiss();
			}
		});
		TextView tv_cancle = (TextView) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_cancle"));
		tv_cancle.setVisibility(View.GONE);

		TextView tv_text = (TextView) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_text"));
		String goon = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_bind_togame"));
		tv_text.setText(Html.fromHtml("<html><u>" + goon + "</u></html>"));
		tv_text.setVisibility(View.VISIBLE);
		tv_text.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});

		d.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface arg0) {
				finish();
			}
		});
	}

	/**
	 * 正式OAS账号，提示完善用户资料
	 */
	private void showAddPersonalInfoNotice() {
		final AlertDialog d = new AlertDialog.Builder(this).create();
		d.show();
		d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_dialog_notitle"));
		d.setCanceledOnTouchOutside(false);

		TextView tv_content = (TextView) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_content"));
		tv_content.setText(getResources()
				.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_addpersonalinfo")));

		TextView tv_sure = (TextView) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_sure"));
		tv_sure.setText(
				getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_common_btn_sure"))); // oasisgames_sdk_pcenter_notice_7
																													// (账户资料)替换为
																													// oasisgames_sdk_common_btn_sure（确认）
		tv_sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent()
						.setClass(OasisSdkLoginActivity.this.getApplicationContext(), OasisSdkWebActivity.class)
						.putExtra("type", 1));// 完善资料界面

				d.dismiss();
			}
		});
		TextView tv_cancle = (TextView) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_cancle"));
		tv_cancle.setVisibility(View.GONE);

		TextView tv_text = (TextView) d
				.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_text"));
		tv_text.setText(Html.fromHtml("<html><u>"
				+ getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_bind_togame"))
				+ "</u></html>"));// 继续游戏
		tv_text.setVisibility(View.VISIBLE);
		tv_text.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});

		d.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface arg0) {
				finish();
			}
		});
	}

	class MyGoogleLoginCallback implements GoogleUtils.GoogleLoginCallback {
		@Override
		public void success(Person p, String email, String token) {
			String personName = "";
			if (p != null)
				personName = p.getDisplayName();

			if (TextUtils.isEmpty(personName))
				personName = email;
			// System.out.println("========Name="+personName +";
			// email="+email+"; token="+token);
			// loginByGoogle(personName, email, token);
			Message msg = new Message();
			msg.what = 101;
			msg.obj = personName + "oasistag" + email + "oasistag" + token;
			myHandler.sendMessage(msg);
		}

		@Override
		public void exception(Exception e) {			
			if (e instanceof UserRecoverableAuthException) {
				Log.e(TAG, "Google Exception:UserRecoverableAuthException ");
				e.printStackTrace();
				Intent intent = ((UserRecoverableAuthException) e).getIntent();
				startActivityForResult(intent, GoogleUtils.REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
				// mAuthException = true;
			} else if (e instanceof GoogleAuthException) {
				Log.e(TAG, "Google Exception:GoogleAuthException ");
				e.printStackTrace();
				myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				mSignInClicked = false;
				// mAuthException = false;
			} else if (e instanceof IOException) {
				Log.e(TAG, "Google Exception:IOException ");
				e.printStackTrace();
				myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				mSignInClicked = false;
				// mAuthException = false;
			}

		}
	}

	/**
	 * Fetching user's information name, email, profile pic
	 */
	private void getProfileInformation() {
		final MyGoogleLoginCallback callback = new MyGoogleLoginCallback();
		final Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
		final String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
		BaseUtils.logDebug(TAG, "email: " + email);
		if (!TextUtils.isEmpty(email)) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					String token;
					try {
						token = GoogleAuthUtil.getToken(OasisSdkLoginActivity.this.getApplicationContext(), email,
								"oauth2:" + Scopes.PROFILE + " https://www.googleapis.com/auth/userinfo.profile");
						BaseUtils.logDebug(TAG, "token: " + token);

						callback.success(currentPerson, email, token);
					} catch (Exception e) {
						callback.exception(e);
					}

				}
			}).start();
		}
		if (currentPerson != null) {

			String personName = currentPerson.getDisplayName();
			String personPhotoUrl = currentPerson.getImage().getUrl();
			String personGooglePlusProfile = currentPerson.getUrl();

			Log.d(TAG, "Name: " + personName + ", plusProfile: " + personGooglePlusProfile + ", email: " + email
					+ ", Image: " + personPhotoUrl);

			// txtName.setText(personName);
			// txtEmail.setText(email);

			// by default the profile url gives 50x50 px image only
			// we can replace the value with whatever dimension we want by
			// replacing sz=X
			// personPhotoUrl = personPhotoUrl.substring(0,
			// personPhotoUrl.length() - 2)
			// + PROFILE_PIC_SIZE;
			//
			// new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);

		}
	}

	@Override
	protected void onResume() {
		if (curView != null)
			buttonOnClick(curView);
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();

		/*
		 * 注释此代码，是不想一进来就connect，希望是用户想connect时再执行 if(!mAuthException)
		 * mGoogleApiClient.connect();
		 */
	}

	@Override
	protected void onStop() {
		super.onStop();
		mGoogleApiClient.disconnect();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(UITypeRank != null)
			UITypeRank.clear();
		myHandler.removeCallbacksAndMessages(null);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!mIntentInProgress) {
			if (mSignInClicked) {
				if (result.hasResolution()) {
					// The user has already clicked 'sign-in' so we attempt to
					// resolve all
					// errors until the user is signed in, or they cancel.
					try {
						result.startResolutionForResult(this, RC_SIGN_IN);
						mIntentInProgress = true;
					} catch (SendIntentException e) {
						// The intent was canceled before it was sent. Return to
						// the default
						// state and attempt to connect to get an updated
						// ConnectionResult.
						mIntentInProgress = false;
						// mAuthException = false;
						mSignInClicked = false;
						setWaitScreen(false);
						myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
					}
				} else {
					mIntentInProgress = false;
					// mAuthException = false;
					mSignInClicked = false;
					setWaitScreen(false);
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				}
			}
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		if (mSignInClicked) {
			getProfileInformation();
		}
		// mSignInClicked = false;
		// BaseUtils.showMsg(this, "User is connected!");
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		mGoogleApiClient.connect();
	}
}
