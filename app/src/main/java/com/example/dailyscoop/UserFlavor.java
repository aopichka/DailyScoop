package com.example.dailyscoop;

public class UserFlavor {

    public String flavorID;
    public String userID;

    public UserFlavor() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UserFlavor(String flavorID, String userID) {
        this.flavorID = flavorID;
        this.userID = userID;
    }
}
