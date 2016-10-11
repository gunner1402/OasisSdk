package com.oasis.sdk.activity.platform;

import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

public class FacebookUtils {
	private static CallbackManager callbackManager;
	
	static FacebookCallbackInterface iterface;
	
//	public static void onCreate(Activity c){
//		FacebookSdk.sdkInitialize(c.getApplicationContext());
//		AppEventsLogger.activateApp(c.getApplicationContext());
//	}
//	public static void onPause(Activity c){
//		AppEventsLogger.deactivateApp(c.getApplicationContext());
//	}
	

	/**
	 * @return 返回逻辑的实例.
	 */
	public FacebookUtils (Activity c) {
//			try {
//				FacebookEntity fe = FacebookEntity.getInfoByGameCode(c);
//				if(fe != null){
//					
//					BaseUtils.logDebug(TAG, "Track:Facebook is running..... AppId="+fe.appID);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				BaseUtils.logError(TAG, "Facebook ads track fail.");
//			}
			if(!FacebookSdk.isInitialized())// 如果没有初始化，执行初始化操作
				FacebookSdk.sdkInitialize(c.getApplicationContext());
	        callbackManager = CallbackManager.Factory.create();
	        LoginManager.getInstance().registerCallback(callbackManager,
	                new FacebookCallback<LoginResult>() {
	                    @Override
	                    public void onSuccess(final LoginResult loginResult) {
	                        // App code
	                    	iterface.onSuccess(loginResult);
	                    	
	                    }

	                    @Override
	                    public void onCancel() {
	                         // App code
	                    	iterface.onCancel();
	                    }

	                    @Override
	                    public void onError(FacebookException exception) {
	                         // App code   
	                    	iterface.onError(exception);
	                    }
	        });
	}
	public static CallbackManager getCallbackManager(){
		return callbackManager;
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	 public void setFacebookCallbackInterface(FacebookCallbackInterface impl){
		 iterface = impl;
	 }
	 
    public static void logout(){
    	LoginManager.getInstance().logOut();
    }
   	
    /**
     * start facebook login
     * @param statusCallback
     */
    public void login(Activity c){
    	login(c, 1);
    }
    public boolean loginCheck(Activity c){
    	if(AccessToken.getCurrentAccessToken() == null || AccessToken.getCurrentAccessToken().isExpired())
    		return false;
    	if(AccessToken.getCurrentAccessToken().getPermissions() == null || 
    			(AccessToken.getCurrentAccessToken().getPermissions() != null && !AccessToken.getCurrentAccessToken().getPermissions().containsAll(Arrays.asList("public_profile", "user_friends"))))
    		return false;
    	return true;
    }
    public void loginForPublish(Activity c){
    	login(c, 2);
    }
    public boolean loginForPublishCheck(Activity c){
    	if(AccessToken.getCurrentAccessToken() == null || AccessToken.getCurrentAccessToken().isExpired())
    		return false;
    	if(AccessToken.getCurrentAccessToken().getPermissions() == null || 
    			(AccessToken.getCurrentAccessToken().getPermissions() != null && !AccessToken.getCurrentAccessToken().getPermissions().containsAll(Arrays.asList("publish_actions"))))
    		return false;
    	return true;
    }
    
    private void login(Activity c, int type) {
    	if(type == 1){
    		LoginManager.getInstance().logInWithReadPermissions(c, Arrays.asList("public_profile", "user_friends"));
    	}
    	else if(type == 2)
    		LoginManager.getInstance().logInWithPublishPermissions(c, Arrays.asList("publish_actions"));
    }	
	
    
    public interface FacebookCallbackInterface{
    	public void onSuccess(LoginResult loginResult);
    	public void onCancel();
    	public void onError(FacebookException exception);
    }
}
