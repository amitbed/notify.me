package com.notifyapp.amitbed.notifyme;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.notifyapp.amitbed.nudges.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by amitbed on 08/10/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String,String> data = remoteMessage.getData();
        String title = data.get("title");
        String subTitle = data.get("subTitle");
        if (title.isEmpty() || subTitle.isEmpty()){
            Log.d("Errors","title or subtitle is empty");
            return;
        }
        String message = data.get("message");
        String senderPhoneNumber = data.get("sender");
        Uri cta = Uri.parse("nudgesapp://nudges/message");
        showNotification(getApplicationContext(),generatedNumber(senderPhoneNumber) ,title,subTitle, message, senderPhoneNumber, cta);
    }

    private static int generatedNumber(String senderPhoneNumber) {
        int rand =(int) (Math.random() * 1000);
        return Integer.parseInt(senderPhoneNumber.substring(4)) + rand;
    }

    private void showNotification(Context context, int id, String title, String subTitle, String message, String sender, Uri deepLink) {
        Intent notiIntent;
        if (deepLink == null) {
            notiIntent = new Intent(context, MainActivity.class);
        }else{
            notiIntent = new Intent(Intent.ACTION_VIEW, deepLink);
        }
        NotificationManager notiManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,"")
                .setContentTitle(title)
                .setContentText(subTitle)
                .setSmallIcon(R.drawable.notification_logo)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true);
        notiIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notiIntent.putExtra("noti_title", title);
        notiIntent.putExtra("noti_sub_title", subTitle);
        notiIntent.putExtra("noti_message", message);
        notiIntent.putExtra("noti_sender", sender);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, id, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);
        Notification notification = notificationBuilder.build();
        notification.defaults |= Notification.DEFAULT_ALL; // set default sound/ vibrate/ lights
        notiManager.notify(id, notification);
    }
}
