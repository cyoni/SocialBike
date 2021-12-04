package com.example.socialbike.groups;


import android.app.Activity;
import android.content.Context;

import com.example.socialbike.activities.MainActivity;
import com.example.socialbike.groups.group.GroupDTO;
import com.example.socialbike.utilities.EMethods;
import com.example.socialbike.utilities.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupManager {


    public ArrayList<Group> container;

    Context context;

    public GroupManager(Context context, ArrayList<Group> container){
        this.context = context;
        this.container = container;
    }

    public GroupManager(Context context){
        this.context = context;
    }

    public Task<HttpsCallableResult> getAllGroups(){
        container.clear();

        Map<String, Object> data = new HashMap<>();
        //   data.put("lat", position.getLatLng().latitude);
        //   data.put("lng", position.getLatLng().longitude);

        return Utils.PostData(EMethods.GetAllGroups, data);
    }


    public Task<HttpsCallableResult> getMyConnectedGroups(){

        container.clear();
        Map<String, Object> data = new HashMap<>();

        return Utils.PostData(EMethods.GetMyGroups, data); // todo: update local groups
    }

/*
    public void getPublicGroups(){
        publicGroups.clear();

        Map<String, Object> data = new HashMap<>();

        Utils.PostData(EMethods.GetAllGroups, data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);
                    parseGroups(response);
                    return null;
                });
    }
*/


    public Task<HttpsCallableResult> joinGroup(String groupId) {
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        return MainActivity.mFunctions
                .getHttpsCallable("JoinGroup")
                .call(data);
    }

    public Task<HttpsCallableResult> exitGroup(String groupId) {
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        return MainActivity.mFunctions
                .getHttpsCallable(EMethods.LeaveGroup.name())
                .call(data);
    }

    public void parseGroups(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            GroupDTO groupDTO = objectMapper.readValue(response, GroupDTO.class);
            container.addAll(groupDTO.getGroups());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void add(Group group) {
        group.setIsMember(true);
        MainActivity.MyConnectedGroups.put(group.getGroupId(), group);
    }

    public void remove(Group group) {
        MainActivity.MyConnectedGroups.remove(group.getGroupId());
    }

}
