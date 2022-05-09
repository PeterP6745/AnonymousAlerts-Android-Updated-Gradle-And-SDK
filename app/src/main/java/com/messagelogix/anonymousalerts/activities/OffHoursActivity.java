package com.messagelogix.anonymousalerts.activities;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.messagelogix.anonymousalerts.R;

import java.util.List;

/**
 * Created by Peter on 4/28/2022.
 * This class was made as a copy of the OopsActivity but with changes made to what is displayed on the screen.
 * This activity will only be seen under certain conditions. The condition being is the account is disabled.
 */
public class OffHoursActivity extends AppCompatActivity {

    FloatingActionButton fab;
    Context context = OffHoursActivity.this;

    TextView call911TextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_off_hours_alert);
        buildActionBar();
        set911Dialer();

        SpannableStringBuilder str = new SpannableStringBuilder(getString(R.string.DisabledMessage));

        call911TextView = (TextView) findViewById(R.id.tv_911_disclaimer);
        call911TextView.setText(str);
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
        getMenuInflater().inflate(R.menu.menu_cutoff, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId){
            case R.id.action_done:
                Intent intent = new Intent(this, MenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent2 = new Intent(this, MenuActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void set911Dialer() {
        fab = (FloatingActionButton) findViewById(R.id.fab_oops);
        assert fab != null;
        fab.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EmergencyDialTwoActivity.class);
                startActivity(intent);
            }
        });
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
}
