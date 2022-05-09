package com.messagelogix.anonymousalerts.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.model.Contact;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.DatabaseHandler;
import com.messagelogix.anonymousalerts.utils.Preferences;

import java.util.ArrayList;
import java.util.List;

public class SmartButtonContactActivity extends Activity {

    private static final String LOG_TAG = SmartButtonContactActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private Uri uriContact;
    private String contactID;

    private Context context = this;
    private EditText nameEntry, phoneEntry, emailEntry;
    private Button saveButton;

    private DatabaseHandler dbHandler;
    private List<Contact> Contacts = new ArrayList<>();

    private int contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate_me_contact);
        buildActionBar();
        nameEntry = (EditText) findViewById(R.id.nameEditText);
        phoneEntry = (EditText) findViewById(R.id.phoneNumberEditText);
        emailEntry = (EditText) findViewById(R.id.emailAddressEditText);
        ImageButton selectContactButton = (ImageButton) findViewById(R.id.add_contact_imageButton);
        saveButton = (Button) findViewById(R.id.save_button);

        Preferences.init(context);

        String name = Preferences.getString(Config.USER_FULL_NAME);
        if (!name.isEmpty()) {
            nameEntry.setText(name);
        }
        dbHandler = new DatabaseHandler(context);

        contactId = Integer.valueOf(getIntent().getStringExtra("contactId"));
        if (contactExists()) {
            saveButton.setEnabled(true);
            saveButton.setText("Update");
            Contact contact = dbHandler.getContact(contactId);
            //populate the view
            nameEntry.setText(contact.getName());
            phoneEntry.setText(contact.getPhoneNumber());
            emailEntry.setText(contact.getEmail());
        }

        selectContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // using native contacts selection
                // Intent.ACTION_PICK = Pick an item from the data, returning what was selected.
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_PICK_CONTACTS);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = String.valueOf(nameEntry.getText());
                String phoneNumber = String.valueOf(phoneEntry.getText());
                String email = String.valueOf(emailEntry.getText());

                if(!isValidEmail(email)) {
                    Toast.makeText(getApplicationContext(), "You have entered an invalid email", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!isValidPhone(phoneNumber)) {
                    Toast.makeText(getApplicationContext(), "You have entered an phone number", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!contactExists()) {
                    int id = dbHandler.getContactsCount();
                    Contact contact = new Contact(id, name, phoneNumber, email);
                    dbHandler.createContact(contact);
                    Contacts.add(contact);
                    Toast.makeText(getApplicationContext(), name + " has been added to your Contacts!", Toast.LENGTH_SHORT).show();
                } else {
                    int id = contactId;
                    Contact contact = new Contact(id, name, phoneNumber, email);
                    dbHandler.updateContact(contact);
                    //update the user
                    Toast.makeText(getApplicationContext(), name + "'s information has been updated", Toast.LENGTH_SHORT).show();
                }
                Intent i = new Intent(context, SmartButtonEditActivity.class);
                startActivity(i);
            }
        });

        phoneEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Boolean hasPhoneOrEmail = ((!phoneEntry.getText().toString().trim().isEmpty()) ||
                        (!emailEntry.getText().toString().trim().isEmpty()));
                saveButton.setEnabled(hasPhoneOrEmail);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        emailEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Boolean hasPhoneOrEmail = ((!phoneEntry.getText().toString().trim().isEmpty()) ||
                        (!emailEntry.getText().toString().trim().isEmpty()));
                saveButton.setEnabled(hasPhoneOrEmail);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Performs validation on given email
     * @param target email
     * @return valid or not
     */
    public static boolean isValidEmail(CharSequence target) {
        return TextUtils.isEmpty(target) || android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    /**
     * Performs validation on given phone
     * @param target phone
     * @return valid or not
     */
    public final static boolean isValidPhone(CharSequence target) {
        return TextUtils.isEmpty(target) || android.util.Patterns.PHONE.matcher(target).matches();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_locate_me_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, SmartButtonEditActivity.class);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            Log.d(LOG_TAG, "Response: " + data.toString());
            uriContact = data.getData();

            String contactName = retrieveContactName();
            if (contactName.length() != 0) {
                nameEntry.setText(contactName);
            }

            String contactNumber = retrieveContactNumber();
            if (contactNumber.length() != 0) {
                phoneEntry.setText(contactNumber);
            }

            String contactEmail = retrieveContactEmail();
            if (contactEmail.length() != 0) {
                emailEntry.setText(contactEmail);
            }
        }
    }

    /**
     * retrieves the contact email
     * @return email
     */
    private String retrieveContactEmail() {
        Cursor cursor = null;
        String email = "";
        try {
            Log.v(LOG_TAG, "Got a contact result: " + uriContact.toString());
            // get the contact id from the Uri
            String id = uriContact.getLastPathSegment();
            // query for everything email
            cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?", new String[]{id}, null);
            int emailIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);

            // let's just get the first email
            if (cursor.moveToFirst()) {
                email = cursor.getString(emailIdx);

                Log.v(LOG_TAG, "Got email: " + email);
            } else {
                Log.w(LOG_TAG, "No results");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to get email data", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (email.length() == 0) {
                Toast.makeText(this, "No Email for Selected Contact", Toast.LENGTH_LONG).show();
            }
        }
        return email;
    }

    /**
     * retrieves the contact number
     * @return phone number
     */
    private String retrieveContactNumber() {
        String contactNumber = "";
        Cursor cursorPhone = null;
        try {
            // getting contacts ID
            Cursor cursorID = getContentResolver().query(uriContact,
                    new String[]{ContactsContract.Contacts._ID},
                    null, null, null);

            if (cursorID.moveToFirst()) {

                contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
            }

            cursorID.close();

            Log.d(LOG_TAG, "Contact ID: " + contactID);

            // Using the contact ID now we will get contact phone number
            cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                            ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                    new String[]{contactID},
                    null);

            if (cursorPhone.moveToFirst()) {
                contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            }

            Log.d(LOG_TAG, "Contact Phone Number: " + contactNumber);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to get email data", e);
        } finally {
            if (cursorPhone != null) {
                cursorPhone.close();
            }

            if (contactNumber.length() == 0) {
                Toast.makeText(this, "No Phone number for Selected Contact", Toast.LENGTH_LONG).show();
            }
        }

        return contactNumber;
    }

    /**
     * retrieves the contact name
     * @return the name
     */
    private String retrieveContactName() {

        String contactName = "";
        Cursor cursor = null;
        try {
            // querying contact data store
            cursor = getContentResolver().query(uriContact, null, null, null, null);

            if (cursor.moveToFirst()) {

                // DISPLAY_NAME = The display name for the contact.
                // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            }

            Log.d(LOG_TAG, "Contact Name: " + contactName);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to get email data", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (contactName.length() == 0) {
                Toast.makeText(this, "No Contact name for Selected Contact", Toast.LENGTH_LONG).show();
            }
        }

        return contactName;
    }

    /**
     * Checks if the contact exist
     * @return true or false
     */
    private boolean contactExists() {
        return contactId != 0;
    }
}
