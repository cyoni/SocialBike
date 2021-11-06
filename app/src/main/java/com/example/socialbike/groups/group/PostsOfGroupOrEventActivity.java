package com.example.socialbike.groups.group;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socialbike.post.PostManager;
import com.example.socialbike.R;
import com.example.socialbike.utilities.Updater;

public class PostsOfGroupOrEventActivity extends AppCompatActivity implements Updater.IUpdate {

    private String groupId, eventId;
    private PostManager postManager;
    private Updater.IUpdate updater = this;

    public PostsOfGroupOrEventActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home);

       // Toolbar toolbar = findViewById(R.id.flexible_example_toolbar);
       // toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        postManager = new PostManager(this, updater, groupId, null);
        getPosts();
    }

    private void getPosts() {
        postManager.getPosts(null);
    }


    @Override
    public void onFinishedUpdating() {
        postManager.hide();
    }
}
