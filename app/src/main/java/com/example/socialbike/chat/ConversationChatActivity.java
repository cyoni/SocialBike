package com.example.socialbike.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialbike.MainActivity;
import com.example.socialbike.R;
import com.example.socialbike.RecyclerViewAdapter;
import com.example.socialbike.chat.ChatMessage;
import com.example.socialbike.databinding.ActivityConversationChatBinding;

import java.util.ArrayList;

public class ConversationChatActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener {

    private EditText messageBox;
    private RecyclerView recyclerView;
    private final ArrayList<ChatMessage> historyMessages = new ArrayList<>();;
    private RecyclerViewAdapter recyclerViewAdapter;
    private String userId;

    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(this, R.layout.item_chat, historyMessages);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_chat);

        setToolbar();

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

    public String getUserId(){
        return userId;
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.flexible_example_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setTitle(getIntent().getStringExtra("name"));
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
        System.out.println("conversationChat got new msg: " + chatMessage.getMessage());
        historyMessages.add(chatMessage);
        recyclerViewAdapter.notifyItemInserted(historyMessages.size() - 1);
    }

    @Override
    public void onBackPressed() {
        MainActivity.chatManager.currentConversationChat = null;
        finish();
    }

}