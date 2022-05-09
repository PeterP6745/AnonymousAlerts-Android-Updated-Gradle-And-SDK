package com.messagelogix.anonymousalerts.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.android.volley.VolleyError;
import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.utils.ProgressIndicator;
import com.messagelogix.anonymousalerts.model.HelpResource;
import com.messagelogix.anonymousalerts.model.Parent;
import com.messagelogix.anonymousalerts.utils.ApiHelper;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.HelpExpandableAdapter;
import com.messagelogix.anonymousalerts.utils.Preferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class HelpActivity extends Activity {

    private String LOG_TAG = "";

    //Sync flag
    boolean syncFlag = false;

    private Activity activity;
    private String accountId;

    Parent how_does_it_works;
    Parent how_does_it_links;
    Parent how_does_it_videos;
    Parent how_does_it_youtube;
    ArrayList<String> arrayChildrenHow;
    ArrayList<String> arrayChildrenLinks;
    ArrayList<String> arrayChildrenVideos;
    ArrayList<String> arrayChildrenYoutube;
    ArrayList<Parent> arrayParents;

    int helpTextCounter = -1;
    int helpLinkCounter = -1;
    int helpVideoCounter = -1;
    int helpYoutubeCounter = -1;

    HashMap<Integer, HelpResource> hmHelpTexts = new HashMap<>();
    HashMap<Integer, HelpResource> hmHelpLinks = new HashMap<>();
    HashMap<Integer, HelpResource> hmHelpVideos = new HashMap<>();
    HashMap<Integer, HelpResource> hmHelpYoutubes = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        activity = this;
        Context context = getApplicationContext();

        setTitle(getString(R.string.help_and_resources));

        ExpandableListView mExpandableList = (ExpandableListView) findViewById(R.id.expandable_list);

        Preferences.init(context);
        accountId = Preferences.getString(Config.ACCOUNT_ID);

        arrayParents = new ArrayList<>();
        how_does_it_works = new Parent(context);
        how_does_it_links = new Parent(context);
        how_does_it_videos = new Parent(context);
        how_does_it_youtube = new Parent(context);
        arrayChildrenHow = new ArrayList<>();
        arrayChildrenLinks = new ArrayList<>();
        arrayChildrenVideos = new ArrayList<>();
        arrayChildrenYoutube = new ArrayList<String>();

        //sets the adapter that provides data to the list.
        mExpandableList.setAdapter(new HelpExpandableAdapter(this, arrayParents));

        mExpandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    Log.d(LOG_TAG, "groupPosition = " + groupPosition + " | childPosition = " + childPosition);
                    HelpResource tempHelpResource = new HelpResource();

                    switch (groupPosition) {
                        case 0:
                            tempHelpResource = hmHelpTexts.get(childPosition);
                            Intent intent = new Intent(HelpActivity.this, TextActivity.class);
                            intent.putExtra("value", tempHelpResource.getValue());
                            startActivity(intent);
                            break;
                        case 1:
                            tempHelpResource = hmHelpLinks.get(childPosition);
                            String resourceUrl = tempHelpResource.getValue();
                            if(resourceUrl.startsWith("mailto:")){
                                Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(resourceUrl));
                                startActivity(intent1);
                            } else {
                                Intent intent1 = new Intent(getApplicationContext(), WebViewActivity.class);
                                intent1.putExtra("url", resourceUrl);
                                startActivity(intent1);
                            }
                            break;
                        case 2:
                            tempHelpResource = hmHelpVideos.get(childPosition);
                            Intent intent2 = new Intent(getApplicationContext(), VideoActivity.class);
                            intent2.putExtra("url", tempHelpResource.getValue());
                            startActivity(intent2);
                            break;
                        case 3:
                            tempHelpResource = hmHelpYoutubes.get(childPosition);
                            Intent intent3 = new Intent(getApplicationContext(), YoutubeActivity.class);
                            intent3.putExtra("url",tempHelpResource.getValue());
                            startActivity(intent3);

                            break;

                        default:
                            Intent intent4 = new Intent(getApplicationContext(), WebViewActivity.class);
                            intent4.putExtra("url", tempHelpResource.getValue());
                            startActivity(intent4);
                            break;
                    }

                    return true;
                }
            }
        );

        //Async Tasks
        //Get all schools
        getAllResourcesTask();

        //After async task
        //How does it works section
        how_does_it_works = new Parent(context);
        how_does_it_works.setIconIndex(0);
        how_does_it_works.setTitle("How Does It Work");

        how_does_it_works.setArrayChildren(arrayChildrenHow);
        arrayParents.add(how_does_it_works);

        //Links
        how_does_it_links = new Parent(context);
        how_does_it_links.setIconIndex(1);
        how_does_it_links.setTitle("Links");

        how_does_it_links.setArrayChildren(arrayChildrenLinks);
        arrayParents.add(how_does_it_links);

        //Videos
        how_does_it_videos = new Parent(context);
        how_does_it_videos.setIconIndex(2);
        how_does_it_videos.setTitle("Videos");

        how_does_it_videos.setArrayChildren(arrayChildrenVideos);
        arrayParents.add(how_does_it_videos);

        //Youtube
        how_does_it_youtube = new Parent(context);
        how_does_it_youtube.setIconIndex(3);
        how_does_it_youtube.setTitle("Youtube");
        how_does_it_youtube.setArrayChildren(arrayChildrenYoutube);
        arrayParents.add(how_does_it_youtube);

        buildActionBar();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            // app icon in action bar clicked; go home
            Intent intent = new Intent(this, MenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.help, menu);
        return true;
    }

    private void getAllResourcesTask (){
        final ProgressIndicator progressDialog = new ProgressIndicator(this);
        progressDialog.showDialog("Loading resources, please wait");

        final ArrayList<HelpResource> helpResources = new ArrayList<>();
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "redhorse");
        params.put("action", "GetHelpResources");
        params.put("accountId", accountId);

        final ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                Log.d("[HelpActivity]","onSuccess --> "+responseData);
                try {
                    JSONObject jsonobject = new JSONObject(responseData);
                    boolean success = jsonobject.getBoolean("success");
                    //throw new JSONException("error");
                    if(success) {
                        JSONArray jsonarray = jsonobject.getJSONArray("data");

                        for (int i = 0; i < jsonarray.length(); i++) {

                            jsonobject = jsonarray.getJSONObject(i);

                            HelpResource tempHelpResource = new HelpResource();

                            tempHelpResource.setTitle(jsonobject.getString("title"));
                            tempHelpResource.setValue(jsonobject.getString("value"));
                            tempHelpResource.setType(jsonobject.getString("type"));

                            helpResources.add(tempHelpResource);

                            switch (tempHelpResource.getType()) {
                                case "Video":
                                    arrayChildrenVideos.add(tempHelpResource.getTitle());
                                    helpVideoCounter++;
                                    hmHelpVideos.put(helpVideoCounter, tempHelpResource);
                                    break;

                                case "Weblink":
                                    arrayChildrenLinks.add(tempHelpResource.getTitle());
                                    helpLinkCounter++;
                                    hmHelpLinks.put(helpLinkCounter, tempHelpResource);
                                    break;

                                case "Text":
                                    arrayChildrenHow.add(tempHelpResource.getTitle());
                                    helpTextCounter++;
                                    hmHelpTexts.put(helpTextCounter, tempHelpResource);
                                    break;

                                case "Youtube":
                                    arrayChildrenYoutube.add(tempHelpResource.getTitle());
                                    helpYoutubeCounter++;
                                    hmHelpYoutubes.put(helpYoutubeCounter, tempHelpResource);
                                    break;
                            }

                            Log.d(LOG_TAG, "Type = " + tempHelpResource.getType()
                                    + "Title = " + tempHelpResource.getTitle()
                                    + "Type = " + tempHelpResource.getType()
                                    + "Value = " + tempHelpResource.getValue()
                                    + " Sync Flag: " + syncFlag);

                        }
                        Log.d(LOG_TAG, "Helps = " + helpResources.toString() + " Sync Flag: " + syncFlag);
                        syncFlag = true;
                    }
                } catch (Exception ignore) {}

                progressDialog.dismiss();
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("[HelpActivity]","getAllResourcesTask() - redhorse-GetHelpResources - onFailure: "+error);
                progressDialog.dismiss();
            }
        });

        apiHelper.prepareRequest(params, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }
}
