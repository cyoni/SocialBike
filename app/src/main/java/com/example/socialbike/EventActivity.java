package com.example.socialbike;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.example.socialbike.events.EventDetails;
import com.example.socialbike.groups.GroupFragment;
import com.example.socialbike.groups.IPageAdapter;
import com.example.socialbike.groups.SectionsPagerAdapter;
import com.example.socialbike.groups.TabManager;
import com.example.socialbike.groups.group.GroupEvents;
import com.example.socialbike.groups.group.MembersGroupFragment;
import com.example.socialbike.groups.group.PrivateGroupFragment;
import com.google.android.material.tabs.TabLayout;

import java.io.Serializable;

public class EventActivity extends AppCompatActivity implements IPageAdapter {

    String[] tabTitles = {"Details", "Discussion"};
    public TabLayout tabLayout;
    EventDetails eventDetails;
    PrivateGroupFragment privateGroupFragment;
    private Event event;

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