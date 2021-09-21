package com.example.socialbike;

import android.app.Activity;
import android.content.Intent;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.arch.core.internal.SafeIterableMap;
import androidx.core.widget.NestedScrollView;

import com.google.firebase.database.DatabaseReference;

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
            registerLike(post, false);
            post.decrementLike();
        } else {
            post.setIsLiked(true);
            registerLike(post, true);
            post.incrementLike();
        }
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

    private void registerLike(Post post, boolean state) {
        DatabaseReference route;

        if (post instanceof Comment) {
            System.out.println("comment TODO");
            return;
        }

        if (eventId == null)
            route = MainActivity.
                    mDatabase.
                    child("groups").
                    child(groupId);
         else
            route = MainActivity.
                    mDatabase.
                    child("events").
                    child(eventId);

        route = route.child("posts").
                child(post.getPostId()).
                child("likes").
                child(ConnectedUser.getPublicKey());

        if (state)
            route.setValue("ok");
        else
            route.removeValue();
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
