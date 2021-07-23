package com.example.socialbike;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class HomeFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener, Updater.IUpdate {

    private static HomeFragment homeFragment = null;
    private FloatingActionButton floatingButton;
    private final ArrayList<Post> container = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private Updater updater;
    private MessageGetter messageManager;
    private ProgressBar progressBar;
    private View root;

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
            progressBar.setVisibility(View.INVISIBLE);

            setToolbar(root);
            activateFloatingButton();
            initAdapter();

            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);

            updater = new Updater(this, this.container, recyclerViewAdapter);
            messageManager = new MessageGetter(updater);

            //  messageManager.getPosts();

        }

        return root;
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
        holder.message.setText(container.get(position).getMsg());
        holder.name.setText(container.get(position).getName());

        holder.commentsButton.setOnClickListener(view -> commentsButtonClick(container.get(position)));
        holder.message.setOnClickListener(view -> commentsButtonClick(container.get(position)));
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
    }

}