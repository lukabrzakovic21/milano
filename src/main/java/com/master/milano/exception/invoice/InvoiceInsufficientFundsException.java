package com.master.milano.exception.invoice;

public class InvoiceInsufficientFundsException extends RuntimeException {
    public InvoiceInsufficientFundsException(String message) {
        super(message);
    }
}
