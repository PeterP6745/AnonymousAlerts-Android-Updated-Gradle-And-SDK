package com.messagelogix.anonymousalerts.utils;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.messagelogix.anonymousalerts.utils.Config;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class ApiHelper {
    private static ApiHelper primaryInstance;
    private static Context context;
    private static Integer example = 0;

    private RequestQueue requestQueue;
    private String url;
    private Response.Listener<String> onSuccessListener;
    private Response.ErrorListener onErrorListener;
    private StringRequest customStringRequest;
    private DefaultRetryPolicy customRetryPolicy;

    private Integer timeoutPeriod;

    private String defaultHeader = Config.API_URL + "?api_key=" + Config.API_KEY + "&app_id=" + Config.APP_ID + "&";

    public ApiHelper() {
        timeoutPeriod = 8000;
    }

    public ApiHelper(boolean doNotWaitForResponse) {
        timeoutPeriod = 4000;
    }

    private ApiHelper(Context passedContext) {
        context = passedContext;
        requestQueue = getRequestQueue();
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public void createUrl(HashMap<String, String> params) {

        StringBuilder url = new StringBuilder();
        url.append(defaultHeader);

        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                url.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                url.append("=");
                url.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                url.append("&");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        url.deleteCharAt(url.length()-1);

        this.url = url.toString();

        LogUtils.debug("fromVolley","url is: "+this.url);
    }

    public void setOnSuccessListener(Response.Listener<String>listener) {
        this.onSuccessListener = listener;
    }

    public void setOnErrorListener(Response.ErrorListener listener) {
        this.onErrorListener = listener;
    }

    public void setCustomStringRequest(StringRequest request) {
        this.customStringRequest = request;
    }

    public void prepareRequest(HashMap<String, String> params, Boolean isPost) {
        createUrl(params);

        StringRequest request;
        if(isPost)
            request = new StringRequest(Request.Method.POST, this.url, getOnSuccessListener(), getOnErrorListener());
        else
            request = new StringRequest(Request.Method.GET, this.url, getOnSuccessListener(), getOnErrorListener());

        DefaultRetryPolicy newRP = new DefaultRetryPolicy(timeoutPeriod, 0, 1.0f);
        this.customRetryPolicy = newRP;
        request.setRetryPolicy(this.customRetryPolicy);

        setCustomStringRequest(request);
    }

    public void startRequest(ApiHelper temp) {
        addToRequestQueue(temp.getCustomStringRequest());
    }

    public static synchronized ApiHelper getInstance(Context context) {
        if (primaryInstance == null) {
            primaryInstance = new ApiHelper(context);
        }

        return primaryInstance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public String getUrl() {
        return url;
    }

    public StringRequest getCustomStringRequest() {
        return customStringRequest;
    }

    public Response.Listener<String> getOnSuccessListener() {
        return this.onSuccessListener;
    }

    public Response.ErrorListener getOnErrorListener() {
        return this.onErrorListener;
    }
    
}
