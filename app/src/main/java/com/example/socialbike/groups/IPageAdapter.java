package com.example.socialbike.groups;

import androidx.fragment.app.Fragment;

public interface IPageAdapter {

    Fragment createFragment(int position);
    int getCount();

}
