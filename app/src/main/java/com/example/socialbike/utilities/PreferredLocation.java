package com.example.socialbike.utilities;

import android.app.Activity;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import com.example.socialbike.utilities.Position;
import com.example.socialbike.utilities.Utils;
import com.google.android.gms.maps.model.LatLng;


public class PreferredLocation {

    private final Activity activity;

    public PreferredLocation(Activity activity) {
        this.activity = activity;
    }

    public void savePosition(Position position) {
        Utils.savePreference(activity, "data", "lat", String.valueOf(position.getLatLng().latitude));
        Utils.savePreference(activity, "data", "lng", String.valueOf(position.getLatLng().longitude));
        Utils.savePreference(activity, "data", "city", position.getCity());
        Utils.savePreference(activity, "data", "country", position.getCountry());
    }

    public void initPreferredLocation(Position position) {
        String lat = Utils.getPreference(activity, "data", "lat");
        String lng = Utils.getPreference(activity, "data", "lng");
        String preferredCity = Utils.getPreference(activity, "data", "city");
        String preferredCountry = Utils.getPreference(activity, "data", "country");

        if (lat != null && lng != null && preferredCity != null){
            position.setLatLng(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
            position.setCity(preferredCity);
            position.setCountry(preferredCountry);
        } else {
            String userCountry = Utils.getUserCountry(activity);
            if (userCountry != null) {
                preferredCity = userCountry.toUpperCase();
                position = Utils.getLatLngOfString(preferredCity + " country");
                savePosition(position);
            }
        }
    }

    public void setLocationText(Position position, TextView view) {
        view.setText(HtmlCompat.fromHtml
                ("<u><b>"+ position.getCity() +"</b></u>", HtmlCompat.FROM_HTML_MODE_LEGACY));

    }
}
