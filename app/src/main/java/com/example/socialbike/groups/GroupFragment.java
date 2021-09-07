package com.example.socialbike.groups;

import android.app.Activity;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GroupFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener, Updater.IUpdate {

    private static GroupFragment groupFragment;
    private static GroupFragment groupFragment2;
    private final boolean isExplore;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private final ArrayList<Group> container = new ArrayList<>();
    private ProgressBar progressBar;
    private View root;
    private SwipeRefreshLayout swipeLayout;

    public GroupFragment(boolean isExplore) {
        this.isExplore = isExplore;
    }


    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), R.layout.item_group, container);
        recyclerViewAdapter.setClassReference(this);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    public static GroupFragment getInstance() {
        if (groupFragment == null) {
            groupFragment = new GroupFragment(false);
        }
        return groupFragment;
    }

    public static GroupFragment getInstance2() {
        if (groupFragment2 == null) {
            groupFragment2 = new GroupFragment(true);
        }
        return groupFragment2;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_group, container, false);
            recyclerView = root.findViewById(R.id.recyclerview);
            progressBar = root.findViewById(R.id.progressBar);
            swipeLayout = root.findViewById(R.id.swipe_refresh);

            setSwipeLayout();
            initAdapter();
            getGroups();
        }
        return root;
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
        String tmp_category;
        try {
            JSONObject obj = new JSONObject(response);
            JSONArray messages_array = obj.getJSONArray("groups");

/*            if (obj.has("upNext")) {
                upNext = obj.getString("upNext");
            }
            else
                upNext = DEFAULT_END_OF_LIST;*/

            for (int i = 0; i < messages_array.length(); i++) {
                String title = messages_array.getJSONObject(i).getString("title");
                String description = messages_array.getJSONObject(i).getString("description");
                String groupId = messages_array.getJSONObject(i).getString("groupId");

                Group group = new Group(groupId, title, description);
                container.add(group);
            }
            onFinishedTakingNewMessages();
        } catch (Exception e) {
            System.out.println("Error caught in message fetcher: " + e.getMessage());
        }
    }


    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        Group current = container.get(position);
        holder.layout.setOnClickListener(view -> openGroupActivity(current.getGroupId()));
        holder.title.setText(current.getTitle());
        holder.description.setText(current.getDescription());
        if (isExplore){
            holder.joinButton.setVisibility(View.VISIBLE);
            holder.joinButton.setOnClickListener(view -> joinGroup(holder, position));
        }
    }

    private void openGroupActivity(String groupId) {
        Intent intent = new Intent(getContext(), GroupActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
    }

    private void joinGroup(RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.joinButton.setText("Joining...");
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", container.get(position).getGroupId());

        MainActivity.mFunctions
                .getHttpsCallable("JoinGroup")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    holder.joinButton.setText("Joined");
                    recyclerViewAdapter.notifyItemChanged(position);
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
    public void onFinishedTakingNewMessages() {
        swipeLayout.setRefreshing(false);
        recyclerViewAdapter.notifyItemRangeChanged(0, container.size());
        progressBar.setVisibility(View.GONE);
    }
}