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
import com.example.socialbike.Utils;
import com.example.socialbike.chat.history.History;
import com.example.socialbike.chat.history.HistoryDao;

import java.util.ArrayList;

public class ConversationChatActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener {

    private EditText messageBox;
    private RecyclerView recyclerView;
    private final ArrayList<History> messages = new ArrayList<>();
    private RecyclerViewAdapter recyclerViewAdapter;
    private String userId;
    private HistoryDao historyDao;
    int messageCount = 0;


    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(this, R.layout.item_chat, messages);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_chat);
        setToolbar();
        historyDao = MainActivity.database.historyDao();

        userId = getIntent().getStringExtra("userId");
        MainActivity.chatManager.currentConversationChat = this;
        sendMessageListener();
        messageBox = findViewById(R.id.messageBox);
        recyclerView = findViewById(R.id.recyclerview);
        initAdapter();
        getChatHistory();
    }

    private void sendMessageListener() {
        Button sendMsgButton = findViewById(R.id.send);

        sendMsgButton.setOnClickListener(view -> {
            String message = messageBox.getText().toString();
            if (message.trim().isEmpty())
                return;
            MainActivity.chatManager.sendMessage(userId, message);
            scrollToBottom();
            messageBox.setText("");
        });
    }

    private void scrollToBottom() {
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.showKeyboard(this);
        messageBox.requestFocus();
        scrollToBottom();
    }

    public String getUserId() {
        return userId;
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.flexible_example_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setTitle(getIntent().getStringExtra("name"));
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        History currentItemToBind = messages.get(position);
        holder.msgStyle.setText(currentItemToBind.message);
        if (currentItemToBind.isIncoming)
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


    public void getChatHistory() {  // TO IMPROVE
        historyDao.getHistoryOfMember(userId).observe(this, history -> {
            for (int i = messageCount; i < history.size(); i++) {
                messages.add(history.get(i));
            }
            recyclerViewAdapter.notifyItemRangeChanged(messageCount, history.size());
            scrollToBottom();
            messageCount = history.size();
        });
    }

    @Override
    public void onBackPressed() {
        Utils.hideKeyboard(this);
        MainActivity.chatManager.currentConversationChat = null;
        finish();
    }
}