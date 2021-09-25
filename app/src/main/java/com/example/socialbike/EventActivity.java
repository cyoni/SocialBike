package com.example.socialbike;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.socialbike.events.EventDetails;
import com.example.socialbike.groups.IPageAdapter;
import com.example.socialbike.groups.SectionsPagerAdapter;
import com.example.socialbike.groups.TabManager;
import com.example.socialbike.groups.group.PrivateGroupFragment;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EventActivity extends AppCompatActivity implements IPageAdapter {

    String[] tabTitles = {"Details", "Discussion"};
    public TabLayout tabLayout;
    EventDetails eventDetails;
    PrivateGroupFragment privateGroupFragment;
    private Event event;
    Button save, interested, going;
    LinearLayout interestedLayOut, goingLayout;
    TextView interested_count, going_count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ViewPager viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tabs);

        Intent intent = getIntent();
        event = (Event) intent.getSerializableExtra("event");

        TabManager tabManager = new TabManager(viewPager, tabLayout, tabTitles);
        tabManager.init();
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(sectionsPagerAdapter);

        privateGroupFragment = new PrivateGroupFragment(event.getGroupId(), event.getEventId());
        eventDetails = new EventDetails(event.getDetails());

        setAllFields();
    }

    private void setAllFields() {
        TextView date_and_time = findViewById(R.id.date_and_time);
        date_and_time.setText(event.getDate());
        TextView location = findViewById(R.id.location);
        location.setText(event.getAddress());
        location.setOnClickListener(view -> openMap());
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.title);
        collapsingToolbarLayout.setTitle(event.getTitle());

        save = findViewById(R.id.save_button);
        interested = findViewById(R.id.interested_button);
        going = findViewById(R.id.going_button);

        interested_count = findViewById(R.id.interested_count);
        going_count = findViewById(R.id.going_count);

        goingLayout = findViewById(R.id.going_layout);
        interestedLayOut = findViewById(R.id.interested_layout);

        goingLayout.setOnClickListener(view -> showWhoIsGoing());
        interestedLayOut.setOnClickListener(view -> showWhoIsInterested());

        save.setOnClickListener(view -> saveEvent());
        interested.setOnClickListener(view -> interested());
        going.setOnClickListener(view -> go());

        setPressed(going, event.getIsGoing());
        setPressed(interested, event.getIsInterested());
        setPressed(save, getIsEventSavedInLocal());

        going_count.setText(String.valueOf(event.getNumParticipants()));
        interested_count.setText(String.valueOf(event.getNumInterestedMembers()));
    }

    private boolean getIsEventSavedInLocal() {
        Map<String, ?> map = Utils.getAllPreferences(this, "saved_events");
        Set<String> keys = map.keySet();
        return (keys.contains(event.getEventId()));
    }

    private void openMap() {
        Maps.openMap(this, event.getPosition(), true);
    }

    private void showWhoIsGoing() {
        if (!going_count.getText().toString().equals("0")) {
            MembersList membersList = new MembersList(this, event.getGroupId(), event.getEventId(), "going");
            membersList.show();
        }
    }

    private void showWhoIsInterested() {
        if (!interested_count.getText().toString().equals("0")) {
            MembersList membersList = new MembersList(this, event.getGroupId(), event.getEventId(), "interested");
            membersList.show();
        }
    }

    private void interested() {
        event.setIsInterested(!event.getIsInterested());
        interested_count.setText(String.valueOf(event.getNumInterestedMembers()));
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", event.getEventId());
        data.put("action", event.getIsInterested());

        setPressed(interested, event.getIsInterested());

        MainActivity.mFunctions
                .getHttpsCallable("interested")
                .call(data)
                .continueWith(task -> {

                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    return "";
                });
    }

    private void setPressed(Button button, boolean state) {
        if (state)
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.button_pressed));
        else
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white));
    }

    private void go() {
        event.setIsGoing(!event.getIsGoing());
        setPressed(going, event.getIsGoing());
        going_count.setText(String.valueOf(event.getNumParticipants()));

        Map<String, Object> data = new HashMap<>();
        data.put("eventId", event.getEventId());
        MainActivity.mFunctions
                .getHttpsCallable("going")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    return "";
                });
    }

    private void saveEvent() {
        boolean isSaved = getIsEventSavedInLocal();
        if (isSaved)
            Utils.removePreference(this, "saved_events", event.getEventId());
        else
             Utils.savePreference(this,"saved_events", event.getEventId(), event.getGroupId() + ",");
        setPressed(save, !isSaved);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return eventDetails;
            case 1:
                return privateGroupFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }
}