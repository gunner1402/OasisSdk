package com.oasis.sdk;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;

import com.oasis.sdk.activity.OasisSdkForumActivity;
import com.oasis.sdk.activity.OasisSdkLogInfoActivity;
import com.oasis.sdk.activity.OasisSdkMenuGuideActivity;
import com.oasis.sdk.activity.OasisSdkPayActivity;
import com.oasis.sdk.activity.OasisSdkPersonCenterActivity;
import com.oasis.sdk.activity.OasisSdkShareActivity;
import com.oasis.sdk.base.report.ReportUtils;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.DisplayUtil;
import com.oasis.sdk.base.utils.SystemCache;

public class OASISPlatfromMenu extends View {
	private static final int handler_hide = 100000; 		// 靠边隐藏
	private static final int handler_init_default = 100001; // 从初始化恢复默认情况
	private static final int handler_loop = 100002; 		// 按住OG时，循环图片
	private static final int TOUCH_TYPE_INIT = -2; 			// 初始化
	private static final int TOUCH_TYPE_DEFAULT = -1; 		// 默认
	private static final int TOUCH_TYPE_DOWN = 0; 			// 按下
	private static final int TOUCH_TYPE_SCROLL = 1; 		// 滑动
	private static final int TOUCH_TYPE_ITEMCLICK = 2; 		// 点击菜单项
	private static final int TOUCH_TYPE_DOWN_UP = 3; 		// 按下抬起后，展开所有菜单
	private static final int TOUCH_TYPE_HIDE = 5; 			// 隐藏（靠边缩进）
	private static final int delayMsec = 5000; 			// 延迟时间
	
	private static final int itemTagsIndex_PERSON = 0;
	private static final int itemTagsIndex_CHARGEOTHER = 1;
	private static final int itemTagsIndex_BBS = 2;
	private static final int itemTagsIndex_SHARE = 3;
	private static final int itemTagsIndex_HELP = 4;

	public static int[] itemImages = new int[]{
		BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_ico_connect"),
		BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_ico_charge_other"),
		BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_ico_bbs"),
		BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_ico_share"),
		BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_ico_help")
		};
	private static boolean[] itemShowFlag = new boolean[]{true,false,true,true,true};
	private List<Integer> itemList = new ArrayList<Integer>();
	
	DisplayMetrics dm ;
	int defalutShowLocation = 1;
	
	private static int MARGIN = 5;// 默认距离
	private int MARGINLOGO = 5;// 带logo的距离
	private int padding = 10;// item背景与logo的距离
	private int itemMargin = 0;// item 间距

	private int itemSingleWidth;// 单个item宽度
	private int itemALLWidth;// 所有item宽度 
	private int logoWidth;
	private int logoHideOffset;//隐藏logo偏移量
	private int screeWidth;
	private int screeHeight;
	
	private GestureDetector detector;// 手势
	private Context context;
	Paint paint ;
	Paint paint_text ;

	int showItemsCount = 0;
	
	int touchType = TOUCH_TYPE_INIT;// 触摸、操作类型:初始化-2，默认为-1，按下logo为0，滑动为1，点击item为2
	Point point;// View左上角坐标
	/**
	 *  默认在左边，为true时 在右边
	 */
	boolean dirction = false;
	/**
	 * 默认为false收缩，展开为true
	 */
	boolean isExpand = false;
	
	private Bitmap bm_left; 
	private Bitmap bm_right; 
	private NinePatch np_left;
	private NinePatch np_right;	
	
	MyHandler myHandler;
	
	Bitmap[] m_animationPics = null;// 拖动或按住不放时 循环的图片
	private int m_CurPic=0;// 循环起始值
	private int m_LoopTimes=80;// 循环循环间隔时间 ；单位 毫秒
	private boolean m_bStartLooper = false;  

	
	public OASISPlatfromMenu(Context context, int defaultShowLocation) {
		super(context);
		this.context = context;
		this.setFocusable(true);
		SystemCache.menu = this;
		defalutShowLocation = defaultShowLocation;
		init(context);
	}

//	public OASISPlatfromMenu(Context context, AttributeSet attrs) {
//		super(context, attrs);
//		this.context = context;
//		this.setFocusable(true);
//		init(context);
//		SystemCache.menu = this;
//	}

	private void init(final Context context) {
		m_animationPics = new Bitmap[]{
				BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_content_0")),
				BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_content_1")),
				BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_content_2")),
				BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_content_3")),
				BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_content_4")),
				BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_content_5")),
				BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_content_6")),
				BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_content_7")),
				BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_content_8")),
				BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_content_9")),
				BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_content_10")),
				BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_content_11"))
		};

		myHandler = new MyHandler();
		paint = new Paint();
		paint.setAntiAlias(true); 
		
//		paint_text = new Paint();
//		paint_text.setTextSize(DisplayUtil.sp2px(12, BaseUtils.getDensity()));  
//		paint_text.setColor(Color.WHITE);// 白色  
//		paint_text.setTextAlign(Paint.Align.CENTER);// 对齐方向  ,此参数影响文字绘制的起始位置
        
		detector = new GestureDetector(context, new GestureListener());
		
		
		// 获取屏幕密度（方法2）
		dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();
		screeWidth = dm.widthPixels;
		screeHeight = dm.heightPixels;
		
		itemSingleWidth = (int) getResources().getDimension(BaseUtils.getResourceValue("dimen", "oasisgames_sdk_dimen_menu_item_w")) + itemMargin;
		
		logoWidth = (int) getResources().getDimension(BaseUtils.getResourceValue("dimen", "oasisgames_sdk_dimen_menu_control_wh"));
		
		logoHideOffset = logoWidth/10*5;
		
		padding = (int) getResources().getDimension(BaseUtils.getResourceValue("dimen", "oasisgames_sdk_dimen_menu_control_padding"));
		MARGIN = 0;
//		itemSingleWidth = DisplayUtil.dip2px(56, BaseUtils.getDensity());
//		
//		logoWidth = DisplayUtil.dip2px(68, BaseUtils.getDensity());
//		
//		padding = DisplayUtil.dip2px(4, BaseUtils.getDensity());
//		MARGIN = DisplayUtil.dip2px(2, BaseUtils.getDensity());
		MARGINLOGO = MARGIN+logoWidth;
		
		String p = (String) BaseUtils.getSettingKVPfromSysCache("MENUPOINT","");
		if(!TextUtils.isEmpty(p)){
			String[] parr = p.split(";");
			try {
				int x = Integer.valueOf(parr[0]);
				int y = Integer.valueOf(parr[1]);
				if(screeWidth > x && x >= 0
						&& screeHeight > y && y >= 0){
					
					if(x < screeWidth/2){
						dirction = false;
						if(x > 0 )
							x = 0;
					}else{ 
						dirction = true;
						x = screeWidth - MARGINLOGO;
					}
					
					point = new Point(x, y);
				}
			} catch (NumberFormatException e) {
			}
		}
		if(point == null){
			point = new Point(MARGIN, MARGIN);
			switch (defalutShowLocation) {
			case 1:// LEFTTOP
				dirction = false;
				break;
			case 2:// LEFTCENTER
				point.y = screeHeight / 2 - MARGINLOGO / 2;
				dirction = false;
				break;
			case 3:// LEFTBOTTOM
				point.y = screeHeight - MARGINLOGO;
				dirction = false;
				break;
			case 4:// RIGHTTOP
				point.x = screeWidth - MARGINLOGO;
				dirction = true;
				break;
			case 5:// RIGHTCENTER
				point.x = screeWidth - MARGINLOGO;
				point.y = screeHeight / 2 - MARGINLOGO / 2;
				dirction = true;
				break;
			case 6:// RIGHTBOTTOM
				point.x = screeWidth - MARGINLOGO;
				point.y = screeHeight - MARGINLOGO;
				dirction = true;
				break;
	
			default:
				break;
			}
		}
		
		bm_left = BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_content_right"));  
		np_left = new NinePatch(bm_left, bm_left.getNinePatchChunk(), null);  
		bm_right = BitmapFactory.decodeResource(getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_content_left"));
		np_right = new NinePatch(bm_right, bm_right.getNinePatchChunk(), null);  
		
	}
	/**
	 * 每次展开OG菜单时，调用该方法
	 */
	private void fresh(){
		// 初始化 或者 item有更新时，需要重新计算
		itemShowFlag[itemTagsIndex_BBS] = SystemCache.controlInfo.getForum_onoff_control();
		itemShowFlag[itemTagsIndex_CHARGEOTHER] = SystemCache.controlInfo.getCharge_onoff_control(context);
		itemShowFlag[itemTagsIndex_SHARE] = SystemCache.controlInfo.getShare_onoff_control();
		
		showItemsCount = 0;// 先清零
		for (Boolean showflag : itemShowFlag) {
			if(showflag)
				showItemsCount ++;
		}
		if(showItemsCount != 0)
			itemALLWidth = itemSingleWidth*showItemsCount + MARGINLOGO/2;
		
	}
	
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		if(touchType == TOUCH_TYPE_INIT){// OG初始化
			boolean isshow = (Boolean) BaseUtils.getSettingKVPfromSysCache("oasis_og_isshow", false);
			if(isshow){
				drawLogo(canvas, BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_control_normal"));
				touchType = TOUCH_TYPE_DEFAULT;
				myHandler.sendEmptyMessageDelayed(handler_hide, delayMsec);
			}else{
				isExpand = true;// 展开状态
				
				drawInitText(canvas);
				
				BaseUtils.saveSettingKVPtoSysCache("oasis_og_isshow", true);// 提示只显示一次，5秒后消失
				myHandler.sendEmptyMessageDelayed(handler_init_default, delayMsec);
			}
		}else if(touchType == TOUCH_TYPE_DOWN_UP){
			fresh();
			
			if(itemALLWidth > 0){
				RectF rectF2 = null;
				if(dirction){
					rectF2 = new RectF(point.x-itemALLWidth, point.y+padding, point.x, point.y+logoWidth-padding);
					np_right.draw(canvas, rectF2); 
				}else{
					rectF2 = new RectF(point.x+logoWidth, point.y+padding, point.x+itemALLWidth+logoWidth, point.y+logoWidth-padding);
					np_left.draw(canvas, rectF2); 
				}
				
				paint.setAlpha(255);
				drawLogo(canvas, BaseUtils.getResourceValue("drawable", dirction?"oasisgames_sdk_menu_control_pressed_right":"oasisgames_sdk_menu_control_pressed_left"));
				
				drawItemTextAndImage(canvas);
				myHandler.sendEmptyMessageDelayed(handler_hide, delayMsec/2);// OG展开时，delayMills后收起 
			}else{
				touchType = TOUCH_TYPE_DEFAULT;
				drawLogo(canvas, BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_control_normal"));
				myHandler.sendEmptyMessageDelayed(handler_hide, delayMsec);
			}
		}else if(touchType == TOUCH_TYPE_DEFAULT){
			drawLogo(canvas, BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_control_normal"));
		}else if(touchType == TOUCH_TYPE_SCROLL){
			canvas.drawBitmap(m_animationPics[m_CurPic], point.x, point.y, paint);
			invalidate();
		}else if(touchType == TOUCH_TYPE_HIDE){
			drawLogo(canvas, BaseUtils.getResourceValue("drawable", dirction?"oasisgames_sdk_menu_control_hide_right":"oasisgames_sdk_menu_control_hide_left"));
		}else{
			drawLogo(canvas, BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_control_normal"));
		}
	}
	private BitmapFactory.Options getOptions(int id){
		TypedValue value = new TypedValue();
		context.getResources().openRawResource(id, value);
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inTargetDensity = value.density;
		return opts;
	}
	private void drawLogo(Canvas canvas, int id){
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id, getOptions(id));
//		bitmap = BitmapFactory.decodeFile(img);
//		bitmap.setDensity((int)(BaseUtils.getDensity()*160));
		canvas.drawBitmap(bitmap, point.x, point.y, paint);
	}
	private void drawItemTextAndImage(Canvas canvas){
		int allItemLen = itemShowFlag.length;
		int i = 0;

		itemList.clear();
		for (int j = 0; j < allItemLen; j++) {
			if(itemShowFlag[j]){
				
				Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), itemImages[j], getOptions(itemImages[j]));
				
				if(dirction){
					canvas.drawBitmap(bitmap, point.x-(showItemsCount -i)*itemSingleWidth+(itemSingleWidth-bitmap.getWidth())/2, point.y+(logoWidth-bitmap.getHeight())/2, paint);
				}else{
					canvas.drawBitmap(bitmap, point.x+MARGINLOGO+i*itemSingleWidth+(itemSingleWidth-bitmap.getWidth())/2, point.y+(logoWidth-bitmap.getHeight())/2, paint);
				}
				
				if((j == itemTagsIndex_BBS && SystemCache.luntan != null)
						|| (j == itemTagsIndex_PERSON && SystemCache.userInfo != null && SystemCache.userInfo.isShowCustomerNewsFlag) ){// 为论坛图标添加标识
					Bitmap flag = BitmapFactory.decodeResource(context.getResources(), BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_ico_tag"));
					if(dirction){
						canvas.drawBitmap(flag, point.x-(showItemsCount -i)*itemSingleWidth+(itemSingleWidth-flag.getScaledWidth(dm)*2), point.y+flag.getScaledHeight(dm), paint);
					}else{
						canvas.drawBitmap(flag, point.x+MARGINLOGO+i*itemSingleWidth+(itemSingleWidth-flag.getScaledWidth(dm)*2), point.y+flag.getScaledHeight(dm), paint);
					}
				}
				
				i++;
				itemList.add(j);
			}
		}
		
	}
	private void drawInitText(Canvas canvas){
		String text = getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_menu_notice_drag"));
//		text = "Suporta Arrastar e Soltar";
//		text = "支持拖拽";
		float textsize = DisplayUtil.sp2px(18, BaseUtils.getDensity());
		
		float[] textWidthArray = new float[text.length()];
		TextPaint textPaint = new TextPaint();
		textPaint.setColor(Color.parseColor("#01aed9"));
		textPaint.setTextSize(textsize);
//		textPaint.setTextAlign(Paint.Align.CENTER);// 对齐方向  ,此参数影响文字绘制的起始位置
		textPaint.getTextWidths(text, textWidthArray);

		int sum = 10;// 统计所有字符显示的宽度，默认为10
		for (int i = 0; i < textWidthArray.length; i++) {
			sum += textWidthArray[i];
		}
		
		RectF rectF2 = null;
		if(dirction){
			rectF2 = new RectF(point.x-sum-logoWidth/2, point.y+padding, point.x, point.y+logoWidth-padding);
			np_right.draw(canvas, rectF2); 
		}else{
			rectF2 = new RectF(point.x+logoWidth, point.y+padding, point.x+sum+logoWidth+logoWidth/2, point.y+logoWidth-padding);
			np_left.draw(canvas, rectF2); 
		}
		
		paint.setAlpha(255);
		drawLogo(canvas, dirction?BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_control_pressed_right"):BaseUtils.getResourceValue("drawable", "oasisgames_sdk_menu_control_pressed_left"));
		
		if(dirction)
			canvas.drawText(text, point.x - sum, point.y + logoWidth -(logoWidth - textsize)/2 - padding, textPaint);
		else
			canvas.drawText(text, (point.x+logoWidth), point.y + logoWidth -(logoWidth - textsize)/2 - padding, textPaint);
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if(MotionEvent.ACTION_UP == event.getAction()){
			m_bStartLooper = false;// 抬起时，停止循环
			int x = (int) event.getX();
			if(touchType == TOUCH_TYPE_DOWN){
				if(isExpand){// 收缩
					touchType = TOUCH_TYPE_DEFAULT;
					isExpand = false;
					
					myHandler.sendEmptyMessageDelayed(handler_hide, delayMsec);
				}else{// 展开
					touchType = TOUCH_TYPE_DOWN_UP;
					isExpand = true;
					if(point.x < 0)
						point.x = 0;
					else if(point.x > screeWidth-MARGINLOGO)
						point.x = screeWidth-MARGINLOGO;
					
				}
				postInvalidate();
			}else if(touchType == TOUCH_TYPE_ITEMCLICK){
				onClickItem(x, (int)event.getY());
				isExpand = false;
				touchType = TOUCH_TYPE_DEFAULT;// 复原
				postInvalidate();
				
				myHandler.sendEmptyMessageDelayed(handler_hide, delayMsec);
			}else if(touchType == TOUCH_TYPE_SCROLL){
				touchType = TOUCH_TYPE_DEFAULT;// 滑动结束，先恢复默认logo，再做其他事情
				postInvalidate();
				// 滑动事件结束
				rejustPosition();
			}
			return false;
		}
		return detector.onTouchEvent(event);
	};
	
	class GestureListener extends GestureDetector.SimpleOnGestureListener {
		int downX = 0, downY=0;
		@Override
		public boolean onDown(MotionEvent event) {
			downX = (int) event.getX();
			downY = (int) event.getY();
//			System.out.println("  downX="+downX +"   downY="+downY);
			if(downX > point.x && downX < point.x+logoWidth
					&& downY > point.y && downY < point.y+logoWidth){
				myHandler.removeMessages(handler_init_default);
				myHandler.removeMessages(handler_hide);
				if(touchType == TOUCH_TYPE_HIDE)
					isExpand = false;
				touchType = TOUCH_TYPE_DOWN;// 按下logo
				return true;
			}else if(isExpand){
				if(downY > point.y && downY < point.y+logoWidth &&
						((dirction && downX < point.x && downX > point.x - showItemsCount*itemSingleWidth)
								|| (!dirction && downX < point.x + MARGINLOGO + showItemsCount*itemSingleWidth && downX > point.x))){// 按下item的某个区域
					touchType = TOUCH_TYPE_ITEMCLICK;
					myHandler.removeMessages(handler_init_default);
					myHandler.removeMessages(handler_hide);
					
					return true;					
				}
				return false;
			}else
				return false;
		}

		@Override
		public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
				float arg3) {
			// 滑动事件结束
//			int x = (int) arg1.getX();
			rejustPosition();
			return false;
		}

		@Override
		public void onLongPress(MotionEvent arg0) {
		}

		@Override
		public boolean onScroll(MotionEvent event, MotionEvent arg1, float arg2,
				float arg3) {
			touchType = TOUCH_TYPE_SCROLL;
			isExpand = false;
			rejustPosition((int)-arg2, (int)-arg3);
			return false;
		}

		@Override
		public void onShowPress(MotionEvent arg0) {}

		@Override
		public boolean onSingleTapUp(MotionEvent arg0) {
			
			System.out.println("onSingleTapUp");
			return super.onSingleTapUp(arg0);
		}
		
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {

			System.out.println("onSingleTapConfirmed");
			return super.onSingleTapConfirmed(e);
			
		}
	};
	/**
	 * 根据横坐标，判断如何调整位置
	 */
	private void rejustPosition(){
//		if(point.x > screeWidth/2){// 往右移
//			int length = (int) (screeWidth - point.x);
//			for (int i = 0; i < length; i++) {
//				rejustPosition(1, 0);
//			}
//			
//		}else if(point.x <= screeWidth/2){// 往左移
//			int length = (int) (point.x);
//			for (int i = 0; i < length; i++) {
//				rejustPosition(-1, 0);
//			}
//		}
		slideview(point.x);
			
	}
	/**
	 * 具体调整位置的方式
	 */
	public void rejustPosition(int dx, int dy){
		int x = point.x+((int)dx);
		int y = point.y+((int)dy);
		
		if(x < MARGIN)
			x = MARGIN;
		if(x > screeWidth - MARGINLOGO)
			x = screeWidth - MARGINLOGO;
		
		if(y < MARGIN)
			y = MARGIN;
		if(y > screeHeight - MARGINLOGO)
			y = screeHeight - MARGINLOGO;
		point.x = x;
		point.y = y;
		
		if(x > screeWidth / 2){
			dirction = true;
		}else{
			dirction = false;
		}
		
		loop();
	}
	private void loop(){
		if(m_bStartLooper)// 如果已经在循环了，就不用再发起循环了
			return;
		
		postInvalidate();
		m_bStartLooper = true;
		myHandler.sendEmptyMessageDelayed(handler_loop, m_LoopTimes);// 间隔m_LoopTimes执行一次		
	}
	/**
	 * 根据坐标判断点击的是哪个item
	 * @param x
	 * @param y
	 */
	private void onClickItem(int x, int y){
		int start = 0 ;
		if(dirction){
			start = point.x - showItemsCount*itemSingleWidth;
		}else{
			start = point.x + MARGINLOGO; 
		}
		
		int selectIndex = -1;
		for (int i = 0; i < showItemsCount; i++) {
			if(x > start+i*itemSingleWidth && x < start+i*itemSingleWidth + itemSingleWidth
					&& y > point.y && y < point.y + MARGINLOGO){
				selectIndex = i;
				break;
			}
		}
		if(selectIndex >= 0 && selectIndex < itemList.size())
			handlerItem(itemList.get(selectIndex));
	}
	
	/**
	 * 处理选中的item
	 * @param index 索引
	 */
	private void handlerItem(int index){
		if(!BaseUtils.networkIsAvailable(context)){
			BaseUtils.showMsg((Activity)context, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_common_net_disable")));
			return;
		}
		
		if(index == itemTagsIndex_CHARGEOTHER && SystemCache.userInfo != null && SystemCache.userInfo.chargeable != 0){
			BaseUtils.showDisableDialog(context, SystemCache.userInfo.chargeable == 1?"oasisgames_sdk_login_notice_11":"oasisgames_sdk_login_notice_12");
			return;
		}
		
		String trackName = "";
		switch (index) {
		case itemTagsIndex_PERSON:
			context.startActivity(new Intent(context, OasisSdkPersonCenterActivity.class));
			trackName = ReportUtils.DEFAULTEVENT_CLICK_OG_UCENTER;
			break;
		case itemTagsIndex_BBS:
			context.startActivity(new Intent(context, OasisSdkForumActivity.class));
			trackName = ReportUtils.DEFAULTEVENT_CLICK_OG_FORUM;
			break;
		case itemTagsIndex_SHARE:
//			OASISPlatform.shareByFacebook((Activity)context, null, null, null, null, null);
			context.startActivity(new Intent(context, OasisSdkShareActivity.class));
			trackName = ReportUtils.DEFAULTEVENT_CLICK_OG_SHARE;
			break;
		case itemTagsIndex_HELP:
			if(BaseUtils.isSandBox()){
				context.startActivity(new Intent(context, OasisSdkLogInfoActivity.class));
			}else{
				context.startActivity(new Intent(context, OasisSdkMenuGuideActivity.class).putExtra("showFlag", itemShowFlag));
				this.setVisibility(View.INVISIBLE);
			}
			trackName = ReportUtils.DEFAULTEVENT_CLICK_OG_HELP;
			break;
		case itemTagsIndex_CHARGEOTHER:
			if(SystemCache.userInfo != null && !TextUtils.isEmpty(SystemCache.userInfo.serverID) && !TextUtils.isEmpty(SystemCache.userInfo.roleID))
				context.startActivity(new Intent(context, OasisSdkPayActivity.class));
			else
				BaseUtils.showMsg((Activity) context, getResources().getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_menu_notice_relogin")));
			trackName = ReportUtils.DEFAULTEVENT_CLICK_OG_PAY;
			break;

		default:
			break;
		}
		
		try {// 发送Mdata信息
			if(!TextUtils.isEmpty(trackName))
				ReportUtils.add(trackName, new ArrayList<String>(), new ArrayList<String>());
		} catch (Exception e) {
		}
	}
	
	public void slideview(final float pointX) {
		TranslateAnimation animation = null;
		if(pointX < screeWidth/2){
			animation = new TranslateAnimation(0, -pointX, 0, 0);
		}else{
			animation = new TranslateAnimation(0, screeWidth-logoWidth-point.x, 0, 0);
		}
	    animation.setInterpolator(new OvershootInterpolator());
	    animation.setDuration(1500);
	    animation.setStartOffset(0);
	    animation.setFillAfter(true);
	    animation.setAnimationListener(new Animation.AnimationListener() {
	        @Override
	        public void onAnimationStart(Animation animation) {
	        }
	        
	        @Override
	        public void onAnimationRepeat(Animation animation) {
	        }
	        
	        @Override
	        public void onAnimationEnd(Animation animation) {
	            
	            clearAnimation();
	            
//	            touchType = -101;
//	        	if(point.x < screeWidth/2){
//	            	point.x = 0;
//	    		}else{
//	            	point.x = screeWidth-logoWidth;
//	    		}
//	        	invalidate();// 此处不能使用postInvalidate()，不能实时刷新UI
	        	onAllAnimationsEnd(0);
	        	
	        	if(!isExpand){// OG没有展开时，才开始计算隐藏时间
		        	touchType = TOUCH_TYPE_DEFAULT;
		        	myHandler.sendEmptyMessageDelayed(handler_hide, delayMsec);
	        	}
	        }
	    });
	    
	    startAnimation(animation);
	}
	public void hideView() {
		TranslateAnimation animation = null;
		if(point.x < screeWidth/2){
			animation = new TranslateAnimation(0, -logoHideOffset, 0, 0);
		}else{
			animation = new TranslateAnimation(0, logoHideOffset, 0, 0);
		}
		animation.setInterpolator(new OvershootInterpolator());
		animation.setDuration(500);
		animation.setStartOffset(0);
		animation.setFillAfter(true);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				
				clearAnimation();
				
				touchType = TOUCH_TYPE_HIDE;
				if(point.x < screeWidth/2){
					onAllAnimationsEnd(-logoHideOffset);
				}else{
					onAllAnimationsEnd(logoHideOffset);
				}
			}
		});
		
		startAnimation(animation);
	}
	private void onAllAnimationsEnd(int offset){
		
    	if(point.x < screeWidth/2){
        	point.x = offset;
		}else{
        	point.x = screeWidth-logoWidth + offset;
		}
    	invalidate();// 此处不能使用postInvalidate()，不能实时刷新UI
	}
	class MyHandler extends Handler{
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case handler_hide:
				hideView();
				break;
			case handler_init_default:
				isExpand = false;
				touchType = TOUCH_TYPE_DEFAULT;
				postInvalidate();
				
				myHandler.sendEmptyMessageDelayed(handler_hide, delayMsec);
				break;
			case handler_loop:
				// 没隔 m_LoopTimes时间，m_CurPic加1，onDraw里就能取到对应图片进行绘制
				if (m_bStartLooper) {
					m_CurPic ++ ;
					if(m_CurPic >= m_animationPics.length)
						m_CurPic = 0;

					myHandler.sendEmptyMessageDelayed(handler_loop, m_LoopTimes);
				}
				break;

			default:
				break;
			}
		}
	}
	
	public void remenberLocation(){
		BaseUtils.saveSettingKVPtoSysCache("MENUPOINT", point.x+";"+point.y);
		for (Bitmap bitmap :
				m_animationPics) {
			if(bitmap != null && !bitmap.isRecycled()){
				bitmap.recycle();
			}
			bitmap = null;
		}
		if (bm_left != null && !bm_left.isRecycled())
			bm_left.recycle();
		bm_left = null;

		if (bm_right != null && bm_right.isRecycled())
			bm_right.recycle();
		bm_right = null;

		m_animationPics = null;
		BaseUtils.logError("Menu", "recycle done!");
	}
}
