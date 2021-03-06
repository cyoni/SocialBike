package com.example.socialbike.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.socialbike.R;


public class FriendsFragment extends Fragment {

    private View root;
    private static FriendsFragment friendsFragment = null;


    public FriendsFragment() {

    }


    public static FriendsFragment getInstance(){
        if (friendsFragment == null){
            friendsFragment = new FriendsFragment();
        }
        return friendsFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.activity_group_posts, container, false);


        return root;
    }
}