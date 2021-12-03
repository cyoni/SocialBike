package com.example.socialbike.groups.group;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.socialbike.activities.AddPostActivity;
import com.example.socialbike.post.PostManager;
import com.example.socialbike.R;
import com.example.socialbike.utilities.Updater;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PostsOfGroupOrEventActivity extends AppCompatActivity implements Updater.IUpdate {

    private String groupId, eventId;
    private PostManager postManager;
    private Updater.IUpdate updater = this;
    ExtendedFloatingActionButton createButton;
    private SwipeRefreshLayout swipeLayout;

    public PostsOfGroupOrEventActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_posts);

       // Toolbar toolbar = findViewById(R.id.flexible_example_toolbar);
       // toolbar.setNavigationOnClickListener(v -> onBackPressed());
        swipeLayout = findViewById(R.id.swipe_refresh);
        createButton = findViewById(R.id.fab);

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        createButton.setOnClickListener(view -> {
            Intent group_intent = new Intent(this, AddPostActivity.class);
            group_intent.putExtra("groupId", groupId);
            startActivity(group_intent);
        });
        setSwipeLayout();

        postManager = new PostManager(this, updater, groupId, null);
        getPosts();
    }

    private void setSwipeLayout() {
        swipeLayout.setOnRefreshListener(this::getPosts);

        // Scheme colors for animation
        swipeLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );
    }

    private void getPosts() {
        postManager.clean();
        postManager.getPosts(null);
    }


    @Override
    public void onFinishedUpdating() {
        postManager.hide();
        swipeLayout.setRefreshing(false);
    }
}
