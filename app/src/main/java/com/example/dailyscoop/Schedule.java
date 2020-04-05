package com.example.dailyscoop;

import com.google.firebase.firestore.DocumentReference;

import java.util.Date;

public class Schedule {

    public Date date;
    public DocumentReference flavorReference;
    public DocumentReference locationReference;

    public Schedule() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Schedule(Date date, DocumentReference flavorReference, DocumentReference locationReference) {
        this.date = date;
        this.flavorReference = flavorReference;
        this.locationReference = locationReference;
    }

}