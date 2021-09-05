package com.example.socialbike;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.socialbike.chat.ConversationChatActivity;

import org.jetbrains.annotations.NotNull;

public class ProfileFragment extends Fragment {

    static ProfileFragment profileFragment = null;
    private View root;
    private String userId, name;

    public static ProfileFragment getInstance() {
        if (profileFragment == null) {
            profileFragment = new ProfileFragment();
        }
        return profileFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_profile, container, false);
            Button message = root.findViewById(R.id.message_button);
            Button follow = root.findViewById(R.id.follow_button);
            message.setOnClickListener(view -> openMessageActivity());
            follow.setOnClickListener(view -> follow());

        }
        return root;
    }

    private void follow() {
        MainActivity.toast(getContext(), "You are not following XXX.", true);
    }

    private void openMessageActivity() {
        MainActivity.chatManager
                .openConversationActivity(getContext(), "2", "ff");
    }
}