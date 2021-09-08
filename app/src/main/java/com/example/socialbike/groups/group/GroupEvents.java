package com.example.socialbike.groups.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.socialbike.AddNewEventActivity;
import com.example.socialbike.EventsManager;
import com.example.socialbike.Position;
import com.example.socialbike.R;
import com.example.socialbike.RecyclerViewAdapter;
import com.example.socialbike.Updater;
import com.example.socialbike.groups.Group;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GroupEvents extends Fragment implements Updater.IUpdate {

    public static GroupEvents groupFragment;
    private final String groupId;
    private ArrayList<Group> container;
    private View root;
    private SwipeRefreshLayout swipeLayout;
    private EventsManager eventsManager;
    Updater.IUpdate update = this;

    public GroupEvents(String groupId) {
        this.groupId = groupId;
    }

    public static GroupEvents getInstance(String groupId) {
        if (groupFragment == null) {
            groupFragment = new GroupEvents(groupId);
        }
        return groupFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void setSwipeLayout() {
        swipeLayout.setOnRefreshListener(this::getEvents);

        swipeLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_group_events, container, false);
            groupFragment.eventsManager = new EventsManager(getActivity(), getContext(), update);
            initiateScreen(root);
            setSwipeLayout();
            ExtendedFloatingActionButton extendedFloatingActionButton = root.findViewById(R.id.extended_fab);
            extendedFloatingActionButton.setOnClickListener(view -> {
                Intent intent = new Intent(getContext(), AddNewEventActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            });
        }
        return root;
    }

    private void initiateScreen(View root) {
        eventsManager.init(root);
        swipeLayout = root.findViewById(R.id.swipe_refresh);
        eventsManager.showProgressbar();
        getEvents();
    }

    private void getEvents() {
        eventsManager.showProgressbar();
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        eventsManager.getEvents(data);
    }

    @Override
    public void onFinishedTakingNewMessages() {
        swipeLayout.setRefreshing(false);
        eventsManager.progressBar.setVisibility(View.INVISIBLE);
        eventsManager.recyclerViewAdapter.notifyItemRangeChanged(0, container.size());
        eventsManager.progressBar.setVisibility(View.GONE);
    }

}