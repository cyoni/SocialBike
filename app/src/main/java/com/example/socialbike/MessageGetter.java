package com.example.socialbike;

import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MessageGetter {

    private final Updater updater;

    public MessageGetter(Updater updater){
        this.updater = updater;
    }

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
                boolean isAuthor = false, has_profile_img = false;
                boolean doesUserLikeThePost = false;

                String user_public_key = messages_array.getJSONObject(i).getString("publicKey");
                String messages = messages_array.getJSONObject(i).getString("message");
                String nickname = messages_array.getJSONObject(i).getString("name");
                String time = messages_array.getJSONObject(i).getString("timestamp");
                String postId = messages_array.getJSONObject(i).getString("postId");

                if (messages_array.getJSONObject(i).has("likes_count")) {
                    likes = Integer.parseInt(messages_array.getJSONObject(i).getString("likes_count"));
                }
                if (messages_array.getJSONObject(i).has("comments_count")) {
                    comments = Integer.parseInt(messages_array.getJSONObject(i).getString("comments_count"));
                }
                if (messages_array.getJSONObject(i).has("doesUserLikeThePost")) {
                    doesUserLikeThePost = (messages_array.getJSONObject(i).getBoolean("doesUserLikeThePost"));
                }
                if (messages_array.getJSONObject(i).has("isauthor")) {
                    isAuthor = messages_array.getJSONObject(i).getBoolean("isauthor");
                }
                if (messages_array.getJSONObject(i).has("has_p_img")) {
                    has_profile_img = messages_array.getJSONObject(i).getBoolean("has_p_img");
                }

                Post post = new Post(postId, user_public_key,
                        nickname,8888, messages, likes, comments, doesUserLikeThePost);
                updater.add(post);

                System.out.println("msg " + i + " " + messages);
            }
            updater.referenceClass.onFinishedTakingNewMessages();
        }
        catch(Exception e){
            System.out.println("Error caught in message fetcher: " + e.getMessage());
        }

    }

    public void getPosts(String groupId){
        System.out.println("getting posts...");

        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
      //  parseMessages("{\"posts\":[{\"postId\":\"77777\",\"publicKey\":null,\"name\":null,\"message\":null,\"timestamp\":null,\"likes_count\":1,\"doesUserLikeThePost\":false},{\"postId\":\"-MZ3E-YJvZ2Hu_Gjww1m\",\"publicKey\":\"-MYVCkWexSO_jumnbr0l\",\"name\":\"yoni :)\",\"message\":\"HELLO\",\"timestamp\":1619273713159,\"likes_count\":0,\"doesUserLikeThePost\":false},{\"postId\":\"-MZ3EQWQZKF9P-LtPrsp\",\"publicKey\":\"-MYVCkWexSO_jumnbr0l\",\"name\":\"yoni :)\",\"message\":\"Ccc\",\"timestamp\":1619273824422,\"likes_count\":0,\"doesUserLikeThePost\":false},{\"postId\":\"-MdmsTw-fo1qCOc2AubY\",\"publicKey\":\"-MYVCkWexSO_jumnbr0l\",\"name\":\"yoni :)\",\"message\":\"עכאיחכככ\",\"timestamp\":1625425047024,\"likes_count\":1,\"doesUserLikeThePost\":false},{\"postId\":\"-MdmsWZ_z7RpGyoJWQ0H\",\"publicKey\":\"-MYVCkWexSO_jumnbr0l\",\"name\":\"yoni :)\",\"message\":\"Eg\",\"timestamp\":1625425058086,\"likes_count\":1,\"doesUserLikeThePost\":false},{\"postId\":\"-MeRCOAy5tbZthY4Qg0K\",\"publicKey\":\"-MYVCkWexSO_jumnbr0l\",\"name\":\"yoni :)\",\"message\":\"Cccc\",\"timestamp\":1626118394452},{\"postId\":\"-MfJTL86E9m8o0F4TdgA\",\"publicKey\":\"-MYVCkWexSO_jumnbr0l\",\"name\":\"yoni :)\",\"message\":\"Ggnn c\\nHh\\n\\nHh\",\"timestamp\":1627062362515},{\"postId\":\"-MfP81zn8VwvcG9VZIBZ\",\"publicKey\":\"-MYVCkWexSO_jumnbr0l\",\"name\":\"yoni :)\",\"message\":\"Fffh\\nFf\\nSsjgr\\n\\nFff\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\nBbb\\n\\n\\nKk\",\"timestamp\":1627157442348,\"likes_count\":1,\"doesUserLikeThePost\":true},{\"postId\":\"-MfP90-zZ-qXk0D47cBr\",\"publicKey\":\"-MYVCkWexSO_jumnbr0l\",\"name\":\"yoni :)\",\"message\":\"Fffh\\nFf\\nSsjgr\\n\\nFff\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\n\\nBbb\\n\\n\\nKk\",\"timestamp\":1627157696409,\"likes_count\":4,\"doesUserLikeThePost\":true},{\"postId\":\"-MflWeng7w8QQKP1gBzP\",\"publicKey\":\"-MYVCkWexSO_jumnbr0l\",\"name\":\"yoni :)\",\"message\":\"Hello\",\"timestamp\":1627549772870,\"likes_count\":4,\"doesUserLikeThePost\":true},{\"postId\":\"-MgVz6HC-n2Qikb7gvjz\",\"publicKey\":\"-MYVCkWexSO_jumnbr0l\",\"name\":\"yoni :)\",\"message\":\"iiyy\",\"timestamp\":1628346020822}]}");

       MainActivity.mFunctions
                .getHttpsCallable("GetGroupPosts")
                .call(data)
                .continueWith(task -> {

                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response: " + response);

                    if (!response.isEmpty()) {
                        parseMessages(response);
                    }
                    return "";
                });
    }

}
