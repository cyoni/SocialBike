package com.example.socialbike.groups.group;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.socialbike.ConnectedUser;
import com.example.socialbike.MainActivity;
import com.example.socialbike.R;
import com.example.socialbike.RecyclerViewAdapter;
import com.example.socialbike.Updater;
import com.example.socialbike.groups.Group;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class MembersGroupFragment extends Fragment
        implements RecyclerViewAdapter.ItemClickListener, Updater.IUpdate {

    public static MembersGroupFragment groupFragment;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private final ArrayList<String> container = new ArrayList<>();
    private ProgressBar progressBar;
    private View root;
    private SwipeRefreshLayout swipeLayout;
    private String groupId;

    public MembersGroupFragment(String groupId) {
        this.groupId = groupId;
    }


    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), R.layout.item_member, container);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }

    public static MembersGroupFragment getInstance(String groupId) {
        if (groupFragment == null) {
            groupFragment = new MembersGroupFragment(groupId);
        }
        return groupFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.name.setText(container.get(position));
    }


    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }

    private void setSwipeLayout() {
        swipeLayout.setOnRefreshListener(this::getMembers);

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
            root = inflater.inflate(R.layout.fragment_group, container, false);
            recyclerView = root.findViewById(R.id.recyclerview);
            progressBar = root.findViewById(R.id.progressBar);
            swipeLayout = root.findViewById(R.id.swipe_refresh);

            progressBar.setVisibility(View.VISIBLE);
            setSwipeLayout();
            initAdapter();
            getMembers();
        }
        return root;
    }

    private void getMembers() {
        container.clear();
        MainActivity.mDatabase.child("groups").child(groupId).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                    DataSnapshot tmp;
                    while (iterator.hasNext()) {
                        tmp = iterator.next();
                        container.add(tmp.getKey());
                    }
                }
                    onFinishedTakingNewMessages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onFinishedTakingNewMessages() {
        swipeLayout.setRefreshing(false);
        recyclerViewAdapter.notifyItemRangeChanged(0, container.size());
        progressBar.setVisibility(View.GONE);
    }
}