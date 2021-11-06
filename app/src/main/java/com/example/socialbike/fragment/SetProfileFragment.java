package com.example.socialbike.fragment;

import static android.app.Activity.RESULT_OK;
import static com.example.socialbike.utilities.Constants.ADDRESS_FROM_MAPS_CODE;
import static com.example.socialbike.activities.MainActivity.geoApiContext;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.socialbike.Enums.Place;
import com.example.socialbike.R;
import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.activities.MapsActivity;
import com.example.socialbike.utilities.Position;
import com.example.socialbike.utilities.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

import java.util.HashMap;
import java.util.Map;


public class SetProfileFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private Button doneButton, clean_map_address_button;
    private ImageButton skipButton;

    private EditText age, preferred_location;
    private NavController nav;
    private Position position;
    private Spinner genderDropdown;
    private int intGender = 2;
    private String city, country;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_set_profile, container, false);
        age = root.findViewById(R.id.age);
        doneButton = root.findViewById(R.id.done_button);
        skipButton = root.findViewById(R.id.skip_button);
        preferred_location = root.findViewById(R.id.preferred_location);
        clean_map_address_button = root.findViewById(R.id.clean_map_address_button);
        nav = Navigation.findNavController(container);

        Toolbar toolbar = root.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        //  toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setNavigationOnClickListener(v -> nav.navigateUp());

        genderDropdown = root.findViewById(R.id.gender);

        setGenderSpinner();

        startListening();
        return root;
    }

    private void setGenderSpinner() {
        String[] items = new String[]{"Male", "Female", "Not specified"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderDropdown.setAdapter(adapter);
        genderDropdown.setOnItemSelectedListener(this);
    }

    private void startListening() {
        doneButton.setOnClickListener(view -> submitForm());
        skipButton.setOnClickListener(view -> getActivity().finish());
        preferred_location.setOnClickListener(view -> openMap());
        clean_map_address_button.setOnClickListener(view -> {
            preferred_location.setText("");
            position = new Position(new LatLng(0, 0), null, null);
            clean_map_address_button.setVisibility(View.GONE);
        });
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
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void openMap() {
        Intent intent = new Intent(getContext(), MapsActivity.class);
        if (position == null) {
            position = new Position(new LatLng(0, 0), null, null);
        }
        intent.putExtra("lng", position.getLatLng().longitude);
        intent.putExtra("lat", position.getLatLng().latitude);

        startActivityForResult(intent, ADDRESS_FROM_MAPS_CODE);
    }

    private void getAddressAndSetBox() {
        GeocodingResult[] results = null;
        preferred_location.setText("Loading...");
        try {
            com.google.maps.model.LatLng newLatLng = new com.google.maps.model.LatLng(position.getLatLng().latitude, position.getLatLng().longitude);
            results = GeocodingApi.newRequest(geoApiContext).latlng(newLatLng).await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (results != null && results.length > 0) {
            String address = results[0].formattedAddress;
            city = Utils.getEntity(results[0], Place.LOCALITY);
            country = Utils.getEntity(results[0], Place.COUNTRY);
            preferred_location.setText(address);
            clean_map_address_button.setVisibility(View.VISIBLE);
        } else {
            preferred_location.setText("");
        }
    }


    private void submitForm() {
        Utils.savePreference(getActivity(), "data", "country", country);
        Utils.savePreference(getActivity(), "data", "city", city);

        Map<String, Object> data = new HashMap<>();
        data.put("gender", intGender);
        if (position.getLatLng() != null) {
            data.put("lat", position.getLatLng().latitude);
            data.put("lng", position.getLatLng().longitude);
        }
        data.put("age", age.getText().toString());
        data.put("country", country);
        data.put("city", city);

        getActivity().finish();

        MainActivity.mFunctions
                .getHttpsCallable("updateProfile")
                .call(data)
                .continueWith(task -> {
                    String answer = task.getResult().getData().toString();
                    System.out.println("Response from Server: " + answer);
                    MainActivity.startChat();
                    return "";
                });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        switch (position) {
            case 0:
                intGender = 0;
                break;
            case 1:
                intGender = 1;
                break;
            case 2:
                intGender = 2;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}