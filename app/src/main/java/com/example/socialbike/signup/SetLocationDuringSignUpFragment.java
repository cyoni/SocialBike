package com.example.socialbike.signup;

import static android.app.Activity.RESULT_OK;
import static com.example.socialbike.utilities.Constants.ADDRESS_FROM_MAPS_CODE;
import static com.example.socialbike.activities.MainActivity.geoApiContext;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.socialbike.Enums.Place;
import com.example.socialbike.PreferredLocationManager;
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


public class SetLocationDuringSignUpFragment extends Fragment/* implements AdapterView.OnItemSelectedListener*/ {

    private Button continueButton, clean_map_address_button;
    private NavController nav;
    View root;
    private PreferredLocationManager preferredLocationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_set_preferred_location, container, false);
            continueButton = root.findViewById(R.id.done_button);
            clean_map_address_button = root.findViewById(R.id.clean_map_address_button);
            nav = Navigation.findNavController(container);

            preferredLocationManager = new PreferredLocationManager(root, this);
            preferredLocationManager.init();

            //  Toolbar toolbar = root.findViewById(R.id.toolbar);
           // ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            //  toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
         //   toolbar.setNavigationOnClickListener(v -> nav.navigateUp());

            //genderDropdown = root.findViewById(R.id.gender);

            setButtonListeners();
        }
        return root;
    }

/*    private void setGenderSpinner() {
        String[] items = new String[]{"Male", "Female", "Not specified"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderDropdown.setAdapter(adapter);
        genderDropdown.setOnItemSelectedListener(this);
    }*/

    private void setButtonListeners() {
        continueButton.setOnClickListener(view ->
                {
                    if (isFormOk())
                        submitForm();
                }
        );

       /* clean_map_address_button.setOnClickListener(view -> {
            preferred_location.setText("");
            position = new Position(new LatLng(0, 0), null, null);
            clean_map_address_button.setVisibility(View.GONE);
        });*/
    }

    private boolean isFormOk() {
        if (preferredLocationManager.position == null ||
                preferredLocationManager.position.getLatLng() == null) {
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        preferredLocationManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void submitForm() {

       Utils.savePreference(getActivity(), "data", "country", preferredLocationManager.position.getCountry());
       Utils.savePreference(getActivity(), "data", "city", preferredLocationManager.position.getCity());

        continueButton.setText("Please wait...");

        Map<String, Object> data = new HashMap<>();

        data.put("lat", preferredLocationManager.position.getLatLng().latitude);
        data.put("lng", preferredLocationManager.position.getLatLng().longitude);
        data.put("country", preferredLocationManager.position.getCountry());
        data.put("city", preferredLocationManager.position.getCity());

        MainActivity.mFunctions
                .getHttpsCallable("updateProfile")
                .call(data)
                .continueWith(task -> {
                    String answer = task.getResult().getData().toString();
                    System.out.println("Response from Server: " + answer);
                    if (answer.equals("OK")){
                        preferredLocationManager.saveLocation();
                        nav.navigate(R.id.action_setProfileFragment_to_setProfilePictureFragment);
                    }
                    return null;
                });
    }

/*    @Override
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
    }*/


}