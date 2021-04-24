package com.example.socialbike;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class HomeFragment extends Fragment {


    private View root;
    private static HomeFragment homeFragment = null;

    public HomeFragment() {

    }

    public static HomeFragment getInstance(){
        if (homeFragment == null){
            homeFragment = new HomeFragment();
        }
        return homeFragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_home, container, false);


        return root;
    }
}