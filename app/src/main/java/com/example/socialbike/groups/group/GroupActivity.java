package com.example.socialbike.groups.group;

import android.content.Intent;
import android.os.Bundle;

import com.example.socialbike.MainActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.socialbike.R;
import com.example.socialbike.groups.IPageAdapter;
import com.example.socialbike.groups.SectionsPagerAdapter;
import com.example.socialbike.groups.TabManager;
import com.google.android.material.tabs.TabLayout;

public class GroupActivity extends FragmentActivity implements IPageAdapter {

    private static GroupActivity groupContainer;
    public TabLayout tabs;
    public MainActivity mainActivity;
    private String groupId;
    private PrivateGroupFragment privateGroupFragment;

    String[] tabTitles = {"Posts", "Events", "Members"};

    public static GroupActivity getInstance() {
        if (groupContainer == null) {
            groupContainer = new GroupActivity();
        }
        return groupContainer;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_group_container2);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        String groupName = intent.getStringExtra("groupName");
        groupId = intent.getStringExtra("groupId");

        toolbar.setTitle(groupName);

        ViewPager viewPager = findViewById(R.id.view_pager);
        tabs = findViewById(R.id.tabs);

        privateGroupFragment = new PrivateGroupFragment(groupId);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabManager tabManager = new TabManager(viewPager, tabs, tabTitles);
        tabManager.init();
    }

    @Override
    public void onBackPressed(){
        GroupEvents.groupFragment = null;
        MembersGroupFragment.groupFragment = null;
        finish();
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return privateGroupFragment;
            case 1:
                return GroupEvents.getInstance(groupId);
            case 2:
                return MembersGroupFragment.getInstance(groupId);
            default:
                return null;
        }
    }

}