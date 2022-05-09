package com.messagelogix.anonymousalerts.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.utils.ApiHelper;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.FunctionHelper;
import com.messagelogix.anonymousalerts.utils.Preferences;
import com.messagelogix.anonymousalerts.utils.Strings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class SendReport2Activity extends Activity {

    private static final String LOG_TAG = SendReport2Activity.class.getSimpleName();

    private final Context context = this;

    private String aalertId = "0";
    private String confirmationCode = "0";
    private String fullName = "";
    private String emailAddress = "";
    private String emailAddressEncrypted = "";
    private String emailAddressEncryptedConfirmation = "";
    private String cellPhoneString = "";
    private String cellPhoneString2 = "";

    private EditText nameEditText, emailEditText, encryptedEmail, encryptedEmail2;

    private EditText cellPhoneEditText1, cellPhoneEditText2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_report2);
        setTouchListenerForKeyboardDismissal();
        Preferences.init(context);

        nameEditText = (EditText) findViewById(R.id.name_editText);
        emailEditText = (EditText) findViewById(R.id.email_editText);
        encryptedEmail = (EditText) findViewById(R.id.encrypted_editText1);
        encryptedEmail2 = (EditText) findViewById(R.id.encrypted_editText2);
        cellPhoneEditText1 = (EditText) findViewById(R.id.cellEditText1);
        cellPhoneEditText2 = (EditText) findViewById(R.id.cellEditText2);

        TextView step2Label1 = (TextView) findViewById(R.id.step3TextView1);
        TextView step2Label2 = (TextView) findViewById(R.id.step3TextView2);

        confirmationCode = getIntent().getStringExtra("confirmation_code");

        TextView confirmationCode = (TextView) findViewById(R.id.confirmation_textView);
        confirmationCode.setText(this.confirmationCode);

        aalertId = getIntent().getStringExtra("aa_id");


        step2Label1.setText(Html.fromHtml(Preferences.getString(Config.STEP2_CONTACT_HTML)));
        step2Label2.setText(Html.fromHtml(Preferences.getString(Config.STEP2_FILL_OUT_FORM_HTML)));

        boolean hasCellFeature = Preferences.getBoolean(Config.HAS_SMS);

        if (!hasCellFeature) {
            cellPhoneEditText1.setVisibility(View.INVISIBLE);
            cellPhoneEditText2.setVisibility(View.INVISIBLE);
        }

        addListenerOnButton();
        //buildActionBar();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

//    public void buildActionBar() {
//        ActionBar actionBar = getActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setDisplayShowHomeEnabled(true);
//            actionBar.setHomeButtonEnabled(true);
//        }
//    }

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

    private boolean validateForm() {
        return (emailAddressEncrypted.equals(emailAddressEncryptedConfirmation));
    }

    public void setTouchListenerForKeyboardDismissal() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.RelativeLayout1);
        layout.setOnTouchListener(new View.OnTouchListener() {
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

    public void showNoContactPrompt() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SendReport2Activity.this);
        alertDialog.setTitle(R.string.step_2_info_missing);
        alertDialog.setMessage(R.string.step_2_info_missing_2);

        alertDialog.setPositiveButton(R.string.yes_continue,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, SendReport3Activity.class);
                        intent.putExtra("confirmation_code",confirmationCode);
                        startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel_go_back, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    public void showErrorAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SendReport2Activity.this);
        alertDialog.setTitle(R.string.email_mistmatch);
        alertDialog.setMessage(R.string.email_mismatch_2);

        alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), R.string.email_mismatch_3, Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_report2, menu);
        return true;
    }

    public void addListenerOnButton() {
        Button submit = (Button) findViewById(R.id.submitBtn1);
        submit.setOnClickListener(new OnClickListener() {


            @Override
            public void onClick(View arg0) {
                fullName = nameEditText.getText().toString();
                emailAddress = emailEditText.getText().toString();
                emailAddressEncrypted = encryptedEmail.getText().toString();
                emailAddressEncryptedConfirmation = encryptedEmail2.getText().toString();
                cellPhoneString = cellPhoneEditText1.getText().toString();
                cellPhoneString2 = cellPhoneEditText2.getText().toString();

                boolean nonAnonEmailIsEmpty = Strings.isNullOrEmpty(emailAddress);
                boolean anonEmailIsEmpty = Strings.isNullOrEmpty(emailAddressEncrypted);
                boolean anonEmailConfirmationIsEmpty = Strings.isNullOrEmpty(emailAddressEncryptedConfirmation);
                boolean anonPhone1IsEmpty = Strings.isNullOrEmpty(cellPhoneString);
                boolean nonAnonPhoneIsEmpty = Strings.isNullOrEmpty(cellPhoneString2);

//                //check upper half (Anonymous)
//                //If anon email field is filled out
//                if(!anonEmailIsEmpty){
//                    //if confirmation field is filled out
//                    if(!anonEmailConfirmationIsEmpty){
//                        //go on
//                    }
//                    else{
//                        //alert for missing info
//                    }
//                }
//                //Else, check anon cell number
//                else{
//                    //if cell is given
//                    if(!anonPhone1IsEmpty){
//                        //go on
//                    }
//                    else{
//                        //alert for missing info
//                    }
//                }


                if(Strings.isNullOrEmpty(emailAddress) &&
                        Strings.isNullOrEmpty(emailAddressEncrypted) &&
                        Strings.isNullOrEmpty(emailAddressEncryptedConfirmation) &&
                        Strings.isNullOrEmpty(cellPhoneString) &&
                        Strings.isNullOrEmpty(cellPhoneString2)){

                    showNoContactPrompt();
                }else{
                    processUserInfo();
                }

            }

        });
    }

    private void processUserInfo() {
        if (validateForm()) {
            submitUserInfoTask();
        } else {
            showErrorAlert();
        }
    }

    private void submitUserInfoTask(){
        final ProgressDialog pDialog = new ProgressDialog(SendReport2Activity.this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();

        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "SaveAllUserInfo");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("aalertId", aalertId);
        params.put("confirmationCode", confirmationCode);
        params.put("anonymousEmail", emailAddressEncrypted);
        params.put("phoneNumber", cellPhoneString);
        params.put("phoneNumber2", cellPhoneString2);
        params.put("name", fullName);
        params.put("email", emailAddress);

        final ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                Log.d("FinalDataCalls","bluedove-SaveAllUserInfo --> response: "+responseData);
                pDialog.dismiss();
                try{
                    Intent intent = new Intent(context, SendReport3Activity.class);
                    intent.putExtra("confirmation_code",confirmationCode);
                    startActivity(intent);
                }catch (Exception e){
                    Log.d("submitUserInfoTask",e.toString());
                }
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("submitUserInfoTask",error.toString());
                pDialog.dismiss();
            }
        });

        apiHelper.prepareRequest(params, true);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
