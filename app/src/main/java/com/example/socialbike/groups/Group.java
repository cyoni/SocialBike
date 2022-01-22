package com.example.socialbike.groups;

import android.app.Activity;

import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.utilities.EMethods;
import com.example.socialbike.utilities.Utils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;


public class Group {

    protected String groupId;
    protected String ownerOfGroup;
    protected String title;
    protected String description;
    protected int memberCount;
    protected boolean isMember;
    protected double lat, lng;

    public Group(){} // do not remove!

    public Group(String groupId, String title, String description, String ownerOfGroup) {
        this.groupId = groupId;
        this.title = title;
        this.description = description;
        this.ownerOfGroup = ownerOfGroup;
    }

    public boolean getIsMember(){
        return isMember;
    }
    public String getOwnerOfGroup(){
        return ownerOfGroup;
    }


    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }


    public void setIsMember(boolean isMember) {
        this.isMember = isMember;
    }
}



