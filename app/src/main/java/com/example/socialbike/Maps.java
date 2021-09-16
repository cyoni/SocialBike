package com.example.socialbike;

import static com.example.socialbike.Constants.ADDRESS_FROM_MAPS_CODE;

import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.socialbike.databinding.ActivityMaps2Binding;

import org.jetbrains.annotations.NotNull;

public class Maps extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMaps2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMaps2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(@NotNull GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent = getIntent();
        double lat = intent.getExtras().getDouble("lat");
        double lng = intent.getExtras().getDouble("lng");

        if (lat != 0 || lng != 0) {
            LatLng location = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(location));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
    }

    public static void openMap(Activity activity, Position position) {
        Intent intent = new Intent(activity, MapsActivity.class);
        if (position != null) {
            intent.putExtra("lng", position.getLatLng().longitude);
            intent.putExtra("lat", position.getLatLng().latitude);
        }
        intent.putExtra("name", "");
        activity.startActivityForResult(intent, ADDRESS_FROM_MAPS_CODE);
    }
}