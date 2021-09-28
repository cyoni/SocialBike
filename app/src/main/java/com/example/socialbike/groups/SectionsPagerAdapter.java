package com.example.socialbike.groups;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.socialbike.groups.IPageAdapter;


public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private final IPageAdapter pageAdapter;

    public SectionsPagerAdapter(FragmentManager fragmentActivity, IPageAdapter pageAdapter) {
        super((FragmentManager) fragmentActivity, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.pageAdapter = pageAdapter;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        return pageAdapter.createFragment(position);
    }

    @Override
    public int getCount() {
        return pageAdapter.getCount();
    }
}