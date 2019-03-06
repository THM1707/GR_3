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
    @SerializedName("username")
    private String username;

    @Expose
    @SerializedName("email")
    private String email;

    @Expose
    @SerializedName("favorites")
    private List<Long> favorites;

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

    public List<Long> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<Long> favorites) {
        this.favorites = favorites;
    }

    public ParkingLot getProperty() {
        return property;
    }

    public void setProperty(ParkingLot property) {
        this.property = property;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
