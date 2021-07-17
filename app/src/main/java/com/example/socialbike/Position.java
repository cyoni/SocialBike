package com.example.socialbike;

import com.google.android.gms.maps.model.LatLng;

public class Position {

    private LatLng latLng;
    private String locationName, address, country, state;

    public Position(LatLng latLng, String locationName, String address) {
        this.latLng = latLng;
        this.locationName = locationName;
        this.address = address;
    }

    public Position(LatLng latLng, String locationName, String country, String state) {
        this.latLng = latLng;
        this.locationName = locationName;
        this.country = country;
        this.state = state;
    }

    public Position(LatLng latLng, String locationName, String address, String country, String state) {
        this(latLng, locationName, address);
        this.country = country;
        this.state = state;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getAddress() {
        return address;
    }

    public String getCountry() {
        return country;
    }

    public String getState() {
        return state;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setLatLng(LatLng latLng){
        this.latLng = latLng;
    }

    @Override
    public String toString(){
        return locationName + ", " + address + ", " + country + ", " + state;
    }

}
