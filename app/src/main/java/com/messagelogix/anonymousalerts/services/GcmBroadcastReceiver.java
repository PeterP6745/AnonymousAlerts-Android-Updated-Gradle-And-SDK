package com.messagelogix.anonymousalerts.services;


//public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
//
//    final private static String LOG_TAG = GcmBroadcastReceiver.class.getSimpleName();
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        String newMessage = intent.getExtras().getString("message");
//        Log.d("GCMBroadcast", "Received push now");
//        Log.d("GCMBroadcast", "newMessage: " + newMessage);
//        // Explicitly specify that GcmIntentService will handle the intent.
//        ComponentName comp = new ComponentName(context.getPackageName(),
//                GcmIntentService.class.getName());
//        // Start the service, keeping the device awake while it is launching.
//        startWakefulService(context, (intent.setComponent(comp)));
//        setResultCode(Activity.RESULT_OK);
//    }
//}
