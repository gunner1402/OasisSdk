package com.oasis.sdk.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.WebDialog;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.facebook.share.widget.ShareDialog.Mode;
import com.oasis.sdk.OASISPlatformConstant;
import com.oasis.sdk.activity.platform.FacebookUtils;
import com.oasis.sdk.base.report.ReportUtils;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.SystemCache;
/**
 * 分享
 * @author Administrator
 *
 */
public class OasisSdkShareActivity extends OasisSdkBaseActivity {
	private static final String TAG = "OasisSdkShareActivity";
	static final int SHAREPHOTO = 123456;
	private FacebookUtils fb;
	ShareDialog shareDialog;

	WebDialog feedDialog = null;
	MyHandler myHandler;
	int action = 0; 
	Bitmap bitmaps;
	String imagePath;
	String link="", pictrue="", name="", caption="", description="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_share"));
		
	    
	    myHandler = new MyHandler(this);
	    
	    fb = new FacebookUtils(this);
	    
	    Intent b = getIntent();
	    if(b != null){
	    	action = b.getIntExtra("action", 0);
	    	if(b.getExtras() != null && b.getExtras().get("bitmaps") != null)
	    		imagePath = (String)b.getExtras().get("bitmaps");
	    	link = b.getStringExtra("link");
	    	if(TextUtils.isEmpty(link))
	    		link = getString(BaseUtils.getResourceValue("string", "facebook_app_linkurl"));
	    	pictrue = b.getStringExtra("picture");
	    	name = b.getStringExtra("name");
	    	caption = b.getStringExtra("caption");
	    	description = b.getStringExtra("description");
	    }
//	    link = "https://developers.facebook.com/android";
//    	pictrue = "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png";
//    	name = "Facebook SDK for Android";
//    	caption = "Build great social apps and get more installs.";
//    	description = "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.";
	    
	    shareDialog = new ShareDialog(this);
        // this part is optional
	    shareDialog.registerCallback(FacebookUtils.getCallbackManager(), new FacebookCallback<Sharer.Result>() {

			@Override
			public void onSuccess(Sharer.Result result) {
				if(result == null){
					setResultInfo(OASISPlatformConstant.RESULT_CANCLE, "");
					close();
					return;
				}
				
				String postId =result.getPostId();
				BaseUtils.logDebug(TAG, "---------onSuccess()；postid="+postId);
//				if(postId != null && !TextUtils.isEmpty(postId)){
				setResultInfo(OASISPlatformConstant.RESULT_SUCCESS, postId);
				sendMdataInfo(postId);					
//				}else{
//					setResultInfo(OASISPlatformConstant.RESULT_CANCLE, "");
//				}
				close();
			}

			@Override
			public void onCancel() {
				BaseUtils.logDebug(TAG, "---------onCancel()");
				setResultInfo(OASISPlatformConstant.RESULT_CANCLE, "");
				close();
			}

			@Override
			public void onError(FacebookException error) {
				BaseUtils.logDebug(TAG, "----------"+error.getMessage());
				setResultInfo(OASISPlatformConstant.RESULT_FAIL, "");
				close();
			}
		});
//	    setWaitScreen(true);
	    myHandler.sendEmptyMessageDelayed(1, 300);
	}
	@Override
	protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
//	    if(data == null)
//	    if(resultCode == Activity.RESULT_CANCELED){
//	    	BaseUtils.showMsg(OasisSdkShareActivity.this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_share_cancled")));
//	    	close();
//	    	return;
//	    }
	    if(fb != null)
	    	fb.onActivityResult(requestCode, resultCode, data);
	    
//	    uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
//	        @Override
//	        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
//	            Log.e(TAG, String.format("Error: %s", error.toString()));
//
//	            BaseUtils.showMsg(OasisSdkShareActivity.this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_share_fail")));
//                close();
//	        }
//
//	        @Override
//	        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
//	        	
//	        	boolean didCancel = FacebookDialog.getNativeDialogDidComplete(data);
//	        	String completionGesture = FacebookDialog.getNativeDialogCompletionGesture(data);
//	        	String postId = FacebookDialog.getNativeDialogPostId(data);
//	        	
//	        	Log.i(TAG, "Share Success! didCancel="+didCancel+" /////// completionGesture="+completionGesture+" /////// post_id="+postId);
//	        	
//	            if(postId != null && !TextUtils.isEmpty(postId)){
//	            	BaseUtils.showMsg(OasisSdkShareActivity.this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_share_success")));
//		            setResultInfo(OASISPlatformConstant.RESULT_SUCCESS, postId);
//		            sendMdataInfo(postId);
//	            }
//	            
//	            if(requestCode == SHAREPHOTO && didCancel && "post".equals(completionGesture)){
////	            	BaseUtils.showMsg(OasisSdkShareActivity.this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_share_success")));
//		            setResultInfo(OASISPlatformConstant.RESULT_SUCCESS, "");
//		            close();
//	            	return;
//	            }
//	            
//	            if(didCancel && (TextUtils.isEmpty(completionGesture) || FacebookDialog.COMPLETION_GESTURE_CANCEL.equals(completionGesture))){
//	            	BaseUtils.showMsg(OasisSdkShareActivity.this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_share_cancled")));
//	            	setResultInfo(OASISPlatformConstant.RESULT_CANCLE, "");
//	            }
//	            close();
//	        }
//	    });
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
		
		if(bitmaps != null)
			bitmaps.recycle();
	    super.onDestroy();
	}
	
	public void share(){
		ShareLinkContent.Builder builder = new ShareLinkContent.Builder();
		builder.setContentTitle(name);
		builder.setContentDescription(TextUtils.isEmpty(description)?null:description);
		if(!TextUtils.isEmpty(link))
			builder.setContentUrl(Uri.parse(link));
		if(!TextUtils.isEmpty(pictrue))
			builder.setImageUrl(Uri.parse(pictrue));
		
		ShareLinkContent linkContent = builder.build();
		if (ShareDialog.canShow(ShareLinkContent.class)) {
		    shareDialog.show(linkContent, Mode.AUTOMATIC);
		}else{
			shareDialog.show(linkContent, Mode.FEED);
//			publishFeedDialog(session);
		}
	}
	
	private void check(){
		fb.setFacebookCallbackInterface(new FBLoginCallbackImpl(this));
//		if(fb.loginForPublishCheck(this))
//			switchActiion();
//		else
//			fb.loginForPublish(this);
		if(fb.loginCheck(this))
			switchActiion();
		else
			fb.login(this);
	}
	class FBLoginCallbackImpl implements FacebookUtils.FacebookCallbackInterface {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkShareActivity> mOuter;

		public FBLoginCallbackImpl(OasisSdkShareActivity activity) {
			mOuter = new WeakReference<OasisSdkShareActivity>(activity);
		}

		@Override
		public void onSuccess(final LoginResult loginResult) {
			mOuter.get().switchActiion();
		}

		@Override
		public void onCancel() {
			BaseUtils.logDebug(TAG, "============FB login onCancel()");
			mOuter.get().close();
		}

		@Override
		public void onError(FacebookException exception) {
			mOuter.get().close();
		}
	}
	
	private void switchActiion(){
		setWaitScreen(true);
		if(action == 1)
			uploadPhoto();
		else
			share();
	}
	
//	public void publishFeedDialog() {
//
//		//授权成功
//		Bundle params = new Bundle();
//	    params.putString("name", name);
//	    params.putString("caption", caption);
//	    params.putString("description", description);
//	    params.putString("link", link);
//	    params.putString("picture", pictrue);
//
//	    WebDialog feedDialog = (
//	        new WebDialog.FeedDialogBuilder(OasisSdkShareActivity.this,
//	        		session,   params))
//	        .setOnCompleteListener(new OnCompleteListener() {
//
//	            @Override
//	            public void onComplete(Bundle values,
//	                FacebookException error) {
//
//                    Log.i(TAG, "Share WebDialog complete!");
//	                if (error == null) {
//	                    // When the story is posted, echo the success
//	                    // and the post Id.
//	                    final String postId = values.getString("post_id");
//	                    Log.i(TAG, "Share cjomplete! WebDialog, post_id="+postId);
//	                    if (postId != null) {
//	                    	BaseUtils.showMsg(OasisSdkShareActivity.this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_share_success")));
//	                    	setResultInfo(OASISPlatformConstant.RESULT_SUCCESS, postId);
//	                    	sendMdataInfo(postId);
//	                    } else {
//	                        // User clicked the Cancel button
//	                    	BaseUtils.showMsg(OasisSdkShareActivity.this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_share_cancled")));
//	                    	setResultInfo(OASISPlatformConstant.RESULT_CANCLE, "");
//	                    }
//	                } else if (error instanceof FacebookOperationCanceledException) {
//	                    // User clicked the "x" button
//	                	BaseUtils.showMsg(OasisSdkShareActivity.this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_share_cancled")));
//	                	setResultInfo(OASISPlatformConstant.RESULT_CANCLE, "");
//	                } else {
//	                    // Generic, ex: network error
//	                	BaseUtils.showMsg(OasisSdkShareActivity.this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_share_fail")));
//	                	setResultInfo(OASISPlatformConstant.RESULT_FAIL, "");
//	                }
//
//	                close();
//	            }
//
//	        })
//	        .build();
//	    feedDialog.show();
//	    
//	}
	
	private void uploadPhoto(){
		if(imagePath == null){
			BaseUtils.logError(TAG, "Don't image , don't share");
			close();
			return ;
		}
//		imagePath = Environment.getExternalStorageDirectory().getPath()+"/DCIM/Camera/22222.png";
		bitmaps = BitmapFactory.decodeFile(imagePath);
//		bitmaps = BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_payway_mob_infobip"));
		SharePhoto photo = new SharePhoto.Builder().setBitmap(bitmaps).setUserGenerated(true).build();
		
		SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(photo).build();
		if (ShareDialog.canShow(SharePhotoContent.class)) {
			shareDialog.show(content);
//			shareDialog.show(content, Mode.NATIVE);
//			ShareApi.share(content, new FacebookCallback<Sharer.Result>() {
//
//				@Override
//				public void onSuccess(Sharer.Result result) {
//					String postId =result.getPostId();
//					if(postId != null && !TextUtils.isEmpty(postId)){
//						setResultInfo(OASISPlatformConstant.RESULT_SUCCESS, postId);
//						sendMdataInfo(postId);					
//					}else{
//						setResultInfo(OASISPlatformConstant.RESULT_CANCLE, "");
//					}
//					close();
//				}
//
//				@Override
//				public void onCancel() {
//					setResultInfo(OASISPlatformConstant.RESULT_CANCLE, "");
//					close();
//				}
//
//				@Override
//				public void onError(FacebookException error) {
//					BaseUtils.logDebug(TAG, error.getMessage());
//					setResultInfo(OASISPlatformConstant.RESULT_FAIL, "");
//					close();
//				}
//			});
		}else{
//			shareDialog.show(content, Mode.WEB);
			BaseUtils.logError(TAG, "没有安装FB，无法分享图片");
			close();
		}
		
//		if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
//				FacebookDialog.ShareDialogFeature.PHOTOS)) {
//			// Publish the post using the Photo Share Dialog
//			FacebookDialog.PhotoShareDialogBuilder builder = new FacebookDialog.PhotoShareDialogBuilder(this);
//			builder.addPhotos(Arrays.asList(bitmaps));
////			builder.setApplicationName("ApplicationName");
//			builder.setRequestCode(SHAREPHOTO);
//			FacebookDialog shareDialog = builder.build();
//			uiHelper.trackPendingDialogCall(shareDialog.present());
//		} else {
//			// The user doesn't have the Facebook for Android app installed.
//			// You may be able to use a fallback.
//
//			Request request = Request.newUploadPhotoRequest(s, bitmaps,
//					new Request.Callback() {
//						@Override
//						public void onCompleted(Response response) {
//
//							if (response.getError() != null) {
//								setResultInfo(
//										OASISPlatformConstant.RESULT_FAIL, "");
//
//							} else {
//								try {
//									String s = response.getRawResponse();
//									if(TextUtils.isEmpty(s)){
//										setResultInfo(
//												OASISPlatformConstant.RESULT_FAIL, "");
//									}else{
//										JSONObject o = new JSONObject(s);
//										setResultInfo(
//												OASISPlatformConstant.RESULT_SUCCESS, o.getString("post_id"));
//									}
//								} catch (JSONException e) {
//								}
////								====response:{"post_id":"100008088182468_1563151937297731","id":"1563192873960304"}
//
//							}
//							close();
//						}
//					});
//			request.executeAsync();
//
//		}	
		
	}
	
	
	public static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<OasisSdkShareActivity> mOuter;

		public MyHandler(OasisSdkShareActivity activity) {
			mOuter = new WeakReference<OasisSdkShareActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			OasisSdkShareActivity outer = mOuter.get();
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
	private void sendMdataInfo(String postId){
		try {
			List<String> parameters = new ArrayList<String>();
			parameters.add("\"share_channal\":\"facebook\"");
			ReportUtils.add(ReportUtils.DEFAULTEVENT_SHARE, parameters, null);
		} catch (Exception e) {
		}
	}
	
	private void setResultInfo(int resultCode, String id){
		if(SystemCache.oasisInterface != null){
			SystemCache.oasisInterface.fbRequestCallback(0, resultCode, id);
		}else
			BaseUtils.logError(TAG, "OASISPlatformInterface 未初始化，无法回调fbRequestCallback。");
	}
	/**
	 * 获取和保存当前屏幕的截图
	 */
	@SuppressWarnings({ "unused", "deprecation" })
	private void GetandSaveCurrentImage() {
		// 构建Bitmap
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		int w = display.getWidth();
		int h = display.getHeight();
		Bitmap Bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		// 获取屏幕
		View decorview = this.getWindow().getDecorView();
		decorview.setDrawingCacheEnabled(true);
		Bmp = decorview.getDrawingCache();
		// 图片存储路径
		String SavePath = getSDCardPath() + "/Demo/ScreenImages";
		// 保存Bitmap
		try {
			File path = new File(SavePath);
			// 文件
			String filepath = SavePath + "/Screen_1.png";
			File file = new File(filepath);
			if (!path.exists()) {
				path.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fos = null;
			fos = new FileOutputStream(file);
			if (null != fos) {
				Bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
				fos.flush();
				fos.close();
				Toast.makeText(this.getApplicationContext(), "截屏文件已保存至SDCard/ScreenImages/目录下",
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取SDCard的目录路径功能
	 * 
	 * @return
	 */
	private String getSDCardPath() {
		File sdcardDir = null;
		// 判断SDCard是否存在
		boolean sdcardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if (sdcardExist) {
			sdcardDir = Environment.getExternalStorageDirectory();
		}
		return sdcardDir.toString();
	}

	
}
