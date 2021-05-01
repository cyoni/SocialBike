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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.socialbike.MainActivity;
import com.example.socialbike.R;
import com.example.socialbike.RecyclerViewAdapter;

import java.util.ArrayList;

public class ChatConversationFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener {

    //private static ChatConversationFragment fragment;
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


/*    public static ChatConversationFragment getInstance(){
        if (fragment == null){
            fragment = new ChatConversationFragment();
        }
        return fragment;
    }*/


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_chat_conversation, container, false);
        Button sendMsgButton = root.findViewById(R.id.send);
        Button backButton = root.findViewById(R.id.back);
        messageBox = root.findViewById(R.id.messageBox);
        recyclerView = root.findViewById(R.id.recyclerview);
        initAdapter();

        MainActivity.chatManager.chatConversationFragment = this;
        MainActivity.chatManager.chatLobbyFragment = null;


        backButton.setOnClickListener(view -> {
            NavController nav = Navigation.findNavController(container);
            nav.navigate(R.id.action_chatConversationFragment_to_chatFragment);
        });


        sendMsgButton.setOnClickListener(view -> {
            String message =  messageBox.getText().toString();
            MainActivity.chatManager.sendMessage("123213", message);
            messageBox.setText("");
        });

        return root;
    }


    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        ChatMessage currentItemToBind = container.get(position);
        holder.msgStyle.setText(currentItemToBind.getMessage());
        if (currentItemToBind.isIncomingMessage())
            holder.msgStyle.setBackgroundResource(R.drawable.chat_incoming_msg);
        else {
            holder.msgStyle.setBackgroundResource(R.drawable.chat_outgoing_msg);
            setMessageBoxToTheRight(holder.msgStyle);
         //   holder.msgStyle.setPadding(0, 4, 20, 0);

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

    public void addMessage(ChatMessage chatMessage) {
        container.add(chatMessage);
        recyclerViewAdapter.notifyItemInserted(container.size() - 1);
    }
}