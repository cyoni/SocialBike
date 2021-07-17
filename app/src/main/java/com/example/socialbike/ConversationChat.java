package com.example.socialbike;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialbike.chat.ChatMessage;
import com.example.socialbike.databinding.ActivityConversationChatBinding;

import java.io.Serializable;
import java.util.ArrayList;

public class ConversationChat extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener, Updater.IUpdate  {
    //private static ChatConversationFragment fragment;
    private EditText messageBox;
    private RecyclerView recyclerView;
    private final ArrayList<ChatMessage> historyMessages;
    private RecyclerViewAdapter recyclerViewAdapter;
    private String userId;


    public ConversationChat() {
        historyMessages = new ArrayList<>();
    }


    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(this, R.layout.item_chat, historyMessages);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }


/*    public static ChatConversationFragment getInstance(){
        if (fragment == null){
            fragment = new ChatConversationFragment();
        }
        return fragment;
    }*/


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ActivityConversationChatBinding binding = ActivityConversationChatBinding.inflate(getLayoutInflater());
        setContentView(R.layout.fragment_chat_conversation);
        setSupportActionBar(binding.toolbar);

        userId = getIntent().getStringExtra("userId");


        MainActivity.chatManager.currentConversationChat = this;

        Button sendMsgButton = findViewById(R.id.send);

        messageBox = findViewById(R.id.messageBox);
        recyclerView = findViewById(R.id.recyclerview);
        initAdapter();


        sendMsgButton.setOnClickListener(view -> {
            String message =  messageBox.getText().toString();
            MainActivity.chatManager.sendMessage(userId, message);
            messageBox.setText("");
        });

    }



    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        ChatMessage currentItemToBind = historyMessages.get(position);
        holder.msgStyle.setText(currentItemToBind.getMessage());
        if (currentItemToBind.isIncomingMessage())
            holder.msgStyle.setBackgroundResource(R.drawable.chat_incoming_msg);
        else {
            holder.msgStyle.setBackgroundResource(R.drawable.chat_outgoing_msg);
            setMessageBoxToTheRight(holder.msgStyle);
               holder.msgStyle.setPadding(0, 4, 20, 0);

        }
    }

    private void setMessageBoxToTheRight(TextView msgStyle) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) msgStyle.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        msgStyle.setLayoutParams(params); //causes layout update
    }


    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }


    public void addNewMessage(ChatMessage chatMessage){
        System.out.println("conversationChat got new msg! : " + chatMessage.getMessage());
        historyMessages.add(chatMessage);
        recyclerViewAdapter.notifyItemInserted(historyMessages.size() - 1);
    }

    @Override
    public void onFinishedTakingNewMessages() {

    }
}