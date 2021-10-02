package com.example.socialbike;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class EventDTO {

    private List<Event> events = null;
    @JsonProperty("extra_events")
    private List<Event> extra_events = null;

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }


}
