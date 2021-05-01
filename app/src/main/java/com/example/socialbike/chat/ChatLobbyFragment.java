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

import java.util.ArrayList;

public class ChatLobbyFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener {

    static ChatLobbyFragment chatFragment = null;
    private RecyclerView recyclerView;
    private final ArrayList<ChatUser> container;
    private RecyclerViewAdapter recyclerViewAdapter;
    //private EditText messageBox;
    private NavController nav;

    public static ChatLobbyFragment getInstance(){
        if (chatFragment == null){
            chatFragment = new ChatLobbyFragment();
        }
        return chatFragment;
    }

    public ChatLobbyFragment(){
        container = new ArrayList<>();
    }

    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), R.layout.item_chat_user, container);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = root.findViewById(R.id.recyclerview);
        initAdapter();

        this.container.add(new ChatUser("22", "Yoni", "Have a nice day!"));
        // Button send = root.findViewById(R.id.send);
        //messageBox = root.findViewById(R.id.messageBox);

        recyclerViewAdapter.setClassReference(this); // reference this class to the adaptor
        nav = Navigation.findNavController(container);

        MainActivity.chatManager.chatLobbyFragment = this;
        MainActivity.chatManager.chatConversationFragment = null;

        return root;
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.start_conversation.setOnClickListener(holder);
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
}