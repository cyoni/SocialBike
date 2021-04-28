package com.example.socialbike;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;


public class EventsFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener{

    static EventsFragment eventsFragment = null;
    private RecyclerView recyclerView;
    private final ArrayList<Post> container;
    private RecyclerViewAdapter recyclerViewAdapter;

    public EventsFragment() {
        container = new ArrayList<>();
    }

    private void initAdapter() {
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), R.layout.item_row, container);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setClassReference(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static EventsFragment getInstance(){
        if (eventsFragment == null){
            eventsFragment = new EventsFragment();
        }
        return eventsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_events, container, false);
        recyclerView = root.findViewById(R.id.recyclerview);
        initAdapter();

        Updater updater = new Updater(this.container, recyclerViewAdapter);
        updater.add(new Post("123", "yoni", 1213, "Hi!"));
        updater.update();

        Button addEvent = root.findViewById(R.id.add_new_event_button);
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddNewEventActivity.class);
                startActivity(intent);
            }
        });

        Button sortButton = root.findViewById(R.id.sort_button);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (sortButton.getText().equals("Most relevant")){
                    sortButton.setText("New Activity");
                }
                else{
                    sortButton.setText("Most relevant");
                }

            }
        });

        return root;
    }

    @Override
    public void onBinding(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.message.setText(container.get(position).getMsg());
        holder.name.setText(container.get(position).getName());
    }

    @Override
    public void onItemClick(@NonNull View holder, int position) {

    }
}