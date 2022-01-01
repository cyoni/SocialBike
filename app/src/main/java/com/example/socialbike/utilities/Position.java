package com.example.socialbike.utilities;

import com.google.android.gms.maps.model.LatLng;

public class Position {

    private LatLng latLng;
    private String address, country, state, city;

    public Position(LatLng latLng, String city, String country) {
        this.latLng = latLng;
        this.city = city;
        this.country = country;
    }

    public Position(LatLng latLng, String address, String city, String country) {
        this.latLng = latLng;
        this.address = address;
        this.country = country;
        this.city = city;
        this.country = country;
    }

    public Position() {

    }

    public Position(double lat, double lng) {
        this.latLng = new LatLng(lat, lng);
    }

    public LatLng getLatLng() {
        return latLng;
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

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    @Override
    public String toString() {
        return address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return this.city;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
