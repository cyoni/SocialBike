package com.example.socialbike.model;

import com.example.socialbike.post.Post;

import java.util.List;

public class PostDTO {

    private List<Post> posts = null;

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

}
