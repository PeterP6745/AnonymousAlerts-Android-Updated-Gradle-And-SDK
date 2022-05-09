package com.messagelogix.anonymousalerts.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.utils.LogUtils;
import com.messagelogix.anonymousalerts.model.Report;
import com.messagelogix.anonymousalerts.model.SpinnerItem;
import com.messagelogix.anonymousalerts.utils.ApiHelper;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class SendReport1Activity extends Activity {

    final Context context = this;

    private static final String LOG_TAG = SendReport1Activity.class.getSimpleName();

    private Spinner spinnerSubmitter;
    private Spinner spinnerLocation;
    private Spinner spinnerPriority;
    private Spinner SpinnerContact;
    private Spinner SpinnerBuilding;
    private Spinner SpinnerIncident;
    private Spinner spinnerBuildingType;
    private Button vicButton;

    private TextView incidentTextView;
    private TextView contactTextView;
    private TextView vicTextView;
    private EditText messageEditText;
    private String confirmationCode = null;
    private int alertId = 0;
    private boolean hasAnonymousCheckbox, isAnonymous, hasPhotoFeature;//, hasVideoFeature;


    private LocationManager locationManager;
    public GpsListener listener;
    public double mLongitude, mLatitude;

    private String buildingNaming;
    private String buildingTypeId = "0";

    private Report report = new Report();


    private String vNameReturnString = "";
    private String aNameReturnString = "";

    private String vGradeReturnString = "";
    private String aGradeReturnString = "";

    private int vGradeReturnInt = 0;
    private int aGradeReturnInt = 0;


    private static final int ADD_VIC_ACTIVITY_RESULT_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_report1);

        Preferences.init(context);

        setTitle(getString(R.string.title_activity_send_report1));
        setTouchListenerForKeyboardDismissal();
        buildActionBar();

        messageEditText = (EditText) findViewById(R.id.messageToSend);
        messageEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        messageEditText.setHorizontallyScrolling(false);
        messageEditText.setMaxLines(20);
        messageEditText.clearFocus();

        //Build SpinnerItems
        TextView buildingTextView = (TextView) findViewById(R.id.building_textView);
        SpinnerBuilding = (Spinner) findViewById(R.id.BuildingSpinner);
        //Building Type
        TextView buildingTypeTextView = (TextView) findViewById(R.id.building_type_textView);
        spinnerBuildingType = (Spinner) findViewById(R.id.BuildingTypeSpinner);
        //Victim/Accused
        vicTextView = (TextView) findViewById(R.id.add_vic_textview);
        vicButton = (Button) findViewById(R.id.AddVictimButton);
        //Contact
        SpinnerContact = (Spinner) findViewById(R.id.ContactSpinner);
        contactTextView = (TextView) findViewById(R.id.contact_textView);
        //Incident
        SpinnerIncident = (Spinner) findViewById(R.id.IncidentSpinner);
        incidentTextView = (TextView) findViewById(R.id.incident_textView);
        //submitter
        spinnerSubmitter = (Spinner) findViewById(R.id.SubmitterSpinner);
        TextView submitterTextView = (TextView) findViewById(R.id.submitter_textView);
        //Location
        spinnerLocation = (Spinner) findViewById(R.id.LocationSpinner);
        TextView locationTextView = (TextView) findViewById(R.id.location_textView);
        //Priority
        spinnerPriority = (Spinner) findViewById(R.id.prioritySpinner);
        TextView priorityTextView = (TextView) findViewById(R.id.priority_textView);
        //Message Textview
        TextView instructionTextView = (TextView) findViewById(R.id.InstructionTextView1);
        instructionTextView.setText(Html.fromHtml(Preferences.getString(Config.STEP1_COMPLETE_FORM_HTML)));

        /**Anonymous Toggle Layout*/
        hasAnonymousCheckbox = Preferences.getBoolean(Config.HAS_ANON_TOGGLE);
        final LinearLayout anonSliderLayout = (LinearLayout) findViewById(R.id.anonymousToggleLayout);
        //anonSliderLayout.setVisibility(hasAnonymousCheckbox ? View.VISIBLE : View.GONE);
        //if has anonymous checkbox
        if(hasAnonymousCheckbox){
            anonSliderLayout.setVisibility(View.VISIBLE);
            final CheckBox checkBox = (CheckBox) findViewById(R.id.anonymous_checkbox);
            checkBox.setChecked(true);
            isAnonymous = true;
            //set onClickListener to checkbox
            checkBox.setOnClickListener(new OnClickListener() {

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View v) {
                    if(isAnonymous){
                        checkBox.setChecked(false);
                        isAnonymous = false;
                        anonSliderLayout.setBackground(getResources().getDrawable(R.drawable.bg_step1_anonymous_toggle_off));
                    }
                    else{
                        checkBox.setChecked(true);
                        isAnonymous = true;
                        anonSliderLayout.setBackground(getResources().getDrawable(R.drawable.bg_step1_anonymous_toggle_active));
                    }
                }
            });


            //set onclick listener to layout as well
            anonSliderLayout.setOnClickListener(new OnClickListener() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View v) {
                    if(isAnonymous){
                        checkBox.setChecked(false);
                        isAnonymous = false;
                        anonSliderLayout.setBackground(getResources().getDrawable(R.drawable.bg_step1_anonymous_toggle_off));
                    }
                    else{
                        checkBox.setChecked(true);
                        isAnonymous = true;
                        anonSliderLayout.setBackground(getResources().getDrawable(R.drawable.bg_step1_anonymous_toggle_active));
                    }
                }
            });
        }
        else{
            anonSliderLayout.setVisibility(View.GONE);
        }

        hasPhotoFeature = Preferences.getBoolean(Config.HAS_PHOTO_FEATURE);
//        hasVideoFeature = Preferences.getBoolean(Config.HAS_VIDEO_FEATURE);
        buildingNaming = Preferences.getString(Config.BUILDING_NAMING);
        String buildingNameString = "*"+ buildingNaming;

        if(buildingNaming.equals("School")){
            buildingNameString = "*" + getString(R.string.school);
        }
        else if(buildingNaming.equals(getString(R.string.building))){
            buildingNameString = "*" + getString(R.string.building);
        }
        buildingTextView.setText(buildingNameString);

        //Toggles
        boolean hasBuildingType = Preferences.getBoolean(Config.HAS_BUILDING_TYPE);
        boolean hasSubmitter = Preferences.getBoolean(Config.HAS_SUBMITTER);
        boolean hasPriority = Preferences.getBoolean(Config.HAS_PRIORITY);
        boolean hasLocation = Preferences.getBoolean(Config.HAS_LOCATION);
        boolean hasVicAcc = Preferences.getBoolean(Config.HAS_ADD_VIC);
        Log.d("getbuilding","not in function yuet");
        if(hasBuildingType){
            Log.d("getbuilding","in the has building");

            buildingTypeTextView.setVisibility(View.VISIBLE);
            spinnerBuildingType.setVisibility(View.VISIBLE);
            getBuildingTypeTask();
            getSchoolBuildingsTask(buildingTypeId);
        }
        else{
            getSchoolBuildingsTask(buildingTypeId);
        }

        if (!hasSubmitter) {
            spinnerSubmitter.setVisibility(View.GONE);
            submitterTextView.setVisibility(View.GONE);
        } else {
            getSubmitterTask();
        }

        if (!hasPriority) {
            spinnerPriority.setVisibility(View.GONE);
            priorityTextView.setVisibility(View.GONE);
        } else {
            new GetPriorityTask().execute();
        }

        if (!hasLocation) {
            spinnerLocation.setVisibility(View.GONE);
            locationTextView.setVisibility(View.GONE);
        } else {
            getLocationTask();
        }

        if(!hasVicAcc) {
            vicButton.setVisibility(View.GONE);
            vicTextView.setVisibility(View.GONE);
        } else {
            /** VICTIM/ACCUSED BUTTON ONCLICK **/
            vicButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, VictimAccusedActivity.class);
                    intent.putExtra("victim_name", vNameReturnString);
                    intent.putExtra("accused_name", aNameReturnString);
                    intent.putExtra("victim_grade", vGradeReturnString);
                    intent.putExtra("accused_grade", aGradeReturnString);
                    intent.putExtra("victim_grade_index", vGradeReturnInt);
                    intent.putExtra("accused_grade_index", aGradeReturnInt);
                    startActivityForResult(intent, ADD_VIC_ACTIVITY_RESULT_CODE);
                }
            });
        }

        //check for location permission
        if(checkLocationPermission()){
            //THIS CODE WILL CRASH IF RUN WITHOUT LOCATION PERMISSION!
            getUserCoordinates();
        }
        else{
            LogUtils.debug("CameraProcess","requesting permissions in SendReportActivity");
            //request location permission
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION/*, Manifest.permission.READ_EXTERNAL_STORAGE*/}, 1);
        }

        addListenerOnSubmitButton();
        //  getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        // Log.d("DeviceToken","Device Token: "+Preferences.getString(Config.PROPERTY_REG_ID));
        // Log.d("DeviceToken","Unique ID: "+Preferences.getString(Config.UNIQUE_ID));
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    getUserCoordinates();
                    //Log.d("Hit Marker", "Permission Granted");
                }

//                if(grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                    LogUtils.debug("CameraProcess","read_external_storage permission was granted");
//                }

                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);

        LogUtils.debug("CameraProcess","checking location permission --> res is: "+res);

        return (res == PackageManager.PERMISSION_GRANTED);
    }

//    public boolean checkReadExtStoragePermission() {
//        String permission = "android.permission.READ_EXTERNAL_STORAGE";
//        int res = this.checkCallingOrSelfPermission(permission);
//
//        LogUtils.debug("CameraProcess","checking read external storage permission --> res is: "+res);
//
//        return (res == PackageManager.PERMISSION_GRANTED);
//    }

    /**Victim/Accused Return Method**/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check that it is the SecondActivity with an OK result
        if (requestCode == ADD_VIC_ACTIVITY_RESULT_CODE) {
            if (resultCode == RESULT_OK) {

                // get String data from Intent
                vNameReturnString = data.getStringExtra("victim_name");
                aNameReturnString = data.getStringExtra("accused_name");
                vGradeReturnString = data.getStringExtra("victim_grade");
                aGradeReturnString = data.getStringExtra("accused_grade");
                vGradeReturnInt = data.getIntExtra("victim_grade_index", 0);
                aGradeReturnInt = data.getIntExtra("accused_grade_index", 0);

                boolean hasVicName = isNotNullNotEmptyNotWhiteSpace(vNameReturnString);
                boolean hasAccName = isNotNullNotEmptyNotWhiteSpace(aNameReturnString);

                if(hasVicName){
                    vicButton.setText(R.string.victim_added);
                }
                if(hasAccName){
                    vicButton.setText(R.string.accused_added);
                }
                if(hasVicName && hasAccName){
                    vicButton.setText(R.string.victim_accused_added);
                }

                if(!hasVicName && !hasAccName){
                    vicButton.setText(R.string.add_victim_accused);
                }
            }
        }
    }

    /**Check if string is empty, null, or consists of white space**/
    public static boolean isNotNullNotEmptyNotWhiteSpace(final String string)
    {
        return string != null && !string.isEmpty() && !string.trim().isEmpty();
    }

    public void showErrorAlert(String error, String title, final String toast) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                SendReport1Activity.this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(error);

        alertDialog.setNegativeButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event
                        Toast.makeText(getApplicationContext(), toast,
                                Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });

        alertDialog.show();

      //  getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    private boolean validateReport() {
        /** NOTE: BOOLEANS ARE NAMED OPPOSITE OF WHAT THEY MEAN! EX: buildingIsSelected means building was not selected **/
        boolean successful = false;
        boolean validateLocation = Preferences.getBoolean(Config.HAS_LOCATION_VALIDATION);
        /** If mandatory location toggle is off (if not validationLocation), use standard validation! **/
        if(!validateLocation){
            try {

                boolean buildingIsSelected = report.getBuildingId().equals("0");
                boolean contactIsSelected = report.getContactId().equals("0");
                boolean incidentIsSelected = report.getIncidentId().equals("0");
                boolean messageIsEntered = report.getMessage().length() == 0;

                if (buildingIsSelected || contactIsSelected || incidentIsSelected
                        || messageIsEntered) {

                    String error = String.format(getString(R.string.you_must) + "\n%s\n%s\n%s\n%s",
                            buildingIsSelected ? getString(R.string.choose_building) : "",
                            contactIsSelected ? getString(R.string.choose_contact) : "",
                            incidentIsSelected ? getString(R.string.choose_incident) : "",
                            messageIsEntered ? getString(R.string.enter_a_message) : "");

                    showErrorAlert(error, getString(R.string.required_field_missing),
                            getString(R.string.please_select_all_required));

                    successful = false;
                } else {
                    showSuccessAlert();
                    successful = true;
                }

            } catch (Exception e) {
                Log.e("Error", "Error: " + e.toString());
                e.printStackTrace();
            }
        }
        /** Else use location validation **/
        else{
            try {

                boolean buildingIsSelected = report.getBuildingId().equals("0");
                boolean contactIsSelected = report.getContactId().equals("0");
                boolean incidentIsSelected = report.getIncidentId().equals("0");
                boolean locationIsSelected = report.getLocationId().equals("0");
                boolean messageIsEntered = report.getMessage().length() == 0;

                if (buildingIsSelected || contactIsSelected || incidentIsSelected
                        || locationIsSelected || messageIsEntered) {

                    String error = String.format(getString(R.string.you_must) + "\n%s\n%s\n%s\n%s\n" +
                                    "%s",
                            buildingIsSelected ? getString(R.string.choose_building) : "",
                            contactIsSelected ? getString(R.string.choose_contact) : "",
                            incidentIsSelected ? getString(R.string.choose_incident) : "",
                            locationIsSelected ? getString(R.string.choose_location) : "",
                            messageIsEntered ? getString(R.string.enter_a_message) : "");

                    showErrorAlert(error, getString(R.string.required_field_missing),
                            getString(R.string.please_select_all_required));

                    successful = false;
                } else {
                    showSuccessAlert();
                    successful = true;
                }

            } catch (Exception e) {
                Log.e("Error", "Error: " + e.toString());
                e.printStackTrace();
            }
        }

        return successful;
    }

    private void showSuccessAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                SendReport1Activity.this);
        alertDialog.setTitle(R.string.confirm_report_send);
        alertDialog.setMessage(R.string.confirm_report_send_2);
        // alertDialog.setIcon(R.drawable.delete);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        submitReportTask();
                    }
                });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }

    private void showPhotoAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                SendReport1Activity.this);
        alertDialog.setTitle(R.string.picture_upload);
        alertDialog
                .setMessage(/*hasVideoFeature ? getString(R.string.photo_or_video) :*/ getString(R.string.photo_or_screenshot));
        alertDialog.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, CameraActivity.class);
                        intent.putExtra("confirmation_code",
                                confirmationCode);
                        intent.putExtra("aa_id", String.valueOf(alertId));
                        //if has anonymous checkbox
                        if(hasAnonymousCheckbox){
                            intent.putExtra("reportIsAnonymousByToggle", isAnonymous);
                        }
                        else{
                            intent.putExtra("reportIsAnonymousByToggle", false);
                        }
                        startActivity(intent);
                        finish();
                    }
                });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        goToNextStep();
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }

    private void goToNextStep() {

        Log.d("NextStep","HAS ANONYMOUS CHECKBOX: " + hasAnonymousCheckbox);
        Log.d("NextStep","IS ANONYMOUS BY TOGGLE: " + isAnonymous);

        //if has anonymousCheckbox
        if(hasAnonymousCheckbox){
            //if is anonymous skip to step 3
            if(isAnonymous){
                Intent intent = new Intent(context, SendReport3Activity.class);
                intent.putExtra("confirmation_code", confirmationCode);
                intent.putExtra("aa_id", String.valueOf(alertId));
                intent.putExtra("reportIsAnonymousByToggle", isAnonymous);
                startActivity(intent);
            }
            //go to step 2
            else{
                Intent intent = new Intent(context, SendReport2Activity.class);
                intent.putExtra("confirmation_code", confirmationCode);
                intent.putExtra("aa_id", String.valueOf(alertId));
                intent.putExtra("reportIsAnonymousByToggle", isAnonymous);
                startActivity(intent);
            }
        }
        //go to step 2
        else{
            Intent intent = new Intent(context, SendReport2Activity.class);
            intent.putExtra("confirmation_code", confirmationCode);
            intent.putExtra("aa_id", String.valueOf(alertId));
            intent.putExtra("reportIsAnonymousByToggle", false);
            startActivity(intent);
        }
        finish();
    }

    private void addListenerOnSubmitButton() {
        Button btnSubmit = (Button) findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                report.setMessage(messageEditText.getText().toString());
                validateReport();
            }

        });
    }

    public void getUserCoordinates() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            listener = new GpsListener();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        } else {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                listener = new GpsListener();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
            }
        }
    }

    private class GpsListener implements LocationListener {

        public void onLocationChanged(Location location) {
            if (location != null) {

                mLatitude = location.getLatitude();
                mLongitude = location.getLongitude();
                try {
                    if (listener != null)
                        locationManager.removeUpdates(listener);
                } catch (Exception e) {
                    Log.d(LOG_TAG, "The exception message :" + e.getMessage());
                }
                locationManager = null;
            } else {
                Log.d(LOG_TAG, "location is null");
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_report1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
//        if (itemId == android.R.id.home) {
//            // app icon in action bar clicked; go home
//            Intent intent = new Intent(this, MenuActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//            return true;
//        } else if (itemId == R.id.action_send) {
//            messageEditText = (EditText) findViewById(R.id.messageToSend);
//            report.setMessage(messageEditText.getText().toString());
//            validateReport();
//            return true;
//        } else {
//            return super.onOptionsItemSelected(item);
//        }

        switch (itemId){
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, MenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.action_send:
                messageEditText = (EditText) findViewById(R.id.messageToSend);
                report.setMessage(messageEditText.getText().toString());
                validateReport();
                return true;
//            case R.id.action_change_language:
//                showChangeLangDialog();
//                return true;
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
        String t = getString(R.string.add_victim_accused_plain);
        final Spinner spinner1 = (Spinner) dialogView.findViewById(R.id.spinner1);

        dialogBuilder.setTitle(R.string.select_language);

        dialogBuilder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int langpos = spinner1.getSelectedItemPosition();
                switch(langpos) {
                    case 0: //English
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("LANG", "en").commit();
                        setLangRecreate("en");
                        return;
                    case 1: //French
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("LANG", "fr").commit();
                        setLangRecreate("fr");
                        return;
                    case 2: //Spanish
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("LANG", "es").commit();
                        setLangRecreate("es");
                        return;
                    case 3: //Arabic
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("LANG", "ar").commit();
                        setLangRecreate("ar");
                        return;
                    default: //By default set to english
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("LANG", "en").commit();
                        setLangRecreate("en");
                        return;
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
        Configuration config = getBaseContext().getResources().getConfiguration();
        Locale locale = new Locale(langval);
        Locale.setDefault(locale);
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        recreate();

    }

    public void setTouchListenerForKeyboardDismissal() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.Step1Layout);
        layout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motion) {
                hideKeyboard(view);
                return false;
            }

        });
    }

    protected void hideKeyboard(View view) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void buildActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void submitCoordinatesTask() {
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "SetUserLocation");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("aalert_id", String.valueOf(alertId));
        params.put("Longitude", String.valueOf(mLongitude));
        params.put("Latitude", String.valueOf(mLatitude));

        ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("FinalDataCalls","bluedove-setuserlocation --> response: "+response);
                LogUtils.debug("[SendReport1Activity]","onSuccess --> "+response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        Log.d(LOG_TAG, "Location sent");
                    }
                } catch(Exception e) {
                    LogUtils.debug("[SendReport1Activity]","Catch exception --> "+e.toString());
                }
            }
        });

        apiHelper.setOnErrorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtils.debug("[SendReport1Activity]","onError --> "+error.toString());
            }
        });

        apiHelper.prepareRequest(params, true);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void submitReportTask() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing your report, please wait");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //IF grade != 0, ADD 2 to value of Grade Int and convert back to string for interaction with Database reasons
        if(vGradeReturnInt != 0){
            vGradeReturnInt = vGradeReturnInt + 2;
        }

        if(aGradeReturnInt != 0){
            aGradeReturnInt = aGradeReturnInt + 2;
        }

        String vGrade = "" + vGradeReturnInt;
        String aGrade = "" + aGradeReturnInt;

        //this boolean decides what procedure to use when submitting the report
        boolean hasVicAcc = Preferences.getBoolean(Config.HAS_ADD_VIC);

        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        if(hasVicAcc){
            params.put("action", "SubmitReportv2");
            params.put("victim_name", vNameReturnString);
            params.put("accused_name", aNameReturnString);
            params.put("victim_grade", vGrade);
            params.put("accused_grade", aGrade);
        } else {
            params.put("action", "SubmitReport");
        }

        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("contactId", report.getContactId());
        params.put("incidentId", report.getIncidentId());
        params.put("message", report.getMessage());
        params.put("priorityId", report.getPriorityId());
        params.put("buildingId", report.getBuildingId());
        params.put("locationId", report.getLocationId());
        params.put("submitterId", report.getSubmitterId());
        params.put("deviceType", "4");
        params.put("deviceId", Preferences.getString(Config.PROPERTY_REG_ID));
        params.put("unique_id", Preferences.getString(Config.UNIQUE_ID));

        ApiHelper apiHelper = new ApiHelper();

        Log.d("FinalDataCalls","bluedove-submitreport/v2 params: "+params);

        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("FinalDataCalls","aa_victim_accused is: "+Preferences.getBoolean(Config.HAS_ADD_VIC)+"bluedove-submitreport/v2 --> response: "+response);
                progressDialog.dismiss();
                try {
                    LogUtils.debug("[SendReport1Activity]","onSuccess --> "+response);
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        JSONObject jData = jsonResponse.getJSONObject("data");
                        alertId = jData.getInt("aa_id");
                        confirmationCode = jData.getString("confirmation_code");

                        if (mLongitude != 0 && mLatitude != 0) {
                            //new SubmitCoordinatesTask().execute();
                            submitCoordinatesTask();
                        }
                        if (hasPhotoFeature) {
                            showPhotoAlert();
                        } else {
                            goToNextStep();
                        }
                        Toast.makeText(getApplicationContext(), "Report successfully sent",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to send report, please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    LogUtils.debug("[SendReport1Activity]","Catch exception --> "+e.toString());
                    Toast.makeText(getApplicationContext(), "An error occurred please try again later",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        apiHelper.setOnErrorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                LogUtils.debug("[SendReport1Activity]","onError --> "+error.toString());
                Toast.makeText(getApplicationContext(), "Failed to send report because of a network issue, please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        apiHelper.prepareRequest(params, true);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private class GetPriorityTask extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // show progress
            pDialog = new ProgressDialog(SendReport1Activity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voidParams) {
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            pDialog.cancel();
            if (success) {
                spinnerPriority.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int position, long id) {
                        switch (position) {
                            case 0:
                                report.setPriorityId("1");
                                break;
                            case 1:
                                report.setPriorityId("2");
                                break;
                            default:
                                report.setPriorityId("1");
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {

                    }
                });
            }
        }
    }

    private void getIncidentsTask(final String buildingId) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "GetIncidentsByBuilding");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("buildingId", buildingId);
        params.put("langCode", Preferences.getString("langCode"));

        ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonobject = new JSONObject(response);
                    boolean success = jsonobject.getBoolean("success");
                    if (success) {
                        final ArrayList<SpinnerItem> spinnerItems = new ArrayList<>();
                        final ArrayList<String> incidentList = new ArrayList<>();
                        // Locate the NodeList name
                        JSONArray jsonarray = jsonobject.getJSONArray("data");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            jsonobject = jsonarray.getJSONObject(i);

                            String id = jsonobject.getString("id");
                            String value = jsonobject.getString("value");
                            spinnerItems.add(new SpinnerItem(id, value));
                            // Populate spinner
                            incidentList.add(jsonobject.optString("value"));
                        }
                        incidentList.add(0, getString(R.string.select_incident));
                        spinnerItems.add(0, new SpinnerItem("0", getString(R.string.select_incident)));

                        SpinnerIncident.setAdapter(new ArrayAdapter<>(SendReport1Activity.this,
                                        // android.R.layout.simple_spinner_dropdown_item,
                                        R.layout.custom_spinner,
                                        incidentList));
                        SpinnerIncident.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                                report.setIncidentId(spinnerItems.get(position).getId());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {

                            }
                        });
                    }
                } catch(Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }

                progressDialog.dismiss();
            }
        });

        apiHelper.setOnErrorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", ""+error);
                error.printStackTrace();
                progressDialog.dismiss();
            }
        });

        apiHelper.prepareRequest(params, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void getLocationTask(){
        final ArrayList<SpinnerItem> spinnerItems = new ArrayList<>();
        final ArrayList<String> locationList = new ArrayList<>();

        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "GetLocations");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("langCode", Preferences.getString("langCode"));

        ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                try {
                    JSONObject jsonobject = new JSONObject(responseData);
                    boolean success = jsonobject.getBoolean("success");
                    if (success) {
                        JSONArray jsonarray = jsonobject.getJSONArray("data");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            jsonobject = jsonarray.getJSONObject(i);

                            String id = jsonobject.getString("id");
                            String value = jsonobject.getString("value");
                            spinnerItems.add(new SpinnerItem(id, value));
                            // Populate spinner
                            locationList.add(jsonobject.optString("value"));
                        }
                        locationList.add(0, getString(R.string.select_location));
                        spinnerItems.add(0, new SpinnerItem("0", getString(R.string.select_location)));

                        spinnerLocation
                                .setAdapter(new ArrayAdapter<>(SendReport1Activity.this,
                                        // android.R.layout.simple_spinner_dropdown_item,
                                        R.layout.custom_spinner,
                                        locationList));
                        spinnerLocation
                                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                                    @Override
                                    public void onItemSelected(AdapterView<?> arg0,
                                                               View arg1, int position, long arg3) {
                                        report.setLocationId(spinnerItems.get(position).getId());
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> arg0) {

                                    }
                                });
                    }
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("getLocationTask", error.toString());
            }
        });

        apiHelper.prepareRequest(params, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void getContactsTask(final String buildingId){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading contacts, please wait...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "GetContacts");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("buildingId", buildingId);
        params.put("langCode", Preferences.getString("langCode"));

        ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                LogUtils.debug("[SendReport1Activity]","onSuccess --> "+response);
                try {
                    JSONObject jsonobject = new JSONObject(response);
                    boolean success = jsonobject.getBoolean("success");
                    if (success) {
                        final ArrayList<SpinnerItem> spinnerItems = new ArrayList<>();
                        final ArrayList<String> contactList = new ArrayList<>();
                        // Locate the NodeList name
                        JSONArray jsonarray = jsonobject.getJSONArray("data");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            jsonobject = jsonarray.getJSONObject(i);

                            String id = jsonobject.getString("id");
                            String value = jsonobject.getString("value");
                            spinnerItems.add(new SpinnerItem(id, value));
                            // Populate spinner
                            contactList.add(jsonobject.optString("value"));
                        }
                        contactList.add(0, getString(R.string.select_contact));
                        spinnerItems.add(0, new SpinnerItem("0", getString(R.string.select_contact)));

                        SpinnerContact.setAdapter(new ArrayAdapter<>(SendReport1Activity.this,
                                        // android.R.layout.simple_spinner_dropdown_item,
                                        R.layout.custom_spinner,
                                        contactList));
                        SpinnerContact.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> arg0,
                                                       View arg1, int position, long arg3) {
                                report.setContactId(spinnerItems.get(position).getId());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {

                            }
                        });
                    }
                } catch(Exception e) {
                    LogUtils.debug("[SendReport1Activity]","Catch exception --> "+e.toString());
                }
                progressDialog.dismiss();
            }
        });

        apiHelper.setOnErrorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtils.debug("[SendReport1Activity]","onError --> "+error.toString());
                progressDialog.dismiss();
            }
        });

        apiHelper.prepareRequest(params, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void getSubmitterTask(){
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "GetSubmitters");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("langCode", Preferences.getString("langCode"));

        final ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonobject = new JSONObject(response);
                    boolean success = jsonobject.getBoolean("success");
                    if (success) {
                        final ArrayList<SpinnerItem> spinnerItems = new ArrayList<>();
                        final ArrayList<String> submitterList = new ArrayList<>();

                        JSONArray jsonarray = jsonobject.getJSONArray("data");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            jsonobject = jsonarray.getJSONObject(i);

                            String id = jsonobject.getString("id");
                            String value = jsonobject.getString("value");
                            spinnerItems.add(new SpinnerItem(id, value));
                            // Populate spinner
                            submitterList.add(jsonobject.optString("value"));
                        }
                        submitterList.add(0, getString(R.string.select_submitter));
                        spinnerItems.add(0, new SpinnerItem("0", getString(R.string.select_submitter)));

                        spinnerSubmitter.setAdapter(new ArrayAdapter<>(SendReport1Activity.this,
                                        // android.R.layout.simple_spinner_dropdown_item,
                                        R.layout.custom_spinner,
                                        submitterList));

                        spinnerSubmitter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                                report.setSubmitterId(spinnerItems.get(position).getId());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {

                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", ""+error);
                error.printStackTrace();
            }
        });

        apiHelper.prepareRequest(params, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void getBuildingTypeTask(){
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "GetSchoolType");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("langCode", Preferences.getString("langCode"));

        final ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    final ArrayList<SpinnerItem> spinnerItems = new ArrayList<>();
                    final ArrayList<String> schoolTypeList = new ArrayList<>();

                    JSONObject jsonobject = new JSONObject(response);
                    boolean success = jsonobject.getBoolean("success");
                    if (success) {
                        JSONArray jsonarray = jsonobject.getJSONArray("data");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            jsonobject = jsonarray.getJSONObject(i);

                            String id = jsonobject.getString("id");
                            buildingTypeId = id;
                            String value = jsonobject.getString("value");
                            spinnerItems.add(new SpinnerItem(id, value));
                            // Populate spinner
                            schoolTypeList.add(jsonobject.optString("value"));
                        }
                        schoolTypeList.add(0, getString(R.string.select_school_type));
                        spinnerItems.add(0, new SpinnerItem("0", getString(R.string.select_school_type)));

                        spinnerBuildingType.setAdapter(new ArrayAdapter<>(SendReport1Activity.this,
                                        // android.R.layout.simple_spinner_dropdown_item,
                                        R.layout.custom_spinner,
                                        schoolTypeList));

                        spinnerBuildingType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> arg0,
                                                       View arg1, int position, long arg3) {
                                report.setBuildingTypeId(spinnerItems.get(position).getId());
                                getSchoolBuildingsTask(report.getBuildingTypeId());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {

                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtils.debug("Error",""+error);
            }
        });

        apiHelper.prepareRequest(params, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void getSchoolBuildingsTask(final String idStr) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "GetBuildings");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("typeId", idStr);
        params.put("langCode", Preferences.getString("langCode"));

        ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    final ArrayList<SpinnerItem> spinnerItems = new ArrayList<>();
                    final ArrayList<String> buildingList = new ArrayList<>();

                    JSONObject jsonobject = new JSONObject(response);
                    boolean success = jsonobject.getBoolean("success");
                    if (success) {
                        JSONArray jsonarray = jsonobject.getJSONArray("data");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            jsonobject = jsonarray.getJSONObject(i);

                            String id = jsonobject.getString("id");
                            String values = jsonobject.getString("value");
                            // Populate spinner
                            spinnerItems.add(new SpinnerItem(id, values));

                            buildingList.add(jsonobject.optString("value"));
                        }

                        String bString = "";
                        if(buildingNaming.equals("School")){
                            bString = getString(R.string.school);
                        }
                        else {
                            bString =getString(R.string.building_1);
                        }

                        String tempString = "-"+getString(R.string.select)+" "+bString+"-";
                        //buildingList.add(0, String.format("-Select %s-", buildingNaming));
                        buildingList.add(0, tempString);
                        spinnerItems.add(0, new SpinnerItem("0", tempString));

                        SpinnerBuilding.setAdapter(new ArrayAdapter<>(SendReport1Activity.this,
                                        // android.R.layout.simple_spinner_dropdown_item,
                                        R.layout.custom_spinner,
                                        buildingList));



                        SpinnerBuilding.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> arg0,
                                                       View arg1, int position, long arg3) {
                                report.setBuildingId(spinnerItems.get(position).getId());
                                if (position != 0) {
                                    getContactsTask(report.getBuildingId());
                                    getIncidentsTask(report.getBuildingId());
                                    SpinnerIncident.setVisibility(View.VISIBLE);
                                    incidentTextView.setVisibility(View.VISIBLE);
                                    SpinnerContact.setVisibility(View.VISIBLE);
                                    contactTextView.setVisibility(View.VISIBLE);

                                    Preferences.putString("lastSelectedBuildingName", spinnerItems.get(position).getValue());
                                    Preferences.putString("lastSelectedBuildingId", spinnerItems.get(position).getId());
                                    Log.e("User Selected:", "ID: " + spinnerItems.get(position).getId() + " || Name: " + spinnerItems.get(position).getValue());

                                } else {
                                    SpinnerIncident.setVisibility(View.GONE);
                                    SpinnerContact.setVisibility(View.GONE);
                                    incidentTextView.setVisibility(View.GONE);
                                    contactTextView.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {

                            }
                        });

                        //Check if the last selected building is on the list. Then set the selection
                        String lastSelectedBuildingName = Preferences.getString("lastSelectedBuildingName");
                        String lastSelectedBuildingId = Preferences.getString("lastSelectedBuildingId");
                        Log.e("LastSelected:", "ID: " + lastSelectedBuildingId + " || Name: " + lastSelectedBuildingName);

                        for(int i = 0; i < spinnerItems.size(); i++){
                            String spinnerBuildingName = spinnerItems.get(i).getValue();
                            if(spinnerBuildingName.equals(lastSelectedBuildingName)){
                                SpinnerItem lastSelectedItem = spinnerItems.get(i);
                                if(spinnerItems.contains(lastSelectedItem)){
                                    int lastSelectedItemIndex = spinnerItems.indexOf(lastSelectedItem);
                                    SpinnerBuilding.setSelection(lastSelectedItemIndex);
                                }
                                break;
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }

                progressDialog.dismiss();
            }
        });

        apiHelper.setOnErrorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", ""+error);
                error.printStackTrace();
                progressDialog.dismiss();
            }
        });

        apiHelper.prepareRequest(params, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }
}
