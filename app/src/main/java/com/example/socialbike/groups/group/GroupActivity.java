package com.example.socialbike.groups.group;

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

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        changeTab(0);
    }

    @Override
    public void onBackPressed(){
        PrivateGroupFragment.homeFragment = null;
        GroupEvents.groupFragment = null;
        MembersGroupFragment.groupFragment = null;
        finish();
    }


    public void changeTab(int index){
        viewPager.setCurrentItem(index);
    }

    public Fragment switchFragment(int position) {
        switch (position) {
            case 0:
                return PrivateGroupFragment.getInstance(groupId);
            case 1:
                return GroupEvents.getInstance(groupId);
            case 2:
                return MembersGroupFragment.getInstance(groupId);
            default:
                return null;
        }
    }
}