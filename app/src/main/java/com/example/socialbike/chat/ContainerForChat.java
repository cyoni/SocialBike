package com.example.socialbike.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.socialbike.R;

public class ContainerForChat extends Fragment {

    static ContainerForChat chat;
    private View root;

    public static ContainerForChat getInstance(){
        if (chat == null){
            chat = new ContainerForChat();
        }
        return chat;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null)
            root = inflater.inflate(R.layout.chat, container, false);
        return root;
    }


}
