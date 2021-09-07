package com.example.socialbike.groups.group;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.socialbike.MainActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.socialbike.R;
import com.google.android.material.tabs.TabLayout;

public class GroupActivity extends AppCompatActivity {

    private static GroupActivity groupContainer;
    public TabLayout tabs;
    public MainActivity mainActivity;
    private ViewPager viewPager;
    private String groupId;


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

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        SectionsPagerAdapter2 sectionsPagerAdapter = new SectionsPagerAdapter2(this, getSupportFragmentManager());

        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        changeTab(0);
    }


    public void changeTab(int index){
        viewPager.setCurrentItem(index);
    }

    public Fragment switchFragment(int position) {
        switch (position) {
            case 0:
                return SpeficicGroupFragment.getInstance(groupId);
            case 1:
                return GroupEvents.getInstance();
            case 2:
                return MembersGroupFragment.getInstance();
            default:
                return null;
        }
    }
}