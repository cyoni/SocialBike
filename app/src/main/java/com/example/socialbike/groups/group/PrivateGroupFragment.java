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

import com.example.socialbike.AddPostActivity;
import com.example.socialbike.ConnectedUser;
import com.example.socialbike.Consts;
import com.example.socialbike.LogInActivity;
import com.example.socialbike.MainActivity;
import com.example.socialbike.MessageGetter;
import com.example.socialbike.Post;
import com.example.socialbike.PostButtons;
import com.example.socialbike.R;
import com.example.socialbike.RecyclerViewAdapter;
import com.example.socialbike.Updater;
import com.example.socialbike.room_database.Member;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class PrivateGroupFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener, Updater.IUpdate {

    public static PrivateGroupFragment homeFragment = null;
    private final String groupId;
    private ExtendedFloatingActionButton floatingButton;
    private final ArrayList<Post> container = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    protected Updater updater;
    private MessageGetter messageManager;
    private ProgressBar progressBar;
    private View root;
    private SwipeRefreshLayout swipeLayout;

    public PrivateGroupFragment(String groupId) {
        this.groupId = groupId;
    }

    public static PrivateGroupFragment getInstance(String groupId) {
        if (homeFragment == null)
            homeFragment = new PrivateGroupFragment(groupId);
        return homeFragment;
    }

    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), R.layout.item_row, container);
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
            recyclerView = root.findViewById(R.id.recyclerview);
            progressBar = root.findViewById(R.id.progressBar);
            swipeLayout = root.findViewById(R.id.swipe_refresh);

            setSwipeLayout();
            activateFloatingButton();
            initAdapter();

            updater = new Updater(this, this.container, recyclerViewAdapter);
            messageManager = new MessageGetter(updater);
            progressBar.setVisibility(View.VISIBLE);
            getPosts();
        }
        return root;
    }

    private void getPosts() {
        container.clear();
        messageManager.getPosts(groupId);
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
        startActivity(intent);
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        Post current = container.get(position);
        String name = Member.getNameFromLocal(current.getPublicKey());
        if (name.equals(Consts.DEFAULT_TMP_NAME)) {
            Member.fetchName(holder, current.getPublicKey());
        }
        holder.message.setText(current.getMsg());
        holder.name.setText(name);
        PostButtons postButtons = new PostButtons(getActivity(), holder, container.get(position));
        holder.message.setOnClickListener(view -> postButtons.commentsButtonClick());
        holder.followButton.setOnClickListener(view -> postButtons.followUser(container, holder, position));
    }



    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }

    @Override
    public void onFinishedUpdating() {
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        swipeLayout.setRefreshing(false);
    }
}