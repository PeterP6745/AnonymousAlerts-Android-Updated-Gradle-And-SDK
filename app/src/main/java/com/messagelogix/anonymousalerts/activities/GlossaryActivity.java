package com.messagelogix.anonymousalerts.activities;

import android.app.ActionBar;
import android.app.Activity;
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
import com.messagelogix.anonymousalerts.utils.ApiHelper;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.Preferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class GlossaryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glossary);
        buildActionBar();
        setTitle(getString(R.string.title_activity_glossary));
        getGlossaryTask();
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
        getMenuInflater().inflate(R.menu.glossary, menu);
        return true;
    }

    public void getGlossaryTask(){
        final ApiHelper apiHelper = new ApiHelper();

        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluerabbit");
        params.put("action", "GetGlossary");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("langCode", Preferences.getString("langCode"));

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                try {
                    JSONObject jsonobject = new JSONObject(responseData);
                    boolean success = jsonobject.getBoolean(Config.SUCCESS);
                    if (success) {
                        final ArrayList<String> termsList = new ArrayList<>();
                        final ArrayList<String> definitionList = new ArrayList<>();

                        JSONArray jsonarray = jsonobject.getJSONArray("data");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            jsonobject = jsonarray.getJSONObject(i);
                            String term = jsonobject.getString("glossary_title");
                            String definition = jsonobject.getString("glossary_description");
                            termsList.add(term);
                            definitionList.add(definition);
                        }

                        ListView GlossaryListView = (ListView) findViewById(R.id.listView1);
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(GlossaryActivity.this, android.R.layout.simple_list_item_1, termsList);
                        GlossaryListView.setAdapter(arrayAdapter);
                        GlossaryListView.setOnItemClickListener(new OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> a,
                                                    View v, int position, long id) {
                                String definition = definitionList.get(position);
                                Intent intent = new Intent(v.getContext(), GlossaryDetailActivity.class);
                                intent.putExtra("termDefinition", definition);
                                startActivity(intent);
                            }
                        });
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("GlossaryActivity","getGlossaryTask() - bluerabbit-GetGlossary - onFailure: "+error);
            }
        });

        apiHelper.prepareRequest(params, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }
}
