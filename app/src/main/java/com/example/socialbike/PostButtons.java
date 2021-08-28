package com.example.socialbike;

import android.app.Activity;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class PostButtons {

    ImageButton likeButton;
    TextView likes, comments;
    Post post;

    public PostButtons(Activity activity, Post post){
        this.post = post;
        likeButton = activity.findViewById(R.id.likeButton);
        likes = activity.findViewById(R.id.likes);
        comments = activity.findViewById(R.id.comments);
        likeButton.setOnClickListener(view -> likeButtonClick());
        initText();
    }

    private void initText() {
        setLikeButton();
        likes.setText(String.valueOf(post.getLikesCount()));
        comments.setText(String.valueOf(post.getCommentsCount()));
    }

    public PostButtons(RecyclerViewAdapter.ViewHolder holder, int position, Post post) {
        likeButton = holder.likeButton;
        likes = holder.likes;
        comments = holder.comments_count;
        likeButton.setOnClickListener(view -> likeButtonClick());
        this.post = post;
        initText();
    }

    public void likeButtonClick(){
        if (post.getIsLiked()) {
            post.setIsLiked(false);
            registerLike(post, false);
            post.decrementLike();
        } else {
            post.setIsLiked(true);
            registerLike(post,true);
            post.incrementLike();
        }
        setLikeButton();
        likes.setText(""+post.getLikesCount());
    }

    public void setLikeButton(){
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
}
