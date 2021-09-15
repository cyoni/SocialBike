package com.example.socialbike;

import static com.example.socialbike.Constants.ADDRESS_FROM_MAPS_CODE;
import static com.example.socialbike.MainActivity.geoApiContext;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNewEventActivity extends AppCompatActivity {

    private EditText time, date, details, title, mapButton;
    private Button submitButton;
    private Button dateButton;
    private Button timeButton;


    private Position position;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_event);

        Toolbar toolbar = findViewById(R.id.flexible_example_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        groupId = getIntent().getStringExtra("groupId");

        time = findViewById(R.id.time);
        date = findViewById(R.id.date);
        details = findViewById(R.id.content);
        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);
        mapButton = findViewById(R.id.map_button);
        title = findViewById(R.id.title);
        setButtonListeners();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == ADDRESS_FROM_MAPS_CODE) {
            if (resultCode == RESULT_OK) {
                double lat = data.getDoubleExtra("lat", -1);
                double lng = data.getDoubleExtra("lng", -1);
                position = new Position(new LatLng(lat, lng), null, null);
                getAddressAndSetBox();
            }
        } else if (requestCode == Constants.AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                initiatePlace(place);

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                System.out.println(status.getStatusMessage());
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getAddressAndSetBox() {
        GeocodingResult[] results = null;
        mapButton.setText("Loading...");
        try {
            com.google.maps.model.LatLng newLatLng = new com.google.maps.model.LatLng(position.getLatLng().latitude, position.getLatLng().longitude);
            results = GeocodingApi.newRequest(geoApiContext).latlng(newLatLng).await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (results != null) {
            String address = results[0].formattedAddress;
           // position.setAddress(address);
           // locationName.setText(name);
            mapButton.setText(address);
          //  if (address.contains(",")) {
        //        position.setLocationName(address.substring(0, address.indexOf(",")));
       //     } else
        //        position.setLocationName("");
        }
        else{
            mapButton.setText("");
        }
    }

    private void initiatePlace(Place place) {

        String country = null, state = null;
        for (int i = 0; i < place.getAddressComponents().asList().size(); i++) {
            String str = place.getAddressComponents().asList().get(i).getTypes().toString();
            String tmp = place.getAddressComponents().asList().get(i).getName();
            if (str.contains("administrative_area_level_1")) {
                state = tmp;
                System.out.println("STATE=" + tmp);
            } else if (str.contains("country")) {
                country = tmp;
                System.out.println("COUNTRY=" + tmp);
            }
        }

        position = new Position(place.getLatLng(), place.getName(), place.getAddress(), country, state);

        title.setText(position.getLocationName());
        mapButton.setText(position.getAddress());
    }

    private void setButtonListeners() {

        setLocationInputListener();

        mapButton.setOnClickListener(view -> startMapsActivity());
        dateButton.setOnClickListener(view -> openDateAndTimeDialog(true));
        timeButton.setOnClickListener(view -> openDateAndTimeDialog(false));

        submitButton = findViewById(R.id.submit);
        submitButton.setOnClickListener(view -> {
            postEvent();
        });
    }

    private void setLocationInputListener() {

    }

    private void openLocationWindow() {

        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS_COMPONENTS, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        startActivityForResult(intent, Constants.AUTOCOMPLETE_REQUEST_CODE);
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
        Maps.openMap(this, position);
    }

    private void openDateAndTimeDialog(boolean isDataLayout) {
        DateAndTimeDialog dateAndTimeDialog = new DateAndTimeDialog(this, R.layout.date_time_layout, isDataLayout);
        dateAndTimeDialog.show();
    }

    private void postEvent() {

        if (submitButton.getText().toString().equals("posting..."))
            return;

        submitButton.setText("posting...");
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        data.put("lat", position.getLatLng().latitude);
        data.put("lng", position.getLatLng().longitude);
        data.put("date", date.getText().toString());
        data.put("time", time.getText().toString());
        data.put("details", details.getText().toString());
        data.put("address", position.getAddress());
        data.put("title", title.getText().toString());

        MainActivity.mFunctions
                .getHttpsCallable("AddNewEvent")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("add new event -> response:" + response);
                    submitButton.setText("Success");
                    MainActivity.toast(getApplicationContext(), "Your event is live.", true);
                    Intent intent = new Intent();
                    intent.putExtra("status", "newEvent");
                    setResult(RESULT_OK, intent);
                    finish();

                    if (response.equals("NOT_OK")) {
                        submitButton.setText("Post");
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