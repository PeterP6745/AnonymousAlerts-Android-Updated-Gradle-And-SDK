package com.messagelogix.anonymousalerts.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.activities.MessageCenterActivity;

/**
 * Created by Richard on 7/6/2017.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService{
    private static final String TAG = "MyFCMMessageService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("FCMNotif", "FROM: " + remoteMessage.getFrom());

        //check if the message contains data
        if(remoteMessage.getData().size() > 0) {
            Log.d("FCMNotif", "Message Data: " + remoteMessage.getData());
            Log.d("FCMNotif","Message: "+remoteMessage.getData().get("message"));
            sendNotification(remoteMessage.getData().get("message"));
        }

//        //check if the message contains notification
//        if(remoteMessage.getNotification() != null){
//            Log.d(TAG, "Message Body: " + remoteMessage.getNotification().getBody());
//            sendNotification(remoteMessage.getNotification().getBody());
//        }
    }

//    private void createDefaultNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//
//            if (notificationManager.getNotificationChannel("fcm_fallback_notification_channel") != null) {
//                return;
//            }
//
//            String channelName = getString(R.string.default_notification_channel_id);
//            NotificationChannel channel = new NotificationChannel(channelName, channelName, NotificationManager.IMPORTANCE_HIGH);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }

    /**
     * Display Notification
     * @param body
     */
    private void sendNotification(String body){

        updateMessages(getApplicationContext());

//       Create the NotificationChannel, but only on API 26+ because
//       the NotificationChannel class is new and not in the support library
        String NOTIFICATION_CHANNEL_ID = "replies_foreground";//getString(R.string.default_notification_channel_id);//"foreground_imadmin_channel_id";

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "replies_foreground";
            String description = "Receive notifications when an administrator responds to your reports.";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);

            //Does seemingly nothing for Android-ASUS phone
            //Does not allow it to wake from lockscreen when an FCM notification is received
            //channel.enableLights(true);
            //channel.enableVibration(true);

            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MessageCenterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /*request code*/, intent, PendingIntent.FLAG_ONE_SHOT);

        //set sound of notification
        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Message Received")
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(notificationSound)
//                .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent);

        notificationManager.notify(1/*Id of notification*/, notificationBuilder.build());
    }

    // This function will create an intent. This intent must take as parameter the "unique_name" that you registered your activity with
    static void updateMessages(Context context) {
        Intent intent = new Intent("update_messagecenter_and_chatmessages");
        //send broadcast
        context.sendBroadcast(intent);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.d("FCMNotif","inside FCM-onDeletedMessages() function for the latest message sent");
    }
}
