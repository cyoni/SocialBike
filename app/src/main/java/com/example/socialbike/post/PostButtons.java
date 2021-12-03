package com.example.socialbike.post;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.socialbike.R;
import com.example.socialbike.activities.PostActivity;
import com.example.socialbike.recyclerview.RecyclerViewAdapter;
import com.example.socialbike.utilities.Utils;

import java.util.ArrayList;

public class PostButtons {

    String groupId, eventId;
    ImageButton likeButton, commentsButton;
    TextView likes, comments;
    Post post;
    Activity activity;

    public PostButtons(Activity activity, Post post, String groupId, String eventId) {
        this.activity = activity;
        this.post = post;
        likes = activity.findViewById(R.id.likes);
        comments = activity.findViewById(R.id.comments);
        likeButton = activity.findViewById(R.id.likeButton);
        commentsButton = activity.findViewById(R.id.commentsButton);
        init();
        this.groupId = groupId;
        this.eventId = eventId;
    }

    public PostButtons(Activity activity, RecyclerViewAdapter.ViewHolder holder, Post post, String groupId, String eventId) {
        this.activity = activity;
        commentsButton = holder.commentsButton;
        likeButton = holder.likeButton;
        likes = holder.likes;
        comments = holder.comments_count;
        this.post = post;
        this.groupId = groupId;
        this.eventId = eventId;
        init();
    }

    private void init() {
        likeButton.setOnClickListener(view -> likeButtonClick());
        commentsButton.setOnClickListener(view -> commentsButtonClick());

        setLikeButton();
        likes.setText(String.valueOf(post.getLikesCount()));
        comments.setText(String.valueOf(post.getCommentsCount()));
    }

    public void likeButtonClick() {
        if (post.getIsLiked()) {
            post.setIsLiked(false);
            post.decrementLike();
        } else {
            post.setIsLiked(true);
            post.incrementLike();
        }
        Utils.registerLike(post, groupId, eventId);
        setLikeButton();
        likes.setText("" + post.getLikesCount());
    }

    public void commentsButtonClick() {
        //   if (!activity.getClass().getSimpleName().equals("PostButtons")) {

        openPost();
        //    }
    /*
        NestedScrollView nestedScrollView = activity.findViewById(R.id.nested_scroll_view);
        EditText commentTextBox = activity.findViewById(R.id.newComment);
        int where = commentTextBox.getTop();
        nestedScrollView.smoothScrollTo(0, where);*/
    }

    public void setLikeButton() {
        if (post.getIsLiked())
            likeButton.setImageResource(R.drawable.ic_like_pressed);
        else
            likeButton.setImageResource(R.drawable.ic_like);
    }

    public void followUser(ArrayList<Post> container, RecyclerViewAdapter.ViewHolder holder, int position) {
        System.out.println("todo");
    }

    public void openPost() {
        Intent intent = new Intent(activity.getApplicationContext(), PostActivity.class);
        intent.putExtra("post", post);
        intent.putExtra("groupId", groupId);
        intent.putExtra("eventId", eventId);
        activity.startActivity(intent);
    }
}
