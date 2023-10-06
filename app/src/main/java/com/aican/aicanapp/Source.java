package com.aican.aicanapp;

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
}

