package com.messagelogix.anonymousalerts.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.messagelogix.anonymousalerts.utils.LogUtils;

/**
 * Created by Richard on 7/6/2017.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "FireBaseIIdService";

    @Override
    public void onTokenRefresh(){
        LogUtils.debug("RegistrationProcess", "myfirebaseinstanceidservice - inside onTokenRefresh()");
        //Get updated token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        LogUtils.debug("RegistrationProcess", "myfirebaseinstanceidservice - onTokenRefresh() - New Token: " + refreshedToken);

        //Save the token
    }
}


