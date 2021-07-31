package com.example.socialbike;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class HomeFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener, Updater.IUpdate {

    private static HomeFragment homeFragment = null;
    private ExtendedFloatingActionButton floatingButton;
    private final ArrayList<Post> container = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    protected Updater updater;
    private MessageGetter messageManager;
    private ProgressBar progressBar;
    private View root;
    private SwipeRefreshLayout swipeLayout;

    public static HomeFragment getInstance() {
        if (homeFragment == null)
            homeFragment = new HomeFragment();
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
            setToolbar(root);
            activateFloatingButton();
            initAdapter();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0) {
                     //   if (floatingButton.isExtended())
                     //       floatingButton.setExtended(false);
                    } else if (dy < 0) {
                    //    if (!floatingButton.isExtended())

                     //       floatingButton.setExtended(true);
                    }
                }
            });
            updater = new Updater(this, this.container, recyclerViewAdapter);
            messageManager = new MessageGetter(updater);

            progressBar.setVisibility(View.VISIBLE);

            getPosts();

        }

        return root;
    }



    private void getPosts() {
        container.clear();
        recyclerView.setVisibility(View.INVISIBLE);
        messageManager.getPosts();
    }

    private void setSwipeLayout() {
        swipeLayout.setOnRefreshListener(this::getPosts);

        // Scheme colors for animation
        swipeLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );
    }

    private void setToolbar(View root) {
        Toolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        toolbar.inflateMenu(R.menu.main_menu);
        toolbar.setOnMenuItemClickListener(this::toolbarClickListener);
    }

    private boolean toolbarClickListener(MenuItem item) {
        if (item.getItemId() == R.id.login) {
            openLoginActivity();
            return true;
        }
        return false;
    }

    private void openLoginActivity() {
        Intent intent = new Intent(getContext(), LogInActivity.class);
        startActivity(intent);
    }

    private void activateFloatingButton() {
        floatingButton.setOnClickListener(view -> openNewPostActivity());
    }

    private void openNewPostActivity() {
        Intent intent = new Intent(getContext(), AddPostActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        Post current = container.get(position);
        holder.message.setText(container.get(position).getMsg());
        holder.name.setText(container.get(position).getName());
        holder.likes.setText(String.valueOf(container.get(position).getLikesCount()));

        PostButtons postButtons = new PostButtons();

        System.out.println(current.getIsLiked() + "$$$$");

        postButtons.changeLikeButton(holder, current.getIsLiked());

        holder.commentsButton.setOnClickListener(view -> commentsButtonClick(container.get(position)));
        holder.likeButton.setOnClickListener(view -> postButtons.likeButtonClick(container, holder, position));
        holder.message.setOnClickListener(view -> commentsButtonClick(container.get(position)));
        holder.followButton.setOnClickListener(view -> postButtons.followUser(container, holder, position));
    }

    private void commentsButtonClick(Post post) {
        Intent intent = new Intent(getContext(), PostActivity.class);
        intent.putExtra("post", post);
        startActivity(intent);
    }

    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }

    public void addPost(Post post) {
        updater.add(post);
    }

    @Override
    public void onFinishedTakingNewMessages() {
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        swipeLayout.setRefreshing(false);
    }

}