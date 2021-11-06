package com.example.socialbike.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.example.socialbike.utilities.Position;
import com.example.socialbike.R;
import com.example.socialbike.utilities.Utils;
import com.example.socialbike.databinding.ActivityMapsBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.example.socialbike.activities.MainActivity.geoApiContext;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LinearLayout search_layout;
    private Position position = null;
    private EditText search_bar;
    private Button set_button;
    Toolbar toolbar;
    private ImageView pin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        search_layout = findViewById(R.id.search_layout);
        search_bar = findViewById(R.id.search_bar);
        search_bar.setOnClickListener(view -> openSearchBar());


        Button search_button = findViewById(R.id.search_button);
        set_button = findViewById(R.id.set_button);

        pin = findViewById(R.id.pin);

        showOnlyLayout(search_layout);
        set_button.setOnClickListener(view -> set());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private void openSearchBar() {
        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(this);
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        startActivityForResult(intent, 1);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                //setMarker(new Position(place.getLatLng(), place.getName(), place.getAddress()));
                if (place.getLatLng() != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
                    position = new Position(place.getLatLng(), null, null);
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                System.out.println(status.getStatusMessage());
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void set() {
        sendDataToActivity();
        finish();
    }

    private void getAddressByCoordinates(LatLng latLng) {

        GeocodingResult[] results = null;
        try {
            com.google.maps.model.LatLng newLatLng = new com.google.maps.model.LatLng(latLng.latitude, latLng.longitude);
            results = GeocodingApi.newRequest(geoApiContext).latlng(newLatLng).await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (results == null) {
            // MainActivity.toast(this, "No result.", 0);
        } else {
            String address = results[0].formattedAddress;
            position.setAddress(address);
            if (address.contains(",")) {
                position.setLocationName(address.substring(0, address.indexOf(",")));
            } else
                position.setLocationName("");
        }
    }

    private void sendDataToActivity() {
        Intent intent = new Intent();
        intent.putExtra("lat", mMap.getCameraPosition().target.latitude);
        intent.putExtra("lng", mMap.getCameraPosition().target.longitude);
        setResult(RESULT_OK, intent);
    }

    private void showOnlyLayout(LinearLayout layout) {
//        search_layout.setVisibility(View.GONE);
        //  layout.setVisibility(View.VISIBLE);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Bundle data = getIntent().getExtras();

        double lat = data.getDouble("lat", 32.078436);
        double lng = data.getDouble("lng", 34.802066);
        boolean isForDisplayOnly = data.getBoolean("isForDisplayOnly", false);
        LatLng latLng;

        if (isForDisplayOnly){
            position = new Position(lat, lng);
        }

       // setMarker(new Position(latLng, null, null));

      //  if (position == null){
            String country = Utils.getUserCountry(this);
            if (country != null){
                latLng = getLatLngOfString(country + " country");
                if (latLng != null)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 1));
            }
     //   }

        if (isForDisplayOnly){
            toolbar.setVisibility(View.GONE);
            search_bar.setVisibility(View.GONE);
            pin.setVisibility(View.GONE);
            ImageButton return_button = findViewById(R.id.return_button);
            return_button.setVisibility(View.VISIBLE);
            return_button.setOnClickListener(view -> finish());
            if (!(position == null || position.getLatLng() == null)) {
                mMap.addMarker(new MarkerOptions()
                        .position(position.getLatLng()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position.getLatLng(), 15));
            }
        }
    }

    public LatLng getLatLngOfString(String address){
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
        return new LatLng(location.lat, location.lng);
    }

    private void setMarker(Position position) {
     //   mMap.clear();
        this.position = position;
       // mMap.addMarker(new MarkerOptions()
       //         .position(position.getLatLng()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position.getLatLng(), 15));
    }
}