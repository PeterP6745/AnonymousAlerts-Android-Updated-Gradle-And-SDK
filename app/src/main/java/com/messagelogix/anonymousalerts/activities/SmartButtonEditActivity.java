package com.messagelogix.anonymousalerts.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.model.Contact;
import com.messagelogix.anonymousalerts.model.SpinnerItem;
import com.messagelogix.anonymousalerts.utils.ApiHelper;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.DatabaseHandler;
import com.messagelogix.anonymousalerts.utils.FunctionHelper;
import com.messagelogix.anonymousalerts.utils.Preferences;
import com.messagelogix.anonymousalerts.utils.ProgressIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SmartButtonEditActivity extends Activity {
    private static final int SELECT_IMAGE = 1;
    private static final int CAPTURE_IMAGE = 2;
    private ImageView viewImage;
    private Context context = this;
    private List<Contact> Contacts = new ArrayList<>();
    private DatabaseHandler dbHandler;
    private ArrayAdapter<Contact> adapter;

    private static final String LOG_TAG = SmartButtonEditActivity.class.getSimpleName();
    //private String picturePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Preferences.init(context);

        setContentView(R.layout.activity_locate_me_edit);

        viewImage = (ImageView) findViewById(R.id.imageViewShowPicture);
        ImageButton addContactImageButton = (ImageButton) findViewById(R.id.contact_imageButton);
        ListView contactListView = (ListView) findViewById(R.id.contact_listView);
        ImageButton chooseImageButton = (ImageButton) findViewById(R.id.buttonAddPicture);
        EditText userNameEditText = (EditText) findViewById(R.id.user_full_name);

        buildActionBar();
        context = getApplicationContext();
        dbHandler = new DatabaseHandler(context);

        String profilePicPath = Preferences.getString(Config.USER_PROFILE_PICTURE);
        if (!profilePicPath.isEmpty()) {
            viewImage.setImageBitmap(decodeFile(profilePicPath));
        }

        String name = Preferences.getString(Config.USER_FULL_NAME);
        if (!name.isEmpty()) {
            userNameEditText.setText(name);
        }

        adapter = new ContactListAdapter();
        contactListView.setAdapter(adapter);
        contactListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a,
                                    View v, int position, long id) {
                int contactId = Contacts.get(position).getId();
                Intent intent = new Intent(v.getContext(), SmartButtonContactActivity.class);
                intent.putExtra("contactId", String.valueOf(contactId));
                startActivity(intent);
            }
        });

        addContactImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = dbHandler.getContactsCount();
                if (id >= 5) {
                    Toast.makeText(getApplicationContext(), "You have exceeded the 5 contact limit", Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(context, SmartButtonContactActivity.class);
                    i.putExtra("contactId", "0");
                    startActivity(i);
                }
            }
        });

        userNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Preferences.putString(Config.USER_FULL_NAME, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        getSchoolBuildingsTask();

        if (dbHandler.getContactsCount() != 0)
            Contacts.addAll(dbHandler.getAllContacts());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, SmartButtonActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_locate_me_edit, menu);
        return true;
    }

    public void buildActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    /**
     * Selects image from the album
     */
    private void selectImage() {
        //final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        final CharSequence[] options = {"Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(SmartButtonEditActivity.this);
        builder.setTitle("Add Photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
//                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    File file = new File(android.os.Environment.getExternalStorageDirectory(), user.getUniqueId() + ".jpg");
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
//                    startActivityForResult(intent, CAPTURE_IMAGE);
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, SELECT_IMAGE);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * Gets the absolute path of the file
     * @param context
     * @param uri
     * @return
     */
    private String getAbsolutePath(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.MediaColumns.DATA};
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);

        } catch (Exception e) {
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Create a backup copy of the picture
     * @param src
     * @param dst
     * @throws IOException
     */
    public void copyFile(File src, File dst) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException ex) {

        } catch (Exception ex) {

        } finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }

    /**
     * Function to rescale image prior to upload
     * @param path
     * @return
     */
    private Bitmap decodeFile(String path) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);
            // The new size we want to scale to
            final int REQUIRED_SIZE = 225;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;

            return BitmapFactory.decodeFile(path, o2);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * Gets the school builings
     */
    private void getSchoolBuildingsTask(){
        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "bluedove");
        params.put("action", "GetBuildings");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("typeId", "0");

        final ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                try {
                    JSONObject jsonobject = new JSONObject(responseData);
                    boolean success = jsonobject.getBoolean("success");
                    if (success) {
                        final ArrayList<SpinnerItem> spinnerItems = new ArrayList<>();
                        final ArrayList<String> buildingList = new ArrayList<>();
                        // Locate the NodeList name
                        JSONArray jsonarray = jsonobject.getJSONArray("data");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            jsonobject = jsonarray.getJSONObject(i);

                            String id = jsonobject.getString("id");
                            String values = jsonobject.getString("value");
                            spinnerItems.add(new SpinnerItem(id, values));
                            // Populate spinner
                            buildingList.add(jsonobject.optString("value"));
                        }
                        buildingList.add(0, "-Select school-");
                        spinnerItems.add(0, new SpinnerItem("0", "-Select school-"));

                        // Locate the spinner in activity_main.xml
                        Spinner mySpinner = (Spinner) findViewById(R.id.BuildingSpinner);

                        // Spinner adapter
                        mySpinner.setAdapter(new ArrayAdapter<>(SmartButtonEditActivity.this,
                                        android.R.layout.simple_spinner_dropdown_item,
                                        buildingList));

                        // Spinner on item click listener
                        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> arg0,
                                                       View arg1, int position, long arg3) {

                                String buildingId = spinnerItems.get(position).getId();
                                String buildingName = spinnerItems.get(position).getValue();

                                Preferences.putString(Config.USER_BUILDING_ID, buildingId);
                                Preferences.putString(Config.USER_BUILDING_NAME, buildingName);

                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {
                                // TODO Auto-generated method stub
                            }
                        });

                        String buildingId = Preferences.getString(Config.USER_BUILDING_ID);
                        String buildingName = Preferences.getString(Config.USER_BUILDING_NAME);
                        if (!buildingId.isEmpty() && !buildingName.isEmpty()) {
                            ArrayAdapter arrayAdapter = (ArrayAdapter) mySpinner.getAdapter();
                            mySpinner.setSelection(arrayAdapter.getPosition(buildingName));
                        }
                    }

                } catch (Exception ex) {
                    Log.d("getSchoolBuildingsTask", ex.toString());
                }
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("getSchoolBuildingsTask", error.toString());
            }
        });

        apiHelper.prepareRequest(params, false);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    /**
     * custom adapter to display contacts in a listview
     */
    private class ContactListAdapter extends ArrayAdapter<Contact> {
        public ContactListAdapter() {
            super(SmartButtonEditActivity.this, R.layout.listview_item, Contacts);
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            if (view == null)
                view = getLayoutInflater().inflate(R.layout.listview_item, parent, false);

            Contact currentContact = Contacts.get(position);
            TextView name = (TextView) view.findViewById(R.id.cNameTextView);
            name.setText(currentContact.getName());
            TextView phone = (TextView) view.findViewById(R.id.cPhoneTextView);
            phone.setText(currentContact.getPhoneNumber());
            TextView email = (TextView) view.findViewById(R.id.cEmailTextView);
            email.setText(currentContact.getEmail());

            Button deleteImageView = (Button) view.findViewById(R.id.delete_btn);
            deleteImageView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dbHandler.deleteContact(Contacts.get(position));
                    Contacts.remove(position);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(),
                            "Deleted contact", Toast.LENGTH_LONG)
                            .show();
                }
            });

            return view;
        }
    }

    private String convertToBitmap(String photoFilePath) {
        try {
            //Compress data into Bitmap
            //If we don't do this it will fail due to large size
            //or time out
            Bitmap bitmapOrg = decodeFile(photoFilePath);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            if (bitmapOrg != null) {
                bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

            return encodedImage;
        } catch(Exception ignore) {}

        return null;
    }

    private void uploadPhotoTask(final String photoFilePath){
        final ProgressIndicator progressDialog = new ProgressIndicator(this);
        progressDialog.showDialog("Uploading image, please wait...");

        String tempEncodedImage = convertToBitmap(photoFilePath);
        if(tempEncodedImage == null) {
            progressDialog.dismiss();
            return;
        }

        Toast.makeText(SmartButtonEditActivity.this, "Uploading image ...", Toast.LENGTH_LONG).show();

        HashMap<String, String> params = new HashMap<>();
        params.put("controller", "whitezebra");
        params.put("action", "UploadUserPicture");
        params.put("accountId", Preferences.getString(Config.ACCOUNT_ID));
        params.put("image", tempEncodedImage);
        params.put("uniqueId", Preferences.getString(Config.UNIQUE_ID));

        final ApiHelper apiHelper = new ApiHelper();

        apiHelper.setOnSuccessListener(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                try{
                    Log.d("FinalDataCalls","uploadphototask - whitezebra-uploaduserpicture - response: "+responseData);
                    JSONObject jsonResponse = new JSONObject(responseData);
                    boolean success = jsonResponse.getBoolean(Config.SUCCESS);
                    if (success) {
                        Preferences.putString(Config.USER_PROFILE_PICTURE, photoFilePath);
                        Toast.makeText(SmartButtonEditActivity.this, "Upload image success", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SmartButtonEditActivity.this, "Failed to upload image", Toast.LENGTH_LONG).show();
                    }
                } catch(Exception e) {
                    Toast.makeText(SmartButtonEditActivity.this, "Failed to get a response", Toast.LENGTH_LONG).show();
                }

                progressDialog.dismiss();
            }
        });

        apiHelper.setOnErrorListener(new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("populateListViewTask",error.toString());
                progressDialog.dismiss();
                Toast.makeText(SmartButtonEditActivity.this, "Error: Failed to upload image", Toast.LENGTH_LONG).show();
            }
        });

        apiHelper.prepareRequest(params, true);
        ApiHelper.getInstance(this).startRequest(apiHelper);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_CANCELED) {
            String picturePath = null;
            switch (requestCode) {
                case SELECT_IMAGE:
                    String originalPath = getAbsolutePath(context, data.getData());
                    File src = new File(originalPath);
                    File dest = new File(Environment.getExternalStorageDirectory(), Preferences.getString(Config.UNIQUE_ID) + ".jpg");
                    try {
                        copyFile(src, dest);
                        picturePath = dest.getAbsolutePath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    viewImage.setImageBitmap(decodeFile(picturePath));
                    break;
                case CAPTURE_IMAGE:
                    File file = new File(Environment.getExternalStorageDirectory(), Preferences.getString(Config.UNIQUE_ID) + ".jpg");
                    picturePath = file.getAbsolutePath();
                    viewImage.setImageBitmap(decodeFile(picturePath));
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
            Log.d(LOG_TAG, "picturePath = " + picturePath);

            if (picturePath != null) {
                uploadPhotoTask(picturePath);
            }
        }
    }
}
