package com.example.dailyscoop;

public class Flavor {

    public String name;
    public String description;

    public Flavor() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Flavor(String name, String description) {
        this.name = name;
        this.description = description;
    }

}