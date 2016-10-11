/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oasis.sdk.base.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.base.http.CallbackResultForActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.oasis.sdk.base.entity.PhoneInfo;
import com.oasis.sdk.base.utils.BaseUtils;
import com.oasis.sdk.base.utils.Constant;
import com.oasis.sdk.base.utils.MD5Encrypt;


public class RegistrationIntentService extends IntentService {
    private static final String TAG = "RegIntentService";
    private SharedPreferences sharedPreferences = null;
    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    	
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
        	String senderid = getString(BaseUtils.getResourceValue("string", "gcm_senderid"));
        	if(TextUtils.isEmpty(senderid))
        		return;
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(senderid,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            	sendRegistrationToServer(token);
            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
//            sharedPreferences.edit().putBoolean(Constant.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.e(TAG, "Failed to complete token refresh"+e.getMessage().toString(), e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            // sharedPreferences.edit().putBoolean(Constant.SENT_TOKEN_TO_SERVER, false).apply();
        }

    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
    	if(TextUtils.isEmpty(token))
    		return;
    	HttpService.instance().setDeviceTokenForPushMessages(token, new CallbackResultForActivity(){

			@Override
			public void success(Object data, String statusCode, String msg) {
				String subscribeTopic = MD5Encrypt.StringToMD5(PhoneInfo.instance().bundleversion+PhoneInfo.instance().locale);
				sharedPreferences.edit().putString(Constant.ENDPOINT_ARN, subscribeTopic+(String)data).apply();;
				
			}

			@Override
			public void excetpion(Exception e) {
				
			}

			@Override
			public void fail(String statusCode, String msg) {
				
			}
    		
    	});
    }



}
