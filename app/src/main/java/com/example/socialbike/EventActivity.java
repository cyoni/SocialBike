package com.example.socialbike;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import com.example.socialbike.events.EventDetails;
import com.example.socialbike.groups.GroupFragment;
import com.example.socialbike.groups.IPageAdapter;
import com.example.socialbike.groups.SectionsPagerAdapter;
import com.example.socialbike.groups.TabManager;
import com.example.socialbike.groups.group.GroupEvents;
import com.example.socialbike.groups.group.MembersGroupFragment;
import com.example.socialbike.groups.group.PrivateGroupFragment;
import com.google.android.material.tabs.TabLayout;

public class EventActivity extends AppCompatActivity implements IPageAdapter {

    String[] tabTitles = {"Details", "Discussion"};
    public TabLayout tabLayout;
    EventDetails eventDetails;
    String groupId, eventId;

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
        groupId = intent.getStringExtra("groupId");
        eventId = intent.getStringExtra("eventId");

        TabManager tabManager = new TabManager(viewPager, tabLayout, tabTitles);
        tabManager.init();
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(sectionsPagerAdapter);

        eventDetails = new EventDetails("" +
                "" +
                "טיול מיטיבי לכת כפול: מחורבת מנות נרד אל מבצר המונפורט המרשים ומשם בשביל מוצל אל בריכות המים בנחל כזיב לרחצה בעין טמיר ונסיים במצד אבירים. לא חובה להרטב !" +
                "");

    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return eventDetails;
            case 1:
                return PrivateGroupFragment.getInstance(groupId, eventId);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }
}