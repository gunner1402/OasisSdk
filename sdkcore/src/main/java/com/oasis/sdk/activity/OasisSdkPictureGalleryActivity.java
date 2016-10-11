package com.oasis.sdk.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.oasis.sdk.base.utils.BaseUtils;
/**
 * 图片预览
 * @author xdb
 */
public class OasisSdkPictureGalleryActivity extends OasisSdkBaseActivity{

	final String TAG = OasisSdkPictureGalleryActivity.class.getName();
	
	public List<String> picSelected = new ArrayList<String>();// 已选择图片
	
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<ImageAdapter.MyViewHolder> mAdapter;
    private GridLayoutManager mLayoutManager;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRecyclerView = new RecyclerView(this);

		picSelected = getIntent().getStringArrayListExtra("data");
		// improve performance if you know that changes in content
		// do not change the size of the RecyclerView
		mRecyclerView.setHasFixedSize(true);

		// use a linear layout manager
		mLayoutManager = new GridLayoutManager(this, 1);// 固定为1
		mLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
		mRecyclerView.setLayoutManager(mLayoutManager);

		// specify an adapter (see also next example)
		mAdapter = new ImageAdapter(this, picSelected);
		mRecyclerView.setAdapter(mAdapter);

		setContentView(mRecyclerView);
	}
	public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {
		private Context mContext;
		private List<String> data ;
		DisplayMetrics dm;
		
		public ImageAdapter(Context c, List<String> pics) {
			mContext = c;
			data = pics;
			dm = BaseUtils.getDisplayMetrics((Activity)mContext);
		}

		public int getCount() {
			return picSelected.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getItemCount() {
			if (data != null) {
				return data.size();
			}
			return 0;
		}

		@Override
		public MyViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
			View convertView = getLayoutInflater().inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_picture_gallery_item"), null);
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					finish();
				}
			});
			MyViewHolder vh = new MyViewHolder(convertView);  
			return (MyViewHolder) vh;
		}

		// Provide a reference to the type of views that you are using  
		// (custom viewholder)  
		public class MyViewHolder extends RecyclerView.ViewHolder {  
			public ImageView mTextView;  

			public MyViewHolder(View v) {
				super(v);
				mTextView = (ImageView) v.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pictrue_gallery_item_img"));
			}
		}

		@Override
		public void onBindViewHolder(MyViewHolder arg0, int arg1) {
			int showWidth = dm.widthPixels, showHeight = dm.heightPixels;
			if(dm.widthPixels > 480 || dm.heightPixels > 800){
				showWidth = 480;
				showHeight = 800;
			}
				
			Bitmap b = BaseUtils.getSmallBitmap(data.get(arg1), showWidth, showHeight);
			if(b != null)
//				arg0.mTextView.setImageResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_customer_bg_default"));
//			else
				arg0.mTextView.setImageBitmap(b);
		} 

	}

}