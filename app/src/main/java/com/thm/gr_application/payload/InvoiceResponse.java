package com.thm.gr_application.payload;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thm.gr_application.model.Invoice;
import com.thm.gr_application.model.ParkingLot;

import java.util.List;

public class InvoiceResponse {
    @Expose
    private String message;

    @Expose
    private  Invoice invoice;

    @Expose
    @SerializedName("parkingLot")
    private ParkingLot parkingLot;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public void setParkingLot(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
    }
}
