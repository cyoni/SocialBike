package com.example.socialbike.groups.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.socialbike.events.EventsManager;
import com.example.socialbike.activities.MainActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.socialbike.groups.Group;
import com.example.socialbike.groups.GroupManager;
import com.example.socialbike.post.PostManager;
import com.example.socialbike.R;
import com.example.socialbike.utilities.Updater;
import com.example.socialbike.utilities.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class GroupActivity extends AppCompatActivity implements Updater.IUpdate {

    public MainActivity mainActivity;
    private String groupId;
    Button events_button, joinButton;
    Updater.IUpdate update = this;
    private EventsManager eventsManager;
    private PostManager postManager;
    private Group group;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        String groupName = intent.getStringExtra("groupName");
        groupId = intent.getStringExtra("groupId");
        group = new Group(groupId, groupName, "description?");

        eventsManager = new EventsManager(this, this, update);
        eventsManager.init();
        postManager = new PostManager(this, update, groupId, null);

        initButtons();
        getGroupDescription();
        getFirstEvent();
        getFirstPost();
        toolbar.setTitle(groupName);

    }

    private void getGroupDescription() {
        MainActivity.mDatabase.child("groups").child(groupId).child("description").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    TextView description = findViewById(R.id.description);
                    description.setText(snapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getFirstPost() {
        //Manager.showProgressbar();
        Map<String, Object> data = new HashMap<>();
        data.put("getFirstEvent", true);
        postManager.getPosts(data);
    }

    private void initButtons() {
        events_button = findViewById(R.id.events_button);
        Button posts_button = findViewById(R.id.posts_button);

        events_button.setOnClickListener(view -> {
            Intent intent1 = new Intent(this, GroupEvents.class);
            intent1.putExtra("groupId", groupId);
            startActivity(intent1);
        });

        posts_button.setOnClickListener(view -> {
            Intent intent1 = new Intent(this, PostsOfGroupOrEventActivity.class);
            intent1.putExtra("groupId", groupId);
            startActivity(intent1);
        });

        joinButton = findViewById(R.id.join_button);

        if (MainActivity.MyConnectedGroups.containsKey(groupId)){
            joinButton.setVisibility(View.GONE);
        }
        else {

            joinButton.setVisibility(View.VISIBLE);
            joinButton.setOnClickListener(view -> {
                GroupManager groupManager = new GroupManager(this);
                joinButton.setText("Joining...");
                groupManager.joinGroup(groupId).continueWith(task -> {
                    joinButton.setVisibility(View.GONE);
                    groupManager.add(group);
                    MainActivity.toast(GroupActivity.this, "Welcome to " + group.getTitle(), true);
                    return null;
                });
            });
        }
    }

    private void getFirstEvent() {
        eventsManager.showProgressbar();
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        data.put("getFirstEvent", true);
        eventsManager.getEvents(data);
    }

    public GroupActivity(){}

    @Override
    public void onBackPressed(){
        MembersGroupFragment.groupFragment = null;
        finish();
    }


    @Override
    public void onFinishedUpdating() {
        eventsManager.hideProgressbar();
        eventsManager.recyclerViewAdapter.notifyItemRangeChanged(0, eventsManager.container.size());
    }
}