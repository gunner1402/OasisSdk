package com.oasis.sdk.activity;

import java.io.IOException;
import java.util.Vector;

import org.json.JSONObject;

import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.camera.CameraManager;
import com.google.zxing.decoding.CaptureActivityHandler;
import com.google.zxing.decoding.InactivityTimer;
import com.google.zxing.view.ViewfinderView;
import com.oasis.sdk.base.service.HttpService;
import com.oasis.sdk.base.utils.BaseUtils;
/**
 * Initial the camera
 * @author xdb
 */
public class OasisSdkCaptureActivity extends OasisSdkBasesActivity implements Callback {
//	private static String CHARGEURL = "http://pay.oasgames.com/payment/oaspay.php?";

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_capture"));
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_captrue_viewfinder"));
		
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		
		TextView notice = ((TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_captrue_copyurl")));//.setT
		notice.setText("1:"+getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_scan_text2_1"))+" http://mpay.oasgames.com \n2:"+getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_scan_text2_2"))+"\n3:"+getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_scan_success")));
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_captrue_preview"));
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
		
		setWaitScreen(false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}
	
	/**
	 * 处理扫描结果
	 * @param result
	 * @param barcode
	 */
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		String resultString = result.getText();
		
//		System.out.println(" saomiao = "+resultString);
		if (!TextUtils.isEmpty(resultString)){
//			BaseUtils.showMsg(this, resultString);
			
			new MyAsyncTask().execute(resultString);
//			Intent resultIntent = new Intent();
//			Bundle bundle = new Bundle();
//			bundle.putString("result", resultString);
//			bundle.putParcelable("bitmap", barcode);
//			resultIntent.putExtras(bundle);
//			this.setResult(RESULT_OK, resultIntent);
		}
//		OasisSdkCaptureActivity.this.finish();
	}
	
	private class MyAsyncTask extends AsyncTask<String,String,Boolean> {
		public MyAsyncTask() {
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setWaitScreen(true);
			CameraManager.get().stopPreview();
		}
		@Override
		protected Boolean doInBackground(String... arg0) {
			try {
				JSONObject json = new JSONObject(arg0[0]);
				if(json.has("pay_wish_id")){
					return HttpService.instance().toPcRecharge(json.getString("pay_wish_id"));
				}
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			setWaitScreen(false);
			if(result){
				BaseUtils.showMsg(OasisSdkCaptureActivity.this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_scan_success")));
				
//				try {
//					// 向Mdata发送数据
//					Map<String, String> parameters = new HashMap<String, String>();
//					parameters.put("payment_channel", "PC Charge");
//					parameters.put("cost", "unkown");
//					parameters.put("currency", "unkown");
//					parameters.put("value", "unkown");
//					OASISPlatform.trackEvent(OasisSdkCaptureActivity.this, ReportUtils.DEFAULTEVENT_ORDER, parameters);
//				} catch (Exception e) {
//					Log.e(TAG, "PC Charge send mdata fail.");
//				}
			}else{
				BaseUtils.showMsg(OasisSdkCaptureActivity.this, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_login_notice_autologin_exception")));
			}
			finish();
		}
		@Override
		protected void onCancelled() {
			super.onCancelled();
			setWaitScreen(false);
		}
	}
	
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
			Log.d("OasisSdkCaptureActivity", "surface created");
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(BaseUtils.getResourceValue("raw","oasisgames_sdk_captrue_beep"));
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

}