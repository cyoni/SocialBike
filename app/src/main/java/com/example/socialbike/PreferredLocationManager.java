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
import com.example.socialbike.utilities.Position;
import com.example.socialbike.utilities.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

import java.util.HashMap;
import java.util.Map;

public class PreferredLocationManager {

    private Activity activity;
    private final Context context;
    public Position position = new Position();
    Fragment fragment;
    EditText preferredLocationBox;
    View root;

    public PreferredLocationManager(Activity activity){
        this.activity = activity;
        this.context = activity;
    }

    public PreferredLocationManager(View root, Fragment fragment){
        this.fragment = fragment;
        this.context = fragment.getContext();
        this.root = root;
    }

    public void init() {
        if (activity != null) {
            preferredLocationBox = activity.findViewById(R.id.preferredLocation);
        } else {
            preferredLocationBox = root.findViewById(R.id.preferredLocation);
        }

        preferredLocationBox.setOnClickListener(view -> openMap());

      /*  clean_map_address_button.setOnClickListener(view -> {
            preferredLocationBox.setText("");
            position = new Position(new LatLng(0, 0), null, null);
            clean_map_address_button.setVisibility(View.GONE);
        });*/
    }

    public void openMap() {
        Intent intent = new Intent(context, MapsActivity.class);
        if (position == null || position.getLatLng() == null) {
            position = new Position(new LatLng(0, 0), null, null);
        }
        intent.putExtra("lng", position.getLatLng().longitude);
        intent.putExtra("lat", position.getLatLng().latitude);

        if (activity != null)
            activity.startActivityForResult(intent, ADDRESS_FROM_MAPS_CODE);
        else if (fragment != null)
            fragment.startActivityForResult(intent, ADDRESS_FROM_MAPS_CODE);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADDRESS_FROM_MAPS_CODE) {
            if (resultCode == RESULT_OK) {
                double lat = data.getDoubleExtra("lat", -1);
                double lng = data.getDoubleExtra("lng", -1);
                position = new Position(new LatLng(lat, lng), null, null);
                getAddressAndSetBox();
            }
        }
    }


    private void getAddressAndSetBox() {
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
    }

    public void saveLocation() {
        Activity tmp = (activity != null) ? activity : fragment.getActivity();
        Utils.savePreference(tmp, "data", "lat", String.valueOf(position.getLatLng().latitude));
        Utils.savePreference(tmp, "data", "lng", String.valueOf(position.getLatLng().longitude));
        Utils.savePreference(tmp, "data", "city", position.getCity());
        Utils.savePreference(tmp, "data", "country", position.getCountry());
        Utils.savePreference(tmp, "data", "address", position.getAddress());
    }

    public void initLocation() {
        String lat = Utils.getPreference(activity, "data", "lat");
        String lng = Utils.getPreference(activity, "data", "lng");
        String preferredCity = Utils.getPreference(activity, "data", "city");
        String preferredCountry = Utils.getPreference(activity, "data", "country");
        String preferredAddress = Utils.getPreference(activity, "data", "address");

        if (lat != null && lng != null && preferredCity != null){
            this.position.setLatLng(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
            this.position.setCity(preferredCity);
            this.position.setCountry(preferredCountry);
            this.position.setAddress(preferredAddress);
        } else {
            String userCountry = Utils.getUserCountry(activity);
            if (userCountry != null) {
                preferredCity = userCountry.toUpperCase();
                this.position = Utils.getLatLngOfString(preferredCity + " country");
                saveLocation();
            }
        }
        preferredLocationBox.setText(this.position.getAddress());
    }
}
