package com.oasis.sdk.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.oasis.sdk.OASISPlatformConstant;
import com.oasis.sdk.activity.platform.FacebookUtils;
import com.oasis.sdk.base.entity.FBPageInfo;
import com.oasis.sdk.base.entity.FriendInfo;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.SystemCache;
/**
 * 获取好友
 * @author Administrator
 *
 */
public class OasisSdkFBFriendsActivity extends OasisSdkBaseActivity {
	private static final String TAG = "OasisSdkFBFriendsActivity";
	private static final int LIMIT = 50;
	
	private FacebookUtils fb;
	
	MyHandler myHandler;
	int limit = 50;
	boolean type = true;
	int requestCode = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_share"));
		
	    myHandler = new MyHandler(this);
	    
	    fb = new FacebookUtils(this);
	    
	    Intent b = getIntent();
	    if(b != null){
	    	limit = b.getIntExtra("limit", LIMIT);
	    	type = b.getBooleanExtra("type", true);
	    	requestCode = b.getIntExtra("requestCode", 0);
	    }
//	    setWaitScreen(true);
	    myHandler.sendEmptyMessageDelayed(1, 300);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if(fb != null)
	    	fb.onActivityResult(requestCode, resultCode, data);
	}
	@Override
	protected void onResume() {
	    super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
	    super.onPause();
	}

	@Override
	public void onDestroy() {
		fb = null;
	    super.onDestroy();
	}
	
	public static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkFBFriendsActivity> mOuter;

		public MyHandler(OasisSdkFBFriendsActivity activity) {
			mOuter = new WeakReference<OasisSdkFBFriendsActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			OasisSdkFBFriendsActivity outer = mOuter.get();
			if (outer != null) {
				switch (msg.what) {
				case 1:
//					outer.getMyFrends();
//					outer.getInvitableFriends();
					outer.checkLogin();
					break;
				case 2:
					outer.close();
					break;
				default:
					
					break;
				}
			}
		}
	}
	private void close(){
		setWaitScreen(false);
		finish();
	}

	public void getInvitableFriends() {
		GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "me/invitable_friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
//                        GraphObject strTemp = response.getGraphObject();
//                        JSONObject j = strTemp.getInnerJSONObject();
//                      
                    	if(response.getError()!=null){
                    		System.out.println("response:"+response.getError().toString());
                    		setResultInfo(OASISPlatformConstant.RESULT_EXCEPTION, null);
                    		close();
                    		return;
                    	}
            			FBPageInfo info = new FBPageInfo();
        				info.setLimit(limit);
        				try {
        					JSONObject json = new JSONObject(response.getRawResponse());
        					if(json.has("paging")){	
        						JSONObject pageing = (JSONObject) json.get("paging");
        						if(pageing.has("next")){
        							info.setHasNext(true);
        							SystemCache.userInfo.setInvitableFriendsNext(((JSONObject)pageing.get("cursors")).getString("after"));
        						}else{
        							info.setHasNext(false);
        							SystemCache.userInfo.setInvitableFriendsNext("");
        						}
        						if(pageing.has("previous")){
        							info.setHasPrevious(true);
        							SystemCache.userInfo.setInvitableFriendsPrevious(((JSONObject)pageing.get("cursors")).getString("before"));
        						}else{
        							info.setHasPrevious(false);
        							SystemCache.userInfo.setInvitableFriendsPrevious("");
        						}
        					}
        					if(json.has("data")){	
        						JSONArray data = (JSONArray) json.get("data");
        						int length = data.length();
        						List<FriendInfo> list = new ArrayList<FriendInfo>();
        						for (int i = 0; i < length; i++) {
        							FriendInfo friend = new FriendInfo();
        							JSONObject subinfo = (JSONObject) data.get(i);
        							friend.setId(subinfo.getString("id"));
        							friend.setName(subinfo.getString("name"));
        							if(subinfo.has("picture")){
        								subinfo = (JSONObject) subinfo.getJSONObject("picture");

        								subinfo = (JSONObject) subinfo.getJSONObject("data");
        								if(subinfo.has("url"))
        									friend.setPicture(subinfo.getString("url"));
        							}
        							
        							list.add(friend);
        						}
        						info.setData(list);
        					}	
        					setResultInfo(OASISPlatformConstant.RESULT_SUCCESS, info);
        				} catch (JSONException e) {
        					e.printStackTrace();
        					setResultInfo(OASISPlatformConstant.RESULT_EXCEPTION, null);
        					close();
        					return;
        				}
        				close();
            			
                    }
                }
            );

		Bundle params = new Bundle();
		params.putString("fields", "id,name,picture");
		params.putInt("limit", limit);// 每次获取记录数
		
		if(SystemCache.userInfo == null || (null != SystemCache.userInfo && 
				(TextUtils.isEmpty(SystemCache.userInfo.invitableFriendsNext) && TextUtils.isEmpty(SystemCache.userInfo.invitableFriendsPrevious)))	){
		}else if(type){
			if(TextUtils.isEmpty(SystemCache.userInfo.invitableFriendsNext)){
				FBPageInfo info = new FBPageInfo();
				info.setData(new ArrayList<FriendInfo>());
				setResultInfo(OASISPlatformConstant.RESULT_SUCCESS, info);
				
				close();
				return ;
			}else{
				// 获取offset
				String after = SystemCache.userInfo.invitableFriendsNext;//URLRequest(SystemCache.userInfo.invitableFriendsNext, "after");
				params.putString("after", after);// 起始位置
				
			}
		}else if(!type){
			if(TextUtils.isEmpty(SystemCache.userInfo.invitableFriendsPrevious)){
				FBPageInfo info = new FBPageInfo();
				info.setData(new ArrayList<FriendInfo>());
				setResultInfo(OASISPlatformConstant.RESULT_SUCCESS, info);
				
				close();
				return ;
			}else{
				// 获取offset
				String before = SystemCache.userInfo.invitableFriendsPrevious;//URLRequest(SystemCache.userInfo.invitableFriendsPrevious, "before");
				params.putString("before", before);// 起始位置
			}
		}
		
		request.setParameters(params);
		request.executeAsync();
	}

	class FBLoginCallbackImpl implements FacebookUtils.FacebookCallbackInterface {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkFBFriendsActivity> mOuter;

		public FBLoginCallbackImpl(OasisSdkFBFriendsActivity activity) {
			mOuter = new WeakReference<OasisSdkFBFriendsActivity>(activity);
		}

		@Override
		public void onSuccess(final LoginResult loginResult) {
			mOuter.get().switchAction();
		}

		@Override
		public void onCancel() {
			BaseUtils.logDebug(TAG, "============FB login onCancel()");
			close();
		}

		@Override
		public void onError(FacebookException exception) {
			close();
		}
	}
	public void checkLogin(){
		fb.setFacebookCallbackInterface(new FBLoginCallbackImpl(this));
		if(fb.loginCheck(this))
			switchAction();
		else
			fb.login(this);
	}
	private void switchAction(){
		setWaitScreen(true);
		if(requestCode == OASISPlatformConstant.REQUEST_CODE_FACEBOOK_GETFRIENDS)
			getFriends();
		else if(requestCode == OASISPlatformConstant.REQUEST_CODE_FACEBOOK_GETINVITABLEFRIENDS)
			getInvitableFriends();
	}
	
	private void getFriends(){
		
		GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), "me/friends", null, HttpMethod.GET, new GraphRequest.Callback() {
			
			@Override
			public void onCompleted(GraphResponse response) {
				if(null != response.getError()){// 错误时，通知游戏失败
					BaseUtils.logDebug(TAG, response.getError().getErrorMessage());
					setResultInfo(OASISPlatformConstant.RESULT_FAIL, null);
					close();
					return;
				}
				
				FBPageInfo info = new FBPageInfo();
				info.setLimit(limit);
				try {
					JSONObject json = new JSONObject(response.getRawResponse());
//						JSONObject tcount = (JSONObject) json.get("summary");
//						info.setTotal_count(tcount.getInt("total_count"));
					if(json.has("paging")){
						JSONObject pageing = (JSONObject) json.get("paging");
						if(pageing.has("next")){
							info.setHasNext(true);
							SystemCache.userInfo.setFriendsNext(pageing.getString("next"));
						}else{
							info.setHasNext(false);
							SystemCache.userInfo.setFriendsNext("");
						}
						if(pageing.has("previous")){
							info.setHasPrevious(true);
							SystemCache.userInfo.setFriendsPrevious(pageing.getString("previous"));
						}else{
							info.setHasPrevious(false);
							SystemCache.userInfo.setFriendsPrevious("");
						}
					}
					if(json.has("data")){	
						JSONArray data = (JSONArray) json.get("data");
						int length = data.length();
						List<FriendInfo> list = new ArrayList<FriendInfo>();
						for (int i = 0; i < length; i++) {
							FriendInfo friend = new FriendInfo();
							JSONObject subinfo = (JSONObject) data.get(i);
							friend.setId(subinfo.getString("id"));
							friend.setName(subinfo.getString("name"));
							if(subinfo.has("picture")){
								subinfo = (JSONObject) subinfo.getJSONObject("picture");

								subinfo = (JSONObject) subinfo.getJSONObject("data");
								if(subinfo.has("url"))
									friend.setPicture(subinfo.getString("url"));
							}
							
							list.add(friend);
						}
						info.setData(list);
					}
					setResultInfo(OASISPlatformConstant.RESULT_SUCCESS, info);
				} catch (JSONException e) {
					e.printStackTrace();
					setResultInfo(OASISPlatformConstant.RESULT_EXCEPTION, null);
					close();
					return;
				}
				close();
				
			}

		});
		Bundle params = new Bundle();
		params.putString("fields", "id,name,picture");
		params.putInt("limit", limit);// 每次获取记录数
		
		if(null != SystemCache.userInfo && 
				(TextUtils.isEmpty(SystemCache.userInfo.friendsNext) && TextUtils.isEmpty(SystemCache.userInfo.friendsPrevious))	){
			params.putInt("offset", 0);// 起始位置
		}else if(type){
			if(TextUtils.isEmpty(SystemCache.userInfo.friendsNext)){
				FBPageInfo info = new FBPageInfo();
				info.setData(new ArrayList<FriendInfo>());
				setResultInfo(OASISPlatformConstant.RESULT_SUCCESS, info);
				close();
				return ;
			}else{
				// 获取offset
				String offset = URLRequest(SystemCache.userInfo.friendsNext, "offset")+"=";
				try {
					params.putInt("offset", Integer.parseInt(offset));// 起始位置
				} catch (NumberFormatException e) {
					params.putInt("offset", 0);// 起始位置
				}
			}
		}else if(!type){
			if(TextUtils.isEmpty(SystemCache.userInfo.friendsPrevious)){
				FBPageInfo info = new FBPageInfo();
				info.setData(new ArrayList<FriendInfo>());
				setResultInfo(OASISPlatformConstant.RESULT_SUCCESS, info);
				close();
				return ;
			}else{
				// 获取offset
				String offset = URLRequest(SystemCache.userInfo.friendsPrevious, "offset")+"=";
				try {
					params.putInt("offset", Integer.parseInt(offset));// 起始位置
				} catch (NumberFormatException e) {
					params.putInt("offset", 0);// 起始位置
				}
			}
		}
		
		request.setParameters(params);
		request.executeAsync();
	}
	
	/**
     * 解析出url参数中的键值对
     * 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     * @param URL  url地址
     * @param tag  参数名称
     * @return  url请求参数部分
     */
	public String URLRequest(String URL, String tag) {

		String res = "";
		String[] arrSplit = null;

		String strUrlParam = TruncateUrlPage(URL);
		if (strUrlParam == null) {
			return res;
		}
		// 每个键值为一组 
		arrSplit = strUrlParam.split("[&]");
		
		
		for (String strSplit : arrSplit) {
			String[] arrSplitEqual = null;
			arrSplitEqual = strSplit.split("[=]");

			if(tag.equalsIgnoreCase(arrSplitEqual[0])){
				// 解析出键值
				if (arrSplitEqual.length > 1) {
					// 获取值
					res = arrSplitEqual[1];

				} else {
					if (arrSplitEqual[0] != "") {
						// 只有参数没有值，默认为空
						res = "";
					}
				}
				break;
			}
			
		}
		return res;
	}
    /**
     * 去掉url中的路径，留下请求参数部分
     * @param strURL url地址
     * @return url请求参数部分
     */
	private String TruncateUrlPage(String strURL) {
		String strAllParam = null;
		String[] arrSplit = null;

		strURL = strURL.trim().toLowerCase();

		arrSplit = strURL.split("[?]");
		if (strURL.length() > 1) {
			if (arrSplit.length > 1) {
				if (arrSplit[1] != null) {
					strAllParam = arrSplit[1];
				}
			}
		}
		return strAllParam;
	}
	
	private void setResultInfo(int resultCode, FBPageInfo info){
		if(SystemCache.oasisInterface != null){
			SystemCache.oasisInterface.fbFriendsListCallback(requestCode, resultCode, info);
						
		}else
			Log.e(TAG, "OASISPlatformInterface 未初始化，无法回调fbFriendsListCallback。");
	}
}
