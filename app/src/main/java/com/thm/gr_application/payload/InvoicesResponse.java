package com.thm.gr_application.payload;

import com.google.gson.annotations.Expose;
import com.thm.gr_application.model.Invoice;
import java.util.List;

public class InvoicesResponse {
    @Expose
    private String message;

    @Expose
    private List<Invoice> data;

    public String getMessage() {
        return message;
    }

    public List<Invoice> getData() {
        return data;
    }
}
