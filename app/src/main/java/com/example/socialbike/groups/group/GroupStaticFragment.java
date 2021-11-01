package com.example.socialbike.groups.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.socialbike.AddPostActivity;
import com.example.socialbike.Consts;
import com.example.socialbike.MessageGetter;
import com.example.socialbike.Post;
import com.example.socialbike.PostButtons;
import com.example.socialbike.R;
import com.example.socialbike.RecyclerViewAdapter;
import com.example.socialbike.Updater;
import com.example.socialbike.room_database.Member;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class GroupStaticFragment extends Fragment {

    View root;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.group_static_fragment, container, false);
        }
        return root;
    }


}