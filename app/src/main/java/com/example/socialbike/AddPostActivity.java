package com.example.socialbike;

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
    private HomeFragment homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_post);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
      //  toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setTitle("New post");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        submit = findViewById(R.id.submit);
        textBox = findViewById(R.id.txt_content);
        homeFragment = HomeFragment.getInstance();

        submitButtonListener();
        getSavedText();

        setTextBoxListener();
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
            MainActivity.toast(this,"Post has some problems", 1);
        }
    }

    private void submitPost() {

        //progressbar.setVisibility(View.VISIBLE);
        final String message = textBox.getText().toString();
        submit.setText("PUBLISHING...");
        submit.setEnabled(false);
        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        MainActivity.mFunctions
                .getHttpsCallable("AddNewPost")
                .call(data)
                .continueWith(task -> {

                    String postIdFromServer = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + postIdFromServer);

                    if (!postIdFromServer.equals("FAIL") && !postIdFromServer.isEmpty()) {
                        saveText("");
                        passItemHome();
                        showSuccessMsg();
                        finish();

                    } else {
                       // notifyUser_error();
                        submit.setText("PUBLISHING");
                        submit.setEnabled(true);
                    }
                    return "";
                });
    }

    private void showSuccessMsg() {
        MainActivity.toast(this, "Success!", 0);
    }

    private void passItemHome() {
        homeFragment.addPost(new Post("77777", ConnectedUser.getPublicKey(), ConnectedUser.getName(), 123412, textBox.getText().toString(), 0, 0, false));
        homeFragment.updater.update(0);
    }

    private boolean isMessageValid() {
        //String message = content.getText().toString().trim();
        return true;
    }

}