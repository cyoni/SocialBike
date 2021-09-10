package com.example.socialbike;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;


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
import com.google.maps.model.GeocodingResult;

import java.util.Arrays;
import java.util.List;

import static com.example.socialbike.MainActivity.geoApiContext;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LinearLayout search_layout;
    private Position position = null;
    private Button set_button;
    private boolean wasPlacesUsed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        search_layout = findViewById(R.id.search_layout);

        Button search_button = findViewById(R.id.search_button);
      //  set_button = findViewById(R.id.set_button);

        showOnlyLayout(search_layout);

//        search_button.setOnClickListener(view -> search());
     //   set_button.setOnClickListener(view -> set());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    private void search() {
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
                setMarker(new Position(place.getLatLng(), place.getName(), place.getAddress()));
                wasPlacesUsed = true;
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                System.out.println(status.getStatusMessage());
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void set() {
        if (position == null)
            MainActivity.toast(this, "Please choose a place on the map.", true);
        else {
            if (!wasPlacesUsed) {
                set_button.setText("Setting...");
                getAddressByCoordinates(position.getLatLng());
            }
            sendDataToActivity();
            finish();
        }
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
        intent.putExtra("lat", position.getLatLng().latitude);
        intent.putExtra("lng", position.getLatLng().longitude);
        intent.putExtra("name", position.getLocationName());
        intent.putExtra("address", position.getAddress());
        System.out.println("Passing data: " + position.getAddress() + ", " + position.getLocationName() + ", " + position.getLatLng().toString());
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

        double lat = data.getDouble("lat", 0);
        double lng = data.getDouble("lng", 0);
        String name = data.getString("name");
        String address = data.getString("address");
        LatLng latLng = new LatLng(lat, lng);

        if (lat != 0 || lng != 0) {
            setMarker(new Position(latLng, name, address));
        }

        googleMap.setOnMapClickListener(tmpLatLng -> {
            setMarker(new Position(tmpLatLng, name, address));
            wasPlacesUsed = false;
        });
    }

    private void setMarker(Position position) {
        mMap.clear();
        this.position = position;
        mMap.addMarker(new MarkerOptions()
                .position(position.getLatLng()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position.getLatLng(), 15));
    }
}