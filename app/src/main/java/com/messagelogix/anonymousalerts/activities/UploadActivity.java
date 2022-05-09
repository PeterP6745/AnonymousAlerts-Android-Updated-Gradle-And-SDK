package com.messagelogix.anonymousalerts.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.utils.AndroidMultiPartEntity;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.FunctionHelper;
import com.messagelogix.anonymousalerts.utils.LogUtils;
import com.messagelogix.anonymousalerts.utils.Preferences;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class UploadActivity extends Activity {
    private Context actContext;
    // LogCat tag
    private static final String LOG_TAG = UploadActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private String filePath = null;
    private String compressedFilePath = null;

    private TextView txtPercentage;
    private ImageView imgPreview;
    private VideoView vidPreview;
    long totalSize = 0;

    private static String aaCode;
    private static String aaId;
    private static String accountId;
    private boolean hasLite;

    private boolean isAnonymousByToggle;

    HttpClient global_httpClient;
    HttpPost global_httpPost;
    HttpResponse global_httpResponse;
    HttpEntity global_r_entity;

    //Uncomment to use with K12Alerts' Media File Upload System
//    HttpURLConnection global_connection;
//    DataOutputStream global_outputStream;
//    FileInputStream global_fileInputStream;

    InterruptThread global_interruptThread;
    int uploadTimeoutPeriod = 30 * 60 * 1000; //(# of min * 60 seconds * 1000) = equivalent # of microseconds

    Button uploadButton;
    Button cancelButton;
    Boolean loadingConfirmationActivity = null;
    boolean wasRequestCancelled = false;
    boolean onBackPressedEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        actContext = this;

        txtPercentage = (TextView) findViewById(R.id.txtPercentage);

        uploadButton = (Button) findViewById(R.id.btnUpload);
        cancelButton = (Button) findViewById(R.id.cancelButton);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        vidPreview = (VideoView) findViewById(R.id.videoPreview);

        Preferences.init(this);
        accountId = Preferences.getString(Config.ACCOUNT_ID);
        hasLite = Preferences.getBoolean(Config.HAS_LITE);

        // Receiving the data from previous activity
        Intent i = getIntent();

        // image or video path that is captured in previous activity
        filePath = i.getStringExtra("filePath");

        compressedFilePath = i.getStringExtra("compressedFilePath");
        LogUtils.debug("CompressedFile","UploadActivity - passed compressedFilePath is: "+compressedFilePath);

        aaId = i.getStringExtra("aa_id");
        aaCode = i.getStringExtra("confirmation_code");
        isAnonymousByToggle = i.getExtras().getBoolean("reportIsAnonymousByToggle");

        // boolean flag to identify the media type, image or video
        boolean isImage = i.getBooleanExtra("isImage", true);
        if(filePath != null) {
            // Displaying the image or video on the screen
            previewMedia(isImage);
        } else {
            Toast.makeText(getApplicationContext(), "Sorry, file path is missing!", Toast.LENGTH_LONG).show();
        }

        global_interruptThread = new InterruptThread();
        global_httpClient = new DefaultHttpClient();
        global_httpPost = new HttpPost(Config.API_UPLOAD_MEDIA_URL);

//        global_connection = null;

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressedEnabled = false;
                disableUploadButton();
//                progressBar.setVisibility(View.VISIBLE);
                RelativeLayout progressBarLayout = (RelativeLayout) findViewById(R.id.progressBarLayout);
                progressBarLayout.setVisibility(View.VISIBLE);
                // uploading the file to server
                new UploadFileToServer().execute();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wasRequestCancelled = true;
                global_interruptThread.interrupt();
                releaseHTTPObjects();
                showErrorPrompt();
            }
        });
    }

    private void disableUploadButton() {
        uploadButton.setEnabled(false);
        uploadButton.setBackgroundResource(R.drawable.uploadbutton_disabled);
        uploadButton.setTextColor(getColor(R.color.black));
    }

    /**
     * Displaying captured image/video on the screen
     */
    private void previewMedia(boolean isImage) {
        // Checking whether captured media is image or video
        if (isImage) {
            imgPreview.setVisibility(View.VISIBLE);
            vidPreview.setVisibility(View.GONE);

            BitmapFactory.Options options = new BitmapFactory.Options();

            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

            imgPreview.setImageBitmap(bitmap);
        } else {
            imgPreview.setVisibility(View.GONE);
            vidPreview.setVisibility(View.VISIBLE);
            vidPreview.setVideoPath(filePath);
            // start playing
            vidPreview.start();
        }
    }

    /**
     * Uploading the file to server
     */
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        UploadFileToServer() {
            LogUtils.debug("CompressedFile","inside constructor of UploadFileToServer");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // setting progress bar to zero
            progressBar.setProgress(0);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            LogUtils.debug("CompressedFile","UploadActivity - received onProgressUpdate()");

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            String tempPercentage = String.valueOf(progress[0]) + "%";
            txtPercentage.setText(tempPercentage);
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {

            String responseString;

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(new AndroidMultiPartEntity.ProgressListener() {
                    @Override
                    public void transferred(long num) {
                        publishProgress((int) ((num / (float) totalSize) * 100));
                    }
                });

                File sourceFile = new File(compressedFilePath);
//                String file_size = Formatter.formatShortFileSize(getApplicationContext(),sourceFile.length());
                LogUtils.debug("CameraProcess","passed image compressedfile size: "+sourceFile.length());
                LogUtils.debug("VideoProcess","passed video compressedfile size: "+sourceFile.length());

                // Adding file data to http body
                entity.addPart("media_file", new FileBody(sourceFile));

                LogUtils.debug("myLog", "aa_id" + aaId + "confirmation_code" + aaCode + "accountId" + accountId);
                // Extra parameters if you want to pass to server
                entity.addPart("api_key", new StringBody(Config.API_KEY));
                entity.addPart("app_id", new StringBody(Config.APP_ID));
                entity.addPart("controller", new StringBody("bluedove"));
                entity.addPart("action", new StringBody("AttachMedia"));
                entity.addPart("aa_id", new StringBody(aaId));
                entity.addPart("confirmation_code", new StringBody(aaCode));
                entity.addPart("accountId", new StringBody(accountId));

                totalSize = entity.getContentLength();
                global_httpPost.setEntity(entity);

                global_interruptThread.start();

                // Making server call
                global_httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
                global_httpResponse = global_httpClient.execute(global_httpPost);

                global_r_entity = global_httpResponse.getEntity();

                int statusCode = global_httpResponse.getStatusLine().getStatusCode();
                if(statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(global_r_entity);
                    LogUtils.debug("CompressedFile","UploadActivity - bluedove-attachmedia --> response: "+responseString);
                } else {
                    LogUtils.debug("CompressedFile","UploadActivity - status code is NOT 200");
                    responseString = "Error occurred! Http Status Code: " + statusCode;
                }

                global_interruptThread.interrupt();

            } catch (Exception e) {
                LogUtils.debug("CompressedFile","UploadActivity - encountered exception");
                responseString = e.toString();
                global_interruptThread.interrupt();
            }

            releaseHTTPObjects();

            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result != null && !wasRequestCancelled) {
                LogUtils.debug("CompressedFile", "UploadActivity - onPostExecute - Response from server: " + result);
                LogUtils.debug(LOG_TAG, "Response from server: " + result);
                String message;
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    boolean success = jsonObject.getBoolean("success");
                    LogUtils.debug("CompressedFile","UploadActivity - onPostExecute - response success value is: "+success);
                    if (success)
                        message = getString(R.string.upload_success);
                    else
                        message = getString(R.string.upload_failed);
                } catch(JSONException ex) {
                    LogUtils.debug("CompressedFile","UploadActivity - onPostExecute - encountered exception");
                    message = "An error occurred please try again later";
                }

                showAlert(message);
            }
        }
    }

    /*
     * Copy of K12Alerts' Media File Upload System
     */

    //    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            progressBar.setProgress(0);
//        }
//
//        @Override
//        protected String doInBackground(Void... voids) {
//            return this.upload(compressedFilePath);
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... progress) {
//            super.onProgressUpdate(progress);
//            LogUtils.debug("CompressedFile","UploadActivity - received onProgressUpdate()");
//            // Making progress bar visible
//            progressBar.setVisibility(View.VISIBLE);
//
//            // updating progress bar value
//            progressBar.setProgress(progress[0]);
//
//            // updating percentage value
//            txtPercentage.setText(String.valueOf(progress[0]) + "%");
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//
//            if(result != null && !wasRequestCancelled) {
//                Log.e("UploadFileToServer", "UploadActivity - onPostExecute - Response from server: " + result);
//                Log.e(LOG_TAG, "Response from server: " + result);
//                String message;
//                try {
//                    JSONObject jsonObject = new JSONObject(result);
//                    boolean success = jsonObject.getBoolean("success");
//                    LogUtils.debug("UploadFileToServer","UploadActivity - onPostExecute - response success value is: "+success);
//                    if (success)
//                        message = getString(R.string.upload_success);
//                    else
//                        message = getString(R.string.upload_failed);
//                } catch(JSONException ex) {
//                    LogUtils.debug("UploadFileToServer","UploadActivity - onPostExecute - encountered exception");
//                    message = "An error occurred please try again later";
//                }
//
////                if(hasLite){
//                   // showConfirmationCode();
////                    showConfirmationScreen();
//                    showAlert(message);
////                } else {
//                    // showing the server response in an alert dialog
////                    showAlert(message);
////                }
//            }
//        }
//
//        public String upload(String pathToOurFile) {
////            global_connection = null;
//            global_outputStream = null;
//
//            String lineEnd = "\r\n";
//            String twoHyphens = "--";
//            String boundary =  "*****";
//
//            int bytesRead;
//            byte[] buffer;
//            int maxBufferSize = 8192;
//
//            StringBuilder serverResponseMessage = new StringBuilder();
//            InterruptThread interruptThread = new InterruptThread();
//            try
//            {
//                File audioFile = new File(pathToOurFile);
//                try {
//                    global_fileInputStream = new FileInputStream(audioFile);
//                } catch(Exception e) {
//                    LogUtils.debug("UploadFileToServer","exception encountered when opening fileinputstream --> "+e);
//                }
//
//                HashMap<String, String> params = new HashMap();
//                params.put("api_key",Config.API_KEY);
//                params.put("app_id",Config.APP_ID);
//                params.put("controller","bluedove");
//                params.put("action","UploadMedia");
//                params.put("aa_id",aaId);
//                params.put("confirmation_code",aaCode);
//                params.put("accountId",accountId);
//                String urlServer = Config.API_UPLOAD_MEDIA_URL + "?" + FunctionHelper.getPostDataString(params);
//
//                URL url = new URL(urlServer);
//                Log.d("UploadFileToServer","urlServer: "+urlServer+"\npathtoourfile: "+pathToOurFile);
//
//                global_connection = (HttpURLConnection) url.openConnection();
////			connection.setFixedLengthStreamingMode((int) audioFileTemp.length() + 152);
//                global_connection.setConnectTimeout(5000);
////                global_connection.setReadTimeout(30000);
//
//                // Allow Inputs & Outputs
//                global_connection.setDoInput(true);
//                global_connection.setDoOutput(true);
//                global_connection.setUseCaches(false);
//
//                // Enable POST method
//                global_connection.setRequestMethod("POST");
//                global_connection.setRequestProperty("Connection", "Keep-Alive");
//                global_connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
//
//                global_interruptThread.start();
//
//                //Original
//                try {
//                    global_outputStream = new DataOutputStream(new BufferedOutputStream(global_connection.getOutputStream()));
//                } catch(Exception e) {
//                    Log.d("UploadFileToServer","Exception encountered getting outputstream: "+e);
//                }
//
//                global_outputStream.writeBytes(twoHyphens + boundary + lineEnd);
//                global_outputStream.writeBytes("Content-Disposition: form-data; name=\"media_file\";filename=\"" + pathToOurFile +"\"" + lineEnd);
//                global_outputStream.writeBytes(lineEnd);
//                global_outputStream.flush();
//
//                long total = 0;
//                int currLength = 0;
//                LogUtils.debug("UploadFileToServer","Read bytes are 0");
//                buffer = new byte[maxBufferSize];
//                while ((bytesRead = global_fileInputStream.read(buffer, 0, maxBufferSize)) > 0) {
//                    global_outputStream.write(buffer, 0, bytesRead);
//                    currLength += bytesRead;
//                    LogUtils.debug("UploadFileToServer","Now read "+currLength+" bytes");
//                    publishProgress((int) ((currLength / (float) audioFile.length()) * 100));
//                }
//                global_outputStream.flush();
//                global_fileInputStream.close();
//
//                //If these lines are included, cause file not found exception
//                global_outputStream.writeBytes(lineEnd);
//                global_outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
//                global_outputStream.flush();
//                global_outputStream.close();
//
//                try {
////                long total = 0;
////                int currLength = 0;
//
//                    InputStreamReader inStreamReader = new InputStreamReader(new BufferedInputStream(global_connection.getInputStream()));
//                    String line;
//                    BufferedReader br = new BufferedReader(inStreamReader);
//                    while ((line = br.readLine()) != null) {
//                        serverResponseMessage.append(line);
//                        Log.d("UploadFileToServer","inside input stream - serverResponseMessage is: "+serverResponseMessage);
//                    }
////                while ((currLength = br.read(buffer)) != -1) {
////                    fos.write(buffer, 0, len1);
////                    total += len1;
////                    publishProgress((int) (total * 100 / fileLength));
////                }
//                    // Responses from the server (code and message)
////                    int serverResponseCode = global_connection.getResponseCode();
////                    serverResponseMessage.append(global_connection.getResponseMessage());
////                    LogUtils.debug("UploadFileToServer","serverResponseCode was: "+serverResponseCode);
//                } catch(Exception e) {
//                    Log.d("UploadFileToServer","Exception encountered getting inputstream: "+e);
//                }
//
//                global_interruptThread.interrupt();
//
//                Log.d("UploadFileToServer", "response from server: "+serverResponseMessage.toString());
//
//            } catch(Exception e) {
//                Log.d("UploadFileToServer", "uploadFileToServer failed - cause is: "+e.toString());
//                global_interruptThread.interrupt();
//            }
//
//            global_connection.disconnect();
//            return serverResponseMessage.toString();
//        }
//    }

    /**
     * Method to show alert dialog
     */
    private void showAlert(String message) {
        loadingConfirmationActivity = true;

        final Context that = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle(R.string.media_upload)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(hasLite) {
                            Intent intent = new Intent(that, ConfirmCodeActivity.class);
                            intent.putExtra("confirm_code", aaCode);
                            startActivity(intent);
                            finish();
                        } else {
                            if(isAnonymousByToggle){
                                Intent i = new Intent(that, SendReport3Activity.class);
                                i.putExtra("confirmation_code", aaCode);
                                i.putExtra("aa_id", aaId);
                                startActivity(i);
                            } else {
                                Intent i = new Intent(that, SendReport2Activity.class);
                                i.putExtra("confirmation_code", aaCode);
                                i.putExtra("aa_id", aaId);
                                startActivity(i);
                            }
                        }
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showErrorPrompt() {
        loadingConfirmationActivity = true;

        final Context that = this;

        String errorMessage = "Your image/video upload was cancelled.";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(errorMessage).setTitle(R.string.media_upload)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(hasLite) {
                            Intent intent = new Intent(that, ConfirmCodeActivity.class);
                            intent.putExtra("confirm_code", aaCode);
                            startActivity(intent);
                            finish();
                        } else {
                            if(isAnonymousByToggle){
                                Intent i = new Intent(that, SendReport3Activity.class);
                                i.putExtra("confirmation_code", aaCode);
                                i.putExtra("aa_id", aaId);
                                startActivity(i);
                            } else {
                                Intent i = new Intent(that, SendReport2Activity.class);
                                i.putExtra("confirmation_code", aaCode);
                                i.putExtra("aa_id", aaId);
                                startActivity(i);
                            }
                        }
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

//    private void showConfirmationCode(){
//        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
//                UploadActivity.this);
//        alertDialog.setTitle("Report Delivered!");
//        alertDialog.setMessage("Confirmation Code: " + aaCode);
//        // alertDialog.setIcon(R.drawable.delete);
//
//        // Setting Positive "Yes" Button
//        alertDialog.setPositiveButton("Ok",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        showConfirmationScreen();
//                    }
//                });
//
//        // Showing Alert Message
//        alertDialog.show();
//    }

    private void showConfirmationScreen() {
        Intent intent = new Intent(this, ConfirmCodeActivity.class);
        intent.putExtra("confirm_code", aaCode);
        startActivity(intent);
        finish();
    }

    public class InterruptThread extends Thread implements Runnable {
        public void run() {
            LogUtils.debug("CompressedFile", "interrupt thread was initiated");
            LogUtils.debug("UploadFileToServer", "interrupt thread was initiated");
            try {
                Thread thread = Thread.currentThread();
                LogUtils.debug("CompressedFile", "thread name in method" + thread.getName());

                Thread.sleep(uploadTimeoutPeriod);
                global_httpClient.getConnectionManager().shutdown();
                global_httpPost.abort();
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1)
                    EntityUtils.consume(global_r_entity);//httpResponse.getEntity().consumeContent();
                else
                    global_r_entity.consumeContent();

//                global_connection.disconnect();
                LogUtils.debug("CompressedFile", "Timer thread closed connection held by parent, exiting");
                LogUtils.debug("UploadFileToServer", "Timer thread closed connection held by parent, exiting");
            } catch (Exception e) {
                LogUtils.debug("CompressedFile", "Timer thread encountered an exception and could not close the app's connection to the server: " + e);
                LogUtils.debug("UploadFileToServer", "Timer thread encountered an exception and could not close the app's connection to the server: " + e);
            }
        }
    }

    public void releaseHTTPObjects() {
        try {
            global_httpClient.getConnectionManager().shutdown();
            global_httpPost.abort();

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1)
                EntityUtils.consume(global_r_entity);
            else
                global_r_entity.consumeContent();

//            if(global_connection != null)
//                global_connection.disconnect();
        } catch(Exception ignore) {}
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(loadingConfirmationActivity == null) {
            LogUtils.debug("UploadFileToServer","onPause -> interrupthread was interrupted, http objects were released, and showerrorprompt() was called");
            wasRequestCancelled = true;
            global_interruptThread.interrupt();
            releaseHTTPObjects();
            showErrorPrompt();
        }
    }

    @Override
    public void onBackPressed() {
        if(onBackPressedEnabled) {
            loadingConfirmationActivity = false;
            wasRequestCancelled = true;
            global_interruptThread.interrupt();
            releaseHTTPObjects();

            super.onBackPressed();
        }
    }
}