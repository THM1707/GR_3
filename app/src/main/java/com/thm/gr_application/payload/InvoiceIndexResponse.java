package com.thm.gr_application.payload;

import com.google.gson.annotations.Expose;
import com.thm.gr_application.model.Invoice;

import java.util.List;

public class InvoiceIndexResponse {
    @Expose
    List<Invoice> active;

    @Expose
    List<Invoice> ended;

    @Expose
    List<Invoice> all;

    public List<Invoice> getActive() {
        return active;
    }

    public List<Invoice> getEnded() {
        return ended;
    }

    public List<Invoice> getAll() {
        return all;
    }
}
