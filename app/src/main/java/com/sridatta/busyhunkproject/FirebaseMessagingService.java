package com.sridatta.busyhunkproject;
import android.app.Notification;
import android.app.NotificationManager;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

@Override
public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);



        //notification builer
        Notification.Builder builder=new Notification.Builder(this)
        .setSmallIcon(R.mipmap.ic_frog)
        .setContentText("Friend Request")
        .setContentText("You have got a friend request");

        int notificationId=(int)System.currentTimeMillis();

        NotificationManager manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        manager.notify(notificationId,builder.build());
        }
}
