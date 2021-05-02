package com.example.socialbike.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.socialbike.MainActivity;
import com.example.socialbike.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatManager {

    public ChatConversationFragment chatConversationFragment;
    public ChatLobbyFragment chatLobbyFragment;
    HashMap<String, List<ChatMsgPreview>> incomingMessages;
    boolean isChatEnabled = true;

    public ChatManager(){
        incomingMessages = new HashMap<>();
    }

    public void listenForNewMessages(){
        System.out.println("Chat has started");
        MainActivity.mDatabase.child("private_msgs").child(User.getPublicKey()).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    System.out.println("raw data: " + snapshot.toString());
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                        String senderPublicKey = String.valueOf(postSnapshot.child("senderPublicKey").toString());
                        String message = String.valueOf(postSnapshot.child("message").getValue());
                        String sendersName = String.valueOf(postSnapshot.child("sendersName").toString());
                        String messageId = snapshot.getKey();
                        System.out.println("New message: " + postSnapshot.child("message").getValue());
                        System.out.println("Message key: " + postSnapshot.getKey());
                        passMessageToRelevantClass(messageId, senderPublicKey, sendersName, message, true);
                        //removeMessage(message); TODO
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("ERROR CONNECT " + databaseError.getDetails());
            }
        });

    }

    private void passMessageToRelevantClass(String messageId, String senderPublicKey, String sendersName, String message, boolean isIncomingMessage) {
        ChatMessage chatMessage = new ChatMessage(messageId, senderPublicKey, sendersName, message, isIncomingMessage);
        //ChatConversationFragment.getInstance().addMessage(chatMessage);

        if (chatConversationFragmentDisplayed())
            chatConversationFragment.addMessage(chatMessage);
        else if (chatLobbyFragmentDisplayed()){
            chatLobbyFragment.updateLobbyList(chatMessage);
        } else {

            if (incomingMessages.get(senderPublicKey) == null) {
                incomingMessages.put(senderPublicKey, new ArrayList<>());
            }
            incomingMessages.get(senderPublicKey).add(new ChatMsgPreview(messageId, senderPublicKey, sendersName, message);
            //chatLobbyFragment.updateLobbyList(chatMessage);

            System.out.println("A new message was added to the data structure in Chat Lobby.");
        }

    }

    private boolean chatLobbyFragmentDisplayed() {
        return chatLobbyFragment != null;
    }

    private boolean chatConversationFragmentDisplayed() {
        return chatConversationFragment != null;
    }


    public void sendMessage(String receiver, String message){
        Map<String, Object> data = new HashMap<>();
        data.put("receiver", "-MYVCkWexSO_jumnbr0l");
        data.put("publicKey", User.getPublicKey());
        data.put("message", message);

        System.out.println("sending private msg to "+ receiver +"...");
        passMessageToRelevantClass("123456", User.getPublicKey(), User.getNickname(), message, false);

        MainActivity.mFunctions
                .getHttpsCallable("sendPrivateMsg")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response: " + response);
                    return "";
                });
    }

}
