package com.example.socialbike;

import java.util.ArrayList;

public class Updater {

    private final ArrayList<Post> container;
    private final RecyclerViewAdapter recyclerViewAdapter;

    public Updater(ArrayList<Post> container, RecyclerViewAdapter recyclerViewAdapter){
        this.container = container;
        this.recyclerViewAdapter = recyclerViewAdapter;
    }

    public void add(Post post){
        this.container.add(post);
    }
    
    public void update(){
        recyclerViewAdapter.notifyItemInserted(container.size() - 1);
    }
    
}
