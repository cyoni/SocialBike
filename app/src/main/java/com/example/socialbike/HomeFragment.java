package com.example.socialbike;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class HomeFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener{

    private static HomeFragment homeFragment = null;
    private FloatingActionButton floatingButton;
    private final ArrayList<Post> container;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private Updater updater;

    public HomeFragment() {
        container = new ArrayList<>();
    }

    public static HomeFragment getInstance(){
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        floatingButton = root.findViewById(R.id.fab);
        recyclerView = root.findViewById(R.id.recyclerview);
        activateFloatingButton();
        initAdapter();
        updater = new Updater(this.container, recyclerViewAdapter);

        return root;
    }

    private void activateFloatingButton() {
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //openNewPostActivity();
                MessageManager messageManager = new MessageManager(updater);
                messageManager.getPosts();
            }
        });
    }

    private void openNewPostActivity() {
        Intent intent = new Intent(getContext(), AddPostActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.message.setText(container.get(position).getMsg());
        holder.name.setText(container.get(position).getName());
    }

    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }

    public void addPost(Post post) {
        updater.add(post);
        updater.update();
    }
}