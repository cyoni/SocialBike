package com.example.socialbike;

import java.util.ArrayList;
import java.util.List;

public class Updater {

    private final List container;
    private final RecyclerViewAdapter recyclerViewAdapter;

    public Updater(List container, RecyclerViewAdapter recyclerViewAdapter){
        this.container = container;
        this.recyclerViewAdapter = recyclerViewAdapter;
    }

    public void add(Post item){
        this.container.add(item);
    }
    
    public void update(){
        recyclerViewAdapter.notifyItemInserted(container.size() - 1);
    }
    
}
