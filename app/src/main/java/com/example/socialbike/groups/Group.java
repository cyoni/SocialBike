package com.example.socialbike.groups;

import android.app.Activity;

import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.utilities.EMethods;
import com.example.socialbike.utilities.Utils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

interface IGroup{
    Task<HttpsCallableResult> joinGroup(Activity activity);
    Task<HttpsCallableResult> exitGroup(Activity activity);
}

public class Group implements IGroup {

    private String groupId;
    private String title;
    private String description;
    private int memberCount;
    private boolean isMember;

    public Group(){} // do not remove!

    public Group(String groupId, String title, String description) {
        this.groupId = groupId;
        this.title = title;
        this.description = description;
    }

    public boolean getIsMember(){
        return isMember;
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

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    @Override
    public Task<HttpsCallableResult> joinGroup(Activity activity) {
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        return MainActivity.mFunctions
                .getHttpsCallable("JoinGroup")
                .call(data).continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    Utils.savePreference(activity, "connected_groups", groupId, "ok");
                    return null;
                });
    }

    @Override
    public Task<HttpsCallableResult> exitGroup(Activity activity) {
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        return MainActivity.mFunctions
                .getHttpsCallable(EMethods.LeaveGroup.name())
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    Utils.removePreference(activity, "connected_groups", groupId);
                    return null;
                });
    }
}



