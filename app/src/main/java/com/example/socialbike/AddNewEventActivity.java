package com.example.socialbike;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNewEventActivity extends AppCompatActivity {

    public static final int ADDRESS_FROM_MAPS_CODE = 1050;
    private EditText time, date, details, locationName, locationAddress;
    private Button submitButton;
    private Button dateButton;
    private Button timeButton, mapButton, locationAutoCompleteButton;
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    private String placeId;
    private EventsFragment eventsFragment;
    private LatLng eventLocation = new LatLng(0, 0);
    private LinearLayout locationAddressSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_event);

        Toolbar toolbar = findViewById(R.id.flexible_example_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        time = findViewById(R.id.time);
        date = findViewById(R.id.date);
        details = findViewById(R.id.content);
        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);
        mapButton = findViewById(R.id.mapButton);
        locationName = findViewById(R.id.location);
        locationAddressSection = findViewById(R.id.locationAddressSection);
        locationAutoCompleteButton = findViewById(R.id.locationAutoCompleteButton);
        locationAddress = findViewById(R.id.location_address);
        locationName.setHint("Location name");
        locationAddress.setHint("Optional");
        Bundle data = getIntent().getExtras();

        locationAddressSection.setVisibility(View.VISIBLE);
        setButtonListeners();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == ADDRESS_FROM_MAPS_CODE) {
            if (resultCode == RESULT_OK) {
                double lat = data.getDoubleExtra("lat", -1);
                double lng = data.getDoubleExtra("lng", -1);
                String address = data.getStringExtra("address");
                String name = data.getStringExtra("name");
                locationAddress.setText(address);
                locationName.setText(name);
                locationAddressSection.setVisibility(View.VISIBLE);
                eventLocation = new LatLng(lat, lng);
            }
        } else if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                locationAddressSection.setVisibility(View.VISIBLE);
                Place place = Autocomplete.getPlaceFromIntent(data);
                placeId = place.getId();
                //System.out.println("Place: " + place.getName() + ", " + place.getId());
                locationName.setText(place.getName());
                locationAddress.setText(getPureAddress(place.getAddress()));
                eventLocation = place.getLatLng();
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                System.out.println(status.getStatusMessage());
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getPureAddress(String address) {
        if (address == null)
            return "";
        int index = address.indexOf(",");
        return address.substring(index+1).trim();
    }

    private void setButtonListeners() {

        setLocationInputListener();

        mapButton.setOnClickListener(view -> {
            startMapsActivity();
        });

        dateButton.setOnClickListener(view -> openDateAndTimeDialog(true));

        timeButton.setOnClickListener(view -> openDateAndTimeDialog(false));

        submitButton = findViewById(R.id.submit);
        submitButton.setOnClickListener(view -> {
            submitButton.setEnabled(false);
            postEvent();
        });
    }

    private void setLocationInputListener() {
        locationAutoCompleteButton.setOnClickListener(view -> openLocationWindow());
    }

    private void openLocationWindow() {
        PlacesClient placesClient = Places.createClient(this);
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    private void getCoordinates() {
        /*  looks up a place and returns coordinates
        String locationName = location.getText().toString();
        GeocodingResult[] request = null;
        try {
            request = GeocodingApi.newRequest(geoApiContext).address(locationName).await();
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
        if (request == null) {
            MainActivity.toast(this, "No result.", 0);
            return;
        }
        LatLng coordinates = request[0].geometry.location;
        System.out.println("Coordinates: " + coordinates.toString());
*/
    }

    private void startMapsActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("lng", eventLocation.longitude);
        intent.putExtra("lat", eventLocation.latitude);
        intent.putExtra("name", locationName.getText().toString());
        intent.putExtra("address", locationAddress.getText().toString());
        startActivityForResult(intent, ADDRESS_FROM_MAPS_CODE);
    }

    private void openDateAndTimeDialog(boolean isDataLayout) {
        DateAndTimeDialog dateAndTimeDialog = new DateAndTimeDialog(this, R.layout.date_time_layout, isDataLayout);
        dateAndTimeDialog.show();
    }

    private void postEvent() {

        Map<String, Object> data = new HashMap<>();
        //data.put("city", "city.getText().toString()");
        //data.put("country", "country.getText().toString()");
        data.put("lat", eventLocation.latitude);
        data.put("lng", eventLocation.longitude);
        data.put("date", date.getText().toString());
        data.put("time", time.getText().toString());
        data.put("eventDetails", details.getText().toString());

        MainActivity.mFunctions
                .getHttpsCallable("AddNewEvent")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("add new event -> response:" + response);

                    MainActivity.toast(getApplicationContext(), "Your event is live.", 1);
                    Intent intent = new Intent();
                    intent.putExtra("status", "newEvent");
                    setResult(RESULT_OK, intent);
                    finish();

                    if (response.equals("NOT_OK")) {
                        submitButton.setEnabled(true);
                    }
                    return "";
                });

    }

    public void setDate(String date) {
        this.date.setText(date);
    }

    public void setTime(String time) {
        this.time.setText(time);
    }
}