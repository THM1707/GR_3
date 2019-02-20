package com.thm.gr_application.payload;

import com.google.gson.annotations.Expose;

public class SignUpResponse {
    @Expose
    private String success;

    @Expose
    private String message;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
