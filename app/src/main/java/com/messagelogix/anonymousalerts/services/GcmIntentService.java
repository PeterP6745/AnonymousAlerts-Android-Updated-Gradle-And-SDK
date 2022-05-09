package com.messagelogix.anonymousalerts.services;

import androidx.core.app.NotificationCompat;

//import com.google.android.gms.gcm.GoogleCloudMessaging;


/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
//public class GcmIntentService extends IntentService {
//    public static final int NOTIFICATION_ID = 1;
//    private NotificationManager mNotificationManager;
//    NotificationCompat.Builder builder;
//
//    public GcmIntentService() {
//        super("GcmIntentService");
//    }
//
//    public static final String TAG = "myLog";
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//        Bundle extras = intent.getExtras();
//        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
//        // The getMessageType() intent parameter must be the intent you received
//        // in your BroadcastReceiver.
//        String messageType = gcm.getMessageType(intent);
//
//        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
//            /*
//			 * Filter messages based on message type. Since it is likely that GCM will be
//			 * extended in the future with new message types, just ignore any message types you're
//			 * not interested in, or that you don't recognize.
//			 */
//            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
//                sendNotification("Send error: " + extras.toString());
//            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
//                sendNotification("Deleted messages on server: " + extras.toString());
//                // If it's a regular GCM message, do some work.
//            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
//                // This loop represents the service doing some work.
//                for (int i = 0; i < 5; i++) {
//                    Log.i(TAG, "Working... " + (i + 1)
//                            + "/5 @ " + SystemClock.elapsedRealtime());
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                    }
//                }
//                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
//                // Post notification of received message.
//                String newMessage = extras.getString("message");
//                String mType = extras.getString("type");
//
//                if(!FunctionHelper.isNullOrEmpty(mType)){
//                    if(mType.equals("3")){
//                        return;
//                    }
//                }
//                sendNotification(newMessage);
//                Log.i(TAG, "Received: " + extras.toString());
//            }
//        }
//        // Release the wake lock provided by the WakefulBroadcastReceiver.
//        GcmBroadcastReceiver.completeWakefulIntent(intent);
//    }
//
//    // Put the message into a notification and post it.
//    // This is just one simple example of what you might choose to do with
//    // a GCM message.
//    private void sendNotification(String msg) {
//        mNotificationManager = (NotificationManager)
//                this.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, MenuActivity.class), 0);
//
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.drawable.ic_launcher)
//                        .setContentTitle("Anonymous Alerts Notification")
//                        .setStyle(new NotificationCompat.BigTextStyle()
//                                .bigText(msg))
//                        .setAutoCancel(true)
//                        .setContentText(msg);
//
//        mBuilder.setContentIntent(contentIntent);
//        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
//    }
//}
