package com.messagelogix.anonymousalerts.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.messagelogix.anonymousalerts.BuildConfig;
import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.utils.LogUtils;
import com.messagelogix.anonymousalerts.utils.RetryCounter;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.FunctionHelper;
import com.messagelogix.anonymousalerts.utils.Preferences;
import com.messagelogix.anonymousalerts.utils.ApiHelper;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {


    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private Boolean mAuthTask = null;


    // Values for email and password at the time of the login attempt.
    private String mUsername;
    private String mPassword;


    // UI references.
    private EditText mAccountView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;
    private CheckBox rememberMeCheckbox;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Check language preference
        Preferences.init(context);
        if(FunctionHelper.isNullOrEmpty(Preferences.getString("langCode"))){
            Preferences.putString("langCode", "en");
        }

        rememberMeCheckbox = (CheckBox)findViewById(R.id.rememberMeCheckBox);

        if(Preferences.getBoolean("shouldRememberMe")){

            rememberMeCheckbox.setChecked(Preferences.getBoolean("shouldRememberMe"));

            if(!FunctionHelper.isNullOrEmpty(Preferences.getString("savedUsername"))){
                mUsername = Preferences.getString("savedUsername");
            }
            if(!FunctionHelper.isNullOrEmpty(Preferences.getString("savedPassword"))){
                mPassword = Preferences.getString("savedPassword");
            }
        }

        mAccountView = (EditText) findViewById(R.id.account);
        mAccountView.setText(mUsername);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setText(mUsername);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id,
                                          KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        //Login Button
        findViewById(R.id.sign_in_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptLogin();
                    }
                });

        findViewById(R.id.login_info_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Create an Alert Dialog
                        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);
                        dlgAlert.setMessage(R.string.activation_code_explanation);
                        dlgAlert.setTitle(R.string.activation_code);
                        dlgAlert.setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss the dialog
                                dialog.dismiss();
                            }
                        });
                        dlgAlert.setCancelable(true);
                        dlgAlert.create().show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mAccountView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mUsername = mPasswordView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }


        // Check for a valid email address.
        if (TextUtils.isEmpty(mUsername)) {
            mAccountView.setError(getString(R.string.error_field_required));
            focusView = mAccountView;
            cancel = true;
        } else if (mUsername.length() == 0) {
            mAccountView.setError(getString(R.string.error_invalid_account));
            focusView = mAccountView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            mAuthTask = true;//new UserLoginTask();
            //mAuthTask.execute();
            userLoginTask();
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void userLoginTask() {
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "redbear");
        params.put("action", "AuthenticateEncryption");
        params.put("username", mUsername);
        params.put("password", mPassword);

        final ApiHelper apiHelper  = new ApiHelper();
        final RetryCounter retryCounter = new RetryCounter();

        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mAuthTask = null;
                showProgress(false);

                try {
                    LogUtils.debug("[LoginActivity]","userLoginTask --> "+response);
                    //throw new JSONException("error");
                    JSONObject responseJsonObject = new JSONObject(response);
                    boolean success = responseJsonObject.getBoolean(Config.SUCCESS);
                    if (success) {
                        //Authenticated. Store any important account information
                        JSONObject data = responseJsonObject.getJSONObject(Config.DATA);
                        String accountId = data.getString(Config.ACCOUNT_ID);
                        Preferences.putString(Config.ACCOUNT_ID, accountId);
                        Preferences.putInteger(Config.LAST_VERSION_CODE, BuildConfig.VERSION_CODE);
                        Preferences.putBoolean("shouldRememberMe", rememberMeCheckbox.isChecked());
                        //If remember me checkbox is checked, store username and password for easy access
                        if(rememberMeCheckbox.isChecked()){
                            Preferences.putString("savedUsername", mUsername);
                            Preferences.putString("savedPassword", mPassword);
                        }
                        //Else reset the values that are currently being stored for username and password
                        else{
                            Preferences.putString("", mUsername);
                            Preferences.putString("", mPassword);
                        }
                        Preferences.putBoolean(Config.IS_LOGGED_IN, true);
                        //new GetCustomTextTask(accountId).execute();
                        getCustomTextTask(accountId);
                    } else {
                        mAccountView.setError(getString(R.string.error_invalid_account));
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

        apiHelper.setOnErrorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int currRC = retryCounter.getRetryCount();
                if(currRC == 0) {
                    mAuthTask = null;
                    showProgress(false);
                }
                else {
                    retryCounter.decrementRetryCount();
                    //LogUtils.debug("covid19surveytask","retryPolicy of temp obj: "+temp.getRetryPolicy().toString());
                    ApiHelper.getInstance(LoginActivity.this).startRequest(apiHelper);
                }
            }
        });

        apiHelper.prepareRequest(params,false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void getCustomTextTask(final String accountId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "redbear");
        params.put("action", "GetCustomTextv2");
        params.put("accountId", accountId);
        params.put("langCode", Preferences.getString("langCode"));

        ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                LogUtils.debug("[LoginActivity]","getCustomTextTask --> "+response);
                try {
                    JSONObject responseJsonObject = new JSONObject(response);
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

                        Intent intent = new Intent(context, MainActivity.class);
                        startActivity(intent);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        apiHelper.setOnErrorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtils.debug("[GetCustomTextTask]","error --> "+error.toString());
            }
        });

        apiHelper.prepareRequest(params, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }
}

