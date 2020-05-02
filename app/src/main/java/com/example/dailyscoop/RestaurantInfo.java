package com.example.dailyscoop;

import android.media.Image;

import java.util.Date;

public class RestaurantInfo {
    private String placeId;
    private String name;
    private String address;
    private String websiteUri;
    private String fotd;
    private Date fotdLastUpdated;
    private Image image;

    public RestaurantInfo() {
        // Needed to be able to pass object to Firebase DB
    }

    public RestaurantInfo(String placeId) {
        this.placeId = placeId;
        this.fotdLastUpdated = new Date();
    }

    // SETTERS
    public void setPlaceId(String placeId) { this.placeId = placeId; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setWebsiteUri(String websiteUri) { this.websiteUri = websiteUri; }
    public void setFotd(String fotd) { this.fotd = fotd; }
    public void setImage(Image image) { this.image = image; }

    // GETTERS
    public String getPlaceId() { return placeId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getWebsiteUri() { return websiteUri; }
    public String getFotd() { return fotd; }
    public Image getImage() { return image; }
    public Date getFotdLastUpdated() { return fotdLastUpdated; }
}
