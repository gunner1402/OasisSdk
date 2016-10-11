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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;



public class MyGcmListenerService extends GcmListenerService {

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(data);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(Bundle data) {
    	Intent clickIntent = new Intent(this, com.oasis.sdk.base.communication.NotificationClickReceiver.class); //点击通知之后要发送的广播
    	clickIntent.putExtras(data);
    	int notificationID = (int) (System.currentTimeMillis() / 1000);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), notificationID, clickIntent, PendingIntent.FLAG_ONE_SHOT);
    	
        String message = data.getString("message");
    	String pkName = this.getPackageName();
    	PackageManager packageManager = this.getPackageManager();   

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        ApplicationInfo applicationInfo = null;
		try {
			applicationInfo = packageManager.getApplicationInfo(pkName, 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			applicationInfo = getApplicationInfo();
		}
        String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
        int appicon = applicationInfo.icon;
        
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText(message);
        style.setBigContentTitle(applicationName);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(appicon)
                .setContentTitle(applicationName)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(style);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID /* ID of notification */, notificationBuilder.build());
    }
}
