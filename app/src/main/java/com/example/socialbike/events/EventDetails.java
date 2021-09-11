package com.example.socialbike.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.socialbike.EventsManager;
import com.example.socialbike.R;

public class EventDetails extends Fragment {

    private final String details;
    private View root;

    public EventDetails(String details) {
        this.details = details;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.event_details, container, false);

            TextView textView = root.findViewById(R.id.description);
            textView.setText(details);
        }
        return root;
    }

}
