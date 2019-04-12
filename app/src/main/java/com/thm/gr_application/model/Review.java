package com.thm.gr_application.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class Review {

    @Expose
    private Long id;

    @Expose
    @SerializedName("ownerName")
    private String ownerName;

    @Expose
    @SerializedName("ownerEmail")
    private String ownerEmail;

    @Expose
    private String comment;

    @Expose
    private int star;

    @Expose
    @SerializedName("updatedAt")
    private String updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
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

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return star == review.star
                && Objects.equals(id, review.id)
                && Objects.equals(ownerName, review.ownerName)
                && Objects.equals(ownerEmail, review.ownerEmail)
                && Objects.equals(comment, review.comment)
                && Objects.equals(updatedAt, review.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerName, ownerEmail, comment, star, updatedAt);
    }
}
