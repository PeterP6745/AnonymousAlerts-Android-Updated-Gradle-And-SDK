package com.messagelogix.anonymousalerts.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.messagelogix.anonymousalerts.Adapter.CustomAdapterMessageCenter;
import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.utils.ProgressIndicator;
import com.messagelogix.anonymousalerts.utils.LogUtils;
import com.messagelogix.anonymousalerts.model.Message;
import com.messagelogix.anonymousalerts.utils.ApiHelper;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.Preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jeremy on 6/21/2017.
 */
public class MessageCenterActivity extends Activity {

    ListView mListView;
    CustomAdapterMessageCenter adapter;
//    Context context = MessageCenterActivity.this;
    private Message messages;
    private List<Message.MessageItem> data = new ArrayList<>();
    Context context = MessageCenterActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_center);
        buildActionBar();
        buildActionBar();
        mListView = (ListView) findViewById(R.id.message_center_list_view);
        setTitle(getString(R.string.message_center));

        //Load Data into ArrayList
        loadMessageSummary();

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
        switch (itemId){
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

    @Override
    protected void onResume() {
        super.onResume();
        context.registerReceiver(mMessageReceiver, new IntentFilter("update_messagecenter_and_chatmessages"));

    }

    @Override
    protected void onPause() {
        super.onPause();
//        setContext(this);
        context.unregisterReceiver(mMessageReceiver);

    }
    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //update message list
        getMessageSummary();
        }
    };


    private void loadMessageSummary(){
        getMessageSummary();

        //set onItemClickListener for List
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String alertId = data.get(position).getAaAlertId();
                String submissionType = data.get(position).getSubmissionTypeId();
                String firstMessage = data.get(position).getMessage();
                Intent intent = new Intent(context,ChatActivity.class);
                intent.putExtra("aaAlertId", alertId);
                intent.putExtra("submission_type", submissionType);
                intent.putExtra("message", firstMessage);
                intent.putExtra("sender_name", data.get(position).getSenderName());
                startActivity(intent);
            }
        });
        //initialize adapter with Context and ArrayList of MessageSummaryItems
       // adapter = new CustomAdapterMessageCenter(context,messageSummaryItems);
       // mListView.setAdapter(adapter);
    }

    /**
     * Get Message Summary
     */
    private void getMessageSummary(){
        final ProgressIndicator progressDialog = new ProgressIndicator(this);
        progressDialog.showDialog("Loading your messages, please wait...");

        final ApiHelper apiHelper = new ApiHelper();

        HashMap<String, String> postDataParams = new HashMap<>();
        postDataParams.put("controller", "GrayBoar");
        postDataParams.put("action", "GetReportMessageSummary");
        postDataParams.put("deviceId", Preferences.getString(Config.UNIQUE_ID));
        postDataParams.put("account_id", Preferences.getString(Config.ACCOUNT_ID));

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                Log.d("OnNotifReceived","grayboar-getreportmessagesummary --> response: "+responseData);
//                LogUtils.debug("[MessageCenterActivity]","onSuccess: "+responseData);
                progressDialog.dismiss();
                try {
                    Gson gson = new GsonBuilder().create();
                    messages = gson.fromJson(responseData, Message.class);
                    data = messages.getData();
                    if(messages.getSuccess()) {
                        // Set the adapter
                        //mListView = (AbsListView) this.view.findViewById(android.R.id.list);
                        // Set OnItemClickListener so we can be notified on item clicks
                        //mListView.setOnItemClickListener(MessageFragment.this);
                        //mListView.setAdapter(adapter);
                        adapter = new CustomAdapterMessageCenter(context, data);
                        mListView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } else {
                        //     new FunctionHelper(context).showToast("Messages are not available");
                        showErrorMessage();
                    }
                } catch (Exception e) {
                    LogUtils.debug("[MessageCenterActivity]","Catch exception"+e.toString());
                    showErrorMessage();
                }
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.d("[MessageCenterActivity]", "onError --> "+error.toString());
                showErrorMessage();
            }
        });

        apiHelper.prepareRequest(postDataParams, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void showErrorMessage() {
//        TextView errorText = new TextView(context);
//        errorText.setText("Failed to load messages. Please reload the page.");
//        errorText.setTextSize(20);
//        errorText.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        LinearLayout messageCenterLayout = findViewById(R.id.messageCenterLayout);
//        messageCenterLayout.addView(errorText);
        mListView.setVisibility(View.GONE);
        messageCenterLayout.findViewById(R.id.errorView).setVisibility(View.VISIBLE);
    }

    public Context getContext() {
        return context;
    }
    public void setContext(Context context) {
        this.context = context;
    }

}
