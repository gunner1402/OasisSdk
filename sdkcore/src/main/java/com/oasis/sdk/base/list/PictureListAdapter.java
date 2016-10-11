package com.oasis.sdk.base.list;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oasis.sdk.activity.OasisSdkPictureListActivity;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.DisplayUtil;
import com.oasis.sdk.base.utils.MyImageView;

/**
 * 图片列表适配器
 * @author Administrator
 *
 */
public class PictureListAdapter extends RecyclerView.Adapter<PictureListAdapter.MyViewHolder> {
	OasisSdkPictureListActivity c;
	List<String> data;

	public PictureListAdapter(Activity activity, List<String> data,
			int count, LinearLayout footerView) {
		this.c = (OasisSdkPictureListActivity) activity;
		this.data = data;
	}

	@Override
	public int getItemCount() {
		if(data != null )
			return data.size();
		return 0;
	}

	public static class MyViewHolder extends RecyclerView.ViewHolder{
		com.oasis.sdk.base.utils.MyImageView img;
		TextView tv_selected;
		public MyViewHolder(View arg0) {
			super(arg0);
			img = (MyImageView) arg0.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pictrue_list_item_img"));
			tv_selected = (TextView) arg0.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pictrue_list_item_selected"));
		}
	}

	@Override
	public void onBindViewHolder(MyViewHolder arg0, int arg1) {
		final String path = data.get(arg1);
		arg0.img.setTag(path);
		loadBitmap(path, arg0.img);
		
		if(c.picSelected.contains(path)){
			arg0.tv_selected.setVisibility(View.VISIBLE);
		}else{
			arg0.tv_selected.setVisibility(View.INVISIBLE);
		}
		//将数据保存在itemView的Tag中，以便点击时进行获取
		arg0.itemView.setTag(path);

	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
		View convertView = c.getLayoutInflater().inflate(BaseUtils.getResourceValue("layout", "oasisgames_sdk_picture_list_item"), null);
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mOnItemClickListener != null) {
		            //注意这里使用getTag方法获取数据
		            mOnItemClickListener.onItemClick(v, (String)v.getTag());
		        }
			}
		});
		MyViewHolder holder = new MyViewHolder(convertView);
		return holder;
	}

	
	public void loadBitmap(String resId, ImageView imageView) {
	      if (cancelPotentialWork(resId, imageView)) {
	          final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
	          final AsyncDrawable asyncDrawable =
	                  new AsyncDrawable(c.getResources(), null, task);
	          imageView.setImageDrawable(asyncDrawable);
	          task.execute(resId);
	      }
	  	
	  }

	  static class AsyncDrawable extends BitmapDrawable {
	      private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

	      public AsyncDrawable(Resources res, Bitmap bitmap,
	              BitmapWorkerTask bitmapWorkerTask) {
	          super(res, bitmap);
	          bitmapWorkerTaskReference =
	              new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
	      }

	      public BitmapWorkerTask getBitmapWorkerTask() {
	          return bitmapWorkerTaskReference.get();
	      }
	  }

	  public static boolean cancelPotentialWork(String data, ImageView imageView) {
	      final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

	      if (bitmapWorkerTask != null) {
	          final String bitmapData = bitmapWorkerTask.data;
	          if (bitmapData.equals(data)) {
	              // Cancel previous task
	              bitmapWorkerTask.cancel(true);
	          } else {
	              // The same work is already in progress
	              return false;
	          }
	      }
	      // No task associated with the ImageView, or an existing task was cancelled
	      return true;
	  }

	  private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
	     if (imageView != null) {
	         final Drawable drawable = imageView.getDrawable();
	         if (drawable instanceof AsyncDrawable) {
	             final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
	             return asyncDrawable.getBitmapWorkerTask();
	         }
	      }
	      return null;
	  }
	  class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
	      private final WeakReference<ImageView> imageViewReference;
	      private String data = "";

	      public BitmapWorkerTask(ImageView imageView) {
	          // Use a WeakReference to ensure the ImageView can be garbage collected
	          imageViewReference = new WeakReference<ImageView>(imageView);
	      }

	      // Decode image in background.
	      @Override
	      protected Bitmap doInBackground(String... params) {
	          data = params[0];
	          Bitmap bitmap = BaseUtils.getSmallBitmap(data, DisplayUtil.dip2px(200, BaseUtils.getDensity()), DisplayUtil.dip2px(200, BaseUtils.getDensity()));
	          return bitmap;
//	          return BitmapFactory.decodeFile(data);
	      }

	      // Once complete, see if ImageView is still around and set bitmap.
	      @Override
	      protected void onPostExecute(final Bitmap bitmap) {
	          if (imageViewReference != null && bitmap != null) {
	              final ImageView imageView = imageViewReference.get();
	              if (imageView != null) {
	              	c.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								imageView.setImageBitmap(bitmap);
								
							}
						});
	              }
	          }
	      }
	  }
	  

		public OnRecyclerViewItemClickListener mOnItemClickListener = null;
	    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
		    this.mOnItemClickListener = listener;
		}

		public static interface OnRecyclerViewItemClickListener {
		    void onItemClick(View view, String data);
		}

}
