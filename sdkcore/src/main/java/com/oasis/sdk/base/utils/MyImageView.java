package com.oasis.sdk.base.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MyImageView extends ImageView {
	private Bitmap bitmap;
	public MyImageView(Context context) {
		super(context);
	}
	public MyImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public MyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		if(this.bitmap != null)
			bitmap.recycle();
		this.bitmap = bm;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}
	
}
