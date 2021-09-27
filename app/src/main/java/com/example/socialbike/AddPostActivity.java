package com.example.socialbike;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    private final String ADD_POST_CODE = "add_post";
    private TextView textBox;
    Button submit;
    private String groupId;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_post);

        Toolbar toolbar = findViewById(R.id.toolbar);

        groupId = getIntent().getStringExtra("groupId");
        eventId = getIntent().getStringExtra("eventId");

        setSupportActionBar(toolbar);
      //  toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setTitle("New post");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        submit = findViewById(R.id.submit);
        textBox = findViewById(R.id.txt_content);

        submitButtonListener();
        getSavedText();

        setTextBoxListener();

        Utils.showKeyboard(this);
        textBox.requestFocus();
    }

    private void setTextBoxListener() {
        textBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveText(editable.toString());
            }
        });

    }

    private void saveText(String text) {
        SharedPreferences.Editor editor = getSharedPreferences(ADD_POST_CODE, MODE_PRIVATE).edit();
        editor.putString("publicKey", ConnectedUser.getPublicKey());
        editor.putString("post", text);
        editor.apply();
    }

    private void getSavedText() {
        SharedPreferences prefs = getSharedPreferences(ADD_POST_CODE, MODE_PRIVATE);
        String publicKey = prefs.getString("publicKey", "");//"No name defined" is the default value.
        String post_text = prefs.getString("post", "");//"No name defined" is the default value.

        if (publicKey.equals(ConnectedUser.getPublicKey())){
            textBox.setText(post_text);
        }
    }

    private void submitButtonListener() {
        submit.setOnClickListener(view -> submit_button_click());
    }

    private void submit_button_click() {
        if (isMessageValid()){
            submitPost();
        }
        else{
            MainActivity.toast(this,"Post has some problems", true);
        }
    }

    private void submitPost() {

        final String message = textBox.getText().toString();

        if (submit.getText().toString().equals("PUBLISHING..."))
            return;

        submit.setText("PUBLISHING...");
        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        data.put("groupId", groupId);

        if (eventId != null)
            data.put("eventId", eventId);

        MainActivity.mFunctions
                .getHttpsCallable("AddNewPost")
                .call(data)
                .continueWith(task -> {

                    String postIdFromServer = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + postIdFromServer);

                    if (!postIdFromServer.equals("FAIL") && !postIdFromServer.isEmpty()) {
                        submit.setText("SUCCESS");
                        saveText("");
                        passPostToActivity();
                        onBackPressed();
                    } else {
                       // notifyUser_error();
                        submit.setText("PUBLISH");
                    }
                    return "";
                });
    }


    private void passPostToActivity() {
        Intent intent = new Intent();
        intent.putExtra("publicId", ConnectedUser.getPublicKey());
        intent.putExtra("name", ConnectedUser.getName());
        intent.putExtra("time", DateUtils.getTimeInMiliSecs());
        intent.putExtra("message", textBox.getText().toString());
        setResult(RESULT_OK, intent);
    }

    private boolean isMessageValid() {
        //String message = content.getText().toString().trim();
        return true;
    }

    @Override
    public void onBackPressed(){
        Utils.hideKeyboard(this);
        finish();
    }
}