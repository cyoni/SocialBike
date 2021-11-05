package com.example.socialbike.groups.group;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.socialbike.AddNewEventActivity;
import com.example.socialbike.EventsManager;
import com.example.socialbike.Position;
import com.example.socialbike.R;
import com.example.socialbike.RecyclerViewAdapter;
import com.example.socialbike.Updater;
import com.example.socialbike.groups.Group;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
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
        setContentView(R.layout.fragment_group_events);

        this.groupId = getIntent().getStringExtra("groupId");

        eventsManager = new EventsManager(this, this, update);
        initiateScreen();
        setSwipeLayout();
        ExtendedFloatingActionButton extendedFloatingActionButton = findViewById(R.id.extended_fab);
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