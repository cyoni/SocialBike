package com.example.socialbike.post;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialbike.utilities.EMethods;
import com.example.socialbike.R;
import com.example.socialbike.model.PostDTO;
import com.example.socialbike.recyclerview.RecyclerViewAdapter;
import com.example.socialbike.room_database.Member;
import com.example.socialbike.utilities.Consts;
import com.example.socialbike.utilities.Updater;
import com.example.socialbike.utilities.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostManager implements RecyclerViewAdapter.ItemClickListener{

    private View root;
    private Activity activity;
    private Updater.IUpdate updater;
    private ArrayList<Post> container = new ArrayList<>();
    private String groupId;
    private String eventId;
    private ProgressBar progressBar;
    public RecyclerViewAdapter recyclerViewAdapter;
    private final RecyclerView recyclerView;

    public PostManager(Activity activity, View root, Updater.IUpdate update, String groupId, String eventId) {
        this.activity = activity;
        this.root = root;
        this.recyclerView = root.findViewById(R.id.post_recyclerView);
        init(activity, update, groupId, eventId);
    }

    public PostManager(Activity activity, Updater.IUpdate update, String groupId, String eventId) {
        this.recyclerView = activity.findViewById(R.id.post_recyclerView);
        init(activity, update, groupId, eventId);
    }

    private void init(Activity activity, Updater.IUpdate update, String groupId, String eventId) {
        this.activity = activity;
        this.updater = update;
        this.groupId = groupId;
        this.eventId = eventId;
        initAdapter();
        initProperties();
    }

    private void initProperties() {
        if (root != null){
            progressBar = root.findViewById(R.id.posts_progressBar);
        }
        else
            progressBar = activity.findViewById(R.id.posts_progressBar);
    }

    protected void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(activity, R.layout.item_post, container);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }

    public void parseMessages(String response){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            PostDTO postDTO = objectMapper.readValue(response, PostDTO.class);
            container.addAll(postDTO.getPosts());
            updater.onFinishedUpdating();
            recyclerViewAdapter.notifyItemRangeChanged(0, container.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void getPosts(Map<String, Object> payload){

        if (payload == null)
            payload = new HashMap<>();

        System.out.println("getting posts...");
        payload.put("groupId", groupId);
        if (eventId != null)
            payload.put("eventId", eventId);

        Utils.PostData(EMethods.getPosts, payload)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response: " + response);
                    if (!response.isEmpty())
                        parseMessages(response);
                    hide();
                    return "";
                });
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        Post current = container.get(position);
        String name = Member.getNameFromLocal(current.getPublicKey());
        if (name.equals(Consts.DEFAULT_TMP_NAME)) {
            Member.fetchName(holder.name, current.getPublicKey());
        }
        holder.message.setText(current.getMsg());
        holder.name.setText(name);
        current.setName(name);
        PostButtons postButtons = new PostButtons(activity, holder, container.get(position), groupId, eventId);
        holder.message.setOnClickListener(view -> postButtons.commentsButtonClick());
        holder.followButton.setOnClickListener(view -> postButtons.followUser(container, holder, position));
    }

    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }

    public void hide() {
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
    }

    public void clean() {
        container.clear();
    }
}
