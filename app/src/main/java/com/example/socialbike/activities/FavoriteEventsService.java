package com.example.socialbike.activities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FavoriteEventsService {

    String CODE = "favorite_events";
    private Set<String> favoriteEvents = new HashSet<>();

    public void init(){
        Map<String, ?> events = MainActivity.utils.getAllPreferences(CODE);
        Set<String> tmp = events.keySet();
        favoriteEvents.addAll(tmp); // because keySet does not support `add` operation
    }

    public void add(String event){
        if (!doesExist(event)) {
            favoriteEvents.add(event);
            MainActivity.utils.savePreference(CODE, event, "-");
        }
    }

    public boolean remove(String event){
        if (doesExist(event)){
            MainActivity.utils.removePreference(CODE, event);
            favoriteEvents.remove(event);
            return true;
        }
        return false;
    }

    public boolean doesExist(String event){
        return favoriteEvents.contains(event);
    }

    public List<String> getEvents() {
        List<String> events = new ArrayList<>();
        events.addAll(favoriteEvents);
        return events;
    }

    @Override
    public String toString(){
        List<String> events = getEvents();
        String result = "";
        for (String event : events){
            result += event + ",";
        }
        return result;
    }

}
