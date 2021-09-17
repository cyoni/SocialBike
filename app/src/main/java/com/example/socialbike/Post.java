package com.example.socialbike;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Post implements Serializable {

    private String postId, publicKey, name, msg;
    private boolean isLiked = false, doesUserLikeThePost = false;
    private long time;
    private int comments_count;
    public static String POSTS_CONTAINER_CODE = "global_posts";
    public String DatabaseContainer = "";
    protected final ArrayList<Comment> commentsContainer = new ArrayList<>();
    private int likes_count;

    public Post(String postId,
                String publicKey,
                String name,
                long time,
                String msg,
                int likes,
                int comments,
                boolean doesUserLikeThePost) {

        this.postId = postId;
        this.publicKey = publicKey;
        this.name = name;
        this.time = time;
        this.msg = msg;
        this.likes_count = likes;
        this.comments_count = comments;
        this.isLiked = doesUserLikeThePost;
    }

    public Post() {

    }

    public String getPostId() {
        return postId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getName() {
        return name;
    }

    public String getMsg() {
        return msg;
    }

    public long getTimestamp() {
        return time;
    }

    public int getCommentsCount() {
        return comments_count;
    }

    public boolean hasComments() {
        return comments_count > 0;
    }

    public void addComment(Comment comment) {
        commentsContainer.add(0, comment);
    }

    public boolean getIsLiked(){
        return isLiked;
    }

    public void setIsLiked(boolean isLiked){
        this.isLiked = isLiked;
    }

    public Task<HttpsCallableResult> getComments() {
        System.out.println("Getting comments for post #" + getPostId());
        Map<String, Object> data = new HashMap<>();
        data.put("postId", getPostId());
        data.put("container", DatabaseContainer);
        return MainActivity.mFunctions
                .getHttpsCallable("getComments")
                .call(data);
    }


    public void incrementLike() {
        this.likes_count++;
    }

    public void decrementLike() {
        this.likes_count--;
    }

    public int getLikesCount() {
        return likes_count;
    }
}
