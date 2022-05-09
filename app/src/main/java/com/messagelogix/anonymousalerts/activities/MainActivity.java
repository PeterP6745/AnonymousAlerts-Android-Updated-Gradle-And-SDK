package com.messagelogix.anonymousalerts.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.messagelogix.anonymousalerts.BuildConfig;
import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.crypto.Crypto;
import com.messagelogix.anonymousalerts.crypto.RSA;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.Preferences;
import com.messagelogix.anonymousalerts.utils.Strings;

import java.security.KeyPair;


public class MainActivity extends Activity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Preferences.init(context);
        if (Strings.isNullOrEmpty(Preferences.getString(Preferences.RSA_PUBLIC_KEY)) ||
                Strings.isNullOrEmpty(Preferences.getString(Preferences.RSA_PRIVATE_KEY))) {
            KeyPair keyPair = RSA.generate();
            Crypto.writePublicKeyToPreferences(keyPair);
            Crypto.writePrivateKeyToPreferences(keyPair);
        }

        Thread logoTimer = new Thread() {
            public void run() {
                try {
                    sleep(1500);
                    Intent intent;
                    if (userIsAuthenticated() && !appIsUpdated()) {
                        intent = new Intent(context, MenuActivity.class);
                    } else {
                        intent = new Intent(context, LoginActivity.class);
                    }
                    startActivity(intent);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    finish();
                }
            }

        };
        logoTimer.start();
    }

    protected boolean userIsAuthenticated() {
        return Preferences.getBoolean(Config.IS_LOGGED_IN);
    }

    protected boolean appIsUpdated() {
        int lastVersionCode = Preferences.getInteger(Config.LAST_VERSION_CODE);
        return lastVersionCode < BuildConfig.VERSION_CODE;
    }
}

























