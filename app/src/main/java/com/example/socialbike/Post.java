package com.example.socialbike;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Post extends Post_Basic {

    private String postId;
    private String publicKey;

    public Post(String postId,
                String publicKey,
                String name,
                long time,
                String msg,
                int likes,
                int comments,
                boolean doesUserLikeThePost) {
        super(publicKey, name, time, msg, likes, comments, doesUserLikeThePost);

        this.postId = postId;

    }

    public Post() {}

    @JsonProperty("postId")
    public String getPostId() {
        return postId;
    }

    @JsonProperty("postId")
    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPublicKey() {
        return publicKey;
    }


}
