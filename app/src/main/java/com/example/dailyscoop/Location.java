package com.example.dailyscoop;

public class Location {

    public String name;
    public String address;

    public Location() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Location(String name, String address) {
        this.name = name;
        this.address = address;
    }

}