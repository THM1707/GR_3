package com.thm.gr_application.model;

public class ParkingData {
    private Long id;
    private String name;
    private int available;
    private float star;
    private double longitude;
    private double latitude;

    public ParkingData(Long id, String name, int available, float star, double longitude,
            double latitude) {
        this.id = id;
        this.name = name;
        this.available = available;
        this.star = star;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public ParkingData() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public float getStar() {
        return star;
    }

    public void setStar(float star) {
        this.star = star;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
