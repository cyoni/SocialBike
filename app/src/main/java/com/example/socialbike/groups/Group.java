package com.example.socialbike.groups;

public class Group {

    private final String title, description;
    private final String groupId;

    public Group(String groupId, String title, String description){
        this.groupId = groupId;
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getGroupId() {
        return groupId;
    }
}
