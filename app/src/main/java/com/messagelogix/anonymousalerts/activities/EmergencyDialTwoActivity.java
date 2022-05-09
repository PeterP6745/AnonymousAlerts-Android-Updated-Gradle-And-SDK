package com.messagelogix.anonymousalerts.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.messagelogix.anonymousalerts.Adapter.EmergencyDialExpListAdapter;
import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.utils.ApiHelper;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Richard on 7/14/2017.
 */
public class EmergencyDialTwoActivity extends Activity {

    public ExpandableListView expandableListView;
    public HashMap<String, List<String>> numbersHashMap;
    public List<String> headers;
    public EmergencyDialExpListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_emergency_dial_2);
        buildActionBar();
        setTitle(getString(R.string.emergency_dial));
        expandableListView = (ExpandableListView) findViewById(R.id.emergency_dial_expandable_list_view);
        numbersHashMap = new HashMap<>();
        headers = new ArrayList<>();

        getPhoneNumbersTask();

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String selectedNumber = (String) adapter.getChild(groupPosition,childPosition);
              //  Log.d("SELECTED NUMBER",selectedNumber);
                dialNumber(selectedNumber);
                return false;
            }
        });
    }

    public void dialNumber(String num){
        Uri number = Uri.parse("tel:"+num);
        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
        if (isCallingSupported(this, callIntent)) {
            //Send to dial pad
            startActivity(callIntent);
        } else {
            Toast.makeText(this,"Phone call is not supported in this device", Toast.LENGTH_SHORT).show();
        }
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

    /**---------------------------------DB CALL----------------------------------------------------------------------*/
    public void getPhoneNumbersTask(){
        final Context that = this;
        final ApiHelper apiHelper = new ApiHelper();
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "redbear");
        params.put("action", "GetEmergencyPhoneNumber");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("langCode", Preferences.getString("langCode"));

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

                            String header = jsonobject.getString("header");
                            String phoneNumber = jsonobject.getString("phonenumber");

                            Log.d("", "Header: " + header + "\nPhone Number: " + phoneNumber);

                            List<String> phoneNumbers = new ArrayList<String>();
                            phoneNumbers.add(phoneNumber);
                            headers.add(header);
                            numbersHashMap.put(header, phoneNumbers);
                        }

                        adapter = new EmergencyDialExpListAdapter(that, numbersHashMap, headers);
                        expandableListView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("EmergencyDialTwoAct","getPhoneNumbersTask() - redbear-GetEmergencyPhoneNumber - onFailure: "+error);
            }
        });

        apiHelper.prepareRequest(params, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
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
}
