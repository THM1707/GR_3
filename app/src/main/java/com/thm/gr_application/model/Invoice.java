package com.thm.gr_application.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

public class Invoice implements Serializable {
    @Expose
    @SerializedName("id")
    private Long mId;

    @Expose
    @SerializedName("createdDate")
    private String mCreatedDate;

    @Expose
    @SerializedName("endDate")
    private String mEndDate;

    @Expose
    @SerializedName("plate")
    private String mPlate;

    @Expose
    @SerializedName("status")
    private String mStatus;

    @Expose
    @SerializedName("booked")
    private boolean isBooked;

    @Expose
    @SerializedName("income")
    private int mIncome;

    @Expose
    @SerializedName("duration")
    private int mDuration;

    @Expose
    @SerializedName("late")
    private int mLate;

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public String getCreatedDate() {
        return mCreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        mCreatedDate = createdDate;
    }

    public String getEndDate() {
        return mEndDate;
    }

    public void setEndDate(String endDate) {
        mEndDate = endDate;
    }

    public String getPlate() {
        return mPlate;
    }

    public void setPlate(String plate) {
        mPlate = plate;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }

    public int getIncome() {
        return mIncome;
    }

    public void setIncome(int income) {
        mIncome = income;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public int getLate() {
        return mLate;
    }

    public void setLate(int late) {
        mLate = late;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;
        return isBooked == invoice.isBooked
                && Objects.equals(mId, invoice.mId)
                && Objects.equals(mCreatedDate, invoice.mCreatedDate)
                && Objects.equals(mEndDate, invoice.mEndDate)
                && Objects.equals(mPlate, invoice.mPlate)
                && Objects.equals(mStatus, invoice.mStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId, mCreatedDate, mEndDate, mPlate, mStatus, isBooked);
    }
}
