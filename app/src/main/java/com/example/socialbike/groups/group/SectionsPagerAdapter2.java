package com.example.socialbike.groups.group;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.socialbike.groups.GroupFragment;


public class SectionsPagerAdapter2 extends FragmentPagerAdapter {

    private static final String[] TAB_TITLES = new String[]{"Posts", "Events", "Members"};
    private final GroupActivity groupActivity;


    public SectionsPagerAdapter2(GroupActivity groupActivity, FragmentManager fm) {
        super(fm);
        this.groupActivity = groupActivity;
    }

    @Override
    public Fragment getItem(int position) {
        return groupActivity.switchFragment(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return TAB_TITLES[position];
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}