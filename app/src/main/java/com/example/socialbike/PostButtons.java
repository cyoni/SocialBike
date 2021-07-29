package com.example.socialbike;

import java.util.ArrayList;

public class PostButtons {

    public void likeButtonClick(ArrayList<Post> container, RecyclerViewAdapter.ViewHolder holder, int position){
        Post post = container.get(position);
        if (post.getIsLiked()) {
            post.setIsLiked(false);
            registerLike(post, false);
            post.decrementLike();
        } else {
            container.get(position).setIsLiked(true);
            registerLike(post, true);
            post.incrementLike();
        }
        changeLikeButton(holder, post.getIsLiked());
        holder.likes.setText(""+post.getLikesCount());
    }

    public void changeLikeButton(RecyclerViewAdapter.ViewHolder holder, boolean like){
        if (like)
            holder.likeButton.setImageResource(R.drawable.ic_like_pressed);
        else
            holder.likeButton.setImageResource(R.drawable.ic_like);
    }

    private void registerLike(Post post, boolean state) {
        if (state)
            MainActivity.mDatabase.child("global_posts").child(post.getPostId()).child("likes").child(ConnectedUser.getPublicKey()).setValue("ok");
        else
            MainActivity.mDatabase.child("global_posts").child(post.getPostId()).child("likes").child(ConnectedUser.getPublicKey()).removeValue();
    }


    public void followUser(ArrayList<Post> container, RecyclerViewAdapter.ViewHolder holder, int position) {
        System.out.println("following ....");
    }
}
