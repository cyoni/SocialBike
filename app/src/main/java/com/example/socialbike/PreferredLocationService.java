package com.example.socialbike;

import static android.app.Activity.RESULT_OK;
import static com.example.socialbike.activities.MainActivity.geoApiContext;
import static com.example.socialbike.utilities.Constants.ADDRESS_FROM_MAPS_CODE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.socialbike.Enums.Place;
import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.activities.MapsActivity;
import com.example.socialbike.utilities.EMethods;
import com.example.socialbike.utilities.Geo;
import com.example.socialbike.utilities.Position;
import com.example.socialbike.utilities.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

import java.util.HashMap;
import java.util.Map;

public class PreferredLocationService {

    // responsible for private location and preferred location in search and groups

    private Activity activity;
    private final Context context;
    public Position position = new Position();
    Fragment fragment;
    EditText preferredLocationBox;
    View root;

    public PreferredLocationService(Activity activity){
        this.activity = activity;
        this.context = activity;
    }

    public PreferredLocationService(View root, Fragment fragment){
        this.fragment = fragment;
        this.context = fragment.getContext();
        this.root = root;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADDRESS_FROM_MAPS_CODE) {
            if (resultCode == RESULT_OK) {
                position = Geo.getPosition(data);
                getAddressAndSetBox();
            }
        }
    }

 /*   private void getAddressAndSetBox() {
        GeocodingResult[] results = null;
        preferredLocationBox.setText("Loading...");
        try {
            com.google.maps.model.LatLng newLatLng = new com.google.maps.model.LatLng(position.getLatLng().latitude, position.getLatLng().longitude);
            results = GeocodingApi.newRequest(geoApiContext).latlng(newLatLng).await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (results != null && results.length > 0) {
            String address = results[0].formattedAddress;
            position.setCity(Utils.getEntity(results[0], Place.LOCALITY));
            position.setCountry(Utils.getEntity(results[0], Place.COUNTRY));
            position.setAddress(address);

            preferredLocationBox.setText(address);
           // clean_map_address_button.setVisibility(View.VISIBLE);
        } else {
            preferredLocationBox.setText("");
        }
    }*/

    public void saveLocation() {
        MainActivity.utils.savePreference("data", "lat", String.valueOf(position.getLatLng().latitude));
        MainActivity.utils.savePreference( "data", "city", position.getCity());
        MainActivity.utils.savePreference("data", "lng", String.valueOf(position.getLatLng().longitude));
        MainActivity.utils.savePreference("data", "country", position.getCountry());
        MainActivity.utils.savePreference( "data", "address", position.getAddress());
    }

    public void initLocation() {
        String lat = MainActivity.utils.getPreference("data", "lat");
        String lng = MainActivity.utils.getPreference("data", "lng");
        String preferredCity = MainActivity.utils.getPreference("data", "city");
        String preferredCountry = MainActivity.utils.getPreference("data", "country");
        String preferredAddress = MainActivity.utils.getPreference("data", "address");

        if (lat != null && lng != null && preferredCity != null){
            this.position.setLatLng(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
            this.position.setCity(preferredCity);
            this.position.setCountry(preferredCountry);
            this.position.setAddress(preferredAddress);
        } else {
            String userCountry = MainActivity.utils.getUserCountry();
            if (userCountry != null) {
                preferredCity = userCountry.toUpperCase();
                this.position = MainActivity.utils.getLatLngOfString(preferredCity + " country");
                saveLocation();
            }
        }
//        preferredLocationBox.setText(this.position.getAddress());
    }
}
