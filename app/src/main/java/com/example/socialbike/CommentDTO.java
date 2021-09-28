package com.example.socialbike;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CommentDTO {
    @JsonProperty("posts")
    private List<Comment> comments = null;

    public CommentDTO() {
    }

    public CommentDTO(List<Comment> posts) {
        super();
        this.comments = posts;
    }

    @JsonProperty("posts")
    public List<Comment> getComments() {
        return comments;
    }

    @JsonProperty("posts")
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
