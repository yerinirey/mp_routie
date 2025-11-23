package com.example.mpteamproj;

public class LightningPost {
    private String title;
    private String placeAndTime;

    public LightningPost(String title, String placeAndTime) {
        this.title = title;
        this.placeAndTime = placeAndTime;
    }

    public String getTitle() {
        return title;
    }

    public String getPlaceAndTime() {
        return placeAndTime;
    }
}
