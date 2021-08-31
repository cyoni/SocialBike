package com.example.socialbike.chat;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.socialbike.MainActivity;
import com.example.socialbike.ConnectedUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatManager {

    public ConversationChatActivity currentConversationChat;
    public ChatLobbyFragment chatLobbyFragment;
    boolean isChatEnabled = true;
    protected HashMap<String, ConversationChatActivity> screens = new HashMap<>(); // useless


    public void listenForNewMessages() {
        System.out.println("Chat has started");
        MainActivity.mDatabase.child("private_msgs").child(ConnectedUser.getPublicKey()).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    System.out.println("raw data: " + snapshot.toString());
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        String senderPublicKey = String.valueOf(postSnapshot.child("senderPublicKey").getValue());
                        String message = String.valueOf(postSnapshot.child("message").getValue());
                        String sendersName = String.valueOf(postSnapshot.child("sendersName").toString());
                        String messageId = snapshot.getKey();
                        System.out.println("New message: " + postSnapshot.child("message").getValue());
                        System.out.println("Message key: " + postSnapshot.getKey());
                        handleNewMessage(messageId, senderPublicKey, sendersName, message, true);
                        removeMessage(messageId);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("ERROR CONNECT " + databaseError.getDetails());
            }
        });
    }

    private void removeMessage(String messageId) {
        MainActivity.mDatabase
                .child("private_msgs")
                .child(ConnectedUser.getPublicKey())
                .child(messageId).removeValue();
    }

    public boolean doesUserAppearOnTheList(String userPublicKey) {
        ArrayList<ChatPreviewUser> container = chatLobbyFragment.getUsers();
        return container.stream().anyMatch(x -> x.senderPublicKey.equals(userPublicKey));
    }

    public ChatPreviewUser getUserFromList() {
        ArrayList<ChatPreviewUser> container = chatLobbyFragment.getUsers();
        return container.stream().filter(x -> x.senderPublicKey.equals(ConnectedUser.getPublicKey())).findAny().orElse(null);
    }


    private void handleNewMessage(String messageId, String otherSidePublicKey, String sendersName, String message, boolean isIncomingMessage) {
        ChatMessage chatMessage = new ChatMessage(messageId, otherSidePublicKey, sendersName, message, isIncomingMessage);

        if (chatLobbyFragment != null) {
            ArrayList<ChatPreviewUser> usersList = chatLobbyFragment.getUsers();
            if (isUserOnTopOfTheList(otherSidePublicKey, usersList)) {
                updateTopElement(message);
            } else if (doesUserAppearOnTheList(otherSidePublicKey)) {
                moveElementOnTopOfTheList(otherSidePublicKey, message);
            } else
                insertNewElement(chatMessage);
        }

        // add new msg to chat history
        HashMap<String, List<String>> chatHistory;

        if (isConversationActivityOpen() && isConversationWith(otherSidePublicKey)){
            chatLobbyFragment.getUsers().get(0).setRead(true);
            MainActivity.chatManager.currentConversationChat.addNewMessage(chatMessage);
            System.out.println("msg passed");
        }
        else{
            System.out.println("currentConversationChat is closed");
            chatLobbyFragment.getUsers().get(0).setRead(false);
/*
            int item = bottomNavigationView.getMenu().getItem(2).getItemId();
            BadgeDrawable xx = bottomNavigationView.getOrCreateBadge(item);
            xx.setNumber(3);*/

        }
    }

    private boolean isConversationWith(String userId) {
        return MainActivity.chatManager.currentConversationChat.getUserId().equals(userId);
    }

    private boolean isConversationActivityOpen() {
        return  MainActivity.chatManager.currentConversationChat != null;
    }

    private boolean isUserOnTopOfTheList(String senderPublicKey, ArrayList<ChatPreviewUser> usersList) {
        return doesUserAppearOnTheList(senderPublicKey) &&
                usersList.get(0).getPublicKey().equals(senderPublicKey);
    }

    private void updateTopElement(String message) {
        chatLobbyFragment.getUsers().get(0).setMessage(message);
        chatLobbyFragment.recyclerViewAdapter.notifyItemChanged(0);
    }

    private void moveElementOnTopOfTheList(String senderPublicKey, String message) {
        ArrayList<ChatPreviewUser> usersList = chatLobbyFragment.getUsers();
        int index = getIndexOfUserOnTheList(senderPublicKey);
        usersList.get(index).setMessage(message);
        ChatPreviewUser tmp = usersList.get(index);
        usersList.remove(index);
        usersList.add(0, tmp);

        chatLobbyFragment.recyclerViewAdapter.notifyItemMoved(0, index);
        chatLobbyFragment.recyclerViewAdapter.notifyItemRangeChanged(0,usersList.size());
    }

    private void insertNewElement(ChatMessage chatMessage) {
        ChatPreviewUser chatMsgPreview = new ChatPreviewUser(chatMessage.messageId,
                        chatMessage.getSenderPublicKey(),
                        chatMessage.getSendersName(),
                        chatMessage.getMessage());
        chatLobbyFragment.getUsers().add(0, chatMsgPreview);
        chatLobbyFragment.recyclerViewAdapter.notifyItemInserted(0);
        System.out.println("A new element was inserted on the chat list.");
    }

    private int getIndexOfUserOnTheList(String senderPublicKey) {
        for (int i = 0; i < chatLobbyFragment.getUsers().size(); i++) {
            if (chatLobbyFragment.getUsers().get(i).getPublicKey().equals(senderPublicKey))
                return i;
        }
        return -1;
    }

    public void sendMessage(String receiver, String message) {

        Map<String, Object> data = new HashMap<>();
        data.put("receiver", receiver);
        data.put("publicKey", ConnectedUser.getPublicKey());
        data.put("message", message);

        System.out.println("sending private msg to " + receiver + "...");
        handleNewMessage("123456", receiver, ConnectedUser.getName(), message, false);

        MainActivity.mFunctions
                .getHttpsCallable("sendPrivateMsg")
                .call(data)
                .continueWith(task -> {
                    String response = String.valueOf(task.getResult().getData());
                    System.out.println("response: " + response);
                    if (response.contains("ERR"))
                        handleErrors(response);
                    return "";
                });
    }

    private void handleErrors(String response) {
        String error = "Error.";
        if (response.contains("NO_USER"))
            error = "Error: Message was not sent. User does not exist.";
        MainActivity.toast(currentConversationChat.getApplicationContext(), error, 1);
    }

    public void openConversationActivity(Context context, String userId, String name) {
        Intent intent = new Intent(context, ConversationChatActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("name", name);
        context.startActivity(intent);
    }
}
