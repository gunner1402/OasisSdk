package com.oasis.sdk.activity.platform;

import android.app.Activity;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class GoogleUtils {

	public static final String TAG = "TRACK_GoogleUtils";
	public static final int REQUEST_CODE_RESOLVE_ERR = 456456;
	public static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 400000;
	private Activity c;
	private Activity useActivity;
	private String trackID ;
	private static GoogleUtils gutils;
	GoogleLoginCallback callback;
	
//	private PlusClient mPlusClient;
	public GoogleApiClient mGoogleApiClient;
	public ConnectionResult mConnectionResult;
	
	private GoogleUtils(Activity c){
		this.c = c;
//		try {
//			trackID = c.getResources().getString(
//					BaseUtils.getResourceValue("string", "ga_trackingId"));
//		} catch (NotFoundException e) {
//			e.printStackTrace();
//			Log.e(TAG,
//					"Please setup ga_trackingId in trackinfo.xml");
//		}
//		if(!TextUtils.isEmpty(trackID))
//			BaseUtils.logDebug(TAG, "Track:Google is running..... ga_trackingId="+trackID);
//		
		
	}
	public static GoogleUtils instance(Activity c) {
		if(gutils == null)
			gutils = new GoogleUtils(c);
		return gutils;
	}
	public void onStart(){
//		if(!TextUtils.isEmpty(trackID)){
//			EasyTracker.getInstance(c.getApplicationContext()).activityStart(c);
//			BaseUtils.logDebug(TAG, "GoogleUtils is onStart.");
//		}
	}
	public void onStop(){
//		if(!TextUtils.isEmpty(trackID)){ 
//			EasyTracker.getInstance(c.getApplicationContext()).activityStop(c); 
//			BaseUtils.logDebug(TAG, "GoogleUtils is onStop.");
//		}
		
		if(mGoogleApiClient != null)
			mGoogleApiClient.disconnect();
	}
	
	public void login(Activity c, GoogleLoginCallback callback){
		this.useActivity = c;
		this.callback = callback;
//		mPlusClient = new PlusClient.Builder(c, new MyConnectionCallbacks(), new MyOnConnectionFailedListener())
//		.setScopes(
////				"http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity",
//				"https://www.googleapis.com/auth/userinfo.profile","https://www.googleapis.com/auth/plus.login")
//		.build();
//		mGoogleApiClient = new GoogleApiClient.Builder(c)
//        .addApi(Plus.API, Plus.PlusOptions.builder()
//                .addActivityTypes(MomentUtil.ACTIONS).build())
//        .addScope(Plus.SCOPE_PLUS_LOGIN)
//        .addConnectionCallbacks(new MyConnectionCallbacks())
//        .addOnConnectionFailedListener(new MyOnConnectionFailedListener())
//        .build();
//		mGoogleApiClient.connect();
		mGoogleApiClient = new GoogleApiClient.Builder(this.useActivity)
        .addConnectionCallbacks(new MyConnectionCallbacks())
        .addOnConnectionFailedListener(new MyOnConnectionFailedListener())
        .addApi(Plus.API)
        .addScope(Plus.SCOPE_PLUS_LOGIN)
//        .addScope(new Scope("profile"))
        .useDefaultAccount()
        .build();
		
		mGoogleApiClient.connect();
	}
	
	class MyConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks{

		@Override
		public void onConnected(Bundle arg0) {
			
			if(mGoogleApiClient != null){
				System.out.println("GoogleUtils:"+mGoogleApiClient.toString());
//				System.out.println("GoogleUtils:"+mPlusClient.);
//				System.out.println("GoogleUtils:"+mPlusClient.getAccountName()+"   "+mPlusClient.getCurrentPerson().getId());
				getProfileInformation();
			}
		}

		@Override
		public void onConnectionSuspended(int arg0) {
			mGoogleApiClient.connect();
		}
		
	}
	class MyOnConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {

		@Override
		public void onConnectionFailed(ConnectionResult result) {
//			if(ConnectionResult.SIGN_IN_REQUIRED == result.getErrorCode()){
//				mGoogleApiClient.reconnect();
//				return;
//			}
			if (result.hasResolution()) {
	            try {
	                result.startResolutionForResult(useActivity, REQUEST_CODE_RESOLVE_ERR);
	            } catch (SendIntentException e) {
	                mGoogleApiClient.connect();
	            }
	        }
	        // 在用户点击时保存结果并解决连接故障。
	        mConnectionResult = result;
			System.out.println("GoogleUtils:onConnectionFailed"+result.toString());
		}
		
	}
	
	/**
	 * Fetching user's information name, email, profile pic
	 * */
	private void getProfileInformation() {
	    	final Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
	    	final String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
	    	Log.e(TAG, "email: " + email);
	    	if(!TextUtils.isEmpty(email)){
	    		new Thread(new Runnable() {
					
					@Override
					public void run() {
						String token;
						try {
							token = GoogleAuthUtil.getToken(c, email, "oauth2:"+Scopes.PROFILE+" https://www.googleapis.com/auth/userinfo.profile");
							Log.e(TAG, "token: " + token);
							
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
	public interface GoogleLoginCallback{
		public void success(Person p, String email, String token);
		public void exception(Exception e);
	}
//	/**
//	 * Background Async task to load user profile picture from url
//	 * */
//	private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
//	    ImageView bmImage;
//	 
//	    public LoadProfileImage(ImageView bmImage) {
//	        this.bmImage = bmImage;
//	    }
//	 
//	    protected Bitmap doInBackground(String... urls) {
//	        String urldisplay = urls[0];
//	        Bitmap mIcon11 = null;
//	        try {
//	            InputStream in = new java.net.URL(urldisplay).openStream();
//	            mIcon11 = BitmapFactory.decodeStream(in);
//	        } catch (Exception e) {
//	            Log.e("Error", e.getMessage());
//	            e.printStackTrace();
//	        }
//	        return mIcon11;
//	    }
//	 
//	    protected void onPostExecute(Bitmap result) {
//	        bmImage.setImageBitmap(result);
//	    }
//	}

	public void clear(){
//		try {
//			GoogleAuthUtil.clearToken(useActivity, token);
//		} catch (GooglePlayServicesAvailabilityException e) {
//			Log.e(TAG, e.getMessage());
//		} catch (GoogleAuthException e) {
//			Log.e(TAG, e.getMessage());
//		} catch (IOException e) {
//			Log.e(TAG, e.getMessage());
//		}
		if(mGoogleApiClient != null){
			if (mGoogleApiClient.isConnected()) {

				mGoogleApiClient.clearDefaultAccountAndReconnect().setResultCallback(new ResultCallback<Status>() {
					
					@Override
					public void onResult(Status arg0) {
						Log.e(TAG, "GoogleUtils "+arg0.toString());
						if(arg0.isSuccess()){
							Log.e(TAG, "GoogleUtils clearDefaultAccountAndReconnect.......");
						}
					}
				});
//				Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
//					.setResultCallback(new ResultCallback<Status>() {
//						
//						@Override
//						public void onResult(Status arg0) {
//							Log.e(TAG, "GoogleUtils "+arg0.toString());
//							if(arg0.isSuccess()){
//								Log.e(TAG, "GoogleUtils revode Access and Disconnect.......");
//							}
//						}
//					});
//				Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
//				Log.e(TAG, "GoogleUtils method clear()........");
            }
		}
	}
}
