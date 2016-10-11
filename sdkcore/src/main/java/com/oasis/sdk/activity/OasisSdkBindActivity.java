package com.oasis.sdk.activity;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.oasis.sdk.OASISPlatformConstant;
import com.oasis.sdk.activity.platform.FacebookUtils;
import com.oasis.sdk.activity.platform.GoogleUtils;
import com.oasis.sdk.base.Exception.OasisSdkDataErrorException;
import com.oasis.sdk.base.Exception.OasisSdkException;
import com.oasis.sdk.base.entity.MemberBaseInfo;
import com.oasis.sdk.base.entity.PhoneInfo;
import com.oasis.sdk.base.report.ReportUtils;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.GuideView;
import com.oasis.sdk.base.utils.SystemCache;
/**
 * 绑定用户/关联免注册账号
 * @author Administrator
 *
 */
public class OasisSdkBindActivity extends OasisSdkBaseActivity implements
GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener{
	public static final String TAG = OasisSdkBindActivity.class.getName();
	int curBindStyle = 0;
	boolean isShowRelative = true;
	
	LinearLayout bindToOASLayout, bindToOther, btn_guide;
	View bind_repw_layout;
	TextView tv_rule;//注册协议
	
	EditText et_login_u, et_login_p;
	View tv_login_clean_u, tv_login_clean_p;
	View view_bind1, view_bind2;
	
	String username = "";
	String password = "";
	
	String oldUserID = "";
	
	// 声明一个Handler对象
	public MyHandler myHandler = null;
	
	FacebookUtils fb = null;
	
	
	/* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;
    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;
    private Boolean mIntentInProgress = false;
    private Boolean mSignInClicked = false;
//    private Boolean mAuthException = false;
    
    GuideView guide;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_bind"));
		
		myHandler = new MyHandler(this);
		
		initHead(true, new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(isShowRelative && (curBindStyle == 1 || curBindStyle == 2)){
					curBindStyle = 0;
					changeView();
				}else{
					setResult(OASISPlatformConstant.RESULT_CANCLE, null);
					finish();
				}
			}
		}, true, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pcenter_notice_11")));
		
		btn_guide = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_function"));
		btn_guide.getChildAt(0).setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_common_head_function"));
		btn_guide.setVisibility(View.VISIBLE);
		
		view_bind1 = findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind1"));
		view_bind2 = findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind2"));
		
		if(null != SystemCache.userInfo)
			oldUserID = SystemCache.userInfo.uid;

		tv_rule = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_rule"));
		
		et_login_u = (EditText)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_username"));
		et_login_p = (EditText)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_pw"));
		tv_login_clean_u = (View)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_username_clean"));
		tv_login_clean_p = (View)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_password_clean"));
		
		et_login_u.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				if(arg0.length() > 0){
					tv_login_clean_u.setVisibility(View.VISIBLE);
					findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_username_clean_img")).setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_common_input_bg_clean_blue"));
				}
				else
					tv_login_clean_u.setVisibility(View.INVISIBLE);
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
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				if(arg0.length() > 0){
					tv_login_clean_p.setVisibility(View.VISIBLE);
					findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_password_clean_img")).setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_common_input_bg_clean_blue"));
				}else
					tv_login_clean_p.setVisibility(View.INVISIBLE);
			}
		});
		tv_login_clean_p.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				et_login_p.setText("");
			}
		});
		
		setOnClickListener();
		
		isShowRelative = getIntent().getBooleanExtra("isVisibility", true);
		if(!isShowRelative){// 不显示 “关联至”功能
			view_bind1.setVisibility(View.GONE);
			view_bind2.setVisibility(View.VISIBLE);
			curBindStyle = 1;
			changeView();
		}
		setWaitScreen(false);
		
		mGoogleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(Plus.API)
        .addScope(new Scope("profile"))
        .build();
	}
	
	private void setOnClickListener(){
		btn_guide.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				// 关闭可能显示的键盘
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
				View view = OasisSdkBindActivity.this.getCurrentFocus();
				if(v!=null && view != null)
					imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); 
				
				if(curBindStyle == 0 || curBindStyle == 3){// 默认或者第三方关联时
//					if(layout_guide1.isShown())
//						layout_guide1.setVisibility(View.GONE);
//					else
//						layout_guide1.setVisibility(View.VISIBLE);
					showGuide(0);
				}else{
//					layout_guide1.setVisibility(View.GONE);
//					
//					if(layout_guide2.isShown())
//						layout_guide2.setVisibility(View.GONE);
//					else
//						layout_guide2.setVisibility(View.VISIBLE);
					showGuide(2);
				}
			}
		});
		tv_rule.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(curBindStyle == 2)// 关联已存在的OAS账号
					startActivity(new Intent(OasisSdkBindActivity.this.getApplicationContext(), OasisSdkWebActivity.class).putExtra("type", 2));// 忘记密码
				else if(curBindStyle == 1)// 关联新注册的OAS账号
					popUserRule();
			}
		});
		findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_facebook")).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSignInClicked = false;// 将google点击操作置为false
				onClick_Facebook(v);
			}
		});
		findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_oas")).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				curBindStyle = 2;// 已有账号关联
				mSignInClicked = false;// 将google点击操作置为false
				changeView();
			}
		});
		findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_google")).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				googleLogin();
				if (mGoogleApiClient.isConnected())
					mGoogleApiClient.clearDefaultAccountAndReconnect();
				
				setWaitScreen(true);
//				if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
				    mSignInClicked = true;
				    mGoogleApiClient.connect();
//				}else{
//					getProfileInformation();
//				}
			}
		});
		
	}
	private void changeView(){
		if(curBindStyle == 2){
			setHeadTitle(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_btn_submit")));
			String rule = getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_forgetpw_text"));
			tv_rule.setText(Html.fromHtml("<html><u>"+rule+"</u></html>")); 
			
			((Button)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_submit"))).setText(getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_btn_submit")));
			((EditText)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_username"))).setText("");
			((EditText)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_pw"))).setText("");
			
			view_bind1.setVisibility(View.GONE);
			view_bind2.setVisibility(View.VISIBLE);
			
			btn_guide.setVisibility(View.INVISIBLE);
		}else if(curBindStyle == 1){
			setHeadTitle(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_head_title_register_1")));
			String rule = getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_bind_btn_userrule"));
			tv_rule.setText(Html.fromHtml("<html><u>"+rule+"</u></html>")); 
			
			((Button)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_submit"))).setText(getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_bind_btn_regist")));
			((EditText)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_username"))).setText("");
			((EditText)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_pw"))).setText("");
			
			btn_guide.setVisibility(View.VISIBLE);
		}else if(curBindStyle == 3){
			// nothing
		}else if(curBindStyle == 0){
			setHeadTitle(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_pcenter_notice_11")));
			view_bind1.setVisibility(View.VISIBLE);
			view_bind2.setVisibility(View.GONE);

			btn_guide.setVisibility(View.VISIBLE);
		}
	}
	
	private boolean check(){
		username = ((EditText)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_username"))).getText().toString().trim();
		password = ((EditText)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_pw"))).getText().toString().trim();
		if(!checkUserInfo())
			return true;
		
		return false;
	}
	/**
	 * 各数据项合法性验证
	 * @return
	 */
	private boolean checkUserInfo(){
		if(TextUtils.isEmpty(username)){
			BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_hint_username")));
			return false;
		}
		if(username.length() < 6 || username.length() > 50){
			BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_username_notice_error_length")));
			return false;
		}
		if(username.contains("@") && !BaseUtils.regexEmail(username)){//包含@符，并且不符合邮箱规则
			BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_username_notice_error")));
			return false;
		}else if(!username.contains("@")){//不包含@符，是普通账号
			if(BaseUtils.regexNum(username)){// 不能为纯数字
				BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_username_notice_error1")));
				return false;
			}else if(!BaseUtils.regexAccount(username)){// 只能包含 a-zA-Z0-9_
				BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_username_notice_error2")));
				return false;
			}
		}
		
		if(TextUtils.isEmpty(password)){
			BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_hint_password")));
			return false;
		}
		if(password.length() < 6 || password.length() > 20){
			BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_password_notice_error")));
			return false;
		}

		return true;
	}
	public void onClick(View v){
		if(v.getId() == BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_notuser")){
			view_bind1.setVisibility(View.GONE);
			view_bind2.setVisibility(View.VISIBLE);
			curBindStyle = 1;
			changeView();
			return;
		}
		if(curBindStyle == 2){// 已有账号绑定
			onClick_Old(v);
			return;
		}
		curBindStyle = 1;
		if(check())
			return;
		if(!BaseUtils.regexSpecilChar(password)){
			BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_password_notice_error2")));
			return;
		}
		setWaitScreen(true);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					HttpService.instance().bindUser(1, MemberBaseInfo.USER_OASIS, username, password, "");
					myHandler.sendEmptyMessage(HANDLER_RESULT);
				} catch (OasisSdkException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION_NETWORK);
				} catch (OasisSdkDataErrorException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				}
			}
		}).start();
	}
	
	public void onClick_Old(View v){
		if(check())
			return;
		setWaitScreen(true);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					HttpService.instance().bindUser(2, MemberBaseInfo.USER_OASIS, username, password, "");
					myHandler.sendEmptyMessage(HANDLER_RESULT);
				} catch (OasisSdkException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION_NETWORK);
				} catch (OasisSdkDataErrorException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				}
			}
		}).start();
		
	}
	public void onClick_Facebook(View v){
//		if(check())
//			return;
//		if(null != OASISPlatform.getUserInfo() 
//				&& !TextUtils.isEmpty(OASISPlatform.getUserInfo().getToken())
//				&& !TextUtils.isEmpty(OASISPlatform.getUserInfo().getPlatform_token())){
//			bindUserToPlatform();
//			return;
//		}

		fb = new FacebookUtils(this);
		
        LoginManager.getInstance().registerCallback(FacebookUtils.getCallbackManager(),
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                    	setWaitScreen(true);
                        // App code
                    	getFBProfile();
                    }

                    @Override
                    public void onCancel() {
                         // App code
                    	BaseUtils.logDebug(TAG, "============FB login onCancel()");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                         // App code   
                    }
        });
        FacebookUtils.logout();
//        if(fb.loginCheck(this)){
//        	setWaitScreen(true);
//        	bindUserToPlatform("facebook", "facebook", AccessToken.getCurrentAccessToken().getToken(), Profile.getCurrentProfile()==null?MemberBaseInfo.USER_FACEBOOK:Profile.getCurrentProfile().getName());
//        }
//		else
			fb.login(this);
	}

	private void getFBProfile(){
		setWaitScreen(true);
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
                    	bindUserToPlatform(MemberBaseInfo.USER_FACEBOOK, "", AccessToken.getCurrentAccessToken().getToken(), Profile.getCurrentProfile()==null?"":Profile.getCurrentProfile().getName());
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,birthday,email,gender,first_name,middle_name,last_name,link");
        request.setParameters(parameters);
        request.executeAsync();
		
	}
	private void bindUserToPlatform(final String platform, final String name, final String token, final String oasNickName){
		
		curBindStyle = 3;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					username = name;
					password = token;
					HttpService.instance().bindUser(3, platform, name, token, oasNickName); 
					myHandler.sendEmptyMessage(HANDLER_RESULT);
				} catch (OasisSdkException e) {
					Log.d("Bind", "Bind is fail。"+e.getMessage());
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION_NETWORK);
				} catch (OasisSdkDataErrorException e) {
					Log.d("Bind", "Bind is fail。"+e.getMessage());
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				}
			}
		}).start();
	}
	private void switchUser(){
		setWaitScreen(true);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					HttpService.instance().login(curBindStyle, SystemCache.bindInfo.platform, username, password, SystemCache.bindInfo.oasnickname, "");

					myHandler.sendEmptyMessage(100);
					
				} catch (OasisSdkException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION_NETWORK);
				} catch (OasisSdkDataErrorException e) {
					myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				} 	
			}
		}).start();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(fb != null)
			fb.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == RC_SIGN_IN) {
		    if (resultCode != RESULT_OK) {
		    	mSignInClicked = false;
		    	mIntentInProgress = false;
//				mAuthException = false;
		    	setWaitScreen(false);
		    	return;
		    }

		    mIntentInProgress = false;

		    if (!mGoogleApiClient.isConnected()) {
		      mGoogleApiClient.reconnect();
		    }
		  }
		if(requestCode == GoogleUtils.REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR){
			if (resultCode != RESULT_OK) {
				mSignInClicked = false;
				mIntentInProgress = false;
//				mAuthException = false;
				setWaitScreen(false);
				return;
			}
			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnected()) {
				mGoogleApiClient.reconnect();
			}
		}
	}
	
//	String googleEmail; // Received from newChooseAccountIntent(); passed to getToken()
//	private static final String GOOGLESCOPE =
//	        "oauth2:https://www.googleapis.com/auth/userinfo.profile";
//
//	/**
//	 * Attempts to retrieve the username.
//	 * If the account is not yet known, invoke the picker. Once the account is known,
//	 * start an instance of the AsyncTask to get the auth token and do work with it.
//	 */
//	private void getGoogleAccount() {
//	    if (googleEmail == null) {
//			String[] accountTypes = new String[]{"com.google"};
//		    Intent intent = AccountPicker.newChooseAccountIntent(new Account("xusongbo0303@gmail.com", "com.google"), null,
//		            accountTypes, false, null, null, null, null);
//		    startActivityForResult(intent, REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR2);
//	    } else {
//	    	setWaitScreen(true);
//	           new GetGoogleAccountTokenTask(this, googleEmail, GOOGLESCOPE).execute();
////	            Toast.makeText(this, R.string.not_online, Toast.LENGTH_LONG).show();
//	    }
//	}
//	public class GetGoogleAccountTokenTask extends AsyncTask{
//	    Activity mActivity;
//	    String mScope;
//	    String mEmail;
//
//	    GetGoogleAccountTokenTask(Activity activity, String name, String scope) {
//	        this.mActivity = activity;
//	        this.mScope = scope;
//	        this.mEmail = name;
//	    }
//		@Override
//		protected void onCancelled() {
//			setWaitScreen(false);
//			super.onCancelled();
//		}
//		@Override
//		protected void onPostExecute(Object result) {
//			setWaitScreen(false);
//			super.onPostExecute(result);
//		}
//	    /**
//	     * Executes the asynchronous job. This runs when you call execute()
//	     * on the AsyncTask instance.
//	     */
//	    @Override
//	    protected Void doInBackground(Object... params) {
//	    	
//	        try {
//	            String token = fetchToken();
//	            if (token != null) {
//	                // Insert the good stuff here.
//	                // Use the token to access the user's Google data.
//	            	
//	            	bindUserToPlatform(MemberBaseInfo.USER_GOOGLE, mEmail, token);
//	            	
//	            }
//	        } catch (IOException e) {
//	            // The fetchToken() method handles Google-specific exceptions,
//	            // so this indicates something went wrong at a higher level.
//	            // TIP: Check for network connectivity before starting the AsyncTask.
//	        	System.out.println("=========获取token失败");
//	        }
//	        return null;
//	    }
//	    
//
//	    /**
//	     * Gets an authentication token from Google and handles any
//	     * GoogleAuthException that may occur.
//	     */
//	    protected String fetchToken() throws IOException {
//	        try {
//	            return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
//	        } catch (UserRecoverableAuthException userRecoverableException) {
//	            // GooglePlayServices.apk is either old, disabled, or not present
//	            // so we need to show the user some UI in the activity to recover.
//	            ((OasisSdkBindActivity)mActivity).handleGoogleException(userRecoverableException);
//	        } catch (GoogleAuthException fatalException) {
//	            // Some other type of unrecoverable exception has occurred.
//	            // Report and log the error as appropriate for your app.
//	        }
//	        return null;
//	    }
//	}
//	static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
//	static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR2 = 1002;
//	/**
//	 * This method is a hook for background threads and async tasks that need to
//	 * provide the user a response UI when an exception occurs.
//	 */
//	public void handleGoogleException(final Exception e) {
//	    // Because this call comes from the AsyncTask, we must ensure that the following
//	    // code instead executes on the UI thread.
//	    runOnUiThread(new Runnable() {
//	        @Override
//	        public void run() {
//	            if (e instanceof GooglePlayServicesAvailabilityException) {
//	                // The Google Play services APK is old, disabled, or not present.
//	                // Show a dialog created by Google Play services that allows
//	                // the user to update the APK
//	                int statusCode = ((GooglePlayServicesAvailabilityException)e)
//	                        .getConnectionStatusCode();
//	                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
//	                        OasisSdkBindActivity.this,
//	                        REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
//	                dialog.show();
//	            } else if (e instanceof UserRecoverableAuthException) {
//	                // Unable to authenticate, such as when the user has not yet granted
//	                // the app access to the account, but the user can fix this.
//	                // Forward the user to an activity in Google Play services.
//	                Intent intent = ((UserRecoverableAuthException)e).getIntent();
//	                startActivityForResult(intent,
//	                        REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
//	            }
//	        }
//	    });
//	}
	static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkBindActivity> mOuter;

		public MyHandler(OasisSdkBindActivity activity) {
			mOuter = new WeakReference<OasisSdkBindActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			OasisSdkBindActivity outer = mOuter.get();
			if (outer != null) {
				switch (msg.what) {
//				case WAITDAILOG_CLOSE:
//					outer.setWaitScreen(false);
//					break;
				case HANDLER_RESULT:
					outer.setWaitScreen(false);
  
					if(SystemCache.bindInfo != null){// 绑定不成功
						if(!TextUtils.isEmpty(SystemCache.bindInfo.error) && "-8".equals(SystemCache.bindInfo.error)){
							outer.bindErrorHandler(1);
							return;
						}
						if(!TextUtils.isEmpty(SystemCache.bindInfo.error) && "-9".equals(SystemCache.bindInfo.error)){
							outer.bindErrorHandler9();
							return;
						}
						
						outer.myHandler.sendEmptyMessage(HANDLER_FAIL);
						return;
					}
					
					try {
						List<String> parameters = new ArrayList<String>();
						parameters.add("\"type\":\""+outer.curBindStyle+"\"");
						parameters.add("\"username\":\""+outer.username+"\"");
						parameters.add("\"platform\":\""+(outer.curBindStyle==3?SystemCache.bindInfo.platform:"")+"\"");
						
						List<String> status = new ArrayList<String>();
						status.add("\"event_type\":\"bind\"");
						
						ReportUtils.add(ReportUtils.DEFAULTEVENT_BIND, parameters, status);
					} catch (Exception e) {
					}
					
					outer.myHandler.sendEmptyMessage(HANDLER_SUCECCES);
					break;
				case HANDLER_SUCECCES:
					if(outer.curBindStyle == 1 || outer.curBindStyle == 2){// oas账号，需要计数；统计的数在登录成功后用于判断 是否显示 “完善资料”的提示
						int count = (Integer)BaseUtils.getSettingKVPfromSysCache("OASIS_USERLOGIN_COUNT", 0) + 1;
						BaseUtils.saveSettingKVPtoSysCache("OASIS_USERLOGIN_COUNT", count);
					}
					if(outer.curBindStyle == 1 && !TextUtils.isEmpty(outer.username)){// 注册用户
						outer.popSucNotice();
					}else{
						BaseUtils.showMsg(outer, outer.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_result_1")));
						outer.finish();
					}
					break;
				case HANDLER_FAIL:
					if(SystemCache.bindInfo != null && !TextUtils.isEmpty(SystemCache.bindInfo.error)){
						if("-4".equals(SystemCache.bindInfo.error)||"-6".equals(SystemCache.bindInfo.error)
								||"-7".equals(SystemCache.bindInfo.error)){
							BaseUtils.showErrorMsg(outer, SystemCache.bindInfo.error);							
						}else if("-13".equals(SystemCache.bindInfo.error)){
//							BaseUtils.showDisableDialog(outer, "oasisgames_sdk_common_errorcode_negative_13");
							outer.startActivity(new Intent().setClass(outer, OasisSdkFeedbackActivity.class).putExtra("type", 0));// 设备被封，退出应用
						}else if("-15".equals(SystemCache.bindInfo.error)||"-14".equals(SystemCache.bindInfo.error)){
//							BaseUtils.showDisableDialog(outer, "oasisgames_sdk_common_errorcode_negative_14");
							outer.startActivity(new Intent().setClass(outer, OasisSdkFeedbackActivity.class).putExtra("type", 0));// Uid From 被封，退出应用
						}else if("-16".equals(SystemCache.bindInfo.error)){
//							BaseUtils.showDisableDialog(outer, "oasisgames_sdk_login_notice_13");
							outer.startActivity(new Intent().setClass(outer, OasisSdkFeedbackActivity.class).putExtra("type", 1));// Uid to 被封，退出反馈界面
						}else if("-12".equals(SystemCache.bindInfo.error)){// 第三方token失效
							if(MemberBaseInfo.USER_FACEBOOK.equals(SystemCache.bindInfo.platform)){
								FacebookUtils.logout();
							}else if(MemberBaseInfo.USER_GOOGLE.equals(SystemCache.bindInfo.platform)){
								outer.mGoogleApiClient.clearDefaultAccountAndReconnect();
							}
							BaseUtils.showDisableDialog(outer, "oasisgames_sdk_error_exception");
						}else{
							BaseUtils.showMsg(outer, outer.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_common_errorcode_negative_999"))+".Error code:"+SystemCache.bindInfo.error);
						}
						
					}
					
					break;
				case HANDLER_EXCEPTION:
					outer.setWaitScreen(false);
					BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_error_exception")));
					break;
				case 100:
					outer.setWaitScreen(false);
					if(null != SystemCache.userInfo && "ok".equals(SystemCache.userInfo.status)){
						if(!TextUtils.isEmpty(SystemCache.bindInfo.uid_from) && SystemCache.bindInfo.uid_from.equals(SystemCache.userInfo.uid)){
							BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_result_4")));
							return;
						}else{
							BaseUtils.showMsg(outer, outer.getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_result_1")));

							BaseUtils.clearInfoForLogout();// 登录、切换成功后，清楚服id、角色id
						}
						try {
							List<String> parameters = new ArrayList<String>();
							parameters.add("\"login_type\":\""+SystemCache.userInfo.loginType+"\"");
							parameters.add("\"username\":\""+outer.username+"\"");
							parameters.add("\"platform\":\""+SystemCache.userInfo.platform+"\"");
							parameters.add("\"uid\":\""+SystemCache.userInfo.uid+"\"");
							parameters.add("\"isreport\":\""+(PhoneInfo.instance().isTrackAble()?"Y":"N")+"\"");
							
							List<String> status = new ArrayList<String>();
							status.add("\"event_type\":\"login\"");
							status.add("\"login_type\":\""+SystemCache.userInfo.loginType+"\"");
							status.add("\"platform\":\""+SystemCache.userInfo.platform+"\"");
							ReportUtils.add(ReportUtils.DEFAULTEVENT_LOGIN, parameters, status);
						} catch (Exception e) {
							Log.e(TAG, outer.username + "-> add mdata event fail by " + ReportUtils.DEFAULTEVENT_LOGIN);
						}

						SystemCache.oasisInterface.reloadGame(SystemCache.userInfo);
						outer.finish();
					}else{
						if("-4".equals(SystemCache.userInfo.error)){
							BaseUtils.showErrorMsg(outer, SystemCache.userInfo.error);
						}else if("-13".equals(SystemCache.userInfo.error)){
							BaseUtils.showDisableDialog(outer, "oasisgames_sdk_common_errorcode_negative_13");
						}else if("-14".equals(SystemCache.userInfo.error)){
							BaseUtils.showDisableDialog(outer, "oasisgames_sdk_login_notice_14");//(切换的账号被封)
						}else{
							BaseUtils.showMsg(outer, outer.getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_common_errorcode_negative_999"))+".Error code:"+SystemCache.userInfo.error);
						}
					}
					break;
				case 10:
					outer.showGuide(0);
					break;
				default:
					
					break;
				}
			}
		}
	}
	/**
	 * 显示引导信息
	 * @param index 引导索引
	 */
	private void showGuide(int index){
		guide = new GuideView(this.getApplicationContext());
		Rect r = new Rect();
		if(index == 0){ 
			View layout = findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_items"));// 使用View兼容不同控件类型
			layout.getGlobalVisibleRect(r);
			guide.setPoint(r, layout.getHeight(), layout.getWidth(),
					getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_bind_guide_1")));
			guide.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					guide.setVisibility(View.GONE);
					showGuide(1);
				}
			});
			this.addContentView(guide, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		}
		if(index == 1){ 
			Button nouser = (Button) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_notuser"));
			nouser.getGlobalVisibleRect(r);
			guide.setPoint(r,
					nouser.getHeight(), nouser.getWidth(),
					getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_bind_guide_2")));
			guide.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					guide.setVisibility(View.GONE);
				}
			});
			this.addContentView(guide, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		}
		if(index == 2){ 
			EditText editUsername = (EditText) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_bind_username"));
			editUsername.getGlobalVisibleRect(r);
			guide.setPoint(r.left, r.top, r.right, r.bottom, 
					editUsername.getHeight(), editUsername.getWidth(),
					getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_guide_notice1")));
			guide.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					guide.setVisibility(View.GONE);
				}
			});
			this.addContentView(guide, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		}
		
		long count = (Long) BaseUtils.getSettingKVPfromSysCache("BINDGUIDECOUNT", 0L);
		if(count == 0){ 
			BaseUtils.saveSettingKVPtoSysCache("BINDGUIDECOUNT", 1L);
		}
	}
	/**
	 * 关联成功后，弹出对话框
	 */
	private void popSucNotice(){
		final AlertDialog d = new AlertDialog.Builder(this).create();
		d.show();
		d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_dialog_notitle"));
		
		TextView tv_content = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_content"));
		if(curBindStyle == 1 && !TextUtils.isEmpty(username)){// 注册用户
			String s = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_bind_success"));
			s = s.replace("OASIS", username);
			tv_content.setText(s);
		}
//		else
//			tv_content.setText(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_result_1));
		
		TextView tv_cancle = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_cancle"));
		tv_cancle.setVisibility(View.GONE);

		TextView tv_sure = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_sure"));
		tv_sure.setText(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_bind_togame")));
		tv_sure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				d.dismiss();
			}
		});
		d.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface arg0) {
				setResult(OASISPlatformConstant.RESULT_SUCCESS, null);
				finish();
			}
		});
		
//		BaseUtils.showMsg(this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_bind_success")));
	}
	
	boolean isCloseRulePage = false;
	private void popUserRule(){
		final AlertDialog d = new AlertDialog.Builder(this).create();
		d.show();
		d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_login_userrule_dialog"));
		WebView rule = (WebView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_userrule_webview"));
		int color = getResources().getColor(BaseUtils.getResourceValue("color", "transparent_background"));
		rule.setBackgroundColor(color); // 设置背景色
		rule.loadUrl("http://mobile.oasgames.com/about/TermsofService.php?lang="+Locale.getDefault().getLanguage());
		rule.getSettings().setJavaScriptEnabled(true);//可用JS
		rule.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				
				view.loadUrl(url);// 使用当前WebView处理跳转
				return true;//true表示此事件在此处被处理，不需要再广播
			}
			@Override	//转向错误时的处理
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
			}
			public void onPageFinished(WebView view, String url) {
				setWaitScreen(false);
			};
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if(!isCloseRulePage)
					setWaitScreen(true);
				super.onPageStarted(view, url, favicon);
			}
			
		});
		LinearLayout tv = (LinearLayout)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_login_userrule_close"));
		tv.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});
		d.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				isCloseRulePage = true;
			}
		});
	}
	
	/**
	 * 当关联账号存在角色时，弹出提示框，警示用户
	 */
	private void bindErrorHandler(final int type){
		
		final AlertDialog d = new AlertDialog.Builder(this).create();
		d.show();
		d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_dialog_notitle"));
		
		TextView tv_content = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_content"));
		String content = "";
		
			if(type == 1)
				content = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_bind_notice_error"));
			else if(type == 2){
				content = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_bind_notice_error2"));
				content = content.replace("PERMANENTLYLOST", "<font color=\"red\">"+(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_bind_notice_error3")))+"</font>");
			}
		String oldStr = username;
		if(curBindStyle==3){
			oldStr = TextUtils.isEmpty(SystemCache.bindInfo.oasnickname)?SystemCache.bindInfo.platform:SystemCache.bindInfo.oasnickname; 
		}
		content = content.replace("USERNAME", TextUtils.isEmpty(oldStr)?"":" \""+oldStr+ "\" ");// </font>
		tv_content.setText(Html.fromHtml(content));
		
		TextView tv_cancle = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_cancle"));
		tv_cancle.setText(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_common_btn_cancle")));
		tv_cancle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(MemberBaseInfo.USER_GOOGLE.equalsIgnoreCase(SystemCache.bindInfo.platform)){
					if (mGoogleApiClient.isConnected()) {
					      mGoogleApiClient.clearDefaultAccountAndReconnect();
//					      Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
					}
				}
				else if(MemberBaseInfo.USER_FACEBOOK.equalsIgnoreCase(SystemCache.bindInfo.platform))
					FacebookUtils.logout();
				d.dismiss();
			}
		});
		TextView tv_sure = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_sure"));
		tv_sure.setText(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_common_btn_sure")));
		tv_sure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				d.dismiss();
				if(type == 1)
					bindErrorHandler(2);
				else if(type == 2)
					switchUser();
			}
		});

	}
	
	/**
	 * 当前匿名账号如果是付费账号时，流程处理对话框
	 */
	private void bindErrorHandler9(){
		
		final AlertDialog d = new AlertDialog.Builder(this).create();
		d.show();
		d.setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_common_dialog_notitle"));

		TextView tv_content = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_content"));
		String content = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_bind_notice_error4"));
		tv_content.setText(content);
		
		TextView tv_cancle = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_cancle"));
		tv_cancle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				d.dismiss();
			}
		});
		TextView tv_sure = (TextView)d.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_dialog_notitle_sure"));
		tv_sure.setText(getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_button_registnewuser")));			
		tv_sure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				d.dismiss();
				view_bind1.setVisibility(View.GONE);
				view_bind2.setVisibility(View.VISIBLE);
				curBindStyle = 1;
				changeView();
			}
		});
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(curBindStyle == 2 || curBindStyle == 1){// 如果是oas界面，返回关联新账号界面
				curBindStyle = 0;
				changeView();
				return true;
			}/*else if(curBindStyle == 1){
				
				if(layout_guide1.isShown()){// 如果是关联新账号界面，并且引导以及展示，先关闭引导信息
					layout_guide1.setVisibility(View.GONE);
					return true;
				}
				if(layout_guide2.isShown()){// 如果是关联新账号界面，并且引导以及展示，先关闭引导信息
					layout_guide2.setVisibility(View.GONE);
					return true;
				}
			}*/
		}
		return super.onKeyDown(keyCode, event);
	}
	
	class MyGoogleLoginCallback implements GoogleUtils.GoogleLoginCallback{
		@Override
		public void success(Person p, String email, String token) {
			String personName = "";
			if(p != null)
				personName = p.getDisplayName();
//			System.out.println("========Name="+personName +"; email="+email+";  token="+token);
			bindUserToPlatform(MemberBaseInfo.USER_GOOGLE, email, token, personName);
			mSignInClicked = false;
		}

		@Override
		public void exception(Exception e) {
			
			if(e instanceof UserRecoverableAuthException){
				Log.e(TAG, "Google Exception:UserRecoverableAuthException ");
				e.printStackTrace();
				Intent intent = ((UserRecoverableAuthException)e).getIntent();
                startActivityForResult(intent, GoogleUtils.REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
//                mAuthException = true;
			}else if(e instanceof GoogleAuthException){
				Log.e(TAG, "Google Exception:GoogleAuthException ");
				e.printStackTrace();
				myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				mSignInClicked = false;
//				mAuthException = false;
			}else if(e instanceof IOException){
				Log.e(TAG, "Google Exception:IOException ");
				e.printStackTrace();
				myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
				mSignInClicked = false;
//				mAuthException = false;
			}
			
		}
	}
	/**
	 * Fetching user's information name, email, profile pic
	 * */
	private void getProfileInformation() {
		final MyGoogleLoginCallback callback = new MyGoogleLoginCallback();
	    	final Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
	    	final String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
	    	BaseUtils.logDebug(TAG, "email: " + email);
	    	if(!TextUtils.isEmpty(email)){
	    		new Thread(new Runnable() {
					
					@Override
					public void run() {
						String token;
						try {
							token = GoogleAuthUtil.getToken(OasisSdkBindActivity.this.getApplicationContext(), email, "oauth2:"+Scopes.PROFILE+" https://www.googleapis.com/auth/userinfo.profile");
							BaseUtils.logDebug(TAG, "token: " + token);
							
							callback.success(currentPerson, email, token);
						} catch(Exception e){
							callback.exception(e);
						}
						
					}
				}).start();
	    	}
	        if (currentPerson != null) {
	            
	            String personName = currentPerson.getDisplayName();
	            String personPhotoUrl = currentPerson.getImage().getUrl();
	            String personGooglePlusProfile = currentPerson.getUrl();
	 
	            Log.d(TAG, "Name: " + personName + ", plusProfile: "
	                    + personGooglePlusProfile + ", email: " + email
	                    + ", Image: " + personPhotoUrl);
	 
//	            txtName.setText(personName);
//	            txtEmail.setText(email);
	 
	            // by default the profile url gives 50x50 px image only
	            // we can replace the value with whatever dimension we want by
	            // replacing sz=X
//	            personPhotoUrl = personPhotoUrl.substring(0,
//	                    personPhotoUrl.length() - 2)
//	                    + PROFILE_PIC_SIZE;
//	 
//	            new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);
	 
	        } 
	}
	@Override
	protected void onResume() {
		super.onResume();
		long count = 0;
			count = (Long) BaseUtils.getSettingKVPfromSysCache("BINDGUIDECOUNT", 0L);
		if(count <= 0 && SystemCache.userInfo.loginType == 1)// 以前没有展示过，第一次显示
			myHandler.sendEmptyMessageDelayed(10, 500);
	}
	@Override
	protected void onStart() {
		super.onStart();
		/*注释此代码，是不想一进来就connect，希望是用户想connect时再执行
		 * if(!mAuthException)
		 * 	mGoogleApiClient.connect();
		*/	
	}

	@Override
	protected void onStop() {
		super.onStop();
		mGoogleApiClient.disconnect();
	}
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!mIntentInProgress) {
			if (mSignInClicked) {
			    if (result.hasResolution()) {
			      // The user has already clicked 'sign-in' so we attempt to resolve all
			      // errors until the user is signed in, or they cancel.
			      try {
			        result.startResolutionForResult(this, RC_SIGN_IN);
			        mIntentInProgress = true;
			      } catch (SendIntentException e) {
			        // The intent was canceled before it was sent.  Return to the default
			        // state and attempt to connect to get an updated ConnectionResult.
						mIntentInProgress = false;
//						mAuthException = false;
						mSignInClicked = false;
						setWaitScreen(false);
						myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
			      }
			    }else{
			    	mIntentInProgress = false;
//			    	mAuthException = false;
			    	mSignInClicked = false;
			    	setWaitScreen(false);
			    	myHandler.sendEmptyMessage(HANDLER_EXCEPTION);
			    }
			}
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		if(mSignInClicked){
			getProfileInformation();
		}
//		mSignInClicked = false;
//		BaseUtils.showMsg(this, "User is connected!");
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		mGoogleApiClient.connect();
	}
}
