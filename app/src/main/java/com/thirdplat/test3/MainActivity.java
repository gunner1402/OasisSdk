package com.thirdplat.test3;

import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.oasis.sdk.OASISPlatform;
import com.oasis.sdk.OASISPlatformConstant;
import com.oasis.sdk.OASISPlatformConstant.Language;
import com.oasis.sdk.OASISPlatformInterface;
import com.oasis.sdk.OasisCallback;
import com.oasis.sdk.base.entity.FBPageInfo;
import com.oasis.sdk.base.entity.FriendInfo;
import com.oasis.sdk.base.entity.UserInfo;

public class MainActivity extends Activity {
	// 声明1个Handler对象
	public MyHandler myHandler = null;
	public MainActivity activity;
	String uri;
	long time ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);
		
		activity = this;
		myHandler = new MyHandler(activity);
		time = System.currentTimeMillis();
		//初始化
		OASISPlatform.init(this);
		
		//设置SDK语言，游戏切换语言时调用
		OASISPlatform.setLanguage(this, Language.DE);
		
		//实例化接口，为登录成功后的操作作准备
		OASISPlatform.setOASISPlatformInterfaceImpl(new OasisInterfaceImpl());
		
		//跟踪
		OASISPlatform.trackOnCreate(this);
		
		// 调用登录页面--自动登录
		OASISPlatform.login(this, -1);

		getFBKeyHash();

		//跟踪事件1
//		HashMap<String, String> map_value = new HashMap<String, String>();
//		map_value.put("key1", "value1");
//		OASISPlatform.trackEvent(this, OASISPlatformConstant.REPORT_TYPE_ADJUST, "LV5", map_value, null); // 当map_value不为空时，需要OASIS市场人员在Adjust后台配置相应的回调地址，如果没有配，请直接采用跟踪事件2.
		
		//跟踪事件2
		OASISPlatform.trackEvent(this, OASISPlatformConstant.REPORT_TYPE_ALL, "LV3", null, null);
//		OASISPlatform.trackRevenue(this, "", 9.99, "RMB", null);

		if(getIntent() != null){
			uri = getIntent().getDataString();
			Log.e("MainActivity", "警告：来源于FB的地址："+getIntent().getDataString());// 当使用Facebook发送请求、索要礼物等情况时，需要处理从facebook跳转过来的URI，
//		https://m.facebook.com/appcenter/heroes_pt?fbs=1001&request_ids=1410279699278863&ref=notif&app_request_type=user_to_user&content=send%3A632964910166909
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		OASISPlatform.trackOnResume(this);
	}
	@Override
	protected void onStart() {
		super.onStart();
		OASISPlatform.trackOnStart(this);
	}
	@Override
	protected void onRestart() {
		super.onRestart();
		OASISPlatform.trackOnRestart(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		OASISPlatform.trackOnPause(this);
	}
	@Override
	protected void onStop() {
		OASISPlatform.trackOnStop(this);
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		OASISPlatform.trackOnDestroy(this);
		//清除数据
		OASISPlatform.destroy(this);
		super.onDestroy();
	}
	
	public void  onBtnClick(View v){
		switch (v.getId()) {
		case R.id.sdk_pay:
			// 调用Google支付页面
			String productID = "oas_mtester_300_7174";// testpay_m_product_1 testpay_nm_product_1 oas_ahbr_300  oas_ahbr_1500
			OASISPlatform.toGoogleBillPayPage(this, OASISPlatformConstant.REQUEST_CODE_GOOGLEPAY, productID, 0.99, "");
			break;
		case R.id.sdk_login:
			// 调用登录页面--自动登录
			OASISPlatform.login(this, -1);
			break;
		case R.id.sdk_switchuser:
			// 切换用户
			OASISPlatform.switchUser(this);
			break;
		case R.id.sdk_logout:
			// 清除当前游戏信息（服ID、角色ID等
			OASISPlatform.cleanGameInfo(this);
			findViewById(R.id.sdk_logout).setVisibility(View.GONE);
			findViewById(R.id.sdk_setuserinfo).setVisibility(View.VISIBLE);
			break;
		case R.id.sdk_setuserinfo:
			// 注销用户后，重新选服或选角色时 设置当前用户信息
			OASISPlatform.setUserInfo("102001", "Server1", "", "usernametest", "102178196");
			findViewById(R.id.sdk_logout).setVisibility(View.VISIBLE);
			findViewById(R.id.sdk_setuserinfo).setVisibility(View.GONE);
			break;
		case R.id.sdk_showmenu:
			// 显示ＯＧ助手
			showOGMenu(true);
			findViewById(R.id.sdk_showmenu).setVisibility(View.GONE);
			findViewById(R.id.sdk_hidemenu).setVisibility(View.VISIBLE);
			break;
		case R.id.sdk_hidemenu:
			// 隐藏ＯＧ助手
			showOGMenu(false);
			findViewById(R.id.sdk_showmenu).setVisibility(View.VISIBLE);
			findViewById(R.id.sdk_hidemenu).setVisibility(View.GONE);
			break;
		case R.id.sdk_getFriends:
			OASISPlatform.getFriends(this, 10, true);
			break;
		case R.id.sdk_getInviteFriends:
			OASISPlatform.getInvitableFriends(this, 5, true);
			break;
		case R.id.sdk_fb_request_Invite:
			OASISPlatform.setAppRequest(activity, 1, "", null, "测试邀请好友");
			break;
		case R.id.sdk_fb_request_send:
			OASISPlatform.setAppRequest(activity, 2, "632964910166909", null, "向好友赠送体力");// 632964910166909    430168697148051
			break;
		case R.id.sdk_fb_request_askfor:
			OASISPlatform.setAppRequest(activity, 3, "430168697148051", null, "向好友索要体力");
			break;
		case R.id.sdk_shareImg:
			String path = Environment.getExternalStorageDirectory().getPath()+"/DCIM/Camera/IMG_20160308_185628.jpg";//"/DCIM/Screenshots/11111.png";//
			OASISPlatform.uploadImage(this, path);//"/data/data/com.oasgames.android.mhres/files/tmp/share_image_fb.jpg"
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		String[] contry = new String[]{"英语","德语","希腊语","西班牙语","法语","意大利语",
				"韩语","荷兰语","波兰语","葡萄牙语","俄语","瑞典语","土耳其语","中文简体"};
		
		for (int i = 0; i < contry.length; i++) {
			menu.add(0, i, i+1, contry[i]);
		}
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Language[] languages = new Language[]{Language.EN,	Language.DE, Language.EL, Language.ES, Language.FR, Language.IT, 
				Language.KO, Language.NL, Language.PL, Language.PT, Language.RU, Language.SV, Language.TR, Language.ZH};
		OASISPlatform.setLanguage(getApplicationContext(), languages[item.getItemId()]);
		return super.onOptionsItemSelected(item);
	}
	public static class MyHandler extends Handler {

		// WeakReference to the outer class's instance.
		private WeakReference<MainActivity> mOuter;

		public MyHandler(MainActivity activity) {
			mOuter = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			MainActivity outer = mOuter.get();
			switch (msg.what) {
			case 1000:

				outer.showOGMenu(true);
				break;
			case 100:
				Toast.makeText(outer, "FB请求操作成功", Toast.LENGTH_LONG).show();
			default:
				break;
			}
		}
		
	}

	public void showOGMenu(boolean showFlag){
		// 显示 OG
		OASISPlatform.showMenu(activity, 1, showFlag);
		
		// 处理来自于FB的跳转
		if(uri != null && !TextUtils.isEmpty(uri)){//
//			uri = "https://m.facebook.com/appcenter/heroes_pt?fbs=1001&request_ids=1410279699278863&ref=notif&app_request_type=user_to_user&content=send%3A632964910166909";
			Uri u = Uri.parse(uri);
			String requestIDs = u.getQueryParameter("request_ids");
			if(TextUtils.isEmpty(requestIDs))
				return;
			
			// 以下内容为测试内容，请接入方自行实现
			activity.test(requestIDs);
		}
	}
	class OasisInterfaceImpl implements OASISPlatformInterface{

		@Override
		public void reloadGame(UserInfo userInfo) {
			if(userInfo == null){
				Toast.makeText(MainActivity.this, "登录失败！用户取消操作", Toast.LENGTH_LONG).show();
				return;
			}

			findViewById(R.id.sdk_login).setVisibility(View.INVISIBLE);
			findViewById(R.id.sdk_ortherbtn).setVisibility(View.VISIBLE);
			
			
			// 选服或选角色后，同步设置用户信息
			OASISPlatform.setUserInfo("102001", "Ölüm Perisi Sulağı", "all", "usernametest", "102178196");
			
			((TextView)findViewById(R.id.sdk_notice)).setText(
					"当前用户是通过 "+("1".equals(userInfo.getOperation())?"用户登录":"切换账号")+" 完成本次登录, 耗时："+(System.currentTimeMillis()-time)+"ms"
					+"\n上次登录账户uid:"+userInfo.uidOld+"\n当前登录账户uid:"+userInfo.uid
					+"\n"+"当前账户类型:"+(userInfo.type==OASISPlatformConstant.USER_TYPE_OFFLINE?"本地账号":userInfo.type==1?"匿名账号":"正式账号"));
//					+"\nReferrer:"+PreferenceManager.getDefaultSharedPreferences(MainActivity.this.getApplicationContext()).getString("referrer", "OASSDK")+"  "+(SystemCache.fbInfo!=null?SystemCache.fbInfo.getId():""));

			myHandler.sendEmptyMessage(1000);
		}
		
		@Override
		public void paymentCallback(String paymentWay, int paymentCode, String errorMessage) {
			
			switch (paymentCode) {
			case OASISPlatformConstant.RESULT_SUCCESS:
				showNotice("游戏提示：充值成功");
				break;
			case OASISPlatformConstant.RESULT_CANCLE:
			case OASISPlatformConstant.RESULT_FAIL:
			case OASISPlatformConstant.RESULT_EXCEPTION:
			case OASISPlatformConstant.RESULT_EXCEPTION_GOOGLEPAY_EXCEPTION:
				showNotice("游戏提示："+errorMessage);
			default:
				break;
			}
		}
		
		@Override
		public void fbRequestCallback(int requestAction, int resultCode, String fbRequestId) {
			Log.d("MainActvity", "动作："+requestAction +"   resultCode："+resultCode);
			if(resultCode != OASISPlatformConstant.RESULT_SUCCESS){// 操作失败 或 操作取消
				switch (requestAction) {
				case 0:
					// share 分享
					showNotice("游戏方提示：分享失败");
					break;
				case 1:
					// invite 邀请
					break;
				case 2:
					// send 发送、赠送
					
					break;
				case 3:
					// askfor 索要
					break;

				default:
					
					break;
				}
				return;
			}
			switch (requestAction) {
			case 0:
				// share 分享
				showNotice("游戏方提示：分享成功");
				break;
			case 1:
				// invite 邀请
				break;
			case 2:
				// send 发送、赠送
				
				break;
			case 3:
				// askfor 索要
				break;

			default:
				
				break;
			}
		}
		
		@Override
		public void fbFriendsListCallback(int type, int resultCode, FBPageInfo info) {
			if(resultCode != OASISPlatformConstant.RESULT_SUCCESS || info == null)
				return;
			for (FriendInfo user : info.data) {
				System.out.println("id="+user.id+"  name="+user.name +" \n "+user.picture);
			}
			Intent in = new Intent();
			in.putExtra("fbpageinfo", info);
			if(type == OASISPlatformConstant.REQUEST_CODE_FACEBOOK_GETFRIENDS){
				// info 为正在玩该游戏的好友
				
				in.putExtra("friendType", 1);
			}
			else if(type == OASISPlatformConstant.REQUEST_CODE_FACEBOOK_GETINVITABLEFRIENDS){
				// info 为 可邀请的好友

				in.putExtra("friendType", 2);
			}
			in.setClass(activity, FriendsListActivity.class);
			startActivity(in);
		}
		
		@Override
		public void getExtendValue(OasisCallback callback) {
			// 生成支付时的透传参数 ext，功能等同于OASISPlatform.toGoogleBillPayPage时的ext参数
			String extValue = null;
			extValue = "ext_value";
			
			// 此方法中必须回调success或error函数，
			callback.success(extValue);
//			callback.error("error");
		}
	}
	
	private void showNotice(String s){
		Toast.makeText(this, s, Toast.LENGTH_LONG).show();
	}
	
	private void test(final String requestIds){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				boolean flag = OASISPlatform.test(activity, requestIds);
				if(flag)
					activity.myHandler.sendEmptyMessage(100);
					
				
			}
		}).start();
	}
	
	/**
	 * 获取FB的key hash
	 */
	private void getFBKeyHash(){

		//获取当前应用的 key hash（分为签名与非签名两种）
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				String sign = Base64
						.encodeToString(md.digest(), Base64.DEFAULT);
				Log.e("MY KEY HASH:", sign);
			}
		} catch (NameNotFoundException e) {
		} catch (NoSuchAlgorithmException e) {
		}
	}

}
