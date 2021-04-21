package com.example.socialbike;

import android.app.Activity;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class MyPreferences {

    public static final String USER_FOLDER = "user";
    public static String DOES_NOT_EXIST_CODE = "-";

    public static void setSharedPreference(Activity activity, String folder, String key, String value){
        SharedPreferences.Editor editor = activity.getSharedPreferences(folder, MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }


    public static String getUserPublicKey(Activity activity){
        return getSharedPreference(activity, MyPreferences.USER_FOLDER, "user_public_key");
    }


    public static String getSharedPreference(Activity activity, String folder, String key){
        SharedPreferences prefs = activity.getSharedPreferences(folder, MODE_PRIVATE);
        return prefs.getString(key, DOES_NOT_EXIST_CODE);
    }

    public static String getNicknameFromDevice(Activity activity) {
        return getSharedPreference(activity, MyPreferences.USER_FOLDER, "nickname");
    }

}
