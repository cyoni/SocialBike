package com.example.socialbike;

public class Event extends Post{

    private final String eventId;
    private final String userPublicKey;
    private final String name;
    private final String dateOfEvent;
    private final String timeOfEvent;
    private final String createdEventTime;
    private final String amountOfInterestedPeople;
    private final String city;
    private final String country;

    public Event(String eventId, String userPublicKey, String name,
                 String dateOfEvent, String timeOfEvent, String createdEventTime,
                 String amountOfInterestedPeople, String city,
                 String country, String message) {
        super(eventId, userPublicKey, name, 1245, message);
        this.eventId = eventId;
        this.userPublicKey = userPublicKey;
        this.name = name;
        this.dateOfEvent = dateOfEvent;
        this.timeOfEvent = timeOfEvent;
        this.createdEventTime = createdEventTime;
        this.amountOfInterestedPeople = amountOfInterestedPeople;
        this.city = city;
        this.country = country;
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

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }
}
