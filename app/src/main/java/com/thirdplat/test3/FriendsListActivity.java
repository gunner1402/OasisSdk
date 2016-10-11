package com.thirdplat.test3;

import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.oasis.sdk.OASISPlatform;
import com.oasis.sdk.base.entity.FBPageInfo;
import com.oasis.sdk.base.entity.FriendInfo;
import com.thirdplat.test3.R;

public class FriendsListActivity extends ListActivity {
	public FriendsListActivity activity;
	public List<FriendInfo> friends;
	int friendType = 0;
	FBPageInfo pageinfo;
	
	String selectUser = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.list);
		
		activity = this;
		
		Bundle b = getIntent().getExtras();
		if(b!= null){
			friendType = (Integer) getIntent().getExtras().get("friendType");
			pageinfo = (FBPageInfo) getIntent().getExtras().get("fbpageinfo");
		}
		
		ListAdapter adapter = new FriendListAdapter();
        setListAdapter(adapter);

	}

	class FriendListAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			return pageinfo.data.size();
		}

		@Override
		public FriendInfo getItem(int position) {
			return pageinfo.data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHoder hoder = null;
			if(null == convertView){
				convertView = activity.getLayoutInflater().inflate(R.layout.listrow, null);
				hoder = new ViewHoder();
				hoder.paywaybg = (TextView) convertView.findViewById(R.id.friend_list_item_name);
				hoder.isselected = (CheckBox) convertView.findViewById(R.id.friend_list_item_checkbox);
				
				convertView.setTag(hoder);
			}else{
				hoder = (ViewHoder) convertView.getTag();
			}
			final FriendInfo info = getItem(position);
			hoder.paywaybg.setText(info.name);
			if(!TextUtils.isEmpty(selectUser) && selectUser.contains(info.id))
				hoder.isselected.setChecked(true);
			else
				hoder.isselected.setChecked(false);
			
			hoder.isselected.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					
					if(isChecked){
						if(!selectUser.contains(info.id))
							selectUser += ","+info.id;
					}else{
						if(selectUser.contains(info.id))
							selectUser = selectUser.replace(info.id, "");
					}
						
				}
			});
//			selectUser
			return convertView;
		}
	}
	static class ViewHoder{
		TextView paywaybg;
		CheckBox isselected;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		 
		if(friendType == 1){
			menu.add(100, 2, 1, "赠送");
			menu.add(100, 3, 2, "索要");
		}
		else
			menu.add(100, 1, 1, "邀请");
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		if(TextUtils.isEmpty(selectUser)){
			Toast.makeText(activity, "请先选择好友", Toast.LENGTH_SHORT).show();
			return true;
		}
			
		switch (item.getItemId()) {
		case 1:
			Toast.makeText(activity, "邀请", Toast.LENGTH_SHORT).show();
			OASISPlatform.setAppRequest(activity, 1, "", selectUser, "测试邀请好友");
			break;
		case 2:
			OASISPlatform.setAppRequest(activity, 2, "632964910166909", selectUser, "向好友赠送体力");// 632964910166909    430168697148051
			Toast.makeText(activity, "使用 赠送 请先确认是否配置为沙箱环境", Toast.LENGTH_LONG).show();
			break;
		case 3:
			OASISPlatform.setAppRequest(activity, 3, "430168697148051", selectUser, "向好友索要体力");
			Toast.makeText(activity, "使用 索要 请先确认是否配置为沙箱环境", Toast.LENGTH_LONG).show();
			break;

		default:
			break;
		}
		return true;
	}
}
