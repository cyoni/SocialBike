package com.example.socialbike.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.socialbike.ConversationChat;
import com.example.socialbike.MainActivity;
import com.example.socialbike.ConnectedUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatManager {

    public ConversationChat currentConversationChat;
    public ChatLobbyFragment chatLobbyFragment;
    boolean isChatEnabled = true;
    protected HashMap<String, ConversationChat> screens = new HashMap<>();


    public ChatManager() {}

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
                        //removeMessage(message); TODO
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

    public boolean doesUserAppearInList(String userPublicKey) {
        ArrayList<ChatPreviewUser> container = chatLobbyFragment.getUsersList();
        return container.stream().anyMatch(x -> x.senderPublicKey.equals(userPublicKey));
    }

    public ChatPreviewUser getUserFromList() {
        ArrayList<ChatPreviewUser> container = chatLobbyFragment.getUsersList();
        return container.stream().filter(x -> x.senderPublicKey.equals(ConnectedUser.getPublicKey())).findAny().orElse(null);
    }

    private void handleNewMessage(String messageId, String senderPublicKey, String sendersName, String message, boolean isIncomingMessage) {
        if (chatLobbyFragment != null) {
            ArrayList<ChatPreviewUser> usersList = chatLobbyFragment.getUsersList();
            if (isUserOnTopOfTheList(senderPublicKey, usersList)) {
                updateTopElement(message);
            } else if (doesUserAppearInList(senderPublicKey)) {
                moveElementToTheTopOfTheList(senderPublicKey, message);
            } else {
                addNewMessageWhenElementNotInTheList
                        (messageId, senderPublicKey, sendersName, message);
            }
        }


        if (MainActivity.chatManager.currentConversationChat != null){
            ChatMessage chatMessage = new ChatMessage(messageId, senderPublicKey, sendersName, message, isIncomingMessage);
            MainActivity.chatManager.currentConversationChat.addNewMessage(chatMessage);
            System.out.println("passed msg");
        }
        else{
            System.out.println("currentConversationChat is null");
        }
    }

    private boolean isUserOnTopOfTheList(String senderPublicKey, ArrayList<ChatPreviewUser> usersList) {
        return doesUserAppearInList(senderPublicKey) &&
                usersList.get(0).getPublicKey().equals(senderPublicKey);
    }

    private void updateTopElement(String message) {
        chatLobbyFragment.getUsersList().get(0).setMessage(message);
        chatLobbyFragment.recyclerViewAdapter.notifyItemChanged(0);
    }

    private void moveElementToTheTopOfTheList(String senderPublicKey, String message) {
        ArrayList<ChatPreviewUser> usersList = chatLobbyFragment.getUsersList();
        int index = getIndexOfUserOnTheList(senderPublicKey);
        usersList.get(index).setMessage(message);
        chatLobbyFragment.recyclerViewAdapter.notifyItemChanged(index);
        ChatPreviewUser tmp = usersList.get(index);
        usersList.remove(index);
        usersList.add(0, tmp);
        chatLobbyFragment.recyclerViewAdapter.notifyItemMoved(0, index);
    }

    private void addNewMessageWhenElementNotInTheList(String messageId, String senderPublicKey, String sendersName, String message) {
        ChatPreviewUser chatMsgPreview = new ChatPreviewUser(messageId, senderPublicKey, sendersName, message);
        //ChatLobbyFragment.getInstance().addNewIncomeMessage(senderPublicKey, chatMsgPreview);
        chatLobbyFragment.getUsersList().add(0, chatMsgPreview);
        chatLobbyFragment.recyclerViewAdapter.notifyItemInserted(0);
        System.out.println("A new message was added to the data structure in Chat Lobby.");
    }

    private int getIndexOfUserOnTheList(String senderPublicKey) {
        for (int i = 0; i < chatLobbyFragment.getUsersList().size(); i++) {
            if (chatLobbyFragment.getUsersList().get(i).getPublicKey().equals(senderPublicKey))
                return i;
        }
        return -1;
    }


    private boolean isConversationActivityOpened() {
        return currentConversationChat != null;
    }


    public void sendMessage(String receiver, String message) {

        Map<String, Object> data = new HashMap<>();
        data.put("receiver", "-MYVCkWexSO_jumnbr0l");
        data.put("publicKey", ConnectedUser.getPublicKey());
        data.put("message", message);

        System.out.println("sending private msg to " + receiver + "...");
        handleNewMessage("123456", ConnectedUser.getPublicKey(), ConnectedUser.getName(), message, false);

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
