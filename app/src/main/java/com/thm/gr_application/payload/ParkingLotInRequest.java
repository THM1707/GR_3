package com.thm.gr_application.payload;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ParkingLotInRequest {
    @Expose
    @SerializedName("idList")
    private List<Long> idList;

    public ParkingLotInRequest(List<Long> idList) {
        this.idList = idList;
    }

    public List<Long> getIdList() {
        return idList;
    }

    public void setIdList(List<Long> idList) {
        this.idList = idList;
    }
}
