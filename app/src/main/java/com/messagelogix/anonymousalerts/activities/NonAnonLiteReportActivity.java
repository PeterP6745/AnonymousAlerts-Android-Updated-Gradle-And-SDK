package com.messagelogix.anonymousalerts.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.model.Report;
import com.messagelogix.anonymousalerts.model.SpinnerItem;
import com.messagelogix.anonymousalerts.utils.ApiHelper;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.FunctionHelper;
import com.messagelogix.anonymousalerts.utils.LogUtils;
import com.messagelogix.anonymousalerts.utils.Preferences;
import com.messagelogix.anonymousalerts.utils.ProgressIndicator;
import com.messagelogix.anonymousalerts.utils.ValidateEmail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Jeremy on 6/15/2017.
 */
public class NonAnonLiteReportActivity extends Activity {

    final Context context = this;
    public static final String LOG_TAG = NonAnonLiteReportActivity.class.getSimpleName();

    private int alertId = 0;
    public String confirmationCode;

    private Spinner submitterSpinner;
    private Spinner regionSpinner;
    private Spinner buildingTypeSpinner;
    private Spinner buildingSpinner;
    private Spinner incidentSpinner;
    private Spinner incidentCategorySpinner;
    private Spinner incidentsByCategorySpinner;
    private Spinner locationSpinner;

    private EditText messageEditText;

    private Button submitReportBtn;

    private CheckBox anonymousCheckbox;

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;

    private boolean isAnonymous;

    private boolean hasPhotoFeature;//, hasVideoFeature;

    private String buildingNaming;

    private Report report = new Report();

    private ValidateEmail emailValidator;
    private boolean emailIsValid;
    private String langCode;

    private static int TIMER_SETTING;
    private boolean timerIsElapsed;
    private CountDownTimer timer;
    private String time;

    private AlertDialog.Builder builder;

    private AlertDialog dialog;

    private String vNameReturnString = "";
    private String aNameReturnString = "";

    private String vGradeReturnString = "";
    private String aGradeReturnString = "";

    private int vGradeReturnInt = 0;
    private int aGradeReturnInt = 0;

    private static final int ADD_VIC_ACTIVITY_RESULT_CODE = 0;

    private Button vicAccButton;

    boolean hasPortalSubmitter;
    boolean hasRegions;
    boolean hasBuildingTypeListings;
    boolean hasLocations;
    boolean showIncidentCategorySpinner;

    private Handler handler = new Handler();
    ProgressIndicator activityProgressIndicator;
    boolean errorWithActivityProgress = false;
    int errorLoadingCondition = -1;

    boolean submitterTrigger = true;
    boolean categoryTrigger = true;
    boolean regionTrigger = true;
    boolean buildingTypeTrigger = true;
    boolean locationTrigger = true;
    boolean buildingTrigger = true;
    boolean incidentTrigger = true;

    boolean submitterCompleted = false;
    boolean regionCompleted = false;
    boolean buildingTypeCompleted = false;
    boolean locationCompleted = false;
    boolean buildingCompleted = false;
    boolean categoryCompleted = false;
    boolean incidentCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.nonanonlitereportactivity_layout);
        buildActionBar();
        setTitle(getString(R.string.submit_report));

        Preferences.init(this);

        //check language code
        langCode = Preferences.getString("langCode");
        if(FunctionHelper.isNullOrEmpty(langCode)){
            langCode = "en";
        }

        hasPortalSubmitter = Preferences.getBoolean(Config.HAS_SUBMITTER_FOR_LITE);
        hasRegions = Preferences.getBoolean(Config.HAS_REGIONS);
        hasBuildingTypeListings = Preferences.getBoolean(Config.HAS_BUILDING_TYPE);
        hasLocations = Preferences.getBoolean(Config.HAS_LOCATION);
        showIncidentCategorySpinner = Preferences.getBoolean(Config.HAS_INCIDENT_CATEGORIES);

        initWidgets();

        emailValidator = new ValidateEmail();

        hasPhotoFeature = Preferences.getBoolean(Config.HAS_PHOTO_FEATURE);
        //hasVideoFeature = Preferences.getBoolean(Config.HAS_VIDEO_FEATURE);
        buildingNaming = Preferences.getString(Config.BUILDING_NAMING);

        //Setup PAUSE timer
        TIMER_SETTING = Preferences.getInteger(Config.PAUSE_TIMEOUT, 180) * 1000;
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkTimeOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkTimeOut();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(asyncTicker);
        stopTimer();
    }

    private void checkTimeOut() {
        long currentTime = System.currentTimeMillis();
        long nextAllowedTime = Preferences.getLong(Config.NEXT_TIME_ALLOWED_AA);
        if (nextAllowedTime >= currentTime) {
            // locatorButton.setBackgroundResource(R.drawable.grey_smart_button);
            long remainingMillis = nextAllowedTime - currentTime;
            if (remainingMillis > 0) {
                startTimer(remainingMillis);
            }
        } else {
            // locatorButton.setBackgroundResource(R.drawable.red_smart_button);
            timerIsElapsed = true;
        }
    }

    public void startTimer(long milliseconds) {
        timer = new CountDownTimer(milliseconds, 1000) {
            public void onTick(long millisUntilFinished) {
                //While timer is going, turn off submission
                submitReportBtn.setEnabled(false);
                timerIsElapsed = false;

                //long currentTime = System.currentTimeMillis();
                //user.setNextAllowedTime(currentTime + millisUntilFinished);

                long secondsUntilFinished = millisUntilFinished / 1000;
                long minutes = secondsUntilFinished / 60;
                long seconds = secondsUntilFinished % 60;

                time = String.format(Locale.US, "%02d:%02d", minutes, seconds);
                if (builder != null && dialog != null) {
                    if (dialog.isShowing())
                        builder.setMessage("remaining time = " + time);
                }
            }

            //When timer completes, allow more submissions
            public void onFinish() {
                submitReportBtn.setEnabled(true);
                timerIsElapsed = true;
                Preferences.putLong(Config.NEXT_TIME_ALLOWED, (long) 0);
            }
        }.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void showTimerAlert() {
        // Redirect the app to enable location services
        builder = new AlertDialog.Builder(context);
        builder.setTitle("Timeout: " + time);
        builder.setMessage("You must wait for " + time + " to use the smart button again");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.cancel();
            }
        });
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void buildActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == android.R.id.home) {
            // app icon in action bar clicked; go home
            Intent intent = new Intent(this, MenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initWidgets(){
        //REGION TOGGLE
        regionSpinner = (Spinner) findViewById(R.id.lite_region_spinner);
        regionSpinner.setVisibility(hasRegions ? View.VISIBLE : View.GONE);

        //Check for building type toggle
        buildingTypeSpinner = (Spinner) findViewById(R.id.lite_building_type_spinner);
        buildingTypeSpinner.setVisibility(hasBuildingTypeListings ? View.VISIBLE : View.GONE);

        //Check for submitter toggle
        submitterSpinner = (Spinner) findViewById(R.id.lite_submitter_spinner);
        submitterSpinner.setVisibility(hasPortalSubmitter ? View.VISIBLE : View.GONE);

        //Check for location toggle
        locationSpinner = (Spinner) findViewById(R.id.lite_location_spinner);
        locationSpinner.setVisibility(hasLocations ? View.VISIBLE : View.GONE);

        incidentCategorySpinner = (Spinner) findViewById(R.id.lite_incident_category_spinner);
        incidentCategorySpinner.setVisibility(showIncidentCategorySpinner ? View.VISIBLE : View.GONE);
        incidentsByCategorySpinner = (Spinner) findViewById(R.id.lite_incident_by_category_spinner);

        //init required spinners
        buildingSpinner = (Spinner) findViewById(R.id.lite_building_spinner);

        incidentSpinner = (Spinner) findViewById(R.id.lite_incident_spinner);
        incidentSpinner.setVisibility(!showIncidentCategorySpinner ? View.VISIBLE : View.GONE);

        //init message box
        messageEditText = (EditText) findViewById(R.id.lite_message_text_view);

        //init submit button
        submitReportBtn = (Button) findViewById(R.id.lite_submit_button);
        submitReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report.setMessage(messageEditText.getText().toString());
                validateNonAnonReport();
            }
        });

        //init text fields
        nameEditText = (EditText) findViewById(R.id.lite_name_field);
        emailEditText = (EditText) findViewById(R.id.lite_email_field);
        phoneEditText = (EditText) findViewById(R.id.lite_phone_field);

        //set disclaimer text
        TextView disclaimerText = (TextView) findViewById(R.id.text_view_disclaimer);
        String customText = Preferences.getString(Config.STEP1_COMPLETE_FORM); //getString(R.string.quick_report_disclaimer)
        if(customText.equals("Please complete the information below then submit your report. If this is an emergency call 911.")){
            customText = getString(R.string.please_complete);
        }

        SpannableString demotext = new SpannableString(customText);
        if(langCode.equals("en")){
            demotext.setSpan(new ForegroundColorSpan(Color.RED), 63, demotext.length(), 0);
            // create a bold StyleSpan to be used on the SpannableStringBuilder
            StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
            demotext.setSpan(b, 63, demotext.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        if(langCode.equals("es")){
            demotext.setSpan(new ForegroundColorSpan(Color.RED), 59, demotext.length(), 0);
            // create a bold StyleSpan to be used on the SpannableStringBuilder
            StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
            demotext.setSpan(b, 59, demotext.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        disclaimerText.setText(demotext);
        loadSpinnerItems();

        //init add vic/accused button
        /*Add Victim/Accused*/
        vicAccButton = (Button) findViewById(R.id.lite_add_vic_button);
        boolean hasVicAcc = Preferences.getBoolean(Config.HAS_ADD_VIC);
        if(hasVicAcc){
            vicAccButton.setVisibility(View.VISIBLE);
            vicAccButton.setOnClickListener(new View.OnClickListener() {
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
        } else {
            vicAccButton.setVisibility(View.GONE);
        }
    }

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
                    vicAccButton.setText(R.string.victim_added);
                }
                if(hasAccName){
                    vicAccButton.setText(R.string.accused_added);
                }
                if(hasVicName && hasAccName){
                    vicAccButton.setText(R.string.victim_accused_added);
                }

                if(!hasVicName && !hasAccName){
                    vicAccButton.setText(R.string.add_victim_accused);
                }
            }
        }
    }

    private void loadSpinnerItems() {
        LogUtils.debug("AsyncHandler","loadSpinnerItems() --> adding handler.post()");
        handler.post(asyncTicker);

        activityProgressIndicator = new ProgressIndicator(this);
        activityProgressIndicator.showDialog("Loading your latest report settings, please wait...");

        submitterCompleted = !hasPortalSubmitter;
        regionCompleted = !hasRegions;
        buildingTypeCompleted = !hasBuildingTypeListings;
        locationCompleted = !hasLocations;
        buildingCompleted = false;

        if(hasPortalSubmitter)
            getSubmitterTask();

        if(hasRegions)
            getRegionsTask();

        if(hasBuildingTypeListings)
            getBuildingTypesTask();

        if(showIncidentCategorySpinner){
            incidentCompleted = true;
            categoryCompleted = false;
            getIncidentCategoryTask();
        } else {
            incidentCompleted = false;
            categoryCompleted = true;
            getIncidentsTask(report.getBuildingId());
        }

        if(hasLocations)
            getLocationTask();

        //load school buildings list
        getSchoolBuildingsTask(report.getBuildingTypeId(), report.getRegionId());
    }

    private void getIncidentsByCategoryTask(String categoryId){
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "GetIncidentsByCategory");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("categoryId", categoryId);
        params.put("langCode", langCode);

        final ApiHelper apiHelper = new ApiHelper();

        final ProgressIndicator progressIndicator  = new ProgressIndicator(this);
        progressIndicator.showDialog("Loading incidents, please wait...");

        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    LogUtils.debug("ReportCreation","getIncidentsByCategoryTask() --> \n"+response);
                    JSONObject jsonobject = new JSONObject(response);

                    boolean success = jsonobject.getBoolean("success");
                    if (success) {
                        incidentsByCategorySpinner.setVisibility(View.VISIBLE);
                        final ArrayList<SpinnerItem> spinnerItems = new ArrayList<>();
                        final ArrayList<String> incidentsBycategoryList = new ArrayList<>();

                        JSONArray jsonarray = jsonobject.getJSONArray("data");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            jsonobject = jsonarray.getJSONObject(i);

                            String id = jsonobject.getString("id");
                            String value = jsonobject.getString("value");
                            spinnerItems.add(new SpinnerItem(id, value));
                            // Populate spinner
                            incidentsBycategoryList.add(jsonobject.optString("value"));
                        }
                        incidentsBycategoryList.add(0, getString(R.string.select_incident));
                        spinnerItems.add(0, new SpinnerItem("0", getString(R.string.select_incident)));

                        incidentsByCategorySpinner.setAdapter(new ArrayAdapter<String>(context,
                                // android.R.layout.simple_spinner_dropdown_item,
                                R.layout.custom_spinner,
                                incidentsBycategoryList) {
                            @Override
                            public boolean isEnabled(int position) {
                                return position != 0;
                            }
                        });

                        incidentsByCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                                report.setIncidentId(spinnerItems.get(position).getId());
                                Log.d("incidentsBycategoryselection",position+""+ " value: "+ incidentsBycategoryList.get(position)+ " report id value: " + report.getIncidentId());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {}
                        });

                        progressIndicator.dismiss();
                    }
                    else {
                        progressIndicator.dismiss();
                        //show error alert
                        errorLoadingIncidentsByCategory();
                    }
                } catch(Exception ignore) {
                    progressIndicator.dismiss();
                    //show error alert
                    errorLoadingIncidentsByCategory();
                }
            }
        });

        apiHelper.setOnErrorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("StandardLiteReportActivity","getIncidentsByCategoryTask() - bluedove-GetIncidentsByCategory - onFailure: "+error);
                progressIndicator.dismiss();
                //show error alert
                errorLoadingIncidentsByCategory();
            }
        });

        apiHelper.prepareRequest(params,false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void errorLoadingIncidentsByCategory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");

        String alertMessage = "Failed to load your Incident List at this time.";
        builder.setMessage(alertMessage);

        builder.setNegativeButton("Go Back to Menu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        builder.setPositiveButton("Reload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                incidentsByCategorySpinner.setVisibility(View.GONE);
                restartActivityProgress();
            }
        });

        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void  getIncidentCategoryTask(){
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "GetIncidentsCategory");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        final ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    LogUtils.debug("ReportCreation","getIncidentCategoryTask() --> \n"+response);
                    JSONObject jsonobject = new JSONObject(response);
                    boolean success = jsonobject.getBoolean("success");

                    if (success) {
                        final ArrayList<SpinnerItem> spinnerItems = new ArrayList<>();
                        final ArrayList<String> categoryList = new ArrayList<>();

                        JSONArray jsonarray = jsonobject.getJSONArray("data");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            jsonobject = jsonarray.getJSONObject(i);

                            String id = jsonobject.getString("id");
                            String value = jsonobject.getString("value");
                            spinnerItems.add(new SpinnerItem(id, value));
                            // Populate spinner
                            categoryList.add(jsonobject.optString("value"));
                        }
                        categoryList.add(0, getString(R.string.select_category));
                        spinnerItems.add(0, new SpinnerItem("0", getString(R.string.select_category)));

                        incidentCategorySpinner.setAdapter(new ArrayAdapter<String>(context,
                                // android.R.layout.simple_spinner_dropdown_item,
                                R.layout.custom_spinner,
                                categoryList){
                            @Override
                            public boolean isEnabled(int position) {
                                return position != 0;
                            }
                        });

                        incidentCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                                if(categoryTrigger){
                                    LogUtils.debug("Spinners","incidentCategorySpinner - spinnerItems.get(position).getId() --> "+spinnerItems.get(position).getId());
                                    categoryTrigger = false;
                                    Log.d("ReportCreation","categoryTrigger is now off");
                                    return;
                                }

                                String currCategoryId = spinnerItems.get(position).getId();
                                String prevCategoryId = report.getCategoryId();
                                if(!currCategoryId.equals(prevCategoryId)) {
                                    report.setCategoryId(currCategoryId);
                                    report.setIncidentId("0");
                                    getIncidentsByCategoryTask(/*position+1+""*/currCategoryId);
                                    LogUtils.debug("categorySelection","Selected Position: " + position + " value: "+ categoryList.get(position)+ " ID: "+spinnerItems.get(position).getId() + " Stored Id in report: "+ report.getCategoryId() );
                                } else {
                                    LogUtils.debug("categorySelection","Selected same category as before, so do nothing");
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {}
                        });
                    } else {
                        errorLoadingReportSettings(6);
                    }
                } catch(Exception ignore) {errorLoadingReportSettings(6);}

                categoryCompleted = true;
            }
        });

        apiHelper.setOnErrorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("StandardLiteReportActivity","getIncidentCategoryTask() - bluedove-GetIncidentsCategory - onFailure: "+error);
                errorLoadingReportSettings(6);
                categoryCompleted = true;
            }
        });

        apiHelper.prepareRequest(params,false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }
    private void getSubmitterTask() {
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "GetSubmitters");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("langCode", langCode);

        final ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    LogUtils.debug("ReportCreation","getSubmitterTask() --> \n"+response);
                    JSONObject jsonobject = new JSONObject(response);
                    boolean success = jsonobject.getBoolean("success");

                    if (success) {
                        final ArrayList<SpinnerItem> spinnerItems = new ArrayList<>();
                        ArrayList<String> submitterList = new ArrayList<>();

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

                        submitterSpinner.setAdapter(new ArrayAdapter<String>(context,
                                // android.R.layout.simple_spinner_dropdown_item,
                                R.layout.custom_spinner,
                                submitterList) {
                            @Override
                            public boolean isEnabled(int position) {
                                return position != 0;
                            }
                        });

                        submitterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                                if(submitterTrigger){
                                    LogUtils.debug("Spinners","submitterSpinner - report.setSubmitterId() - spinnerItems.get(position).getId() --> "+spinnerItems.get(position).getId());
                                    submitterTrigger = false;
                                    Log.d("ReportCreation","submitterTrigger is now off");
                                    return;
                                }

                                report.setSubmitterId(spinnerItems.get(position).getId());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {}
                        });
                    } else {
                        errorLoadingReportSettings(0);
                    }
                } catch(Exception ignore) {errorLoadingReportSettings(0);}

                submitterCompleted = true;
            }
        });

        apiHelper.setOnErrorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("NonAnonLiteReportActivity","getRegionsTask() - bluedove-GetSubmitters - onFailure: "+error);
                errorLoadingReportSettings(0);
                submitterCompleted = true;
            }
        });

        apiHelper.prepareRequest(params,false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void getRegionsTask() {
        LogUtils.debug("TempDataCalls","calling getRegionsTask()");

        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "GetRegions");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("langCode", Preferences.getString("langCode"));

        ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    LogUtils.debug("ReportCreation","getRegionsTask() --> \n"+response);
                    Log.d("LiteReport","getRegions() -> repsonse: "+response);
                    JSONObject jsonobject = new JSONObject(response);
                    boolean success = jsonobject.getBoolean("success");

                    if (success) {
                        final ArrayList<SpinnerItem> spinnerItems = new ArrayList<>();
                        ArrayList<String> regionsList = new ArrayList<>();

                        JSONArray jsonarray = jsonobject.getJSONArray("data");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            jsonobject = jsonarray.getJSONObject(i);

                            String id = jsonobject.getString("id");
                            //buildingTypeId = id;
                            String value = jsonobject.getString("value");
                            spinnerItems.add(new SpinnerItem(id, value));
                            // Populate spinner
                            regionsList.add(jsonobject.optString("value"));
                        }

                        regionsList.add(0, getString(R.string.select_a_region));
                        spinnerItems.add(0, new SpinnerItem("0", getString(R.string.select_a_region)));

                        regionSpinner.setAdapter(new ArrayAdapter<String>(context,
                                // android.R.layout.simple_spinner_dropdown_item,
                                R.layout.custom_spinner,
                                regionsList) {
                            @Override
                            public boolean isEnabled(int position) {
                                return position != 0;
                            }
                        });

                        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                                if(regionTrigger){
                                    LogUtils.debug("Spinners","regionSpinner - report.setRegionId() - spinnerItems.get(position).getId() --> "+spinnerItems.get(position).getId());
                                    LogUtils.debug("Spinners","regionSpinner - regionId = spinnerItems.get(position).getId() --> "+spinnerItems.get(position).getId());
                                    LogUtils.debug("Spinners","regionSpinner - getSchoolBuildingsTask() - report.getBuildingTypeId() --> " + report.getBuildingTypeId());
                                    LogUtils.debug("Spinners","regionSpinner - getSchoolBuildingsTask() - report.getRegionId() --> " + report.getRegionId());
                                    LogUtils.debug("Spinners","regionSpinner - getIncidentsTask(buildingId = 0)");
                                    regionTrigger = false;
                                    Log.d("ReportCreation","regionTrigger is now off");
                                    return;
                                }

                                report.setRegionId(spinnerItems.get(position).getId());

                                if(showIncidentCategorySpinner) {
                                    if(hasBuildingTypeListings) {
                                        displayActivityProgressIndicator(5);
                                        report.setBuildingTypeId("0");
                                        getBuildingTypesTask();
                                    } else
                                        displayActivityProgressIndicator(4);

                                    getSchoolBuildingsTask(report.getBuildingTypeId(), report.getRegionId());
                                } else {
                                    if(hasBuildingTypeListings) {
                                        displayActivityProgressIndicator(3);
                                        report.setBuildingTypeId("0");
                                        getBuildingTypesTask();
                                    } else
                                        displayActivityProgressIndicator(2);

                                    getSchoolBuildingsTask(report.getBuildingTypeId(), report.getRegionId());

                                    report.setIncidentId("0");
                                    getIncidentsTask("0");
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {}
                        });
                    } else {
                        errorLoadingReportSettings(1);
                    }
                } catch (JSONException e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                    errorLoadingReportSettings(1);
                }

                regionCompleted = true;
            }
        });

        apiHelper.setOnErrorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("NonAnonLiteReportActivity","getRegionsTask() - bluedove-GetRegions - onFailure: "+error);
                errorLoadingReportSettings(1);
                regionCompleted = true;
            }
        });

        apiHelper.prepareRequest(params,false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void getBuildingTypesTask() {
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "GetSchoolType");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("langCode", Preferences.getString("langCode"));

        ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    LogUtils.debug("ReportCreation","getBuildingTypesTask() --> \n"+response);
                    LogUtils.debug("LiteReport","getSchoolType() --> response: "+response);
                    JSONObject jsonobject = new JSONObject(response);
                    boolean success = jsonobject.getBoolean("success");
                    if (success) {
                        final ArrayList<SpinnerItem> spinnerItems = new ArrayList<>();
                        final ArrayList<String> schoolTypeList = new ArrayList<>();

                        JSONArray jsonarray = jsonobject.getJSONArray("data");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            jsonobject = jsonarray.getJSONObject(i);

                            String id = jsonobject.getString("id");
                            //buildingTypeId = id;
                            String value = jsonobject.getString("value");
                            spinnerItems.add(new SpinnerItem(id, value));
                            // Populate spinner
                            schoolTypeList.add(jsonobject.optString("value"));
                        }
                        String buildingNaming =  "-Select a " + Preferences.getString(Config.BUILDING_NAMING) + " Type-";

                        schoolTypeList.add(0, getString(R.string.select_school_type));
                        spinnerItems.add(0, new SpinnerItem("0", getString(R.string.select_school_type)));

                        buildingTypeSpinner.setAdapter(new ArrayAdapter<String>(context,
                                // android.R.layout.simple_spinner_dropdown_item,
                                R.layout.custom_spinner,
                                schoolTypeList) {
                            @Override
                            public boolean isEnabled(int position) {
                                return position != 0;
                            }
                        });

                        buildingTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                                if(buildingTypeTrigger){
                                    LogUtils.debug("Spinners","buildingTypeSpinner - report.setBuildingTypeId - spinnerItems.get(position).getId() --> "+spinnerItems.get(position).getId());
                                    LogUtils.debug("Spinners","buildingTypeSpinner - buildingTypeId = spinnerItems.get(position).getId() --> "+spinnerItems.get(position).getId());
                                    LogUtils.debug("Spinners","buildingTypeSpinner - getSchoolBuildingsTask() - report.getBuildingTypeId() --> " + report.getBuildingTypeId());
                                    LogUtils.debug("Spinners","buildingTypeSpinner - getSchoolBuildingsTask() - report.getBuildingTypeId() --> " + report.getRegionId());
                                    buildingTypeTrigger = false;
                                    Log.d("ReportCreation","buildingTypeTrigger is now off");
                                    return;
                                }

                                report.setBuildingTypeId(spinnerItems.get(position).getId());

                                if(showIncidentCategorySpinner) {
                                    displayActivityProgressIndicator(4);

                                    getSchoolBuildingsTask(report.getBuildingTypeId(), report.getRegionId());
                                } else {
                                    displayActivityProgressIndicator(2);

                                    getSchoolBuildingsTask(report.getBuildingTypeId(), report.getRegionId());

                                    report.setIncidentId("0");
                                    getIncidentsTask("0");
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {}
                        });

                        buildingTypeSpinner.setSelection(0);
                    } else {
                        errorLoadingReportSettings(2);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    errorLoadingReportSettings(2);
                }

                buildingTypeCompleted = true;
            }
        });

        apiHelper.setOnErrorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("NonAnonLiteReportActivity","getBuildingTypesTask() - bluedove-GetSchoolType - onFailure: "+error);
                errorLoadingReportSettings(2);
                buildingTypeCompleted = true;
            }
        });

        apiHelper.prepareRequest(params,false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void getLocationTask(){
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "GetLocations");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("langCode", Preferences.getString("langCode"));

        final ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                try {
                    LogUtils.debug("ReportCreation","getLocationTask() --> \n"+responseData);
                    JSONObject jsonobject = new JSONObject(responseData);
                    boolean success = jsonobject.getBoolean("success");
                    if(success) {
                        final ArrayList<SpinnerItem> spinnerItems = new ArrayList<>();
                        final ArrayList<String> locationList = new ArrayList<>();

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

                        locationSpinner.setAdapter(new ArrayAdapter<String>(context,
                                // android.R.layout.simple_spinner_dropdown_item,
                                R.layout.custom_spinner,
                                locationList) {
                            @Override
                            public boolean isEnabled(int position) {
                                return position != 0;
                            }
                        });

                        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                                if(locationTrigger){
                                    LogUtils.debug("Spinners","locationSpinner - report.setLocationId() - spinnerItems.get(position).getId() --> " + spinnerItems.get(position).getId());
                                    locationTrigger = false;
                                    Log.d("ReportCreation","locationTrigger is now off");
                                    return;
                                }

                                report.setLocationId(spinnerItems.get(position).getId());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {}
                        });
                    } else {
                        errorLoadingReportSettings(5);
                    }
                } catch (Exception e) {
                    Log.e("getLocationTask", e.getMessage());
                    e.printStackTrace();
                    errorLoadingReportSettings(5);
                }

                locationCompleted = true;
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("NonAnonLiteReportActivity","getLocationTask() - bluedove-GetLocation - onFailure: "+error);
                errorLoadingReportSettings(5);
                locationCompleted = true;
            }
        });

        apiHelper.prepareRequest(params, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void getSchoolBuildingsTask(final String bTypeId, final String bRegionId){
        final ApiHelper apiHelper = new ApiHelper();

        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "GetBuildingsByRegion");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("typeId", bTypeId);
        params.put("region", bRegionId);
        params.put("langCode", langCode);

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                try {
                    LogUtils.debug("ReportCreation","getSchoolBuildingsTask --> \n"+responseData);
                    JSONObject jsonobject = new JSONObject(responseData);
                    boolean success = jsonobject.getBoolean("success");

                    if (success) {
                        final ArrayList<SpinnerItem> spinnerItems = new ArrayList<>();
                        final ArrayList<String> buildingList = new ArrayList<>();

                        JSONArray jsonarray = jsonobject.getJSONArray("data");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            jsonobject = jsonarray.getJSONObject(i);

                            String id = jsonobject.getString("id");
                            String values = jsonobject.getString("value");
                            spinnerItems.add(new SpinnerItem(id, values));
                            // Populate spinner
                            buildingList.add(jsonobject.optString("value"));
                        }

                        String bString = Preferences.getString(Config.BUILDING_NAMING);
                        LogUtils.debug("ReportSettings","building_naming settings: "+bString);
                        if(buildingNaming.equals(getString(R.string.school))){
                            bString = getString(R.string.school);
                        }
                        else if(buildingNaming.equals(getString(R.string.building))){
                            bString = getString(R.string.building_1);
                        }

                        String temp = "-"+getString(R.string.select)+" "+bString+"-";
                        //buildingList.add(0, String.format("-Select %s-", buildingNaming));
                        buildingList.add(0, temp);
                        spinnerItems.add(0, new SpinnerItem("0", temp));

                        buildingSpinner.setAdapter(new ArrayAdapter<String>(context,
                                // android.R.layout.simple_spinner_dropdown_item,
                                R.layout.custom_spinner,
                                buildingList) {
                            @Override
                            public boolean isEnabled(int position) {
                                return position != 0;
                            }
                        });

                        buildingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                                if(buildingTrigger){
                                    LogUtils.debug("Spinners","schoolBuildingSpinner - report.setBuildingId() - spinnerItems.get(position).getId() --> " + spinnerItems.get(position).getId());
                                    LogUtils.debug("Spinners","schoolBuildingSpinner - spinnerItems.get(position).getValue() --> " + spinnerItems.get(position).getValue());
                                    LogUtils.debug("Spinners","schoolBuildingSpinner - spinnerItems.get(position).getId() --> " + spinnerItems.get(position).getId());
                                    buildingTrigger = false;
                                    Log.d("ReportCreation","schoolBuildingTrigger is now off");
                                    return;
                                }

                                Log.d("ReportCreation","schoolBuildingTrigger is off, about to check if position != 0");

                                report.setBuildingId(spinnerItems.get(position).getId());

                                Preferences.putString("lastSelectedBuildingName", spinnerItems.get(position).getValue());
                                Preferences.putString("lastSelectedBuildingId", spinnerItems.get(position).getId());

                                if(!showIncidentCategorySpinner) {
                                    displayActivityProgressIndicator(1);

                                    report.setIncidentId("0");
                                    getIncidentsTask(report.getBuildingId());
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {}
                        });

                        //Check if the last selected building is on the list. Then set the selection
                        String lastSelectedBuildingName = Preferences.getString("lastSelectedBuildingName");
                        String lastSelectedBuildingId = Preferences.getString("lastSelectedBuildingId");
                        Log.e("LastSelected:", "ID: " + lastSelectedBuildingId + " || Name: " + lastSelectedBuildingName);

                        for(int i = 0; i < spinnerItems.size(); i++){
                            String spinnerBuildingName = spinnerItems.get(i).getValue();
                            if(spinnerBuildingName.equals(lastSelectedBuildingName)){
                                Log.d("ReportCreation","schoolBuildingSpinner, previously selected building was found");
                                SpinnerItem lastSelectedItem = spinnerItems.get(i);
                                if(spinnerItems.contains(lastSelectedItem)){
                                    Log.d("ReportCreation","schoolBuildingSpinner, setting position to "+lastSelectedBuildingName);
                                    int lastSelectedItemIndex = spinnerItems.indexOf(lastSelectedItem);
                                    buildingSpinner.setSelection(lastSelectedItemIndex);
                                    report.setBuildingId(spinnerItems.get(lastSelectedItemIndex).getId());
                                } else {
                                    Log.d("ReportCreation","schoolBuildingSpinner, previously selected building not found, setting position to 0");
                                    buildingSpinner.setSelection(0);
                                    report.setBuildingId("0");
                                }

                                break;
                            }
                            else {
                                if(i == spinnerItems.size() - 1)
                                    Log.d("ReportCreation","schoolBuildingSpinner, looped through full building list, previously selected building not found, setting position to 0");

                                buildingSpinner.setSelection(0);
                                report.setBuildingId("0");
                            }
                        }
                    } else {
                        errorLoadingReportSettings(3);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    errorLoadingReportSettings(3);
                }

                buildingCompleted = true;
//                progressDialog.dismiss();
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("NonAnonLiteReportActivity","getSchoolBuildingsTask() - bluedove-GetBuildingsByRegion - onFailure: "+error);
                errorLoadingReportSettings(3);
                buildingCompleted = true;
            }
        });

        apiHelper.prepareRequest(params, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void getIncidentsTask(final String buildingId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "GetIncidentsByBuilding");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("buildingId", buildingId);
        params.put("langCode", langCode);

        ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    LogUtils.debug("ReportCreation","getIncidentsTask() --> \n"+response);
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

                        incidentSpinner.setAdapter(new ArrayAdapter<String>(NonAnonLiteReportActivity.this,
                                // android.R.layout.simple_spinner_dropdown_item,
                                R.layout.custom_spinner,
                                incidentList) {
                            @Override
                            public boolean isEnabled(int position) {
                                return position != 0;
                            }
                        });

                        incidentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                                if(incidentTrigger){
                                    LogUtils.debug("Spinners","incidentSpinner - report.setIncidentId() - spinnerItems.get(position).getId() --> " + spinnerItems.get(position).getId());
                                    incidentTrigger = false;
                                    Log.d("ReportCreation","incidentTrigger is now off");
                                    return;
                                }

                                report.setIncidentId(spinnerItems.get(position).getId());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {}
                        });
                    } else {
                        errorLoadingReportSettings(4);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    errorLoadingReportSettings(4);
                }

                incidentCompleted = true;
            }
        });

        apiHelper.setOnErrorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("NonAnonLiteReportActivity","getIncidentsTask() - bluedove-GetIncidentsByBuilding - onFailure: "+error);
                errorLoadingReportSettings(4);
                incidentCompleted = true;
            }
        });

        apiHelper.prepareRequest(params,false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void validateNonAnonReport() {
        boolean nameWasEntered = nameEditText.getText().toString().trim().length() != 0;

        boolean emailIsFilled = emailEditText.getText().toString().length() != 0;
        emailIsValid = emailValidator.validate(emailEditText.getText().toString().replace(" ",""));

        Log.d("!!!", "EmailisValid: " + emailIsValid);

        boolean phoneIsFilled = phoneEditText.getText().toString().length() != 0;
        boolean phoneIsValid = phoneEditText.getText().toString().length() == 10;

        boolean successful = false;
        //first validate normal (anonymous) content
        try {
            boolean buildingIsSelected = !report.getBuildingId().equals("0");
            boolean incidentIsSelected = !report.getIncidentId().equals("0");
            boolean messageIsEntered = !(report.getMessage().length() == 0);

            if(!buildingIsSelected || !messageIsEntered) {
                String error = String.format(getString(R.string.you_must) + "\n%s\n%s",
                        !buildingIsSelected ? getString(R.string.choose_building) : "",
                        !messageIsEntered ? getString(R.string.enter_a_message) : "");

                showErrorAlert(error, getString(R.string.required_field_missing), getString(R.string.please_select_all_required));

                successful = false;
            } else {
                //then validate non-anonymous content
                boolean allComplete = true; //this will be used to only send the report if all scenarios check out
                //checking if the user has incident categories enabled. If so it will check if they selected a category, it will check the incident only if the category is selected first
                if(showIncidentCategorySpinner) {
                    boolean isCategorySelected = !report.getCategoryId().equals("0");
                    Log.d("Logictest", "Category is not selected"+isCategorySelected);
                    if(!isCategorySelected) {
                        String error =  getString(R.string.error_select_category);
                        showErrorAlert(error, getString(R.string.required_field_missing), getString(R.string.please_select_all_required));
                        allComplete = false;
                    } else {
                        if(!incidentIsSelected){
                            String error = getString(R.string.choose_incident);
                            showErrorAlert(error, getString(R.string.required_field_missing), getString(R.string.please_select_all_required));
                            allComplete = false;
                        }
                    }
                } else if(!incidentIsSelected) { //checking if incident is selected on account with no categories
                    String error = getString(R.string.choose_incident);
                    showErrorAlert(error, getString(R.string.required_field_missing), getString(R.string.please_select_all_required));
                    allComplete = false;
                }

                if(!nameWasEntered) {
                    String error = "You must enter your name to send this report.";

                    showErrorAlert(error, getString(R.string.missing_info), getString(R.string.please_fill_out));
                    allComplete = false;
                } else if(!emailIsFilled && !phoneIsFilled) {
                    String error = getString(R.string.missing_email_phone);

                    showErrorAlert(error, getString(R.string.missing_info), getString(R.string.please_fill_out));
                    allComplete = false;
                } else if(emailIsFilled && !emailIsValid) {
                    String error = getString(R.string.enter_a_valid_email);

                    showErrorAlert(error, getString(R.string.invalid_email), getString(R.string.please_fill_out));
                    allComplete = false;
                } else if(phoneIsFilled && !phoneIsValid) {
                    String error = getString(R.string.enter_valid_phone);

                    showErrorAlert(error, getString(R.string.invalid_phone), getString(R.string.please_fill_out));
                    allComplete = false;
                }

                if (allComplete) {
                    showSuccessAlert();
                    successful = true;
                }
            }
        } catch (Exception e) {
            Log.e("Error", "Error: " + e.toString());
            e.printStackTrace();
        }
    }

    private void showSuccessAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(R.string.confirm_report_send);
        alertDialog.setMessage(R.string.confirm_report_send_2);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                submitReportTask(nameEditText.getText().toString(), emailEditText.getText().toString(), phoneEditText.getText().toString());
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public void showErrorAlert(String error, String title, final String toast) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                context);
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
    }

    private void submitReportTask(final String naaName, final String naaEmail, final String naaPhone) {
        final ProgressIndicator progressDialog = new ProgressIndicator(this);
        progressDialog.showDialog("Please wait...");

        //IF grade != 0, ADD 2 to value of Grade Int and convert back to string for interaction with Database reasons
        if(vGradeReturnInt != 0)
            vGradeReturnInt = vGradeReturnInt + 2;

        if(aGradeReturnInt != 0)
            aGradeReturnInt = aGradeReturnInt + 2;

        String vGrade = "" + vGradeReturnInt;
        String aGrade = "" + aGradeReturnInt;

        String anonymousToggle = "0";

        //this boolean decides what procedure to use when submitting the report
        boolean hasVicAcc = Preferences.getBoolean(Config.HAS_ADD_VIC);

        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        if(hasVicAcc) {
            params.put("action", "SubmitLiteReportv2");
            params.put("victim_name", vNameReturnString);
            params.put("accused_name", aNameReturnString);
            params.put("victim_grade", vGrade);
            params.put("accused_grade", aGrade);
        } else {
            params.put("action", "SubmitLiteReport");
        }

        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("incidentId", report.getIncidentId());
        params.put("message", report.getMessage());
        params.put("buildingId", report.getBuildingId());
        params.put("submitterId", report.getSubmitterId());
        params.put("locationId", report.getLocationId());
        params.put("deviceType", "4");
        params.put("deviceId", Preferences.getString(Config.PROPERTY_REG_ID));
        params.put("unique_id", Preferences.getString(Config.UNIQUE_ID));
        params.put("naaName",naaName);
        params.put("naaEmail",naaEmail);
        params.put("naaCell",naaPhone);
        params.put("remain_anonymous", anonymousToggle);

        ApiHelper apiHelper = new ApiHelper();

        LogUtils.debug("FinalDataCalls","params from submitlitereport/v2 -> "+params);
        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    Log.d("FinalDataCalls","aa_victim_accused is: "+Preferences.getBoolean(Config.HAS_ADD_VIC)+"bluedove-submitlitereport/v2 --> response: "+response);
                    LogUtils.debug("LiteReport","response from submitReportTask() -> "+response);
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");

                    if (success) {
                        //if hasPause
                        //--> start the timer once report is successfully submitted
                        long currentTime = System.currentTimeMillis();
                        Preferences.putLong(Config.NEXT_TIME_ALLOWED, TIMER_SETTING + currentTime);
                        startTimer(TIMER_SETTING);

                        JSONObject jData = jsonResponse.getJSONObject("data");
                        alertId = jData.getInt("aa_id");
                        confirmationCode = jData.getString("confirmation_code");

                        if(hasPhotoFeature) {
                            showPhotoAlert();
                        } else {
                            //  showConfirmationCode();
                            showConfirmationScreen();
                        }

                        Toast.makeText(getApplicationContext(), R.string.report_sent_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.report_sent_fail, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), R.string.error_try_again, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        apiHelper.setOnErrorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtils.debug("NonAnonLiteReportActivity","submitReportTask() - onFailure: "+error);
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), R.string.error_try_again, Toast.LENGTH_SHORT).show();
            }
        });

        apiHelper.prepareRequest(params,true);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void showPhotoAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.picture_upload));
        alertDialog.setMessage(/*hasVideoFeature ? getString(R.string.photo_or_video) :*/ getString(R.string.photo_or_screenshot));

        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(context, CameraActivity.class);
                intent.putExtra("confirmation_code", confirmationCode);
                intent.putExtra("aa_id", String.valueOf(alertId));
                startActivity(intent);
                finish();
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // showConfirmationCode();
                showConfirmationScreen();
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void showConfirmationCode(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                context);
        alertDialog.setTitle("Report Delivered!");
        alertDialog.setMessage("Confirmation Code: " + confirmationCode);
        // alertDialog.setIcon(R.drawable.delete);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showConfirmationScreen();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }

    private void showConfirmationScreen() {
        Intent intent = new Intent(context, ConfirmCodeActivity.class);
        intent.putExtra("confirm_code", confirmationCode);
        startActivity(intent);
        finish();
    }

    private void displayActivityProgressIndicator(int activityLoadingCondition) {
        switch(activityLoadingCondition) {
            case 1:
                incidentTrigger = true;

                incidentCompleted = false;
                break;
            case 2:
                buildingTrigger = true;
                incidentTrigger = true;

                buildingCompleted = false;
                incidentCompleted = false;
                break;
            case 3:
                buildingTypeTrigger = true;
                buildingTrigger = true;
                incidentTrigger = true;

                buildingTypeCompleted = false;
                buildingCompleted = false;
                incidentCompleted = false;
                break;
            case 4:
                buildingTrigger = true;

                buildingCompleted = false;
                break;
            case 5:
                buildingTypeTrigger = true;
                buildingTrigger = true;

                buildingTypeCompleted = false;
                buildingCompleted = false;
                break;
        }

        activityProgressIndicator.show();
        LogUtils.debug("AsyncHandler","displayActivityProgressIndicator --> adding handler.post()");
        handler.post(asyncTicker);
    }

    private void errorLoadingReportSettings(int loadingCondition) {
        errorWithActivityProgress = true;
        errorLoadingCondition = loadingCondition;
    }

    private void restartActivityProgress() {
        errorLoadingCondition = -1;
        errorWithActivityProgress = false;

        submitterTrigger = true;
        regionTrigger = true;
        buildingTypeTrigger = true;
        locationTrigger = true;
        buildingTrigger = true;
        incidentTrigger = true;
        categoryTrigger = true;

        report = new Report();
        loadSpinnerItems();
    }

    private void showActivityProgressError() {
        String[] loadedLists = new String[]{"Submitter","Region","School Type","Building","Incident","Location","Categories"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");

        String alertMessage = "Failed to load your " + loadedLists[errorLoadingCondition] + " List at this time.";
        builder.setMessage(alertMessage);

        builder.setNegativeButton("Go Back to Menu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        builder.setPositiveButton("Reload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                restartActivityProgress();
            }
        });

        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private final Runnable asyncTicker = new Runnable() {
        @Override
        public void run() {
            if(submitterCompleted && regionCompleted && buildingTypeCompleted && locationCompleted && buildingCompleted && incidentCompleted && categoryCompleted) {
                LogUtils.debug("AsyncHandler","removing handler.postDelayed()");
                LogUtils.debug("AsyncHandler","handler was removed:" +
                        "\nsubmitterCompleted = "+submitterCompleted+"\nregionCompleted = "+regionCompleted+"\nbuildingTypeCompleted = "+buildingTypeCompleted+
                        "\nlocationCompleted = " + locationCompleted + "\nbuildingCompleted = "+buildingCompleted+"\nincidentCompleted = "+incidentCompleted);
                handler.removeCallbacks(this);
                LogUtils.debug("AsyncHandler","asyncTicker() - about to dimiss activityprogressindicator");

                activityProgressIndicator.dismiss();

                if(errorWithActivityProgress) {
                    //to test error condition - leads to crash without setting the value of this variable first
//                    errorLoadingCondition = 0;
                    showActivityProgressError();
                }
            } else {
                LogUtils.debug("AsyncHandler","running handler.postDelayed()");
                handler.postDelayed(this,  1000);
            }
        }
    };

    /**Check if string is empty, null, or consists of white space**/
    public static boolean isNotNullNotEmptyNotWhiteSpace(final String string) {
        return string != null && !string.isEmpty() && !string.trim().isEmpty();
    }
}