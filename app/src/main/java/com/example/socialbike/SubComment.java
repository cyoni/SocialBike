package com.example.socialbike;

import java.util.ArrayList;

public class SubComment extends Comment {
    String subCommentId;

    public SubComment(String container,
                      String postId,
                      String headCommentId,
                      String subCommentId,
                      String publicKey,
                      String name,
                      long time,
                      String msg) {
        super(container, headCommentId, postId, publicKey, name, time, msg);
        this.subCommentId = subCommentId;
    }

    public String getSubCommentId(){
        return subCommentId;
    }

    @Override
    public ArrayList<SubComment> getSubComments(){
        return null;
    }

    public void setam(){
        // setam comment
    }
}
