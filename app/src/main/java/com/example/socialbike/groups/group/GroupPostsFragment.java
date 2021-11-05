package com.example.socialbike.groups.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.socialbike.AddPostActivity;
import com.example.socialbike.MessageGetter;
import com.example.socialbike.Post;
import com.example.socialbike.R;
import com.example.socialbike.RecyclerViewAdapter;
import com.example.socialbike.Updater;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class GroupPostsFragment extends Fragment implements Updater.IUpdate {

    private final String groupId;
    private ExtendedFloatingActionButton floatingButton;
    private final ArrayList<Post> container = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    protected Updater updater;
    private MessageGetter messageManager;
    private View root;
    private SwipeRefreshLayout swipeLayout;
    private String eventId;

    public GroupPostsFragment(String groupId) {
        this.groupId = groupId;
    }

    public GroupPostsFragment(String groupId, String eventId) {
        this.groupId = groupId;
        this.eventId = eventId;
    }

    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), R.layout.item_post, container);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_home, container, false);
            floatingButton = root.findViewById(R.id.fab);
            recyclerView = root.findViewById(R.id.post_recyclerView);
            swipeLayout = root.findViewById(R.id.swipe_refresh);

            setSwipeLayout();
            activateFloatingButton();
            initAdapter();

            updater = new Updater(this, this.container, recyclerViewAdapter);
            messageManager = new MessageGetter(updater);
            getPosts();
        }
        return root;
    }

    private void getPosts() {
        container.clear();
        messageManager.getPosts(groupId, eventId);
    }

    private void setSwipeLayout() {
        swipeLayout.setOnRefreshListener(this::getPosts);
        swipeLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );
    }

    private void activateFloatingButton() {
        floatingButton.setOnClickListener(view -> openNewPostActivity());
    }

    private void openNewPostActivity() {
        Intent intent = new Intent(getContext(), AddPostActivity.class);
        intent.putExtra("groupId", groupId);
        if (eventId != null)
            intent.putExtra("eventId", eventId);
        startActivity(intent);
    }

    @Override
    public void onFinishedUpdating() {
        recyclerViewAdapter.notifyDataSetChanged();
        recyclerView.setVisibility(View.VISIBLE);
        swipeLayout.setRefreshing(false);

    }
}