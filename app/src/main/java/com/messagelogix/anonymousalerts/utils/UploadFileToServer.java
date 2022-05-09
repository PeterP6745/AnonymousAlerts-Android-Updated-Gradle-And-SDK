package com.messagelogix.anonymousalerts.utils;

import android.content.Context;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class UploadFileToServer {

    Context mContext;

    // constructor
    public UploadFileToServer(Context context) {
        this.mContext = context;
    }

    int serverResponseCode;

    /**
     * This function uploads anytype of file to the server using HTTP POST
     *
     * @param urlServer
     * @param pathToOurFile
     * @return Server Response Code
     */
    public int upload(String urlServer, String pathToOurFile) {

        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        try {
            FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile));

            URL url = new URL(urlServer);
            connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs & Outputs
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Enable POST method
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);


            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            //If 200 OK then we gucci!
            serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();
            Log.d("mylog", serverResponseMessage);

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            Log.e("mylog", "MalformedURLException: " + ex.getMessage(), ex);

        } catch (Exception ex) {
            Log.e("mylog", "Exception : " + ex.getMessage(), ex);
        }

        return serverResponseCode;

    }


}
