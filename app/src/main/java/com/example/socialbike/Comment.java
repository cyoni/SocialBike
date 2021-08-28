package com.example.socialbike;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Comment extends Post {
    private final String commentHeadId;
    private final ArrayList<SubComment> subComments = new ArrayList<>();

    public Comment(String container,
                   String commentHeadId,
                   String commentId,
                   String publicKey,
                   String name,
                   long time,
                   String msg) {
        super(commentId, publicKey, name, time, msg, 0, 0, false);
        // super(container, postId, commentId )
        this.commentHeadId = commentHeadId;
        this.DatabaseContainer = container;
    }

    public void addSubComment(SubComment subComment) {
        subComments.add(0, subComment);
    }

    public ArrayList<SubComment> getSubComments() {
        return subComments;
    }


    public Task<HttpsCallableResult> sendSubComment(String message) {
        System.out.println("sending comment. " + message + ". CommentId: " + getPostId() + ", " + DatabaseContainer + ", " + commentHeadId);
        Map<String, Object> data = new HashMap<>();
        data.put("comment", message);
        data.put("postId", commentHeadId);
        data.put("replyTo", getPostId());
        data.put("container", DatabaseContainer);
        return MainActivity.mFunctions
                .getHttpsCallable("sendComment")
                .call(data);
    }

    protected void registerLike(boolean state) {
        setIsLiked(state);
        DatabaseReference ref = MainActivity.mDatabase.
                child("global_posts").
                child(commentHeadId).
                child("comments").
                child(getPostId()).
                child("likes").
                child(ConnectedUser.getPublicKey());
        if (state)
            ref.setValue("ok");
        else
            ref.removeValue();
    }

    public String getCommentId() {
        return commentHeadId;
    }
}
