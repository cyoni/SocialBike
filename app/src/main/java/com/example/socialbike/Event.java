package com.example.socialbike;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Event extends Post {

    @JsonProperty("event_id")
    private String eventId;
    @JsonProperty("group_id")
    private String groupId;
    private String name, details, created_event_time;
    private long start, end;
    @JsonProperty("num_interested_members")
    private int numInterestedMembers;
    @JsonProperty("num_participants")
    private int numParticipants;
    private double lat, lng;
    private String title, address;
    @JsonProperty("comments_num")
    private String commentsNum, elementScore;
    private Position position;
    private boolean isInterested, isGoing;
    @JsonProperty("picture_header_created")
    private long created;
    @JsonProperty("has_header_picture")
    private boolean hasHeaderPicture;
    public boolean getHasHeaderPicture(){
        return hasHeaderPicture;
    }
    public long getCreated(){return created;}
    public void setCreated(long created){this.created = created;}

    @JsonProperty("event_id")
    public String getEventId() {
        return eventId;
    }

    @JsonProperty("group_id")
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @JsonProperty("group_id")
    public String getGroupId() {
        return groupId;
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

    public long getStart() {
        return start;
    }

    @JsonProperty("start")
    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long time) {
        this.end = time;
    }

    @JsonProperty("num_interested_members")
    public int getNumInterestedMembers() {
        return numInterestedMembers;
    }

    @JsonProperty("num_interested_members")
    public void setNumInterestedMembers(int numInterestedMembers) {
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

    public boolean getIsInterested(){
        return this.isInterested;
    }

    public boolean getIsGoing(){
        return this.isGoing;
    }

    public void setIsInterested(boolean state){
        this.isInterested = state;
    }

    public void setIsGoing(boolean state){
        this.isGoing = state;
    }

    public Position getPosition() {
        return new Position(getLat(), getLng());
    }

}

