package com.thm.gr_application.payload;

import com.google.gson.annotations.Expose;

public class TestResponse {
    @Expose
    private String message;

    @Expose
    private String type;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
