package com.example.socialbike.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
    TextView emptyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_events);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Saved events");
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        emptyList = findViewById(R.id.empty_favorite_list);
        hideEmptyListIndicator();

        eventsManager.init();
        eventsManager.showProgressbar();
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
        if (MainActivity.favoriteEventsService.getEvents().isEmpty()){
            eventsManager.container.clear();
            onFinishedUpdating();
            return;
        }
        String events = MainActivity.favoriteEventsService.toString();
        Map<String, Object> data = new HashMap<>();
        data.put("specificEvents", events);
        eventsManager.getEvents(data);
    }

    private void showEmptyListIndicator(){
        emptyList.setVisibility(View.VISIBLE);
    }

    private void hideEmptyListIndicator(){
        emptyList.setVisibility(View.GONE);
    }


    @Override
    public void onFinishedUpdating() {
        eventsManager.hideProgressbar();
        if (eventsManager.container.isEmpty()){
            showEmptyListIndicator();
        }
        else{
            hideEmptyListIndicator();
        }
        eventsManager.recyclerViewAdapter.notifyDataSetChanged();
    }
}