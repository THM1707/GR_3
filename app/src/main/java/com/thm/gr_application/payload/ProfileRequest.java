package com.thm.gr_application.payload;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProfileRequest {
    @Expose
    private String name;

    @Expose
    @SerializedName("newPassword")
    private String newPassword;

    @Expose
    @SerializedName("oldPassword")
    private String oldPassword;

    @Expose
    private String phone;

    @Expose
    private int gender;

    public ProfileRequest(String name, String newPassword, String oldPassword, String phone,
            int gender) {
        this.name = name;
        this.newPassword = newPassword;
        this.oldPassword = oldPassword;
        this.phone = phone;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }
}
