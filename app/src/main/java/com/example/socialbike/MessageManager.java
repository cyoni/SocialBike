package com.example.socialbike;

import org.json.JSONArray;
import org.json.JSONObject;

public class MessageManager {


    public void parseMessages(String fresh_msgs){

        String tmp_category;
        try {

            JSONObject obj = new JSONObject(fresh_msgs);
            JSONArray messages_array = obj.getJSONArray("posts");

/*            if (obj.has("upNext")) {
                upNext = obj.getString("upNext");
            }
            else
                upNext = DEFAULT_END_OF_LIST;*/

            for (int i = 0; i < messages_array.length(); i++) {
                int likes = 0, comments = 0;
                boolean doILike = false, isAuthor = false, has_profile_img = false;

                String user_public_key = messages_array.getJSONObject(i).getString("user_public_key");
                String messages = messages_array.getJSONObject(i).getString("message");
                String nickname = messages_array.getJSONObject(i).getString("nickname");
                String time = messages_array.getJSONObject(i).getString("timestamp");
                String messageId = messages_array.getJSONObject(i).getString("postId");

                if (messages_array.getJSONObject(i).has("likes_count")) {
                    likes = Integer.parseInt(messages_array.getJSONObject(i).getString("likes_count"));
                }
                if (messages_array.getJSONObject(i).has("comments_count")) {
                    comments = Integer.parseInt(messages_array.getJSONObject(i).getString("comments_count"));
                }
                if (messages_array.getJSONObject(i).has("doILike")) {
                    doILike = messages_array.getJSONObject(i).getBoolean("doILike");
                }
                if (messages_array.getJSONObject(i).has("isauthor")) {
                    isAuthor = messages_array.getJSONObject(i).getBoolean("isauthor");
                }
                if (messages_array.getJSONObject(i).has("has_p_img")) {
                    has_profile_img = messages_array.getJSONObject(i).getBoolean("has_p_img");
                }
                if (messages_array.getJSONObject(i).has("cat")){
                    tmp_category = messages_array.getJSONObject(i).getString("cat");
                } else
                    tmp_category = "category";

            }
        }
        catch(Exception e){
            System.out.println("Error caught in message fetcher: " +e.getMessage());
        }
    }


    public void getPosts(){
        System.out.println("getting posts...");
        MainActivity.mFunctions
                .getHttpsCallable("getPosts")
                .call(null)
                .continueWith(task -> {

                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response:" + response);

                    if (!response.isEmpty()) {



                    } else {
                        // notifyUser_error();

                    }
                    return "";
                });

    }


}
