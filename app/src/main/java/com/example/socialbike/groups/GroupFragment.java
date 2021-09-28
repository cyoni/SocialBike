package com.example.socialbike.groups;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.socialbike.MainActivity;
import com.example.socialbike.R;
import com.example.socialbike.RecyclerViewAdapter;
import com.example.socialbike.Updater;
import com.example.socialbike.groups.group.GroupActivity;
import com.example.socialbike.groups.group.GroupDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class GroupFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener, Updater.IUpdate {

    private final boolean isExplore;
    private final GroupContainer groupContainer;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private final ArrayList<Group> container = new ArrayList<>();
    private ProgressBar progressBar;
    private View root;
    private SwipeRefreshLayout swipeLayout;
    protected Set<String> groupIds = new HashSet<>();

    public GroupFragment(GroupContainer groupContainer, boolean isExplore) {
        this.isExplore = isExplore;
        this.groupContainer = groupContainer;
    }

    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), R.layout.item_group, container);
        recyclerViewAdapter.setClassReference(this);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_group, container, false);
            recyclerView = root.findViewById(R.id.recyclerview);
            progressBar = root.findViewById(R.id.progressBar);
            swipeLayout = root.findViewById(R.id.swipe_refresh);

            setSwipeLayout();
            initAdapter();

        }
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (container.isEmpty())
            getGroups();
    }

    private void getGroups() {
        container.clear();
        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> data = new HashMap<>();

        MainActivity.mFunctions
                .getHttpsCallable(isExplore ? "GetAllGroups" : "GetMyGroups")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);

                    parseGroups(response);

                    return null;
                });
    }

    private void parseGroups(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            GroupDTO groupDTO = objectMapper.readValue(response, GroupDTO.class);
            container.addAll(groupDTO.getGroups());
            for (Group group : container)
                groupIds.add(group.getGroupId());
            onFinishedUpdating();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        Group current = container.get(position);
        if (isExplore) {
            holder.joinButton.setVisibility(View.VISIBLE);
            holder.joinButton.setOnClickListener(view -> joinOrLeaveGroup(holder, position));
        }
        else
            holder.joinButton.setVisibility(View.GONE);

        if (isExplore && getIndex(groupContainer.groupsThatImInFragment.container, current.getGroupId()) != -1)
            holder.joinButton.setText("Joined");
        else
            holder.joinButton.setText("Join");
        holder.layout.setOnClickListener(view -> openGroupActivity(current.getGroupId(), current.getTitle()));
        holder.title.setText(current.getTitle());
        holder.description.setText(current.getDescription());
        holder.memberCount.setText(current.getMemberCount() + " members");
    }

    private void joinOrLeaveGroup(RecyclerViewAdapter.ViewHolder holder, int position) {
        if (holder.joinButton.getText().toString().toLowerCase().equals("join")) {
            joinGroup(holder, position);
        } else
            leaveGroup(holder, position);
    }

    private void leaveGroup(RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.joinButton.setText("Join");
        String groupId = container.get(position).getGroupId();
        int index = getIndex(groupContainer.groupsThatImInFragment.container, groupId);
        groupContainer.groupsThatImInFragment.container.remove(index);
        groupContainer.groupsThatImInFragment.recyclerViewAdapter.notifyItemRemoved(index);
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        MainActivity.mFunctions
                .getHttpsCallable("LeaveGroup")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    return null;
                });

    }

    private int getIndex(ArrayList<Group> container, String groupId) {
        for (int i = 0; i < container.size(); i++)
            if (container.get(i).getGroupId().equals(groupId))
                return i;
        return -1;
    }

    private void openGroupActivity(String groupId, String groupName) {
        Intent intent = new Intent(getContext(), GroupActivity.class);
        intent.putExtra("groupId", groupId);
        intent.putExtra("groupName", groupName);
        startActivity(intent);
    }

    private void joinGroup(RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.joinButton.setText("Joining");
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", container.get(position).getGroupId());

        MainActivity.mFunctions
                .getHttpsCallable("JoinGroup")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    groupContainer.groupsThatImInFragment.container.add(0, container.get(position));
                    groupContainer.groupsThatImInFragment.recyclerViewAdapter.notifyItemRangeChanged(0, groupContainer.groupsThatImInFragment.container.size());
                    holder.joinButton.setText("Joined");
                    return null;
                });
    }


    @Override
    public void onItemClick(@NonNull View holder, int position) {
        System.out.println(position);
    }

    private void setSwipeLayout() {
        swipeLayout.setOnRefreshListener(this::getGroups);

        // Scheme colors for animation
        swipeLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );
    }


    @Override
    public void onFinishedUpdating() {
        swipeLayout.setRefreshing(false);
        recyclerViewAdapter.notifyItemRangeChanged(0, container.size());
        progressBar.setVisibility(View.GONE);
    }
}