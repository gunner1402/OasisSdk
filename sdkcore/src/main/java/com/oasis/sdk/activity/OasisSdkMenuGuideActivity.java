package com.oasis.sdk.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oasis.sdk.OASISPlatfromMenu;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.Constant;
import com.oasis.sdk.base.utils.DisplayUtil;
import com.oasis.sdk.base.utils.SystemCache;
/**
 * OG菜单引导
 * @author xdb
 *
 */
public class OasisSdkMenuGuideActivity extends OasisSdkBaseActivity {
	public static final String TAG = OasisSdkMenuGuideActivity.class.getName();
	public static int[] itemTexts = new int[]{
		BaseUtils.getResourceValue("string", "oasisgames_sdk_pcenter_notice_2"),
		BaseUtils.getResourceValue("string", "oasisgames_sdk_head_title_charge"),
		BaseUtils.getResourceValue("string", "oasisgames_sdk_pcenter_notice_1"),
		BaseUtils.getResourceValue("string", "oasisgames_sdk_guide_notice4"),
		BaseUtils.getResourceValue("string", "oasisgames_sdk_guide_notice5")
		};
	boolean[] showFlag;
	LinearLayout menuLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_menu_guide"));
		
		FrameLayout rootLayout = (FrameLayout)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_menu_guide"));
		rootLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SystemCache.menu.setVisibility(View.VISIBLE);
				finish();
			}
		});
		showFlag = getIntent().getBooleanArrayExtra("showFlag");
		List<Integer> showTexts = new ArrayList<Integer>();
		
		menuLayout = (LinearLayout)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_menu_guide_layout"));
		TextView item = null;
		LinearLayout itemParent = null;
		for (int i = 0; i < showFlag.length; i++) {
			if(showFlag[i]){
				item = new TextView(this.getApplicationContext());
				itemParent = new LinearLayout(this.getApplicationContext());
				item.setBackgroundResource(OASISPlatfromMenu.itemImages[i]);
				itemParent.addView(item);
				itemParent.setPadding(DisplayUtil.dip2px(6, BaseUtils.getDensity()), 0, 0, 0);
				itemParent.setGravity(Gravity.CENTER);
				menuLayout.addView(itemParent);
				
				showTexts.add(itemTexts[i]);
			}
		}
		int i = 0;
		for (i = 0; i < showFlag.length; i++) {
			RelativeLayout layout = (RelativeLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_menu_guide_layout_notice"+i));
			if(i < showTexts.size()){
				TextView tv = (TextView) layout.getChildAt(1);
				String notice = getResources().getString(showTexts.get(i));
				notice = notice.replace(" ", "\n");
				tv.setText(notice);
			}else
				layout.setVisibility(View.INVISIBLE);
		}
		setWaitScreen(false);
		
		((TextView)findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_menu_guide_version"))).setText("SDK V"+Constant.SDKVERSION);
	}

	@Override
	protected void onDestroy() {
		SystemCache.menu.setVisibility(View.VISIBLE);
		super.onDestroy();
	}
	
}
