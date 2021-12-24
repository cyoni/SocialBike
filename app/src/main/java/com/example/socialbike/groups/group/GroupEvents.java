package com.example.socialbike.groups.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.socialbike.activities.AddNewEventActivity;
import com.example.socialbike.events.EventsManager;
import com.example.socialbike.R;
import com.example.socialbike.utilities.Updater;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.HashMap;
import java.util.Map;


public class GroupEvents extends FragmentActivity implements Updater.IUpdate {

    private String groupId;
    private SwipeRefreshLayout swipeLayout;
    private EventsManager eventsManager;
    Updater.IUpdate update = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_events);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        this.groupId = getIntent().getStringExtra("groupId");

        String groupTitle = getIntent().getStringExtra("title");
        toolbar.setTitle("Events - " + groupTitle);

        eventsManager = new EventsManager(this, this, update);
        initiateScreen();
        setSwipeLayout();
        ExtendedFloatingActionButton extendedFloatingActionButton = findViewById(R.id.fab);
        extendedFloatingActionButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddNewEventActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        });


    }

    public GroupEvents(){}


    private void setSwipeLayout() {
        swipeLayout = findViewById(R.id.swipe_refresh);
        swipeLayout.setOnRefreshListener(this::getEvents);

        swipeLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );
    }


    private void initiateScreen() {
        eventsManager.init();
        swipeLayout = findViewById(R.id.swipe_refresh);
        eventsManager.showProgressbar();
        getEvents();
    }

    private void getEvents() {
        eventsManager.showProgressbar();
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        eventsManager.getEvents(data);
    }

    @Override
    public void onFinishedUpdating() {
        eventsManager.hideProgressbar();
        swipeLayout.setRefreshing(false);
        eventsManager.progressBar.setVisibility(View.INVISIBLE);
        eventsManager.recyclerViewAdapter.notifyItemRangeChanged(0, eventsManager.container.size());
        eventsManager.progressBar.setVisibility(View.GONE);
    }

}