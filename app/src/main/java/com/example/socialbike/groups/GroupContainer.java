package com.example.socialbike.groups;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.socialbike.MainActivity;
import com.example.socialbike.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;


public class GroupContainer extends Fragment {

    private ExtendedFloatingActionButton floatingButton;
    private static GroupContainer groupContainer;
    public TabLayout tabs;
    public MainActivity mainActivity;
    private ViewPager viewPager;

    public static GroupContainer getInstance() {
        if (groupContainer == null) {
            groupContainer = new GroupContainer();
        }
        return groupContainer;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_group_container, container, false);
        floatingButton = root.findViewById(R.id.fab);
        activateFloatingButton();

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        viewPager = root.findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        tabs = root.findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(0);
        return root;
    }

    private void activateFloatingButton() {
        floatingButton.setOnClickListener(view -> openCreateGroup());
    }

    private void openCreateGroup() {
        Intent intent = new Intent(getContext(), CreateGroupActivity.class);
        startActivity(intent);
    }

    public void changeTab(int index){
        viewPager.setCurrentItem(index);
    }
}