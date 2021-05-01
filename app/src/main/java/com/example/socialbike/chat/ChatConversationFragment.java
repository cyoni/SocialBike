package com.example.socialbike.chat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.socialbike.MainActivity;
import com.example.socialbike.R;
import com.example.socialbike.RecyclerViewAdapter;

import java.util.ArrayList;

public class ChatConversationFragment extends Fragment  implements RecyclerViewAdapter.ItemClickListener {

    private static ChatConversationFragment fragment;
    private EditText messageBox;
    private RecyclerView recyclerView;
    private final ArrayList<ChatMessage> container;
    private RecyclerViewAdapter recyclerViewAdapter;


    public ChatConversationFragment() {
        container = new ArrayList<>();
    }


    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), R.layout.item_chat, container);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }


    public static ChatConversationFragment getInstance(){
        if (fragment == null){
            fragment = new ChatConversationFragment();
        }
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_chat_conversation, container, false);
        Button sendMsgButton = root.findViewById(R.id.send);
        messageBox = root.findViewById(R.id.messageBox);
        recyclerView = root.findViewById(R.id.recyclerview);
        initAdapter();

        MainActivity.chatManager.chatConversationFragment = this;
        MainActivity.chatManager.chatLobbyFragment = null;

        sendMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.chatManager.sendMessage("123213", messageBox.getText().toString());
            }
        });
        return root;
    }


    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.msgStyle.setBackgroundResource(R.drawable.chat_incoming_msg);
        holder.msgStyle.setText(container.get(position).getMessage());
       // holder.start_conversation.setOnClickListener(holder);
    }


    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }

    public void addMessage(ChatMessage chatMessage) {
        container.add(chatMessage);
        recyclerViewAdapter.notifyItemInserted(container.size() - 1);
    }
}