package com.example.socialbike.groups.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.socialbike.events.EventsManager;
import com.example.socialbike.activities.MainActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.socialbike.groups.Group;
import com.example.socialbike.post.PostManager;
import com.example.socialbike.R;
import com.example.socialbike.utilities.Updater;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class GroupActivity extends AppCompatActivity implements Updater.IUpdate {

    public MainActivity mainActivity;
    private String groupId, ownerOfGroup;
    Button events_button, joinButton;
    Updater.IUpdate update = this;
    private EventsManager eventsManager;
    private PostManager postManager;
    private Group group;
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.inflateMenu(R.menu.group);
        setToolbarMenuListener(toolbar);

        Intent intent = getIntent();
        String groupName = intent.getStringExtra("groupName");
        groupId = intent.getStringExtra("groupId");
        ownerOfGroup = intent.getStringExtra("ownerOfGroup");

        group = new Group(groupId, groupName, "description?", ownerOfGroup);

        eventsManager = new EventsManager(this, this, update, false);
        eventsManager.init();
        postManager = new PostManager(this, update, groupId, null);

        joinButton = findViewById(R.id.join_button);

        initButtons();
        getGroupDescription();
        getFirstEvent();
        getFirstPost();
        toolbar.setTitle(groupName);

    }

    private void setToolbarMenuListener(Toolbar toolbar) {
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.leave_group) {
                    MainActivity.groupManager.MyConnectedGroups.remove(groupId);
                    MainActivity.groupManager.leaveGroup(groupId);
                    showOrHideJoinButton();
                }

                return false;
            }
        });
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
        data.put("getFirstPost", true);
        postManager.getPosts(data);
    }

    private void initButtons() {
        events_button = findViewById(R.id.events_button);
        Button posts_button = findViewById(R.id.posts_button);

        events_button.setOnClickListener(view -> {
            Intent intent1 = new Intent(this, GroupEvents.class);
            intent1.putExtra("groupId", groupId);
            intent1.putExtra("ownerOfGroup", ownerOfGroup);
            startActivity(intent1);
        });

        posts_button.setOnClickListener(view -> {
            Intent intent1 = new Intent(this, GroupPosts.class);
            intent1.putExtra("groupId", groupId);
            startActivity(intent1);
        });

        joinButton.setOnClickListener(view -> {
            if (!joinButton.getText().toString().equals("Joining...")) {
                joinButton.setText("Joining...");
                MainActivity.groupManager.joinGroup(groupId).continueWith(task -> {
                    joinButton.setVisibility(View.GONE);
                    joinButton.setText("Join Group");
                    MainActivity.groupManager.add(group);
                    MainActivity.toast(GroupActivity.this, "Welcome to " + group.getTitle(), true);
                    showOrHideJoinButton();
                    return null;
                });
            }
        });

        showOrHideJoinButton();
    }

    private void showOrHideJoinButton() {

        if (MainActivity.groupManager.MyConnectedGroups.containsKey(groupId)){
            joinButton.setVisibility(View.GONE);
            toolbar.getMenu().findItem(R.id.leave_group).setVisible(true);
        }
        else {
            toolbar.getMenu().findItem(R.id.leave_group).setVisible(false);
            joinButton.setVisibility(View.VISIBLE);
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