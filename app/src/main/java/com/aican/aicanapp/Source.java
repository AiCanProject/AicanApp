package com.aican.aicanapp;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

public class Source {

    public static int calibMode = 0;

    public static String userId, userPasscode, userRole, userName, userTrack, deviceID, expiryDate, dateCreated;
    public static Boolean status_export = false;
    public static Boolean status_phMvTable = false;
    public static Boolean status_setExtrapolate = false;
    public static Boolean toggle_is_checked = false;
    public static Boolean calibratingNow = false;
    public static String extrapolateValue;
    public static String extrapolateValueDeviceID;
    public static ArrayList<String> id_fetched, passcode_fetched, role_fetched, name_fetched, expiryDate_fetched, dateCreated_fetched;
    public static String subscription;
    public static String scannerData;
    public static String logUserName;
    public static String loginUserRole;

    public static int offlineStatus = 0;

    public static int auto_log = 0;
    public static String calib_completed_by;

    public static Dialog loadingDialog;

    public static void showLoading(Activity context, boolean cancelable, boolean cancelOnTouchOutside, String message) {
        if (!context.isFinishing()) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                cancelLoading();
            }
            loadingDialog = new Dialog(context);
            loadingDialog.setContentView(R.layout.loading_dialog);

            TextView loadingMessageTextView = loadingDialog.findViewById(R.id.textView);
            loadingMessageTextView.setText(message);

            try {
                loadingDialog.getWindow().setDimAmount(0);
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            } catch (Exception e) {
                Log.d("TAG", "showLoading: " + e.getMessage());
            }
            loadingDialog.setCanceledOnTouchOutside(cancelOnTouchOutside);
            loadingDialog.setCancelable(cancelable);
            loadingDialog.show();
        }
    }

    public static void cancelLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            try {
                loadingDialog.cancel();
                loadingDialog = null;
            } catch (Exception e) {
                Log.d("TAG", "cancelLoading: " + e.getMessage());
            }
        }
    }



}

