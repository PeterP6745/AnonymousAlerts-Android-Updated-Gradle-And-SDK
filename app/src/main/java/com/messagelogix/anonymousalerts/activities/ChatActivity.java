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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.messagelogix.anonymousalerts.Adapter.MessageChatAdapter;
import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.model.Chat;
import com.messagelogix.anonymousalerts.model.Message;
import com.messagelogix.anonymousalerts.model.MessageChat;
import com.messagelogix.anonymousalerts.utils.ApiHelper;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.FunctionHelper;
import com.messagelogix.anonymousalerts.utils.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Richard on 6/22/2017.
 */
public class ChatActivity extends Activity {

    private EditText inputMsg;

    // Chat messages list adapter
    private MessageChatAdapter adapter;

    private List<MessageChat> listMessages;

    public Message.MessageItem message;

//    = ChatActivity.this;
    private String submissionTypeId;

    public String adminDeviceId;

    private String chatId = "";

    boolean canRespond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);
        buildActionBar();
        setTitle(getString(R.string.chat_center));
        Button btnSend = (Button) findViewById(R.id.btnSend);
        inputMsg = (EditText) findViewById(R.id.inputMsg);
        ListView listViewMessages = (ListView) findViewById(R.id.list_view_messages);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sending message server
                if(canRespond){
//                    new SendMessageChatTask(inputMsg.getText().toString()).execute();
                    sendMessageChat(inputMsg.getText().toString());
                }
                else{
                    showToast(getString(R.string.cannot_send_message_at_this_time));
                }
                // Clearing the input filed once message was sent
                inputMsg.setText("");
            }
        });
        listMessages = new ArrayList<MessageChat>();
        adapter = new MessageChatAdapter(this, listMessages);
        listViewMessages.setAdapter(adapter);

        submissionTypeId = getIntent().getStringExtra("submission_type");
        message = new Message.MessageItem();
        message.setAaAlertId(getIntent().getStringExtra("aaAlertId"));
        message.setMessage(getIntent().getStringExtra("message"));
        message.setSubmissionTypeId(submissionTypeId);
        message.setSenderName("");
        //Log.d("Submission Type: ", " " + submissionTypeId);
        if (message != null) {
            populateChatView(message);
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
            Intent intent = new Intent(this, MessageCenterActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(mMessageReceiver, new IntentFilter("update_messagecenter_and_chatmessages"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mMessageReceiver);

    }

    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //update chat tables
            if(!FunctionHelper.isNullOrEmpty(chatId))
                updateMessageList(chatId);
        }
    };

    private void populateChatView(Message.MessageItem m) {
        MessageChat messageChat = new MessageChat(m.getSenderName(), m.getMessage(), false);
        appendMessage(messageChat);

        message.setSenderName(getIntent().getStringExtra("sender_name"));
        String mReportId = message.getAaAlertId();
        chatId = mReportId;
        getMessageChat(mReportId);
//        new ClearNewMessageTask().execute();
        clearNewMessageTask();
   //     if (m.getReplyType().equals("200")) {
          //  new FunctionHelper(context).showToast("User will receive a text message");
   //     } else if (m.getReplyType().equals("500")) {
         //   new FunctionHelper(context).showToast("User will receive an email message");
    //    }
    }

    /**
     * Appending message to list view
     */
    private void appendMessage(final MessageChat m) {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                listMessages.add(m);
//                adapter.notifyDataSetChanged();
                // Playing device's notification
                //playBeep();
            }
        });
    }

    private void showToast(final String message) {
        final Context that = this;
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(that, message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendMessageChat(final String replyMessage){
        final ApiHelper apiHelper = new ApiHelper();

        final HashMap<String, String> postDataParams = new HashMap<>();
        postDataParams.put("controller", "GrayBoar");
        postDataParams.put("action", "SendReportMessage2");
        postDataParams.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        postDataParams.put("pinId", "0");
        postDataParams.put("aalertId", message.getAaAlertId());
        postDataParams.put("message", replyMessage);
        postDataParams.put("sender_device", adminDeviceId);
        postDataParams.put("receiver_device",Preferences.getString(Config.PROPERTY_REG_ID)); //receiver = student = this

        Log.d("ChatActivity","sendMessageChat() - GrayBoar-SendReportMessage2 - params: " + postDataParams);

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                try {
//                    Log.d("FinalDataCalls","grayboar-sendreportmessage2 --> response: "+responseData);
                    JSONObject json = new JSONObject(responseData);
                    if (json.getBoolean("success")) {
                        String name = "";
                        MessageChat message = new MessageChat(name, replyMessage, false);
                        appendMessage(message);
                        adapter.notifyDataSetChanged();
                        showToast("Message sent");

                        //if has push, send a push as well
//                      new PushToAdmin(replyMessage).execute();
                        pushToAdmin(replyMessage);
                    } else {
                        showToast("Message failed to send");
                    }
                } catch (Exception ex) {
                    Log.d("[ChatActivity]","sendMessageChat -> Success Error"+ex.toString());
                    showToast("An error was encountered while processing your message. Please try again.");
                }
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ChatActivity","sendMessageChat() - GrayBoar-SendReportMessage2 - onSuccess: " + error);
                showToast("Message failed to send");
            }
        });

        apiHelper.prepareRequest(postDataParams, true);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void getMessageChat(final String id){
        final ApiHelper apiHelper = new ApiHelper();

        HashMap<String, String> postDataParams = new HashMap<>();
        postDataParams.put("controller", "GrayBoar");
        postDataParams.put("action", "GetReportMessageChat");
        postDataParams.put("aalertId", id);

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                try {
                    Log.d("FinalDataCalls","grayboar-getreportmessagechat --> response: "+responseData);
                    Log.d("OnNotifReceived","grayboar-getreportmessagechat --> response: "+responseData);

//                    Log.d("FinalDataCalls","grayboar-getreportmessagechat --> response: "+responseData);
                    Gson gson = new GsonBuilder().create();
                    Chat chat = gson.fromJson(responseData, Chat.class);
                    if (chat.getSuccess()) {
                        List<Chat.ChatItem> chatItems = chat.getData();

                        int prevMessageListSize = listMessages.size();
                        int tempIndex = 0;

                        for (Chat.ChatItem chatItem : chatItems) {
                            String currentMessage = chatItem.getMessage();
                            boolean senderIsAdmin = !chatItem.getPinId().equals("0");//0 is user
                            String sender;
                            if (senderIsAdmin) {
                                sender = "Admin";//chatItem.getContact();
                            } else {
                                sender = !FunctionHelper.isNullOrEmpty(message.getSenderName()) ? message.getSenderName() : "Anonymous User";
                            }
                            MessageChat tempMessage = new MessageChat(sender, currentMessage, senderIsAdmin);

//                            if(tempIndex > prevMessageListSize-1) {
                                appendMessage(tempMessage);
                                adminDeviceId = !FunctionHelper.isNullOrEmpty(chatItem.getAdminDeviceId()) ? chatItem.getAdminDeviceId() : " ";
                                adminDeviceId = adminDeviceId.replace(" ","");
//                            }

                            tempIndex++;
                        }
                        Log.d("ADMIN DEVICE TOKEN: ",adminDeviceId);

                        adapter.notifyDataSetChanged();
                        canRespond = true;
                    } else {
                        //  showToast("No replies yet");
                        canRespond = false;
                    }
                } catch (Exception e) {
                    canRespond = false;
                }
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ChatActivity","getMessageChat() - GrayBoar-GetReportMessageChat - onFailure: " + error);
                canRespond = false;
            }
        });

        apiHelper.prepareRequest(postDataParams, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    public void updateMessageList(final String id) {
        final ApiHelper apiHelper = new ApiHelper();

        HashMap<String, String> postDataParams = new HashMap<>();
        postDataParams.put("controller", "GrayBoar");
        postDataParams.put("action", "GetReportMessageChat");
        postDataParams.put("aalertId", id);

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                try {
                    Log.d("FinalDataCalls","updateMessageList() --> grayboar-getreportmessagechat --> response: "+responseData);
                    Log.d("OnNotifReceived","updateMessageList() --> grayboar-getreportmessagechat --> response: "+responseData);
                    printListMessages(true);

                    Gson gson = new GsonBuilder().create();
                    Chat chat = gson.fromJson(responseData, Chat.class);
                    if (chat.getSuccess()) {
                        List<Chat.ChatItem> chatItems = chat.getData();

                        printChatMessages(chatItems,true);

                        int currMessageListSize = chatItems.size();
                        int prevMessageListSize = listMessages.size()-1;
                        int sizeDiff = currMessageListSize - prevMessageListSize;

                        Log.d("APPEND","updateMessageList() --> currMessageList size: "+currMessageListSize+" prevMessageList size: "+prevMessageListSize);
//                        int tempIndex = 0;

                        //currMessageListSize-sizeDiff | currMessageListSize
                        for(int i=currMessageListSize-sizeDiff; i<currMessageListSize; i++) {
                            Log.d("APPEND","\nnew message "+i+": "+chatItems.get(i).getMessage());
                            String currentMessage = chatItems.get(i).getMessage();
                            boolean senderIsAdmin = !chatItems.get(i).getPinId().equals("0");//0 is user
                            String sender;

//                            if(tempIndex > prevMessageListSize-1) {
                                if (senderIsAdmin)
                                    sender = "Admin";
                                else
                                    sender = !FunctionHelper.isNullOrEmpty(message.getSenderName()) ? message.getSenderName() : "Anonymous User";

                                MessageChat tempMessage = new MessageChat(sender, currentMessage, senderIsAdmin);

                                appendMessage(tempMessage);
                                adminDeviceId = !FunctionHelper.isNullOrEmpty(chatItems.get(i).getAdminDeviceId()) ? chatItems.get(i).getAdminDeviceId() : " ";
                                adminDeviceId = adminDeviceId.replace(" ","");
//                            }

//                            tempIndex++;
                        }

                        Log.d("ADMIN DEVICE TOKEN: ",adminDeviceId);
                        printListMessages(false);
                        adapter.notifyDataSetChanged();
                        canRespond = true;
                    } else
                        canRespond = false;
                } catch (Exception e) {
                    canRespond = false;
                }
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ChatActivity","updateMessageList() - GrayBoar-GetReportMessageChat - onFailure: " + error);
                canRespond = false;
            }
        });

        apiHelper.prepareRequest(postDataParams, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    public void clearNewMessageTask(){
        final ApiHelper apiHelper = new ApiHelper();

        HashMap<String, String> postDataParams = new HashMap<>();
        postDataParams.put("controller", "GrayBoar");
        postDataParams.put("action", "ClearNewMessages");
        postDataParams.put("aa_id", message.getAaAlertId());
        postDataParams.put("view_type", "1");

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                try {
                    JSONObject json = new JSONObject(responseData);
                    if (json.getBoolean("success")) {
                        Log.d("NEW MESSAGES CLEARED!", "Yup");
                    }
                } catch (Exception ex) {
                    Log.d("clearNewMessageTask", ex.toString());
                }
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("clearNewMessageTask", error.toString());
            }
        });

        apiHelper.prepareRequest(postDataParams, true);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void pushToAdmin(final String replyMessage){
        final ApiHelper apiHelper = new ApiHelper();

        Log.d("AAResponse","rMessage String is: "+replyMessage);

        if(adminDeviceId.length() == 64)
            submissionTypeId = "3"; //IOS
        else
            submissionTypeId = "4"; //Android

        HashMap<String, String> postDataParams = new HashMap<>();
        postDataParams.put("controller", "GrayBoar");
        postDataParams.put("action", "SendAAPush");
        postDataParams.put("message",replyMessage);
        postDataParams.put("device_token",adminDeviceId);
        postDataParams.put("device_type", submissionTypeId);

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                try {
                    //Log.d("FinalDataCalls","grayboar-sendaapush --> response: "+responseData);
                    JSONObject json = new JSONObject(responseData);
                    if (json.getBoolean("success")) {
                        Log.d("FinalDataCalls","pushToAdmin() - grayboay-sendaapush - response: "+responseData);
                    }
                    else {}
                } catch (JSONException ex) {}
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ChatActivity","pushToAdmin() - GrayBoard-SendAAPush - onFailure: "+error);
            }
        });

        apiHelper.prepareRequest(postDataParams, true);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    private void printListMessages(boolean before) {
        StringBuilder str = new StringBuilder("MESSAGE LIST START --> ");
        for(int i=0; i<listMessages.size(); i++) {
            str.append(listMessages.get(i).getMessage());
            str.append(" ");
        }
        str.append("MESSAGE LIST END");

        if(before)
            Log.d("APPEND","BEFORE\n"+str);
        else
            Log.d("APPEND","AFTER\n"+str);
    }

    private void printChatMessages(List<Chat.ChatItem>chatItems, boolean before) {
        StringBuilder str = new StringBuilder("CHAT ITEMS START --> ");
        for(int i=0; i<chatItems.size(); i++) {
            str.append(chatItems.get(i).getMessage());
            str.append(" ");
        }
        str.append("CHAT ITEMS END");

        if(before)
            Log.d("APPEND","BEFORE\n"+str);
        else
            Log.d("APPEND","AFTER\n"+str);
    }
}
