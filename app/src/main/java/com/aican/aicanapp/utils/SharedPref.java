package com.aican.aicanapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

        public static String PR_LIMIT_KEY = "prLimit";
        public static String PR_ACTUAL_LIMIT_KEY = "prActLimit";

    SharedPref() {

        }

        public static void saveData(Context context, String key, String value) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            myEdit.putString(key, value);
            myEdit.commit();
        }

        public static String getSavedData(Context context, String key) {
            SharedPreferences sh = context.getSharedPreferences(key, Context.MODE_PRIVATE);
            return sh.getString(key, "");
        }



    }
