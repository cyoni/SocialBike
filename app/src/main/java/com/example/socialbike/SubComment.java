package com.example.socialbike;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class SubComment extends Comment {

    String subCommentId;

    public SubComment(
            String postId,
                      String commentKey,
                      String subCommentId,
                      String publicKey,
                      String name,
                      long time,
                      String msg) {
        super(postId, commentKey, publicKey, name, time, msg);
        this.subCommentId = subCommentId;
    }

    public SubComment(){
    }

    public String getSubCommentId(){
        return subCommentId;
    }

    @Override
    public ArrayList<SubComment> getSubComments(){
        return null;
    }

    public void setSubCommentId(String subCommentId) {
        this.subCommentId = subCommentId;
    }
}
