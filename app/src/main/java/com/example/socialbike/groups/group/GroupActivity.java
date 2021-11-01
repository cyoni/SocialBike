package com.example.socialbike.groups.group;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.socialbike.MainActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import com.example.socialbike.R;

public class GroupActivity extends AppCompatActivity {

    private static GroupActivity groupContainer;
    public MainActivity mainActivity;
    private String groupId;
    Button events_button;

    public static GroupActivity getInstance() {
        if (groupContainer == null) {
            groupContainer = new GroupActivity();
        }
        return groupContainer;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        String groupName = intent.getStringExtra("groupName");
        groupId = intent.getStringExtra("groupId");

        events_button = findViewById(R.id.events_button);
        events_button.setOnClickListener(view -> {
            Intent intent1 = new Intent(this, GroupEvents.class);
            intent1.putExtra("groupId", groupId);
            startActivity(intent1);
        });
        toolbar.setTitle(groupName);

    }

    public GroupActivity(){}

    @Override
    public void onBackPressed(){
        MembersGroupFragment.groupFragment = null;
        finish();
    }


}