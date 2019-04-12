package com.thm.gr_application.payload;

import com.google.gson.annotations.Expose;
import com.thm.gr_application.model.Review;
import java.util.List;

public class ReviewsResponse {
    @Expose
    private String message;

    @Expose
    private List<Review> data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Review> getData() {
        return data;
    }

    public void setData(List<Review> data) {
        this.data = data;
    }
}
