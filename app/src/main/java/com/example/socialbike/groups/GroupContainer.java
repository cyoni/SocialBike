package com.example.socialbike.groups;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.socialbike.MainActivity;
import com.example.socialbike.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;


public class GroupContainer extends Fragment implements IPageAdapter {

    private ExtendedFloatingActionButton floatingButton;
    private static GroupContainer groupContainer;
    public TabLayout tabLayout;
    public MainActivity mainActivity;
    String[] tabTitles = {"My Groups", "Explore"};
    private View root;
    protected final GroupFragment groupsThatImInFragment = new GroupFragment(this, false);
    protected final GroupFragment exploreFragment = new GroupFragment(this, true);

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_group_container, container, false);
            floatingButton = root.findViewById(R.id.fab);
            activateFloatingButton();

            ViewPager viewPager = root.findViewById(R.id.view_pager);
            tabLayout = root.findViewById(R.id.tabs);

            TabManager tabManager = new TabManager(viewPager, tabLayout, tabTitles);
            tabManager.init();
            SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getParentFragmentManager(), this);
            viewPager.setAdapter(sectionsPagerAdapter);

         //   tabLayout.getTabAt(0).getOrCreateBadge().setNumber(3);

        }
        return root;
    }


    private void activateFloatingButton() {
        floatingButton.setOnClickListener(view -> openCreateGroup());
    }

    private void openCreateGroup() {
        Intent intent = new Intent(getContext(), CreateGroupActivity.class);
        startActivity(intent);
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return groupsThatImInFragment;
            case 1:
                return exploreFragment;
            default:
                return null;
        }
    }

}