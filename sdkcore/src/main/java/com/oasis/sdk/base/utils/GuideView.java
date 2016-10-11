package com.oasis.sdk.base.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.View;

public class GuideView extends View{
		int space = 25;
		int left, top, right, bottom, width, height, screeWidth, screeHeight;
		String text;
		Paint paint ;
		DisplayMetrics dm;
		Bitmap bm_shadow, bm_jiantou;
		NinePatch np_shadow;
		public GuideView(Context context) {
			super(context);
			paint = new Paint();
			paint.setAntiAlias(true); 
			// 获取屏幕密度（方法2）
			dm = new DisplayMetrics();
			dm = getResources().getDisplayMetrics();
			screeWidth = dm.widthPixels;
			screeHeight = dm.heightPixels;
			
			bm_shadow = BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_guide_shadow"));  
			np_shadow = new NinePatch(bm_shadow, bm_shadow.getNinePatchChunk(), null);   
		}
		public void setPoint(int left, int top, int right, int bottom, int height, int width, String text){
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
			this.height = height;
			this.width = width;
			this.text = text;
			
			bm_jiantou = BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", (left<screeWidth/2)?"oasisgames_sdk_guide_leftbottom":"oasisgames_sdk_guide_rightbottom")); 
			
		}
		public void setPoint(Rect r, int height, int width, String text){
			this.left = r.left;
			this.top = r.top;
			this.right = r.right;
			this.bottom = r.bottom;
			this.height = height;
			this.width = width;
			this.text = text;
			
			bm_jiantou = BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", (left<screeWidth/2)?"oasisgames_sdk_guide_leftbottom":"oasisgames_sdk_guide_rightbottom")); 
			
		}
		@Override
		protected void onDraw(Canvas canvas) {
			//1 先画目标上方的区域
			paint.setColor(Color.parseColor("#B2000000"));
			canvas.drawRect(0, 0, screeWidth, top-space, paint);
			
			//2画突出显示位置
			RectF rectF2 = null;
			if(width > (screeWidth-space*2)){
				rectF2 = new RectF(0, top-space, screeWidth, bottom+space);
				np_shadow.draw(canvas, rectF2); 
			}else{
				paint.setColor(Color.parseColor("#B2000000"));
				canvas.drawRect(0, top-space, left - space, bottom+space, paint);
				
				rectF2 = new RectF(left - space, top-space, right + space, bottom+space);
				np_shadow.draw(canvas, rectF2); 
				
				paint.setColor(Color.parseColor("#B2000000"));
				canvas.drawRect(right + space, top-space, screeWidth, bottom+space, paint);
			}
			
			//3 画目标下方的区域
			paint.setColor(Color.parseColor("#B2000000"));
			canvas.drawRect(0, top+height+space, screeWidth, screeHeight, paint);
			
			//4引导指示箭头
			canvas.drawBitmap(bm_jiantou, left+width*0.2f, top-space-bm_jiantou.getHeight(), paint);
			
			//5引导文字
			paint.setColor(Color.parseColor("#FFFFFF"));
			float textsize = DisplayUtil.sp2px(18, BaseUtils.getDensity());
			paint.setTextSize(textsize);
			int textLen = (int) paint.measureText(text);
			int startX = 0;
			int endX = 0;
			if(left<screeWidth/2){
				startX = (int) (left+width*0.2f+bm_jiantou.getWidth());
				endX = screeWidth;
			}else{
				startX = 0;
				endX = (int) (left+width*0.2f);
			}
			int rows = textLen / (endX-startX) + 1;
			String[] texts = null;
			if(text.contains(" ")){
				rows += 1;// 以空格分割，可能包括其他标点符号，估多显示一行，以防止显示不完整的情况
				texts = new String[rows];
				String[] split = text.split(" "); 
				int size = (int) Math.ceil(split.length / rows);
				if(size * rows < split.length)
					size += 1;
				for (int i = 0; i < rows; i++) {
					StringBuffer sb = new StringBuffer("");
					for (int j = i*size; j < (i+1)*size; j++) {
						if(j >= split.length)
							break;
						sb.append(split[j]);
						sb.append(" ");
					}
					texts[i] = sb.toString();
				}
			}else{
				texts = new String[rows];
				int size = (int)(text.length() / rows);
				for (int i = 0; i < rows; i++) {
					if((i+1) < rows)
						texts[i] = text.substring(i*size, (i+1)*size);
					else
						texts[i] = text.substring(i*size);
				}
			}
			for (int i = 0; i < rows; i++) {
				if(left<screeWidth/2)
					canvas.drawText(texts[i], startX, top-space-((rows -i)*textsize), paint);
				else{
					int lenth = (int) paint.measureText(texts[i]);
					canvas.drawText(texts[i], endX-lenth, top-space-((rows -i)*textsize), paint);
				}
			}
		}
	}