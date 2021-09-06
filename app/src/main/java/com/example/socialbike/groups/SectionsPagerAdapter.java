package com.example.socialbike.groups;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.socialbike.ProfileFragment;
import com.example.socialbike.groups.GroupFragment;


public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private static final String[] TAB_TITLES = new String[]{"My Groups", "Explore"};


    public SectionsPagerAdapter( FragmentManager fm) {
        super(fm);
     //   mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return GroupFragment.getInstance();
            case 1:
                return GroupFragment.getInstance2();
            default:
                return null;
        }
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