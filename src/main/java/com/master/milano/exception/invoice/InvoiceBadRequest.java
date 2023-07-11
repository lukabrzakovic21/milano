package com.master.milano.exception.invoice;

public class InvoiceBadRequest extends RuntimeException{

    public InvoiceBadRequest(String message) {
        super(message);
    }
}
