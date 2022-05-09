package com.messagelogix.anonymousalerts.utils;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

/**
 * Created by Vahid on 6/2/2015.
 */
public class OperatingSystem {

    static Context context;

    public OperatingSystem(Context mContext) {
        context = mContext;
    }

    public String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android SDK: " + sdkVersion + " (" + release + ")";
    }

    public Boolean isTablet() {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int type = telephony.getPhoneType();
        if (type == TelephonyManager.PHONE_TYPE_NONE) {
            //It is a tablet
            return true;
        }
        //It is a cellphone
        return false;
    }

}
