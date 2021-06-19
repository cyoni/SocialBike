package com.example.socialbike;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ProfileFragment extends Fragment {
    ArrayAdapter adapter;

    static ProfileFragment profileFragment = null;

    public static ProfileFragment getInstance() {
        if (profileFragment == null) {
            profileFragment = new ProfileFragment();
        }
        return profileFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        String[] options = {"Following", "Friends", "Follow", "Posts", "Events", "Send message"};

        // Inflate the layout for this fragment
        adapter = new ArrayAdapter<>(getContext(), R.layout.item_listview, options);

        ListView listView =  root.findViewById(R.id.ListView);
        listView.setAdapter(adapter);

        return root;
    }
}