package com.example.socialbike;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class AddNewEventActivity extends AppCompatActivity {

    private EditText city, country, time, date, info;
    private Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_event);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        city = findViewById(R.id.city);
        country = findViewById(R.id.country);
        time = findViewById(R.id.time);
        date = findViewById(R.id.date);
        info = findViewById(R.id.content);

        setButtonListener();
    }

    private void setButtonListener() {
        b = findViewById(R.id.submit);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b.setEnabled(false);
                postEvent();
            }
        });
    }

    private void postEvent() {

        Map<String, Object> data = new HashMap<>();
        data.put("city", city.getText().toString());
        data.put("country", country.getText().toString());
        data.put("time", time.getText().toString());
        data.put("date", date.getText().toString());
        data.put("content", info.getText().toString());

        MainActivity.mFunctions
                .getHttpsCallable("AddNewEvent")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("add new event -> response:" + response);

                    MainActivity.toast(getApplicationContext(), "Your event is live.", 1);
                    finish();

                    if (response.equals("NOT_OK")) {
                        b.setEnabled(true);
                    }
                    return "";
                });

    }
}