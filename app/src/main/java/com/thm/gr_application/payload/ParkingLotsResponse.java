package com.thm.gr_application.payload;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thm.gr_application.model.ParkingLot;

import java.util.List;

public class ParkingLotsResponse {
    @SerializedName("message")
    @Expose
    private String mMessage;

    @SerializedName("data")
    @Expose
    private List<ParkingLot> mData;

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public List<ParkingLot> getData() {
        return mData;
    }

    public void setData(List<ParkingLot> data) {
        mData = data;
    }
}
