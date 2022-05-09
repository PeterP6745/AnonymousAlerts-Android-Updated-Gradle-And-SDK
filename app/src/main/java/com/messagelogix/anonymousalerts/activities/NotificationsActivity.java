package com.messagelogix.anonymousalerts.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.utils.ProgressIndicator;
import com.messagelogix.anonymousalerts.utils.LogUtils;
import com.messagelogix.anonymousalerts.utils.ApiHelper;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.Preferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationsActivity extends Activity {

    private static final String LOG_TAG = NotificationsActivity.class.getSimpleName();
    public static final String PROPERTY_REG_ID = "registration_id";

    final Context mContext = this;

    private ListView notificationListView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notifications, menu);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        setTitle(getString(R.string.title_activity_notifications));

        notificationListView = (ListView) findViewById(R.id.notificationListView);

        buildActionBar();

        Preferences.init(mContext);
        String registrationId = Preferences.getString(Config.PROPERTY_REG_ID);
        if (registrationId.isEmpty()) {
            Log.d(LOG_TAG, "Registration not found.");
        } else {
//            new PopulateListViewTask(registrationId).execute();
            getNotificationsList(registrationId);
        }
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void buildActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void getNotificationsList(final String registrationId){
        final ApiHelper apiHelper = new ApiHelper();
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "greencow");
        params.put("action", "GetPushAlerts");
        params.put("deviceId", registrationId);

        final ProgressIndicator progressDialog = new ProgressIndicator(this);
        progressDialog.showDialog("Loading your notifications, please wait...");

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                progressDialog.dismiss();
                Log.d("FinalDataCalls","greencow-getpushalerts --> response: "+responseData);
                LogUtils.debug("[NotificationsActivity]","onSuccess --> "+responseData);
                try {
                    JSONObject jsonResponse = new JSONObject(responseData);
                    Boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        final ArrayList<String> pushMessageArray = new ArrayList<>();
                        final ArrayList<String> dateTimeArray = new ArrayList<>();

                        JSONArray data = jsonResponse.getJSONArray("data");

                        for (int i = 0; i < data.length(); i++) {
                            dateTimeArray.add(data.getJSONObject(i).getString("schedule_datetime"));
                            pushMessageArray.add(data.getJSONObject(i).getString("message"));
                        }

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(NotificationsActivity.this,
                                android.R.layout.simple_list_item_1, pushMessageArray);
                        notificationListView.setAdapter(arrayAdapter);
                        notificationListView.setOnItemClickListener(new OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> a,
                                                    View v, int position, long id) {
                                String message = pushMessageArray.get(position);
                                String dateTime = dateTimeArray.get(position);

                                Intent intent = new Intent(v.getContext(), NotificationsDetailActivity.class);
                                intent.putExtra("message", message);
                                intent.putExtra("schedule_datetime", dateTime);
                                startActivity(intent);
                            }
                        });
                    }
                } catch (Exception e) {
                    LogUtils.debug("[NotificationsActivity]","Catch exception --> "+e.toString());
                    e.printStackTrace();
                }
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                LogUtils.debug("[NotificationsActivity]","onError --> "+error.toString());
            }
        });

        apiHelper.prepareRequest(params, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }
}
