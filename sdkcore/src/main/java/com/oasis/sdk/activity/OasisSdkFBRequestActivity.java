package com.oasis.sdk.activity;

import java.lang.ref.WeakReference;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.share.model.GameRequestContent;
import com.facebook.share.model.GameRequestContent.ActionType;
import com.facebook.share.widget.GameRequestDialog;
import com.oasis.sdk.OASISPlatformConstant;
import com.oasis.sdk.activity.platform.FacebookUtils;
import com.oasis.sdk.base.Exception.OasisSdkException;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.SystemCache;
/**
 * 分享
 * @author Administrator
 *
 */
public class OasisSdkFBRequestActivity extends OasisSdkBaseActivity {
	private static final String TAG = "OasisSdkFBRequestActivity";
	
	private FacebookUtils fb;
	GameRequestDialog requestDialog;
	
	MyHandler myHandler;
	int actionType = 0; 
	String objectID="", uids="", message="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_share"));
		
	    myHandler = new MyHandler(this);
	    
	    fb = new FacebookUtils(this);
	    
	    Intent b = getIntent();
	    if(b != null){
	    	actionType = b.getIntExtra("actionType", 0);
	    	objectID = b.getStringExtra("objectID");
	    	uids = b.getStringExtra("uids");
	    	message = b.getStringExtra("message");
	    }
	    requestDialog = new GameRequestDialog(this);
        requestDialog.registerCallback(FacebookUtils.getCallbackManager(), new FacebookCallback<GameRequestDialog.Result>() {
            public void onSuccess(GameRequestDialog.Result result) {
                String requestId = result.getRequestId();
                if (requestId != null) {
                	saveRequestInfo(requestId);
                	return;
                } else {
                	setResultInfo(OASISPlatformConstant.RESULT_CANCLE, "");
                }
                close();
            }

            public void onCancel() {
            	setResultInfo(OASISPlatformConstant.RESULT_CANCLE, "");
            	close();
            }

            public void onError(FacebookException error) {
            	BaseUtils.logDebug(TAG, error.toString());
            	setResultInfo(OASISPlatformConstant.RESULT_FAIL, "");
            	close();
            }
        });
//        setWaitScreen(true);
//	    saveRequestInfo("1410279699278863");
	    myHandler.sendEmptyMessageDelayed(1, 300);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if(data == null)
	    Log.i(TAG, "onActivityResult is coming!  requestCode="+requestCode+"   resultCode = "+resultCode);
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
	class FacebookCallbackImpl implements FacebookUtils.FacebookCallbackInterface {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkFBRequestActivity> mOuter;

		public FacebookCallbackImpl(OasisSdkFBRequestActivity activity) {
			mOuter = new WeakReference<OasisSdkFBRequestActivity>(activity);
		}

		@Override
		public void onSuccess(final LoginResult loginResult) {
			mOuter.get().sendRequest();
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
	private void check(){
		fb.setFacebookCallbackInterface(new FacebookCallbackImpl(this));
		if(fb.loginCheck(this))
			sendRequest();
		else
			fb.login(this);
	}

	/**
	 * 发送请求
	 */
	private void sendRequest(){
		setWaitScreen(true);
	    GameRequestContent.Builder builder = new GameRequestContent.Builder();
	    builder.setMessage(message);
	    builder.setTo(uids);
	    if(actionType == 2){
	    	builder.setActionType(ActionType.SEND);
	    	builder.setObjectId(objectID);
	    }else if(actionType == 3){
	    	builder.setActionType(ActionType.ASKFOR);
	    	builder.setObjectId(objectID);
	    }
	    
	    GameRequestContent content = builder.build();
	    
	    requestDialog.show(content);
	}
	
	private void saveRequestInfo(final String requestId){
		setResultInfo(OASISPlatformConstant.RESULT_SUCCESS, requestId);
		myHandler.sendEmptyMessage(2);
		
		if(BaseUtils.isSandBox()){
			if (actionType == 2 || actionType == 3) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							HttpService.instance().setFbRequest(requestId,
									uids, objectID, actionType);
						} catch (OasisSdkException e) {
						}

					}
				}).start();
			}
		}
		
	}
	public static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkFBRequestActivity> mOuter;

		public MyHandler(OasisSdkFBRequestActivity activity) {
			mOuter = new WeakReference<OasisSdkFBRequestActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			OasisSdkFBRequestActivity outer = mOuter.get();
			if (outer != null) {
				switch (msg.what) {
				case 1:
					outer.check();
					
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
	
	private void setResultInfo(int resultCode, String id){
		if(SystemCache.oasisInterface != null){
			SystemCache.oasisInterface.fbRequestCallback(actionType, resultCode, id);
						
		}else
			Log.e(TAG, "OASISPlatformInterface 未初始化，无法回调fbRequestCallback。");
	}
}
