package com.messagelogix.anonymousalerts.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.messagelogix.anonymousalerts.R;
import com.messagelogix.anonymousalerts.utils.Config;
import com.messagelogix.anonymousalerts.utils.LogUtils;
import com.messagelogix.anonymousalerts.utils.Preferences;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CameraActivity extends Activity {

    // LogCat tag
    private static final String LOG_TAG = CameraActivity.class.getSimpleName();

    // Directory name to store captured images and videos
    public static final String IMAGE_DIRECTORY_NAME = "AnonymousFileUpload";

    // Camera activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    private static final int CAMERA_SELECT_IMAGE_REQUEST_CODE = 300;
    private static final int CAMERA_SELECT_VIDEO_REQUEST_CODE = 400;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final boolean IS_ABOVE_KITKAT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    private Uri fileUri; // file url to store image/video

    private String compressedFilePath;

    private static String aaCode;
    private static String aaId;
    private boolean isAnonymousByToggle;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Preferences.init(context);

        aaId = getIntent().getStringExtra("aa_id");
        aaCode = getIntent().getStringExtra("confirmation_code");
        isAnonymousByToggle = getIntent().getExtras().getBoolean("reportIsAnonymousByToggle");

        //Button btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
        //Button btnRecordVideo = (Button) findViewById(R.id.btnRecordVideo);
        Button btnSelectPicture = (Button) findViewById(R.id.selectPictureButton);

//        Button btnSelectVideo = (Button) findViewById(R.id.selectVideoButton);

        Button skipButton = (Button) findViewById(R.id.skipButton);

        final boolean hasLite = Preferences.getBoolean(Config.HAS_LITE);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasLite) {
                    Intent intent = new Intent(context, ConfirmCodeActivity.class);
                    intent.putExtra("confirm_code", aaCode);
                    startActivity(intent);
                    finish();
                } else {
                    if(isAnonymousByToggle){
                        Intent i = new Intent(context, SendReport3Activity.class);
                        i.putExtra("confirmation_code", aaCode);
                        i.putExtra("aa_id", aaId);
                        startActivity(i);
                    } else {
                        Intent i = new Intent(context, SendReport2Activity.class);
                        i.putExtra("confirmation_code", aaCode);
                        i.putExtra("aa_id", aaId);
                        startActivity(i);
                    }
                }
            }
        });

//        Boolean hasVideoFeature = Preferences.getBoolean(Config.HAS_VIDEO_FEATURE);
//        if (!hasVideoFeature) {
//            //btnRecordVideo.setVisibility(View.GONE);
//            btnSelectVideo.setVisibility(View.INVISIBLE);
//        } else {
//            btnSelectVideo.setVisibility(View.VISIBLE);
//        }


        /**
         * Select image button click event
         */
        btnSelectPicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //check for access external storage permission

                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(CameraActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    // Request the permission

                    ActivityCompat.requestPermissions(CameraActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                } else {
                    // Permission has already been granted
                    selectImage();
                }
            }
        });


        /**
         * Select video button click event
         */
//        btnSelectVideo.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // capture picture
//                if (ContextCompat.checkSelfPermission(CameraActivity.this,
//                        Manifest.permission.READ_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED) {
//
//                    // Permission is not granted
//                    // Request the permission
//
//                    ActivityCompat.requestPermissions(CameraActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
//
//                } else {
//                    // Permission has already been granted
//                    selectVideo();
//                }
//            }
//        });

        ///**
        // * Capture image button click event
        // */
        //btnCapturePicture.setOnClickListener(new View.OnClickListener() {
        //
        //    @Override
        //    public void onClick(View v) {
        //        // capture picture
        //        captureImage();
        //    }
        //});
        //
        ///**
        // * Record video button click event
        // */
        //btnRecordVideo.setOnClickListener(new View.OnClickListener() {
        //
        //    @Override
        //    public void onClick(View v) {
        //        // record video
        //        recordVideo();
        //    }
        //});

        // Checking camera availability
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device doesn't have camera
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        boolean hasLite = Preferences.getBoolean(Config.HAS_LITE);
        if (itemId == R.id.action_cancel) {
            if(hasLite){
                Intent intent = new Intent(CameraActivity.this, ConfirmCodeActivity.class);
                intent.putExtra("confirm_code", aaCode);
                //intent.putExtra("aa_id", aaId);
                startActivity(intent);
            } else {
                if(isAnonymousByToggle){
                    Intent intent = new Intent(CameraActivity.this, SendReport3Activity.class);
                    intent.putExtra("confirmation_code", aaCode);
                    intent.putExtra("aa_id", aaId);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(CameraActivity.this, SendReport2Activity.class);
                    intent.putExtra("confirmation_code", aaCode);
                    intent.putExtra("aa_id", aaId);
                    startActivity(intent);
                }
            }

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Checking device has camera hardware or not
     */
    private boolean isDeviceSupportCamera() {
        return getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }

    /**
     * Launching camera app to select image
     */
    private void selectImage() {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, ""), CAMERA_SELECT_IMAGE_REQUEST_CODE);
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, CAMERA_SELECT_IMAGE_REQUEST_CODE);
    }

    /**
     * Launching camera app to select video
     */
    private void selectVideo() {
//        Intent intent = new Intent();
//        intent.setType("video/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, ""), CAMERA_SELECT_VIDEO_REQUEST_CODE);
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, CAMERA_SELECT_VIDEO_REQUEST_CODE);
    }

    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Launching camera app to record video
     */
    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
        // name

        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        switch (requestCode) {
//            case CAMERA_CAPTURE_IMAGE_REQUEST_CODE:
//                LogUtils.debug("CameraProcess","capture_image request code");
//                if (resultCode == RESULT_OK) {
//
//                    // successfully captured the image
//                    // launching upload activity
//                    launchUploadActivity(true);
//                } else if (resultCode == RESULT_CANCELED) {
//                    // user cancelled Image capture
//                    Toast.makeText(getApplicationContext(),
//                            "User cancelled image capture", Toast.LENGTH_SHORT)
//                            .show();
//                } else {
//                    // failed to capture image
//                    Toast.makeText(getApplicationContext(),
//                            "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
//                            .show();
//                }
//                break;
            case CAMERA_SELECT_IMAGE_REQUEST_CODE:
                LogUtils.debug("CompressedFile","select_image request code");
                if (resultCode == RESULT_OK) {
                    LogUtils.debug("CompressedFile","result code is RESULT_OK");
                    LogUtils.debug("CompressedFile","android version is above kitkat --> "+String.valueOf(IS_ABOVE_KITKAT));

                    if(!IS_ABOVE_KITKAT) {
                        fileUri = Uri.parse(getAbsolutePath(getApplicationContext(), data.getData()));
                        try {
                            compressedFilePath = (getAbsolutePath(getApplicationContext(), data.getData()).substring(0,getAbsolutePath(getApplicationContext(), data.getData()).length()-4)+"-compressed.jpg");
                            Log.d("CompressedFile","compressed file name - "+compressedFilePath);

                            File compressedFile = new File(compressedFilePath);
                            String compressedFileSize = Formatter.formatFileSize(getApplicationContext(),compressedFile.length());
                            Log.d("CompressedFile","file size prior to compression" + compressedFile.length());

                            manageCompressedFile(data);
                        }catch (Exception e){}
                    } else {
                        LogUtils.debug("CompressedFile","what is the data object: "+data.getData());
                        LogUtils.debug("CompressedFile","what is the returned path w/context: "+getPath(context, data.getData()));
                        LogUtils.debug("CompressedFile","what is the returned path w/applicationcontext: "+getPath(getApplicationContext(), data.getData()));

                        fileUri = Uri.parse(getPath(getApplicationContext(), data.getData()));
//                        File sourceFile = new File(fileUri.getPath());
//                        String file_size = Formatter.formatShortFileSize(getApplicationContext(),sourceFile.length());
//                        Log.d("fileSize", file_size);
                        try {
                            compressedFilePath = (getPath(getApplicationContext(), data.getData()).substring(0,getPath(getApplicationContext(), data.getData()).length()-4)+"-compressed.jpg");
                            Log.d("CompressedFile","compressed file name - "+compressedFilePath);

                            File compressedFile = new File(compressedFilePath);
                            String compressedFileSize = Formatter.formatShortFileSize(getApplicationContext(),compressedFile.length());
                            String compFileSize = String.valueOf(compressedFile.length());
                            Log.d("CompressedFile","file size prior to compression - comp.length(): " + compressedFile.length());
                            Log.d("CompressedFile","file size prior to compression - comp.formatfilesize(): " + compressedFileSize);

                            manageCompressedFile(data);
                        }catch (Exception e){}
                    }

//                    if (!IS_ABOVE_KITKAT) {
//                        fileUri = Uri.parse(getAbsolutePath(getApplicationContext(), data.getData()));
//                    } else {
//                        LogUtils.debug("CameraProcess","what is the data object: "+data.getData());
//                        LogUtils.debug("CameraProcess","what is the returned path w/context: "+getPath(context, data.getData()));
//                        LogUtils.debug("CameraProcess","what is the returned path w/applicationcontext: "+getPath(getApplicationContext(), data.getData()));
//                        fileUri = Uri.parse(getPath(getApplicationContext(), data.getData()));
//                    }

                    launchUploadActivity(true);
                } else if (resultCode == RESULT_CANCELED) {
                    // user cancelled Image capture
                    Toast.makeText(getApplicationContext(), "User cancelled image capture", Toast.LENGTH_SHORT).show();
                } else {
                    // failed to capture image
                    Toast.makeText(getApplicationContext(), "Sorry! Failed to capture image", Toast.LENGTH_SHORT).show();
                }
                break;
//            case CAMERA_CAPTURE_VIDEO_REQUEST_CODE:
//                LogUtils.debug("CameraProcess","capture_video request code");
//                if (resultCode == RESULT_OK) {
//                    // video successfully recorded
//                    // launching upload activity
//
//                    launchUploadActivity(false);
//                } else if (resultCode == RESULT_CANCELED) {
//
//                    // user cancelled recording
//                    Toast.makeText(getApplicationContext(),
//                            "User cancelled video recording", Toast.LENGTH_SHORT)
//                            .show();
//                } else {
//                    // failed to record video
//                    Toast.makeText(getApplicationContext(),
//                            "Sorry! Failed to record video", Toast.LENGTH_SHORT)
//                            .show();
//                }
//                break;
//            case CAMERA_SELECT_VIDEO_REQUEST_CODE:
//                LogUtils.debug("VideoProcess","select_video request code");
//                if (resultCode == RESULT_OK) {
//                    if (!IS_ABOVE_KITKAT) {
//                        LogUtils.debug("VideoProcess","NOT ABOVE KITKAT ANDROID VERSION");
//                        fileUri = Uri.parse(getAbsolutePath(getApplicationContext(), data.getData()));
//                        compressedFilePath = (getAbsolutePath(getApplicationContext(), data.getData()).substring(0,getAbsolutePath(getApplicationContext(), data.getData()).length()-4)+".zip");
//                        String [] zipPath  = {fileUri.getPath()};
//
//                        LogUtils.debug("VideoProcess","name of video file is: "+compressedFilePath);
//                        File tempFile = new File(compressedFilePath);
//                        LogUtils.debug("VideoProcess","video file prior to zip: "+tempFile.length());
//
//                        convertToZipFile(zipPath,compressedFilePath);
//                    } else {
//                        LogUtils.debug("VideoProcess","ABOVE KITKAT ANDROID VERSION");
//                        fileUri = Uri.parse(getPath(getApplicationContext(), data.getData()));
//                        compressedFilePath = (getPath(getApplicationContext(), data.getData()).substring(0,getPath(getApplicationContext(), data.getData()).length()-4)+".zip");
//                        String [] zipPath  = {fileUri.getPath()};
//
//                        LogUtils.debug("VideoProcess","name of video file is: "+compressedFilePath);
//                        File tempFile = new File(compressedFilePath);
//                        LogUtils.debug("VideoProcess","video file prior to zip: "+tempFile.length());
//
//                        convertToZipFile(zipPath,compressedFilePath);
////                        fileUri = Uri.parse(getPath(getApplicationContext(), data.getData()));
//                    }
//
////                    if (!IS_ABOVE_KITKAT) {
////                        fileUri = Uri.parse(getAbsolutePath(getApplicationContext(), data.getData()));
////                    } else {
////                        fileUri = Uri.parse(getPath(getApplicationContext(), data.getData()));
////                    }
//
//                    launchUploadActivity(false);
//                } else if(resultCode == RESULT_CANCELED) {
//                    // user cancelled recording
//                    Toast.makeText(getApplicationContext(),
//                            "User cancelled video selection", Toast.LENGTH_SHORT)
//                            .show();
//
//                } else {
//                    // failed to record video
//                    Toast.makeText(getApplicationContext(),
//                            "Sorry! Failed to select video", Toast.LENGTH_SHORT)
//                            .show();
//                }
//                break;
        }
    }

    private void launchUploadActivity(boolean isImage) {
        Intent i = new Intent(this, UploadActivity.class);
        i.putExtra("filePath", fileUri.getPath());
        i.putExtra("compressedFilePath",compressedFilePath);
        i.putExtra("confirmation_code", aaCode);
        i.putExtra("aa_id", aaId);
        i.putExtra("isImage", isImage);
        i.putExtra("reportIsAnonymousByToggle", isAnonymousByToggle);
        startActivity(i);
    }

    private String getAbsolutePath(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.MediaColumns.DATA};
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch(Exception e) {
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {
        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(LOG_TAG, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + aaId + "_" + aaCode + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + aaId + "_" + aaCode + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */
    @TargetApi(19)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public void manageCompressedFile(Intent data) {
        try {
            //ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputStream out = new FileOutputStream(compressedFilePath);
            Bitmap imageMap = BitmapFactory.decodeFile(getPath(getApplicationContext(), data.getData()));
            imageMap.compress(Bitmap.CompressFormat.JPEG,50,out);
            out.close();
            File compressedFile = new File(compressedFilePath);
            String compressedFileSize = Formatter.formatShortFileSize(getApplicationContext(),compressedFile.length());
            Log.d("CompressedFile","file size post compression: " + compressedFileSize);
        } catch(Exception e) {LogUtils.debug("CompressedFile","manageCompressedFile() - encountered exception: "+e);}
    }

    public void convertToZipFile(String[] _files, String zipFileName) {
        try {
            int BUFFER = 6 * 1024;
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.length; i++) {
                Log.v("Compress", "Adding: " + _files[i]);
                FileInputStream fi = new FileInputStream(_files[i]);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();

            File tempFile = new File(compressedFilePath);
            LogUtils.debug("VideoProcess","video file after zip: "+tempFile.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}