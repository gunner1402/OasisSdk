package com.oasis.sdk.base.utils;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.v4.util.LruCache;

public class ImageCache {
	private final static int MAX_MEMORY = 4 * 1024 * 1024;
	private LruCache<String, BitmapDrawable> mMemoryCache;

	private Set<SoftReference<Bitmap>> mReusableBitmaps;

	private void init() {
		if (hasHoneycomb()) {
			mReusableBitmaps = Collections
					.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
		}

		mMemoryCache = new LruCache<String, BitmapDrawable>(MAX_MEMORY) {

			/**
			 * 当保存的BitmapDrawable对象从LruCache中移除出来的时候回调的方法
			 */
			@Override
			protected void entryRemoved(boolean evicted, String key,
					BitmapDrawable oldValue, BitmapDrawable newValue) {

				if (hasHoneycomb()) {
					mReusableBitmaps.add(new SoftReference<Bitmap>(oldValue
							.getBitmap()));
				}
			}

		};
	}

	
	/**
	 * 从mReusableBitmaps中获取满足 能设置到BitmapFactory.Options.inBitmap上面的Bitmap对象
	 * @param options
	 * @return
	 */
	protected Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
		Bitmap bitmap = null;

		if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
			synchronized (mReusableBitmaps) {
				final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps
						.iterator();
				Bitmap item;

				while (iterator.hasNext()) {
					item = iterator.next().get();

					if (null != item && item.isMutable()) {
						if (canUseForInBitmap(item, options)) {
							bitmap = item;
							iterator.remove();
							break;
						}
					} else {
						iterator.remove();
					}
				}
			}
		}
		return bitmap;
	}

	/**
	 * 判断该Bitmap是否可以设置到BitmapFactory.Options.inBitmap上
	 * 
	 * @param candidate
	 * @param targetOptions
	 * @return
	 */
	@TargetApi(VERSION_CODES.KITKAT)
	public static boolean canUseForInBitmap(Bitmap candidate,
			BitmapFactory.Options targetOptions) {

		// 在Anroid4.4以后，如果要使用inBitmap的话，只需要解码的Bitmap比inBitmap设置的小就行了，对inSampleSize
		// 没有限制
		if (Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			int width = targetOptions.outWidth / targetOptions.inSampleSize;
			int height = targetOptions.outHeight / targetOptions.inSampleSize;
			int byteCount = width * height
					* getBytesPerPixel(candidate.getConfig());
			return byteCount <= candidate.getAllocationByteCount();
		}

		// 在Android
		// 4.4之前，如果想使用inBitmap的话，解码的Bitmap必须和inBitmap设置的宽高相等，且inSampleSize为1
		return candidate.getWidth() == targetOptions.outWidth
				&& candidate.getHeight() == targetOptions.outHeight
				&& targetOptions.inSampleSize == 1;
	}

	/**
	 * 获取每个像素所占用的Byte数
	 * 
	 * @param config
	 * @return
	 */
	public static int getBytesPerPixel(Config config) {
		if (config == Config.ARGB_8888) {
			return 4;
		} else if (config == Config.RGB_565) {
			return 2;
		} else if (config == Config.ARGB_4444) {
			return 2;
		} else if (config == Config.ALPHA_8) {
			return 1;
		}
		return 1;
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;
	}

}
