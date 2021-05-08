package com.example.socialbike.chat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.socialbike.MainActivity;
import com.example.socialbike.R;
import com.example.socialbike.RecyclerViewAdapter;
import com.example.socialbike.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatLobbyFragment extends Fragment
        implements RecyclerViewAdapter.ItemClickListener {

    static ChatLobbyFragment chatFragment = null;
    private RecyclerView recyclerView;
    private final ArrayList<ChatPreviewUser> container = new ArrayList<>();
    public RecyclerViewAdapter recyclerViewAdapter;
    private NavController nav;
    private HashMap<String, List<ChatPreviewUser>> incomingMessages = new HashMap<>();


    public static ChatLobbyFragment getInstance() {
        if (chatFragment == null) {
            chatFragment = new ChatLobbyFragment();
        }
        return chatFragment;
    }

    public ChatLobbyFragment() {
        System.out.println("ChatLobbyFragment() call");
    }

    private void initAdapter() {
        recyclerViewAdapter = new
                RecyclerViewAdapter(getContext(), R.layout.item_chat_user, container);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.chatManager.chatLobbyFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = root.findViewById(R.id.recyclerview);
        initAdapter();

        this.container.add(new ChatPreviewUser("22", "444444", "John", "Hi"));
        this.container.add(new ChatPreviewUser("1245", User.getPublicKey(), "Yoni", "Shalom Everybody"));
        this.container.add(new ChatPreviewUser("123", "4343", "Yoram", "Let's ride tonight"));


        // Button send = root.findViewById(R.id.send);
        //messageBox = root.findViewById(R.id.messageBox);

        recyclerViewAdapter.setClassReference(this); // reference this class to the adaptor
        nav = Navigation.findNavController(container);

        MainActivity.chatManager.chatConversationFragment = null;
        if (incomingMessages.get("-MYVCkWexSO_jumnbr0l") != null)
            System.out.println("ChatLobbyFragment: " + incomingMessages.get("-MYVCkWexSO_jumnbr0l").size() + " items in incomingMessages list");
        return root;
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.layout.setOnClickListener(holder);
        holder.name.setText(container.get(position).getName());
        holder.message_preview.setText(container.get(position).getMessagePreview());
    }

    @Override
    public void onItemClick(@NonNull View holder, int position) {
        nav.navigate(R.id.action_chatFragment_to_chatConversationFragment);
    }

    public void updateLobbyList(ChatMessage chatMessage) {
        System.out.println("CHAT: a new message from " + chatMessage.getSendersName() + ": " + chatMessage.getMessage());
    }

    public void addNewIncomeMessage(String senderPublicKey, ChatPreviewUser chatMsgPreview) {
        if (incomingMessages.get(senderPublicKey) == null) {
            incomingMessages.put(senderPublicKey, new ArrayList<>());
        }
        incomingMessages.get(senderPublicKey).add(chatMsgPreview);
        System.out.println("New message by " + senderPublicKey + " was recorded successfully");
    }

    public ArrayList<ChatPreviewUser> getUsersList() {
        return container;
    }
}