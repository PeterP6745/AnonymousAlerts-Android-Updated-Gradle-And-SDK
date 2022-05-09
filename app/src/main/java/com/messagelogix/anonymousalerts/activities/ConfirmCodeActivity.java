package com.messagelogix.anonymousalerts.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.utils.FunctionHelper;

/**
 * Created by Richard on 7/17/2017.
 */
public class ConfirmCodeActivity extends Activity {

    //private ImageView smileyImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmation_code_activity);
        buildActionBar();

        //smileyImageView = (ImageView) findViewById(R.id.image_view_confirmation_smiley);
        TextView confirmationCodeTextView = (TextView) findViewById(R.id.confirmation_code_text_view);
        String confirmCode = getIntent().getStringExtra("confirm_code");
        if(!FunctionHelper.isNullOrEmpty(confirmCode)){
            confirmationCodeTextView.setText(confirmCode);
        } else {
            Log.d("ConfirmCode", "isNull!");
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
            // app icon in action bar clicked; go home
            Intent intent = new Intent(this, MenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
