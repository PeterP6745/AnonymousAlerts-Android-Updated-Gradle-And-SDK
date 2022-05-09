package com.messagelogix.anonymousalerts.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.firebase.iid.FirebaseInstanceId;
import com.messagelogix.anonymousalerts.BuildConfig;
import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.utils.LogUtils;
import com.messagelogix.anonymousalerts.utils.ApiHelper;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.DeviceManager;
import com.messagelogix.anonymousalerts.utils.FunctionHelper;
import com.messagelogix.anonymousalerts.utils.OperatingSystem;
import com.messagelogix.anonymousalerts.utils.Preferences;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MenuActivity extends AppCompatActivity {

    private static Context context;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String LOGO_DIRECTORY_NAME = "AnonymousLogo";
    private static final String LOG_TAG = MenuActivity.class.getSimpleName();

    private String accountId;
//    private GoogleCloudMessaging gcm;
//    private FirebaseInstanceId fcmObj;
    private String registrationDeviceId;
    private ImageView imageView;
    private File logoFile;

    private String logoName;

    public TextView hooTextView;

    private FloatingActionButton fab;

    private String hoo;

    private String lang = "en";

    private int lang_selected;
    private Resources resources;

    Locale myLocale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String language = Preferences.getString(Config.LANGUAGE);
        setLocale(language);
        //RelativeLayout show_lan_dialog;

        createDefaultNotificationChannel();

        setContext(this);

        setContentView(R.layout.activity_menu);
        setTitle(R.string.home);

        Preferences.init(context);


        imageView = (ImageView) findViewById(R.id.imageView);
        TextView accountNameTextView = (TextView) findViewById(R.id.account_textView);
        accountNameTextView.setText(Preferences.getString(Config.DISPLAY_NAME));

        hooTextView = (TextView) findViewById(R.id.hours_of_op);

        //set911 + initButtons()
        set911Dialer();
        initButtons();

        accountId = Preferences.getString(Config.ACCOUNT_ID);
        logoName = Preferences.getString(Config.LOGO_NAME);

        registerDeviceId();

        getCustomTextTask(accountId);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MenuActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Request the permission

            ActivityCompat.requestPermissions(MenuActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        } else {
            // Permission has already been granted
            displayLogo();
        }

        //displayLogo();

        //Set Preferred Language
        //setLangRecreate(Preferences.getString("langCode"));
    }

    private void createDefaultNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String name = getString(R.string.default_notification_channel_id);
            Log.d("FCMNotif","creating default notif channel");

            // Create the NotificationChannel
            String description = "While the application is in the background, receive notifications when an administrator responds to your reports.";

            NotificationChannel channel = new NotificationChannel(name, name, NotificationManager.IMPORTANCE_HIGH);

            //Does seemingly nothing for Android-ASUS phone
            //Does not allow it to wake from lockscreen when an FCM notification is received
            //channel.enableLights(true);
            //channel.enableVibration(true);
            //channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            //channel.setBypassDnd(true);

            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    displayLogo();
                    //Log.d("Hit Marker", "Permission Granted");
                }
                else
                {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // Log.d("Hit Marker", "Permission Denied");
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void set911Dialer() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

//                Uri number = Uri.parse("tel:911");
//                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
//                if (isCallingSupported(context, callIntent)) {
//                    //Send to dial pad
//                    startActivity(callIntent);
//                } else {
//                    Snackbar.make(view, "Phone call is not supported in this device", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                }

                Intent intent = new Intent(context, EmergencyDialTwoActivity.class);
                startActivity(intent);
            }
        });
    }

    private static boolean isCallingSupported(Context context, Intent intent) {
        boolean result = true;
        PackageManager manager = context.getPackageManager();
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);
        if (infos.size() <= 0) {
            result = false;
        }
        return result;
    }

    private void displayLogo() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                LOGO_DIRECTORY_NAME);

        logoFile = new File(mediaStorageDir.getPath() + File.separator + logoName);

        if (logoFile.exists()) {
            Log.d(LOG_TAG, "Found Logo loading it");
            String filePath = logoFile.getPath();
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            imageView.setImageBitmap(bitmap);
        } else {
            if (!logoName.trim().isEmpty()) {
                String imageUrl = Config.API_LOGO_URL + logoName;
                Log.d("myLog", "imageUrl = " + imageUrl);
                new DownLoadImageTask().execute(imageUrl);
            }
        }
    }

    public class DownLoadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... url) {
            return getBitmapFromURL(url[0]);
        }

        @SuppressLint("WrongThread")
        @Override
        protected void onPostExecute(Bitmap logo) {
            if (imageView != null) {
                if (logo != null) {

                    // External sdcard location
                    File mediaStorageDir = new File(
                            Environment
                                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            LOGO_DIRECTORY_NAME);

                    // Create the storage directory if it does not exist
                    if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {
                            Log.d(LOG_TAG, "Oops! Failed create "
                                    + LOGO_DIRECTORY_NAME + " directory");
                            return;
                        }
                    }
                    //create the file then write to it
                    logoFile = new File(mediaStorageDir.getPath() + File.separator + logoName);
                    FileOutputStream out = null;

                    try {
                        out = new FileOutputStream(logoFile);
                        String extension = getExtension(logoFile);
                        if (extension.toLowerCase().equals("png")) {
                            logo.compress(Bitmap.CompressFormat.PNG, 100, out);
                        } else if (extension.toLowerCase().equals("jpg") || extension.toLowerCase().equals("jpeg")) {
                            logo.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        } else {
                            logo.compress(Bitmap.CompressFormat.valueOf(extension), 100, out);
                        }

                        //
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    imageView.setImageBitmap(logo);
                    imageView.setAdjustViewBounds(true);
                } else {
                    Log.d("mylog", "logo is null");
                }
            } else {
                Log.d("mylog", "imageView is null");
            }
        }

        @Override
        protected void onCancelled() {
        }

        public Bitmap getBitmapFromURL(String src) {
            try {
                java.net.URL url = new java.net.URL(src);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    //Initializes and sets the buttons for the main page of the app
    @SuppressLint("RestrictedApi")
    public void initButtons() {
        Button liteReportButton = (Button) findViewById(R.id.SendIncidentLiteBtn);
        Button messageCenterButton = (Button) findViewById(R.id.MessageCenterBtn);
        final Button reportButton = (Button) findViewById(R.id.SendIncidentBtn);
        final Button fakeReportButton = (Button) findViewById(R.id.SendFakeIncidentBtn);
        Button glossaryButton = (Button) findViewById(R.id.GlossaryBtn);
        Button helpButton = (Button) findViewById(R.id.HelpBtn);
        Button locateMeButton = (Button) findViewById(R.id.locate_button);
        Button emergencyDial = (Button) findViewById(R.id.emergency_dial);

        final Boolean isUnlocked = Preferences.getBoolean(Config.IS_UNLOCKED);
        final Boolean hasCutoff = Preferences.getBoolean(Config.HAS_CUTOFF);
        boolean hasLite = Preferences.getBoolean(Config.HAS_LITE);
        boolean hasNonAnonLiteReports = Preferences.getBoolean(Config.HAS_NONANONYMOUS_LITEREPORTS);

        messageCenterButton.setVisibility(Preferences.getBoolean(Config.HAS_MESSAGE_CENTER) ? View.VISIBLE : View.GONE);

        if(hasLite) {
            fab.setVisibility(View.VISIBLE);
            //reportButton.setVisibility(View.GONE);
            glossaryButton.setVisibility(View.GONE);
//            locateMeButton.setVisibility(View.GONE); --> visibility set to Visibility.GONE in MenuActivity's layout file
            emergencyDial.setVisibility(View.GONE);

            //Send Lite Report
            liteReportButton.setVisibility(View.VISIBLE);

            //This if block checks to see if the account is enabled. If it is enabled then it will proceed as normal and skip
            //the block. If it is disabled, then the litebutton will be swapped out for another visually identical button but
            //this button will push the user to the OffHoursActivity instead.
            if(Preferences.getBoolean(Config.AA_APP_ENABLED) == false){
                liteReportButton.setEnabled(false);
                liteReportButton.setVisibility(View.GONE);
                fakeReportButton.setEnabled(true);
                fakeReportButton.setVisibility(View.VISIBLE);
                fakeReportButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        showOffHoursMessage();
                    }
                });
            }
            if(hasNonAnonLiteReports) {
                liteReportButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if(hasCutoff){
                            if(!isUnlocked){
                                showCutOffAlert();
                                return;
                            }
                        }
                        Intent intent = new Intent(context, NonAnonLiteReportActivity.class);
                        startActivity(intent);
                    }
                });
            } else {
                liteReportButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if(hasCutoff){
                            if(!isUnlocked){
                                showCutOffAlert();
                                return;
                            }
                        }
                        Intent intent = new Intent(context, StandardLiteReportActivity.class);
                        startActivity(intent);
                    }
                });
            }
            //Message Center
            if(messageCenterButton.getVisibility() == View.VISIBLE) {
                messageCenterButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        /*
                        if(hasCutoff){
                            if(!isUnlocked){
                                showCutOffAlert();
                                return;
                            }
                        }
                        */
                        Intent intent = new Intent(context, MessageCenterActivity.class);
                        startActivity(intent);
                    }
                });
            }
        } else {
            fab.setVisibility(View.GONE);

            liteReportButton.setVisibility(View.GONE);

            if(messageCenterButton.getVisibility() == View.VISIBLE) {
                messageCenterButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent(context, MessageCenterActivity.class);
                        startActivity(intent);
                    }
                });
            }

            //Send Report Full Version
            reportButton.setVisibility(Preferences.getBoolean(Config.HAS_INCIDENT_BUTTON) ? View.VISIBLE : View.GONE);
            if(reportButton.getVisibility() == View.VISIBLE) {

                fakeReportButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if(hasCutoff){
                            if(!isUnlocked){
                                showCutOffAlert();
                                return;
                            }
                        }
                        Intent intent = new Intent(context, SendReport1Activity.class);
                        startActivity(intent);
                    }
                });

                reportButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if(hasCutoff){
                            if(!isUnlocked){
                                showCutOffAlert();
                                return;
                            }
                        }
                        Intent intent = new Intent(context, SendReport1Activity.class);
                        startActivity(intent);
                    }
                });
            }

            //Glossary
            glossaryButton.setVisibility(Preferences.getBoolean(Config.HAS_GLOSSARY_BUTTON) ? View.VISIBLE : View.GONE);
            if(glossaryButton.getVisibility() == View.VISIBLE) {
                glossaryButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent(context, GlossaryActivity.class);
                        startActivity(intent);
                    }
                });
            }

            /**Locate me disabled for all users. Instead of deleting the code, hardcode the toggle to always hide this button*/
//            locateMeButton.setVisibility(Preferences.getBoolean(Config.HAS_SMART_BUTTON) ? View.GONE : View.GONE);
            if(locateMeButton.getVisibility() == View.VISIBLE) {
                locateMeButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent(context, SmartButtonActivity.class);
                        startActivity(intent);
                    }
                });
            }

            //Emergency Dial
            emergencyDial.setVisibility(Preferences.getBoolean(Config.HAS_EMERGENCY_BUTTON) ? View.VISIBLE : View.GONE);
            if(emergencyDial.getVisibility() == View.VISIBLE) {
                emergencyDial.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent(context, EmergencyDialTwoActivity.class);
                        startActivity(intent);
                    }
                });
            }
        }

        //Help
        helpButton.setVisibility(Preferences.getBoolean(Config.HAS_HELP_BUTTON) ? View.VISIBLE : View.GONE);
        if(helpButton.getVisibility() == View.VISIBLE) {
            helpButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(context, HelpActivity.class);
                    //Intent intent = new Intent(context, MediaActivity.class);
                    startActivity(intent);
                }
            });
        }

        //As of 10/14/21, Notifications feature has been turned off until the notification system is updated on the Incident Management app
        //Notifications
        Button notificationButton = (Button) findViewById(R.id.notificationBtn);
        notificationButton.setVisibility(Preferences.getBoolean(Config.HAS_NOTIFICATION_BUTTON) ? View.GONE : View.GONE);
        if(notificationButton.getVisibility() == View.VISIBLE) {
            notificationButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(context, NotificationsActivity.class);
                    //Intent intent = new Intent(context, MediaActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void showOffHoursMessage(){
        Intent intent = new Intent(context, OffHoursActivity.class);
        Log.d("Intent.putextra: ", hoo);
        intent.putExtra("hours", hoo);
        startActivity(intent);
    }

    private void showCutOffAlert(){
//        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
//                context);
//        alertDialog.setTitle("Unable to send reports at this time");
//        alertDialog.setMessage("You are currently outside the monitoring hours for this service. Please try again during the active hours. If this is an emergency, call 911!");
//        // alertDialog.setIcon(R.drawable.delete);
//
//        // Setting Positive "Yes" Button
//        alertDialog.setPositiveButton("Ok",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//
//        // Showing Alert Message
//        alertDialog.show();
        Intent intent = new Intent(context, OopsActivity.class);
        Log.d("Intent.putextra: ", hoo);
        intent.putExtra("hours", hoo);
        startActivity(intent);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        Log.d(LOG_TAG, "resultCode: " + resultCode);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.d(LOG_TAG, "This device is not supported.");
                finish();
            }
            Log.d(LOG_TAG, "FAIL");
            return false;
        }
        Log.d(LOG_TAG, "SUCCESS");
        return true;
    }

    private void registerDeviceId() {
        if (checkPlayServices()) {
            //check succeeds, proceed with normal processing.
            Log.d(LOG_TAG, "check succeeds, proceed with normal processing.!!!");
            //fcmObj = FirebaseInstanceId.getInstance();//GoogleCloudMessaging.getInstance(this);

            registrationDeviceId = getRegistrationId(context);

            Log.d("RegistrationProcess","About to call registerInBackground()");
            registerInBackground(accountId);
//            Log.d("RegistrationProcess","registrationDeviceId is not-empty, about to call registerUniqueId()");
//            registerUniqueId();
//            Log.d(LOG_TAG, "registrationDeviceId = " + registrationDeviceId);
        } else {
            Toast.makeText(getApplicationContext(), "No valid Google Play Services found.", Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, "No valid Google Play Services APK found.");
        }
    }

    /********************************************************/
    /**
     * Gets the current registration ID for application on GCM service.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    public String getRegistrationId(Context context) {
        Log.d("RegistrationProcess","MenuActivity-getRegistrationId()-inside method");
        String registrationId = Preferences.getString(Config.PROPERTY_REG_ID);
        if (registrationId.isEmpty()) {
            Log.d("RegistrationProcess", "MenuActivity-getRegistrationId()-Registration is empty, inside if-statement.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = Preferences.getInteger(Config.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.d(LOG_TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerUniqueId() {
        String uniqueId = Preferences.getString(Config.UNIQUE_ID);
        if (uniqueId.isEmpty()) {
            DeviceManager.init(context);
            uniqueId = DeviceManager.generateID();
            Log.d("RegistrationProcess", "uniqueId = " + uniqueId);
            Log.d("RegistrationProcess", "Now Sending...");
            registerUniqueIdTask(uniqueId);
        } else {
            Log.d("RegistrationProcess", "Device was Stored");
            Log.d("RegistrationProcess", "uniqueId = " + uniqueId);
            registerUniqueIdTask(uniqueId);
        }
    }

    private void registerUniqueIdTask(final String uniqueId){
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "redbear");
        params.put("action", "SaveUniqueIdAndroid");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("unique_id", uniqueId);//unique id
        params.put("os", getOS());//Get the operating system of the connected device
        params.put("deviceType", getDeviceType());//Get the connected device type (cellphone or tablet)
        params.put("deviceId", registrationDeviceId);//Getting a unique ID from Android devices
        params.put("android_id", deviceId);

        final ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                Log.d("FinalDataCalls","redbear-saveuniqueidandroid --> response: "+responseData);
                Log.d("[MenuActivity]","registerUniqueIdTask --> "+responseData);
                try {
                    JSONObject json = new JSONObject(responseData);
                    if (json.getBoolean(Config.SUCCESS)) {
                        Preferences.putString(Config.UNIQUE_ID, uniqueId);

                    }
                } catch (Exception e) {
                    Log.d("[MenuActivity]","registerUniqueIdTask - Catch exception --> "+e.toString());
                }
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("[MenuActivity]"," registerUniqueIdTask - onError --> "+error.toString());
            }
        });

        apiHelper.prepareRequest(params, true);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    public void registerInBackground(String accountId) {
        RegisterDeviceToServerTask registerTask = new RegisterDeviceToServerTask(context, accountId);
        registerTask.execute();
//        registerDeviceToServerTask(accountId);
    }

    public class RegisterDeviceToServerTask extends AsyncTask<String, Void, String> {
        String accountId;
        Context context;
        String deviceId;


        RegisterDeviceToServerTask(Context context, String acctId) {
            this.context = context;
            this.accountId = acctId;
        }

        @Override
        protected String doInBackground(String... url) {
            try {
                Log.d("RegistrationProcess","greencow-savedvicetoken - about to check if gcm is null");
//                if (fcmObj == null) {
//                    Log.d("RegistrationProcess","greencow-savedvicetoken - gcm is null");
////                    gcm = GoogleCloudMessaging.getInstance(context);
////                    fcmObj = FirebaseMessaging();
//                }
//                int trialCounter = 0;
                String tempDeviceToken = FirebaseInstanceId.getInstance().getToken();
                if(tempDeviceToken == null)
                    tempDeviceToken = "token_unavailable";

                this.deviceId = tempDeviceToken;//gcm.register(Config.SENDER_ID);
                LogUtils.debug("RegistrationProcess","greencow-savedevicetoken --> this.deviceId is: "+tempDeviceToken);

                HashMap<String, String> params = new HashMap<>();
                params.put("controller", "greencow");
                params.put("action", "SaveDeviceToken");
                params.put("accountId", this.accountId);
                params.put("deviceId", this.deviceId);
                params.put("deviceType", Config.ANDROID_DEVICE_TYPE_ID);
                params.put("production", BuildConfig.DEBUG ? "0" : "1");

                Log.d("RegistrationProcess","greencow-savedvicetoken params are --> "+params);
                return FunctionHelper.apiCaller(params);
            } catch (Exception ex) {
                Log.d("RegistrationProcess","greencow-savedvicetoken - encountered exception --> "+ex);
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String responseData) {
            if (responseData != null) {
                try {
                    Log.d("FinalDataCalls","greencow-savedevicetoken --> response: "+responseData);
                    Log.d("RegistrationProcess","response from greencow-savedvicetoken --> "+responseData);
                    JSONObject responseJsonObject = new JSONObject(responseData);
                    boolean success = responseJsonObject.getBoolean("success");
                    if (success) {
                        storeRegistrationId(this.context, this.deviceId);
                        registrationDeviceId = this.deviceId;
                        registerUniqueId();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                LogUtils.debug("RegistrationProcess","greencow-savedevicetoken --> inside else-statmeent - no response received");
                Log.d(LOG_TAG, "No JSON received ! :(");
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

    /*
     * gcm.register() is a method that must be executed asynchronously,
     * otherwise, app returns an IOException (MAIN_THREAD)
    */
//    private void registerDeviceToServerTask(final String accountId){
//        String deviceId = null;
//        try {
//            if (gcm == null) {
//                gcm = GoogleCloudMessaging.getInstance(context);
//            }
//            deviceId = gcm.register(Config.SENDER_ID);
//        } catch (Exception ex) {
//            Log.d("[MenuActivity]","registerDeviceToServerTask - Outter catch exception --> "+ex.toString());
//        }
//
//        if(deviceId != null) {
//            Log.d("[MenuActivity]", "registerDeviceToServerTask() - deviceId is not null");
//            HashMap<String, String> params = new HashMap<>();
//            params.put("controller", "greencow");
//            params.put("action", "SaveDeviceToken");
//            params.put("accountId", accountId);
//            params.put("deviceId", deviceId);
//            params.put("deviceType", Config.ANDROID_DEVICE_TYPE_ID);
//            params.put("production", BuildConfig.DEBUG ? "0" : "1");
//
//            final ApiHelper apiHelper = new ApiHelper();
//
//            final String finalDeviceId = deviceId;
//            apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
//                @Override
//                public void onResponse(String responseData) {
//                    Log.d("[MenuActivity2]", "registerDeviceToServerTask --> "+responseData);
//                    try {
//                        JSONObject responseJsonObject = new JSONObject(responseData);
//
//                        boolean success = responseJsonObject.getBoolean("success");
//                        if(success) {
//                            storeRegistrationId(context, finalDeviceId);
//                            registrationDeviceId = finalDeviceId;
//                            registerUniqueId();
//                        }
//                    } catch (Exception e) {
//                        Log.d("[MenuActivity]", "registerDeviceToServerTask - Catch exception --> "+e+toString());
//                    }
//                }
//            });
//
//            apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Log.d("[MenuActivity]", "registerDeviceToServerTask - onError--> "+error.toString());
//                }
//            });
//
//            apiHelper.prepareRequest(params, true);
//            ApiHelper.getInstance(this).startRequest(apiHelper);
//        }
//    }

    private void logout() {
        //Preferences.clear();
        Preferences.putBoolean(Config.IS_LOGGED_IN, false);

        if (logoFile.exists()) {
            if (!logoFile.delete()) {
                Log.d("logoutfile", "failed to delete file");
            }
            Log.d(LOG_TAG, "failed to delete file");
        }
    }

    private void updateUserSettings(final String accountId){
        final Context that = this;
        final ApiHelper apiHelper = new ApiHelper();
        //String accountId = Preferences.getString(Config.ACCOUNT_ID);
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "redbear");
        params.put("action", "GetSettings");
        params.put("accountId", accountId);

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                LogUtils.debug("[MenuActivity]", "updateUserSettings -> "+response);
                try {
                    JSONObject responseJsonObject = new JSONObject(response);
                    boolean success = responseJsonObject.getBoolean(Config.SUCCESS);
                    if(success) {
                        JSONObject data = responseJsonObject.getJSONObject(Config.DATA);

                        Preferences.putBoolean(Config.HAS_SMART_BUTTON, data.getInt(Config.HAS_SMART_BUTTON) == 1);
                        Preferences.putBoolean(Config.HAS_EMERGENCY_BUTTON, data.getInt(Config.HAS_EMERGENCY_BUTTON) == 1);
                        Preferences.putBoolean(Config.HAS_INCIDENT_BUTTON, data.getInt(Config.HAS_INCIDENT_BUTTON) == 1);
                        Preferences.putBoolean(Config.HAS_GLOSSARY_BUTTON, data.getInt(Config.HAS_GLOSSARY_BUTTON) == 1);
                        Preferences.putBoolean(Config.HAS_HELP_BUTTON, data.getInt(Config.HAS_HELP_BUTTON) == 1);
                        Preferences.putBoolean(Config.HAS_NOTIFICATION_BUTTON, data.getInt(Config.HAS_NOTIFICATION_BUTTON) == 1);
                        Preferences.putBoolean(Config.HAS_VIDEO_FEATURE, data.getInt(Config.HAS_VIDEO_FEATURE) == 1);
                        Preferences.putBoolean(Config.HAS_PHOTO_FEATURE, data.getInt(Config.HAS_PHOTO_FEATURE) == 1);
                        Preferences.putBoolean(Config.HAS_PRIORITY, data.getInt(Config.HAS_PRIORITY) == 1);

                        //Get aa_app_enabled
                        Log.e("IronMan", "AA_APP_ENABLED: " + Preferences.getBoolean(Config.AA_APP_ENABLED));
                        //System.out.println("ProfX"+responseJsonObject.getString(Config.AA_APP_ENABLED));
                        Preferences.putBoolean(Config.AA_APP_ENABLED, data.getInt(Config.AA_APP_ENABLED) == 1);

                        Preferences.putBoolean(Config.HAS_SMS, data.getInt(Config.HAS_SMS) == 1);
                        Preferences.putBoolean(Config.HAS_SUICIDE_HOTLINE, data.getInt(Config.HAS_SUICIDE_HOTLINE) == 1);
                        Preferences.putString(Config.SUICIDE_HOT_LINE, data.getString(Config.SUICIDE_HOT_LINE));
                        Preferences.putString(Config.HOT_LINE_LABEL, data.getString(Config.HOT_LINE_LABEL));
                        Preferences.putInteger(Config.LOCATOR_TIMEOUT, data.getInt(Config.LOCATOR_TIMEOUT));
                        //ANONYMOUS TOGGLE
                        Preferences.putBoolean(Config.HAS_ANON_TOGGLE, data.getInt(Config.HAS_ANON_TOGGLE) == 1);
                        //LOCATION VALIDATION
                        Preferences.putBoolean(Config.HAS_LOCATION_VALIDATION, data.getInt(Config.HAS_LOCATION_VALIDATION) == 1);
                        //MESSAGE CENTER
                        Preferences.putBoolean(Config.HAS_MESSAGE_CENTER, data.getInt(Config.HAS_MESSAGE_CENTER) == 1);
                        //AA LITE
                        Preferences.putBoolean(Config.HAS_LITE, data.getInt(Config.HAS_LITE) == 1);
                        Preferences.putBoolean(Config.HAS_NONANONYMOUS_LITEREPORTS, data.getInt(Config.HAS_NONANONYMOUS_LITEREPORTS) == 1);

                        /*////////////*/
                        //START LITE REPORT TOGGLES
                        /*///////////*/
                        Preferences.putBoolean(Config.HAS_SUBMITTER, data.getInt(Config.HAS_SUBMITTER) == 1);
                        Preferences.putBoolean(Config.HAS_SUBMITTER_FOR_LITE, data.getInt(Config.HAS_SUBMITTER_FOR_LITE) == 1);
                        //HAS REGIONS SELECTION
                        Preferences.putBoolean(Config.HAS_REGIONS, data.getInt(Config.HAS_REGIONS) == 1);
                        Preferences.putBoolean(Config.HAS_BUILDING_TYPE, data.getInt(Config.HAS_BUILDING_TYPE) == 1);
                        Preferences.putBoolean(Config.HAS_LOCATION, data.getInt(Config.HAS_LOCATION) == 1);
                        //ADD VIC/ACC FEATURE
                        Preferences.putBoolean(Config.HAS_ADD_VIC, data.getInt(Config.HAS_ADD_VIC) == 1);
                        Preferences.putBoolean(Config.HAS_INCIDENT_CATEGORIES, data.getInt(Config.HAS_INCIDENT_CATEGORIES) == 1);
                        /*////////////*/
                        //END LITE REPORT TOGGLES
                        /*///////////*/
                        
                        //Cutoff hours
                        Preferences.putBoolean(Config.HAS_CUTOFF, data.getInt(Config.HAS_CUTOFF) == 1);
                        Preferences.putBoolean(Config.IS_UNLOCKED, data.getInt(Config.IS_UNLOCKED) == 1);
                        //PAUSE FEATURE
                        Preferences.putBoolean(Config.HAS_PAUSE, data.getInt(Config.HAS_PAUSE) == 1);
                        //PAUSE TIMEOUT
                        Preferences.putInteger(Config.PAUSE_TIMEOUT, data.getInt(Config.PAUSE_TIMEOUT));

                        /**Logging for Debug**/
                        Log.e("PAUSE FEATURE", "Has Pause: " + Preferences.getBoolean(Config.HAS_PAUSE));
                        Log.e("ADD_VIC FEATURE", "Has ADD VIC/ACC: " + Preferences.getBoolean(Config.HAS_ADD_VIC));

                        //Hours of Operation Label
                        hoo = data.getString(Config.HOURS_OF_OPERATION);
                        Preferences.putString(Config.HOURS_OF_OPERATION, hoo);
                        if (hoo.length() > 5) {
                            translateMonitoringHours();
                            hooTextView.setText(hoo);
                            hooTextView.setVisibility(View.VISIBLE);
                        } else {
                            hooTextView.setVisibility(View.GONE);
                        }

                        initButtons();
                        // Log.d("building type","Has Building type: " + Preferences.getBoolean(Config.HAS_BUILDING_TYPE));
                    } else {
                        Toast.makeText(that, R.string.error_try_again, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.d("[MenuActivity1]", "updateUserSettings - Catch exception --> "+e.toString());
                    Toast.makeText(that, R.string.error_try_again, Toast.LENGTH_LONG).show();
                }
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("[MenuActivity1]","updateUserSettings - onError --> "+error.toString());
                Toast.makeText(that, R.string.error_try_again, Toast.LENGTH_LONG).show();
            }
        });

        apiHelper.prepareRequest(params, true);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void translateMonitoringHours(){
        String langCode = Preferences.getString("langCode");
        if(FunctionHelper.isNullOrEmpty(langCode)){
            langCode = "en";
        }

        if(langCode.equals("es")){
            hoo = hoo.replace("Monitoring Hours:","Horas de operación:");
            hoo = hoo.replace("Sun","Domingo");
            hoo = hoo.replace("Mon","Lunes");
            hoo = hoo.replace("Tue","Martes");
            hoo = hoo.replace("Wed","Miércoles");
            hoo = hoo.replace("Thu","Jueves");
            hoo = hoo.replace("Fri","Viernes");
            hoo = hoo.replace("Sat","Sábado");
        }

        if(langCode.equals("vi")){
            hoo = hoo.replace("Monitoring Hours:","Giám sát giờ:");
            hoo = hoo.replace("Sun","chủ nhật");
            hoo = hoo.replace("Mon","thứ hai");
            hoo = hoo.replace("Tue","thứ ba");
            hoo = hoo.replace("Wed","thứ tư");
            hoo = hoo.replace("Thu","thứ năm");
            hoo = hoo.replace("Fri","thứ sáu");
            hoo = hoo.replace("Sat","thứ bảy");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
//        if (itemId == R.id.action_logout) {
//            logout();
//            Intent intent = new Intent(this, LoginActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//            return true;
//        } else {
//            return super.onOptionsItemSelected(item);
//        }

        switch (itemId){
            case R.id.action_logout:
                logout();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.action_change_language:
                showChangeLangDialog();
                return true;
            case R.id.action_help:
                Intent intentHelp = new Intent(context, HelpActivity.class);
                startActivity(intentHelp);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Show the Language Setting dialog
     */
    public void showChangeLangDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_show_language_settings, null);
        dialogBuilder.setView(dialogView);

        final Spinner spinner1 = (Spinner) dialogView.findViewById(R.id.spinner1);

        dialogBuilder.setTitle(R.string.select_language);

        dialogBuilder.setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int langpos = spinner1.getSelectedItemPosition();
                switch(langpos) {
                    case 0: //English
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("LANG", "en").commit();
                        Preferences.putString("langCode", "en");
                        Preferences.putString(Config.LANGUAGE,"en");
                        //setLangRecreate("en");
                        //changeLang("en");
                        //setLocale("en");
                        refreshAct();
                        //recreate();
                        lang = "en";
                        return;
                    case 1: //Spanish
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("LANG", "es").commit();
                        Preferences.putString("langCode", "es");
                        Preferences.putString(Config.LANGUAGE,"es");
                        //setLangRecreate("es");
                        //changeLang("es");
                        //setLocale("es");
                        refreshAct();
                        //recreate();
                        lang = "es";
                        return;
                    case 2: //Vietnamese
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("LANG", "vi").commit();
                        Preferences.putString("langCode", "vi");
                        Preferences.putString(Config.LANGUAGE,"vi");
                        //setLangRecreate("vi");
                        //changeLang("vi");
                        //setLocale("vi");
                        refreshAct();
                        //recreate();
                        lang = "vi";
                        return;
                    /*case 3: //Arabic
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("LANG", "ar").commit();
                        setLangRecreate("ar");
                        lang = "ar";
                        return;*/
                    default: //By default set to english
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("LANG", "en").commit();
                        Preferences.putString(Config.LANGUAGE,"en");
                        //setLangRecreate("en");
                        //changeLang("en");
                        //setLocale("en");
                        refreshAct();
                        //recreate();
                        lang = "en";
                        return;
                }
            }
        });
        dialogBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    /**
     * Set the selected language and update the settings
     * @param langval
     */
    public void setLangRecreate(String langval) {
        //initButtons();
        getCustomTextTask(accountId);
        Configuration config = getBaseContext().getResources().getConfiguration();
        Locale locale = new Locale(langval);
        Locale.setDefault(locale);
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        Preferences.init(context);
        recreate();
    }

    private void refreshAct(){ //this will close and reopen the tab bar and the home activity
        finish(); //this will close the current activities opened
        startActivity(getIntent());
        Intent intent = new Intent(MenuActivity.this,MainActivity.class);
        startActivity(intent);
        //Intent i = new Intent(context, MenuActivity.class);
        //startActivity(i);
        //finish();
    }

    private void setLocale(String languageToLoad){
        Locale locale;
        if(languageToLoad.equals("not-set")){ //use any value for default
            locale = Locale.getDefault();
        }
        else {
            locale = new Locale(languageToLoad);
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        //config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        //initButtons();
        System.out.println("Spawn: " + locale);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        Log.d("RESUME","HAS RESUMED");
        updateUserSettings(accountId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "onActivityResult() called!");
        Log.d(LOG_TAG, String.format("requestCode: %s | resultCode: %s |  data: %s | ", requestCode, resultCode, data));
    }

    private String getOS() {
        Context context = getApplicationContext();
        OperatingSystem oSGetter = new OperatingSystem(context);
        String osVersion = oSGetter.getAndroidVersion();
        Log.d(LOG_TAG, "Getting device type and unique Android device ID");
        Log.d(LOG_TAG, "OS = " + osVersion);

        return osVersion;
    }

    private String getDeviceType() {
        Context context = getApplicationContext();
        //Get the operating system of the connected device
        OperatingSystem oSGetter = new OperatingSystem(context);
        //Get the connected device type (cellphone or tablet)
        String deviceType = (oSGetter.isTablet()) ? "Tablet" : "Cellphone";
        Log.d(LOG_TAG, "Device type = " + deviceType);

        return deviceType;
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        int appVersion = getAppVersion(context);
        Log.i(LOG_TAG, "Saving regId " + regId + " on app version " + appVersion);
        Preferences.putString(Config.PROPERTY_REG_ID, regId);
        Preferences.putInteger(Config.PROPERTY_APP_VERSION, appVersion);
    }

    public void getCustomTextTask(final String accountId){
        final ApiHelper apiHelper = new ApiHelper();
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "redbear");
        params.put("action", "GetCustomTextv2");
        params.put("accountId", accountId);
        params.put("langCode", Preferences.getString("langCode"));

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                Log.d("[MenuActivity]", "getCustomTextTask --> "+responseData);
                try {
                    JSONObject responseJsonObject = new JSONObject(responseData);
                    boolean success = responseJsonObject.getBoolean("success");
                    if (success) {
                        JSONObject data = responseJsonObject.getJSONObject("data");

                        Preferences.putString(Config.LOGO_NAME, data.getString(Config.LOGO_NAME));
                        Preferences.putString(Config.DISPLAY_NAME, data.getString(Config.DISPLAY_NAME));
                        Preferences.putString(Config.STEP1_INSTRUCTION, data.getString(Config.STEP1_INSTRUCTION));
                        Preferences.putString(Config.STEP1_COMPLETE_FORM, data.getString(Config.STEP1_COMPLETE_FORM));
                        Preferences.putString(Config.STEP2_CONTACT, data.getString(Config.STEP2_CONTACT));
                        Preferences.putString(Config.STEP2_FILL_OUT_FORM, data.getString(Config.STEP2_FILL_OUT_FORM));
                        Preferences.putString(Config.STEP3_MESSAGE_SENT, data.getString(Config.STEP3_MESSAGE_SENT));
                        Preferences.putString(Config.STEP3_EMERGENCY_NUMBER, data.getString(Config.STEP3_EMERGENCY_NUMBER));
                        Preferences.putString(Config.STEP3_OTHER_NUMBERS, data.getString(Config.STEP3_OTHER_NUMBERS));
                        Preferences.putString(Config.BUILDING_NAMING, data.getString(Config.BUILDING_NAMING));

                        //HTML
                        Preferences.putString(Config.STEP1_COMPLETE_FORM_HTML, data.getString(Config.STEP1_COMPLETE_FORM_HTML));
                        Preferences.putString(Config.STEP2_CONTACT_HTML, data.getString(Config.STEP2_CONTACT_HTML));
                        Preferences.putString(Config.STEP2_FILL_OUT_FORM_HTML, data.getString(Config.STEP2_FILL_OUT_FORM_HTML));
                        Preferences.putString(Config.STEP3_MESSAGE_SENT_HTML, data.getString(Config.STEP3_MESSAGE_SENT_HTML));
                        Preferences.putString(Config.STEP3_EMERGENCY_NUMBER_HTML, data.getString(Config.STEP3_EMERGENCY_NUMBER_HTML));
                        Preferences.putString(Config.STEP3_OTHER_NUMBERS_HTML, data.getString(Config.STEP3_OTHER_NUMBERS_HTML));

                        //Intent intent = new Intent(context, MainActivity.class);
                        //startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.d("[MenuActivity]", "getCustomTextTask - Catch exception --> "+e.toString());
                }
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("[MenuActivity]", "getCustomTextTask - onError --> "+error.toString());
            }
        });

        apiHelper.prepareRequest(params, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    @Override
    public void onBackPressed() {
        //Override default onBackPressed interaction - does nothing
        //Prevents user from navigating back to login screen by pressing back button
    }

    public static Context getContext() {
        return context;
    }
    public void setContext(Context context) {
        this.context = context;
    }
}
