package com.example.socialbike;

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.socialbike.Event.EVENTS_CONTAINER_CODE;

public class Comment extends Post {
    private final String commentHeadId;
    private final ArrayList<String> subComments = new ArrayList<>();

    public Comment(String container, String commentHeadId, String commentId, String publicKey, String name, int time, String msg) {
        super(commentId, publicKey, name, time, msg, 0);
        this.commentHeadId = commentHeadId;
        this.container = container;
    }


    public void addSubComment(String subComment){
        subComments.add(0, subComment);
    }

    public ArrayList<String> getSubComments(){
        return subComments;
    }

    public Task<HttpsCallableResult> sendSubComment(String message){
        System.out.println("sending sub-comment. " + message + ". CommentId: " + getPostId() + ", " + container + ", " + commentHeadId);
        Map<String, Object> data = new HashMap<>();
        data.put("comment", message);
        data.put("postId", commentHeadId);
        data.put("replyTo", getPostId());
        data.put("container", container);
        return MainActivity.mFunctions
                .getHttpsCallable("sendComment")
                .call(data);
    }

}
