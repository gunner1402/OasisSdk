package com.oasis.sdk.activity;

import android.os.Bundle;

import com.oasis.sdk.base.utils.SystemCache;

public class OasisSdkBaseActivity extends OasisSdkBasesActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(SystemCache.SCREENROTATION);
	}
}
