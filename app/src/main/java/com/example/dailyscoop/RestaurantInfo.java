package com.example.dailyscoop;

import android.media.Image;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class RestaurantInfo {
    private String placeId;
    private String name;
    private String address;
    private String websiteUri;
    private String fotd;
    private long fotdLastUpdated;
    private Image image;
    private Map<String, String> flavorSchedule;

    // CONSTRUCTORS

    public RestaurantInfo() {
        // Needed to be able to pass object to Firebase DB
    }

    public RestaurantInfo(String placeId) {
        this.placeId = placeId;
        this.fotdLastUpdated = new GregorianCalendar().getTimeInMillis();
        this.flavorSchedule = new HashMap<>();
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
    public Map<String, String> getFlavorSchedule() { return flavorSchedule; }

    // INSTANCE METHODS
    public String getDateForFlavor(String flavor) {
        for (String key : flavorSchedule.keySet()) {
            if (flavorSchedule.get(key).equals(flavor)) {
                return key;
            }
        }
        return "";
    }

    @Exclude
    public Calendar getFotdLastUpdatedDate() {
        Calendar datetime = Calendar.getInstance();
        datetime.setTimeInMillis(this.fotdLastUpdated);
        return datetime;
    }

    public void addFotdToSchedule(String date, String fotd) {
        // Add the record to the list
        if (!flavorSchedule.containsKey(date)) {
            flavorSchedule.put(date, fotd);
        }
    }
}
