package com.example.socialbike.signup;

import static android.app.Activity.RESULT_OK;
import static com.example.socialbike.utilities.Constants.ADDRESS_FROM_MAPS_CODE;

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

import com.example.socialbike.PreferredLocationService;
import com.example.socialbike.R;
import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.utilities.Geo;
import com.example.socialbike.utilities.Position;
import com.google.android.libraries.places.api.model.TypeFilter;

import java.util.HashMap;
import java.util.Map;


public class SetLocationDuringSignUpFragment extends Fragment/* implements AdapterView.OnItemSelectedListener*/ {

    private Button continueButton, clean_map_address_button;
    private NavController nav;
    View root;
    Position position;
    EditText preferredLocationBox;

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

            preferredLocationBox = root.findViewById(R.id.preferredLocation);
            preferredLocationBox.setOnClickListener(view -> Geo.startAutoComplete(null, this, TypeFilter.CITIES));

            //  Toolbar toolbar = root.findViewById(R.id.toolbar);
           // ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            //  toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
         //   toolbar.setNavigationOnClickListener(v -> nav.navigateUp());

            //genderDropdown = root.findViewById(R.id.gender);

            setButtonListeners();
        }
        return root;
    }


    private void setButtonListeners() {
        continueButton.setOnClickListener(view ->
                {
                    if (isFormOk()) {
                        submitForm();
                    }
                }
        );

       /* clean_map_address_button.setOnClickListener(view -> {
            preferred_location.setText("");
            position = new Position(new LatLng(0, 0), null, null);
            clean_map_address_button.setVisibility(View.GONE);
        });*/
    }

    private void savePosition() {
        MainActivity.preferredLocationService.savePrivateLocation(position);
    }

    private boolean isFormOk() {
        if (position == null || position.getLatLng() == null) {
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADDRESS_FROM_MAPS_CODE) {
            if (resultCode == RESULT_OK) {
                position = Geo.getPosition(data);
                preferredLocationBox.setText(position.getAddress());
            }
        }
    }

    private void submitForm() {


        continueButton.setText("Please wait...");

        Map<String, Object> data = new HashMap<>();

        data.put("lat", position.getLatLng().latitude);
        data.put("lng", position.getLatLng().longitude);
        data.put("country",position.getCountry());
        data.put("city", position.getCity());

        MainActivity.mFunctions
                .getHttpsCallable("updateProfile")
                .call(data)
                .continueWith(task -> {
                    String answer = task.getResult().getData().toString();
                    System.out.println("Response from Server: " + answer);
                    if (answer.equals("OK")){
                        savePosition();
                        nav.navigate(R.id.action_setProfileFragment_to_setProfilePictureFragment);
                    }
                    return null;
                });
    }
}