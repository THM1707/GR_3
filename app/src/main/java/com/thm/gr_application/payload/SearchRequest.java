package com.thm.gr_application.payload;

import com.google.gson.annotations.Expose;

public class SearchRequest {
    @Expose
    private double latitude;

    @Expose
    private double longitude;

    @Expose
    private double distance;

    @Expose
    private int duration;

    @Expose
    private int budget;

    @Expose
    private int option;

    public SearchRequest(double latitude, double longitude, double distance, int duration,
            int budget, int option) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.duration = duration;
        this.budget = budget;
        this.option = option;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }
}
