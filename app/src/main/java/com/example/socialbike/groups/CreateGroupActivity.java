package com.example.socialbike.groups;

import android.os.Bundle;

import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.utilities.Utils;
import com.example.socialbike.databinding.ActivityCreateGroupBinding;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.widget.Toolbar;


import com.example.socialbike.R;

import java.util.HashMap;
import java.util.Map;

public class CreateGroupActivity extends AppCompatActivity {

    private ActivityCreateGroupBinding binding;
    Button submit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        Toolbar toolbar = binding.toolbar;
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        submit = findViewById(R.id.submit);
        submit.setOnClickListener(view -> submitForm());

        binding.title.requestFocus();
        MainActivity.utils.showKeyboard();
    }

    private void submitForm() {
        EditText title = findViewById(R.id.title);
        EditText description = findViewById(R.id.description);

        if (submit.getText().toString().equals("Creating..."))
            return;

        submit.setText("Creating...");

        Map<String, Object> data = new HashMap<>();
        data.put("title", title.getText().toString());
        data.put("description", description.getText().toString());
        MainActivity.mFunctions
                .getHttpsCallable("CreateGroup")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);

                    if (response.equals("OK")) {
                        submit.setText("Success!");
                        onBackPressed();
                    } else {
                        submit.setText("Create");
                    }
                    return null;
                });
    }

    @Override
    public void onBackPressed(){
        MainActivity.utils.hideKeyboard();
        finish();
    }


}