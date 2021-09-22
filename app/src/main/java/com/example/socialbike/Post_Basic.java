package com.example.socialbike;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;

public class Post_Basic implements Serializable {

    private String publicKey, name, msg;
    @JsonProperty("doesUserLikeThePost")
    private boolean isLiked = false;
    private long timestamp;
    private int comments_count;
    public String DatabaseContainer = "";
    protected final ArrayList<Comment> commentsContainer = new ArrayList<>();
    @JsonProperty("likes_count")
    private int likesCount;
    public Post_Basic(){}


    public Post_Basic(
            String publicKey,
                String name,
                long time,
                String msg,
                int likes,
                int comments,
                boolean doesUserLikeThePost) {

        this.publicKey = publicKey;
        this.name = name;
        this.timestamp = time;
        this.msg = msg;
        this.likesCount = likes;
        this.comments_count = comments;
        this.isLiked = doesUserLikeThePost;
    }

    @JsonProperty("publicKey")
    public String getPublicKey() {
        return publicKey;
    }

    @JsonProperty("publicKey")
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("message")
    public String getMessage() {
        return msg;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.msg = message;
    }

    @JsonProperty("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("comments_count")
    public int getCommentCount() {
        return comments_count;
    }

    @JsonProperty("comments_count")
    public void setCommentCount(int commentCount) {
        this.comments_count = commentCount;
    }

    @JsonProperty("likes_count")
    public int getLikesCount() {
        return likesCount;
    }

    @JsonProperty("likes_count")
    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public String getMsg() {
        return msg;
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

    public void incrementLike() {
        this.likesCount++;
    }

    public void decrementLike() {
        this.likesCount--;
    }



}
