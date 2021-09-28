package com.example.socialbike;


import static android.content.Context.MODE_PRIVATE;

import static com.example.socialbike.MainActivity.geoApiContext;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.socialbike.Enums.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.GeocodingResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Utils {

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static String getUserCountry(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US);
            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static Position getLatLngOfString(String address) {
        GeocodingResult[] request = new GeocodingResult[0];
        try {
            request = GeocodingApi.newRequest(geoApiContext).address(address).await();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (request.length == 0)
            return null;
        com.google.maps.model.LatLng location = request[0].geometry.location;
        String locationName = request[0].formattedAddress;
        return new Position(new LatLng(location.lat, location.lng), locationName, locationName);
    }


    public static void savePreference(Activity activity, String preferenceFolder, String key, String value) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(preferenceFolder, MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getPreference(Activity activity, String preferenceFolder, String key) {
        SharedPreferences prefs = activity.getSharedPreferences(preferenceFolder, MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    public static Map<String, ?> getAllPreferences(Activity activity, String preferenceFolder) {
        SharedPreferences prefs = activity.getSharedPreferences(preferenceFolder, MODE_PRIVATE);
        return prefs.getAll();
    }

    public static void removePreference(EventActivity activity, String preferenceFolder, String key) {
        SharedPreferences prefs = activity.getSharedPreferences(preferenceFolder, MODE_PRIVATE);
        prefs.edit().remove(key).apply();
    }

    public static String getEntity(GeocodingResult results, Place entity) {
        AddressComponent[] addressComponents = results.addressComponents;
        for (AddressComponent current : addressComponents) {
            for (int j = 0; j < current.types.length; j++) {
                if (current.types[j].name().equals(entity.name())) {
                    return current.longName;
                }
            }
        }
        return null;
    }

    public static void registerLike(Post post, String groupId, String eventId) {

        Map<String, Object> data = new HashMap<>();
        data.put("eventId", eventId);
        data.put("groupId", groupId);
        data.put("postId", post.getPostId());

        if (post instanceof SubComment){
            data.put("commentId", ((SubComment) post).getCommentKey());
            data.put("subCommentId", ((SubComment) post).getSubCommentId());
        } else if (post instanceof Comment) {
            data.put("commentId", ((Comment) post).getCommentKey());
        }

        MainActivity.mFunctions
                .getHttpsCallable("RegisterLike")
                .call(data);



    }



}
