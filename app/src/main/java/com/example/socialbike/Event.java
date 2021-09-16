package com.example.socialbike;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Event extends Post{

    @JsonProperty("event_id")
    private String eventId;
    private String name;
    private String user_public_key;
    private String details;
    private String created_event_time;
    private String date;
    private String time;
    @JsonProperty("num_interested_members")
    private String numInterestedMembers;
    @JsonProperty("num_participants")
    private int numParticipants;
    private double lat;
    private double lng;
    private String title;
    private String address;
    @JsonProperty("comments_num")
    private String commentsNum;
    private String elementScore;

    public Event(String eventId, String userPublicKey, String name,
                 String dateOfEvent, String timeOfEvent, String createdEventTime,
                 String amountOfInterestedPeople, int numberOfParticipants,
                 Position position, String message, int commentsNumber) {

        super(eventId, userPublicKey, name, 1245, message,0, commentsNumber, false);

        this.DatabaseContainer = EVENTS_CONTAINER_CODE;
        this.eventId = eventId;
        this.user_public_key = userPublicKey;
        this.name = name;
        this.date = dateOfEvent;
        this.time = timeOfEvent;
        this.created_event_time = createdEventTime;
        this.numInterestedMembers = amountOfInterestedPeople;
        this.numParticipants = numberOfParticipants;
        this.position = position;
    }

    @JsonProperty("event_id")
    public String getEventId() {
        return eventId;
    }

    @JsonProperty("event_id")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("user_public_key")
    public String getUser_public_key() {
        return user_public_key;
    }

    @JsonProperty("user_public_key")
    public void setUser_public_key(String user_public_key) {
        this.user_public_key = user_public_key;
    }

    @JsonProperty("details")
    public String getDetails() {
        return details;
    }

    @JsonProperty("details")
    public void setDetails(String details) {
        this.details = details;
    }

    @JsonProperty("created_event_time")
    public String getCreated_event_time() {
        return created_event_time;
    }

    @JsonProperty("created_event_time")
    public void setCreated_event_time(String created_event_time) {
        this.created_event_time = created_event_time;
    }

    @JsonProperty("date")
    public String getDate() {
        return date;
    }

    @JsonProperty("date")
    public void setDate(String date) {
        this.date = date;
    }

    @JsonProperty("time")
    public String getTime() {
        return time;
    }

    @JsonProperty("time")
    public void setTime(String time) {
        this.time = time;
    }

    @JsonProperty("num_interested_members")
    public String getNumInterestedMembers() {
        return numInterestedMembers;
    }

    @JsonProperty("num_interested_members")
    public void setNumInterestedMembers(String numInterestedMembers) {
        this.numInterestedMembers = numInterestedMembers;
    }

    @JsonProperty("num_participants")
    public int getNumParticipants() {
        return numParticipants;
    }

    @JsonProperty("num_participants")
    public void setNumParticipants(int numParticipants) {
        this.numParticipants = numParticipants;
    }

    @JsonProperty("lat")
    public double getLat() {
        return lat;
    }

    @JsonProperty("lat")
    public void setLat(double lat) {
        this.lat = lat;
    }

    @JsonProperty("lng")
    public double getLng() {
        return lng;
    }

    @JsonProperty("lng")
    public void setLng(double lng) {
        this.lng = lng;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("address")
    public String getAddress() {
        return address;
    }

    @JsonProperty("address")
    public void setAddress(String address) {
        this.address = address;
    }

    @JsonProperty("comments_num")
    public String getCommentsNum() {
        return commentsNum;
    }

    @JsonProperty("comments_num")
    public void setCommentsNum(String commentsNum) {
        this.commentsNum = commentsNum;
    }

    @JsonProperty("elementScore")
    public String getElementScore() {
        return elementScore;
    }

    @JsonProperty("elementScore")
    public void setElementScore(String elementScore) {
        this.elementScore = elementScore;
    }
}
