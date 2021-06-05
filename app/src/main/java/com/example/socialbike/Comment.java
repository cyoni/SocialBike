package com.example.socialbike;

import java.util.ArrayList;

public class Comment extends Post {

    private final ArrayList<String> subComments = new ArrayList<>();

    public Comment(String postId, String publicKey, String name, int time, String msg) {
        super(postId, publicKey, name, time, msg);
    }

    public void addSubComment(String subComment){
        subComments.add( subComment);
    }

    public ArrayList<String> getSubComments(){
        return subComments;
    }

}
