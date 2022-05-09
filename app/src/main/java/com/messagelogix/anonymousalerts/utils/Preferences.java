package com.messagelogix.anonymousalerts.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


/**
 * This class is a helper to store data to shared preferences
 * some sensitive values need to be encrypted in method putString()
 * You must add logic to decrypt them when retrieving them back
 * in the method getString()
 */
import com.messagelogix.anonymousalerts.crypto.RSA;

import java.util.Objects;

public class Preferences {

    public static final String SHARED_PREFERENCES = "com.messagelogix.anonymousalerts";
    public static final String RSA_GENERATED = "com.anonymousalerts.RSA_GENERATED";
    public static final String RSA_PUBLIC_KEY = "com.anonymousalerts.RSA_PUBLIC_KEY";
    public static final String RSA_PRIVATE_KEY = "com.anonymousalerts.RSA_PRIVATE_KEY";
    private static final String LOG_TAG = Preferences.class.getSimpleName();

    public static SharedPreferences mPreferences;

    public static void init(Context context) {

        mPreferences = context.getSharedPreferences(SHARED_PREFERENCES, 0);
    }


    public static String getString(String key) {
        String value = mPreferences.getString(key, "");

        switch (key){
            case Config.ACCOUNT_ID:
            case Config.UNIQUE_ID:
            case Config.DISPLAY_NAME:
                return value.equals("") ? value : RSA.decryptWithStoredKey(value);
        }
        return value;
    }

    public static void putString(String key, String s) {
        switch (key){
            case Config.ACCOUNT_ID:
            case Config.UNIQUE_ID:
            case Config.DISPLAY_NAME:
                s = RSA.encryptWithStoredKey(s);
                break;
        }
        mPreferences.edit().putString(key, s).apply();
    }



    public static boolean getBoolean(String key) {
        return mPreferences.getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {

        return mPreferences.getBoolean(key, defaultValue);
    }

    public static void putBoolean(String key, boolean bool) {
        mPreferences.edit().putBoolean(key, bool).apply();
    }

    public static void putInteger(String key, Integer integer) {
        mPreferences.edit().putInt(key, integer).apply();
    }

    public static void putLong(String key, long longValue) {
        mPreferences.edit().putLong(key, longValue).apply();
    }

    public static int getInteger(String key) {
        return mPreferences.getInt(key, 0);
    }

    public static int getInteger(String key, int defaultValue) {
        return mPreferences.getInt(key, defaultValue);
    }

    public static long getLong(String key) {
        return mPreferences.getLong(key, 0);
    }

    public static void clear() {
        mPreferences.edit().clear().apply();
    }

    public static void remove(String key) {
        mPreferences.edit().remove(key).apply();
    }

    public static boolean isAvailable(String key){
        if (mPreferences!=null){
            return mPreferences.contains(key);
        }
        return false;
    }

}
