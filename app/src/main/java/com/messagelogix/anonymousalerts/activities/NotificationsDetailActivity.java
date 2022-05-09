package com.messagelogix.anonymousalerts.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.messagelogix.anonymousalerts.R;

public class NotificationsDetailActivity extends Activity {

    private static final String LOG_TAG = NotificationsDetailActivity.class.getSimpleName();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notifications_detail, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_detail);
        buildReport(getMessage(), getDate());
        buildActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, NotificationsActivity.class);
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

    private String getMessage() {
        return getIntent().getStringExtra("message");
    }

    private String getDate() {
        return getIntent().getStringExtra("schedule_datetime");
    }

    private void buildReport(String message, String date) {
        TextView tvTextView = (TextView) findViewById(R.id.PushDetailTextView);
        tvTextView.setText(Html.fromHtml("<h2>Push notifications</h2><br>" +
                "<p>Date: " + date + "</p>" +
                "<p>Message: " + message + "</p>"));
    }

}
