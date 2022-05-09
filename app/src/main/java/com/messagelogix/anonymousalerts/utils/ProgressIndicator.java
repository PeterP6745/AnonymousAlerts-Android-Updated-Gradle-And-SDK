package com.messagelogix.anonymousalerts.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.messagelogix.anonymousalerts.R;

public class ProgressIndicator extends Dialog {

    private final Context context;
    private Dialog progressDialog;
    //private String progressMessage;

    public ProgressIndicator(@NonNull Fragment context) {
        super(context.getActivity());

        this.context = context.getActivity();
        setContext();
    }

    public ProgressIndicator(@NonNull Activity context) {
        super(context);

        this.context = context;
        setContext();
    }

    private void setContext() {
        progressDialog = new Dialog(this.context);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    public void showDialog(String progressMessage){
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.custom_progress_indicator);

        TextView text = progressDialog.findViewById(R.id.text_view);
        text.setText(progressMessage);

        progressDialog.show();

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(progressDialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        progressDialog.getWindow().setAttributes(layoutParams);
    }

    public void setDialogMessage(String message) {
        TextView text = progressDialog.findViewById(R.id.text_view);
        text.setText(message);
    }

    public void show() {progressDialog.show();}

    public void dismiss() {
        LogUtils.debug("AsyncHandler","progressindicator - dismissing activityprogressindicator");
        progressDialog.dismiss();
    }

    public boolean isShowing() {
        return progressDialog.isShowing();
    }
}
