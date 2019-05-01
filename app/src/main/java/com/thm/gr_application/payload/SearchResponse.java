package com.thm.gr_application.payload;

import com.google.gson.annotations.Expose;
import java.util.Map;

public class SearchResponse {
    @Expose
    private String message;

    @Expose
    private Map<Integer, Long> data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<Integer, Long> getData() {
        return data;
    }

    public void setData(Map<Integer, Long> data) {
        this.data = data;
    }
}
