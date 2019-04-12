package com.thm.gr_application.payload;

import com.google.gson.annotations.Expose;
import com.thm.gr_application.model.ParkingLot;

public class ParkingLotResponse {
    @Expose
    private String message;

    @Expose
    private ParkingLot data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ParkingLot getData() {
        return data;
    }

    public void setData(ParkingLot data) {
        this.data = data;
    }
}
