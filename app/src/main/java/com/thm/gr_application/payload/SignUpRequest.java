package com.thm.gr_application.payload;

import com.google.gson.annotations.Expose;

public class SignUpRequest {
    @Expose
    private String name;

    @Expose
    private String username;

    @Expose
    private String email;

    @Expose
    private String password;

    @Expose
    private String phone;

    @Expose
    private int gender;

    public SignUpRequest(String name, String username, String email, String password, String phone,
            int gender) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
