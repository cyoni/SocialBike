package com.example.socialbike.post;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Comment extends Post {
    ArrayList<SubComment> subComments = new ArrayList<>();
    @JsonProperty("commentId")
    public String commentKey;

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

    @JsonProperty("commentId")
    public String getCommentKey(){
        return commentKey;
    }

    @JsonProperty("commentId")
    public void SetCommentKey(String commentKey){
        this.commentKey = commentKey;
    }

    public Comment(){ }

    public void addSubComment(SubComment subComment) {
        subComments.add(0, subComment);
    }

    public void setCommentKey(String commentKey){
        this.commentKey = commentKey;
    }

    public ArrayList<SubComment> getSubComments() {
        return subComments;
    }

}
