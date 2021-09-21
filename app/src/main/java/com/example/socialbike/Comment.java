package com.example.socialbike;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class Comment extends Post {
    ArrayList<SubComment> subComments = new ArrayList<>();
    @JsonProperty("commentId")
    String commentKey;

    public Comment(
                   String postId,
                   String commentKey,
                   String publicKey,
                   String name,
                   long time,
                   String msg) {
        super(postId, publicKey, name, time, msg, 0, 0, false);
        this.commentKey = commentKey;
    }

    public String getCommentKey(){
        return commentKey;
    }

    public Comment(){ }

    public void addSubComment(SubComment subComment) {
        subComments.add(0, subComment);
    }

    public ArrayList<SubComment> getSubComments() {
        return subComments;
    }

    protected void registerLike(boolean state) {
        setIsLiked(state);
        DatabaseReference ref = MainActivity.mDatabase.
                child("global_posts").
                child(ConnectedUser.getPublicKey());
        if (state)
            ref.setValue("ok");
        else
            ref.removeValue();
    }
}
