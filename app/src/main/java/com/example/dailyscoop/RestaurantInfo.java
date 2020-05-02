package com.example.dailyscoop;

import android.media.Image;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.Calendar;
import java.util.GregorianCalendar;


public class RestaurantInfo {
    private String placeId;
    private String name;
    private String address;
    private String websiteUri;
    private String fotd;
    private long fotdLastUpdated;
    private Image image;

    public RestaurantInfo() {
        // Needed to be able to pass object to Firebase DB
    }

    public RestaurantInfo(String placeId) {
        this.placeId = placeId;
        this.fotdLastUpdated = new GregorianCalendar().getTimeInMillis();
    }

    // SETTERS
    public void setPlaceId(String placeId) { this.placeId = placeId; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setWebsiteUri(String websiteUri) { this.websiteUri = websiteUri; }
    public void setFotd(String fotd) { this.fotd = fotd; this.fotdLastUpdated = new GregorianCalendar().getTimeInMillis(); }
    public void setImage(Image image) { this.image = image; }

    // GETTERS
    public String getPlaceId() { return placeId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getWebsiteUri() { return websiteUri; }
    public String getFotd() { return fotd; }
    public Image getImage() { return image; }
    public long getFotdLastUpdated() { return fotdLastUpdated; }


    @Exclude
    public Calendar getFotdLastUpdatedDate() {
        Calendar datetime = Calendar.getInstance();
        datetime.setTimeInMillis(this.fotdLastUpdated);
        return datetime;
    }
}
