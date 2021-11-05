package com.example.socialbike;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MessageGetter {

    private final Updater updater;

    public MessageGetter(Updater updater){
        this.updater = updater;
    }

    public void parseMessages(String response){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            PostDTO postDTO = objectMapper.readValue(response, PostDTO.class);
            updater.container.addAll(postDTO.getPosts());
            updater.referenceClass.onFinishedUpdating();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getPosts(@NonNull String groupId, String eventId){
        System.out.println("getting posts...");

        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        if (eventId != null)
            data.put("eventId", eventId);

       Utils.PostData(EMethods.getPosts, data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response: " + response);
                    if (!response.isEmpty())
                        parseMessages(response);
                    return "";
                });
    }

}
