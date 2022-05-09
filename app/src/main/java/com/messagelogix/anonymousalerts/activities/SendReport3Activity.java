package com.messagelogix.anonymousalerts.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.model.SpinnerItem;
import com.messagelogix.anonymousalerts.utils.ApiHelper;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.FunctionHelper;
import com.messagelogix.anonymousalerts.utils.Preferences;
import com.messagelogix.anonymousalerts.utils.ProgressIndicator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class SendReport3Activity extends Activity {

    private Context context = this;
    private static final String LOG_TAG = SendReport3Activity.class.getSimpleName();
    private String PhoneNumber;
    private String confirmationCode = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_report3);

        Preferences.init(context);

        TextView Step3Label1 = (TextView) findViewById(R.id.step3TextView1);
        Step3Label1.setText(Html.fromHtml(Preferences.getString(Config.STEP3_MESSAGE_SENT_HTML)));

        TextView Step3Label2 = (TextView) findViewById(R.id.step3TextView2);
        Step3Label2.setText(Html.fromHtml(Preferences.getString(Config.STEP3_EMERGENCY_NUMBER_HTML)));

        ImageView smiley = (ImageView) findViewById(R.id.tvwSadSmiley);
        smiley.setColorFilter(Color.rgb(255, 196, 0));

        getPhoneNumbersTask();
        addListenerOnButton();
        buildActionBar();

        //get confirmation code
        confirmationCode = getIntent().getStringExtra("confirmation_code");

        TextView confirmationCodeTextView = (TextView) findViewById(R.id.confirmation_code_text_view_step_3);
        String confirmationString = getString(R.string.confirmation_code) + ": " + confirmationCode;
        confirmationCodeTextView.setText(confirmationString);
    }

    public void buildActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    public void addListenerOnButton() {
        final Context context = this;

        Button doneButton = (Button) findViewById(R.id.Donebtn);
        Button callButton = (Button) findViewById(R.id.Callbtn);
        callButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + PhoneNumber));
                startActivity(dialIntent);
            }

        });

        doneButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_report3, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, MenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
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

    private void getPhoneNumbersTask() {
        final ProgressIndicator progressDialog = new ProgressIndicator(this);
        progressDialog.showDialog("Please wait...");

        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "redbear");
        params.put("action", "GetPhoneNumbersV2");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));

        ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("FinalDataCalls","redbear-getphonenumbersv2 --> response: "+response);
                try {
                    JSONObject jsonobject = new JSONObject(response);
                    boolean success = jsonobject.getBoolean("success");
                    if (success) {
                        final ArrayList<SpinnerItem> spinnerItems = new ArrayList<>();
                        final ArrayList<String> phoneList = new ArrayList<>();

                        JSONArray jsonarray = jsonobject.getJSONArray("data");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            jsonobject = jsonarray.getJSONObject(i);

                            String phone_description = jsonobject.getString("phone_description");
                            String phone_number = jsonobject.getString("phone_number");

                            spinnerItems.add(new SpinnerItem(phone_description, phone_number));
                            // Populate spinner
                            phoneList.add(phone_description);
                        }

                        Spinner spinnerPhone = (Spinner) findViewById(R.id.phoneSpinner);

                        TextView phoneNumber = (TextView) findViewById(R.id.phoneTextView);
                        Button callBtn = (Button) findViewById(R.id.Callbtn);
                        phoneNumber.setVisibility(View.VISIBLE);
                        callBtn.setVisibility(View.VISIBLE);
                        spinnerPhone.setVisibility(View.VISIBLE);

                        spinnerPhone.setAdapter(new ArrayAdapter<>(SendReport3Activity.this,
                                        android.R.layout.simple_spinner_dropdown_item,
                                        phoneList));

                        spinnerPhone = (Spinner) findViewById(R.id.phoneSpinner);

                        int spinnerCount = spinnerPhone.getCount();
                        Log.d(LOG_TAG,"Phone count: " + spinnerCount);
                        spinnerPhone.setOnItemSelectedListener(new OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                                       int position, long id) {
                                PhoneNumber = spinnerItems.get(position).getValue();
                                TextView text = (TextView) findViewById(R.id.phoneTextView);
                                text.setText(PhoneNumber);

                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {

                            }

                        });
                        //TextView phoneNumber = (TextView) findViewById(R.id.phoneTextView);
                        //Button callBtn = (Button) findViewById(R.id.Callbtn);
//                        if (spinnerCount == 0){
//                            phoneNumber.setVisibility(View.GONE);
//                            callBtn.setVisibility(View.GONE);
//                            spinnerPhone.setVisibility(View.GONE);
//                        }

                    } else {
                        Log.d(LOG_TAG, "failed to retrieve phone number");
                        Spinner spinnerPhone = (Spinner) findViewById(R.id.phoneSpinner);
                        TextView phoneNumber = (TextView) findViewById(R.id.phoneTextView);
                        Button callBtn = (Button) findViewById(R.id.Callbtn);
                        phoneNumber.setVisibility(View.GONE);
                        callBtn.setVisibility(View.GONE);
                        spinnerPhone.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }

                progressDialog.dismiss();
            }
        });

        apiHelper.setOnErrorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.e("Error", ""+error);
                error.printStackTrace();
            }
        });

        apiHelper.prepareRequest(params, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(this, MenuActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
    }
}
