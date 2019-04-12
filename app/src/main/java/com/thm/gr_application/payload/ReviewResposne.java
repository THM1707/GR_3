package com.thm.gr_application.payload;

import com.google.gson.annotations.Expose;
import com.thm.gr_application.model.Review;

public class ReviewResposne {
    @Expose
    private String message;

    @Expose
    private Review data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Review getData() {
        return data;
    }

    public void setData(Review data) {
        this.data = data;
    }
}
