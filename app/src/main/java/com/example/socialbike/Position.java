package com.example.socialbike;

import com.google.android.gms.maps.model.LatLng;

public class Position {

    private LatLng latLng;
    private String locationName, address;

    public Position(LatLng latLng, String locationName, String address) {
        this.latLng = latLng;
        this.locationName = locationName;
        this.address = address;
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

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setLatLng(LatLng latLng){
        this.latLng = latLng;
    }

}
