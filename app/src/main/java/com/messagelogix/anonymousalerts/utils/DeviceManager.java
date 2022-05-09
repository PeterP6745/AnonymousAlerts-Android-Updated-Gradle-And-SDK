package com.messagelogix.anonymousalerts.utils;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class DeviceManager {

    static Context context;

    public static void init(Context mContext) {
        context = mContext;
    }

    // generate a unique uniqueDeviceIdentifier for each device
    // use available schemes if possible / generate a random signature instead
    public static String generateID() {

        // use the ANDROID_ID constant, generated at the first device boot
        String deviceId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
       // Log.d("","Device ID at first: " + deviceId);

        // in case known problems are occured
        if ("9774d56d682e549c".equals(deviceId) || deviceId == null) {

            // get a unique deviceID like IMEI for GSM or ESN for CDMA phones
            // don't forget:
            //    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
            deviceId = ((TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE))
                    .getDeviceId();

        //    Log.d("","Device ID at second: " + deviceId);

            // if nothing else works, generate a random number
            if (deviceId == null) {

                Random tmpRand = new Random();
                deviceId = String.valueOf(tmpRand.nextLong());

            //    Log.d("","Device ID at last resort: " + deviceId);
            }

        }

        //store the device id in preferences to send to server later
        Preferences.putString(Config.ANDROID_ID, deviceId);
        // any value is hashed to have consistent format
        return getHash(deviceId);
    }

    // generates a SHA-1 hash for any string
    public static String getHash(String stringToHash) {

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] result = null;

        try {
            result = digest.digest(stringToHash.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();

        for (byte b : result) {
            sb.append(String.format("%02X", b));
        }

        String messageDigest = sb.toString();
        return messageDigest;
    }

}
