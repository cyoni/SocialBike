package com.example.socialbike.groups;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class TabManager {

    private final String[] tabTitles;
    private final ViewPager viewPager;
    public TabLayout tabLayout;

    public TabManager(ViewPager viewPager, TabLayout tabs, String[] tabTitles) {
        this.viewPager = viewPager;
        this.tabTitles = tabTitles;
        this.tabLayout = tabs;
    }

    public void init(){
        setTabTitles();
        setTabListener();
        setViewPageListener();
    }

    private void setViewPageListener() {
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    private void setTabListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void setTabTitles() {
        for (String tabTitle : tabTitles){
            tabLayout.addTab(tabLayout.newTab().setText(tabTitle));
        }
    }
}
