package com.example.socialbike;

import static com.example.socialbike.Constants.ADDRESS_FROM_MAPS_CODE;
import static com.example.socialbike.ImageManager.SELECT_PICTURE_CODE;
import static com.example.socialbike.MainActivity.geoApiContext;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.StorageReference;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNewEventActivity extends AppCompatActivity {

    private EditText details, title, mapButton;
    private TextView time, date, time2, date2;
    private Button submitButton;
    private Position position;
    private String groupId;
    private CheckBox end_time_checkbox;
    ImageView headerPicture;
    ImageManager imageManager;
    Bitmap compressImage;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_event);

        Toolbar toolbar = findViewById(R.id.flexible_example_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        imageManager = new ImageManager(this);
        groupId = getIntent().getStringExtra("groupId");

        time = findViewById(R.id.time);
        date = findViewById(R.id.date);
        date2 = findViewById(R.id.date2);
        time2 = findViewById(R.id.time2);
        end_time_checkbox = findViewById(R.id.end_time_checkbox);

        headerPicture = findViewById(R.id.image_header);
        headerPicture.setOnClickListener(view -> openSheet());

        date.setText(DateUtils.convertDateToDay(DateUtils.getDate()));
        date2.setText(date.getText().toString());
        time.setText(DateUtils.convertTime(DateUtils.getTime()));
        time2.setText(time.getText().toString());

        details = findViewById(R.id.content);
        mapButton = findViewById(R.id.map_button);
        title = findViewById(R.id.title);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.setMessage("Uploading event");

        setButtonListeners();
    }

    private void openSheet() {
        if (compressImage == null){
            imageManager.loadPictureFromGallery(this);
        }
        else {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            bottomSheetDialog.setContentView(R.layout.activity_profile_bottom_sheet);

            Button button2 = bottomSheetDialog.findViewById(R.id.picture_locally);
            Button button3 = bottomSheetDialog.findViewById(R.id.button_remove_picture);

            button2.setOnClickListener(v -> {
                imageManager.loadPictureFromGallery(this);
                bottomSheetDialog.dismiss();
            });

            button3.setOnClickListener(v -> {
                compressImage = null;
                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == ADDRESS_FROM_MAPS_CODE) {
            if (resultCode == RESULT_OK) {
                double lat = data.getDoubleExtra("lat", -1);
                double lng = data.getDoubleExtra("lng", -1);
                position = new Position();
                position.setLatLng(new LatLng(lat, lng));
                getAddressAndSetBox();
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                System.out.println(status.getStatusMessage());
            }
            return;
        }

        if (requestCode == SELECT_PICTURE_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                compressImage = imageManager.compressImage(bitmap);
                imageManager.setImage(compressImage, headerPicture);
            } catch (IOException e) {
                e.printStackTrace();
            }

            super.onActivityResult(requestCode, resultCode, data);
        }
    }


/*    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                Bitmap out = imageManager.compressImage(bitmap);
                imageManager.uploadImage(this, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            Bitmap out = imageManager.compressImage(image);
            imageManager.uploadImage(this, out);
        }
    }*/

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
            mapButton.setText(address);
            String city = Utils.getEntity(results[0], com.example.socialbike.Enums.Place.LOCALITY);
            String country = Utils.getEntity(results[0], com.example.socialbike.Enums.Place.COUNTRY);
            position.setCity(city);
            position.setCountry(country);
            position.setAddress(address);
        } else {
            mapButton.setText("");
        }
    }

/*
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

        position = new Position(place.getLatLng(), place.getAddress(), place.getAddress(), country);

        title.setText(position.getLocationName());
        mapButton.setText(position.getAddress());
    }
*/

    private void setButtonListeners() {

        setLocationInputListener();

        mapButton.setOnClickListener(view -> startMapsActivity());
        date.setOnClickListener(view -> openDateAndTimeDialog(true, date, date2));
        time.setOnClickListener(view -> openDateAndTimeDialog(false, time, time2));
        time2.setOnClickListener(view -> openDateAndTimeDialog(false, time2, null));
        date2.setOnClickListener(view -> openDateAndTimeDialog(true, date2, null));

        submitButton = findViewById(R.id.submit);
        submitButton.setOnClickListener(view -> postEvent());

        end_time_checkbox.setOnClickListener(view -> enableOrDisableEndDate());
        enableOrDisableEndDate();
    }

    private void enableOrDisableEndDate() {
        if (end_time_checkbox.isChecked()) {
            date2.setEnabled(true);
            time2.setEnabled(true);
        } else {
            date2.setEnabled(false);
            time2.setEnabled(false);
        }
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
        Maps.openMap(this, position, false);
    }

    private void openDateAndTimeDialog(boolean isDataLayout, TextView view, TextView view2) {
        DateAndTimeDialog dateAndTimeDialog = new DateAndTimeDialog(this, R.layout.date_time_layout, isDataLayout, view, view2);
        dateAndTimeDialog.show();
    }

    private void postEvent() {

        if (submitButton.getText().toString().equals("posting..."))
            return;


        String pattern = "EEE, d MMM yyyy h:m a";
        String dateTime1 = date.getText().toString() + " " + time.getText().toString();
        String dateTime2 = date2.getText().toString() + " " + time2.getText().toString();
        long start = DateUtils.convertTimes(dateTime1, pattern);
        long end = DateUtils.convertTimes(dateTime2, pattern);
        if (start > end) {
            MainActivity.toast(this, "Please correct the dates.", true);
            return;
        }

        submitButton.setText("posting...");
        progressDialog.show();

        uploadPost(start, end).continueWith(task -> {

            String response = String.valueOf(task.getResult().getData());
            System.out.println("add new event -> response:" + response);

            if (compressImage == null) {
                onFinishPosting();
            } else {
                uploadHeaderPicture(response);
            }
            return null;
        });

    }

    private void uploadHeaderPicture(String response) {
        System.out.println("Uploading Image...");
        StorageReference ref;
        if (groupId == null) {
            ref = MainActivity.storageRef.
                    child("events").
                    child(response).
                    child("header");
        } else
            ref = MainActivity.storageRef.child("groups").
                    child(groupId).child("events").
                    child(response).
                    child("header");

        imageManager.uploadImage(compressImage, ref)
                .addOnSuccessListener(taskSnapshot -> {
                    progressDialog.dismiss();
                    System.out.println("Picture was uploaded successfully.");
                    onFinishPosting();
                }).addOnFailureListener(e -> {
            // progressDialog.dismiss();
            System.out.println("Failed uploading the picture.");
            progressDialog.dismiss();
        });


        if (response.equals("NOT_OK")) {
            submitButton.setText("Post");
        }
    }

    private void onFinishPosting() {
        submitButton.setText("Success");
        MainActivity.toast(getApplicationContext(), "Your event is live.", true);
        Intent intent = new Intent();
        intent.putExtra("status", "newEvent");
        setResult(RESULT_OK, intent);
        finish();
    }

    private Task<HttpsCallableResult> uploadPost(long start, long end) {
        System.out.println("Uploading Post...");

        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        data.put("lat", position.getLatLng().latitude);
        data.put("lng", position.getLatLng().longitude);
        data.put("date", date.getText().toString());
        data.put("start", start);
        data.put("end", end);
        data.put("time", time.getText().toString());
        data.put("details", details.getText().toString());
        data.put("address", position.getAddress());
        data.put("country", position.getCountry());
        data.put("city", position.getCity());
        data.put("title", title.getText().toString());

        return
                MainActivity.mFunctions
                        .getHttpsCallable("AddNewEvent").call(data);
    }

    public void setDate(String date) {
        this.date.setText(date);
    }

    public void setTime(String time) {
        this.time.setText(time);
    }
}