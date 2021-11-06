package com.example.socialbike.utilities;

import android.app.Activity;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import com.example.socialbike.utilities.Position;
import com.example.socialbike.utilities.Utils;
import com.google.android.gms.maps.model.LatLng;


public class PreferredLocation {

    private final Activity activity;
    private Position position;

    public PreferredLocation(Activity activity, Position position){
        this.activity = activity;
        this.position = position;
    }

    public void savePosition() {
        Utils.savePreference(activity, "data", "lat", String.valueOf(position.getLatLng().latitude));
        Utils.savePreference(activity, "data", "lng", String.valueOf(position.getLatLng().longitude));
        Utils.savePreference(activity, "data", "city", position.getCity());
        Utils.savePreference(activity, "data", "country", position.getCountry());
    }

    public void initPreferredLocation() {
        String lat = Utils.getPreference(activity, "data", "lat");
        String lng = Utils.getPreference(activity, "data", "lng");
        String preferredCity = Utils.getPreference(activity, "data", "city");
        String preferredCountry = Utils.getPreference(activity, "data", "country");

        if (lat != null && lng != null && preferredCity != null){
            this.position.setLatLng(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
            this.position.setCity(preferredCity);
            this.position.setCountry(preferredCountry);
        } else {
            String userCountry = Utils.getUserCountry(activity);
            if (userCountry != null) {
                preferredCity = userCountry.toUpperCase();
                this.position = Utils.getLatLngOfString(preferredCity + " country");
                savePosition();
            }
        }
    }

    public void setLocationText(TextView view) {
        view.setText(HtmlCompat.fromHtml
                ("<u><b>"+ this.position.getCity() +"</b></u>", HtmlCompat.FROM_HTML_MODE_LEGACY));

    }
}
