package com.messagelogix.anonymousalerts.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.Preferences;


public class EmergencyDialActivity extends Activity {

    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_dial);

        Preferences.init(context);

        Button dial911 = (Button) findViewById(R.id.dial911button);
        dial911.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Uri number = Uri.parse("tel:911");
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                startActivity(callIntent);
            }


        });
        Button hotlineDial = (Button) findViewById(R.id.dialHotlineButton);
        TextView hotLineLabel = (TextView) findViewById(R.id.hotLineTextView);

        if (Preferences.getBoolean(Config.HAS_SUICIDE_HOTLINE)) {
            //display
            hotlineDial.setVisibility(View.VISIBLE);
            hotLineLabel.setVisibility(View.VISIBLE);
            //set text
            hotLineLabel.setText(Preferences.getString(Config.HOT_LINE_LABEL));
            hotlineDial.setText(Preferences.getString(Config.SUICIDE_HOT_LINE));

            hotlineDial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Uri number = Uri.parse("tel:" + Preferences.getString(Config.SUICIDE_HOT_LINE));
                    Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                    startActivity(callIntent);
                }
            });
        } else {
            hotlineDial.setVisibility(View.GONE);
            hotLineLabel.setVisibility(View.GONE);
        }

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_emergency_dial, menu);
        return true;
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


}
