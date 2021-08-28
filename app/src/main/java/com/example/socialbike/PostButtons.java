package com.example.socialbike;

import android.app.Activity;
import android.content.Intent;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

import java.util.ArrayList;

public class PostButtons {

    ImageButton likeButton, commentsButton;
    TextView likes, comments;
    Post post;
    Activity activity;

    public PostButtons(Activity activity, Post post) {
        this.activity = activity;
        this.post = post;
        likes = activity.findViewById(R.id.likes);
        comments = activity.findViewById(R.id.comments);
        likeButton = activity.findViewById(R.id.likeButton);
        commentsButton = activity.findViewById(R.id.commentsButton);
        init();
    }

    public PostButtons(Activity activity, RecyclerViewAdapter.ViewHolder holder, Post post) {
        this.activity = activity;
        commentsButton = holder.commentsButton;
        likeButton = holder.likeButton;
        likes = holder.likes;
        comments = holder.comments_count;
        this.post = post;
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
        if (state)
            MainActivity.mDatabase.child("global_posts").child(post.getPostId()).child("likes").child(ConnectedUser.getPublicKey()).setValue("ok");
        else
            MainActivity.mDatabase.child("global_posts").child(post.getPostId()).child("likes").child(ConnectedUser.getPublicKey()).removeValue();
    }

    public void followUser(ArrayList<Post> container, RecyclerViewAdapter.ViewHolder holder, int position) {
        System.out.println("todo");
    }

    public void openPost() {
        Intent intent = new Intent(activity.getApplicationContext(), PostActivity.class);
        intent.putExtra("post", post);
        activity.startActivity(intent);
    }
}
