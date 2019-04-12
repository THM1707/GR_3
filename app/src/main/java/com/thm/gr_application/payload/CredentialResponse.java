package com.thm.gr_application.payload;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thm.gr_application.model.ParkingLot;

import java.util.List;

public class CredentialResponse {
    @Expose
    @SerializedName("accessToken")
    private String accessToken;

    @Expose
    @SerializedName("role")
    private String role;

    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("email")
    private String email;

    @Expose
    @SerializedName("gender")
    private int gender;

    @Expose
    @SerializedName("property")
    private ParkingLot property;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public ParkingLot getProperty() {
        return property;
    }

    public void setProperty(ParkingLot property) {
        this.property = property;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
