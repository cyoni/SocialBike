package com.example.socialbike.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.socialbike.R;
import com.example.socialbike.events.Event;
import com.example.socialbike.events.EventsManager;
import com.example.socialbike.groups.Group;
import com.example.socialbike.recyclerview.RecyclerViewAdapter;
import com.example.socialbike.utilities.Updater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteEventsActivity extends AppCompatActivity implements Updater.IUpdate {


    EventsManager eventsManager = new EventsManager(this, this, this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_events);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Saved events");
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        eventsManager.init();
        getEvents();
        setSwipeLayout();
    }

    private void setSwipeLayout() {
        eventsManager.swipe_refresh.setOnRefreshListener(this::getEvents);

        eventsManager.swipe_refresh.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );
    }


    private void getEvents() {
        String events = MainActivity.favoriteEventsService.toString();
        Map<String, Object> data = new HashMap<>();
        data.put("specificEvents", events);
        eventsManager.getEvents(data);
    }


    @Override
    public void onFinishedUpdating() {
        eventsManager.hideProgressbar();
        eventsManager.recyclerViewAdapter.notifyDataSetChanged();
    }
}