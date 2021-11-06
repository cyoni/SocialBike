package com.example.socialbike.utilities;

import com.example.socialbike.recyclerview.RecyclerViewAdapter;

import java.util.List;

public class Updater {

    public final List container;
    private final RecyclerViewAdapter recyclerViewAdapter;
    public IUpdate referenceClass;

    public Updater(Object referenceClass, List container, RecyclerViewAdapter recyclerViewAdapter){
        this.container = container;
        this.recyclerViewAdapter = recyclerViewAdapter;
        this.referenceClass = (IUpdate) referenceClass;
    }

    public void add(Object item){
        container.add(0, item);
        update();
    }

    public void update(){
        recyclerViewAdapter.notifyItemInserted(container.size() - 1);
    }

    public void update(int index){
        recyclerViewAdapter.notifyItemInserted(index);
    }



    public interface IUpdate {
        void onFinishedUpdating();
    }

}
