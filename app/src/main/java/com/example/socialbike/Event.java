package com.example.socialbike;


import com.google.android.gms.maps.model.LatLng;

public class Event extends Post{

    private final String eventId, userPublicKey;
    private final String name;
    private final String dateOfEvent;
    private final String timeOfEvent;
    private final String createdEventTime;
    private final String amountOfInterestedPeople;
    private final int numberOfParticipants;
    public static String EVENTS_CONTAINER_CODE = "events";
    private final Position position;

    public Event(String eventId, String userPublicKey, String name,
                 String dateOfEvent, String timeOfEvent, String createdEventTime,
                 String amountOfInterestedPeople, int numberOfParticipants,
                 Position position, String message, int commentsNumber) {

        super(eventId, userPublicKey, name, 1245, message, commentsNumber);

        this.container = EVENTS_CONTAINER_CODE;
        this.eventId = eventId;
        this.userPublicKey = userPublicKey;
        this.name = name;
        this.dateOfEvent = dateOfEvent;
        this.timeOfEvent = timeOfEvent;
        this.createdEventTime = createdEventTime;
        this.amountOfInterestedPeople = amountOfInterestedPeople;
        this.numberOfParticipants = numberOfParticipants;
        this.position = position;
    }

    public String getEventId() {
        return eventId;
    }

    public String getUserPublicKey() {
        return userPublicKey;
    }

    public String getName() {
        return name;
    }

    public String getDateOfEvent() {
        return dateOfEvent;
    }

    public String getTimeOfEvent() {
        return timeOfEvent;
    }

    public String getCreatedEventTime() {
        return createdEventTime;
    }

    public String getAmountOfInterestedPeople() {
        return amountOfInterestedPeople;
    }

    public int getNumberOfParticipants() {
        return numberOfParticipants;
    }

    public LatLng getLatLng(){
        return position.getLatLng();
    }

    public Position getPosition() {
        return position;
    }

}
