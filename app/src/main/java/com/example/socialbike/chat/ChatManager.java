package com.example.socialbike.chat;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.socialbike.Date;
import com.example.socialbike.MainActivity;
import com.example.socialbike.ConnectedUser;
import com.example.socialbike.chat.history.History;
import com.example.socialbike.chat.history.HistoryDao;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatManager {

    public ConversationChatActivity currentConversationChat;
    public ChatLobbyFragment chatLobbyFragment;
    private HistoryDao historyDao;
    public PreviewChatMessageDao memberDao;


    public void listenForNewMessages() {
        System.out.println("Chat Manager has started");
        initDatabases();

        MainActivity.mDatabase.child("private_msgs").child(ConnectedUser.getPublicKey()).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    System.out.println("raw data: " + snapshot.toString());
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        String senderPublicKey = String.valueOf(postSnapshot.child("senderPublicKey").getValue());
                        String message = String.valueOf(postSnapshot.child("message").getValue());
                        String sendersName = String.valueOf(postSnapshot.child("sendersName").getValue());
                        long timestamp = Long.parseLong(String.valueOf(postSnapshot.child("timestamp").getValue()));
                        String messageId = snapshot.getKey();
                        System.out.println("New message: " + postSnapshot.child("message").getValue());
                        System.out.println("Message key: " + postSnapshot.getKey());
                        handleNewMessage(messageId, senderPublicKey, sendersName, message, true, timestamp);
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

    private void initDatabases() {
        historyDao = MainActivity.database.historyDao();
        memberDao = MainActivity.database.chatMemberDao();
    }

    private void removeMessage(String messageId) {
        MainActivity.mDatabase
                .child("private_msgs")
                .child(ConnectedUser.getPublicKey())
                .child(messageId).removeValue();
    }

    public boolean doesUserAppearOnTheList(String userPublicKey) {
        ArrayList<PreviewChatMessage> container = chatLobbyFragment.users;
        return container.stream().anyMatch(x -> x.publicKey.equals(userPublicKey));
    }


/*    public ChatPreviewUser getUserFromList() {
        ArrayList<ChatMember> container = chatLobbyFragment.users;
        return container.stream().filter(x -> x.publicKey.equals(ConnectedUser.getPublicKey())).findAny().orElse(null);
    }*/


    private void handleNewMessage(String messageId, String otherSidePublicKey, String sendersName, String message, boolean isIncomingMessage, long time) {
        ChatMessage chatMessage = new ChatMessage(messageId, otherSidePublicKey, sendersName, message, isIncomingMessage, time);

        if (chatLobbyFragment != null) {
            ArrayList<PreviewChatMessage> usersList = chatLobbyFragment.users;
            if (isUserOnTopOfTheList(otherSidePublicKey, usersList)) {
                updateTopElement(message);
            } else if (doesUserAppearOnTheList(otherSidePublicKey)) {
                moveElementOnTopOfTheList(otherSidePublicKey, message, time);
            } else
                insertNewElement(chatMessage);
            chatLobbyFragment.users.get(0).time = time; // ??

            if (isConversationActivityOpen() && isConversationWith(otherSidePublicKey)) {
                memberDao.resetUnreadMessages(chatMessage.senderPublicKey);
                usersList.get(0).unreadMessages = 0;
                chatLobbyFragment.recyclerViewAdapter.notifyItemChanged(0);
            }
        }

        // add new msg to chat history
        recordMessageInHistory(chatMessage);

/*        if (isConversationActivityOpen() && isConversationWith(otherSidePublicKey)) {


            //   MainActivity.chatManager.currentConversationChat.addNewMessage(chatMessage);
            System.out.println("msg passed");
        } else {
            System.out.println("currentConversationChat is closed");
*//*
            int item = bottomNavigationView.getMenu().getItem(2).getItemId();
            BadgeDrawable xx = bottomNavigationView.getOrCreateBadge(item);
            xx.setNumber(3);*//*

        }*/
    }

    private void recordMessageInHistory(ChatMessage chatMessage) {
        History history = new History(
                chatMessage.getSenderPublicKey(),
                chatMessage.getMessage(),
                chatMessage.getTime(),
                chatMessage.isIncomingMessage());
        historyDao.insert(history);
        System.out.println("Message was recorded in db successfully. " + chatMessage.getMessage());
    }

    private boolean isConversationWith(String userId) {
        return MainActivity.chatManager.currentConversationChat.getUserId().equals(userId);
    }

    private boolean isConversationActivityOpen() {
        return MainActivity.chatManager.currentConversationChat != null;
    }

    private boolean isUserOnTopOfTheList(String senderPublicKey,
                                         ArrayList<PreviewChatMessage> usersList) {
        return doesUserAppearOnTheList(senderPublicKey) &&
                usersList.get(0).publicKey.equals(senderPublicKey);
    }

    private void updatePreviewMessage(int index, String message) {
        chatLobbyFragment.users.get(index).previewMsg = message;
    }

    private void updateTopElement(String message) {
        updatePreviewMessage(0, message);
        incrementUnreadMessages(chatLobbyFragment.users.get(0));
        chatLobbyFragment.users.get(0).unreadMessages += 1;
        chatLobbyFragment.recyclerViewAdapter.notifyItemChanged(0);
    }

    private void moveElementOnTopOfTheList(String senderPublicKey, String message, long time) {
        ArrayList<PreviewChatMessage> usersList = chatLobbyFragment.users;
        int index = getIndexOfUserOnTheList(senderPublicKey);
        updatePreviewMessage(index, message);
        PreviewChatMessage tmp = usersList.get(index);
        usersList.remove(index);
        usersList.add(0, tmp);
        chatLobbyFragment.recyclerViewAdapter.notifyItemMoved(0, index);
        incrementUnreadMessages(usersList.get(0));
        usersList.get(0).unreadMessages += 1;
        chatLobbyFragment.recyclerViewAdapter
                .notifyItemRangeChanged(0, usersList.size());
    }

    private void insertNewElement(ChatMessage chatMessage) {
        ArrayList<PreviewChatMessage> users = chatLobbyFragment.users;
        PreviewChatMessage chatMember = new PreviewChatMessage(
                chatMessage.getSenderPublicKey(),
                chatMessage.getSendersName(),
                chatMessage.getMessage(), chatMessage.getTime(),
                1);
        chatLobbyFragment.insert(chatMember);
        users.add(0, chatMember);
        chatLobbyFragment.recyclerViewAdapter.notifyItemInserted(0);
        chatLobbyFragment.recyclerViewAdapter.notifyItemRangeChanged(0, users.size() );

        System.out.println("A new element was inserted on the chat list.");
    }

    private int getIndexOfUserOnTheList(String senderPublicKey) {
        for (int i = 0; i < chatLobbyFragment.users.size(); i++) {
            if (chatLobbyFragment.users.get(i).publicKey.equals(senderPublicKey))
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
        handleNewMessage("123456", receiver, ConnectedUser.getName(), message, false, Date.getTimeInMiliSecs());

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

    public void incrementUnreadMessages(PreviewChatMessage chatMember) {
        memberDao.incrementUnreadMessages(chatMember.publicKey);
    }

}
