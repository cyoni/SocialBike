package com.example.socialbike.utilities;

import android.app.Activity;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.utilities.Position;
import com.example.socialbike.utilities.Utils;
import com.google.android.gms.maps.model.LatLng;


public class PreferredLocation {

    private final Activity activity;

    public PreferredLocation(Activity activity) {
        this.activity = activity;
    }

    public void savePosition(Position position) {
        MainActivity.utils.savePreference("data", "lat", String.valueOf(position.getLatLng().latitude));
        MainActivity.utils.savePreference("data", "lng", String.valueOf(position.getLatLng().longitude));
        MainActivity.utils.savePreference("data", "city", position.getCity());
        MainActivity.utils.savePreference("data", "country", position.getCountry());
    }

    public void initPreferredLocation(Position position) {
        String lat = MainActivity.utils.getPreference("data", "lat");
        String lng = MainActivity.utils.getPreference("data", "lng");
        String preferredCity = MainActivity.utils.getPreference("data", "city");
        String preferredCountry = MainActivity.utils.getPreference("data", "country");

        if (lat != null && lng != null && preferredCity != null){
            position.setLatLng(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
            position.setCity(preferredCity);
            position.setCountry(preferredCountry);
        } else {
            String userCountry = MainActivity.utils.getUserCountry();
            if (userCountry != null) {
                preferredCity = userCountry.toUpperCase();
                position = MainActivity.utils.getLatLngOfString(preferredCity + " country");
                savePosition(position);
            }
        }
    }

    public void setLocationText(Position position, TextView view) {
        view.setText(HtmlCompat.fromHtml
                ("<u><b>"+ position.getCity() +"</b></u>", HtmlCompat.FROM_HTML_MODE_LEGACY));

    }
}
