package com.example.socialbike.groups;

import android.content.Intent;
import android.os.Bundle;

import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.utilities.EMethods;
import com.example.socialbike.utilities.Geo;
import com.example.socialbike.utilities.Position;
import com.example.socialbike.utilities.Utils;
import com.example.socialbike.databinding.ActivityCreateGroupBinding;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.widget.Toolbar;


import com.example.socialbike.R;
import com.google.android.libraries.places.api.model.TypeFilter;

import java.util.HashMap;
import java.util.Map;

public class CreateGroupActivity extends AppCompatActivity {

    private ActivityCreateGroupBinding binding;
    Button submit;
    Position position;
    EditText region;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        Toolbar toolbar = binding.toolbar;
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        submit = findViewById(R.id.submit);
        region = findViewById(R.id.region);
        submit.setOnClickListener(view -> submitForm());
        region.setOnClickListener(view -> Geo.startAutoComplete(this, null, TypeFilter.REGIONS));

        initPosition();

        binding.title.requestFocus();
        MainActivity.utils.showKeyboard();
    }

    private void initPosition() {
        position = MainActivity.preferredLocationService.getPrivatePosition();
        region.setText(position.getAddress());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            position = Geo.getPosition(data);
            region.setText(position.getAddress());
        }
    }

    private void submitForm() {
        EditText title = findViewById(R.id.title);
        EditText description = findViewById(R.id.description);

        if (position == null)
            return;

        if (submit.getText().toString().equals("Creating..."))
            return;

        submit.setText("Creating...");

        Map<String, Object> data = new HashMap<>();
        data.put("title", title.getText().toString());
        data.put("description", description.getText().toString());
        data.put("lat", position.getLatLng().latitude);
        data.put("lng", position.getLatLng().longitude);
        data.put("city", position.getCity());
        data.put("country", position.getCountry());

        MainActivity.utils.PostData(EMethods.CreateGroup , data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);

                    if (response.equals("OK")) {
                        submit.setText("Success");
                        onBackPressed();
                    } else {
                        submit.setText("Create");
                    }
                    return null;
                });
    }

    @Override
    public void onBackPressed(){
        MainActivity.utils.hideKeyboard(this);
        finish();
    }


}