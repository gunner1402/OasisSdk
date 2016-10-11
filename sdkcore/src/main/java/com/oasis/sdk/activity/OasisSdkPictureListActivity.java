package com.oasis.sdk.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oasis.sdk.base.list.PictureListAdapter;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.DisplayUtil;
/**
 * 图片列表  Environment.DIRECTORY_PICTURES  Environment.DIRECTORY_DCIM
 * @author xdb
 */
public class OasisSdkPictureListActivity extends OasisSdkBaseActivity {

	final String TAG = OasisSdkPictureListActivity.class.getName();
	static int MAXSELECT = 9;
	
	List<String> pics = null;
	public List<String> picSelected = new ArrayList<String>();// 已选择图片
	TextView tv_previewnum;// 预览条数
	TextView tv_send;// 发送条数
	
	RecyclerView gv_all;
    private GridLayoutManager mLayoutManager;
	PictureListAdapter adapter;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(BaseUtils.getResourceValue("layout", "oasisgames_sdk_picture_list"));
		initHead(true, null, false, getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_customer_notice9")));
		
		init();

		loadData();
	}
	
	public void loadData(){
		File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		searchImg(f);
		f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		searchImg(f);
		
		updateViewForNum();
		
		updateAdapter();
	}
	private void searchImg(File f){
		if (f.isHidden())
			return;
		if (pics == null)
			pics = new ArrayList<String>();
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			for (File file : files) {
				searchImg(file);
			}
		}
		String path = f.getAbsolutePath();
		if (path.endsWith(".jpg") || path.endsWith(".jpeg")
				|| path.endsWith(".JPG") || path.endsWith(".JPEG")
				|| path.endsWith(".png") || path.endsWith(".PNG"))
			pics.add(f.getAbsolutePath());
		
	}
	/**
	 * 更新adapter，刷新UI
	 * @param type
	 */
	private void updateAdapter(){
		adapter = new PictureListAdapter(this, pics, 1, null);
		gv_all.setAdapter(adapter);
		adapter.setOnItemClickListener(new PictureListAdapter.OnRecyclerViewItemClickListener() {
		    @Override
		    public void onItemClick(View view, String path) {
				
				TextView tv_selected = (TextView) view.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pictrue_list_item_selected"));
				if(picSelected.contains(path)){
					picSelected.remove(path);
					tv_selected.setVisibility(View.INVISIBLE);
				}else{
					if(picSelected.size() < MAXSELECT){
						picSelected.add(path);
						tv_selected.setVisibility(View.VISIBLE);
					}
				}
					
				updateViewForNum();
		    }
		});

		adapter.notifyDataSetChanged();
	}
	public void updateViewForNum(){
		tv_send.setText(getString(BaseUtils.getResourceValue("string", "oasisgames_sdk_customer_notice11")) + "("+picSelected.size()+"/"+MAXSELECT+")");
		tv_previewnum.setVisibility(picSelected.size() > 0?View.VISIBLE:View.INVISIBLE);
		tv_previewnum.setText(""+picSelected.size());
	}
	private void init(){
		LinearLayout btn_chargePC = (LinearLayout) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_common_head_function"));
		btn_chargePC.setVisibility(View.VISIBLE);
		tv_send = (TextView) btn_chargePC.getChildAt(0);
		
		tv_send.setBackgroundResource(BaseUtils.getResourceValue("drawable", "oasisgames_sdk_common_00aed9_017baa"));
		tv_send.setTextSize(18);
		tv_send.setTextColor(Color.parseColor("#FFFFFF"));
		tv_send.setPadding(20, 10, 20, 10);
		btn_chargePC.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(picSelected == null || picSelected.size() <= 0)
					return;
				setResult(Activity.RESULT_OK, new Intent().putStringArrayListExtra("data", (ArrayList<String>) picSelected));
				finish();
			}
		});
		
		
		tv_previewnum = (TextView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_picture_func_previewnum"));
		
		gv_all = (RecyclerView) findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_picture_list"));
		int size = BaseUtils.getDisplayMetrics(this).widthPixels / DisplayUtil.dip2px(200, BaseUtils.getDensity());
		mLayoutManager = new GridLayoutManager(this, size);
		gv_all.setLayoutManager(mLayoutManager);

//		gv_all.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id) {
//				String path = pics.get(position);
//				
//				TextView tv_selected = (TextView) view.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pictrue_list_item_selected"));
//				if(picSelected.contains(path)){
//					picSelected.remove(path);
//					tv_selected.setVisibility(View.INVISIBLE);
//				}else{
//					if(picSelected.size() < MAXSELECT){
//						picSelected.add(path);
//						tv_selected.setVisibility(View.VISIBLE);
//					}
//				}
//					
//				updateViewForNum();
//			}
//		});
//		gv_all.setRecyclerListener(new RecyclerListener() {
//			
//			@Override
//			public void onMovedToScrapHeap(View view) {
//				com.oasis.sdk.base.utils.MyImageView iv = (com.oasis.sdk.base.utils.MyImageView) view.findViewById(BaseUtils.getResourceValue("id", "oasisgames_sdk_pictrue_list_item_img"));
//				Bitmap b = iv.getBitmap();
//				if(b != null)
//					b.recycle();
//				b = null;
//			}
//		});
		
	}

	public void onClickPreview(View v){
		if(picSelected == null || picSelected.size() <= 0)
			return;
		startActivity(new Intent().setClass(this.getApplicationContext(), OasisSdkPictureGalleryActivity.class).putStringArrayListExtra("data", (ArrayList<String>) picSelected));
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if(pics != null)
			pics.clear();
		if(picSelected != null)
			picSelected.clear();
		super.onDestroy();
	}
	
	public final static String[] imageThumbUrls = new String[] {
		"http://www.yzdsb.com.cn/pic/0/11/65/53/11655321_951970.jpg",
		"http://sd.china.com.cn/uploadfile/2015/0623/20150623021220623.jpg",
		"https://www.baidu.com/img/bd_logo1.png", 
		"http://img.my.csdn.net/uploads/201407/26/1406383299_1976.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383291_6518.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383291_8239.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383290_9329.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383290_1042.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383275_3977.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383265_8550.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383264_3954.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383264_4787.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383264_8243.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383248_3693.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383243_5120.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383242_3127.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383242_9576.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383242_1721.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383219_5806.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383214_7794.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383213_4418.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383213_3557.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383210_8779.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383172_4577.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383166_3407.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383166_2224.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383166_7301.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383165_7197.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383150_8410.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383131_3736.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383130_5094.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383130_7393.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383129_8813.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383100_3554.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383093_7894.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383092_2432.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383092_3071.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383091_3119.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383059_6589.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383059_8814.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383059_2237.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383058_4330.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406383038_3602.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382942_3079.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382942_8125.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382942_4881.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382941_4559.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382941_3845.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382924_8955.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382923_2141.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382923_8437.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382922_6166.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382922_4843.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382905_5804.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382904_3362.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382904_2312.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382904_4960.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382900_2418.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382881_4490.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382881_5935.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382880_3865.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382880_4662.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382879_2553.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382862_5375.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382862_1748.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382861_7618.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382861_8606.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382861_8949.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382841_9821.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382840_6603.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382840_2405.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382840_6354.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382839_5779.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382810_7578.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382810_2436.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382809_3883.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382809_6269.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382808_4179.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382790_8326.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382789_7174.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382789_5170.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382789_4118.jpg", /*
		"http://img.my.csdn.net/uploads/201407/26/1406382788_9532.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382767_3184.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382767_4772.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382766_4924.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382766_5762.jpg", 
		"http://img.my.csdn.net/uploads/201407/26/1406382765_7341.jpg", */};
}