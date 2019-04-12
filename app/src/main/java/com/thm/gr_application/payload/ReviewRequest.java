package com.thm.gr_application.payload;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReviewRequest {
    @Expose
    @SerializedName("targetId")
    private Long id;

    @Expose
    private String comment;

    @Expose
    private int star;

    public ReviewRequest(Long id, String comment, int star) {
        this.id = id;
        this.comment = comment;
        this.star = star;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }
}
