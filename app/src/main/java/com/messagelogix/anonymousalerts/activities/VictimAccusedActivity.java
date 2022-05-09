package com.messagelogix.anonymousalerts.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.messagelogix.anonymousalerts.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Richard on 1/5/2018.
 */
public class VictimAccusedActivity extends AppCompatActivity {

    private EditText vicNameET;
    private EditText accNameET;

    private Spinner vicGradeSpinner;
    private Spinner accGradeSpinner;

    private Button addToReportButton;

    private String victimNameReturnString = "";
    private String accuseNameReturnString = "";

    private String victimGradeReturnString = "";
    private String accusedGradeReturnString = "";

    private int selectedVicGradeReturnInt = 0;
    private int selectedAccGradeReturnInt = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_victim);
        buildActionBar();
        /**Get Extras**/
        victimNameReturnString = getIntent().getExtras().getString("victim_name","");
        accuseNameReturnString = getIntent().getExtras().getString("accused_name","");
        victimGradeReturnString = getIntent().getExtras().getString("victim_grade", "");
        accusedGradeReturnString = getIntent().getExtras().getString("accused_grade", "");
        selectedVicGradeReturnInt = getIntent().getIntExtra("victim_grade_index", 0);
        selectedAccGradeReturnInt = getIntent().getIntExtra("accused_grade_index", 0);
        initComponents();
    }

    private void initComponents(){
        /**Edit Text**/
        //Victim Name
        vicNameET = (EditText) findViewById(R.id.textedit_vic_name);
        vicNameET.setText(victimNameReturnString);
        //Accused Name
        accNameET = (EditText) findViewById(R.id.textedit_acc_name);
        accNameET.setText(accuseNameReturnString);
        /**Spinner**/
        vicGradeSpinner = (Spinner) findViewById(R.id.spinner_vic_grade);
        accGradeSpinner = (Spinner) findViewById(R.id.spinner_acc_grade);

        /**Populate Spinners**/
        populateGradeSpinner(vicGradeSpinner);
        populateGradeSpinner(accGradeSpinner);
        //Set selected index
        vicGradeSpinner.setSelection(selectedVicGradeReturnInt);
        accGradeSpinner.setSelection(selectedAccGradeReturnInt
        );

        /**VIC_GRADE_Spinner OnItemSelectedListener**/
        vicGradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGrade = vicGradeSpinner.getItemAtPosition(position).toString();
                selectedVicGradeReturnInt = position;
                victimGradeReturnString = selectedGrade;
               // Log.e("","Selected Vic Grade: " + selectedGrade);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /**ACC_GRADE_Spinner OnItemSelectedListener**/
        accGradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGrade = accGradeSpinner.getItemAtPosition(position).toString();
                selectedAccGradeReturnInt = position;
                accusedGradeReturnString = selectedGrade;
             //   Log.e("","Selected Acc Grade: " + selectedGrade);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /**Add to Report Button**/
        addToReportButton = (Button) findViewById(R.id.AddToReportButton);
        addToReportButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /**On Click**/
                victimNameReturnString = vicNameET.getText().toString();
                accuseNameReturnString = accNameET.getText().toString();

                Intent intent = new Intent();
                intent.putExtra("victim_name", victimNameReturnString);
                intent.putExtra("accused_name", accuseNameReturnString);
                intent.putExtra("victim_grade", victimGradeReturnString);
                intent.putExtra("accused_grade", accusedGradeReturnString);
                intent.putExtra("victim_grade_index", selectedVicGradeReturnInt);
                intent.putExtra("accused_grade_index", selectedAccGradeReturnInt);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void populateGradeSpinner(Spinner gradeSpinner){
        // you need to have a list of data that you want the spinner to display
        List<String> spinnerArray =  new ArrayList<String>();

        for(int i = 0; i<=12;i++){
            spinnerArray.add("" + i);
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        gradeSpinner.setAdapter(adapter);
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
        getMenuInflater().inflate(R.menu.menu_cancel_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId){
            case R.id.action_done:
                finish();
//                Intent intent = new Intent(this, MenuActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
