package com.example.socialbike;

import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    private TextView textBox;
    Button submit;
    private HomeFragment homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_post);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());
        submit = findViewById(R.id.submit);
        textBox = findViewById(R.id.txt_content);
        homeFragment = HomeFragment.getInstance();

        submitButtonListener();

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
        submit.setText("Posting");
        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        MainActivity.mFunctions
                .getHttpsCallable("AddNewPost")
                .call(data)
                .continueWith(task -> {

                    String postIdFromServer = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + postIdFromServer);

                    if (!postIdFromServer.equals("FAIL") && !postIdFromServer.isEmpty()) {

                        passItemToHome();
                        showSuccessMsg();
                        finish();

                    } else {
                       // notifyUser_error();
                    }
                    return "";
                });
    }

    private void showSuccessMsg() {
        MainActivity.toast(this, "Success!", 0);
    }

    private void passItemToHome() {

        homeFragment.addPost(new Post("77777", User.getPublicKey(), User.getName(), 123412, textBox.getText().toString(), 0));
    }

    private boolean isMessageValid() {
        //String message = content.getText().toString().trim();
        return true;
    }

}